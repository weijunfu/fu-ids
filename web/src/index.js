const DEFAULT_VERSION = "v1";
const ALG = "RSA-OAEP-256";
const ENC = "A256GCM";
const AES_256_KEY_BYTES = 32;
const GCM_IV_BYTES = 12;
const GCM_TAG_BYTES = 16;
const KEY_INFO_PREFIX = "fu-ids:erp:";

const textEncoder = new TextEncoder();
const textDecoder = new TextDecoder();
const sessionKeys = new WeakMap();

async function encryptRequest(requestBody, serverPublicKey, context) {
  const requestContext = newRequestContext(context);
  validateContext(requestContext);
  const plaintext = bodyToString(requestBody);
  const cek = randomBytes(AES_256_KEY_BYTES);
  const encryptedKey = await wrapAesKeyByPublicKey(cek, serverPublicKey);
  const requestKey = await importAesKey(await deriveKey(cek, requestContext, requestKeyInfo(requestContext.version)));
  const encrypted = await encryptAesGcm(requestKey, utf8(plaintext), buildRequestAad(requestContext));
  const parts = splitEncrypted(encrypted);

  return {
    envelope: {
      version: requestContext.version,
      alg: ALG,
      enc: ENC,
      kid: requestContext.kid,
      clientId: requestContext.clientId,
      tenantId: requestContext.tenantId,
      method: requestContext.method,
      path: requestContext.path,
      timestamp: requestContext.timestamp,
      nonce: requestContext.nonce,
      requestId: requestContext.requestId,
      encryptedKey,
      iv: bytesToBase64(parts.iv),
      ciphertext: bytesToBase64(parts.ciphertext),
      tag: bytesToBase64(parts.tag)
    },
    session: createSession(cek, requestContext)
  };
}

async function decryptResponse(envelope, session) {
  if (!session) {
    throw new Error("session cannot be null");
  }
  validateResponseMatchesRequest(envelope, session.context);
  const responseKey = await importAesKey(
    await deriveKey(getSessionCek(session), session.context, responseKeyInfo(session.context.version))
  );
  const encrypted = combineEncrypted(envelope);
  const plaintext = await decryptAesGcm(responseKey, encrypted, buildResponseAad(responseContextFromEnvelope(envelope)));
  return textDecoder.decode(plaintext);
}

function newRequestContext({ version, kid, clientId, tenantId, method, path, timestamp, nonce, requestId } = {}) {
  return freezePlainObject({
    version: version ?? DEFAULT_VERSION,
    kid,
    clientId,
    tenantId,
    method,
    path,
    timestamp: timestamp ?? Date.now(),
    nonce: nonce ?? randomBase64(16),
    requestId: requestId ?? randomUuid()
  });
}

function exportClientSession(session) {
  return exportSession(session);
}

function importClientSession(session) {
  return importSession(session);
}

async function decryptRequest(envelope, serverPrivateKey) {
  validateRequestEnvelope(envelope);
  validateAlgorithms(envelope.version, envelope.alg, envelope.enc);
  const cek = await unwrapAesKeyByPrivateKey(envelope.encryptedKey, serverPrivateKey);
  const context = requestContextFromEnvelope(envelope);
  const requestKey = await importAesKey(await deriveKey(cek, context, requestKeyInfo(context.version)));
  const encrypted = combineEncrypted(envelope);
  const plaintext = await decryptAesGcm(requestKey, encrypted, buildRequestAad(context));

  return {
    plaintext: textDecoder.decode(plaintext),
    session: createSession(cek, context)
  };
}

async function encryptResponse(responseBody, session, statusCode) {
  if (!session) {
    throw new Error("session cannot be null");
  }
  const requestContext = session.context;
  validateContext(requestContext);
  validateStatusCode(statusCode);
  const plaintext = bodyToString(responseBody);
  const context = {
    version: requestContext.version,
    kid: requestContext.kid,
    clientId: requestContext.clientId,
    tenantId: requestContext.tenantId,
    method: requestContext.method,
    path: requestContext.path,
    requestId: requestContext.requestId,
    statusCode,
    timestamp: Date.now(),
    nonce: randomBase64(16)
  };
  const responseKey = await importAesKey(
    await deriveKey(getSessionCek(session), requestContext, responseKeyInfo(requestContext.version))
  );
  const encrypted = await encryptAesGcm(responseKey, utf8(plaintext), buildResponseAad(context));
  const parts = splitEncrypted(encrypted);

  return {
    version: context.version,
    enc: ENC,
    kid: context.kid,
    clientId: context.clientId,
    tenantId: context.tenantId,
    method: context.method,
    path: context.path,
    requestId: context.requestId,
    statusCode: context.statusCode,
    timestamp: context.timestamp,
    nonce: context.nonce,
    iv: bytesToBase64(parts.iv),
    ciphertext: bytesToBase64(parts.ciphertext),
    tag: bytesToBase64(parts.tag)
  };
}

