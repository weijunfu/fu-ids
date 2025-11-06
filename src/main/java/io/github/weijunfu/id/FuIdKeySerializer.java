package io.github.weijunfu.id;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.github.weijunfu.id.util.FuIds;

import java.io.IOException;
import java.util.Objects;

/**
 * Map key: Long 序列化类
 */
public class FuIdKeySerializer extends StdSerializer<Long> {

  public FuIdKeySerializer() {
    super(Long.class);
  }

  protected FuIdKeySerializer(Class<Long> t) {
    super(t);
  }

  @Override
  public void serialize(Long value, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
    if(Objects.isNull(value)) {
      gen.writeNull();
      return;
    }

    gen.writeFieldName(FuIds.getInstance().encode(value));
  }
}
