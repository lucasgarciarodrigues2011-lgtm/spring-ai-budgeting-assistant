package dio.budgeting.application.assistant;

/** Porta: transforma a resposta em áudio (MP3). */
public interface TextToSpeech {

    byte[] synthesize(String text);
}
