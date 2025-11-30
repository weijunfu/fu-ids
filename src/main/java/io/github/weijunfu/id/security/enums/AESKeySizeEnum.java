package io.github.weijunfu.id.security.enums;

/**
 * AES密钥长度枚举类
 * 定义了AES加密算法支持的密钥长度选项
 */
public enum AESKeySizeEnum {
  K_128(128), K_256(256);

  private int keySize;

  /**
   * 构造函数
   * @param keySize 密钥长度值
   */
  AESKeySizeEnum(int keySize) {
    this.keySize = keySize;
  }

  /**
   * 获取密钥长度
   * @return 返回密钥长度值
   */
  public int getKeySize() {
    return keySize;
  }
}


