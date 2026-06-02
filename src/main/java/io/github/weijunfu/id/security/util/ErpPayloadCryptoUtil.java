package io.github.weijunfu.id.security.util;

import io.github.weijunfu.id.security.enums.AESKeySizeEnum;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

/**
 * ERP request/response envelope encryption utility.
 */
public final class ErpPayloadCryptoUtil {
  public static final String DEFAULT_VERSION = "v1";
  public static final String ALG = "RSA-OAEP-256";
  public static final String ENC = "A256GCM";

  private static final String HKDF_ALGORITHM = "HmacSHA256";
  private static final int AES_256_KEY_BYTES = 32;
  private static final int NONCE_BYTES = 16;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();
  private static final String KEY_INFO_PREFIX = "fu-ids:erp:";

  private ErpPayloadCryptoUtil() {
  }

  public static RequestContext newRequestContext(String kid, String clientId, String tenantId,
      String method, String path) {
    return newRequestContext(DEFAULT_VERSION, kid, clientId, tenantId, method, path);
  }

  public static RequestContext newRequestContext(String version, String kid, String clientId, String tenantId,
      String method, String path) {
    return new RequestContext(
        version,
        kid,
        clientId,
        tenantId,
        method,
        path,
        Instant.now().toEpochMilli(),
        randomNonce(),
        UUID.randomUUID().toString()
    );
  }

  public static ClientRequest encryptRequest(String requestBody, String serverPublicKey,
      RequestContext context) throws Exception {
    validateContext(context);
    Objects.requireNonNull(requestBody, "requestBody cannot be null");

    SecretKey cek = AESUtil.generateKey(AESKeySizeEnum.K_256);
    byte[] cekBytes = cek.getEncoded();
    String encryptedKey = RSAUtil.wrapAesKeyByPublicKey(Base64Util.encodeToString(cekBytes), serverPublicKey);
    SecretKeySpec requestKey = new SecretKeySpec(
        deriveKey(cekBytes, context, requestKeyInfo(context.getVersion())),
        AESUtil.ALGORITHM
    );
    byte[] aad = buildRequestAad(context);
    EncryptedParts encryptedParts = split(AESUtil.encrypt(requestKey, requestBody, aad));

    RequestEnvelope envelope = new RequestEnvelope(
        context.getVersion(),
        ALG,
        ENC,
        context.getKid(),
        context.getClientId(),
        context.getTenantId(),
        context.getMethod(),
        context.getPath(),
        context.getTimestamp(),
        context.getNonce(),
        context.getRequestId(),
        encryptedKey,
        encryptedParts.getIv(),
        encryptedParts.getCiphertext(),
        encryptedParts.getTag()
    );
    return new ClientRequest(envelope, new ClientSession(cekBytes, context));
  }

  public static DecryptedRequest decryptRequest(RequestEnvelope envelope, String serverPrivateKey) throws Exception {
    validateEnvelope(envelope);
    validateAlgorithms(envelope.getVersion(), envelope.getAlg(), envelope.getEnc());

    String base64Cek = RSAUtil.unwrapAesKeyByPrivateKey(envelope.getEncryptedKey(), serverPrivateKey);
    byte[] cekBytes = Base64Util.decode(base64Cek);
    RequestContext context = envelope.toContext();
    SecretKeySpec requestKey = new SecretKeySpec(
        deriveKey(cekBytes, context, requestKeyInfo(context.getVersion())),
        AESUtil.ALGORITHM
    );
    byte[] aad = buildRequestAad(context);
    String plaintext = AESUtil.decryptToString(requestKey, combine(envelope), aad);
    return new DecryptedRequest(plaintext, new ServerSession(cekBytes, context));
  }

  public static ResponseEnvelope encryptResponse(String responseBody, ServerSession session,
      int statusCode) throws Exception {
    Objects.requireNonNull(responseBody, "responseBody cannot be null");
    Objects.requireNonNull(session, "session cannot be null");

    ResponseContext context = ResponseContext.fromRequest(
        session.getContext(),
        statusCode,
        Instant.now().toEpochMilli(),
        randomNonce()
    );
    SecretKeySpec responseKey = new SecretKeySpec(
        deriveKey(session.getCek(), session.getContext(), responseKeyInfo(session.getContext().getVersion())),
        AESUtil.ALGORITHM
    );
    byte[] aad = buildResponseAad(context);
    EncryptedParts encryptedParts = split(AESUtil.encrypt(responseKey, responseBody, aad));

    return new ResponseEnvelope(
        context.getVersion(),
        ENC,
        context.getKid(),
        context.getClientId(),
        context.getTenantId(),
        context.getMethod(),
        context.getPath(),
        context.getRequestId(),
        context.getStatusCode(),
        context.getTimestamp(),
        context.getNonce(),
        encryptedParts.getIv(),
        encryptedParts.getCiphertext(),
        encryptedParts.getTag()
    );
  }

