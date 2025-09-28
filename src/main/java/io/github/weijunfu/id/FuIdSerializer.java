package io.github.weijunfu.id;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.github.weijunfu.id.util.FuIds;

import java.io.IOException;
import java.util.Objects;

public class FuIdSerializer extends JsonSerializer<Long> {

  @Override
  public void serialize(Long value, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
    if(Objects.isNull(value)) {
      gen.writeNull();
      return;
    }

    gen.writeString(FuIds.getInstance().encode(value));
  }
}
