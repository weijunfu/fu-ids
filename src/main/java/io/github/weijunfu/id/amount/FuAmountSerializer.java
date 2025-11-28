package io.github.weijunfu.id.amount;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import io.github.weijunfu.id.amount.annotation.JsonAmount;
import io.github.weijunfu.id.util.NumberUtil;

import java.io.IOException;

public class FuAmountSerializer extends JsonSerializer<Double> implements ContextualSerializer {

  private final Integer precision;

  public FuAmountSerializer() {
    this.precision = null;
  }

  public FuAmountSerializer(Integer precision) {
    this.precision = precision;
  }

  @Override
  public void serialize(Double value, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
    if (value == null) {
      gen.writeNull();
      return;
    }

    int scale = (precision != null) ? precision : 2; // 默认保留两位小数

    gen.writeNumber(NumberUtil.format(value, scale));
  }

  @Override
  public JsonSerializer<Double> createContextual(SerializerProvider serializerProvider, BeanProperty property) throws JsonMappingException {
    System.out.println("FuAmountSerializer");
    if (property != null) {
      JsonAmount ann = property.getAnnotation(JsonAmount.class);
      if (ann != null) {
        return new FuAmountSerializer(ann.precision());
      }

      // 默认两位小数
      return new FuAmountSerializer(2);
    }
    return this; // 无注解时返回默认实例
  }
}
