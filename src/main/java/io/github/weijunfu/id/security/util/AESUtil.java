package io.github.weijunfu.id.security.util;

import io.github.weijunfu.id.security.enums.AESKeySizeEnum;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;

/**
 * AES-GCM authenticated encryption utility.
 */
public final class AESUtil {

  public static final String ALGORITHM = "AES";
  public static final String TRANSFORMATION = "AES/GCM/NoPadding";

  public static final int GCM_NONCE_LENGTH = 12;
  public static final int GCM_TAG_LENGTH = 128;
  public static final int GCM_TAG_LENGTH_BYTES = GCM_TAG_LENGTH / Byte.SIZE;
  public static final int MIN_ENCRYPTED_LENGTH = GCM_NONCE_LENGTH + GCM_TAG_LENGTH_BYTES;

  private static final int AES_128_KEY_BYTES = 16;
  private static final int AES_256_KEY_BYTES = 32;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private AESUtil() {
  }

  public static String generateKeyToString(AESKeySizeEnum keySize) throws Exception {
    return Base64Util.encodeToString(generateKey(keySize).getEncoded());
  }

  public static SecretKey generateKey(AESKeySizeEnum keySize) throws Exception {
    if (Objects.isNull(keySize)) {
      throw new IllegalArgumentException("AES key size must be 128 or 256 bits");
    }
    KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
    keyGen.init(keySize.getKeySize(), SECURE_RANDOM);
    return keyGen.generateKey();
  }

  public static String encrypt(String plainText, String base64Key) throws Exception {
    return encrypt(plainText, base64Key, (byte[]) null);
  }

  public static String encrypt(String plainText, String base64Key, String aad) throws Exception {
    return encrypt(plainText, base64Key, toUtf8Bytes(aad));
  }

  public static String encrypt(String plainText, String base64Key, byte[] aad) throws Exception {
    return encryptToString(toAesKey(base64Key), plainText, aad);
  }

  public static String encryptToString(SecretKeySpec keySpec, String plainText) throws Exception {
    return encryptToString(keySpec, plainText, (byte[]) null);
  }

  public static String encryptToString(SecretKeySpec keySpec, String plainText, String aad) throws Exception {
    return encryptToString(keySpec, plainText, toUtf8Bytes(aad));
  }

  public static String encryptToString(SecretKeySpec keySpec, String plainText, byte[] aad) throws Exception {
    byte[] ciphertext = encrypt(keySpec, plainText, aad);
    return Base64Util.encodeToString(ciphertext);
  }

  public static byte[] encrypt(SecretKeySpec keySpec, String plainText) throws Exception {
    return encrypt(keySpec, plainText, (byte[]) null);
  }

  public static byte[] encrypt(SecretKeySpec keySpec, String plainText, String aad) throws Exception {
    return encrypt(keySpec, plainText, toUtf8Bytes(aad));
  }

  public static byte[] encrypt(SecretKeySpec keySpec, String plainText, byte[] aad) throws Exception {
    validatePlainText(plainText);
    validateAesKey(keySpec);

    byte[] nonce = new byte[GCM_NONCE_LENGTH];
    SECURE_RANDOM.nextBytes(nonce);

    Cipher cipher = Cipher.getInstance(TRANSFORMATION);
    GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, nonce);
    cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
    updateAad(cipher, aad);

