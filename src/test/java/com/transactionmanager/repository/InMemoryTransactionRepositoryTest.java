package com.transactionmanager.repository;

import com.transactionmanager.enums.TransactionType;
import com.transactionmanager.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("InMemoryTransactionRepository")
class InMemoryTransactionRepositoryTest {

    private InMemoryTransactionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTransactionRepository();
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("should persist a new transaction")
        void persistsNewTransaction() {
            Transaction tx = new Transaction(1L, 100.0, TransactionType.CARS, null);
            repository.save(tx);
            assertThat(repository.findById(1L)).contains(tx);
        }

        @Test
        @DisplayName("should overwrite a transaction with the same id")
        void overwritesExisting() {
            repository.save(new Transaction(1L, 100.0, TransactionType.CARS, null));
            Transaction updated = new Transaction(1L, 200.0, TransactionType.SHOPPING, null);
            repository.save(updated);

            assertThat(repository.findById(1L)).contains(updated);
            assertThat(repository.findByType(TransactionType.CARS)).isEmpty();
        }

        @Test
        @DisplayName("should remove from old parent's children index when parent changes on overwrite")
        void updatesChildrenIndexOnParentChange() {
            repository.save(new Transaction(1L, 100.0, TransactionType.CARS, null));
            repository.save(new Transaction(2L, 100.0, TransactionType.CARS, null));
            repository.save(new Transaction(3L, 50.0, TransactionType.FOOD, 1L));

            assertThat(repository.findByParentId(1L)).extracting(Transaction::id).containsExactly(3L);
            assertThat(repository.findByParentId(2L)).isEmpty();

            // reasignar tx3 al padre 2
            repository.save(new Transaction(3L, 50.0, TransactionType.FOOD, 2L));

            assertThat(repository.findByParentId(1L)).isEmpty();
            assertThat(repository.findByParentId(2L)).extracting(Transaction::id).containsExactly(3L);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return empty when id not found")
        void returnsEmptyWhenNotFound() {
            assertThat(repository.findById(999L)).isEmpty();
        }

        @Test
        @DisplayName("should return the transaction when found")
        void returnsTransactionWhenFound() {
            Transaction tx = new Transaction(5L, 50.0, TransactionType.FOOD, null);
            repository.save(tx);
            Optional<Transaction> result = repository.findById(5L);
            assertThat(result).isPresent().contains(tx);
        }
    }

    @Nested
    @DisplayName("findByType")
    class FindByType {

        @Test
        @DisplayName("should return all transactions matching the given type")
        void returnsMatchingTransactions() {
            repository.save(new Transaction(1L, 100.0, TransactionType.CARS, null));
            repository.save(new Transaction(2L, 200.0, TransactionType.CARS, null));
            repository.save(new Transaction(3L, 300.0, TransactionType.FOOD, null));

            List<Transaction> cars = repository.findByType(TransactionType.CARS);
            assertThat(cars).hasSize(2).extracting(Transaction::id).containsExactlyInAnyOrder(1L, 2L);
        }

        @Test
        @DisplayName("should return empty list when no transactions match")
        void returnsEmptyWhenNoMatch() {
            repository.save(new Transaction(1L, 100.0, TransactionType.FOOD, null));
            assertThat(repository.findByType(TransactionType.CARS)).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByParentId")
    class FindByParentId {

        @Test
        @DisplayName("should return direct children of a given parent")
        void returnsChildren() {
            repository.save(new Transaction(1L, 100.0, TransactionType.CARS, null));
            repository.save(new Transaction(2L, 50.0, TransactionType.SHOPPING, 1L));
            repository.save(new Transaction(3L, 75.0, TransactionType.FOOD, 1L));
            repository.save(new Transaction(4L, 25.0, TransactionType.FOOD, 2L));

            List<Transaction> children = repository.findByParentId(1L);
            assertThat(children).hasSize(2).extracting(Transaction::id).containsExactlyInAnyOrder(2L, 3L);
        }

        @Test
        @DisplayName("should return empty list when transaction has no children")
        void returnsEmptyWhenNoChildren() {
            repository.save(new Transaction(1L, 100.0, TransactionType.CARS, null));
            assertThat(repository.findByParentId(1L)).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsById")
    class ExistsById {

        @Test
        @DisplayName("should return true when transaction exists")
        void returnsTrueWhenExists() {
            repository.save(new Transaction(1L, 100.0, TransactionType.CARS, null));
            assertThat(repository.existsById(1L)).isTrue();
        }

        @Test
        @DisplayName("should return false when transaction does not exist")
        void returnsFalseWhenNotExists() {
            assertThat(repository.existsById(999L)).isFalse();
        }
    }
}
