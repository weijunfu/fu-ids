package io.github.weijunfu.id.security;

import io.github.weijunfu.id.security.util.AESUtil;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AES key manager with external key loading support.
 */
public class KeyManager {
  public static final String ACTIVE_KEY_ID_PROPERTY = "activeKeyId";
  public static final String KEY_PROPERTY_PREFIX = "key.";

  private final Map<String, SecretKeySpec> keyStore = new ConcurrentHashMap<>();
  private volatile String activeKeyId;

  /**
   * Creates an empty key manager. Keys must be supplied externally before use.
   */
  public KeyManager() {
  }

  public KeyManager(String activeKeyId, Map<String, String> base64Keys) {
    addKeys(base64Keys);
    setActiveKey(activeKeyId);
  }

  public static KeyManager fromBase64Keys(String activeKeyId, Map<String, String> base64Keys) {
    return new KeyManager(activeKeyId, base64Keys);
  }

  public static KeyManager fromKeySource(String activeKeyId, KeySource keySource) throws Exception {
    Objects.requireNonNull(keySource, "keySource cannot be null");
    return fromBase64Keys(activeKeyId, keySource.loadBase64Keys());
  }

  public static KeyManager fromProperties(Path path) throws IOException {
    Properties properties = loadProperties(path);
    return fromProperties(properties);
  }

  public static KeyManager fromProperties(Path path, String activeKeyId) throws IOException {
    Properties properties = loadProperties(path);
    return fromProperties(properties, activeKeyId);
  }

  public static KeyManager fromProperties(Properties properties) {
    Objects.requireNonNull(properties, "properties cannot be null");
    String activeKeyId = properties.getProperty(ACTIVE_KEY_ID_PROPERTY);
    return fromProperties(properties, activeKeyId);
  }

  public static KeyManager fromProperties(Properties properties, String activeKeyId) {
    Objects.requireNonNull(properties, "properties cannot be null");
    Map<String, String> base64Keys = new ConcurrentHashMap<>();
    for (String name : properties.stringPropertyNames()) {
      if (!name.startsWith(KEY_PROPERTY_PREFIX)) {
        continue;
      }

      String keyId = name.substring(KEY_PROPERTY_PREFIX.length()).trim();
      if (keyId.isEmpty()) {
        throw new IllegalArgumentException("keyId cannot be empty");
      }
      base64Keys.put(keyId, properties.getProperty(name));
    }
    return fromBase64Keys(activeKeyId, base64Keys);
  }

  public void addKeys(Map<String, String> base64Keys) {
    Objects.requireNonNull(base64Keys, "base64Keys cannot be null");
    base64Keys.forEach(this::addKey);
  }

  public void addKey(String keyId, String base64Key) {
    validateKeyId(keyId);
    keyStore.put(keyId, AESUtil.toAesKey(base64Key));
  }

  public void setActiveKey(String keyId) {
    validateKeyId(keyId);
    if (!keyStore.containsKey(keyId)) {
      throw new IllegalArgumentException("Key does not exist: " + keyId);
    }
    this.activeKeyId = keyId;
  }

  public String getActiveKeyId() {
    if (activeKeyId == null) {
      throw new IllegalStateException("No active AES key configured");
    }
    return activeKeyId;
  }

  public SecretKeySpec getKey(String keyId) {
    validateKeyId(keyId);
    SecretKeySpec key = keyStore.get(keyId);
    if (key == null) {
      throw new IllegalArgumentException("Unknown keyId: " + keyId);
    }
    return key;
  }

  public void rotateKey(String newKeyId, String newBase64Key) {
    addKey(newKeyId, newBase64Key);
    setActiveKey(newKeyId);
  }

  private static Properties loadProperties(Path path) throws IOException {
    Objects.requireNonNull(path, "path cannot be null");
    Properties properties = new Properties();
    try (InputStream inputStream = Files.newInputStream(path)) {
      properties.load(inputStream);
    }
    return properties;
  }

  private static void validateKeyId(String keyId) {
    if (keyId == null || keyId.trim().isEmpty()) {
      throw new IllegalArgumentException("keyId cannot be null or empty");
    }
  }

  @FunctionalInterface
  public interface KeySource {
    Map<String, String> loadBase64Keys() throws Exception;
  }
}
