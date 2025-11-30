package io.github.weijunfu.id.security;

import io.github.weijunfu.id.security.enums.AESKeySizeEnum;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

/**
 * AES-GCM 加解密工具类（带认证加密）
 * 支持 128/256 位密钥，使用 12 字节 nonce（推荐）和 16 字节认证标签（128 位）
 */
public final class AESUtil {

  private static final String ALGORITHM = "AES";
  private static final String TRANSFORMATION = "AES/GCM/NoPadding"; // GCM 不需要填充

  private static final int GCM_NONCE_LENGTH = 12; // 推荐 96 位 (12 字节)
  private static final int GCM_TAG_LENGTH = 128;  // 认证标签长度（bit），必须为 128

  private AESUtil() {
    // 工具类，禁止实例化
  }

  /**
   * 生成 AES 密钥（128 或 256 位），返回 Base64 字符串
   */
  public static String generateKey(AESKeySizeEnum keySize) throws Exception {
    if (Objects.isNull(keySize)) {
      throw new IllegalArgumentException("密钥长度必须为 128 或 256");
    }
    KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
    keyGen.init(keySize.getKeySize(), new SecureRandom());
    SecretKey secretKey = keyGen.generateKey();
    return Base64.getEncoder().encodeToString(secretKey.getEncoded());
  }

  /**
   * AES-GCM 加密（自动随机生成 nonce）
   *
   * @param plainText 明文
   * @param base64Key Base64 编码的密钥
   * @return Base64( nonce + ciphertext + authTag )
   */
  public static String encrypt(String plainText, String base64Key) throws Exception {
    byte[] keyBytes = Base64.getDecoder().decode(base64Key);
    SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);

    // 生成随机 nonce（12 字节）
    byte[] nonce = new byte[GCM_NONCE_LENGTH];
    new SecureRandom().nextBytes(nonce);

    Cipher cipher = Cipher.getInstance(TRANSFORMATION);
    GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, nonce);
    cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

    byte[] ciphertext = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

    // 拼接：nonce (12) + ciphertext（含 auth tag）
    byte[] combined = new byte[nonce.length + ciphertext.length];
    System.arraycopy(nonce, 0, combined, 0, nonce.length);
    System.arraycopy(ciphertext, 0, combined, nonce.length, ciphertext.length);

    return Base64.getEncoder().encodeToString(combined);
  }

  /**
   * AES-GCM 解密（自动提取 nonce 并验证完整性）
   *
   * @param base64Encrypted Base64 编码的密文（nonce + ciphertext + authTag）
   * @param base64Key       Base64 编码的密钥
   * @return 明文（若认证失败，抛出 AEADBadTagException）
   */
  public static String decrypt(String base64Encrypted, String base64Key) throws Exception {
    byte[] combined = Base64.getDecoder().decode(base64Encrypted);
    byte[] keyBytes = Base64.getDecoder().decode(base64Key);

    if (combined.length < GCM_NONCE_LENGTH) {
      throw new IllegalArgumentException("密文太短，无法提取 nonce");
    }

    byte[] nonce = new byte[GCM_NONCE_LENGTH];
    byte[] ciphertext = new byte[combined.length - GCM_NONCE_LENGTH];
    System.arraycopy(combined, 0, nonce, 0, nonce.length);
    System.arraycopy(combined, nonce.length, ciphertext, 0, ciphertext.length);

    SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);
    Cipher cipher = Cipher.getInstance(TRANSFORMATION);
    GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, nonce);
    cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

    byte[] decrypted = cipher.doFinal(ciphertext);
    return new String(decrypted, StandardCharsets.UTF_8);
  }

  // ===== 测试示例 =====
  public static void main(String[] args) throws Exception {
    String key = generateKey(AESKeySizeEnum.K_128); // 或 256
    System.out.println("密钥 (Base64): " + key);

    String plaintext = "Sensitive data: credit card = 1234-5678-9012-3456";
    System.out.println("明文: " + plaintext);

    String encrypted = encrypt(plaintext, key);
    System.out.println("密文 (Base64): " + encrypted);

    String decrypted = decrypt(encrypted, key);
    System.out.println("解密后: " + decrypted);
    System.out.println("一致: " + plaintext.equals(decrypted));

    // 尝试篡改密文（会抛出异常）
    try {
      String tampered = encrypted.substring(0, encrypted.length() - 4) + "AAAA";
      decrypt(tampered, key);
    } catch (Exception e) {
      System.out.println("✅ 篡改检测成功！解密失败（预期行为）: " + e.getClass().getSimpleName());
    }
  }
}
