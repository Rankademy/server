package maruhxn.rankademy.adapter.webapi;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import lombok.extern.slf4j.Slf4j;
import maruhxn.rankademy.adapter.util.ProblemDetailBuilder;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.method.MethodValidationException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@RestControllerAdvice
@Order(0)
public class MvcExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(status)
                .body(ProblemDetailBuilder.build(status, ex));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return ResponseEntity.status(status)
                .body(ProblemDetailBuilder.build(status, ex));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(status)
                .body(ProblemDetailBuilder.build(status, ex));
    }

    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn(ex.getMessage(), ex);
        ValidationError validationError = new ValidationError(ex.getVariableName(), null, "필수 경로 변수가 누락되었습니다.");
        return ResponseEntity.status(status)
                .body(ProblemDetailBuilder.build(status, ex, List.of(validationError)));
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn(ex.getMessage(), ex);
        ValidationError validationError = new ValidationError(ex.getParameterName(), null, "필수 파라미터가 누락되었습니다.");
        return ResponseEntity.status(status)
                .body(ProblemDetailBuilder.build(status, ex, validationError));
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(MissingServletRequestPartException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn(ex.getMessage(), ex);
        ValidationError validationError = new ValidationError(ex.getRequestPartName(), null, "필수 Part가 누락되었습니다.");
        return ResponseEntity.status(status)
                .body(ProblemDetailBuilder.build(status, ex, validationError));
    }

    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(status)
                .body(ProblemDetailBuilder.build(status, ex));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn(ex.getMessage(), ex);
        List<ValidationError> errors = ex.getBindingResult().getFieldErrors().stream().map(
                fe -> new ValidationError(fe.getField(), fe.getRejectedValue(), fe.getDefaultMessage())
        ).toList();
        return ResponseEntity.status(status)
                .body(ProblemDetailBuilder.build(status, ex, errors));
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn(ex.getMessage(), ex);
        List<ValidationError> errors = ex.getParameterValidationResults().stream().flatMap(
                pr -> pr.getResolvableErrors().stream().map(error ->
                        {
                            String parameterName = pr.getMethodParameter().getParameterName();
                            if (parameterName == null)
                                parameterName = String.valueOf(pr.getMethodParameter().getParameterIndex());
                            return new ValidationError(parameterName, error.getArguments(), error.getDefaultMessage());
                        }
                )
        ).toList();
        return ResponseEntity.status(status)
                .body(ProblemDetailBuilder.build(status, ex, errors));
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(status)
                .body(ProblemDetailBuilder.build(status, ex));
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(status)
                .body(ProblemDetailBuilder.build(status, ex));
    }

    @Override
    protected ResponseEntity<Object> handleAsyncRequestTimeoutException(AsyncRequestTimeoutException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(status)
                .body(ProblemDetailBuilder.build(status, ex));
    }

    @Override
    protected ResponseEntity<Object> handleErrorResponseException(ErrorResponseException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(status)
                .body(ProblemDetailBuilder.build(status, ex));
    }

    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(status)
                .body(ProblemDetailBuilder.build(status, ex));
    }

    @Override
    protected ResponseEntity<Object> handleConversionNotSupported(ConversionNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(status)
                .body(ProblemDetailBuilder.build(status, ex));
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn(ex.getMessage(), ex);
        List<ValidationError> errors;

        if (ex instanceof MethodArgumentTypeMismatchException) {
            MethodArgumentTypeMismatchException e = (MethodArgumentTypeMismatchException) ex;
            errors = List.of(
                    new ValidationError(
                            e.getName(),
                            e.getValue(),
                            "타입 변환에 실패했습니다 to " + e.getRequiredType().getSimpleName()
                    )
            );
        } else errors = new ArrayList<>();
        return ResponseEntity.status(status)
                .body(ProblemDetailBuilder.build(status, ex, errors));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn(ex.getMessage(), ex);
        List<ValidationError> errors = summarizeReadableErrors(ex).stream().distinct().toList();
        return ResponseEntity.status(status)
                .body(ProblemDetailBuilder.build(status, ex, errors));
    }

    private List<ValidationError> summarizeReadableErrors(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        List<ValidationError> errors = new ArrayList<>();

        if (cause instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) cause;
            String field = ife.getPath() != null && !ife.getPath().isEmpty()
                    ? ife.getPath().get(ife.getPath().size() - 1).getFieldName()
                    : "(unknown)";

            String msg;
            Class<?> targetType = ife.getTargetType();
            if (targetType != null) {
                String typeName = targetType.getSimpleName();
                switch (typeName) {
                    case "Integer":
                    case "Long":
                    case "BigDecimal":
                    case "BigInteger":
                    case "Double":
                    case "Float":
                        msg = "숫자 형식이 아닙니다.";
                        break;
                    case "Boolean":
                        msg = "참/거짓 형식이 아닙니다.";
                        break;
                    case "LocalDate":
                        msg = "날짜 형식이 올바르지 않습니다.";
                        break;
                    case "LocalDateTime":
                    case "OffsetDateTime":
                    case "ZonedDateTime":
                        msg = "일시 형식이 올바르지 않습니다.";
                        break;
                    default:
                        msg = "값의 형식이 올바르지 않습니다.";
                        break;
                }
            } else {
                msg = "값의 형식이 올바르지 않습니다.";
            }

            errors.add(new ValidationError(field, null, msg));

        } else if (cause instanceof MismatchedInputException) {
            MismatchedInputException mie = (MismatchedInputException) cause;
            String field = mie.getPath() != null && !mie.getPath().isEmpty()
                    ? mie.getPath().get(mie.getPath().size() - 1).getFieldName()
                    : "(unknown)";
            errors.add(new ValidationError(field, null, "값의 형식이 올바르지 않습니다."));

        } else if (cause instanceof UnrecognizedPropertyException) {
            UnrecognizedPropertyException upe = (UnrecognizedPropertyException) cause;
            String field = upe.getPropertyName() != null ? upe.getPropertyName() : "(unknown)";
            errors.add(new ValidationError(field, null, "허용되지 않은 필드입니다."));

        } else if (cause instanceof JsonParseException) {
            errors.add(new ValidationError("(json)", null, "본문 파싱에 실패했습니다."));
        }

        return errors.isEmpty() ? Collections.emptyList() : errors;
    }

    // 예시용 ValidationError 클래스
    public static class ValidationError {
        private final String field;
        private final Object rejectedValue;
        private final String message;

        public ValidationError(String field, Object rejectedValue, String message) {
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public Object getRejectedValue() {
            return rejectedValue;
        }

        public String getMessage() {
            return message;
        }
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(HttpMessageNotWritableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(status)
                .body(ProblemDetailBuilder.build(status, ex));
    }

    @Override
    protected ResponseEntity<Object> handleMethodValidationException(MethodValidationException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(status)
                .body(ProblemDetailBuilder.build(status, ex));
    }

    @Override
    protected ResponseEntity<Object> handleAsyncRequestNotUsableException(AsyncRequestNotUsableException ex, WebRequest request) {
        log.warn(ex.getMessage(), ex);
        HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status)
                .body(ProblemDetailBuilder.build(status, ex));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        log.warn(ex.getMessage(), ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status)
                .body(ProblemDetailBuilder.build(status, ex));
    }
}