function exportServerSession(session) {
  return exportSession(session);
}

function importServerSession(session) {
  return importSession(session);
}

function exportSession(session) {
  return freezePlainObject({
    cek: bytesToBase64(getSessionCek(session)),
    context: freezePlainObject({ ...session.context })
  });
}

function importSession(session) {
  if (!session || !session.cek || !session.context) {
    throw new Error("session must contain cek and context");
  }
  validateContext(session.context);
  return createSession(base64ToBytes(session.cek), session.context);
}

async function wrapAesKeyByPublicKey(cek, serverPublicKey) {
  validateAesKeyBytes(cek);
  const crypto = requireCrypto();
  const publicKey = await crypto.subtle.importKey(
    "spki",
    base64ToBytes(serverPublicKey),
    { name: "RSA-OAEP", hash: "SHA-256" },
    false,
    ["encrypt"]
  );
  const encrypted = await crypto.subtle.encrypt({ name: "RSA-OAEP" }, publicKey, cek);
  return bytesToBase64(new Uint8Array(encrypted));
}

async function unwrapAesKeyByPrivateKey(encryptedKey, serverPrivateKey) {
  const crypto = requireCrypto();
  const privateKey = await crypto.subtle.importKey(
    "pkcs8",
    base64ToBytes(serverPrivateKey),
    { name: "RSA-OAEP", hash: "SHA-256" },
    false,
    ["decrypt"]
  );
  const decrypted = await crypto.subtle.decrypt(
    { name: "RSA-OAEP" },
    privateKey,
    base64ToBytes(requireText(encryptedKey, "encryptedKey"))
  );
  const cek = new Uint8Array(decrypted);
  validateAesKeyBytes(cek);
  return cek;
}

async function importAesKey(keyBytes) {
  validateAesKeyBytes(keyBytes);
  const crypto = requireCrypto();
  return crypto.subtle.importKey("raw", keyBytes, { name: "AES-GCM" }, false, ["encrypt", "decrypt"]);
}

async function encryptAesGcm(key, plaintext, aad) {
  const crypto = requireCrypto();
  const iv = randomBytes(GCM_IV_BYTES);
  const encrypted = await crypto.subtle.encrypt(
    {
      name: "AES-GCM",
      iv,
      additionalData: aad,
      tagLength: 128
    },
    key,
    plaintext
  );
  return { iv, data: new Uint8Array(encrypted) };
}

async function decryptAesGcm(key, encrypted, aad) {
  const crypto = requireCrypto();
  return new Uint8Array(await crypto.subtle.decrypt(
    {
      name: "AES-GCM",
      iv: encrypted.iv,
      additionalData: aad,
      tagLength: 128
    },
    key,
    encrypted.data
  ));
}

async function deriveKey(cek, context, info) {
  const salt = buildKdfSalt(context);
  const prk = await hmac(salt, cek);
  return hkdfExpand(prk, utf8(info), AES_256_KEY_BYTES);
}

function requestKeyInfo(version) {
  return keyInfo("request", version);
}

function responseKeyInfo(version) {
  return keyInfo("response", version);
}

function keyInfo(direction, version) {
  return `${KEY_INFO_PREFIX}${direction}:${requireText(version, "version")}`;
}

async function hkdfExpand(prk, info, length) {
  const result = new Uint8Array(length);
  let previous = new Uint8Array(0);
  let offset = 0;
  let counter = 1;
  while (offset < length) {
    const input = concatBytes(previous, info, new Uint8Array([counter]));
    previous = await hmac(prk, input);
    const chunk = previous.subarray(0, Math.min(previous.length, length - offset));
    result.set(chunk, offset);
    offset += chunk.length;
    counter += 1;
  }
  return result;
}

async function hmac(keyBytes, messageBytes) {
  const crypto = requireCrypto();
  const key = await crypto.subtle.importKey(
    "raw",
    keyBytes,
    { name: "HMAC", hash: "SHA-256" },
    false,
    ["sign"]
  );
  return new Uint8Array(await crypto.subtle.sign("HMAC", key, messageBytes));
}

function buildKdfSalt(context) {
  return buildFields(
    context.version,
    context.kid,
    context.clientId,
    context.tenantId,
    context.method,
    context.path,
    context.requestId
  );
}

function buildRequestAad(context) {
  return buildFields(
    "request",
    context.version,
    ALG,
    ENC,
    context.kid,
    context.clientId,
    context.tenantId,
    context.method,
    context.path,
    String(context.timestamp),
    context.nonce,
    context.requestId
  );
}

