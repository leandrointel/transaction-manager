package com.transactionmanager.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transactionmanager.dto.TransactionRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full integration tests for the Transaction Manager REST API.
 * Each nested class gets a fresh context via @DirtiesContext to guarantee store isolation.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("Transaction Manager – Integration Tests")
class TransactionIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    // ─────────────────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────────────────

    private void putTransaction(long id, double amount, String type, Long parentId) throws Exception {
        TransactionRequestDTO dto = TransactionRequestDTO.builder()
                .amount(amount)
                .type(type)
                .parentId(parentId)
                .build();

        mockMvc.perform(put("/transactions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /transactions/{transactionId}
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /transactions/{transactionId}")
    class PutTransaction {

        @Test
        @DisplayName("should create a root transaction and return status ok")
        void createRootTransaction() throws Exception {
            putTransaction(10L, 5000.0, "cars", null);
        }

        @Test
        @DisplayName("should create a child transaction linked to an existing parent")
        void createChildTransaction() throws Exception {
            putTransaction(10L, 5000.0, "cars", null);
            putTransaction(11L, 10000.0, "shopping", 10L);
        }

        @Test
        @DisplayName("should overwrite an existing transaction with the same id")
        void overwriteExistingTransaction() throws Exception {
            putTransaction(20L, 100.0, "food", null);
            putTransaction(20L, 200.0, "food", null);

            mockMvc.perform(get("/transactions/sum/20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sum").value(200.0));
        }

        @Test
        @DisplayName("should return 400 when amount is missing")
        void missingAmount() throws Exception {
            String body = """
                    {"type": "cars"}
                    """;
            mockMvc.perform(put("/transactions/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"));
        }

        @Test
        @DisplayName("should return 400 when type is missing")
        void missingType() throws Exception {
            String body = """
                    {"amount": 100.0}
                    """;
            mockMvc.perform(put("/transactions/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("should return 400 when type is blank")
        void blankType() throws Exception {
            String body = """
                    {"amount": 100.0, "type": "   "}
                    """;
            mockMvc.perform(put("/transactions/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when parent_id does not exist")
        void nonExistentParentId() throws Exception {
            String body = """
                    {"amount": 100.0, "type": "cars", "parent_id": 9999}
                    """;
            mockMvc.perform(put("/transactions/5")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Bad Request"));
        }

        @Test
        @DisplayName("should accept a transaction without parent_id (optional field)")
        void parentIdIsOptional() throws Exception {
            String body = """
                    {"amount": 250.0, "type": "salary"}
                    """;
            mockMvc.perform(put("/transactions/30")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ok"));
        }

        @Test
        @DisplayName("should return 400 for empty body")
        void emptyBody() throws Exception {
            mockMvc.perform(put("/transactions/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /transactions/types/{type}
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /transactions/types/{type}")
    class GetByType {

        @BeforeEach
        void seed() throws Exception {
            putTransaction(100L, 5000.0, "cars", null);
            putTransaction(101L, 10000.0, "shopping", 100L);
            putTransaction(102L, 5000.0, "shopping", 101L);
        }

        @Test
        @DisplayName("should return ids of all transactions matching the type")
        void returnMatchingIds() throws Exception {
            mockMvc.perform(get("/transactions/types/shopping"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$", containsInAnyOrder(101, 102)));
        }

        @Test
        @DisplayName("should return a single id when only one transaction of that type exists")
        void singleMatch() throws Exception {
            mockMvc.perform(get("/transactions/types/cars"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0]").value(100));
        }

        @Test
        @DisplayName("should return an empty list when no transactions match the type")
        void noMatch() throws Exception {
            mockMvc.perform(get("/transactions/types/nonexistent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("response should be a JSON array")
        void responseIsArray() throws Exception {
            mockMvc.perform(get("/transactions/types/cars"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /transactions/sum/{transactionId}
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /transactions/sum/{transactionId}")
    class GetSum {

        @BeforeEach
        void seed() throws Exception {
            putTransaction(10L, 5000.0, "cars", null);
            putTransaction(11L, 10000.0, "shopping", 10L);
            putTransaction(12L, 5000.0, "shopping", 11L);
        }

        @Test
        @DisplayName("should return transitive sum for root transaction (example: 10 -> 20000)")
        void sumForRoot() throws Exception {
            mockMvc.perform(get("/transactions/sum/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sum").value(20000.0));
        }

        @Test
        @DisplayName("should return transitive sum for intermediate transaction (example: 11 -> 15000)")
        void sumForIntermediate() throws Exception {
            mockMvc.perform(get("/transactions/sum/11"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sum").value(15000.0));
        }

        @Test
        @DisplayName("should return own amount when transaction has no children")
        void sumForLeaf() throws Exception {
            mockMvc.perform(get("/transactions/sum/12"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sum").value(5000.0));
        }

        @Test
        @DisplayName("should return 404 when transaction id does not exist")
        void notFound() throws Exception {
            mockMvc.perform(get("/transactions/sum/9999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"));
        }

        @Test
        @DisplayName("response should contain the 'sum' field")
        void responseShape() throws Exception {
            mockMvc.perform(get("/transactions/sum/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sum").exists());
        }

        @Test
        @DisplayName("should handle deep transitive chain correctly")
        void deepChain() throws Exception {
            putTransaction(200L, 1.0, "deep", null);
            putTransaction(201L, 2.0, "deep", 200L);
            putTransaction(202L, 4.0, "deep", 201L);
            putTransaction(203L, 8.0, "deep", 202L);

            mockMvc.perform(get("/transactions/sum/200"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sum").value(15.0));
        }

        @Test
        @DisplayName("sum with decimal amounts should be accurate")
        void decimalAmounts() throws Exception {
            putTransaction(300L, 1.5, "fractional", null);
            putTransaction(301L, 2.5, "fractional", 300L);

            mockMvc.perform(get("/transactions/sum/300"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sum").value(4.0));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Cross-cutting: full example from the spec
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Spec example scenario")
    class SpecExample {

        @Test
        @DisplayName("should reproduce the exact example from the spec")
        void specExample() throws Exception {
            putTransaction(10L, 5000.0, "cars", null);
            putTransaction(11L, 10000.0, "shopping", 10L);
            putTransaction(12L, 5000.0, "shopping", 11L);

            mockMvc.perform(get("/transactions/types/cars"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", contains(10)));

            mockMvc.perform(get("/transactions/sum/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sum").value(20000.0));

            mockMvc.perform(get("/transactions/sum/11"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sum").value(15000.0));
        }
    }
}
