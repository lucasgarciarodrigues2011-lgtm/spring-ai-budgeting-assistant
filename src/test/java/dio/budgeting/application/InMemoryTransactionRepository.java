package dio.budgeting.application;

import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionFilter;
import dio.budgeting.domain.TransactionRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Repositório em memória para testar os casos de uso sem banco de dados. */
public class InMemoryTransactionRepository implements TransactionRepository {

    private final List<Transaction> transactions = new ArrayList<>();

    @Override
    public Transaction save(Transaction transaction) {
        transactions.add(transaction);
        return transaction;
    }

    @Override
    public List<Transaction> search(TransactionFilter filter) {
        return transactions.stream()
                .filter(t -> filter.category() == null || t.getCategory() == filter.category())
                .filter(t -> !t.getDate().isBefore(filter.from()) && !t.getDate().isAfter(filter.to()))
                .sorted(Comparator.comparing(Transaction::getDate).reversed())
                .toList();
    }

    public List<Transaction> all() {
        return List.copyOf(transactions);
    }
}
