package dio.budgeting.infrastructure.http.request;

import dio.budgeting.application.input.PersistTransactionInput;
import dio.budgeting.domain.Category;

import java.math.BigDecimal;

/**
 * Corpo do POST /transactions. {@code amount} em reais (ex.: 15.90) e {@code date} opcional (AAAA-MM-DD).
 */
public record TransactionRequest(String description, Category category, BigDecimal amount, String date) {

    public PersistTransactionInput toInput() {
        return new PersistTransactionInput(description, amount, category, date);
    }
}
