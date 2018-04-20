package de.khamrakulov.schema.registry;

public class SchemaMetadata {
  private final String subject;
  private final String version;
  private final String schema;

  public SchemaMetadata(String subject, String version, String schema) {
    this.subject = subject;
    this.version = version;
    this.schema = schema;

  }

  public String getSubject() {
    return subject;
  }

  public String getVersion() {
    return version;
  }

  public String getSchema() {
    return schema;
  }
}
