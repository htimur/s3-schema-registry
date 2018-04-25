package de.khamrakulov.schema.registry;

import java.io.IOException;

public class SchemaRegistry<T> {
  private final SchemaRegistryBackend backend;
  private final SchemaContractVerifier<T> contractVerifier;
  private final SchemaParser<T> parser;

  public SchemaRegistry(SchemaRegistryBackend backend, SchemaContractVerifier<T> contractVerifier, SchemaParser<T> parser) {
    this.backend = backend;
    this.contractVerifier = contractVerifier;
    this.parser = parser;
  }

  public SchemaMetadata register(String subject, T schema) throws IOException, SchemaCompatibilityException {
    if (backend.isSubjectRegistered(subject)) {
      final SchemaMetadata currentMd = backend.getLatestSchemaMetadata(subject);
      final T currentSchema = parser.parse(currentMd.getSchema());
      if (contractVerifier.isCompatible(schema, currentSchema)) {
        return backend.register(subject, schema.toString());
      } else {
        throw new SchemaCompatibilityException("New schema is not compatible with latest registered schema.");
      }
    } else {
      return backend.register(subject, schema.toString());
    }
  }

  public T getSchema(String subject) throws IOException {
    final String schema = backend.getLatestSchemaMetadata(subject).getSchema();
    return parser.parse(schema);
  }
}
