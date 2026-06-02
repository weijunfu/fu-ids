import assert from "node:assert/strict";
import * as namespace from "../dist/index.mjs";
import fuIds from "../dist/index.mjs";

assert.deepEqual(Object.keys(namespace), ["default"]);
assert.equal(Object.isFrozen(fuIds), true);
assert.equal(typeof fuIds.newRequestContext, "function");
assert.equal(typeof fuIds.encryptRequest, "function");
assert.equal(typeof fuIds.decryptResponse, "function");
assert.equal(Object.hasOwn(fuIds, "VERSION"), false);

assert.throws(() => {
  fuIds.encryptRequest = null;
}, TypeError);

const context = fuIds.newRequestContext({
  version: "v2",
  kid: "kid",
  clientId: "client",
  tenantId: "tenant",
  method: "POST",
  path: "/api/orders"
});
assert.equal(Object.isFrozen(context), true);
assert.equal(context.version, "v2");
assert.throws(() => {
  context.kid = "changed";
}, TypeError);

const session = fuIds.importClientSession({
  cek: "MDEyMzQ1Njc4OUFCQ0RFRjAxMjM0NTY3ODlBQkNERUY=",
  context
});
assert.equal(Object.isFrozen(session), true);
assert.equal(Object.isFrozen(session.context), true);
assert.equal(Object.hasOwn(session, "cek"), false);
