package dio.budgeting.domain;

import java.util.List;

/**
 * Lançada quando uma transação viola alguma regra. A mensagem é pensada para ser lida
 * tanto pela API REST quanto pelo modelo de IA, que a recebe como resultado da tool
 * e consegue explicar o problema para a pessoa usuária.
 */
public class InvalidTransactionException extends RuntimeException {

    private final List<String> errors;

    public InvalidTransactionException(List<String> errors) {
        super(String.join(" ", errors));
        this.errors = List.copyOf(errors);
    }

    public InvalidTransactionException(String error) {
        this(List.of(error));
    }

    public List<String> getErrors() {
        return errors;
    }
}
