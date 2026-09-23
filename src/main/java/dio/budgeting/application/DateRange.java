package dio.budgeting.application;

import dio.budgeting.domain.InvalidTransactionException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Período de consulta. Quando o início não é informado, usa o primeiro dia do mês atual;
 * quando o fim não é informado, usa hoje. Assim "quanto gastei?" responde sobre o mês corrente.
 */
public record DateRange(LocalDate from, LocalDate to) {

    public static DateRange of(String from, String to, LocalDate today) {
        LocalDate end = isBlank(to) ? today : parse(to, "final");
        LocalDate start = isBlank(from) ? end.withDayOfMonth(1) : parse(from, "inicial");
        if (start.isAfter(end)) {
            throw new InvalidTransactionException("A data inicial (" + start + ") é posterior à data final (" + end + ").");
        }
        return new DateRange(start, end);
    }

    /** Converte uma data opcional no formato AAAA-MM-DD; vazia vira {@code fallback}. */
    public static LocalDate parseOptional(String value, LocalDate fallback) {
        return isBlank(value) ? fallback : parse(value, "da transação");
    }

    private static LocalDate parse(String value, String label) {
        try {
            return LocalDate.parse(value.strip());
        } catch (DateTimeParseException e) {
            throw new InvalidTransactionException("Data " + label + " inválida: '" + value + "'. Use o formato AAAA-MM-DD.");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
