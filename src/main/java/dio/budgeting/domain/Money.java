package dio.budgeting.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Conversão entre reais (como as pessoas falam e leem) e centavos (como o valor é guardado).
 * <p>
 * Guardar em centavos ({@code long}) evita erros de arredondamento de ponto flutuante;
 * converter aqui, num lugar só, evita que cada camada faça a conta de um jeito.
 */
public final class Money {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private Money() {
    }

    /** R$ 15,90 -> 1590 centavos. Frações abaixo de um centavo são arredondadas. */
    public static long toCents(BigDecimal reais) {
        if (reais == null) {
            throw new InvalidTransactionException("O valor é obrigatório.");
        }
        try {
            return reais.multiply(CEM).setScale(0, RoundingMode.HALF_UP).longValueExact();
        } catch (ArithmeticException e) {
            throw new InvalidTransactionException("O valor informado é grande demais.");
        }
    }

    /** 1590 centavos -> R$ 15.90. */
    public static BigDecimal toReais(long cents) {
        return BigDecimal.valueOf(cents).divide(CEM).setScale(2, RoundingMode.UNNECESSARY);
    }
}
