package io.github.weijunfu.id.security;

import io.github.weijunfu.id.security.dto.EncryptedData;
import io.github.weijunfu.id.security.enums.AESKeySizeEnum;
import io.github.weijunfu.id.security.util.AESUtil;
import io.github.weijunfu.id.security.util.RSAUtil;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HybridEncryptionTest {

  @Test
  void rsaWrapsAesKeyThenKeyManagerUsesAesForPayloadEncryption() throws Exception {
    String[] rsaKeys = RSAUtil.generateKeyPair();
    String publicKey = rsaKeys[0];
    String privateKey = rsaKeys[1];

    String generatedAesKey = AESUtil.generateKeyToString(AESKeySizeEnum.K_256);
    String wrappedAesKey = RSAUtil.wrapAesKeyByPublicKey(generatedAesKey, publicKey);
    String unwrappedAesKey = RSAUtil.unwrapAesKeyByPrivateKey(wrappedAesKey, privateKey);

    KeyManager keyManager = KeyManager.fromBase64Keys("v1", Map.of("v1", unwrappedAesKey));
    String context = "tenant=fu;table=orders;id=1001";
    EncryptedData encryptedData = EncryptedData.encrypt("payload", keyManager, context);

    assertEquals(generatedAesKey, unwrappedAesKey);
    assertEquals("payload", encryptedData.decrypt(keyManager, context));
  }
}
