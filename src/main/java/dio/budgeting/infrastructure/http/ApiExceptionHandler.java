package dio.budgeting.infrastructure.http;

import dio.budgeting.application.assistant.InvalidAudioException;
import dio.budgeting.domain.InvalidTransactionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Erros de negócio no formato Problem Details (RFC 9457), em vez de um 500 genérico. */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InvalidTransactionException.class)
    ProblemDetail invalidTransaction(InvalidTransactionException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, "A transação tem dados inválidos.");
        problem.setTitle("Transação inválida");
        problem.setProperty("errors", e.getErrors());
        return problem;
    }

    @ExceptionHandler(InvalidAudioException.class)
    ProblemDetail invalidAudio(InvalidAudioException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problem.setTitle("Requisição inválida");
        return problem;
    }
}
