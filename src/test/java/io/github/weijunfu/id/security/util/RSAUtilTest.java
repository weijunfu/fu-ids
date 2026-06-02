package io.github.weijunfu.id.security.util;

import io.github.weijunfu.id.security.enums.AESKeySizeEnum;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RSAUtilTest {

  @Test
  void wrapsAndUnwrapsAesKey() throws Exception {
    String[] keys = RSAUtil.generateKeyPair();
    String aesKey = AESUtil.generateKeyToString(AESKeySizeEnum.K_256);

    String wrapped = RSAUtil.wrapAesKeyByPublicKey(aesKey, keys[0]);
    String unwrapped = RSAUtil.unwrapAesKeyByPrivateKey(wrapped, keys[1]);

    assertEquals(aesKey, unwrapped);
  }

  @Test
  @SuppressWarnings("deprecation")
  void legacyPublicEncryptOnlyAcceptsAesKey() throws Exception {
    String[] keys = RSAUtil.generateKeyPair();

    assertThrows(IllegalArgumentException.class,
        () -> RSAUtil.encryptByPublicKey("Hello, RSA!", keys[0]));
  }

  @Test
  @SuppressWarnings({"deprecation", "removal"})
  void privateEncryptAndPublicDecryptAreDisabled() throws Exception {
    String[] keys = RSAUtil.generateKeyPair();

    assertThrows(UnsupportedOperationException.class,
        () -> RSAUtil.encryptByPrivate("secret", keys[1]));
    assertThrows(UnsupportedOperationException.class,
        () -> RSAUtil.decryptByPublicKey("secret", keys[0]));
  }

  @Test
  void signsAndVerifiesWithRsaPss() throws Exception {
    String[] keys = RSAUtil.generateKeyPair();
    String message = "This is a signed message.";

    String signature = RSAUtil.sign(message, keys[1]);

    assertTrue(RSAUtil.verify(message, signature, keys[0]));
    assertFalse(RSAUtil.verify(message + "tampered", signature, keys[0]));
  }

  @Test
  void rejectsWeakRsaKeySize() {
    assertThrows(IllegalArgumentException.class, () -> RSAUtil.generateKeyPair(1024));
  }
}
