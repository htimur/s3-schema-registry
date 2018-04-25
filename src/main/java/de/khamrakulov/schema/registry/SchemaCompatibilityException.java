package de.khamrakulov.schema.registry;

public class SchemaCompatibilityException extends Exception {
  public SchemaCompatibilityException(String message) {
    super(message);
  }

  public SchemaCompatibilityException(String message, Throwable cause) {
    super(message, cause);
  }
}
