package de.khamrakulov.schema.registry.avro;

import de.khamrakulov.schema.registry.SchemaParser;
import org.apache.avro.Schema;

public class AvroSchemaParser implements SchemaParser<Schema> {
  @Override
  public Schema parse(String schema) {
    return new Schema.Parser().parse(schema);
  }
}
