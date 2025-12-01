package io.github.weijunfu.id.security.util;

import io.github.weijunfu.id.util.StringUtil;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64Util {

  public static byte[] encode(byte[] bytes){
    if (bytes == null) {
      throw new IllegalArgumentException("bytes cannot be null");
    }
    return Base64.getEncoder().encode(bytes);
  }

  public static String encodeToString(byte[] bytes) {
    if (bytes == null) {
      throw new IllegalArgumentException("bytes cannot be null or empty");
    }

    return Base64.getEncoder().encodeToString(bytes);
  }

  public static byte[] decode(byte[] bytes) {
    if (bytes == null) {
      throw new IllegalArgumentException("bytes cannot be null");
    }
    return Base64.getDecoder().decode(bytes);
  }

  public static String decodeToString(String base64) {
    if (!StringUtil.hasText(base64)) {
      throw new IllegalArgumentException("base64 cannot be null or empty");
    }

    return new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
  }

  public static void main(String[] args) {
    String originalText = "Hello, World!";
    byte[] bytes = originalText.getBytes(StandardCharsets.UTF_8);
;
    String encodedText = encodeToString(bytes);
    String decodedText = decodeToString(encodedText);

    System.out.println("原始文本: " + originalText);
    System.out.println("编码后的文本: " + encodedText);
    System.out.println("解码后的文本: " + decodedText);
  }
}
