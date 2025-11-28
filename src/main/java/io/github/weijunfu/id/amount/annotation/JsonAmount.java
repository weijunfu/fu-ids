package io.github.weijunfu.id.amount.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotation;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.weijunfu.id.amount.FuAmountDeserializer;
import io.github.weijunfu.id.amount.FuAmountSerializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义整个字段的序列化逻辑
 */
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
@JacksonAnnotationsInside
@JsonSerialize(using = FuAmountSerializer.class)
@JsonDeserialize(using = FuAmountDeserializer.class)
public @interface JsonAmount {
  int precision() default 2;
}
