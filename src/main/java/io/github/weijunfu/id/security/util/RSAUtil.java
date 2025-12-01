package io.github.weijunfu.id.security.util;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA 工具类：支持加解密、签名验签
 * 默认使用 SHA256withRSA 签名算法，RSA/ECB/PKCS1Padding 加密填充
 */
public final class RSAUtil {

  private static final String KEY_ALGORITHM = "RSA";
  private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
  private static final String CIPHER_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"; // 或 OAEPWithSHA-256AndMGF1Padding

  // RSA 密钥长度（建议 2048 或 3072）
  private static final int KEY_SIZE = 2048;

  private RSAUtil() {
    // 工具类，禁止实例化
  }

  /**
   * 生成 RSA 密钥对（公钥 + 私钥），返回 Base64 字符串
   *
   * @return [0] 公钥 (Base64), [1] 私钥 (Base64)
   */
  public static String[] generateKeyPair() throws Exception {
    return generateKeyPair(KEY_SIZE);
  }

  public static String[] generateKeyPair(int keySize) throws Exception {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
    keyGen.initialize(keySize, new SecureRandom());
    KeyPair keyPair = keyGen.generateKeyPair();

    String publicKey = Base64Util.encodeToString(keyPair.getPublic().getEncoded());
    String privateKey = Base64Util.encodeToString(keyPair.getPrivate().getEncoded());

    return new String[]{publicKey, privateKey};
  }

  /**
   * 公钥加密（适合加密小数据，如 AES 密钥）
   */
  public static String encryptByPublicKey(String data, String base64PublicKey) throws Exception {
    byte[] keyBytes = Base64Util.decode(base64PublicKey);
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
    PublicKey publicKey = KeyFactory.getInstance(KEY_ALGORITHM).generatePublic(keySpec);

    Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
    cipher.init(Cipher.ENCRYPT_MODE, publicKey);
    byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

    return Base64Util.encodeToString(encrypted);
  }

  /**
   * 私钥解密
   */
  public static String decryptByPrivateKey(String base64EncryptedData, String base64PrivateKey) throws Exception {
    byte[] encryptedData = Base64Util.decode(base64EncryptedData);
    byte[] keyBytes = Base64Util.decode(base64PrivateKey);
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
    PrivateKey privateKey = KeyFactory.getInstance(KEY_ALGORITHM).generatePrivate(keySpec);

    Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
    cipher.init(Cipher.DECRYPT_MODE, privateKey);
    byte[] decrypted = cipher.doFinal(encryptedData);

    return new String(decrypted, StandardCharsets.UTF_8);
  }

  /**
   * 私钥签名（对数据摘要进行签名）
   */
  public static String sign(String data, String base64PrivateKey) throws Exception {
    byte[] keyBytes = Base64Util.decode(base64PrivateKey);
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
    PrivateKey privateKey = KeyFactory.getInstance(KEY_ALGORITHM).generatePrivate(keySpec);

    Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
    signature.initSign(privateKey);
    signature.update(data.getBytes(StandardCharsets.UTF_8));
    byte[] signed = signature.sign();

    return Base64Util.encodeToString(signed);
  }

  /**
   * 公钥验签
   */
  public static boolean verify(String data, String base64Signature, String base64PublicKey) throws Exception {
    byte[] sigBytes = Base64Util.decode(base64Signature);
    byte[] keyBytes = Base64Util.decode(base64PublicKey);
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
    PublicKey publicKey = KeyFactory.getInstance(KEY_ALGORITHM).generatePublic(keySpec);

    Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
    signature.initVerify(publicKey);
    signature.update(data.getBytes(StandardCharsets.UTF_8));

    return signature.verify(sigBytes);
  }

  // ===== 测试示例 =====
  public static void main(String[] args) throws Exception {
    // 1. 生成密钥对
    String[] keys = generateKeyPair();
    String publicKey = keys[0];
    String privateKey = keys[1];

    System.out.println("公钥: " + publicKey);
    System.out.println("私钥: " + privateKey);

    // 2. 加解密测试
    String original = "Hello, RSA!";
    String encrypted = encryptByPublicKey(original, publicKey);
    String decrypted = decryptByPrivateKey(encrypted, privateKey);
    System.out.println("\n--- 加解密 ---");
    System.out.println("原文: " + original);
    System.out.println("密文: " + encrypted);
    System.out.println("解密: " + decrypted);

    // 3. 签名验签测试
    String message = "This is a signed message.";
    String signature = sign(message, privateKey);
    boolean isValid = verify(message, signature, publicKey);
    System.out.println("\n--- 签名验签 ---");
    System.out.println("消息: " + message);
    System.out.println("签名: " + signature);
    System.out.println("验签结果: " + isValid);
  }
}
