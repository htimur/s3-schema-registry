package de.khamrakulov.schema.registry.avro;

import de.khamrakulov.schema.registry.SchemaParser;
import org.apache.avro.Schema;

public class AvroSchemaParser implements SchemaParser<Schema> {
  private static Schema.Parser parser = new Schema.Parser();

  @Override
  public Schema parse(String schema) {
    return parser.parse(schema);
  }
}
