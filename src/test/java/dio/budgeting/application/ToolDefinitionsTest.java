package dio.budgeting.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;

import java.time.Clock;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Garante que os casos de uso são expostos ao modelo como tools, com nome e schema dos parâmetros.
 * É exatamente isso que o ChatClient envia para a OpenAI, então dá para verificar sem chamar a API.
 */
class ToolDefinitionsTest {

    @Test
    @DisplayName("As três tools são registradas com os parâmetros esperados")
    void toolsRegistradas() {
        var repository = new InMemoryTransactionRepository();
        var clock = Clock.systemDefaultZone();
        ToolCallback[] callbacks = MethodToolCallbackProvider.builder()
                .toolObjects(new PersistTransactionUseCase(repository, clock),
                        new ListTransactionsUseCase(repository, clock),
                        new SummarizeExpensesUseCase(repository, clock))
                .build()
                .getToolCallbacks();

        Map<String, String> schemas = Arrays.stream(callbacks)
                .collect(Collectors.toMap(c -> c.getToolDefinition().name(), c -> c.getToolDefinition().inputSchema()));

        assertThat(schemas).containsOnlyKeys("persist-transaction", "list-transactions", "summarize-expenses");
        assertThat(schemas.get("persist-transaction")).contains("amount", "description", "category", "GROCERIES");
        assertThat(schemas.get("list-transactions")).contains("category", "from", "to");
        assertThat(schemas.get("summarize-expenses")).contains("from", "to");
    }
}
