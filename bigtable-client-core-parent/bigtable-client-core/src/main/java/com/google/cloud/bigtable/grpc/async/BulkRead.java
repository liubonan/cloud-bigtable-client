/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.cloud.bigtable.grpc.async;

import com.google.api.core.ApiFuture;
import com.google.api.core.SettableApiFuture;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.cloud.bigtable.core.IBigtableDataClient;
import com.google.cloud.bigtable.data.v2.internal.RequestContext;
import com.google.cloud.bigtable.data.v2.models.Filters;
import com.google.cloud.bigtable.data.v2.models.Query;
import com.google.common.base.Preconditions;
import com.google.bigtable.v2.ReadRowsRequest;
import com.google.bigtable.v2.RowFilter;
import com.google.cloud.bigtable.config.Logger;
import com.google.cloud.bigtable.grpc.BigtableTableName;
import com.google.cloud.bigtable.grpc.scanner.FlatRow;
import com.google.cloud.bigtable.grpc.scanner.ResultScanner;
import com.google.cloud.bigtable.util.ByteStringComparator;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.SettableFuture;
import com.google.protobuf.ByteString;
import java.util.concurrent.ExecutorService;

/**
 * This class combines a collection of {@link com.google.bigtable.v2.ReadRowsRequest}s with a single row key into a single
 * {@link com.google.bigtable.v2.ReadRowsRequest} with a {@link com.google.bigtable.v2.RowSet} which will result in fewer round trips. This class
 * is not thread safe, and requires calling classes to make it thread safe.
 *
 * @author sduskis
 * @version $Id: $Id
 */
public class BulkRead {

  /** Constant <code>LOG</code> */
  protected static final Logger LOG = new Logger(BulkRead.class);

  private static final Comparator<Entry<ByteString, SettableApiFuture<FlatRow>>> ENTRY_SORTER =
      new Comparator<Entry<ByteString, SettableApiFuture<FlatRow>>>() {
        @Override
        public int compare(Entry<ByteString, SettableApiFuture<FlatRow>> o1,
            Entry<ByteString, SettableApiFuture<FlatRow>> o2) {
          return ByteStringComparator.INSTANCE.compare(o1.getKey(), o2.getKey());
        }
      };

  private final IBigtableDataClient client;
  private final int batchSizes;
  private final ExecutorService threadPool;
  private final String tableId;
  private final RequestContext requestContext;

  private final Map<RowFilter, Batch> batches;

  /**
   * Constructor for BulkRead.
   * @param client a {@link IBigtableDataClient} object.
   * @param tableName a {@link BigtableTableName} object.
   * @param batchSizes The number of keys to lookup per RPC.
   * @param threadPool the {@link ExecutorService} to execute the batched reads on
   */
  public BulkRead(IBigtableDataClient client,
      BigtableTableName tableName, int batchSizes, ExecutorService threadPool) {
    this.client = client;
    this.tableId = tableName.getTableId();
    this.requestContext =
        RequestContext.create(tableName.getProjectId(), tableName.getInstanceId(), "");
    this.batchSizes = batchSizes;
    this.threadPool = threadPool;
    this.batches = new HashMap<>();
  }

  /**
   * Adds the key in the request to a batch read. The future will be resolved when the batch response
   * is received.
   *
   * @param query a {@link Query} with a single row key.
   * @return a {@link ApiFuture} that will be populated with the {@link FlatRow} that
   * corresponds to the request
   */
  public synchronized ApiFuture<FlatRow> add(Query query) {
    Preconditions.checkNotNull(query);
    ReadRowsRequest request = query.toProto(requestContext);

    Preconditions.checkArgument(request.getRows().getRowKeysCount() == 1);
    ByteString rowKey = request.getRows().getRowKeysList().get(0);
    Preconditions.checkArgument(!rowKey.equals(ByteString.EMPTY));

    final RowFilter filter = request.getFilter();
    Batch batch = batches.get(filter);
    if (batch == null) {
      batch = new Batch(filter);
      batches.put(filter, batch);
    }
    return batch.addKey(rowKey);
  }

  /**
   * Sends all remaining requests to the server. This method does not wait for the method to
   * complete.
   */
  public void flush() {
    for (Batch batch : batches.values()) {
      Collection<Batch> subbatches = batch.split();
      for (Batch miniBatch : subbatches) {
        threadPool.submit(miniBatch);
      }
    }
    batches.clear();
  }

  /**
   * ReadRowRequests have to be batched based on the {@link RowFilter} since {@link ReadRowsRequest}
   * only support a single RowFilter. A batch represents this grouping.
   */
  private class Batch implements Runnable {
    private final RowFilter filter;
    /**
     * Maps row keys to a collection of {@link SettableFuture}s that will be populated once the batch
     * operation is complete. The value of the {@link Multimap} is a {@link SettableFuture} of
     * a {@link List} of {@link FlatRow}s.  The {@link Multimap} is used because a user could request
     * the same key multiple times in the same batch. The {@link List} of {@link FlatRow}s mimics the
     * interface of {@link IBigtableDataClient#readRowsAsync(Query)}.
     */
    private final Multimap<ByteString, SettableApiFuture<FlatRow>> futures;

    Batch(RowFilter filter) {
      this.filter = filter;
      this.futures = HashMultimap.create();
    }

    Collection<Batch> split() {
      if (futures.values().size() <= batchSizes) {
        return ImmutableList.of(this);
      }
      List<Entry<ByteString, SettableApiFuture<FlatRow>>> toSplit =
          new ArrayList<>(futures.entries());
      Collections.sort(toSplit, ENTRY_SORTER);
      
      List<Batch> batches = new ArrayList<>();
      for (List<Entry<ByteString, SettableApiFuture<FlatRow>>> entries : Iterables.partition(toSplit, batchSizes)) {
        Batch batch = new Batch(filter);
        for (Entry<ByteString, SettableApiFuture<FlatRow>> entry : entries) {
          batch.futures.put(entry.getKey(), entry.getValue());
        }
        batches.add(batch);
      }
      return batches;
    }

    SettableApiFuture<FlatRow> addKey(ByteString rowKey) {
      SettableApiFuture<FlatRow> future = SettableApiFuture.create();
      futures.put(rowKey, future);
      return future;
    }

    /**
     * Sends the requests and resolves the futures using the response.
     */
    @Override
    public void run() {
      try {
        Query query = Query.create(tableId)
            .filter(Filters.FILTERS.fromProto(filter));

        for(ByteString key : futures.keys()) {
          query.rowKey(key);
        }

        ResultScanner<FlatRow> scanner = client.readFlatRows(query);
        while (true) {
          FlatRow row = scanner.next();
          if (row == null) {
            break;
          }
          Collection<SettableApiFuture<FlatRow>> rowFutures = futures.get(row.getRowKey());
          if (rowFutures != null) {
            for (SettableApiFuture<FlatRow> rowFuture : rowFutures) {
              rowFuture.set(row);
            }
            futures.removeAll(row.getRowKey());
          } else {
            LOG.warn("Found key: %s, but it was not in the original request.", row.getRowKey());
          }
        }
        // Deal with remaining/missing keys
        for (Entry<ByteString, SettableApiFuture<FlatRow>> entry : futures.entries()) {
          entry.getValue().set(null);
        }
      } catch (Throwable e) {
        for (Entry<ByteString, SettableApiFuture<FlatRow>> entry : futures.entries()) {
          entry.getValue().setException(e);
        }
      }
    }
  }

  public int getBatchSizes() {
    return batchSizes;
  }
}
