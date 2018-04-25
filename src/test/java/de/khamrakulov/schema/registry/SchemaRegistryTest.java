package de.khamrakulov.schema.registry;

import de.khamrakulov.schema.registry.avro.AvroSchemaContractVerifier;
import de.khamrakulov.schema.registry.avro.AvroSchemaParser;
import org.apache.avro.Schema;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class SchemaRegistryTest {

  private static final String firstSchemaText = getResourceFileAsString("schemas/avro/userInfo.avsc");
  private static final Schema firstSchema = new Schema.Parser().parse(Objects.requireNonNull(firstSchemaText));

  private static final String compatibleSchemaText = getResourceFileAsString("schemas/avro/userInfo_compatible.avsc");
  private static final Schema compatibleSchema = new Schema.Parser().parse(Objects.requireNonNull(compatibleSchemaText));

  private static final String incompatibleSchemaText = getResourceFileAsString("schemas/avro/userInfo_incompatible.avsc");
  private static final Schema incompatibleSchema = new Schema.Parser().parse(Objects.requireNonNull(incompatibleSchemaText));

  private SchemaRegistry<Schema> schemaRegistry;
  private SchemaRegistryBackend backend;
  private AvroSchemaContractVerifier fullChecker;

  @Before
  public void setUp() {
    backend = mock(SchemaRegistryBackend.class);
    AvroSchemaParser avroSchemaParser = new AvroSchemaParser();
    fullChecker = mock(AvroSchemaContractVerifier.class);
    schemaRegistry = new SchemaRegistry<>(backend, fullChecker, avroSchemaParser);
  }

  @Test
  public void shouldRegisterNewSchemaWithoutChecksFirstTime() throws IOException, SchemaCompatibilityException {
    when(backend.isSubjectRegistered(anyString())).thenReturn(false);
    SchemaMetadata returnResult = new SchemaMetadata("1", "1", "1");
    when(backend.register(anyString(), anyString())).thenReturn(returnResult);

    SchemaMetadata result = schemaRegistry.register("test", firstSchema);

    assertEquals(result, returnResult);

    verify(backend).register(anyString(), anyString());
    verify(fullChecker, never()).isCompatible(any(Schema.class), any(Schema.class));
  }

  @Test
  public void shouldRegisterNewSchemaIfItsCompatible() throws IOException, SchemaCompatibilityException {
    SchemaMetadata returnResult = new SchemaMetadata("1", "1", "1");

    when(backend.isSubjectRegistered(anyString())).thenReturn(true);
    when(backend.register(anyString(), anyString())).thenReturn(returnResult);
    when(backend.getLatestSchemaMetadata(anyString())).thenReturn(new SchemaMetadata("1", "1", compatibleSchemaText));
    when(fullChecker.isCompatible(any(Schema.class), any(Schema.class))).thenReturn(true);

    SchemaMetadata result = schemaRegistry.register("test", compatibleSchema);

    assertEquals(result, returnResult);

    verify(backend).register(anyString(), anyString());
    verify(fullChecker).isCompatible(any(Schema.class), any(Schema.class));
  }

  @Test(expected = SchemaCompatibilityException.class)
  public void shouldFailToRegisterIfSchemasAreIncompatible() throws IOException, SchemaCompatibilityException {
    SchemaMetadata returnResult = new SchemaMetadata("1", "1", "1");

    when(backend.isSubjectRegistered(anyString())).thenReturn(true);
    when(backend.register(anyString(), anyString())).thenReturn(returnResult);
    when(backend.getLatestSchemaMetadata(anyString())).thenReturn(new SchemaMetadata("1", "1", firstSchemaText));

    when(fullChecker.isCompatible(any(Schema.class), any(Schema.class))).thenReturn(false);

    schemaRegistry.register("test", incompatibleSchema);

    verify(backend).register(anyString(), anyString());
    verify(fullChecker).isCompatible(any(Schema.class), any(Schema.class));
  }

  @Test
  public void shouldBeAbleToGetLatestSchema() throws IOException {
    SchemaMetadata returnResult = new SchemaMetadata("1", "1", firstSchemaText);
    when(backend.getLatestSchemaMetadata(anyString())).thenReturn(returnResult);

    assertEquals(firstSchema, schemaRegistry.getSchema("1"));

    verify(backend).getLatestSchemaMetadata(anyString());
  }

  /**
   * Reads given resource file as a string.
   *
   * @param fileName the path to the resource file
   * @return the file's contents or null if the file could not be opened
   */
  private static String getResourceFileAsString(String fileName) {

    try {
      InputStream is = SchemaRegistryTest.class.getClassLoader().getResourceAsStream(fileName);
      if (is != null) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
        return reader.lines().collect(Collectors.joining(System.lineSeparator()));
      }
    } catch (UnsupportedEncodingException ignored) {

    }
    return null;
  }

}