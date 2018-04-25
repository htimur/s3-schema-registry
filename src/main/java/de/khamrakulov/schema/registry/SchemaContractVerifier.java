package de.khamrakulov.schema.registry;

import java.util.List;

public interface SchemaContractVerifier<T> {
  public boolean isCompatible(T newSchema, T prevSchema);

  public boolean isCompatible(T newSchema, List<T> previousSchemas);
}
