package io.github.weijunfu.id.security.util;

import io.github.weijunfu.id.security.enums.AESKeySizeEnum;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.PSSParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;

/**
 * RSA utility for AES key wrapping and RSA-PSS signatures.
 */
public final class RSAUtil {

  public static final String PUBLIC_KEY = "PUBLIC";
  public static final String PRIVATE_KEY = "PRIVATE";

  private static final String KEY_ALGORITHM = "RSA";
  private static final String SIGNATURE_ALGORITHM = "RSASSA-PSS";
  private static final String CIPHER_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
  private static final int DEFAULT_KEY_SIZE = 2048;
  private static final int MIN_KEY_SIZE = 2048;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();
  private static final OAEPParameterSpec OAEP_SHA256_SPEC = new OAEPParameterSpec(
      "SHA-256",
      "MGF1",
      MGF1ParameterSpec.SHA256,
      PSource.PSpecified.DEFAULT
  );
  private static final PSSParameterSpec PSS_SHA256_SPEC = new PSSParameterSpec(
      "SHA-256",
      "MGF1",
      MGF1ParameterSpec.SHA256,
      32,
      1
  );

  private RSAUtil() {
  }

  public static Map<String, String> generateMapKeyPair(int keySize) {
    try {
      String[] keyPair = generateKeyPair(keySize);
      return Map.of(PUBLIC_KEY, keyPair[0], PRIVATE_KEY, keyPair[1]);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static String[] generateKeyPair() throws Exception {
    return generateKeyPair(DEFAULT_KEY_SIZE);
  }

  public static String[] generateKeyPair(int keySize) throws Exception {
    if (keySize < MIN_KEY_SIZE) {
      throw new IllegalArgumentException("RSA key size must be at least 2048 bits");
    }

    KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
    keyGen.initialize(keySize, SECURE_RANDOM);
    KeyPair keyPair = keyGen.generateKeyPair();

    String publicKey = Base64Util.encodeToString(keyPair.getPublic().getEncoded());
    String privateKey = Base64Util.encodeToString(keyPair.getPrivate().getEncoded());
    return new String[]{publicKey, privateKey};
  }

  public static byte[][] generateByteKeyPair() throws NoSuchAlgorithmException {
    return generateByteKeyPair(DEFAULT_KEY_SIZE);
  }

  public static byte[][] generateByteKeyPair(int keySize) throws NoSuchAlgorithmException {
    if (keySize < MIN_KEY_SIZE) {
      throw new IllegalArgumentException("RSA key size must be at least 2048 bits");
    }

    KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
    keyGen.initialize(keySize, SECURE_RANDOM);
    KeyPair keyPair = keyGen.generateKeyPair();

    return new byte[][]{keyPair.getPublic().getEncoded(), keyPair.getPrivate().getEncoded()};
  }

  /**
   * @deprecated Use {@link #wrapAesKeyByPublicKey(String, String)}. RSA encryption is limited to
   * AES key wrapping.
   */
  @Deprecated(forRemoval = false)
  public static String encryptByPublicKey(String base64AesKey, String base64PublicKey) throws Exception {
    return wrapAesKeyByPublicKey(base64AesKey, base64PublicKey);
  }

  public static String wrapAesKeyByPublicKey(String base64AesKey, String base64PublicKey) throws Exception {
    SecretKeySpec aesKey = AESUtil.toAesKey(base64AesKey);
    PublicKey publicKey = parsePublicKey(base64PublicKey);

    Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
    cipher.init(Cipher.ENCRYPT_MODE, publicKey, OAEP_SHA256_SPEC);
    byte[] encrypted = cipher.doFinal(aesKey.getEncoded());
    return Base64Util.encodeToString(encrypted);
  }

  /**
   * @deprecated Private-key encryption is not supported. Use {@link #sign(String, String)} instead.
   */
  @Deprecated(forRemoval = true)
  public static String encryptByPrivate(String data, String base64PrivateKey) {
    throw new UnsupportedOperationException("Private-key encryption is not supported; use sign/verify");
  }

  /**
   * @deprecated Use {@link #unwrapAesKeyByPrivateKey(String, String)}. RSA decryption is limited to
   * AES key unwrapping.
   */
  @Deprecated(forRemoval = false)
  public static String decryptByPrivateKey(String base64EncryptedAesKey, String base64PrivateKey) throws Exception {
    return unwrapAesKeyByPrivateKey(base64EncryptedAesKey, base64PrivateKey);
  }

  public static String unwrapAesKeyByPrivateKey(String base64EncryptedAesKey, String base64PrivateKey) throws Exception {
    byte[] encryptedData = Base64Util.decode(base64EncryptedAesKey);
    PrivateKey privateKey = parsePrivateKey(base64PrivateKey);

    Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
    cipher.init(Cipher.DECRYPT_MODE, privateKey, OAEP_SHA256_SPEC);
    byte[] decrypted = cipher.doFinal(encryptedData);

    SecretKeySpec aesKey = new SecretKeySpec(decrypted, AESUtil.ALGORITHM);
    AESUtil.validateAesKey(aesKey);
    return Base64Util.encodeToString(decrypted);
  }

  /**
   * @deprecated Public-key decryption is not supported. Use {@link #verify(String, String, String)}
   * instead.
   */
  @Deprecated(forRemoval = true)
  public static String decryptByPublicKey(String data, String base64PublicKey) {
    throw new UnsupportedOperationException("Public-key decryption is not supported; use sign/verify");
  }

  public static String sign(String data, String base64PrivateKey) throws Exception {
    PrivateKey privateKey = parsePrivateKey(base64PrivateKey);

    Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
    signature.setParameter(PSS_SHA256_SPEC);
    signature.initSign(privateKey, SECURE_RANDOM);
    signature.update(data.getBytes(StandardCharsets.UTF_8));
    return Base64Util.encodeToString(signature.sign());
  }

  public static boolean verify(String data, String base64Signature, String base64PublicKey) throws Exception {
    byte[] sigBytes = Base64Util.decode(base64Signature);
    PublicKey publicKey = parsePublicKey(base64PublicKey);

    Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
    signature.setParameter(PSS_SHA256_SPEC);
    signature.initVerify(publicKey);
    signature.update(data.getBytes(StandardCharsets.UTF_8));
    return signature.verify(sigBytes);
  }

  private static PublicKey parsePublicKey(String base64PublicKey) throws Exception {
    byte[] keyBytes = Base64Util.decode(base64PublicKey);
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
    return KeyFactory.getInstance(KEY_ALGORITHM).generatePublic(keySpec);
  }

  private static PrivateKey parsePrivateKey(String base64PrivateKey) throws Exception {
    byte[] keyBytes = Base64Util.decode(base64PrivateKey);
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
    return KeyFactory.getInstance(KEY_ALGORITHM).generatePrivate(keySpec);
  }

  public static void main(String[] args) throws Exception {
    String[] keys = generateKeyPair();
    String publicKey = keys[0];
    String privateKey = keys[1];

    String aesKey = AESUtil.generateKeyToString(AESKeySizeEnum.K_256);
    String wrappedAesKey = wrapAesKeyByPublicKey(aesKey, publicKey);
    String unwrappedAesKey = unwrapAesKeyByPrivateKey(wrappedAesKey, privateKey);
    System.out.println("AES key unwrap matched: " + aesKey.equals(unwrappedAesKey));

    String message = "This is a signed message.";
    String signature = sign(message, privateKey);
    System.out.println("RSA-PSS verify result: " + verify(message, signature, publicKey));
  }
}
