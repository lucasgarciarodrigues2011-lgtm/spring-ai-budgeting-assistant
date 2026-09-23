package dio.budgeting.application.output;

import java.math.BigDecimal;

/** Total gasto em uma categoria no período, com a participação percentual no total. */
public record CategoryTotalOutput(String category, BigDecimal total, int transactions, BigDecimal percentage) {
}
