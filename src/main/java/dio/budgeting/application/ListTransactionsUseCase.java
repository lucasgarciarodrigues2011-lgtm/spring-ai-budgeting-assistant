package dio.budgeting.application;

import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.TransactionFilter;
import dio.budgeting.domain.TransactionRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/** Evolução do antigo ListTransactionsByCategoryUseCase: categoria opcional e filtro por período. */
@Service
public class ListTransactionsUseCase {

    private final TransactionRepository transactionRepository;
    private final Clock clock;

    public ListTransactionsUseCase(TransactionRepository transactionRepository, Clock clock) {
        this.transactionRepository = transactionRepository;
        this.clock = clock;
    }

    @Tool(name = "list-transactions",
            description = "Lista gastos individuais, dos mais recentes para os mais antigos, opcionalmente por categoria e período. "
                    + "Sem período, considera o mês atual.")
    public List<TransactionOutput> execute(
            @ToolParam(description = "Categoria para filtrar. Omita para todas", required = false) Category category,
            @ToolParam(description = "Data inicial AAAA-MM-DD (inclusive). Omita para o início do mês atual", required = false) String from,
            @ToolParam(description = "Data final AAAA-MM-DD (inclusive). Omita para hoje", required = false) String to) {
        DateRange range = DateRange.of(from, to, LocalDate.now(clock));
        return transactionRepository.search(new TransactionFilter(category, range.from(), range.to()))
                .stream()
                .map(TransactionOutput::from)
                .toList();
    }
}
