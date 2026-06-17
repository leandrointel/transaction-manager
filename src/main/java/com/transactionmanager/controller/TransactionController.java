package com.transactionmanager.controller;

import com.transactionmanager.dto.StatusResponseDTO;
import com.transactionmanager.dto.SumResponseDTO;
import com.transactionmanager.dto.TransactionRequestDTO;
import com.transactionmanager.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing the transaction management endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Transaction management API")
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Creates or updates a transaction.
     *
     * @param transactionId path variable identifying the transaction
     * @param request       request body with amount, type and optional parent_id
     * @return 200 OK with status "ok"
     */
    @Operation(
            summary = "Create or update a transaction",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Transaction saved",
                            content = @Content(schema = @Schema(implementation = StatusResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request")
            }
    )
    @PutMapping("/{transactionId}")
    public ResponseEntity<StatusResponseDTO> saveTransaction(
            @Parameter(description = "Unique transaction id") @PathVariable long transactionId,
            @Valid @RequestBody TransactionRequestDTO request) {

        log.info("PUT /transactions/{}", transactionId);
        StatusResponseDTO response = transactionService.saveTransaction(
                transactionId, request.getAmount(), request.getType(), request.getParentId());
        return ResponseEntity.ok(response);
    }

    /**
     * Returns the ids of all transactions of the specified type.
     *
     * @param type the type to filter
     * @return list of transaction ids
     */
    @Operation(
            summary = "Get transaction ids by type",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of ids")
            }
    )
    @GetMapping("/types/{type}")
    public ResponseEntity<List<Long>> getByType(
            @Parameter(description = "Transaction type") @PathVariable String type) {

        log.info("GET /transactions/types/{}", type);
        return ResponseEntity.ok(transactionService.getIdsByType(type));
    }

    /**
     * Returns the transitive sum of all transactions linked to the given id.
     *
     * @param transactionId root transaction id
     * @return sum response
     */
    @Operation(
            summary = "Get transitive sum of a transaction tree",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Transitive sum",
                            content = @Content(schema = @Schema(implementation = SumResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Transaction not found")
            }
    )
    @GetMapping("/sum/{transactionId}")
    public ResponseEntity<SumResponseDTO> getSum(
            @Parameter(description = "Root transaction id") @PathVariable long transactionId) {

        log.info("GET /transactions/sum/{}", transactionId);
        return ResponseEntity.ok(transactionService.getTransitiveSum(transactionId));
    }
}
