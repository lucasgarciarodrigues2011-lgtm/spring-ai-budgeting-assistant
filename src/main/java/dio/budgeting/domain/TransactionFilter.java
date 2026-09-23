package dio.budgeting.domain;

import java.time.LocalDate;

/**
 * Critérios de busca de transações.
 *
 * @param category opcional; {@code null} significa todas as categorias
 * @param from     início do período (inclusive)
 * @param to       fim do período (inclusive)
 */
public record TransactionFilter(Category category, LocalDate from, LocalDate to) {
}