  public static String decryptResponse(ResponseEnvelope envelope, ClientSession session) throws Exception {
    Objects.requireNonNull(envelope, "envelope cannot be null");
    Objects.requireNonNull(session, "session cannot be null");
    validateResponseMatchesRequest(envelope, session.getContext());

    ResponseContext context = envelope.toContext();
    SecretKeySpec responseKey = new SecretKeySpec(
        deriveKey(session.getCek(), session.getContext(), responseKeyInfo(session.getContext().getVersion())),
        AESUtil.ALGORITHM
    );
    return AESUtil.decryptToString(responseKey, combine(envelope), buildResponseAad(context));
  }

  private static byte[] deriveKey(byte[] cek, RequestContext context, byte[] info) {
    byte[] salt = buildKdfSalt(context);
    byte[] prk = hmac(salt, cek);
    return hkdfExpand(prk, info, AES_256_KEY_BYTES);
  }

  private static byte[] requestKeyInfo(String version) {
    return keyInfo("request", version);
  }

  private static byte[] responseKeyInfo(String version) {
    return keyInfo("response", version);
  }

  private static byte[] keyInfo(String direction, String version) {
    return (KEY_INFO_PREFIX + direction + ":" + requireText(version, "version")).getBytes(StandardCharsets.UTF_8);
  }

  private static byte[] hkdfExpand(byte[] prk, byte[] info, int length) {
    byte[] result = new byte[length];
    byte[] previous = new byte[0];
    int offset = 0;
    int counter = 1;
    while (offset < length) {
      ByteBuffer input = ByteBuffer.allocate(previous.length + info.length + 1);
      input.put(previous);
      input.put(info);
      input.put((byte) counter);
      previous = hmac(prk, input.array());
      int bytesToCopy = Math.min(previous.length, length - offset);
      System.arraycopy(previous, 0, result, offset, bytesToCopy);
      offset += bytesToCopy;
      counter++;
    }
    return result;
  }

  private static byte[] hmac(byte[] key, byte[] message) {
    try {
      Mac mac = Mac.getInstance(HKDF_ALGORITHM);
      mac.init(new SecretKeySpec(key, HKDF_ALGORITHM));
      return mac.doFinal(message);
    } catch (Exception e) {
      throw new RuntimeException("Error computing HKDF HMAC", e);
    }
  }

  private static byte[] buildKdfSalt(RequestContext context) {
    return buildFields(
        context.getVersion(),
        context.getKid(),
        context.getClientId(),
        context.getTenantId(),
        context.getMethod(),
        context.getPath(),
        context.getRequestId()
    );
  }

  private static byte[] buildRequestAad(RequestContext context) {
    return buildFields(
        "request",
        context.getVersion(),
        ALG,
        ENC,
        context.getKid(),
        context.getClientId(),
        context.getTenantId(),
        context.getMethod(),
        context.getPath(),
        Long.toString(context.getTimestamp()),
        context.getNonce(),
        context.getRequestId()
    );
  }

  private static byte[] buildResponseAad(ResponseContext context) {
    return buildFields(
        "response",
        context.getVersion(),
        ENC,
        context.getKid(),
        context.getClientId(),
        context.getTenantId(),
        context.getMethod(),
        context.getPath(),
        context.getRequestId(),
        Integer.toString(context.getStatusCode()),
        Long.toString(context.getTimestamp()),
        context.getNonce()
    );
  }

  private static byte[] buildFields(String... fields) {
    int size = 0;
    byte[][] encodedFields = new byte[fields.length][];
    for (int i = 0; i < fields.length; i++) {
      encodedFields[i] = requireText(fields[i], "aadField").getBytes(StandardCharsets.UTF_8);
      size += Integer.BYTES + encodedFields[i].length;
    }

    ByteBuffer buffer = ByteBuffer.allocate(size);
    for (byte[] field : encodedFields) {
      buffer.putInt(field.length);
      buffer.put(field);
    }
    return buffer.array();
  }

