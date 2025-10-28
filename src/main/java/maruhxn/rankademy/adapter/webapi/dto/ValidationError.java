package maruhxn.rankademy.adapter.webapi.dto;

public record ValidationError(
        String field,
        String rejectedValue,
        String message
) {
}
