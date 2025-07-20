package maruhxn.rankademy.adapter.webapi;

import maruhxn.rankademy.adapter.util.ProblemDetailBuilder;
import maruhxn.rankademy.domain.user.exception.DuplicateUsernameException;
import maruhxn.rankademy.domain.user.exception.RequiredUnivInfoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ApiControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(DuplicateUsernameException.class)
    public ProblemDetail duplicateUsernameExceptionHandler(DuplicateUsernameException ex) {
        return ProblemDetailBuilder.build(HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler(RequiredUnivInfoException.class)
    public ProblemDetail requiredUnivInfoExceptionHandler(RequiredUnivInfoException ex) {
        return ProblemDetailBuilder.build(HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail exceptionHandler(Exception ex) {

        return ProblemDetailBuilder.build(HttpStatus.INTERNAL_SERVER_ERROR, ex);
    }
}
