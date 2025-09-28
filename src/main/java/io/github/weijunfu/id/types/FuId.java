package io.github.weijunfu.id.types;

import io.github.weijunfu.id.util.FuIds;

import java.util.Objects;

public class FuId {

  private final String value;

  public FuId(String value) {
    this.value = value;
  }

  public Long getValue() {

    if(Objects.isNull(value) || value.trim().length() == 0) {
      return null;
    }

    return FuIds.getInstance().decode(value);
  }
}
