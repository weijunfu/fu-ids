package io.github.weijunfu.id.security.dto;

import io.github.weijunfu.id.security.KeyManager;
import io.github.weijunfu.id.security.enums.AESKeySizeEnum;
import io.github.weijunfu.id.security.util.AESUtil;

import javax.crypto.AEADBadTagException;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EncryptedDataTest {

  @Test
  void decryptsWithAuthenticatedMetadata() throws Exception {
    KeyManager keyManager = keyManager();
    String context = "tenant=fu;table=message;id=1";

    EncryptedData encryptedData = EncryptedData.encrypt("secret", keyManager, context);

    assertEquals("secret", encryptedData.decrypt(keyManager, context));
  }

  @Test
  void rejectsWrongBusinessContext() throws Exception {
    KeyManager keyManager = keyManager();
    EncryptedData encryptedData = EncryptedData.encrypt("secret", keyManager, "record=1");

    assertThrows(AEADBadTagException.class, () -> encryptedData.decrypt(keyManager, "record=2"));
  }

  @Test
  void rejectsTamperedVersion() throws Exception {
    KeyManager keyManager = keyManager();
    EncryptedData encryptedData = EncryptedData.encrypt("secret", keyManager, "record=1");

    encryptedData.setVersion("v2");

    assertThrows(AEADBadTagException.class, () -> encryptedData.decrypt(keyManager, "record=1"));
  }

  @Test
  void rejectsTamperedKeyIdWhenAnotherKeyExists() throws Exception {
    String key1 = AESUtil.generateKeyToString(AESKeySizeEnum.K_256);
    String key2 = AESUtil.generateKeyToString(AESKeySizeEnum.K_256);
    KeyManager keyManager = KeyManager.fromBase64Keys("v1", Map.of("v1", key1, "v2", key2));
    EncryptedData encryptedData = EncryptedData.encrypt("secret", keyManager, "record=1");

    encryptedData.setKeyId("v2");

    assertThrows(AEADBadTagException.class, () -> encryptedData.decrypt(keyManager, "record=1"));
  }

  private static KeyManager keyManager() throws Exception {
    String key = AESUtil.generateKeyToString(AESKeySizeEnum.K_256);
    return KeyManager.fromBase64Keys("v1", Map.of("v1", key));
  }
}
