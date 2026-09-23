package dio.budgeting.application;

import dio.budgeting.application.input.PersistTransactionInput;
import dio.budgeting.application.output.ExpenseSummaryOutput;
import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.InvalidTransactionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UseCasesTest {

    /** "Hoje" fixo nos testes: 23/09/2026. */
    private final Clock clock = Clock.fixed(
            ZonedDateTime.of(2026, 9, 23, 10, 0, 0, 0, ZoneId.of("America/Sao_Paulo")).toInstant(),
            ZoneId.of("America/Sao_Paulo"));

    private InMemoryTransactionRepository repository;
    private PersistTransactionUseCase persist;
    private ListTransactionsUseCase list;
    private SummarizeExpensesUseCase summarize;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTransactionRepository();
        persist = new PersistTransactionUseCase(repository, clock);
        list = new ListTransactionsUseCase(repository, clock);
        summarize = new SummarizeExpensesUseCase(repository, clock);
    }

    private TransactionOutput gasto(String descricao, String reais, Category categoria, String data) {
        return persist.execute(new PersistTransactionInput(descricao, new BigDecimal(reais), categoria, data));
    }

    @Test
    @DisplayName("Bug corrigido: valor volta em reais, e não em centavos")
    void valorEmReais() {
        TransactionOutput output = gasto("Mercado", "15.00", Category.GROCERIES, null);

        assertThat(output.amount()).isEqualByComparingTo("15.00");
        assertThat(repository.all().getFirst().getAmount()).isEqualTo(1500);
    }

    @Test
    @DisplayName("Sem data informada, registra com a data de hoje")
    void dataPadraoHoje() {
        assertThat(gasto("Farmácia", "32.50", Category.PHARMA, null).date()).isEqualTo(LocalDate.of(2026, 9, 23));
        assertThat(gasto("Farmácia", "10", Category.PHARMA, "2026-09-20").date()).isEqualTo(LocalDate.of(2026, 9, 20));
    }

    @Test
    @DisplayName("Validações: nada é salvo quando o gasto é inválido")
    void validacoes() {
        assertThatThrownBy(() -> gasto("Mercado", "0", Category.GROCERIES, null))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("maior que zero");
        assertThatThrownBy(() -> gasto("Mercado", "10", Category.GROCERIES, "23/09/2026"))
                .hasMessageContaining("AAAA-MM-DD");
        assertThatThrownBy(() -> gasto("Mercado", "10", Category.GROCERIES, "2026-12-25"))
                .hasMessageContaining("futuro");
        assertThat(repository.all()).isEmpty();
    }

    @Test
    @DisplayName("Lista por categoria e período, das mais recentes para as mais antigas")
    void listaComFiltros() {
        gasto("Mercado A", "100", Category.GROCERIES, "2026-09-01");
        gasto("Mercado B", "50", Category.GROCERIES, "2026-09-20");
        gasto("Uber", "25", Category.TRANSPORT, "2026-09-21");
        gasto("Mercado agosto", "80", Category.GROCERIES, "2026-08-15");

        List<TransactionOutput> setembro = list.execute(Category.GROCERIES, null, null);
        assertThat(setembro).extracting(TransactionOutput::description).containsExactly("Mercado B", "Mercado A");

        assertThat(list.execute(null, "2026-08-01", "2026-08-31")).hasSize(1);
        assertThatThrownBy(() -> list.execute(null, "2026-09-10", "2026-09-01"))
                .hasMessageContaining("posterior");
    }

    @Test
    @DisplayName("Resumo soma em código e ordena as categorias da maior para a menor")
    void resumo() {
        gasto("Mercado", "100.00", Category.GROCERIES, "2026-09-05");
        gasto("Mercado", "50.50", Category.GROCERIES, "2026-09-10");
        gasto("Remédio", "49.50", Category.PHARMA, "2026-09-12");
        gasto("Mês passado", "999", Category.LEISURE, "2026-08-30");

        ExpenseSummaryOutput resumo = summarize.execute(null, null);

        assertThat(resumo.from()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(resumo.to()).isEqualTo(LocalDate.of(2026, 9, 23));
        assertThat(resumo.total()).isEqualByComparingTo("200.00");
        assertThat(resumo.transactions()).isEqualTo(3);
        assertThat(resumo.byCategory()).hasSize(2);
        assertThat(resumo.byCategory().getFirst().category()).isEqualTo("GROCERIES");
        assertThat(resumo.byCategory().getFirst().total()).isEqualByComparingTo("150.50");
        assertThat(resumo.byCategory().getFirst().percentage()).isEqualByComparingTo("75.3");
    }

    @Test
    @DisplayName("Resumo de período sem gastos vem zerado, sem erro")
    void resumoVazio() {
        ExpenseSummaryOutput resumo = summarize.execute("2026-01-01", "2026-01-31");

        assertThat(resumo.total()).isEqualByComparingTo("0");
        assertThat(resumo.byCategory()).isEmpty();
    }
}
