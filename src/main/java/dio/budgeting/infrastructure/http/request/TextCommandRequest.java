package dio.budgeting.infrastructure.http.request;

/** Corpo do POST /transactions/ai/text, ex.: {"message": "gastei 32 reais na farmácia"}. */
public record TextCommandRequest(String message) {
}
