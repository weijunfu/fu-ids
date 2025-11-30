package io.github.weijunfu.id.security.util;
import java.nio.charset.StandardCharsets;

/**
 * 十六进制（Hex）编码/解码工具类
 */
public final class HexUtil {

  private static final char[] HEX_DIGITS_LOWER = "0123456789abcdef".toCharArray();
  private static final char[] HEX_DIGITS_UPPER = "0123456789ABCDEF".toCharArray();

  private HexUtil() {
    // 私有构造，防止实例化
  }

  /**
   * 将字节数组转换为小写十六进制字符串
   *
   * @param bytes 字节数组
   * @return 十六进制字符串（小写），若输入为 null 则返回 null
   */
  public static String encodeHexString(byte[] bytes) {
    return encodeHexString(bytes, false);
  }

  /**
   * 将字节数组转换为十六进制字符串
   *
   * @param bytes      字节数组
   * @param toUpperCase 是否转为大写
   * @return 十六进制字符串，若输入为 null 则返回 null
   */
  public static String encodeHexString(byte[] bytes, boolean toUpperCase) {
    if (bytes == null) {
      return null;
    }
    char[] digits = toUpperCase ? HEX_DIGITS_UPPER : HEX_DIGITS_LOWER;
    char[] hexChars = new char[bytes.length * 2];
    for (int i = 0; i < bytes.length; i++) {
      int v = bytes[i] & 0xFF;
      hexChars[i * 2] = digits[v >>> 4];
      hexChars[i * 2 + 1] = digits[v & 0x0F];
    }
    return new String(hexChars);
  }

  /**
   * 将十六进制字符串解析为字节数组（仅支持偶数长度、合法十六进制字符）
   *
   * @param hexString 十六进制字符串
   * @return 字节数组，若输入为 null 或非法格式则抛出 IllegalArgumentException
   */
  public static byte[] decodeHexString(String hexString) {
    if (hexString == null) {
      return null;
    }
    hexString = hexString.toLowerCase();
    int len = hexString.length();
    if (len % 2 != 0) {
      throw new IllegalArgumentException("Invalid hex string: length must be even.");
    }
    byte[] bytes = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      char high = hexString.charAt(i);
      char low = hexString.charAt(i + 1);
      int h = Character.digit(high, 16);
      int l = Character.digit(low, 16);
      if (h == -1 || l == -1) {
        throw new IllegalArgumentException("Invalid hex character at position " + i);
      }
      bytes[i / 2] = (byte) ((h << 4) | l);
    }
    return bytes;
  }

  // 可选：提供字符串直接转 MD5 并用此工具输出（结合 MessageDigest）
  public static void main(String[] args) throws Exception {
    String input = "Hello, World!";
    byte[] md5Bytes = java.security.MessageDigest.getInstance("MD5")
        .digest(input.getBytes(StandardCharsets.UTF_8));
    System.out.println("MD5 (lower): " + encodeHexString(md5Bytes));
    System.out.println("MD5 (upper): " + encodeHexString(md5Bytes, true));
  }
}
