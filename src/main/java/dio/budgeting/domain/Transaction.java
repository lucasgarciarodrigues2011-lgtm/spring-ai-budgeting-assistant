package dio.budgeting.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Transação financeira (um gasto). As regras de validade ficam aqui, no domínio,
 * para valerem igualmente para o endpoint REST e para a IA (tool calling).
 */
public class Transaction {

    /** Limite de sanidade: R$ 1.000.000,00. Protege contra valores absurdos vindos da transcrição. */
    public static final long MAX_AMOUNT_CENTS = 100_000_000L;
    public static final int MAX_DESCRIPTION_LENGTH = 255;

    private final TransactionId id;
    private final String description;
    private final long amount;
    private final Category category;
    private final LocalDate date;

    private Transaction(TransactionId id, String description, long amount, Category category, LocalDate date) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.date = date;
    }

    /**
     * Cria uma nova transação validando todas as regras de uma vez.
     *
     * @param amount valor em centavos
     * @param today  data de hoje, usada para impedir lançamentos no futuro
     */
    public static Transaction create(String description, long amount, Category category, LocalDate date, LocalDate today) {
        List<String> errors = new ArrayList<>();
        String cleanDescription = description == null ? "" : description.strip();

        if (cleanDescription.isEmpty()) {
            errors.add("A descrição é obrigatória.");
        } else if (cleanDescription.length() > MAX_DESCRIPTION_LENGTH) {
            errors.add("A descrição deve ter no máximo " + MAX_DESCRIPTION_LENGTH + " caracteres.");
        }
        if (amount <= 0) {
            errors.add("O valor deve ser maior que zero.");
        } else if (amount > MAX_AMOUNT_CENTS) {
            errors.add("O valor não pode passar de R$ 1.000.000,00.");
        }
        if (category == null) {
            errors.add("A categoria é obrigatória.");
        }
        if (date == null) {
            errors.add("A data é obrigatória.");
        } else if (date.isAfter(today)) {
            errors.add("A data não pode estar no futuro.");
        }

        if (!errors.isEmpty()) {
            throw new InvalidTransactionException(errors);
        }
        return new Transaction(new TransactionId(), cleanDescription, amount, category, date);
    }

    /** Reconstrói uma transação já gravada, sem revalidar (dados antigos podem não ter data). */
    public static Transaction restore(TransactionId id, String description, long amount, Category category, LocalDate date) {
        return new Transaction(id, description, amount, category, date);
    }

    public TransactionId getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    /** Valor em centavos. */
    public long getAmount() {
        return amount;
    }

    public Category getCategory() {
        return category;
    }

    public LocalDate getDate() {
        return date;
    }
}
