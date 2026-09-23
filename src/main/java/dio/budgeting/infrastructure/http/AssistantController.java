package dio.budgeting.infrastructure.http;

import dio.budgeting.application.assistant.AssistantResult;
import dio.budgeting.application.assistant.InvalidAudioException;
import dio.budgeting.application.assistant.ProcessCommandUseCase;
import dio.budgeting.infrastructure.http.request.TextCommandRequest;
import dio.budgeting.infrastructure.http.response.AssistantResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Endpoints do assistente de IA (voz e texto). */
@RestController
@RequestMapping("/transactions/ai")
public class AssistantController {

    public static final String HEADER_TRANSCRIPTION = "X-Transcription";
    public static final String HEADER_REPLY = "X-Assistant-Reply";

    private final ProcessCommandUseCase processCommand;

    public AssistantController(ProcessCommandUseCase processCommand) {
        this.processCommand = processCommand;
    }

    /**
     * Fluxo original: recebe áudio, devolve MP3. Agora também devolve, em cabeçalhos,
     * o que foi entendido e a resposta em texto (codificados em URL, pois cabeçalhos HTTP não aceitam acentos).
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "audio/mp3")
    public ResponseEntity<Resource> fromAudio(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidAudioException("Envie um arquivo de áudio não vazio no campo 'file'.");
        }
        AssistantResult result = processCommand.fromAudio(file.getResource());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("resposta.mp3").build().toString())
                .header(HEADER_TRANSCRIPTION, encode(result.transcription()))
                .header(HEADER_REPLY, encode(result.reply()))
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HEADER_TRANSCRIPTION + ", " + HEADER_REPLY)
                .body(new ByteArrayResource(result.audio()));
    }

    /** Novo: o mesmo assistente em texto. Bom para testar a IA sem gravar áudio. */
    @PostMapping(value = "/text", consumes = MediaType.APPLICATION_JSON_VALUE)
    public AssistantResponse fromText(@RequestBody TextCommandRequest request) {
        return AssistantResponse.from(processCommand.fromText(request.message()));
    }

    private static String encode(String value) {
        return value == null ? "" : URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
