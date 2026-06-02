package io.github.weijunfu.id.security;

import io.github.weijunfu.id.security.enums.AESKeySizeEnum;
import io.github.weijunfu.id.security.util.AESUtil;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KeyManagerTest {

  @Test
  void newManagerHasNoDefaultKey() {
    KeyManager keyManager = new KeyManager();

    assertThrows(IllegalStateException.class, keyManager::getActiveKeyId);
    assertThrows(IllegalArgumentException.class, () -> keyManager.getKey("v1"));
  }

  @Test
  void acceptsAes128AndAes256Keys() throws Exception {
    String aes128Key = AESUtil.generateKeyToString(AESKeySizeEnum.K_128);
    String aes256Key = AESUtil.generateKeyToString(AESKeySizeEnum.K_256);

    KeyManager keyManager = KeyManager.fromBase64Keys("v1", Map.of(
        "v1", aes128Key,
        "v2", aes256Key
    ));

    assertEquals("v1", keyManager.getActiveKeyId());
    assertEquals(16, keyManager.getKey("v1").getEncoded().length);
    assertEquals(32, keyManager.getKey("v2").getEncoded().length);
  }

  @Test
  void rejectsAes192KeyLength() {
    String aes192Key = Base64.getEncoder().encodeToString(new byte[24]);

    assertThrows(IllegalArgumentException.class, () -> new KeyManager("v1", Map.of("v1", aes192Key)));
  }

  @Test
  void loadsKeysFromPropertiesObject() throws Exception {
    String aes256Key = AESUtil.generateKeyToString(AESKeySizeEnum.K_256);
    Properties properties = new Properties();
    properties.setProperty(KeyManager.ACTIVE_KEY_ID_PROPERTY, "v1");
    properties.setProperty(KeyManager.KEY_PROPERTY_PREFIX + "v1", aes256Key);

    KeyManager keyManager = KeyManager.fromProperties(properties);

    assertEquals("v1", keyManager.getActiveKeyId());
    assertEquals(32, keyManager.getKey("v1").getEncoded().length);
  }

  @Test
  void loadsKeysFromPropertiesFile() throws Exception {
    String aes256Key = AESUtil.generateKeyToString(AESKeySizeEnum.K_256);
    Path path = Files.createTempFile("fu-ids-keys", ".properties");
    try {
      Files.writeString(path, KeyManager.ACTIVE_KEY_ID_PROPERTY + "=v1\n"
          + KeyManager.KEY_PROPERTY_PREFIX + "v1=" + aes256Key + "\n");

      KeyManager keyManager = KeyManager.fromProperties(path);

      assertEquals("v1", keyManager.getActiveKeyId());
      assertEquals(32, keyManager.getKey("v1").getEncoded().length);
    } finally {
      Files.deleteIfExists(path);
    }
  }

  @Test
  void loadsKeysFromExternalSource() throws Exception {
    String aes256Key = AESUtil.generateKeyToString(AESKeySizeEnum.K_256);

    KeyManager keyManager = KeyManager.fromKeySource("v1", () -> Map.of("v1", aes256Key));

    assertEquals("v1", keyManager.getActiveKeyId());
    assertEquals(32, keyManager.getKey("v1").getEncoded().length);
  }
}
