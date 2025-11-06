package io.github.weijunfu.id.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.weijunfu.id.FuIdKeyDeserializer;
import io.github.weijunfu.id.FuIdKeySerializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(keyUsing = FuIdKeySerializer.class)
@JsonDeserialize(keyUsing =  FuIdKeyDeserializer.class)
public @interface JsonKeyLong {
}
