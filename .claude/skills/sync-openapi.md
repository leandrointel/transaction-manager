---
name: sync-openapi
description: Generate or update the OpenAPI spec for the transaction-manager project after any controller or DTO change. Use when asked to update/sync/regenerate the OpenAPI spec, or proactively after modifying controllers or DTOs.
triggers:
  - /sync-openapi
  - update openapi
  - regenerate openapi spec
  - sync api spec
---

## Skill: sync-openapi

Keep the OpenAPI specification in sync with the current controller and DTO code.

### When to invoke
- After any change to `TransactionController.java`
- After adding, removing, or renaming fields in any DTO class
- After modifying `@Operation`, `@ApiResponse`, or `@Schema` annotations
- When the user asks to update or validate the OpenAPI spec

### Steps

1. Start the application temporarily in the background:
   ```bash
   ./gradlew bootRun &
   sleep 15
   ```
2. Download the live spec:
   ```bash
   curl -s http://localhost:8080/v3/api-docs -o src/main/resources/openapi.json
   ```
3. Stop the background application.
4. Alternatively, use the Gradle plugin (requires the app to be running):
   ```bash
   ./gradlew generateOpenApiDocs
   # output: build/openapi/openapi.json
   cp build/openapi/openapi.json src/main/resources/openapi.json
   ```
5. Show a diff of what changed in `openapi.json` compared to the previous version.
6. Commit the updated spec with message: `docs: update openapi spec`.

### Validation checklist
After generating, verify the spec contains:
- `PUT /transactions/{transactionId}` with request body schema (amount, type, parent_id)
- `GET /transactions/types/{type}` returning array of longs
- `GET /transactions/sum/{transactionId}` returning `{"sum": double}`
- 400/404 error responses documented
- All DTOs listed under `components/schemas`

### Output location
`src/main/resources/openapi.json`
