# fu-ids

ERP request/response payload encryption utility.

## Usage

```js
import fuIds from "fu-ids";

const context = fuIds.newRequestContext({
  version: "v2",
  kid: "erp-api-rsa-2026-01",
  clientId: "client-a",
  tenantId: "tenant-001",
  method: "POST",
  path: "/api/orders"
});

const { envelope, session } = await fuIds.encryptRequest(
  JSON.stringify({ orderNo: "SO-001" }),
  serverPublicKey,
  context
);

const responseEnvelope = await fetch("/api/orders", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify(envelope)
}).then((response) => response.json());

const responseBody = await fuIds.decryptResponse(responseEnvelope, session);
```

`version` is optional and defaults to `v1`. When configured, it is bound into the envelope, AAD, and HKDF info strings:

- `fu-ids:erp:request:{version}`
- `fu-ids:erp:response:{version}`

The package exposes one frozen entry object. Internal constants, helper functions, and session keys are not exported.

## Build

```bash
npm run build
```

Build output:

- `dist/index.mjs`
- `dist/index.cjs`

Only `dist` is included when publishing to npm.
