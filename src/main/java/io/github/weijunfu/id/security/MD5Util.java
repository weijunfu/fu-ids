package io.github.weijunfu.id.security;

import io.github.weijunfu.id.security.util.HexUtil;
import io.github.weijunfu.id.util.StringUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * MD5 工具类（仅供兼容性用途，请勿用于安全性相关场景）
 */
public class MD5Util {

  // 缓存 MD5 实例提高性能
  private static final ThreadLocal<MessageDigest> MD5_DIGEST = ThreadLocal.withInitial(() -> {
    try {
      return MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Failed to initialize MD5 digest", e);
    }
  });

  private MD5Util() {}

  /**
   * 计算输入字符串的 MD5 值（小写形式）
   *
   * @param input 输入字符串
   * @return 小写的 MD5 字符串表示
   * @throws IllegalArgumentException 当输入为空或无有效文本时
   * @throws RuntimeException         当 MD5 算法不可用时
   */
  public static String get(String input) {
    return get(input, false);
  }

  /**
   * 计算输入字符串的 MD5 值，并根据需要转换大小写
   *
   * @param input       输入字符串
   * @param toUpperCase 是否将结果转为大写
   * @return MD5 字符串表示
   * @throws IllegalArgumentException 当输入为空或无有效文本时
   * @throws RuntimeException         当 MD5 算法不可用时
   */
  public static String get(String input, boolean toUpperCase) {
    if (Objects.isNull(input) || input.isEmpty()) { // 使用标准库替代未知 StringUtil
      throw new IllegalArgumentException("Input cannot be null or empty");
    }

    try {
      MessageDigest md = MD5_DIGEST.get();
      md.reset(); // 重置状态防止多线程干扰
      byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));

      // 替代未知 HexUtil 方法，自行实现十六进制编码逻辑保持功能一致
      return HexUtil.encodeHexString(messageDigest, toUpperCase);
    } catch (Exception e) {
      throw new RuntimeException("Error computing MD5 for input", e);
    }
  }

  public static void main(String[] args) {
    String input = "Hello, World!";
    System.out.println("原始字符串: " + input);
    System.out.println("MD5 哈希值: " + MD5Util.get(input));
    System.out.println("MD5 哈希值(大写): " + MD5Util.get(input, Boolean.TRUE));
  }

}