  private static EncryptedParts split(byte[] encrypted) {
    AESUtil.validateEncrypted(encrypted);
    int ciphertextLength = encrypted.length - AESUtil.GCM_NONCE_LENGTH - AESUtil.GCM_TAG_LENGTH_BYTES;
    byte[] iv = new byte[AESUtil.GCM_NONCE_LENGTH];
    byte[] ciphertext = new byte[ciphertextLength];
    byte[] tag = new byte[AESUtil.GCM_TAG_LENGTH_BYTES];
    System.arraycopy(encrypted, 0, iv, 0, iv.length);
    System.arraycopy(encrypted, iv.length, ciphertext, 0, ciphertext.length);
    System.arraycopy(encrypted, iv.length + ciphertext.length, tag, 0, tag.length);
    return new EncryptedParts(
        Base64Util.encodeToString(iv),
        Base64Util.encodeToString(ciphertext),
        Base64Util.encodeToString(tag)
    );
  }

  private static byte[] combine(RequestEnvelope envelope) {
    return combine(envelope.getIv(), envelope.getCiphertext(), envelope.getTag());
  }

  private static byte[] combine(ResponseEnvelope envelope) {
    return combine(envelope.getIv(), envelope.getCiphertext(), envelope.getTag());
  }

  private static byte[] combine(String iv, String ciphertext, String tag) {
    byte[] ivBytes = decodeBase64(requireText(iv, "iv"));
    byte[] ciphertextBytes = decodeBase64(requireNonNull(ciphertext, "ciphertext"));
    byte[] tagBytes = decodeBase64(requireText(tag, "tag"));
    if (ivBytes.length != AESUtil.GCM_NONCE_LENGTH) {
      throw new IllegalArgumentException("GCM IV must be 12 bytes");
    }
    if (tagBytes.length != AESUtil.GCM_TAG_LENGTH_BYTES) {
      throw new IllegalArgumentException("GCM tag must be 16 bytes");
    }
    byte[] encrypted = new byte[ivBytes.length + ciphertextBytes.length + tagBytes.length];
    System.arraycopy(ivBytes, 0, encrypted, 0, ivBytes.length);
    System.arraycopy(ciphertextBytes, 0, encrypted, ivBytes.length, ciphertextBytes.length);
    System.arraycopy(tagBytes, 0, encrypted, ivBytes.length + ciphertextBytes.length, tagBytes.length);
    AESUtil.validateEncrypted(encrypted);
    return encrypted;
  }

  private static byte[] decodeBase64(String value) {
    return Base64.getDecoder().decode(value);
  }

  private static String randomNonce() {
    byte[] nonce = new byte[NONCE_BYTES];
    SECURE_RANDOM.nextBytes(nonce);
    return Base64Util.encodeToString(nonce);
  }

  private static void validateContext(RequestContext context) {
    Objects.requireNonNull(context, "context cannot be null");
    requireText(context.getVersion(), "version");
    requireText(context.getKid(), "kid");
    requireText(context.getClientId(), "clientId");
    requireText(context.getTenantId(), "tenantId");
    requireText(context.getMethod(), "method");
    requireText(context.getPath(), "path");
    requireText(context.getNonce(), "nonce");
    requireText(context.getRequestId(), "requestId");
    if (context.getTimestamp() <= 0) {
      throw new IllegalArgumentException("timestamp must be positive");
    }
  }

  private static void validateEnvelope(RequestEnvelope envelope) {
    Objects.requireNonNull(envelope, "envelope cannot be null");
    validateContext(envelope.toContext());
    requireText(envelope.getEncryptedKey(), "encryptedKey");
    requireText(envelope.getIv(), "iv");
    requireNonNull(envelope.getCiphertext(), "ciphertext");
    requireText(envelope.getTag(), "tag");
  }

  private static void validateAlgorithms(String version, String alg, String enc) {
    requireText(version, "version");
    if (!ALG.equals(alg)) {
      throw new IllegalArgumentException("Unsupported key algorithm: " + alg);
    }
    if (!ENC.equals(enc)) {
      throw new IllegalArgumentException("Unsupported content encryption algorithm: " + enc);
    }
  }

  private static void validateResponseMatchesRequest(ResponseEnvelope envelope, RequestContext requestContext) {
    requireText(envelope.getVersion(), "version");
    if (!Objects.equals(envelope.getVersion(), requestContext.getVersion())) {
      throw new IllegalArgumentException("Response version does not match the request session");
    }
    if (!ENC.equals(envelope.getEnc())) {
      throw new IllegalArgumentException("Unsupported content encryption algorithm: " + envelope.getEnc());
    }
    if (!Objects.equals(envelope.getKid(), requestContext.getKid())
        || !Objects.equals(envelope.getClientId(), requestContext.getClientId())
        || !Objects.equals(envelope.getTenantId(), requestContext.getTenantId())
        || !Objects.equals(envelope.getMethod(), requestContext.getMethod())
        || !Objects.equals(envelope.getPath(), requestContext.getPath())
        || !Objects.equals(envelope.getRequestId(), requestContext.getRequestId())) {
      throw new IllegalArgumentException("Response metadata does not match the request session");
    }
    requireText(envelope.getNonce(), "nonce");
    requireText(envelope.getIv(), "iv");
    requireNonNull(envelope.getCiphertext(), "ciphertext");
    requireText(envelope.getTag(), "tag");
    if (envelope.getTimestamp() <= 0) {
      throw new IllegalArgumentException("timestamp must be positive");
    }
  }

