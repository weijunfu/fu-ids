package io.github.weijunfu.id.security.dto;

import io.github.weijunfu.id.security.KeyManager;
import io.github.weijunfu.id.security.enums.AESKeySizeEnum;
import io.github.weijunfu.id.security.util.AESUtil;
import io.github.weijunfu.id.security.util.Base64Util;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * 表示一条加密数据，包含 keyId、IV、密文
 */
public class EncryptedData {
  private String keyId;
  private String iv;          // Base64
  private String ciphertext;  // Base64

  public void setKeyId(String keyId) {
    this.keyId = keyId;
  }

  public void setIv(String iv) {
    this.iv = iv;
  }

  public void setCiphertext(String ciphertext) {
    this.ciphertext = ciphertext;
  }

  public static EncryptedData encrypt(String plaintext, KeyManager keyManager) throws Exception {
    String keyId = keyManager.getActiveKeyId();
    SecretKeySpec key = keyManager.getKey(keyId);

    byte[] iv = new byte[AESUtil.GCM_NONCE_LENGTH]; // GCM 推荐 12 字节 nonce
    new SecureRandom().nextBytes(iv);

    Cipher cipher = Cipher.getInstance(AESUtil.TRANSFORMATION);
    GCMParameterSpec spec = new GCMParameterSpec(AESUtil.GCM_TAG_LENGTH, iv);
    cipher.init(Cipher.ENCRYPT_MODE, key, spec);
    byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

    return new EncryptedData(keyId,
        Base64Util.encodeToString(iv),
        Base64Util.encodeToString(ciphertext));
  }

  public String decrypt(KeyManager keyManager) throws Exception {
    SecretKeySpec key = keyManager.getKey(this.keyId);
    byte[] iv = Base64Util.decode(this.iv);
    byte[] ciphertext = Base64Util.decode(this.ciphertext);

    Cipher cipher = Cipher.getInstance(AESUtil.TRANSFORMATION);
    GCMParameterSpec spec = new GCMParameterSpec(AESUtil.GCM_TAG_LENGTH, iv);
    cipher.init(Cipher.DECRYPT_MODE, key, spec);
    byte[] plaintext = cipher.doFinal(ciphertext);

    return new String(plaintext, StandardCharsets.UTF_8);
  }

  // 构造函数
  public EncryptedData(String keyId, String iv, String ciphertext) {
    this.keyId = keyId;
    this.iv = iv;
    this.ciphertext = ciphertext;
  }

  // Getters
  public String getKeyId() { return keyId; }
  public String getIv() { return iv; }
  public String getCiphertext() { return ciphertext; }

  public static void main(String[] args) throws Exception {
    KeyManager km = new KeyManager();

    // 1. 用 v1 加密数据
    EncryptedData data1 = encrypt("Secret message v1", km);
    System.out.println("v1 加密: " + data1.getCiphertext());

    // 2. 轮换到 v2
    String newKey = AESUtil.generateKey(AESKeySizeEnum.K_256); // 256-bit key
    km.rotateKey("v2", newKey);

    // 3. 用 v2 加密新数据
    EncryptedData data2 = EncryptedData.encrypt("Secret message v2", km);
    System.out.println("v2 加密: " + data2.getCiphertext());

    // 4. 解密旧数据（自动用 v1）
    String plain1 = data1.decrypt(km);
    String plain2 = data2.decrypt(km);
    System.out.println("解密 v1: " + plain1);
    System.out.println("解密 v2: " + plain2);
  }
}
