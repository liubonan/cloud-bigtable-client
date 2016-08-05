// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: google/bigtable/v1/bigtable_service.proto

package com.google.bigtable.v1;

public final class BigtableServicesProto {
  private BigtableServicesProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n)google/bigtable/v1/bigtable_service.pr" +
      "oto\022\022google.bigtable.v1\032\034google/api/anno" +
      "tations.proto\032&google/bigtable/v1/bigtab" +
      "le_data.proto\0322google/bigtable/v1/bigtab" +
      "le_service_messages.proto\032\033google/protob" +
      "uf/empty.proto2\335\010\n\017BigtableService\022\245\001\n\010R" +
      "eadRows\022#.google.bigtable.v1.ReadRowsReq" +
      "uest\032$.google.bigtable.v1.ReadRowsRespon" +
      "se\"L\202\323\344\223\002F\"A/v1/{table_name=projects/*/z" +
      "ones/*/clusters/*/tables/*}/rows:read:\001*",
      "0\001\022\267\001\n\rSampleRowKeys\022(.google.bigtable.v" +
      "1.SampleRowKeysRequest\032).google.bigtable" +
      ".v1.SampleRowKeysResponse\"O\202\323\344\223\002I\022G/v1/{" +
      "table_name=projects/*/zones/*/clusters/*" +
      "/tables/*}/rows:sampleKeys0\001\022\243\001\n\tMutateR" +
      "ow\022$.google.bigtable.v1.MutateRowRequest" +
      "\032\026.google.protobuf.Empty\"X\202\323\344\223\002R\"M/v1/{t" +
      "able_name=projects/*/zones/*/clusters/*/" +
      "tables/*}/rows/{row_key}:mutate:\001*\022\252\001\n\nM" +
      "utateRows\022%.google.bigtable.v1.MutateRow",
      "sRequest\032&.google.bigtable.v1.MutateRows" +
      "Response\"M\202\323\344\223\002G\"B/v1/{table_name=projec" +
      "ts/*/zones/*/clusters/*/tables/*}:mutate" +
      "Rows:\001*\022\322\001\n\021CheckAndMutateRow\022,.google.b" +
      "igtable.v1.CheckAndMutateRowRequest\032-.go" +
      "ogle.bigtable.v1.CheckAndMutateRowRespon" +
      "se\"`\202\323\344\223\002Z\"U/v1/{table_name=projects/*/z" +
      "ones/*/clusters/*/tables/*}/rows/{row_ke" +
      "y}:checkAndMutate:\001*\022\277\001\n\022ReadModifyWrite" +
      "Row\022-.google.bigtable.v1.ReadModifyWrite",
      "RowRequest\032\027.google.bigtable.v1.Row\"a\202\323\344" +
      "\223\002[\"V/v1/{table_name=projects/*/zones/*/" +
      "clusters/*/tables/*}/rows/{row_key}:read" +
      "ModifyWrite:\001*B4\n\026com.google.bigtable.v1" +
      "B\025BigtableServicesProtoP\001\210\001\001b\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.api.AnnotationsProto.getDescriptor(),
          com.google.bigtable.v1.BigtableDataProto.getDescriptor(),
          com.google.bigtable.v1.BigtableServiceMessagesProto.getDescriptor(),
          com.google.protobuf.EmptyProto.getDescriptor(),
        }, assigner);
    com.google.protobuf.ExtensionRegistry registry =
        com.google.protobuf.ExtensionRegistry.newInstance();
    registry.add(com.google.api.AnnotationsProto.http);
    com.google.protobuf.Descriptors.FileDescriptor
        .internalUpdateFileDescriptor(descriptor, registry);
    com.google.api.AnnotationsProto.getDescriptor();
    com.google.bigtable.v1.BigtableDataProto.getDescriptor();
    com.google.bigtable.v1.BigtableServiceMessagesProto.getDescriptor();
    com.google.protobuf.EmptyProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}