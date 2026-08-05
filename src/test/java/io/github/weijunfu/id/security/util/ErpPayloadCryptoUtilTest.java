package io.github.weijunfu.id.security.util;

import javax.crypto.AEADBadTagException;

import io.github.weijunfu.id.util.RsaKeyPrint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpPayloadCryptoUtilTest {

  @Test
  void genRsaKeys() throws Exception {
    byte[][] rsaKeys = RSAUtil.generateByteKeyPair();

    if(rsaKeys.length != 2) {
      System.err.println("生成RSA密钥对失败");
      return;
    }

    String publicKey = RsaKeyPrint.toPublicPem(rsaKeys[0]);
    System.out.println(publicKey);
    System.out.println("====================================");
    String privateKey = RsaKeyPrint.toPrivatePem(rsaKeys[1]);
    System.out.println(privateKey);
  }


  @Test
  void encryptsRequestAndResponseWithDerivedDirectionalKeys() throws Exception {
    String[] rsaKeys = RSAUtil.generateKeyPair();
    ErpPayloadCryptoUtil.RequestContext context = ErpPayloadCryptoUtil.newRequestContext(
        "erp-api-rsa-2026-01",
        "client-a",
        "tenant-001",
        "POST",
        "/api/orders"
    );

    ErpPayloadCryptoUtil.ClientRequest clientRequest = ErpPayloadCryptoUtil.encryptRequest(
        "{\"orderNo\":\"SO-001\"}",
        rsaKeys[0],
        context
    );
    ErpPayloadCryptoUtil.DecryptedRequest serverRequest = ErpPayloadCryptoUtil.decryptRequest(
        clientRequest.getEnvelope(),
        rsaKeys[1]
    );
    ErpPayloadCryptoUtil.ResponseEnvelope response = ErpPayloadCryptoUtil.encryptResponse(
        "{\"success\":true}",
        serverRequest.getSession(),
        200
    );

    String responseBody = ErpPayloadCryptoUtil.decryptResponse(response, clientRequest.getSession());

    assertEquals("{\"orderNo\":\"SO-001\"}", serverRequest.getPlaintext());
    assertEquals("{\"success\":true}", responseBody);
  }

  @Test
  void encryptsWithConfiguredVersionAndBindsItToKeyInfo() throws Exception {
    String[] rsaKeys = RSAUtil.generateKeyPair();
    ErpPayloadCryptoUtil.RequestContext context = ErpPayloadCryptoUtil.newRequestContext(
        "v2",
        "erp-api-rsa-2026-01",
        "client-a",
        "tenant-001",
        "POST",
        "/api/orders"
    );
    ErpPayloadCryptoUtil.ClientRequest clientRequest = ErpPayloadCryptoUtil.encryptRequest(
        "{\"orderNo\":\"SO-002\"}",
        rsaKeys[0],
        context
    );
    ErpPayloadCryptoUtil.DecryptedRequest serverRequest = ErpPayloadCryptoUtil.decryptRequest(
        clientRequest.getEnvelope(),
        rsaKeys[1]
    );
    ErpPayloadCryptoUtil.ResponseEnvelope response = ErpPayloadCryptoUtil.encryptResponse(
        "{\"success\":true}",
        serverRequest.getSession(),
        200
    );

    assertEquals("v2", clientRequest.getEnvelope().getVersion());
    assertEquals("v2", response.getVersion());
    assertEquals("{\"orderNo\":\"SO-002\"}", serverRequest.getPlaintext());
    assertEquals("{\"success\":true}", ErpPayloadCryptoUtil.decryptResponse(response, clientRequest.getSession()));

    ErpPayloadCryptoUtil.RequestEnvelope tamperedVersion = new ErpPayloadCryptoUtil.RequestEnvelope(
        "v3",
        clientRequest.getEnvelope().getAlg(),
        clientRequest.getEnvelope().getEnc(),
        clientRequest.getEnvelope().getKid(),
        clientRequest.getEnvelope().getClientId(),
        clientRequest.getEnvelope().getTenantId(),
        clientRequest.getEnvelope().getMethod(),
        clientRequest.getEnvelope().getPath(),
        clientRequest.getEnvelope().getTimestamp(),
        clientRequest.getEnvelope().getNonce(),
        clientRequest.getEnvelope().getRequestId(),
        clientRequest.getEnvelope().getEncryptedKey(),
        clientRequest.getEnvelope().getIv(),
        clientRequest.getEnvelope().getCiphertext(),
        clientRequest.getEnvelope().getTag()
    );

    assertThrows(AEADBadTagException.class, () -> ErpPayloadCryptoUtil.decryptRequest(tamperedVersion, rsaKeys[1]));
  }

  @Test
  void rejectsTamperedRequestMetadata() throws Exception {
    String[] rsaKeys = RSAUtil.generateKeyPair();
    ErpPayloadCryptoUtil.ClientRequest clientRequest = ErpPayloadCryptoUtil.encryptRequest(
        "{\"orderNo\":\"SO-001\"}",
        rsaKeys[0],
        context()
    );
    ErpPayloadCryptoUtil.RequestEnvelope envelope = clientRequest.getEnvelope();
    ErpPayloadCryptoUtil.RequestEnvelope tampered = new ErpPayloadCryptoUtil.RequestEnvelope(
        envelope.getVersion(),
        envelope.getAlg(),
        envelope.getEnc(),
        envelope.getKid(),
        envelope.getClientId(),
        envelope.getTenantId(),
        envelope.getMethod(),
        "/api/payments",
        envelope.getTimestamp(),
        envelope.getNonce(),
        envelope.getRequestId(),
        envelope.getEncryptedKey(),
        envelope.getIv(),
        envelope.getCiphertext(),
        envelope.getTag()
    );

    assertThrows(AEADBadTagException.class, () -> ErpPayloadCryptoUtil.decryptRequest(tampered, rsaKeys[1]));
  }

  @Test
  void rejectsTamperedResponseStatus() throws Exception {
    String[] rsaKeys = RSAUtil.generateKeyPair();
    ErpPayloadCryptoUtil.ClientRequest clientRequest = ErpPayloadCryptoUtil.encryptRequest(
        "{\"orderNo\":\"SO-001\"}",
        rsaKeys[0],
        context()
    );
    ErpPayloadCryptoUtil.DecryptedRequest serverRequest = ErpPayloadCryptoUtil.decryptRequest(
        clientRequest.getEnvelope(),
        rsaKeys[1]
    );
    ErpPayloadCryptoUtil.ResponseEnvelope response = ErpPayloadCryptoUtil.encryptResponse(
        "{\"success\":true}",
        serverRequest.getSession(),
        200
    );
    ErpPayloadCryptoUtil.ResponseEnvelope tampered = new ErpPayloadCryptoUtil.ResponseEnvelope(
        response.getVersion(),
        response.getEnc(),
        response.getKid(),
        response.getClientId(),
        response.getTenantId(),
        response.getMethod(),
        response.getPath(),
        response.getRequestId(),
        500,
        response.getTimestamp(),
        response.getNonce(),
        response.getIv(),
        response.getCiphertext(),
        response.getTag()
    );

    assertThrows(AEADBadTagException.class,
        () -> ErpPayloadCryptoUtil.decryptResponse(tampered, clientRequest.getSession()));
  }

  @Test
  void supportsEmptyRequestAndResponseBodies() throws Exception {
    String[] rsaKeys = RSAUtil.generateKeyPair();
    ErpPayloadCryptoUtil.ClientRequest clientRequest = ErpPayloadCryptoUtil.encryptRequest(
        "",
        rsaKeys[0],
        context()
    );
    ErpPayloadCryptoUtil.DecryptedRequest serverRequest = ErpPayloadCryptoUtil.decryptRequest(
        clientRequest.getEnvelope(),
        rsaKeys[1]
    );
    ErpPayloadCryptoUtil.ResponseEnvelope response = ErpPayloadCryptoUtil.encryptResponse(
        "",
        serverRequest.getSession(),
        204
    );

    assertEquals("", serverRequest.getPlaintext());
    assertEquals("", ErpPayloadCryptoUtil.decryptResponse(response, clientRequest.getSession()));
  }

  private static ErpPayloadCryptoUtil.RequestContext context() {
    return ErpPayloadCryptoUtil.newRequestContext(
        "erp-api-rsa-2026-01",
        "client-a",
        "tenant-001",
        "POST",
        "/api/orders"
    );
  }
}
