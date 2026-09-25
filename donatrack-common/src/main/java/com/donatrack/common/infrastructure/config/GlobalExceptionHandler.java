package com.donatrack.common.infrastructure.config;

import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  // --- 400 Bad Request ---

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArgument(
      IllegalArgumentException ex, HttpServletRequest request) {
    return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<Map<String, Object>> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
    String mensaje =
        String.format(
            "El parámetro '%s' tiene un valor inválido: '%s'. Se esperaba un valor de tipo %s.",
            ex.getName(),
            ex.getValue(),
            ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconocido");
    return buildResponse(HttpStatus.BAD_REQUEST, mensaje, request);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Map<String, Object>> handleUnreadableMessage(
      HttpMessageNotReadableException ex, HttpServletRequest request) {

    String mensaje = "El cuerpo de la solicitud no es válido.";

    Throwable cause = ex.getCause();
    if (cause instanceof InvalidTypeIdException typeEx) {
      mensaje =
          String.format(
              "Falta el campo discriminador de tipo o su valor es inválido. "
                  + "Para 'Persona' se requiere el campo 'tipo' con valor 'HUMANA' o 'JURIDICA'. "
                  + "Detalle: %s",
              typeEx.getOriginalMessage());
    } else if (cause != null) {
      String causeMsg = cause.getMessage();
      if (causeMsg != null && causeMsg.length() > 200) {
        causeMsg = causeMsg.substring(0, 200) + "...";
      }
      mensaje = String.format("Error al parsear el JSON: %s", causeMsg);
    }

    return buildResponse(HttpStatus.BAD_REQUEST, mensaje, request);
  }

  // --- 404 Not Found ---

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<Map<String, Object>> handleNotFound(
      NoSuchElementException ex, HttpServletRequest request) {
    String mensaje =
        ex.getMessage() != null ? ex.getMessage() : "El recurso solicitado no fue encontrado.";
    return buildResponse(HttpStatus.NOT_FOUND, mensaje, request);
  }

  // --- 500 Internal Server Error (fallback) ---

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGenericException(
      Exception ex, HttpServletRequest request) {
    log.error("Error interno no manejado en {}: ", request.getRequestURI(), ex);
    return buildResponse(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Error interno del servidor. Revisá los logs para más detalles.",
        request);
  }

  // --- Utilidad ---

  private ResponseEntity<Map<String, Object>> buildResponse(
      HttpStatus status, String message, HttpServletRequest request) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now().toString());
    body.put("status", status.value());
    body.put("error", status.getReasonPhrase());
    body.put("message", message);
    body.put("path", request.getRequestURI());
    return ResponseEntity.status(status).body(body);
  }
}
