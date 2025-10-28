package maruhxn.rankademy.adapter.util;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;

import java.time.LocalDateTime;

public class ProblemDetailBuilder {

    public static ProblemDetail build(HttpStatusCode status, Exception exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());

        problemDetail.setProperty("timestamp", LocalDateTime.now().toString());
        problemDetail.setProperty("exception", exception.getClass().getSimpleName());

        return problemDetail;
    }

    public static ProblemDetail build(HttpStatusCode status, Exception exception, Object data) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());

        problemDetail.setProperty("timestamp", LocalDateTime.now().toString());
        problemDetail.setProperty("data", data.toString());

        return problemDetail;
    }
}
