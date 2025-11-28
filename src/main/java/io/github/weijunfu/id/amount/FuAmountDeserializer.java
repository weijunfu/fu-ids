package io.github.weijunfu.id.amount;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import io.github.weijunfu.id.amount.annotation.JsonAmount;
import io.github.weijunfu.id.util.NumberUtil;

import java.io.IOException;

public class FuAmountDeserializer extends JsonDeserializer<Double> implements ContextualDeserializer {

  private final Integer precision;

  public FuAmountDeserializer() {
    this.precision = null;
  }

  public FuAmountDeserializer(Integer precision) {
    this.precision = precision;
  }

  @Override
  public Double deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    if (p.getCurrentToken().isNumeric()) {
      double value = p.getValueAsDouble();

      int scale = (precision != null) ? precision : 2;

      return Double.valueOf(NumberUtil.format(value, scale));
    }
    return null;
  }

  @Override
  public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
    if (property != null) {
      JsonAmount ann = property.getAnnotation(JsonAmount.class);
      if (ann != null) {
        return new FuAmountDeserializer(ann.precision());
      }

      // 默认两位小数
      return new FuAmountDeserializer(2);
    }
    return this;
  }
}