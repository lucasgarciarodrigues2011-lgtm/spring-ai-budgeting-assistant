package dio.budgeting.infrastructure.ai;

import dio.budgeting.application.ListTransactionsUseCase;
import dio.budgeting.application.PersistTransactionUseCase;
import dio.budgeting.application.SummarizeExpensesUseCase;
import dio.budgeting.application.assistant.BudgetAssistant;
import dio.budgeting.domain.Category;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Implementação da porta {@link BudgetAssistant} com o ChatClient do Spring AI e tool calling.
 * <p>
 * Melhoria em relação ao base: o prompt de sistema agora é um template que recebe a data de hoje
 * (sem ela o modelo não sabe o que é "ontem" ou "este mês") e a lista de categorias com descrição.
 */
@Component
public class SpringAiBudgetAssistant implements BudgetAssistant {

    private final ChatClient chatClient;
    private final String systemTemplate;
    private final Clock clock;

    public SpringAiBudgetAssistant(ChatClient.Builder chatClientBuilder,
                                   @Value("classpath:prompts/system-message.st") Resource systemPrompt,
                                   PersistTransactionUseCase persistTransaction,
                                   ListTransactionsUseCase listTransactions,
                                   SummarizeExpensesUseCase summarizeExpenses,
                                   Clock clock) throws IOException {
        this.chatClient = chatClientBuilder
                .defaultTools(persistTransaction, listTransactions, summarizeExpenses)
                .build();
        this.systemTemplate = systemPrompt.getContentAsString(StandardCharsets.UTF_8);
        this.clock = clock;
    }

    @Override
    public String reply(String message) {
        return chatClient.prompt()
                .system(s -> s.text(systemTemplate)
                        .param("hoje", LocalDate.now(clock).toString())
                        .param("categorias", categorias()))
                .user(message)
                .call()
                .content();
    }

    private static String categorias() {
        return Arrays.stream(Category.values())
                .map(c -> "- " + c.name() + ": " + c.getDescricao())
                .collect(Collectors.joining("\n"));
    }
}
