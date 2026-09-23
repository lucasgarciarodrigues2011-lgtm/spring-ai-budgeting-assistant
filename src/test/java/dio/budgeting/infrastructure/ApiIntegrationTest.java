package dio.budgeting.infrastructure;

import dio.budgeting.application.assistant.BudgetAssistant;
import dio.budgeting.application.assistant.SpeechToText;
import dio.budgeting.application.assistant.TextToSpeech;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sobe a aplicação inteira (Spring + JPA + H2) e testa os endpoints.
 * As três portas de IA são substituídas por mocks: o teste roda no CI sem chave da OpenAI e sem custo.
 */
@SpringBootTest
@ActiveProfiles("test")
class ApiIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private SpeechToText speechToText;

    @MockitoBean
    private BudgetAssistant assistant;

    @MockitoBean
    private TextToSpeech textToSpeech;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("POST /transactions grava em reais e devolve em reais, com a data de hoje")
    void criaTransacao() throws Exception {
        mvc.perform(post("/transactions").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\": \"Padaria\", \"category\": \"GROCERIES\", \"amount\": 12.40}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(12.40))
                .andExpect(jsonPath("$.date").value(LocalDate.now(java.time.ZoneId.of("America/Sao_Paulo")).toString()));

        mvc.perform(get("/transactions").param("category", "GROCERIES"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].description", hasItem("Padaria")));

        mvc.perform(get("/transactions/GROCERIES"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].description", hasItem("Padaria")));
    }

    @Test
    @DisplayName("Dados inválidos retornam 422 com a lista de erros, sem gravar")
    void validacao() throws Exception {
        mvc.perform(post("/transactions").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\": \"\", \"category\": \"PHARMA\", \"amount\": -5}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors", hasSize(2)));
    }

    @Test
    @DisplayName("GET /transactions/summary soma o período por categoria")
    void resumo() throws Exception {
        mvc.perform(post("/transactions").contentType(MediaType.APPLICATION_JSON)
                .content("{\"description\": \"Cinema\", \"category\": \"LEISURE\", \"amount\": 40, \"date\": \"2025-01-10\"}"));
        mvc.perform(post("/transactions").contentType(MediaType.APPLICATION_JSON)
                .content("{\"description\": \"Show\", \"category\": \"LEISURE\", \"amount\": 160.50, \"date\": \"2025-01-20\"}"));

        mvc.perform(get("/transactions/summary").param("from", "2025-01-01").param("to", "2025-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(200.50))
                .andExpect(jsonPath("$.transactions").value(2))
                .andExpect(jsonPath("$.byCategory[0].category").value("LEISURE"));
    }

    @Test
    @DisplayName("POST /transactions/ai recebe áudio e devolve MP3 com transcrição e resposta nos cabeçalhos")
    void fluxoDeVoz() throws Exception {
        given(speechToText.transcribe(any())).willReturn("Gastei 30 reais na farmácia");
        given(assistant.reply("Gastei 30 reais na farmácia")).willReturn("Registrei trinta reais em farmácia.");
        given(textToSpeech.synthesize(anyString())).willReturn(new byte[]{9, 9, 9});

        var audio = new MockMultipartFile("file", "comando.m4a", "audio/mp4", new byte[]{1, 2, 3});

        mvc.perform(multipart("/transactions/ai").file(audio))
                .andExpect(status().isOk())
                .andExpect(content().bytes(new byte[]{9, 9, 9}))
                .andExpect(header().string("X-Transcription", "Gastei+30+reais+na+farm%C3%A1cia"))
                .andExpect(header().exists("X-Assistant-Reply"));
    }

    @Test
    @DisplayName("Áudio vazio é recusado antes de chamar a IA")
    void audioVazio() throws Exception {
        var vazio = new MockMultipartFile("file", "vazio.m4a", "audio/mp4", new byte[0]);

        mvc.perform(multipart("/transactions/ai").file(vazio))
                .andExpect(status().isBadRequest());
        verify(speechToText, never()).transcribe(any());
    }

    @Test
    @DisplayName("POST /transactions/ai/text conversa com o assistente em texto")
    void fluxoDeTexto() throws Exception {
        given(assistant.reply("quanto gastei este mês?")).willReturn("Você gastou cem reais.");

        mvc.perform(post("/transactions/ai/text").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\": \"quanto gastei este mês?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("Você gastou cem reais."));
    }
}
