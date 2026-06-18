package com.transactionmanager.controller;

import com.transactionmanager.dto.StatusResponseDTO;
import com.transactionmanager.dto.SumResponseDTO;
import com.transactionmanager.dto.TransactionRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing the transaction management endpoints.
 * Methods are stubs — implementation will be wired in subsequent steps.
 */
@RestController
@RequestMapping("/transactions")
@Tag(name = "Transactions", description = "Transaction management API")
public class TransactionController {

    @Operation(summary = "Create or update a transaction")
    @PutMapping("/{transactionId}")
    public ResponseEntity<StatusResponseDTO> saveTransaction(
            @PathVariable long transactionId,
            @Valid @RequestBody TransactionRequestDTO request) {

        throw new UnsupportedOperationException("not implemented yet");
    }

    @Operation(summary = "Get transaction ids by type")
    @GetMapping("/types/{type}")
    public ResponseEntity<List<Long>> getByType(@PathVariable String type) {

        throw new UnsupportedOperationException("not implemented yet");
    }

    @Operation(summary = "Get transitive sum of a transaction tree")
    @GetMapping("/sum/{transactionId}")
    public ResponseEntity<SumResponseDTO> getSum(@PathVariable long transactionId) {

        throw new UnsupportedOperationException("not implemented yet");
    }
}
