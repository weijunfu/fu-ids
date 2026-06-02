package io.github.weijunfu.id.security.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SHA256UtilTest {

  @Test
  void digestsTextToHex() {
    assertEquals(
        "dffd6021bb2bd5b0af676290809ec3a53191dd81c7f70a4b28688a362182986f",
        SHA256Util.digestToHex("Hello, World!")
    );
  }

  @Test
  void digestsTextToUppercaseHex() {
    assertEquals(
        "DFFD6021BB2BD5B0AF676290809EC3A53191DD81C7F70A4B28688A362182986F",
        SHA256Util.digestToHex("Hello, World!", true)
    );
  }

  @Test
  void rejectsNullInput() {
    assertThrows(NullPointerException.class, () -> SHA256Util.digest((String) null));
  }
}
