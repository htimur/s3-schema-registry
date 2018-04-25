package de.khamrakulov.schema.registry;

import java.io.IOException;
import java.util.List;

public interface SchemaRegistryBackend {
  public boolean isSubjectRegistered(String subject);

  public SchemaMetadata register(String subject, String schema) throws IOException;

  public SchemaMetadata getBySubjectAndVersion(String subject, String version) throws IOException;

  public SchemaMetadata getLatestSchemaMetadata(String subject) throws IOException;

  public String getLatestVersion(String subject) throws IOException;

  public SchemaMetadata revertToVersion(String subject, String version) throws IOException;

  public List<String> getAllSubjects() throws IOException;

  public List<String> getAllVersions(String subject) throws IOException;
}
