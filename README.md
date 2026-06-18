# Transaction Manager

REST API for creating, querying, and aggregating financial transactions organized as trees via parent-child relationships.

Built with **Java 18 · Spring Boot 3.2 · Gradle**.

---

## Running locally

```bash
./gradlew bootRun
```

Server starts at `http://localhost:8080`.

---

## Running tests

```bash
./gradlew test
```

---

## Running with Docker

```bash
docker compose up --build
```

---

## Health check

```
GET /actuator/health
```

---

## API

### Create or update a transaction

```
PUT /transactions/{transactionId}
```

**Body**

| Field | Type | Required | Description |
|---|---|---|---|
| `amount` | number | yes | Monetary amount |
| `type` | string | yes | See transaction types below |
| `parent_id` | number | no | ID of the parent transaction |

**Transaction types:** `CARS`, `SHOPPING`, `FOOD`, `SALARY`, `TRAVEL`, `ENTERTAINMENT`, `HEALTH`, `OTHER`

**Example — root transaction**

```bash
curl -X PUT http://localhost:8080/transactions/10 \
  -H "Content-Type: application/json" \
  -d '{"amount": 5000.00, "type": "CARS"}'
```

**Example — child transaction**

```bash
curl -X PUT http://localhost:8080/transactions/11 \
  -H "Content-Type: application/json" \
  -d '{"amount": 42.50, "type": "FOOD", "parent_id": 10}'
```

**Response `200`**
```json
{ "status": "ok" }
```

---

### Get transaction IDs by type

```
GET /transactions/types/{type}
```

**Example**

```bash
curl http://localhost:8080/transactions/types/CARS
```

**Response `200`**
```json
[10, 11, 12]
```

---

### Get transitive sum of a transaction tree

Returns the sum of a transaction's amount plus all transactions transitively linked to it via `parent_id`, traversing the full subtree downward.

```
GET /transactions/sum/{transactionId}
```

**Example**

```bash
curl http://localhost:8080/transactions/sum/10
```

**Response `200`**
```json
{ "sum": 5042.50 }
```

---

## Error responses

All errors follow this shape:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "amount: amount is required",
  "timestamp": "2026-06-17T12:00:00Z"
}
```

| Status | Cause |
|---|---|
| `400` | Validation error, invalid type, missing field, parent not found |
| `404` | Transaction ID does not exist |
| `405` | HTTP method not allowed on that route |
| `500` | Unexpected server error |

---

## OpenAPI spec

The full spec is available at [`openapi.yaml`](openapi.yaml) and can be visualized by pasting it into [editor.swagger.io](https://editor.swagger.io).

```yaml
openapi: 3.0.3
info:
  title: Transaction Manager API
  description: REST API for creating, querying, and aggregating financial transactions.
  version: 0.0.1-SNAPSHOT

servers:
  - url: http://localhost:8080
    description: Local development server

tags:
  - name: Transactions
    description: Transaction management API

paths:
  /transactions/{transactionId}:
    put:
      tags: [Transactions]
      summary: Create or update a transaction
      operationId: saveTransaction
      parameters:
        - name: transactionId
          in: path
          required: true
          schema:
            type: integer
            format: int64
          example: 10
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/TransactionRequest'
            examples:
              car_no_parent:
                summary: New car transaction without parent
                value:
                  amount: 5000.00
                  type: CARS
              food_with_parent:
                summary: Food transaction linked to a parent
                value:
                  amount: 42.50
                  type: FOOD
                  parent_id: 10
      responses:
        '200':
          description: Transaction saved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/StatusResponse'
              example:
                status: ok
        '400':
          description: Validation error or invalid transaction data
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
              examples:
                missing_amount:
                  value:
                    status: 400
                    error: Bad Request
                    message: "amount: amount is required"
                    timestamp: "2026-06-17T12:00:00Z"
                invalid_type:
                  value:
                    status: 400
                    error: Bad Request
                    message: "Invalid transaction type: 'XYZ'. Valid values: [CARS, SHOPPING, FOOD, SALARY, TRAVEL, ENTERTAINMENT, HEALTH, OTHER]"
                    timestamp: "2026-06-17T12:00:00Z"
        '500':
          $ref: '#/components/responses/InternalServerError'

  /transactions/types/{type}:
    get:
      tags: [Transactions]
      summary: Get transaction IDs by type
      operationId: getByType
      parameters:
        - name: type
          in: path
          required: true
          schema:
            $ref: '#/components/schemas/TransactionType'
          example: CARS
      responses:
        '200':
          description: List of transaction IDs matching the given type
          content:
            application/json:
              schema:
                type: array
                items:
                  type: integer
                  format: int64
              example: [10, 11, 12]
        '400':
          description: Unknown transaction type
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
              example:
                status: 400
                error: Bad Request
                message: "Invalid transaction type: 'XYZ'. Valid values: [CARS, SHOPPING, FOOD, SALARY, TRAVEL, ENTERTAINMENT, HEALTH, OTHER]"
                timestamp: "2026-06-17T12:00:00Z"
        '500':
          $ref: '#/components/responses/InternalServerError'

  /transactions/sum/{transactionId}:
    get:
      tags: [Transactions]
      summary: Get transitive sum of a transaction tree
      description: >
        Returns the sum of the given transaction's amount plus the amounts of all
        transactions that are transitively linked to it via `parent_id`.
      operationId: getSum
      parameters:
        - name: transactionId
          in: path
          required: true
          schema:
            type: integer
            format: int64
          example: 10
      responses:
        '200':
          description: Transitive sum of the transaction tree
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/SumResponse'
              example:
                sum: 5042.50
        '404':
          description: Transaction not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
              example:
                status: 404
                error: Not Found
                message: "Transaction not found with id: 99"
                timestamp: "2026-06-17T12:00:00Z"
        '500':
          $ref: '#/components/responses/InternalServerError'

components:
  schemas:
    TransactionType:
      type: string
      enum: [CARS, SHOPPING, FOOD, SALARY, TRAVEL, ENTERTAINMENT, HEALTH, OTHER]
      description: Valid transaction type identifiers (case-insensitive on input)

    TransactionRequest:
      type: object
      required: [amount, type]
      properties:
        amount:
          type: number
          format: double
          description: Monetary amount of the transaction
          example: 5000.00
        type:
          $ref: '#/components/schemas/TransactionType'
        parent_id:
          type: integer
          format: int64
          nullable: true
          description: ID of the parent transaction, if this transaction belongs to a tree
          example: 10

    StatusResponse:
      type: object
      properties:
        status:
          type: string
          example: ok

    SumResponse:
      type: object
      properties:
        sum:
          type: number
          format: double
          description: Transitive sum of the transaction tree
          example: 5042.50

    ErrorResponse:
      type: object
      properties:
        status:
          type: integer
          example: 400
        error:
          type: string
          example: Bad Request
        message:
          type: string
          example: amount is required
        timestamp:
          type: string
          format: date-time
          example: "2026-06-17T12:00:00Z"

  responses:
    InternalServerError:
      description: Unexpected server error
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
          example:
            status: 500
            error: Internal Server Error
            message: An unexpected error occurred
            timestamp: "2026-06-17T12:00:00Z"
```
