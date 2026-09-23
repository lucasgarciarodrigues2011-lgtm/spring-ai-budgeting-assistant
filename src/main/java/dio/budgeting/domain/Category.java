package dio.budgeting.domain;

/**
 * Categorias de gasto. A descrição em português é enviada ao modelo no prompt de sistema
 * para ajudar a IA a escolher a categoria certa.
 */
public enum Category {
    GROCERIES("mercado, supermercado, hortifruti, padaria"),
    PHARMA("farmácia e medicamentos"),
    AUTO("carro: combustível, estacionamento, manutenção, pedágio"),
    RESTAURANT("restaurante, lanchonete, bar, delivery de comida"),
    TRANSPORT("ônibus, metrô, trem, aplicativo de corrida, táxi"),
    HOUSING("moradia: aluguel, condomínio, luz, água, gás, internet"),
    LEISURE("lazer: cinema, shows, streaming, viagens, jogos"),
    OTHER("qualquer gasto que não se encaixe nas outras categorias");

    private final String descricao;

    Category(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
