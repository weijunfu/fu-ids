package io.github.weijunfu.id.types;

import io.github.weijunfu.id.util.FuIds;
import io.github.weijunfu.id.util.StringUtil;

import java.util.Objects;

/**
 * FuId 用于定义参数类型
 */
public class FuId {

  private final String value;

  public FuId(String value) {
    this.value = value;
  }

  public Long getValue() {

    if(Objects.isNull(value) || !StringUtil.hasText(value)) {
      return null;
    }

    return FuIds.getInstance().decode(value);
  }
}
