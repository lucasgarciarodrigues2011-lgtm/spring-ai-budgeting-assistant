package dio.budgeting.infrastructure.ai;

import dio.budgeting.application.assistant.SpeechToText;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class OpenAiSpeechToText implements SpeechToText {

    private final TranscriptionModel transcriptionModel;

    public OpenAiSpeechToText(TranscriptionModel transcriptionModel) {
        this.transcriptionModel = transcriptionModel;
    }

    @Override
    public String transcribe(Resource audio) {
        return transcriptionModel.transcribe(audio);
    }
}
