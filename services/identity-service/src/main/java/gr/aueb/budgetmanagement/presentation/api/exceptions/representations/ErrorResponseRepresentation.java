package gr.aueb.budgetmanagement.presentation.api.exceptions.representations;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public record ErrorResponseRepresentation(
    int status,
    String message,
    String timestamp
) {
    public static ErrorResponseRepresentation create(int status, String message) {
        return new ErrorResponseRepresentation(
            status,
            message,
            ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        );
    }
}
