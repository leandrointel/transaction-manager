package com.transactionmanager.service;

import com.transactionmanager.dto.StatusResponseDTO;
import com.transactionmanager.dto.SumResponseDTO;
import com.transactionmanager.dto.TransactionRequestDTO;
import com.transactionmanager.enums.TransactionType;
import com.transactionmanager.exception.InvalidTransactionException;
import com.transactionmanager.exception.TransactionNotFoundException;
import com.transactionmanager.model.Transaction;
import com.transactionmanager.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService")
class TransactionServiceTest {

    @Mock
    private TransactionRepository repository;

    @InjectMocks
    private TransactionService service;

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("should save a root transaction and return status ok")
        void savesRootTransaction() {
            TransactionRequestDTO request = new TransactionRequestDTO(500.0, "cars", null);

            StatusResponseDTO result = service.save(1L, request);

            assertThat(result.status()).isEqualTo("ok");
            verify(repository).save(new Transaction(1L, 500.0, TransactionType.CARS, null));
        }

        @Test
        @DisplayName("should save a child transaction when parent exists")
        void savesChildTransaction() {
            when(repository.existsById(10L)).thenReturn(true);
            TransactionRequestDTO request = new TransactionRequestDTO(100.0, "shopping", 10L);

            service.save(20L, request);

            verify(repository).save(new Transaction(20L, 100.0, TransactionType.SHOPPING, 10L));
        }

        @Test
        @DisplayName("should throw InvalidTransactionException for unknown type")
        void throwsForInvalidType() {
            TransactionRequestDTO request = new TransactionRequestDTO(100.0, "invalid_type", null);

            assertThatThrownBy(() -> service.save(1L, request))
                    .isInstanceOf(InvalidTransactionException.class);

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("should throw InvalidTransactionException when parent does not exist")
        void throwsWhenParentNotFound() {
            when(repository.existsById(999L)).thenReturn(false);
            TransactionRequestDTO request = new TransactionRequestDTO(100.0, "cars", 999L);

            assertThatThrownBy(() -> service.save(1L, request))
                    .isInstanceOf(InvalidTransactionException.class)
                    .hasMessageContaining("999");

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("should accept type in lowercase")
        void acceptsLowercaseType() {
            TransactionRequestDTO request = new TransactionRequestDTO(100.0, "food", null);

            service.save(1L, request);

            verify(repository).save(new Transaction(1L, 100.0, TransactionType.FOOD, null));
        }
    }

    @Nested
    @DisplayName("getIdsByType")
    class GetIdsByType {

        @Test
        @DisplayName("should return ids of all transactions matching the type")
        void returnsMatchingIds() {
            when(repository.findByType(TransactionType.CARS)).thenReturn(List.of(
                    new Transaction(1L, 100.0, TransactionType.CARS, null),
                    new Transaction(2L, 200.0, TransactionType.CARS, null)
            ));

            List<Long> ids = service.getIdsByType("CARS");

            assertThat(ids).containsExactlyInAnyOrder(1L, 2L);
        }

        @Test
        @DisplayName("should return empty list for unknown type string")
        void returnsEmptyForUnknownType() {
            List<Long> ids = service.getIdsByType("nonexistent");
            assertThat(ids).isEmpty();
            verify(repository, never()).findByType(any());
        }

        @Test
        @DisplayName("should be case-insensitive")
        void caseInsensitive() {
            when(repository.findByType(TransactionType.FOOD)).thenReturn(List.of(
                    new Transaction(5L, 50.0, TransactionType.FOOD, null)
            ));

            List<Long> ids = service.getIdsByType("food");
            assertThat(ids).containsExactly(5L);
        }
    }

    @Nested
    @DisplayName("getSum")
    class GetSum {

        @Test
        @DisplayName("should throw TransactionNotFoundException when id does not exist")
        void throwsWhenTransactionNotFound() {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getSum(999L))
                    .isInstanceOf(TransactionNotFoundException.class);
        }

        @Test
        @DisplayName("should return own amount for a leaf transaction")
        void sumOfLeaf() {
            Transaction leaf = new Transaction(1L, 100.0, TransactionType.CARS, null);
            when(repository.findById(1L)).thenReturn(Optional.of(leaf));
            when(repository.findByParentId(1L)).thenReturn(List.of());

            SumResponseDTO result = service.getSum(1L);

            assertThat(result.sum()).isEqualTo(100.0);
        }

        @Test
        @DisplayName("should return transitive sum including all descendants")
        void sumIncludesDescendants() {
            Transaction root = new Transaction(10L, 5000.0, TransactionType.CARS, null);
            Transaction child = new Transaction(11L, 10000.0, TransactionType.SHOPPING, 10L);
            Transaction grandchild = new Transaction(12L, 5000.0, TransactionType.SHOPPING, 11L);

            when(repository.findById(10L)).thenReturn(Optional.of(root));
            when(repository.findByParentId(10L)).thenReturn(List.of(child));
            when(repository.findByParentId(11L)).thenReturn(List.of(grandchild));
            when(repository.findByParentId(12L)).thenReturn(List.of());

            SumResponseDTO result = service.getSum(10L);

            assertThat(result.sum()).isEqualTo(20000.0);
        }

        @Test
        @DisplayName("should return sum for an intermediate node")
        void sumForIntermediateNode() {
            Transaction child = new Transaction(11L, 10000.0, TransactionType.SHOPPING, 10L);
            Transaction grandchild = new Transaction(12L, 5000.0, TransactionType.SHOPPING, 11L);

            when(repository.findById(11L)).thenReturn(Optional.of(child));
            when(repository.findByParentId(11L)).thenReturn(List.of(grandchild));
            when(repository.findByParentId(12L)).thenReturn(List.of());

            SumResponseDTO result = service.getSum(11L);

            assertThat(result.sum()).isEqualTo(15000.0);
        }

        @Test
        @DisplayName("should handle a deep transitive chain correctly")
        void deepChain() {
            Transaction t1 = new Transaction(1L, 1.0, TransactionType.OTHER, null);
            Transaction t2 = new Transaction(2L, 2.0, TransactionType.OTHER, 1L);
            Transaction t3 = new Transaction(3L, 4.0, TransactionType.OTHER, 2L);
            Transaction t4 = new Transaction(4L, 8.0, TransactionType.OTHER, 3L);

            when(repository.findById(1L)).thenReturn(Optional.of(t1));
            when(repository.findByParentId(1L)).thenReturn(List.of(t2));
            when(repository.findByParentId(2L)).thenReturn(List.of(t3));
            when(repository.findByParentId(3L)).thenReturn(List.of(t4));
            when(repository.findByParentId(4L)).thenReturn(List.of());

            assertThat(service.getSum(1L).sum()).isEqualTo(15.0);
        }
    }
}
