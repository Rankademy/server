package maruhxn.rankademy.adapter.webapi;

import lombok.extern.slf4j.Slf4j;
import maruhxn.rankademy.adapter.util.ProblemDetailBuilder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn(ex.getMessage(), ex);
        return ProblemDetailBuilder.build(HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalStateException(IllegalStateException ex) {
        log.warn(ex.getMessage(), ex);
        return ProblemDetailBuilder.build(HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDeniedException(AccessDeniedException ex) {
        log.warn(ex.getMessage(), ex);
        return ProblemDetailBuilder.build(HttpStatus.FORBIDDEN, ex);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail handleNoSuchElementException(NoSuchElementException ex) {
        log.warn(ex.getMessage(), ex);
        return ProblemDetailBuilder.build(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail exceptionHandler(Exception ex) {
        log.error(ex.getMessage(), ex);
        return ProblemDetailBuilder.build(HttpStatus.INTERNAL_SERVER_ERROR, ex);
    }
}
