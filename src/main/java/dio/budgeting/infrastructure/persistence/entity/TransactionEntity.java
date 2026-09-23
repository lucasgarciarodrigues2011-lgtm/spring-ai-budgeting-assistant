package dio.budgeting.infrastructure.persistence.entity;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

import java.time.LocalDate;
import java.util.UUID;

@Entity
public class TransactionEntity {

    @Id
    private UUID id;

    private String description;

    /** Valor em centavos. */
    private long amount;

    @Enumerated(EnumType.STRING)
    private Category category;

    /** "date" é palavra reservada em alguns bancos, por isso o nome explícito da coluna. */
    @Column(name = "transaction_date")
    private LocalDate date;

    protected TransactionEntity() {
    }

    private TransactionEntity(UUID id, String description, long amount, Category category, LocalDate date) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.date = date;
    }

    public static TransactionEntity from(Transaction transaction) {
        return new TransactionEntity(
                transaction.getId().uuid(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getCategory(),
                transaction.getDate());
    }

    public Transaction toDomain() {
        return Transaction.restore(new TransactionId(id), description, amount, category, date);
    }
}
