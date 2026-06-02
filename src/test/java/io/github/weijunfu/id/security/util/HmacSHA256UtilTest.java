package io.github.weijunfu.id.security.util;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HmacSHA256UtilTest {

  @Test
  void signsMessageToHexAndBase64() {
    String key = Base64.getEncoder().encodeToString(
        "12345678901234567890123456789012".getBytes(StandardCharsets.UTF_8)
    );

    assertEquals(
        "44116aae98ab5b1ba7fbbcbc475244d3afd08f2965a69df31a74dff7e273a3b1",
        HmacSHA256Util.signToHex("message", key)
    );
    assertEquals(
        "RBFqrpirWxun+7y8R1JE06/Qjyllpp3zGnTf9+Jzo7E=",
        HmacSHA256Util.signToBase64("message", key)
    );
  }

  @Test
  void verifiesExpectedMacInConstantTime() {
    String key = HmacSHA256Util.generateKeyToString();
    String mac = HmacSHA256Util.signToBase64("message", key);

    assertTrue(HmacSHA256Util.verifyBase64("message", mac, key));
    assertFalse(HmacSHA256Util.verifyBase64("tampered", mac, key));
  }

  @Test
  void rejectsShortKeys() {
    String shortKey = Base64.getEncoder().encodeToString("short-key".getBytes(StandardCharsets.UTF_8));

    assertThrows(IllegalArgumentException.class, () -> HmacSHA256Util.signToHex("message", shortKey));
  }
}
