package io.github.weijunfu.id;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import io.github.weijunfu.id.util.FuIds;
import io.github.weijunfu.id.util.StringUtil;

import java.io.IOException;
import java.util.Objects;

public class FuIdDeserializer extends JsonDeserializer<Long> {

  @Override
  public Long deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException, JacksonException {
    String text = parser.getText();

    if(Objects.isNull(text) || !StringUtil.hasText(text)) {
      return null;
    }

    return FuIds.getInstance().decode(text);
  }
}
