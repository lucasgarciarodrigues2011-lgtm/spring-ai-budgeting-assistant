package dio.budgeting.infrastructure.ai;

import dio.budgeting.application.assistant.TextToSpeech;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.stereotype.Component;

@Component
public class OpenAiTextToSpeech implements TextToSpeech {

    private final TextToSpeechModel textToSpeechModel;

    public OpenAiTextToSpeech(TextToSpeechModel textToSpeechModel) {
        this.textToSpeechModel = textToSpeechModel;
    }

    @Override
    public byte[] synthesize(String text) {
        return textToSpeechModel.call(text);
    }
}
