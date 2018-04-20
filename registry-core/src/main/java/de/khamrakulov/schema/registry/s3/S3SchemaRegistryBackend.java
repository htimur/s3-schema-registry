package de.khamrakulov.schema.registry.s3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import de.khamrakulov.schema.registry.SchemaMetadata;
import de.khamrakulov.schema.registry.SchemaRegistryBackend;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;

final public class S3SchemaRegistryBackend implements SchemaRegistryBackend {
  private final static String DEFAULT_CONTENT_TYPE = "application/octet-stream";
  private final AmazonS3 client;
  private final String bucketName;
  private final String extensionFolderName;
  private final String contentType;

  public S3SchemaRegistryBackend(AmazonS3 client, String bucketName, String extensionFolderName) {
    this(client, bucketName, extensionFolderName, DEFAULT_CONTENT_TYPE);
  }

  public S3SchemaRegistryBackend(AmazonS3 client, String bucketName, String extensionFolderName, String contentType) {
    this.client = client;
    this.bucketName = bucketName;
    this.extensionFolderName = extensionFolderName;
    this.contentType = contentType;
  }

  @Override
  public SchemaMetadata register(String subject, String schema) throws IOException {
    final byte[] objectBytes = schema.getBytes(StandardCharsets.UTF_8);
    final InputStream input = new ByteArrayInputStream(objectBytes);
    final String s3Key = getS3Key(subject);
    final ObjectMetadata md = new ObjectMetadata();
    md.setContentLength(objectBytes.length);
    md.setContentType(contentType);
    final PutObjectRequest request = new PutObjectRequest(bucketName, s3Key, input, md);
    final PutObjectResult result;
    try {
      result = client.putObject(request);
    } catch (AmazonServiceException exception) {
      String message = String.format("Error registering schema for subject %s", subject);
      throw new IOException(message, exception);
    }

    return new SchemaMetadata(subject, result.getVersionId(), schema);
  }

  private String getS3Key(String subject) {
    return String.format("%s/%s/schema", extensionFolderName, subject);
  }

  @Override
  public SchemaMetadata getBySubjectAndVersion(String subject, String version) throws IOException {
    final String schema;
    try {
      final GetObjectRequest rq = new GetObjectRequest(bucketName, getS3Key(subject), version);
      final S3Object object = client.getObject(rq);
      version = object.getObjectMetadata().getVersionId();
      schema = IOUtils.toString(object.getObjectContent());
      object.close();
    } catch (AmazonServiceException exception) {
      String message = String.format("Error retrieving schema for subject %s", subject);
      throw new IOException(message, exception);
    }

    return new SchemaMetadata(subject, version, schema);
  }

  @Override
  public SchemaMetadata getLatestSchemaMetadata(String subject) throws IOException {
    final String version;
    final String schema;
    final S3Object object;
    try {
      object = client.getObject(bucketName, getS3Key(subject));
      version = object.getObjectMetadata().getVersionId();
      schema = IOUtils.toString(object.getObjectContent());
      object.close();
    } catch (AmazonServiceException exception) {
      String message = String.format("Error retrieving schema for subject %s", subject);
      throw new IOException(message, exception);
    }

    return new SchemaMetadata(subject, version, schema);
  }

  @Override
  public String getLatestVersion(String subject) throws IOException {
    try {
      return client.getObjectMetadata(bucketName, getS3Key(subject)).getVersionId();
    } catch (AmazonServiceException exception) {
      String message = String.format("Error retrieving version for subject %s", subject);
      throw new IOException(message, exception);
    }
  }

  @Override
  public void revertToVersion(String subject, String version) throws IOException {
    try {
      final SchemaMetadata versionedMd = getBySubjectAndVersion(subject, version);
      register(versionedMd.getSubject(), versionedMd.getSchema());
    } catch (AmazonServiceException exception) {
      String message = String.format("Error reverting schema for subject %s to version %s", subject, version);
      throw new IOException(message, exception);
    }
  }

  @Override
  public Collection<String> getAllSubjects() throws IOException {
    try {
      final ListObjectsV2Result result;
      final ListObjectsV2Request req = new ListObjectsV2Request()
        .withBucketName(bucketName)
        .withPrefix(extensionFolderName);
      do {
        result = client.listObjectsV2(req);
        return result
          .getObjectSummaries()
          .stream()
          .map(S3ObjectSummary::getKey)
          .map(Paths::get)
          .map(Path::getFileName)
          .map(Path::toString)
          .collect(Collectors.toList());

      } while (result.isTruncated());
    } catch (AmazonServiceException exception) {
      String message = "Error retrieving subjects";
      throw new IOException(message, exception);
    }
  }

  @Override
  public Collection<String> getAllVersions(String subject) throws IOException {
    try {
      final ListVersionsRequest request = new ListVersionsRequest()
        .withBucketName(bucketName)
        .withPrefix(getS3Key(subject));

      VersionListing versionListing;
      do {
        versionListing = client.listVersions(request);
        return versionListing
          .getVersionSummaries()
          .stream()
          .map(S3VersionSummary::getVersionId)
          .collect(Collectors.toList());
      } while (versionListing.isTruncated());
    } catch (AmazonServiceException exception) {
      String message = "Error retrieving versions for subject " + subject;
      throw new IOException(message, exception);
    }
  }
}
