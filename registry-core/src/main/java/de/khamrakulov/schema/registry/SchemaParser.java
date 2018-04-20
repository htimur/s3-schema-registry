package de.khamrakulov.schema.registry;

public interface SchemaParser<T> {
  public T parse(String schema);
}
