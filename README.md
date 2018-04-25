[![Download](https://api.bintray.com/packages/htimur/maven/s3-schema-registry/images/download.svg)](https://bintray.com/htimur/maven/s3-schema-registry/_latestVersion)

# Schema registry based on AWS S3

Lightweight schema registry implementation with AWS S3 service as the storage engine.

# The idea

To reduce the operational overhead hosting solutions like "Confluent Schema Registry", the idea is to delegate the schema validation to the data producer (schema owner) on client side during the CI/CD flow. This insures the required compatibility level and consumers can download the latest schema using the client on the proper stage of the process.

![alt text](https://github.com/htimur/s3-schema-registry/raw/master/docs/process.png "Solution design")

## Supported formats

* Avro - avro schema validation is supported in the core package.

Other format support can be added, by implementing the `SchemaContractVerifier`, `SchemaParser` interfaces.

## Backends

Theoretically any storage engine can be used as a backend. The `SchemaRegistryBackend` should be implemented for this.

* S3 - core package contains backend implementation for the AWS S3 service.

The following object structure will be crated by the backend
```
. s3 bucket
  |- project folder (prefix)
       |- subject 1
       |- subject 2
       .
       .
       |- subject n 

```

## Installation

Maven
```xml
<dependency>
  <groupId>de.khamrakulov.schema-registry</groupId>
  <artifactId>registry-core</artifactId>
  <version>1.0.1</version>
  <type>pom</type>
</dependency>
```

Gradle
```groovy
compile 'de.khamrakulov.schema-registry:registry-core:1.0.1'
```

SBT
```scala
libraryDependencies += "de.khamrakulov.schema-registry" % "registry-core" % "1.0.1"
```

### AWS CloudFormation template

In the `stack` folder, can be found an AWS stack definition, that can be used to create a versioned S3 bucket.

### Java publish example

```java
import de.khamrakulov.schema.registry.*;
import de.khamrakulov.schema.registry.avro.*;
import de.khamrakulov.schema.registry.s3.*;

public class Main {
  public static void main(String[] args) {
    AmazonS3 client = ...;
    Schema mySchema = ...;
    
    String bucketName = "test-bucket";
    String extensionFolderName = "my-project-name";
    SchemaRegistryBackend backend = new S3SchemaRegistryBackend(client, bucketName, extensionFolderName);
    SchemaParser parser = new AvroSchemaParser();
    SchemaContractVerifier verifier = AvroSchemaContractVerifier.FULL_CHECKER;
    
    SchemaRegistry<Schema> registry = new SchemaRegistry<>(backend, verifier, parser);
    
    registry.register("mySchemaSubject", mySchema);
  }
}
```

### Scala publish example

```scala
import de.khamrakulov.schema.registry._
import de.khamrakulov.schema.registry.avro._
import de.khamrakulov.schema.registry.s3._

object Main {
  def main(args: Array[String]) {
    val s3Client: AmazonS3 = ...
    val mySchema: Schema = ...
    
    val bucketName = "test-bucket"
    val extensionFolderName = "my-project-name"
    val backend = new S3SchemaRegistryBackend(client, bucketName, extensionFolderName)
    val parser = new AvroSchemaParser()
    val verifier = AvroSchemaContractVerifier.FULL_CHECKER
    
    val registry = new SchemaRegistry[Schema](backend, verifier, parser)
    
    registry.register("mySchemaSubject", mySchema)
  }
}
```