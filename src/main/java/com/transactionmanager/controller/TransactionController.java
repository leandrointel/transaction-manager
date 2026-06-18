package com.transactionmanager.controller;

import com.transactionmanager.dto.StatusResponseDTO;
import com.transactionmanager.dto.SumResponseDTO;
import com.transactionmanager.dto.TransactionRequestDTO;
import com.transactionmanager.enums.TransactionType;
import com.transactionmanager.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
        List<String> types = Arrays.stream(TransactionType.values())
                .map(Enum::name)
                .toList();
        return ResponseEntity.ok(types);
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

        return ResponseEntity.ok(transactionService.save(transactionId, request));
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
        return ResponseEntity.ok(transactionService.getIdsByType(type));
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
        return ResponseEntity.ok(transactionService.getSum(transactionId));
    }
}
