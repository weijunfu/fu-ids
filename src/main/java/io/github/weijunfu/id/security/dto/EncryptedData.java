package io.github.weijunfu.id.security.dto;

import io.github.weijunfu.id.security.KeyManager;
import io.github.weijunfu.id.security.enums.AESKeySizeEnum;
import io.github.weijunfu.id.security.util.AESUtil;
import io.github.weijunfu.id.security.util.Base64Util;

import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Encrypted AES-GCM payload with authenticated metadata.
 */
public class EncryptedData {
  public static final String CURRENT_VERSION = "v1";

  private String version;
  private String keyId;
  private String aadContext;
  private String iv;
  private String ciphertext;

  public static EncryptedData encrypt(String plaintext, KeyManager keyManager) throws Exception {
    return encrypt(plaintext, keyManager, null);
  }

  public static EncryptedData encrypt(String plaintext, KeyManager keyManager, String aadContext) throws Exception {
    String keyId = keyManager.getActiveKeyId();
    SecretKeySpec key = keyManager.getKey(keyId);
    byte[] aad = buildAad(CURRENT_VERSION, keyId, aadContext);
    byte[] encrypted = AESUtil.encrypt(key, plaintext, aad);

    byte[] iv = new byte[AESUtil.GCM_NONCE_LENGTH];
    byte[] ciphertext = new byte[encrypted.length - AESUtil.GCM_NONCE_LENGTH];
    System.arraycopy(encrypted, 0, iv, 0, iv.length);
    System.arraycopy(encrypted, iv.length, ciphertext, 0, ciphertext.length);

    return new EncryptedData(
        CURRENT_VERSION,
        keyId,
        aadContext,
        Base64Util.encodeToString(iv),
        Base64Util.encodeToString(ciphertext)
    );
  }

  public String decrypt(KeyManager keyManager) throws Exception {
    return decrypt(keyManager, this.aadContext);
  }

  public String decrypt(KeyManager keyManager, String aadContext) throws Exception {
    SecretKeySpec key = keyManager.getKey(this.keyId);
    byte[] aad = buildAad(this.version, this.keyId, aadContext);
    return AESUtil.decryptToString(key, toCombinedEncrypted(), aad);
  }

  public EncryptedData(String keyId, String iv, String ciphertext) {
    this(CURRENT_VERSION, keyId, null, iv, ciphertext);
  }

  public EncryptedData(String version, String keyId, String aadContext, String iv, String ciphertext) {
    this.version = version;
    this.keyId = keyId;
    this.aadContext = aadContext;
    this.iv = iv;
    this.ciphertext = ciphertext;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getKeyId() {
    return keyId;
  }

  public void setKeyId(String keyId) {
    this.keyId = keyId;
  }

  public String getAadContext() {
    return aadContext;
  }

  public void setAadContext(String aadContext) {
    this.aadContext = aadContext;
  }

  public String getIv() {
    return iv;
  }

  public void setIv(String iv) {
    this.iv = iv;
  }

  public String getCiphertext() {
    return ciphertext;
  }

  public void setCiphertext(String ciphertext) {
    this.ciphertext = ciphertext;
  }

  private byte[] toCombinedEncrypted() {
    byte[] ivBytes = Base64Util.decode(this.iv);
    if (ivBytes.length != AESUtil.GCM_NONCE_LENGTH) {
      throw new IllegalArgumentException("GCM IV must be 12 bytes");
    }

    byte[] ciphertextBytes = Base64Util.decode(this.ciphertext);
    byte[] encrypted = new byte[ivBytes.length + ciphertextBytes.length];
    System.arraycopy(ivBytes, 0, encrypted, 0, ivBytes.length);
    System.arraycopy(ciphertextBytes, 0, encrypted, ivBytes.length, ciphertextBytes.length);
    AESUtil.validateEncrypted(encrypted);
    return encrypted;
  }

  private static byte[] buildAad(String version, String keyId, String aadContext) {
    byte[] versionBytes = toRequiredUtf8Bytes(version, "version");
    byte[] keyIdBytes = toRequiredUtf8Bytes(keyId, "keyId");
    byte[] contextBytes = normalizeContext(aadContext).getBytes(StandardCharsets.UTF_8);

    ByteBuffer buffer = ByteBuffer.allocate(
        Integer.BYTES * 3 + versionBytes.length + keyIdBytes.length + contextBytes.length
    );
    putField(buffer, versionBytes);
    putField(buffer, keyIdBytes);
    putField(buffer, contextBytes);
    return buffer.array();
  }

  private static void putField(ByteBuffer buffer, byte[] value) {
    buffer.putInt(value.length);
    buffer.put(value);
  }

  private static byte[] toRequiredUtf8Bytes(String value, String fieldName) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(fieldName + " cannot be null or empty");
    }
    return value.getBytes(StandardCharsets.UTF_8);
  }

  private static String normalizeContext(String aadContext) {
    return aadContext == null ? "" : aadContext;
  }

  public static void main(String[] args) throws Exception {
    String initialKey = AESUtil.generateKeyToString(AESKeySizeEnum.K_256);
    KeyManager km = KeyManager.fromBase64Keys("v1", Map.of("v1", initialKey));
    String context = "table=message;id=1";

    EncryptedData data1 = encrypt("Secret message v1", km, context);
    System.out.println("v1 encrypted: " + data1.getCiphertext());

    String newKey = AESUtil.generateKeyToString(AESKeySizeEnum.K_256);
    km.rotateKey("v2", newKey);

    EncryptedData data2 = EncryptedData.encrypt("Secret message v2", km, context);
    System.out.println("v2 encrypted: " + data2.getCiphertext());

    System.out.println("decrypted v1: " + data1.decrypt(km, context));
    System.out.println("decrypted v2: " + data2.decrypt(km, context));
  }
}
