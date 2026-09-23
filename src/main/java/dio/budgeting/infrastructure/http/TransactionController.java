package dio.budgeting.infrastructure.http;

import dio.budgeting.application.ListTransactionsUseCase;
import dio.budgeting.application.PersistTransactionUseCase;
import dio.budgeting.application.SummarizeExpensesUseCase;
import dio.budgeting.application.output.ExpenseSummaryOutput;
import dio.budgeting.domain.Category;
import dio.budgeting.infrastructure.http.request.TransactionRequest;
import dio.budgeting.infrastructure.http.response.TransactionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Endpoints REST "tradicionais". Usam os mesmos casos de uso que a IA chama como tools. */
@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final PersistTransactionUseCase persistTransactionUseCase;
    private final ListTransactionsUseCase listTransactionsUseCase;
    private final SummarizeExpensesUseCase summarizeExpensesUseCase;

    public TransactionController(PersistTransactionUseCase persistTransactionUseCase,
                                 ListTransactionsUseCase listTransactionsUseCase,
                                 SummarizeExpensesUseCase summarizeExpensesUseCase) {
        this.persistTransactionUseCase = persistTransactionUseCase;
        this.listTransactionsUseCase = listTransactionsUseCase;
        this.summarizeExpensesUseCase = summarizeExpensesUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse createTransaction(@RequestBody TransactionRequest request) {
        return TransactionResponse.from(persistTransactionUseCase.execute(request.toInput()));
    }

    /** Ex.: GET /transactions?category=GROCERIES&from=2026-09-01&to=2026-09-30 (todos opcionais). */
    @GetMapping
    public List<TransactionResponse> listTransactions(@RequestParam(required = false) Category category,
                                                      @RequestParam(required = false) String from,
                                                      @RequestParam(required = false) String to) {
        return listTransactionsUseCase.execute(category, from, to).stream().map(TransactionResponse::from).toList();
    }

    /** Mantido por compatibilidade com o projeto base: todas as transações da categoria, sem filtro de data. */
    @GetMapping("/{category}")
    public List<TransactionResponse> readTransactions(@PathVariable Category category) {
        return listTransactions(category, "1970-01-01", null);
    }

    /** Ex.: GET /transactions/summary?from=2026-09-01 */
    @GetMapping("/summary")
    public ExpenseSummaryOutput summary(@RequestParam(required = false) String from,
                                        @RequestParam(required = false) String to) {
        return summarizeExpensesUseCase.execute(from, to);
    }
}
