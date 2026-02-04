package io.github.weijunfu.id.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义字段注解
 * @author weijunfu
 */
@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.FIELD)
public @interface FuField {

  /**
   * 字段权重，默认权重为 0
   * @return int
   */
  int weight() default 0;

  /**
   * 字段格式化, 默认为空, 例如："yyyy-MM-dd"
   * @return String
   */
  String pattern() default "";
}
