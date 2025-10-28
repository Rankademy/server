package maruhxn.rankademy.adapter.webapi;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import maruhxn.rankademy.adapter.util.ProblemDetailBuilder;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

@Slf4j
@RestControllerAdvice
@Order(1)
public class DbExceptionHandler {

    @ExceptionHandler(
            exception = {
                    DataIntegrityViolationException.class, // 스프링이 대부분의 RDB 제약 위반을 포장해서 던지는 최상위 예외
                    ConstraintViolationException.class, // Hibernate가 던지는 제약 위반 (유니크/외래키/NOT NULL 등)
                    SQLIntegrityConstraintViolationException.class, // JDBC 드라이버가 직접 던지는 무결성 위반
                    DuplicateKeyException.class
            } // 중복 키(보통 유니크 인덱스 충돌)
    )
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail handleDataIntegrityViolation(Exception ex) {
        log.warn(ex.getMessage(), ex);
        return ProblemDetailBuilder.build(HttpStatus.CONFLICT, ex);
    }
}