  private static String requireText(String value, String fieldName) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(fieldName + " cannot be null or empty");
    }
    return value;
  }

  private static String requireNonNull(String value, String fieldName) {
    if (value == null) {
      throw new IllegalArgumentException(fieldName + " cannot be null");
    }
    return value;
  }

  private static final class EncryptedParts {
    private final String iv;
    private final String ciphertext;
    private final String tag;

    private EncryptedParts(String iv, String ciphertext, String tag) {
      this.iv = iv;
      this.ciphertext = ciphertext;
      this.tag = tag;
    }

    private String getIv() {
      return iv;
    }

    private String getCiphertext() {
      return ciphertext;
    }

    private String getTag() {
      return tag;
    }
  }

  public static final class ClientRequest {
    private final RequestEnvelope envelope;
    private final ClientSession session;

    private ClientRequest(RequestEnvelope envelope, ClientSession session) {
      this.envelope = envelope;
      this.session = session;
    }

    public RequestEnvelope getEnvelope() {
      return envelope;
    }

    public ClientSession getSession() {
      return session;
    }
  }

  public static final class DecryptedRequest {
    private final String plaintext;
    private final ServerSession session;

    private DecryptedRequest(String plaintext, ServerSession session) {
      this.plaintext = plaintext;
      this.session = session;
    }

    public String getPlaintext() {
      return plaintext;
    }

    public ServerSession getSession() {
      return session;
    }
  }

  public static class RequestContext {
    private final String version;
    private final String kid;
    private final String clientId;
    private final String tenantId;
    private final String method;
    private final String path;
    private final long timestamp;
    private final String nonce;
    private final String requestId;

    public RequestContext(String kid, String clientId, String tenantId, String method, String path,
        long timestamp, String nonce, String requestId) {
      this(DEFAULT_VERSION, kid, clientId, tenantId, method, path, timestamp, nonce, requestId);
    }

    public RequestContext(String version, String kid, String clientId, String tenantId, String method, String path,
        long timestamp, String nonce, String requestId) {
      this.version = version;
      this.kid = kid;
      this.clientId = clientId;
      this.tenantId = tenantId;
      this.method = method;
      this.path = path;
      this.timestamp = timestamp;
      this.nonce = nonce;
      this.requestId = requestId;
    }

    public String getVersion() {
      return version;
    }

    public String getKid() {
      return kid;
    }

    public String getClientId() {
      return clientId;
    }

    public String getTenantId() {
      return tenantId;
    }

    public String getMethod() {
      return method;
    }

    public String getPath() {
      return path;
    }

    public long getTimestamp() {
      return timestamp;
    }

    public String getNonce() {
      return nonce;
    }

    public String getRequestId() {
      return requestId;
    }
  }

  public static final class RequestEnvelope {
    private final String version;
    private final String alg;
    private final String enc;
    private final String kid;
    private final String clientId;
    private final String tenantId;
    private final String method;
    private final String path;
    private final long timestamp;
    private final String nonce;
    private final String requestId;
    private final String encryptedKey;
    private final String iv;
    private final String ciphertext;
    private final String tag;

    public RequestEnvelope(String version, String alg, String enc, String kid, String clientId,
        String tenantId, String method, String path, long timestamp, String nonce, String requestId,
        String encryptedKey, String iv, String ciphertext, String tag) {
      this.version = version;
      this.alg = alg;
      this.enc = enc;
      this.kid = kid;
      this.clientId = clientId;
      this.tenantId = tenantId;
      this.method = method;
      this.path = path;
      this.timestamp = timestamp;
      this.nonce = nonce;
      this.requestId = requestId;
      this.encryptedKey = encryptedKey;
      this.iv = iv;
      this.ciphertext = ciphertext;
      this.tag = tag;
    }

    private RequestContext toContext() {
      return new RequestContext(version, kid, clientId, tenantId, method, path, timestamp, nonce, requestId);
    }

    public String getVersion() {
      return version;
    }

    public String getAlg() {
      return alg;
    }

    public String getEnc() {
      return enc;
    }

    public String getKid() {
      return kid;
    }

    public String getClientId() {
      return clientId;
    }

    public String getTenantId() {
      return tenantId;
    }

    public String getMethod() {
      return method;
    }

    public String getPath() {
      return path;
    }

    public long getTimestamp() {
      return timestamp;
    }

    public String getNonce() {
      return nonce;
    }

    public String getRequestId() {
      return requestId;
    }

    public String getEncryptedKey() {
      return encryptedKey;
    }

    public String getIv() {
      return iv;
    }

    public String getCiphertext() {
      return ciphertext;
    }

    public String getTag() {
      return tag;
    }
  }

  public static final class ResponseEnvelope {
    private final String version;
    private final String enc;
    private final String kid;
    private final String clientId;
    private final String tenantId;
    private final String method;
    private final String path;
    private final String requestId;
    private final int statusCode;
    private final long timestamp;
    private final String nonce;
    private final String iv;
    private final String ciphertext;
    private final String tag;

    public ResponseEnvelope(String version, String enc, String kid, String clientId, String tenantId,
        String method, String path, String requestId, int statusCode, long timestamp, String nonce,
        String iv, String ciphertext, String tag) {
      this.version = version;
      this.enc = enc;
      this.kid = kid;
      this.clientId = clientId;
      this.tenantId = tenantId;
      this.method = method;
      this.path = path;
      this.requestId = requestId;
      this.statusCode = statusCode;
      this.timestamp = timestamp;
      this.nonce = nonce;
      this.iv = iv;
      this.ciphertext = ciphertext;
      this.tag = tag;
    }

    private ResponseContext toContext() {
      return new ResponseContext(version, kid, clientId, tenantId, method, path, requestId, statusCode, timestamp,
          nonce);
    }

    public String getVersion() {
      return version;
    }

    public String getEnc() {
      return enc;
    }

    public String getKid() {
      return kid;
    }

    public String getClientId() {
      return clientId;
    }

    public String getTenantId() {
      return tenantId;
    }

    public String getMethod() {
      return method;
    }

    public String getPath() {
      return path;
    }

    public String getRequestId() {
      return requestId;
    }

    public int getStatusCode() {
      return statusCode;
    }

    public long getTimestamp() {
      return timestamp;
    }

    public String getNonce() {
      return nonce;
    }

    public String getIv() {
      return iv;
    }

    public String getCiphertext() {
      return ciphertext;
    }

    public String getTag() {
      return tag;
    }
  }

  private static final class ResponseContext {
    private final String version;
    private final String kid;
    private final String clientId;
    private final String tenantId;
    private final String method;
    private final String path;
    private final String requestId;
    private final int statusCode;
    private final long timestamp;
    private final String nonce;

    private ResponseContext(String version, String kid, String clientId, String tenantId, String method, String path,
        String requestId, int statusCode, long timestamp, String nonce) {
      this.version = version;
      this.kid = kid;
      this.clientId = clientId;
      this.tenantId = tenantId;
      this.method = method;
      this.path = path;
      this.requestId = requestId;
      this.statusCode = statusCode;
      this.timestamp = timestamp;
      this.nonce = nonce;
    }

    private static ResponseContext fromRequest(RequestContext requestContext, int statusCode,
        long timestamp, String nonce) {
      return new ResponseContext(
          requestContext.getVersion(),
          requestContext.getKid(),
          requestContext.getClientId(),
          requestContext.getTenantId(),
          requestContext.getMethod(),
          requestContext.getPath(),
          requestContext.getRequestId(),
          statusCode,
          timestamp,
          nonce
      );
    }

    private String getVersion() {
      return version;
    }

    private String getKid() {
      return kid;
    }

    private String getClientId() {
      return clientId;
    }

    private String getTenantId() {
      return tenantId;
    }

    private String getMethod() {
      return method;
    }

    private String getPath() {
      return path;
    }

    private String getRequestId() {
      return requestId;
    }

    private int getStatusCode() {
      return statusCode;
    }

    private long getTimestamp() {
      return timestamp;
    }

    private String getNonce() {
      return nonce;
    }
  }

  public static class ClientSession {
    private final byte[] cek;
    private final RequestContext context;

    private ClientSession(byte[] cek, RequestContext context) {
      this.cek = cek.clone();
      this.context = context;
    }

    private byte[] getCek() {
      return cek.clone();
    }

    public RequestContext getContext() {
      return context;
    }
  }

  public static class ServerSession {
    private final byte[] cek;
    private final RequestContext context;

    private ServerSession(byte[] cek, RequestContext context) {
      this.cek = cek.clone();
      this.context = context;
    }

    private byte[] getCek() {
      return cek.clone();
    }

    public RequestContext getContext() {
      return context;
    }
  }
}
