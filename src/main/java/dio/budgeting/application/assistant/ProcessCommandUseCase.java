package dio.budgeting.application.assistant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * Orquestra o fluxo principal do assistente: áudio -> texto -> IA com tools -> resposta (texto e voz).
 * <p>
 * No projeto base esse fluxo ficava dentro do controller, preso às classes do Spring AI.
 * Aqui ele depende apenas de três portas, o que permite testar o fluxo inteiro sem chamar a OpenAI
 * e trocar de provedor sem mexer na regra.
 */
@Service
public class ProcessCommandUseCase {

    static final String NAO_ENTENDI = "Não consegui entender o áudio. Pode repetir, dizendo o valor e onde foi o gasto?";
    static final int MAX_MESSAGE_LENGTH = 1000;

    private static final Logger log = LoggerFactory.getLogger(ProcessCommandUseCase.class);

    private final SpeechToText speechToText;
    private final BudgetAssistant assistant;
    private final TextToSpeech textToSpeech;

    public ProcessCommandUseCase(SpeechToText speechToText, BudgetAssistant assistant, TextToSpeech textToSpeech) {
        this.speechToText = speechToText;
        this.assistant = assistant;
        this.textToSpeech = textToSpeech;
    }

    /** Fluxo completo por voz: devolve a transcrição, a resposta em texto e a resposta em áudio. */
    public AssistantResult fromAudio(Resource audio) {
        if (audio == null || !audio.exists()) {
            throw new InvalidAudioException("Envie um arquivo de áudio no campo 'file'.");
        }
        long inicio = System.currentTimeMillis();
        String transcription = speechToText.transcribe(audio);
        String reply = isBlank(transcription) ? NAO_ENTENDI : assistant.reply(transcription.strip());
        byte[] speech = textToSpeech.synthesize(reply);
        audit("voz", transcription, reply, inicio);
        return new AssistantResult(transcription, reply, speech);
    }

    /** Mesmo fluxo, só que em texto: útil para testar a IA sem gravar áudio e sem custo de transcrição e voz. */
    public AssistantResult fromText(String message) {
        if (isBlank(message)) {
            throw new InvalidAudioException("A mensagem não pode ser vazia.");
        }
        if (message.length() > MAX_MESSAGE_LENGTH) {
            throw new InvalidAudioException("A mensagem deve ter no máximo " + MAX_MESSAGE_LENGTH + " caracteres.");
        }
        long inicio = System.currentTimeMillis();
        String reply = assistant.reply(message.strip());
        audit("texto", message, reply, inicio);
        return new AssistantResult(message, reply, null);
    }

    /** Auditoria simples: registra o que foi pedido, o que foi respondido e quanto tempo levou. */
    private void audit(String canal, String entrada, String resposta, long inicio) {
        log.info("[assistente] canal={} duracaoMs={} entrada=\"{}\" resposta=\"{}\"",
                canal, System.currentTimeMillis() - inicio, entrada, resposta);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
