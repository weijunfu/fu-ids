package io.github.weijunfu.id.amount;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import io.github.weijunfu.id.amount.annotation.JsonAmount;
import io.github.weijunfu.id.util.NumberUtil;
import io.github.weijunfu.id.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class FuAmountDeserializer extends JsonDeserializer<Double> implements ContextualDeserializer {

  private final static Logger log = LoggerFactory.getLogger(FuAmountDeserializer.class);

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

    String val = p.getValueAsString();
    log.warn("format string double: {}", val);

    if(StringUtil.isNumeric(val)) { // 支持值为字符串的数字
      int scale = (precision != null) ? precision : 2;
      return Double.valueOf(NumberUtil.format(Double.valueOf(val), scale));
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