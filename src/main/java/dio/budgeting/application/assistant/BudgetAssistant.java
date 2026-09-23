package dio.budgeting.application.assistant;

/** Porta: entende o pedido em texto, executa as tools necessárias e devolve a resposta final. */
public interface BudgetAssistant {

    String reply(String message);
}
