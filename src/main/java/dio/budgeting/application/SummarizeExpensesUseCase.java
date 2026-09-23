package dio.budgeting.application;

import dio.budgeting.application.output.CategoryTotalOutput;
import dio.budgeting.application.output.ExpenseSummaryOutput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.Money;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionFilter;
import dio.budgeting.domain.TransactionRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Nova tool: responde perguntas como "quanto gastei esse mês?" ou "onde estou gastando mais?".
 * A soma é feita aqui, em código, e não pelo modelo: modelos de linguagem erram contas,
 * então a IA só recebe o resultado pronto.
 */
@Service
public class SummarizeExpensesUseCase {

    private final TransactionRepository transactionRepository;
    private final Clock clock;

    public SummarizeExpensesUseCase(TransactionRepository transactionRepository, Clock clock) {
        this.transactionRepository = transactionRepository;
        this.clock = clock;
    }

    @Tool(name = "summarize-expenses",
            description = "Calcula o total gasto em um período e o total por categoria, do maior para o menor. "
                    + "Use para perguntas de 'quanto gastei' ou 'onde gastei mais'. Sem período, considera o mês atual.")
    public ExpenseSummaryOutput execute(
            @ToolParam(description = "Data inicial AAAA-MM-DD (inclusive). Omita para o início do mês atual", required = false) String from,
            @ToolParam(description = "Data final AAAA-MM-DD (inclusive). Omita para hoje", required = false) String to) {
        DateRange range = DateRange.of(from, to, LocalDate.now(clock));
        List<Transaction> transactions = transactionRepository.search(new TransactionFilter(null, range.from(), range.to()));

        Map<Category, Long> centsByCategory = new EnumMap<>(Category.class);
        Map<Category, Integer> countByCategory = new EnumMap<>(Category.class);
        long totalCents = 0;
        for (Transaction t : transactions) {
            centsByCategory.merge(t.getCategory(), t.getAmount(), Long::sum);
            countByCategory.merge(t.getCategory(), 1, Integer::sum);
            totalCents += t.getAmount();
        }

        final long total = totalCents;
        List<CategoryTotalOutput> byCategory = centsByCategory.entrySet().stream()
                .sorted(Map.Entry.<Category, Long>comparingByValue(Comparator.reverseOrder()))
                .map(e -> new CategoryTotalOutput(
                        e.getKey().name(),
                        Money.toReais(e.getValue()),
                        countByCategory.get(e.getKey()),
                        percentage(e.getValue(), total)))
                .toList();

        return new ExpenseSummaryOutput(range.from(), range.to(), Money.toReais(total), transactions.size(), byCategory);
    }

    private static BigDecimal percentage(long part, long total) {
        if (total == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(part * 100).divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
    }
}
