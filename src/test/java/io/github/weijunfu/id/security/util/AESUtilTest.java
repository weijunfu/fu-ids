package io.github.weijunfu.id.security.util;

import io.github.weijunfu.id.security.enums.AESKeySizeEnum;

import javax.crypto.AEADBadTagException;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AESUtilTest {

  @Test
  void encryptsAndDecryptsWithAad() throws Exception {
    String key = AESUtil.generateKeyToString(AESKeySizeEnum.K_256);
    String aad = "tenant=fu;table=orders;id=1";

    String encrypted = AESUtil.encrypt("secret", key, aad);

    assertEquals("secret", AESUtil.decryptToString(encrypted, key, aad));
  }

  @Test
  void rejectsChangedAad() throws Exception {
    String key = AESUtil.generateKeyToString(AESKeySizeEnum.K_256);
    String encrypted = AESUtil.encrypt("secret", key, "context-a");

    assertThrows(AEADBadTagException.class,
        () -> AESUtil.decryptToString(encrypted, key, "context-b"));
  }

  @Test
  void rejectsInvalidKeyLength() {
    String aes192Key = Base64.getEncoder().encodeToString(new byte[24]);

    assertThrows(IllegalArgumentException.class, () -> AESUtil.encrypt("secret", aes192Key));
  }

  @Test
  void rejectsEncryptedDataShorterThanNonceAndTag() {
    String shortEncrypted = Base64.getEncoder().encodeToString(new byte[AESUtil.MIN_ENCRYPTED_LENGTH - 1]);
    String key = Base64.getEncoder().encodeToString(new byte[32]);

    assertThrows(IllegalArgumentException.class, () -> AESUtil.decrypt(shortEncrypted, key));
  }
}
