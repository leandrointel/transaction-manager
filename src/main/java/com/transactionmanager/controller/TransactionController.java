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

@RestController
@RequestMapping("/transactions")
@Tag(name = "Transactions", description = "Transaction management API")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(summary = "List all available transaction types")
    @GetMapping("/types")
    public ResponseEntity<List<String>> getTransactionTypes() {
        List<String> types = Arrays.stream(TransactionType.values())
                .map(Enum::name)
                .toList();
        return ResponseEntity.ok(types);
    }

    @Operation(summary = "Create or update a transaction")
    @PutMapping("/{transactionId}")
    public ResponseEntity<StatusResponseDTO> saveTransaction(
            @PathVariable long transactionId,
            @Valid @RequestBody TransactionRequestDTO request) {

        return ResponseEntity.ok(transactionService.save(transactionId, request));
    }

    @Operation(summary = "Get transaction ids by type")
    @GetMapping("/types/{type}")
    public ResponseEntity<List<Long>> getByType(@PathVariable String type) {
        return ResponseEntity.ok(transactionService.getIdsByType(type));
    }

    @Operation(summary = "Get transitive sum of a transaction tree")
    @GetMapping("/sum/{transactionId}")
    public ResponseEntity<SumResponseDTO> getSum(@PathVariable long transactionId) {
        return ResponseEntity.ok(transactionService.getSum(transactionId));
    }
}
