package dio.budgeting.application.output;

import dio.budgeting.domain.Money;
import dio.budgeting.domain.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Transação como é devolvida para a API e para o modelo.
 * <p>
 * Correção em relação ao projeto base: o valor é guardado em centavos, mas antes era
 * devolvido sem dividir por 100 (um gasto de R$ 15,00 aparecia como 1500.00).
 * Agora {@code amount} está sempre em reais.
 */
public record TransactionOutput(String id, String description, String category, BigDecimal amount, LocalDate date) {

    public static TransactionOutput from(Transaction transaction) {
        return new TransactionOutput(
                transaction.getId().uuid().toString(),
                transaction.getDescription(),
                transaction.getCategory().name(),
                Money.toReais(transaction.getAmount()),
                transaction.getDate());
    }
}
