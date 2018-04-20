package de.khamrakulov.schema.registry;

public interface SchemaContractVerifier<T> {
  public boolean isCompatible(T newSchema, T prevSchema);
}
