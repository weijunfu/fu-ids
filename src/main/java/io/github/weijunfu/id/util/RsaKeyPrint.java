package io.github.weijunfu.id.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class RsaKeyPrint {

    private static enum RSAType {
        PUBLIC("PUBLIC KEY"), PRIVATE("PRIVATE KEY");

        private String label;

        RSAType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }
    }

    public static String toPublicPem(byte[] derBytes) {
        return toPem(RSAType.PUBLIC.getLabel(), derBytes);
    }

    public static String toPrivatePem(byte[] derBytes) {
        return toPem(RSAType.PRIVATE.getLabel(), derBytes);
    }

    private static String toPem(String type, byte[] derBytes) {
        String base64 = Base64.getMimeEncoder(
                64,
                System.lineSeparator().getBytes(StandardCharsets.US_ASCII)
        ).encodeToString(derBytes);

        return "-----BEGIN " + type + "-----" + System.lineSeparator()
                + base64 + System.lineSeparator()
                + "-----END " + type + "-----";
    }
}
