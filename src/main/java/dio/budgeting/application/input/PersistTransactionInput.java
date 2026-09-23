package dio.budgeting.application.input;

import dio.budgeting.domain.Category;
import org.springframework.ai.tool.annotation.ToolParam;

import java.math.BigDecimal;

/**
 * Dados para registrar um gasto. O valor agora é em REAIS (ex.: 15.90), e não em centavos:
 * é assim que as pessoas falam, e o modelo erra menos quando não precisa fazer a conversão.
 */
public record PersistTransactionInput(
        @ToolParam(description = "Descrição curta do gasto, ex.: 'Compras no mercado'") String description,
        @ToolParam(description = "Valor do gasto em reais, com até 2 casas decimais. Ex.: 15.90 para R$ 15,90") BigDecimal amount,
        @ToolParam(description = "Categoria que melhor descreve o gasto") Category category,
        @ToolParam(description = "Data do gasto no formato AAAA-MM-DD. Omita quando a pessoa não disser a data (usa hoje)",
                required = false) String date) {
}
