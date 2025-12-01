package io.github.weijunfu.id.security;

import io.github.weijunfu.id.security.util.Base64Util;

import javax.crypto.spec.SecretKeySpec;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 密钥管理器：支持多版本密钥，标记当前活跃密钥
 */
public class KeyManager {
  // keyId -> Base64 编码的 AES 密钥（32字节 = AES-256）
  private final Map<String, String> keyStore = new ConcurrentHashMap<>();
  private volatile String activeKeyId;

  public KeyManager() {
    // 初始化（实际应从安全源加载，如 KMS、配置中心）
    addKey("v1", "Gior2bp1I5KkvkkXMQog+A=="); // 示例密钥
    setActiveKey("v1");
  }

  public void addKey(String keyId, String base64Key) {
    keyStore.put(keyId, base64Key);
  }

  public void setActiveKey(String keyId) {
    if (!keyStore.containsKey(keyId)) {
      throw new IllegalArgumentException("密钥不存在: " + keyId);
    }
    this.activeKeyId = keyId;
  }

  public String getActiveKeyId() {
    return activeKeyId;
  }

  public SecretKeySpec getKey(String keyId) {
    String base64Key = keyStore.get(keyId);
    if (base64Key == null) {
      throw new IllegalArgumentException("未知密钥ID: " + keyId);
    }
    byte[] keyBytes = Base64Util.decode(base64Key);
    return new SecretKeySpec(keyBytes, "AES");
  }

  // 模拟密钥轮换（生产环境应从外部触发）
  public void rotateKey(String newKeyId, String newBase64Key) {
    addKey(newKeyId, newBase64Key);
    setActiveKey(newKeyId);
    System.out.println("✅ 密钥已轮换至: " + newKeyId);
  }
}
