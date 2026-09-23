package dio.budgeting.infrastructure.http.response;

import dio.budgeting.application.assistant.AssistantResult;

public record AssistantResponse(String message, String reply) {

    public static AssistantResponse from(AssistantResult result) {
        return new AssistantResponse(result.transcription(), result.reply());
    }
}
