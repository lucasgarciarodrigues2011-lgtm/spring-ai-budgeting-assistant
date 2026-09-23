package dio.budgeting.application.output;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Resumo dos gastos de um período: total geral e quebra por categoria (da maior para a menor). */
public record ExpenseSummaryOutput(LocalDate from,
                                   LocalDate to,
                                   BigDecimal total,
                                   int transactions,
                                   List<CategoryTotalOutput> byCategory) {
}
