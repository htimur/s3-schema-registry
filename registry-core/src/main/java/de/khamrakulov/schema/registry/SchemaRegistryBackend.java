package de.khamrakulov.schema.registry;

import java.io.IOException;
import java.util.Collection;

public interface SchemaRegistryBackend {
  public SchemaMetadata register(String subject, String schema) throws IOException;

  public SchemaMetadata getBySubjectAndVersion(String subject, String version) throws IOException;

  public SchemaMetadata getLatestSchemaMetadata(String subject) throws IOException;

  public String getLatestVersion(String subject) throws IOException;

  public void revertToVersion(String subject, String version) throws IOException;

  public Collection<String> getAllSubjects() throws IOException;

  public Collection<String> getAllVersions(String subject) throws IOException;
}
