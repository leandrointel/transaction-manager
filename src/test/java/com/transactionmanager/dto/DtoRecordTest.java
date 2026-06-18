package com.transactionmanager.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates that all DTOs work correctly as Java records:
 * Jackson serialization/deserialization and Bean Validation constraints.
 */
@JsonTest
@DisplayName("DTO records — serialization and validation")
class DtoRecordTest {

    @Autowired
    ObjectMapper objectMapper;

    static Validator validator;

    @BeforeAll
    static void setupValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TransactionRequestDTO
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("TransactionRequestDTO")
    class TransactionRequestDTOTest {

        @Test
        @DisplayName("deserializes snake_case JSON correctly")
        void deserializeSnakeCase() throws Exception {
            String json = """
                    {"amount": 5000.0, "type": "cars", "parent_id": 10}
                    """;
            TransactionRequestDTO dto = objectMapper.readValue(json, TransactionRequestDTO.class);

            assertThat(dto.amount()).isEqualTo(5000.0);
            assertThat(dto.type()).isEqualTo("cars");
            assertThat(dto.parentId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("deserializes without optional parent_id")
        void deserializeWithoutParentId() throws Exception {
            String json = """
                    {"amount": 100.0, "type": "food"}
                    """;
            TransactionRequestDTO dto = objectMapper.readValue(json, TransactionRequestDTO.class);

            assertThat(dto.parentId()).isNull();
        }

        @Test
        @DisplayName("fails validation when amount is null")
        void validationFailsOnNullAmount() {
            TransactionRequestDTO dto = new TransactionRequestDTO(null, "cars", null);
            Set<ConstraintViolation<TransactionRequestDTO>> violations = validator.validate(dto);

            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getMessage().equals("amount is required"));
        }

        @Test
        @DisplayName("fails validation when type is blank")
        void validationFailsOnBlankType() {
            TransactionRequestDTO dto = new TransactionRequestDTO(100.0, "  ", null);
            Set<ConstraintViolation<TransactionRequestDTO>> violations = validator.validate(dto);

            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getMessage().equals("type is required"));
        }

        @Test
        @DisplayName("passes validation with valid fields")
        void validationPassesWithValidFields() {
            TransactionRequestDTO dto = new TransactionRequestDTO(500.0, "cars", null);
            Set<ConstraintViolation<TransactionRequestDTO>> violations = validator.validate(dto);

            assertThat(violations).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // StatusResponseDTO
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("StatusResponseDTO")
    class StatusResponseDTOTest {

        @Test
        @DisplayName("serializes to {\"status\":\"ok\"}")
        void serializesOk() throws Exception {
            String json = objectMapper.writeValueAsString(StatusResponseDTO.ok());
            assertThat(json).isEqualTo("{\"status\":\"ok\"}");
        }

        @Test
        @DisplayName("factory method ok() returns status ok")
        void factoryMethodOk() {
            assertThat(StatusResponseDTO.ok().status()).isEqualTo("ok");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SumResponseDTO
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("SumResponseDTO")
    class SumResponseDTOTest {

        @Test
        @DisplayName("serializes to {\"sum\": value}")
        void serializes() throws Exception {
            String json = objectMapper.writeValueAsString(new SumResponseDTO(20000.0));
            assertThat(json).isEqualTo("{\"sum\":20000.0}");
        }

        @Test
        @DisplayName("deserializes from JSON")
        void deserializes() throws Exception {
            SumResponseDTO dto = objectMapper.readValue("{\"sum\":15000.0}", SumResponseDTO.class);
            assertThat(dto.sum()).isEqualTo(15000.0);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ErrorResponseDTO
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("ErrorResponseDTO")
    class ErrorResponseDTOTest {

        @Test
        @DisplayName("factory method of() populates all fields")
        void factoryMethod() {
            ErrorResponseDTO dto = ErrorResponseDTO.of(404, "Not Found", "Transaction not found");

            assertThat(dto.status()).isEqualTo(404);
            assertThat(dto.error()).isEqualTo("Not Found");
            assertThat(dto.message()).isEqualTo("Transaction not found");
            assertThat(dto.timestamp()).isNotNull();
        }

        @Test
        @DisplayName("serializes all fields including timestamp")
        void serializes() throws Exception {
            String json = objectMapper.writeValueAsString(
                    ErrorResponseDTO.of(400, "Bad Request", "type is required"));

            assertThat(json).contains("\"status\":400");
            assertThat(json).contains("\"error\":\"Bad Request\"");
            assertThat(json).contains("\"message\":\"type is required\"");
            assertThat(json).contains("\"timestamp\"");
        }
    }
}
