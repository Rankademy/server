package maruhxn.rankademy.adapter.webapi;

import maruhxn.rankademy.domain.member.exception.DuplicateUsernameException;
import maruhxn.rankademy.domain.member.exception.RequiredUnivInfoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ApiControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(DuplicateUsernameException.class)
    public ProblemDetail duplicateUsernameExceptionHandler(DuplicateUsernameException ex) {
        return buildProblemDetail(HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler(RequiredUnivInfoException.class)
    public ProblemDetail requiredUnivInfoExceptionHandler(RequiredUnivInfoException ex) {
        return buildProblemDetail(HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail exceptionHandler(Exception ex) {

        return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex);
    }

    private static ProblemDetail buildProblemDetail(HttpStatus status, Exception exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());

        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setProperty("exception", exception.getClass().getSimpleName());

        return problemDetail;
    }
}