function buildResponseAad(context) {
  return buildFields(
    "response",
    context.version,
    ENC,
    context.kid,
    context.clientId,
    context.tenantId,
    context.method,
    context.path,
    context.requestId,
    String(context.statusCode),
    String(context.timestamp),
    context.nonce
  );
}

function buildFields(...fields) {
  const encodedFields = fields.map((field) => utf8(requireAadField(field)));
  const size = encodedFields.reduce((total, field) => total + 4 + field.length, 0);
  const result = new Uint8Array(size);
  const view = new DataView(result.buffer);
  let offset = 0;
  for (const field of encodedFields) {
    view.setUint32(offset, field.length);
    offset += 4;
    result.set(field, offset);
    offset += field.length;
  }
  return result;
}

function splitEncrypted(encrypted) {
  const data = encrypted.data;
  if (data.length < GCM_TAG_BYTES) {
    throw new Error("Encrypted data must contain a 16-byte GCM tag");
  }
  return {
    iv: encrypted.iv,
    ciphertext: data.subarray(0, data.length - GCM_TAG_BYTES),
    tag: data.subarray(data.length - GCM_TAG_BYTES)
  };
}

function combineEncrypted(envelope) {
  const iv = base64ToBytes(requireText(envelope.iv, "iv"));
  const ciphertext = base64ToBytes(requireNonNull(envelope.ciphertext, "ciphertext"));
  const tag = base64ToBytes(requireText(envelope.tag, "tag"));
  if (iv.length !== GCM_IV_BYTES) {
    throw new Error("GCM IV must be 12 bytes");
  }
  if (tag.length !== GCM_TAG_BYTES) {
    throw new Error("GCM tag must be 16 bytes");
  }
  return { iv, data: concatBytes(ciphertext, tag) };
}

function validateRequestEnvelope(envelope) {
  if (!envelope) {
    throw new Error("envelope cannot be null");
  }
  validateContext(requestContextFromEnvelope(envelope));
  requireText(envelope.encryptedKey, "encryptedKey");
  requireText(envelope.iv, "iv");
  requireNonNull(envelope.ciphertext, "ciphertext");
  requireText(envelope.tag, "tag");
}

function validateContext(context) {
  if (!context) {
    throw new Error("context cannot be null");
  }
  requireText(context.version, "version");
  requireText(context.kid, "kid");
  requireText(context.clientId, "clientId");
  requireText(context.tenantId, "tenantId");
  requireText(context.method, "method");
  requireText(context.path, "path");
  requireText(context.nonce, "nonce");
  requireText(context.requestId, "requestId");
  if (!Number.isFinite(Number(context.timestamp)) || Number(context.timestamp) <= 0) {
    throw new Error("timestamp must be positive");
  }
}

function validateAlgorithms(version, alg, enc) {
  requireText(version, "version");
  if (alg !== ALG) {
    throw new Error(`Unsupported key algorithm: ${alg}`);
  }
  if (enc !== ENC) {
    throw new Error(`Unsupported content encryption algorithm: ${enc}`);
  }
}

function validateStatusCode(statusCode) {
  if (!Number.isInteger(Number(statusCode))) {
    throw new Error("statusCode must be an integer");
  }
}

function validateResponseMatchesRequest(envelope, requestContext) {
  if (!envelope) {
    throw new Error("envelope cannot be null");
  }
  requireText(envelope.version, "version");
  if (envelope.version !== requestContext.version) {
    throw new Error("Response version does not match the request session");
  }
  if (envelope.enc !== ENC) {
    throw new Error(`Unsupported content encryption algorithm: ${envelope.enc}`);
  }
  if (envelope.kid !== requestContext.kid
      || envelope.clientId !== requestContext.clientId
      || envelope.tenantId !== requestContext.tenantId
      || envelope.method !== requestContext.method
      || envelope.path !== requestContext.path
      || envelope.requestId !== requestContext.requestId) {
    throw new Error("Response metadata does not match the request session");
  }
  requireText(envelope.nonce, "nonce");
  requireText(envelope.iv, "iv");
  requireNonNull(envelope.ciphertext, "ciphertext");
  requireText(envelope.tag, "tag");
  validateStatusCode(envelope.statusCode);
  if (!Number.isFinite(Number(envelope.timestamp)) || Number(envelope.timestamp) <= 0) {
    throw new Error("timestamp must be positive");
  }
}

function requestContextFromEnvelope(envelope) {
  return freezePlainObject({
    version: envelope.version,
    kid: envelope.kid,
    clientId: envelope.clientId,
    tenantId: envelope.tenantId,
    method: envelope.method,
    path: envelope.path,
    timestamp: envelope.timestamp,
    nonce: envelope.nonce,
    requestId: envelope.requestId
  });
}

