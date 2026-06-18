package com.transactionmanager.controller;

import com.transactionmanager.dto.StatusResponseDTO;
import com.transactionmanager.dto.SumResponseDTO;
import com.transactionmanager.dto.TransactionRequestDTO;
import com.transactionmanager.enums.TransactionType;
import com.transactionmanager.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * REST controller exposing the transaction management endpoints.
 * Delegates all business logic to {@link TransactionService}.
 */
@RestController
@RequestMapping("/transactions")
@Tag(name = "Transactions", description = "Transaction management API")
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Returns the names of all values defined in {@link TransactionType}.
     *
     * @return 200 OK with a JSON array of type name strings
     */
    @Operation(summary = "List all available transaction types")
    @GetMapping("/types")
    public ResponseEntity<List<String>> getTransactionTypes() {
        MDC.put("method", "GET");
        MDC.put("uri", "/transactions/types");
        try {
            log.debug("Fetching all transaction types");
            List<String> types = Arrays.stream(TransactionType.values())
                    .map(Enum::name)
                    .toList();
            log.debug("Returning {} transaction types", types.size());
            return ResponseEntity.ok(types);
        } finally {
            MDC.clear();
        }
    }

    /**
     * Creates or replaces the transaction identified by {@code transactionId}.
     *
     * @param transactionId the unique id for the transaction
     * @param request       the request body with amount, type, and optional parent id
     * @return 200 OK with {@code {"status": "ok"}} on success,
     *         400 Bad Request if validation fails or the parent does not exist
     */
    @Operation(summary = "Create or update a transaction")
    @PutMapping("/{transactionId}")
    public ResponseEntity<StatusResponseDTO> saveTransaction(
            @PathVariable long transactionId,
            @Valid @RequestBody TransactionRequestDTO request) {

        MDC.put("method", "PUT");
        MDC.put("uri", "/transactions/" + transactionId);
        try {
            log.info("Saving transaction id={} type={} parentId={}", transactionId, request.type(), request.parentId());
            StatusResponseDTO response = transactionService.save(transactionId, request);
            log.info("Transaction id={} saved successfully", transactionId);
            return ResponseEntity.ok(response);
        } finally {
            MDC.clear();
        }
    }

    /**
     * Returns the ids of all transactions whose type matches {@code type} (case-insensitive).
     * Unknown type strings yield an empty array rather than an error.
     *
     * @param type the transaction type string from the path
     * @return 200 OK with a JSON array of transaction ids
     */
    @Operation(summary = "Get transaction ids by type")
    @GetMapping("/types/{type}")
    public ResponseEntity<List<Long>> getByType(@PathVariable String type) {
        MDC.put("method", "GET");
        MDC.put("uri", "/transactions/types/" + type);
        try {
            log.debug("Fetching transaction ids for type={}", type);
            List<Long> ids = transactionService.getIdsByType(type);
            log.debug("Found {} transactions of type={}", ids.size(), type);
            return ResponseEntity.ok(ids);
        } finally {
            MDC.clear();
        }
    }

    /**
     * Returns the transitive sum of the transaction tree rooted at {@code transactionId}.
     * The sum includes the transaction's own amount plus the amounts of all its descendants.
     *
     * @param transactionId the root transaction id
     * @return 200 OK with {@code {"sum": <value>}}, or 404 Not Found if the id does not exist
     */
    @Operation(summary = "Get transitive sum of a transaction tree")
    @GetMapping("/sum/{transactionId}")
    public ResponseEntity<SumResponseDTO> getSum(@PathVariable long transactionId) {
        MDC.put("method", "GET");
        MDC.put("uri", "/transactions/sum/" + transactionId);
        try {
            log.debug("Computing transitive sum for transactionId={}", transactionId);
            SumResponseDTO response = transactionService.getSum(transactionId);
            log.debug("Transitive sum for transactionId={} is {}", transactionId, response.sum());
            return ResponseEntity.ok(response);
        } finally {
            MDC.clear();
        }
    }
}
