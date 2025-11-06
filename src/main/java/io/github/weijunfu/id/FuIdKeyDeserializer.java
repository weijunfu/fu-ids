package io.github.weijunfu.id;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import io.github.weijunfu.id.util.FuIds;
import io.github.weijunfu.id.util.StringUtil;

import java.io.IOException;
import java.util.Objects;

/**
 * Map key: Long 反序列化
 */
public class FuIdKeyDeserializer extends KeyDeserializer {

  public FuIdKeyDeserializer() {}

  @Override
  public Object deserializeKey(String key, DeserializationContext deserializationContext) throws IOException {

    if(Objects.isNull(key) || !StringUtil.hasText(key)) {
      return null;
    }

    return FuIds.getInstance().decode(key);
  }
}
