package com.utch.microservicio.exception;

import com.utch.microservicio.dto.CampoError;
import com.utch.microservicio.dto.ErrorResponse;
import com.utch.microservicio.util.AppConstants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> manejarNoEncontrado(
            RecursoNoEncontradoException ex, HttpServletRequest request) {
        return construirRespuesta(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(DatoDuplicadoException.class)
    public ResponseEntity<ErrorResponse> manejarDuplicado(
            DatoDuplicadoException ex, HttpServletRequest request) {
        return construirRespuesta(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidacion(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<CampoError> errores = ex.getBindingResult().getFieldErrors().stream()
                .map(this::mapearCampoError)
                .toList();
        return construirRespuesta(
                HttpStatus.BAD_REQUEST,
                AppConstants.ERROR_VALIDACION,
                request.getRequestURI(),
                errores);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarGenerico(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado: {}", ex.getMessage(), ex);
        return construirRespuesta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                AppConstants.ERROR_INTERNO,
                request.getRequestURI(),
                null);
    }

    private CampoError mapearCampoError(FieldError fieldError) {
        return CampoError.builder()
                .campo(fieldError.getField())
                .mensaje(fieldError.getDefaultMessage())
                .build();
    }

    private ResponseEntity<ErrorResponse> construirRespuesta(
            HttpStatus status, String mensaje, String path, List<CampoError> errores) {
        ErrorResponse cuerpo = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .mensaje(mensaje)
                .path(path)
                .errores(errores)
                .build();
        return ResponseEntity.status(status).body(cuerpo);
    }
}
