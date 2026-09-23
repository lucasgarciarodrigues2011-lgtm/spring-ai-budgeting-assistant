package dio.budgeting.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 9, 23);

    @Test
    @DisplayName("Cria transação válida e remove espaços da descrição")
    void criaValida() {
        Transaction t = Transaction.create("  Mercado  ", 1590, Category.GROCERIES, HOJE, HOJE);

        assertThat(t.getDescription()).isEqualTo("Mercado");
        assertThat(t.getAmount()).isEqualTo(1590);
        assertThat(t.getId()).isNotNull();
    }

    @Test
    @DisplayName("Acumula todos os erros de validação de uma vez")
    void acumulaErros() {
        assertThatThrownBy(() -> Transaction.create(" ", 0, null, HOJE.plusDays(1), HOJE))
                .isInstanceOfSatisfying(InvalidTransactionException.class, e -> assertThat(e.getErrors())
                        .containsExactly(
                                "A descrição é obrigatória.",
                                "O valor deve ser maior que zero.",
                                "A categoria é obrigatória.",
                                "A data não pode estar no futuro."));
    }

    @Test
    @DisplayName("Recusa valor negativo e acima do limite de R$ 1 milhão")
    void limitesDeValor() {
        assertThatThrownBy(() -> Transaction.create("X", -100, Category.OTHER, HOJE, HOJE))
                .isInstanceOf(InvalidTransactionException.class);
        assertThatThrownBy(() -> Transaction.create("X", Transaction.MAX_AMOUNT_CENTS + 1, Category.OTHER, HOJE, HOJE))
                .hasMessageContaining("1.000.000,00");
    }

    @Test
    @DisplayName("Money converte reais em centavos e de volta, sem perder precisão")
    void conversaoDeMoeda() {
        assertThat(Money.toCents(new BigDecimal("15.90"))).isEqualTo(1590);
        assertThat(Money.toCents(new BigDecimal("0.015"))).isEqualTo(2);
        assertThat(Money.toReais(1590)).isEqualByComparingTo("15.90");
        assertThat(Money.toReais(1500).toPlainString()).isEqualTo("15.00");
        assertThatThrownBy(() -> Money.toCents(null)).isInstanceOf(InvalidTransactionException.class);
    }
}
