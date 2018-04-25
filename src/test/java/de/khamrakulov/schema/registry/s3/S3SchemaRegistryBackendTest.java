package de.khamrakulov.schema.registry.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.BucketVersioningConfiguration;
import com.amazonaws.services.s3.model.SetBucketVersioningConfigurationRequest;
import de.khamrakulov.schema.registry.SchemaMetadata;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

public class S3SchemaRegistryBackendTest {
  private final static String bucketName = "test-bucket";
  private final static String folderPrefix = "testPrefix";
  private final static String schema1 = "test schema";

  private S3SchemaRegistryBackend backend;

  @Rule
  public LocalStackContainer localstack = new LocalStackContainer().withServices(S3);

  @Before
  public void setup() {
    AmazonS3 client = AmazonS3ClientBuilder
      .standard()
      .withEndpointConfiguration(localstack.getEndpointConfiguration(S3))
      .withCredentials(localstack.getDefaultCredentialsProvider())
      .build();

    client.createBucket(bucketName);

    BucketVersioningConfiguration configuration =
      new BucketVersioningConfiguration().withStatus("Enabled");

    SetBucketVersioningConfigurationRequest setBucketVersioningConfigurationRequest =
      new SetBucketVersioningConfigurationRequest(bucketName, configuration);

    client.setBucketVersioningConfiguration(setBucketVersioningConfigurationRequest);


    backend = new S3SchemaRegistryBackend(client, bucketName, folderPrefix);
  }

  @Test
  public void shouldBeAbleToRegisterNewSchemaTest() throws IOException {
    String testSubject = "testSubject";
    SchemaMetadata result = backend.register(testSubject, schema1);

    assertEquals(result.getSchema(), schema1);
    assertEquals(result.getSubject(), testSubject);
    assertNotNull(result.getVersion());
  }

  @Test
  public void shouldBeAbleToCheckIfSubjectIsRegistered() throws IOException {
    String testSubject = "testSubject";
    backend.register(testSubject, schema1);

    assertTrue(backend.isSubjectRegistered(testSubject));
    assertFalse(backend.isSubjectRegistered("notRegistered"));
  }

  @Test
  public void shouldReturnCorrectSchemaBySubjectAndVersion() throws IOException {
    String testSubject = "testSubject";
    SchemaMetadata result = backend.register(testSubject, schema1);
    SchemaMetadata current = backend.getBySubjectAndVersion(testSubject, result.getVersion());

    assertEquals(current.getSubject(), testSubject);
    assertEquals(current.getSchema(), schema1);
  }

  @Test
  public void shouldGetLatestSchemaMetadata() throws IOException {
    String testSubject = "testSubject";
    backend.register(testSubject, schema1);
    String newSchema = schema1 + " edit";
    SchemaMetadata result = backend.register(testSubject, newSchema);
    SchemaMetadata current = backend.getLatestSchemaMetadata(testSubject);

    assertEquals(current.getSchema(), newSchema);
    assertEquals(current.getSubject(), testSubject);
    assertEquals(result.getVersion(), current.getVersion());
  }

  @Test
  public void shouldGetLatestVersion() throws IOException {
    String testSubject = "testSubject";
    backend.register(testSubject, schema1);
    String newSchema = schema1 + " edit";
    SchemaMetadata result = backend.register(testSubject, newSchema);
    String currentVersion = backend.getLatestVersion(testSubject);

    assertEquals(result.getVersion(), currentVersion);
  }

  @Test
  public void shouldBeAbleToRevertToVersion() throws IOException {
    String testSubject = "testSubject";

    SchemaMetadata result1 = backend.register(testSubject, schema1);
    String newSchema = schema1 + " edit";

    SchemaMetadata result2 = backend.register(testSubject, newSchema);
    backend.revertToVersion(testSubject, result1.getVersion());

    SchemaMetadata current = backend.getLatestSchemaMetadata(testSubject);

    assertNotEquals(result2.getVersion(), current.getVersion());
    assertEquals(result1.getSchema(), schema1);
  }

  @Test
  public void shouldReturnAllSubjects() throws IOException {
    String testSubject1 = "testSubject1";
    String testSubject2 = "testSubject2";
    String testSubject3 = "testSubject3";

    backend.register(testSubject1, schema1);
    backend.register(testSubject2, schema1);
    backend.register(testSubject3, schema1);

    List<String> subjects = backend.getAllSubjects();
    assertEquals(Arrays.asList(testSubject1, testSubject2, testSubject3), subjects);
  }

  @Test
  @Ignore
  public void shouldReturnAllVersions() throws IOException {
    String testSubject = "testSubject";
    SchemaMetadata result1 = backend.register(testSubject, schema1);
    SchemaMetadata result2 = backend.register(testSubject, schema1);
    SchemaMetadata result3 = backend.register(testSubject, schema1);

    List<String> versions = backend.getAllVersions(testSubject);
    assertEquals(Arrays.asList(result1.getVersion(), result2.getVersion(), result3.getVersion()), versions);
  }
}