function responseContextFromEnvelope(envelope) {
  return {
    version: envelope.version,
    kid: envelope.kid,
    clientId: envelope.clientId,
    tenantId: envelope.tenantId,
    method: envelope.method,
    path: envelope.path,
    requestId: envelope.requestId,
    statusCode: envelope.statusCode,
    timestamp: envelope.timestamp,
    nonce: envelope.nonce
  };
}

function createSession(cek, context) {
  validateAesKeyBytes(cek);
  const session = freezePlainObject({ context: freezePlainObject({ ...context }) });
  sessionKeys.set(session, cloneBytes(cek));
  return session;
}

function getSessionCek(session) {
  const cek = sessionKeys.get(session);
  if (!cek) {
    throw new Error("session does not contain a CEK");
  }
  return cloneBytes(cek);
}

function validateAesKeyBytes(keyBytes) {
  if (!(keyBytes instanceof Uint8Array) || keyBytes.length !== AES_256_KEY_BYTES) {
    throw new Error("AES key must be 32 bytes");
  }
}

function randomBase64(length) {
  return bytesToBase64(randomBytes(length));
}

function randomUuid() {
  const crypto = requireCrypto();
  if (typeof crypto.randomUUID === "function") {
    return crypto.randomUUID();
  }
  const bytes = randomBytes(16);
  bytes[6] = (bytes[6] & 0x0f) | 0x40;
  bytes[8] = (bytes[8] & 0x3f) | 0x80;
  const hex = [...bytes].map((byte) => byte.toString(16).padStart(2, "0"));
  return `${hex.slice(0, 4).join("")}-${hex.slice(4, 6).join("")}-${hex.slice(6, 8).join("")}`
      + `-${hex.slice(8, 10).join("")}-${hex.slice(10, 16).join("")}`;
}

function randomBytes(length) {
  const bytes = new Uint8Array(length);
  requireCrypto().getRandomValues(bytes);
  return bytes;
}

function bytesToBase64(bytes) {
  if (typeof Buffer !== "undefined") {
    return Buffer.from(bytes).toString("base64");
  }
  let binary = "";
  for (let offset = 0; offset < bytes.length; offset += 0x8000) {
    binary += String.fromCharCode(...bytes.subarray(offset, offset + 0x8000));
  }
  return btoa(binary);
}

function base64ToBytes(value) {
  const normalized = normalizeBase64(value);
  if (normalized === "") {
    return new Uint8Array(0);
  }
  if (typeof Buffer !== "undefined") {
    return new Uint8Array(Buffer.from(normalized, "base64"));
  }
  const binary = atob(normalized);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) {
    bytes[i] = binary.charCodeAt(i);
  }
  return bytes;
}

function normalizeBase64(value) {
  return requireNonNull(value, "base64")
    .replace(/-----BEGIN [^-]+-----/g, "")
    .replace(/-----END [^-]+-----/g, "")
    .replace(/\s+/g, "");
}

function concatBytes(...arrays) {
  const length = arrays.reduce((total, array) => total + array.length, 0);
  const result = new Uint8Array(length);
  let offset = 0;
  for (const array of arrays) {
    result.set(array, offset);
    offset += array.length;
  }
  return result;
}

function cloneBytes(bytes) {
  return new Uint8Array(bytes);
}

function bodyToString(value) {
  if (value == null) {
    throw new Error("body cannot be null");
  }
  return typeof value === "string" ? value : JSON.stringify(value);
}

function utf8(value) {
  return textEncoder.encode(value);
}

function requireAadField(value) {
  if (value == null) {
    throw new Error("aadField cannot be null or empty");
  }
  const text = String(value);
  if (text.trim() === "") {
    throw new Error("aadField cannot be null or empty");
  }
  return text;
}

function requireText(value, fieldName) {
  if (typeof value !== "string" || value.trim() === "") {
    throw new Error(`${fieldName} cannot be null or empty`);
  }
  return value;
}

function requireNonNull(value, fieldName) {
  if (value == null) {
    throw new Error(`${fieldName} cannot be null`);
  }
  return value;
}

function freezePlainObject(value) {
  return Object.freeze(value);
}

function requireCrypto() {
  if (!globalThis.crypto || !globalThis.crypto.subtle) {
    throw new Error("Web Crypto API is required");
  }
  return globalThis.crypto;
}

const fuIds = Object.freeze({
  newRequestContext,
  encryptRequest,
  decryptResponse,
  exportClientSession,
  importClientSession,
  decryptRequest,
  encryptResponse,
  exportServerSession,
  importServerSession
});

export default fuIds;
