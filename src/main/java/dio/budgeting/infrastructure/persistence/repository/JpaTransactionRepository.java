package dio.budgeting.infrastructure.persistence.repository;

import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionFilter;
import dio.budgeting.domain.TransactionRepository;
import dio.budgeting.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JpaTransactionRepository implements TransactionRepository {

    private final TransactionEntityRepository transactionEntityRepository;

    public JpaTransactionRepository(TransactionEntityRepository transactionEntityRepository) {
        this.transactionEntityRepository = transactionEntityRepository;
    }

    @Override
    public Transaction save(Transaction transaction) {
        return transactionEntityRepository.save(TransactionEntity.from(transaction)).toDomain();
    }

    @Override
    public List<Transaction> search(TransactionFilter filter) {
        var entities = filter.category() == null
                ? transactionEntityRepository.findAllByDateBetweenOrderByDateDesc(filter.from(), filter.to())
                : transactionEntityRepository.findAllByCategoryAndDateBetweenOrderByDateDesc(
                        filter.category(), filter.from(), filter.to());
        return entities.stream().map(TransactionEntity::toDomain).toList();
    }
}
