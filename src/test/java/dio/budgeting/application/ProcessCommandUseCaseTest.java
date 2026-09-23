package dio.budgeting.application;

import dio.budgeting.application.assistant.AssistantResult;
import dio.budgeting.application.assistant.InvalidAudioException;
import dio.budgeting.application.assistant.ProcessCommandUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Testa o fluxo áudio -> texto -> IA -> voz com dublês simples (lambdas), sem OpenAI. */
class ProcessCommandUseCaseTest {

    private final List<String> perguntasAoAssistente = new ArrayList<>();

    private ProcessCommandUseCase useCase(String transcricao) {
        return new ProcessCommandUseCase(
                audio -> transcricao,
                mensagem -> {
                    perguntasAoAssistente.add(mensagem);
                    return "Registrei o gasto.";
                },
                texto -> ("mp3:" + texto).getBytes());
    }

    @Test
    @DisplayName("Áudio percorre o fluxo inteiro e volta como texto e MP3")
    void fluxoCompleto() {
        AssistantResult result = useCase(" Gastei 20 reais no mercado ").fromAudio(new ByteArrayResource(new byte[]{1, 2}));

        assertThat(perguntasAoAssistente).containsExactly("Gastei 20 reais no mercado");
        assertThat(result.reply()).isEqualTo("Registrei o gasto.");
        assertThat(new String(result.audio())).isEqualTo("mp3:Registrei o gasto.");
    }

    @Test
    @DisplayName("Transcrição vazia não chama a IA e pede para repetir")
    void naoEntendeu() {
        AssistantResult result = useCase("   ").fromAudio(new ByteArrayResource(new byte[]{1}));

        assertThat(perguntasAoAssistente).isEmpty();
        assertThat(result.reply()).contains("Pode repetir");
        assertThat(result.audio()).isNotEmpty();
    }

    @Test
    @DisplayName("Fluxo de texto não gera áudio e valida a mensagem")
    void fluxoTexto() {
        AssistantResult result = useCase("ignorado").fromText("quanto gastei?");

        assertThat(result.reply()).isEqualTo("Registrei o gasto.");
        assertThat(result.audio()).isNull();
        assertThatThrownBy(() -> useCase("x").fromText(" ")).isInstanceOf(InvalidAudioException.class);
        assertThatThrownBy(() -> useCase("x").fromText("a".repeat(1001))).isInstanceOf(InvalidAudioException.class);
    }
}
