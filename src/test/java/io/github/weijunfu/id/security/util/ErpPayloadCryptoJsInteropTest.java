package io.github.weijunfu.id.security.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ErpPayloadCryptoJsInteropTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final Path JS_MODULE = Path.of("web/src/index.js").toAbsolutePath();

  @Test
  void javascriptRequestAndJavaResponseAreCompatible() throws Exception {
    assumeTrue(isNodeAvailable(), "Node.js is not available");
    String[] rsaKeys = RSAUtil.generateKeyPair();
    ErpPayloadCryptoUtil.RequestContext context = fixedContext();

    ObjectNode input = MAPPER.createObjectNode();
    input.put("publicKey", rsaKeys[0]);
    input.put("requestBody", "{\"orderNo\":\"SO-JS-001\"}");
    input.set("context", MAPPER.valueToTree(context));

    JsonNode jsResult = runNode("""
        const { default: fuIds } = await import(MODULE_URL);
        const input = await readInput();
        const request = await fuIds.encryptRequest(input.requestBody, input.publicKey, input.context);
        writeOutput({
          envelope: request.envelope,
          session: fuIds.exportClientSession(request.session)
        });
        """, input);

    ErpPayloadCryptoUtil.RequestEnvelope requestEnvelope = toRequestEnvelope(jsResult.get("envelope"));
    ErpPayloadCryptoUtil.DecryptedRequest decryptedRequest = ErpPayloadCryptoUtil.decryptRequest(
        requestEnvelope,
        rsaKeys[1]
    );
    ErpPayloadCryptoUtil.ResponseEnvelope responseEnvelope = ErpPayloadCryptoUtil.encryptResponse(
        "{\"success\":true}",
        decryptedRequest.getSession(),
        200
    );

    ObjectNode decryptInput = MAPPER.createObjectNode();
    decryptInput.set("envelope", MAPPER.valueToTree(responseEnvelope));
    decryptInput.set("session", jsResult.get("session"));

    JsonNode decryptedResponse = runNode("""
        const { default: fuIds } = await import(MODULE_URL);
        const input = await readInput();
        const plaintext = await fuIds.decryptResponse(input.envelope, fuIds.importClientSession(input.session));
        writeOutput({ plaintext });
        """, decryptInput);

    assertEquals("{\"orderNo\":\"SO-JS-001\"}", decryptedRequest.getPlaintext());
    assertEquals("{\"success\":true}", decryptedResponse.get("plaintext").asText());
  }

  @Test
  void javaRequestAndJavascriptResponseAreCompatible() throws Exception {
    assumeTrue(isNodeAvailable(), "Node.js is not available");
    String[] rsaKeys = RSAUtil.generateKeyPair();
    ErpPayloadCryptoUtil.ClientRequest clientRequest = ErpPayloadCryptoUtil.encryptRequest(
        "{\"orderNo\":\"SO-JAVA-001\"}",
        rsaKeys[0],
        fixedContext()
    );

    ObjectNode input = MAPPER.createObjectNode();
    input.put("privateKey", rsaKeys[1]);
    input.put("responseBody", "{\"success\":true}");
    input.put("statusCode", 201);
    input.set("envelope", MAPPER.valueToTree(clientRequest.getEnvelope()));

    JsonNode jsResult = runNode("""
        const { default: fuIds } = await import(MODULE_URL);
        const input = await readInput();
        const request = await fuIds.decryptRequest(input.envelope, input.privateKey);
        const response = await fuIds.encryptResponse(input.responseBody, request.session, input.statusCode);
        writeOutput({
          plaintext: request.plaintext,
          response
        });
        """, input);

    ErpPayloadCryptoUtil.ResponseEnvelope responseEnvelope = toResponseEnvelope(jsResult.get("response"));
    String responseBody = ErpPayloadCryptoUtil.decryptResponse(responseEnvelope, clientRequest.getSession());

    assertEquals("{\"orderNo\":\"SO-JAVA-001\"}", jsResult.get("plaintext").asText());
    assertEquals("{\"success\":true}", responseBody);
  }

  private static ErpPayloadCryptoUtil.RequestContext fixedContext() {
    return new ErpPayloadCryptoUtil.RequestContext(
        "v2",
        "erp-api-rsa-2026-01",
        "client-a",
        "tenant-001",
        "POST",
        "/api/orders",
        1798790400000L,
        "MDEyMzQ1Njc4OUFCQ0RFRg==",
        "2f4e4c22-8bd2-4f67-802e-f3f64e79a111"
    );
  }

  private static JsonNode runNode(String script, JsonNode input) throws Exception {
    String prelude = """
        const MODULE_URL = process.env.ERP_PAYLOAD_CRYPTO_JS;
        const readInput = async () => {
          let input = '';
          for await (const chunk of process.stdin) {
            input += chunk;
          }
          return JSON.parse(input);
        };
        const writeOutput = (value) => process.stdout.write(JSON.stringify(value));
        """;
    ProcessBuilder processBuilder = new ProcessBuilder("node", "--input-type=module", "--eval", prelude + script)
        .redirectErrorStream(true);
    processBuilder.environment().put("ERP_PAYLOAD_CRYPTO_JS", JS_MODULE.toUri().toString());
    Process process = processBuilder.start();

    try (OutputStream stdin = process.getOutputStream()) {
      MAPPER.writeValue(stdin, input);
    }

    boolean completed = process.waitFor(Duration.ofSeconds(30).toMillis(), TimeUnit.MILLISECONDS);
    if (!completed) {
      process.destroyForcibly();
      fail("Node.js interop test timed out");
    }
    String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    if (process.exitValue() != 0) {
      fail("Node.js interop test failed: " + output);
    }
    return MAPPER.readTree(output);
  }

  private static boolean isNodeAvailable() {
    try {
      Process process = new ProcessBuilder("node", "--version")
          .redirectErrorStream(true)
          .start();
      return process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0;
    } catch (Exception e) {
      return false;
    }
  }

  private static ErpPayloadCryptoUtil.RequestEnvelope toRequestEnvelope(JsonNode node) {
    return new ErpPayloadCryptoUtil.RequestEnvelope(
        node.get("version").asText(),
        node.get("alg").asText(),
        node.get("enc").asText(),
        node.get("kid").asText(),
        node.get("clientId").asText(),
        node.get("tenantId").asText(),
        node.get("method").asText(),
        node.get("path").asText(),
        node.get("timestamp").asLong(),
        node.get("nonce").asText(),
        node.get("requestId").asText(),
        node.get("encryptedKey").asText(),
        node.get("iv").asText(),
        node.get("ciphertext").asText(),
        node.get("tag").asText()
    );
  }

  private static ErpPayloadCryptoUtil.ResponseEnvelope toResponseEnvelope(JsonNode node) {
    return new ErpPayloadCryptoUtil.ResponseEnvelope(
        node.get("version").asText(),
        node.get("enc").asText(),
        node.get("kid").asText(),
        node.get("clientId").asText(),
        node.get("tenantId").asText(),
        node.get("method").asText(),
        node.get("path").asText(),
        node.get("requestId").asText(),
        node.get("statusCode").asInt(),
        node.get("timestamp").asLong(),
        node.get("nonce").asText(),
        node.get("iv").asText(),
        node.get("ciphertext").asText(),
        node.get("tag").asText()
    );
  }
}
