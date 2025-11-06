package io.github.weijunfu.id.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.weijunfu.id.FuIdDeserializer;
import io.github.weijunfu.id.FuIdSerializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义集合中每个元素的序列化逻辑
 */
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(contentUsing = FuIdSerializer.class)
@JsonDeserialize(contentUsing =  FuIdDeserializer.class)
public @interface JsonContentLong {
}
