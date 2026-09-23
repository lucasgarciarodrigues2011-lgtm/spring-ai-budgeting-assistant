package dio.budgeting.application.assistant;

/**
 * Resultado de um comando de voz ou texto.
 *
 * @param transcription o que foi entendido do áudio (igual à mensagem, no fluxo de texto)
 * @param reply         resposta final em texto
 * @param audio         resposta em MP3; {@code null} no fluxo de texto
 */
public record AssistantResult(String transcription, String reply, byte[] audio) {
}
