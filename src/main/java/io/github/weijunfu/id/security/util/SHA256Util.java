package io.github.weijunfu.id.security.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;

/**
 * SHA-256 digest utility for non-password data.
 */
public final class SHA256Util {
  private static final String ALGORITHM = "SHA-256";

  private SHA256Util() {
  }

  public static byte[] digest(String input) {
    Objects.requireNonNull(input, "input cannot be null");
    return digest(input.getBytes(StandardCharsets.UTF_8));
  }

  public static byte[] digest(byte[] input) {
    Objects.requireNonNull(input, "input cannot be null");
    try {
      return MessageDigest.getInstance(ALGORITHM).digest(input);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Failed to initialize SHA-256 digest", e);
    }
  }

  public static String digestToHex(String input) {
    return digestToHex(input, false);
  }

  public static String digestToHex(String input, boolean toUpperCase) {
    return HexUtil.encodeHexString(digest(input), toUpperCase);
  }

  public static String digestToHex(byte[] input) {
    return HexUtil.encodeHexString(digest(input));
  }

  public static String digestToBase64(String input) {
    return Base64.getEncoder().encodeToString(digest(input));
  }

  public static String digestToBase64(byte[] input) {
    return Base64.getEncoder().encodeToString(digest(input));
  }
}
