package dio.budgeting.domain;

import java.util.List;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    /** Transações do filtro, das mais recentes para as mais antigas. */
    List<Transaction> search(TransactionFilter filter);
}
