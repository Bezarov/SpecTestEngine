package com.example.spectestengine.exception;

import com.example.spectestengine.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@ControllerAdvice
@Slf4j
public class RestExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponseDTO> handleResponseStatusException(ResponseStatusException exception, HttpServletRequest request) {
        log.info("Response Exception was intercepted and relayed, Status code: '{}', message : '{}'",
                exception.getStatusCode(), exception.getReason());

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                exception.getStatusCode().toString(),
                exception.getReason(),
                request.getRequestURI()
        );

        return ResponseEntity.status(exception.getStatusCode()).body(errorResponse);
    }

    @ExceptionHandler(InvalidSpecException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidSpecException(InvalidSpecException invalidSpecException, HttpServletRequest request) {
        log.info("InvalidSpecException was intercepted and relayed, message: '{}'", invalidSpecException.getMessage());

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                HttpStatus.BAD_REQUEST.toString(),
                invalidSpecException.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<String> handleGenericError(Throwable throwable) {
        StackTraceElement traceElement = throwable.getStackTrace()[0];
        log.error("Generic error in class: '{}', method: '{}', line: '{}', the error: '{}'",
                traceElement.getClassName(), traceElement.getMethodName(),
                traceElement.getLineNumber(), throwable.getMessage(), throwable);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
    }
}