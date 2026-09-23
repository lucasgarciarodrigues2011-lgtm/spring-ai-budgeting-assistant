package dio.budgeting.application.assistant;

import org.springframework.core.io.Resource;

/** Porta: transforma áudio em texto. A implementação real usa o TranscriptionModel do Spring AI. */
public interface SpeechToText {

    String transcribe(Resource audio);
}
