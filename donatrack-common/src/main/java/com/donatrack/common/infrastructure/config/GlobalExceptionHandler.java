package com.donatrack.common.infrastructure.config;

import com.donatrack.common.dto.ErrorResponse;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  // --- 400 Bad Request ---

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(
      IllegalArgumentException ex, HttpServletRequest request) {
    return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ErrorResponse> handleIllegalState(
      IllegalStateException ex, HttpServletRequest request) {
    return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
    String mensaje =
        String.format(
            "El parámetro '%s' tiene un valor inválido: '%s'. Se esperaba un valor de tipo %s.",
            ex.getName(),
            ex.getValue(),
            ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconocido");
    return buildResponse(HttpStatus.BAD_REQUEST, mensaje, request);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingParameter(
      MissingServletRequestParameterException ex, HttpServletRequest request) {
    String mensaje =
        String.format(
            "Falta el parámetro de solicitud requerido: '%s' (tipo esperado: %s).",
            ex.getParameterName(), ex.getParameterType());
    return buildResponse(HttpStatus.BAD_REQUEST, mensaje, request);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleUnreadableMessage(
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

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    List<ErrorResponse.CampoError> detalles =
        ex.getBindingResult().getFieldErrors().stream()
            .map(
                fieldError ->
                    new ErrorResponse.CampoError(
                        fieldError.getField(), fieldError.getDefaultMessage()))
            .toList();

    ErrorResponse response =
        new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "Error de validación en los campos enviados.",
            request.getRequestURI(),
            detalles);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(
      ConstraintViolationException ex, HttpServletRequest request) {
    List<ErrorResponse.CampoError> detalles =
        ex.getConstraintViolations().stream()
            .map(
                cv ->
                    new ErrorResponse.CampoError(cv.getPropertyPath().toString(), cv.getMessage()))
            .toList();

    ErrorResponse response =
        new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "Violación de restricciones de validación.",
            request.getRequestURI(),
            detalles);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  // --- 404 Not Found ---

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(
      NoSuchElementException ex, HttpServletRequest request) {
    String mensaje =
        ex.getMessage() != null ? ex.getMessage() : "El recurso solicitado no fue encontrado.";
    return buildResponse(HttpStatus.NOT_FOUND, mensaje, request);
  }

  // --- 405 Method Not Allowed ---

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMethodNotSupported(
      HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
    String mensaje =
        String.format(
            "El método HTTP '%s' no está soportado para esta ruta. Métodos permitidos: %s",
            ex.getMethod(),
            ex.getSupportedHttpMethods() != null ? ex.getSupportedHttpMethods() : "[]");
    return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, mensaje, request);
  }

  // --- 409 Conflict ---

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
      DataIntegrityViolationException ex, HttpServletRequest request) {
    log.warn(
        "Violación de integridad de datos en {}: {}", request.getRequestURI(), ex.getMessage());
    return buildResponse(
        HttpStatus.CONFLICT,
        "Conflicto de datos: se violó una restricción de integridad (clave duplicada o referencia inválida).",
        request);
  }

  // --- 413 Payload Too Large ---

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ErrorResponse> handleMaxUploadSize(
      MaxUploadSizeExceededException ex, HttpServletRequest request) {
    return buildResponse(
        HttpStatus.PAYLOAD_TOO_LARGE,
        "El archivo subido supera el límite máximo de tamaño permitido.",
        request);
  }

  // --- 415 Unsupported Media Type ---

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(
      HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
    String mensaje =
        String.format(
            "El Content-Type '%s' no es compatible con este endpoint. Formatos esperados: %s",
            ex.getContentType(), ex.getSupportedMediaTypes());
    return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, mensaje, request);
  }

  // --- 500 Internal Server Error (fallback) ---

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(
      Exception ex, HttpServletRequest request) {
    log.error("Error interno no manejado en {}: ", request.getRequestURI(), ex);
    return buildResponse(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Error interno del servidor. Revisá los logs para más detalles.",
        request);
  }

  // --- Utilidad ---

  private ResponseEntity<ErrorResponse> buildResponse(
      HttpStatus status, String message, HttpServletRequest request) {
    ErrorResponse response =
        new ErrorResponse(
            LocalDateTime.now(),
            status.value(),
            status.getReasonPhrase(),
            message,
            request.getRequestURI());
    return ResponseEntity.status(status).body(response);
  }
}
