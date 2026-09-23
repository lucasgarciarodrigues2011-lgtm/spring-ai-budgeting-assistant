package dio.budgeting.application;

import dio.budgeting.application.input.PersistTransactionInput;
import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.Money;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;

@Service
public class PersistTransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final Clock clock;

    public PersistTransactionUseCase(TransactionRepository transactionRepository, Clock clock) {
        this.transactionRepository = transactionRepository;
        this.clock = clock;
    }

    @Tool(name = "persist-transaction",
            description = "Registra um novo gasto. Use quando a pessoa disser que gastou, pagou ou comprou algo. "
                    + "Se o registro for recusado, a resposta explica o motivo: repasse-o à pessoa.")
    public TransactionOutput execute(PersistTransactionInput input) {
        LocalDate today = LocalDate.now(clock);
        var transaction = Transaction.create(
                input.description(),
                Money.toCents(input.amount()),
                input.category(),
                DateRange.parseOptional(input.date(), today),
                today);
        return TransactionOutput.from(transactionRepository.save(transaction));
    }
}
