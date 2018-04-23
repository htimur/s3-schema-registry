package de.khamrakulov.schema.registry.avro;

import org.apache.avro.SchemaValidationException;
import org.apache.avro.SchemaValidator;
import org.apache.avro.SchemaValidatorBuilder;
import de.khamrakulov.schema.registry.SchemaContractVerifier;
import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AvroSchemaContractVerifier implements SchemaContractVerifier<Schema> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AvroSchemaContractVerifier.class);

  // Check if the new schema can be used to read data produced by the previous schema
  private static final SchemaValidator BACKWARD_VALIDATOR =
    new SchemaValidatorBuilder().canReadStrategy().validateLatest();
  public static final AvroSchemaContractVerifier BACKWARD_CHECKER
    = new AvroSchemaContractVerifier(BACKWARD_VALIDATOR);

  // Check if data produced by the new schema can be read by the previous schema
  private static final SchemaValidator FORWARD_VALIDATOR =
    new SchemaValidatorBuilder().canBeReadStrategy().validateLatest();
  public static final AvroSchemaContractVerifier FORWARD_CHECKER
    = new AvroSchemaContractVerifier(FORWARD_VALIDATOR);

  // Check if the new schema is both forward and backward compatible with the previous schema
  private static final SchemaValidator FULL_VALIDATOR =
    new SchemaValidatorBuilder().mutualReadStrategy().validateLatest();
  public static final AvroSchemaContractVerifier FULL_CHECKER
    = new AvroSchemaContractVerifier(FULL_VALIDATOR);

  // Check if the new schema can be used to read data produced by all earlier schemas
  private static final SchemaValidator BACKWARD_TRANSITIVE_VALIDATOR =
    new SchemaValidatorBuilder().canReadStrategy().validateAll();
  public static final AvroSchemaContractVerifier BACKWARD_TRANSITIVE_CHECKER
    = new AvroSchemaContractVerifier(BACKWARD_TRANSITIVE_VALIDATOR);

  // Check if data produced by the new schema can be read by all earlier schemas
  private static final SchemaValidator FORWARD_TRANSITIVE_VALIDATOR =
    new SchemaValidatorBuilder().canBeReadStrategy().validateAll();
  public static final AvroSchemaContractVerifier FORWARD_TRANSITIVE_CHECKER
    = new AvroSchemaContractVerifier(FORWARD_TRANSITIVE_VALIDATOR);

  // Check if the new schema is both forward and backward compatible with all earlier schemas
  private static final SchemaValidator FULL_TRANSITIVE_VALIDATOR =
    new SchemaValidatorBuilder().mutualReadStrategy().validateAll();
  public static final AvroSchemaContractVerifier FULL_TRANSITIVE_CHECKER
    = new AvroSchemaContractVerifier(FULL_TRANSITIVE_VALIDATOR);

  private static final SchemaValidator NO_OP_VALIDATOR = new SchemaValidator() {
    @Override
    public void validate(Schema schema, Iterable<Schema> schemas) throws SchemaValidationException {
      // do nothing
    }
  };
  public static final AvroSchemaContractVerifier NO_OP_CHECKER = new AvroSchemaContractVerifier(
    NO_OP_VALIDATOR);

  private final SchemaValidator validator;

  private AvroSchemaContractVerifier(SchemaValidator validator) {
    this.validator = validator;
  }

  /**
   * Check the compatibility between the new schema and the latest schema
   */
  @Override
  public boolean isCompatible(Schema newSchema, Schema latestSchema) {
    return isCompatible(newSchema, Collections.singletonList(latestSchema));
  }

  /**
   * Check the compatibility between the new schema and the specified schemas
   *
   * @param previousSchemas Full schema history in chronological order
   */
  public boolean isCompatible(Schema newSchema, List<Schema> previousSchemas) {
    List<Schema> previousSchemasCopy = new ArrayList<>(previousSchemas);
    try {
      // Validator checks in list order, but checks should occur in reverse chronological order
      Collections.reverse(previousSchemasCopy);
      validator.validate(newSchema, previousSchemasCopy);
    } catch (SchemaValidationException e) {
      LOGGER.error("Schema validation error", e);
      return false;
    }

    return true;
  }
}
