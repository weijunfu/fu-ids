package io.github.weijunfu.id.security.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

/**
 * HMAC-SHA256 utility for keyed message authentication.
 */
public final class HmacSHA256Util {
  private static final String ALGORITHM = "HmacSHA256";
  private static final int DEFAULT_KEY_BYTES = 32;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private HmacSHA256Util() {
  }

  public static String generateKeyToString() {
    byte[] key = new byte[DEFAULT_KEY_BYTES];
    SECURE_RANDOM.nextBytes(key);
    return Base64.getEncoder().encodeToString(key);
  }

  public static byte[] sign(String message, String base64Key) {
    Objects.requireNonNull(message, "message cannot be null");
    return sign(message.getBytes(StandardCharsets.UTF_8), Base64Util.decode(base64Key));
  }

  public static byte[] sign(byte[] message, byte[] key) {
    Objects.requireNonNull(message, "message cannot be null");
    validateKey(key);
    try {
      Mac mac = Mac.getInstance(ALGORITHM);
      mac.init(new SecretKeySpec(key, ALGORITHM));
      return mac.doFinal(message);
    } catch (Exception e) {
      throw new RuntimeException("Error computing HMAC-SHA256", e);
    }
  }

  public static String signToHex(String message, String base64Key) {
    return HexUtil.encodeHexString(sign(message, base64Key));
  }

  public static String signToBase64(String message, String base64Key) {
    return Base64.getEncoder().encodeToString(sign(message, base64Key));
  }

  public static boolean verifyHex(String message, String expectedHex, String base64Key) {
    Objects.requireNonNull(expectedHex, "expectedHex cannot be null");
    return MessageDigest.isEqual(sign(message, base64Key), HexUtil.decodeHexString(expectedHex));
  }

  public static boolean verifyBase64(String message, String expectedBase64, String base64Key) {
    Objects.requireNonNull(expectedBase64, "expectedBase64 cannot be null");
    return MessageDigest.isEqual(sign(message, base64Key), Base64Util.decode(expectedBase64));
  }

  private static void validateKey(byte[] key) {
    if (key == null || key.length < DEFAULT_KEY_BYTES) {
      throw new IllegalArgumentException("HMAC-SHA256 key must be at least 32 bytes");
    }
  }
}