    byte[] ciphertext = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
    byte[] combined = new byte[nonce.length + ciphertext.length];
    System.arraycopy(nonce, 0, combined, 0, nonce.length);
    System.arraycopy(ciphertext, 0, combined, nonce.length, ciphertext.length);
    return combined;
  }

  public static String decryptToString(String base64Encrypted, String base64Key) throws Exception {
    return decryptToString(base64Encrypted, base64Key, (byte[]) null);
  }

  public static String decryptToString(String base64Encrypted, String base64Key, String aad) throws Exception {
    return decryptToString(base64Encrypted, base64Key, toUtf8Bytes(aad));
  }

  public static String decryptToString(String base64Encrypted, String base64Key, byte[] aad) throws Exception {
    byte[] plaintext = decrypt(base64Encrypted, base64Key, aad);
    return new String(plaintext, StandardCharsets.UTF_8);
  }

  public static String decryptToString(SecretKeySpec keySpec, byte[] encrypted, byte[] aad) throws Exception {
    byte[] plaintext = decrypt(keySpec, encrypted, aad);
    return new String(plaintext, StandardCharsets.UTF_8);
  }

  public static byte[] decrypt(String base64Encrypted, String base64Key) throws Exception {
    return decrypt(base64Encrypted, base64Key, (byte[]) null);
  }

  public static byte[] decrypt(String base64Encrypted, String base64Key, String aad) throws Exception {
    return decrypt(base64Encrypted, base64Key, toUtf8Bytes(aad));
  }

  public static byte[] decrypt(String base64Encrypted, String base64Key, byte[] aad) throws Exception {
    return decrypt(toAesKey(base64Key), Base64Util.decode(base64Encrypted), aad);
  }

  public static byte[] decrypt(SecretKeySpec keySpec, byte[] encrypted, byte[] aad) throws Exception {
    validateAesKey(keySpec);
    validateEncrypted(encrypted);

    byte[] nonce = Arrays.copyOfRange(encrypted, 0, GCM_NONCE_LENGTH);
    byte[] ciphertext = Arrays.copyOfRange(encrypted, GCM_NONCE_LENGTH, encrypted.length);

    Cipher cipher = Cipher.getInstance(TRANSFORMATION);
    GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, nonce);
    cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
    updateAad(cipher, aad);

    return cipher.doFinal(ciphertext);
  }

  public static SecretKeySpec toAesKey(String base64Key) {
    byte[] keyBytes = Base64Util.decode(base64Key);
    if (!isValidAesKeyLength(keyBytes.length)) {
      throw new IllegalArgumentException("AES key must be 16 or 32 bytes after Base64 decoding");
    }
    return new SecretKeySpec(keyBytes, ALGORITHM);
  }

  public static void validateAesKey(SecretKeySpec keySpec) {
    if (keySpec == null) {
      throw new IllegalArgumentException("AES key cannot be null");
    }
    if (!ALGORITHM.equalsIgnoreCase(keySpec.getAlgorithm())) {
      throw new IllegalArgumentException("Key algorithm must be AES");
    }

    byte[] keyBytes = keySpec.getEncoded();
    if (keyBytes == null || !isValidAesKeyLength(keyBytes.length)) {
      throw new IllegalArgumentException("AES key must be 16 or 32 bytes");
    }
  }

  public static void validateEncrypted(byte[] encrypted) {
    if (encrypted == null || encrypted.length < MIN_ENCRYPTED_LENGTH) {
      throw new IllegalArgumentException("Encrypted data must contain a 12-byte nonce and 16-byte GCM tag");
    }
  }

  private static boolean isValidAesKeyLength(int keyLength) {
    return keyLength == AES_128_KEY_BYTES || keyLength == AES_256_KEY_BYTES;
  }

  private static void validatePlainText(String plainText) {
    if (plainText == null) {
      throw new IllegalArgumentException("plainText cannot be null");
    }
  }

  private static byte[] toUtf8Bytes(String value) {
    return value == null ? null : value.getBytes(StandardCharsets.UTF_8);
  }

  private static void updateAad(Cipher cipher, byte[] aad) {
    if (aad != null && aad.length > 0) {
      cipher.updateAAD(aad);
    }
  }

  public static void main(String[] args) throws Exception {
    String key = generateKeyToString(AESKeySizeEnum.K_128);
    String plaintext = "Sensitive data: credit card = 1234-5678-9012-3456";
    String aad = "demo-context";

    String encrypted = encrypt(plaintext, key, aad);
    String decrypted = decryptToString(encrypted, key, aad);

    System.out.println("ciphertext (Base64): " + encrypted);
    System.out.println("decrypted: " + decrypted);
    System.out.println("matched: " + plaintext.equals(decrypted));
  }
}
