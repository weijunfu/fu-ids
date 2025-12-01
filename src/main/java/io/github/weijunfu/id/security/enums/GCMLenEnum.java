package io.github.weijunfu.id.security.enums;

public enum GCMLenEnum {
  LEN_128(128),
  LEN_120(120),
  LEN_112(112),
  LEN_104(104),
  LEN_96(96);

  private int len;

  GCMLenEnum(int len) {
    this.len = len;
  }

  public int getLen() {
    return len;
  }
}
