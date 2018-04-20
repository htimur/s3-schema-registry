package de.khamrakulov.schema.registry.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import de.khamrakulov.schema.registry.SchemaMetadata;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class S3SchemaRegistryBackendTest {
  private final static String bucketName = "test-bucket";
  private final static String folderPrefix = "testPrefix";
  private final static String schema1 = "test schema";

  private S3SchemaRegistryBackend backend;
  private AmazonS3 client;

  @Before
  public void setup() {
    client = mock(AmazonS3.class);
    backend = new S3SchemaRegistryBackend(client, bucketName, folderPrefix);
  }

  @Test
  public void theBackendShouldBeAbleToRegisterNewSchemaTest() throws IOException {
    String testVersion = "1";
    PutObjectResult putResult = new PutObjectResult() {{
      setVersionId(testVersion);
    }};
    when(client.putObject(any(PutObjectRequest.class))).thenReturn(putResult);

    String testSubject = "testSubject";
    SchemaMetadata result = backend.register(testSubject, schema1);
    assertEquals(result.getSchema(), schema1);
    assertEquals(result.getSubject(), testSubject);
    assertEquals(result.getVersion(), testVersion);

    verify(client).putObject(any(PutObjectRequest.class));
  }

  @Test(expected = IOException.class)
  public void onRegistrationErrorTheIOExceptionShouldBeThrown() throws IOException {
    String testExceptionMessage = "test exception";
    when(client.putObject(any(PutObjectRequest.class))).thenThrow(new AmazonS3Exception(testExceptionMessage));

    String testSubject = "testSubject";
    backend.register(testSubject, schema1);
  }

  @Test
  public void getBySubjectAndVersion() {

  }

  @Test
  public void getLatestSchemaMetadata() {
  }

  @Test
  public void getLatestVersion() {
  }

  @Test
  public void revertToVersion() {
  }

  @Test
  public void getAllSubjects() {
  }

  @Test
  public void getAllVersions() {
  }
}
