package com.donatrack.web.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class WebGlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(WebGlobalExceptionHandler.class);
  public static final String GENERIC_ERROR_MESSAGE =
      "Algo ha salido mal, intentalo de nuevo en unos minutos...";

  @ExceptionHandler({AccessDeniedException.class, AuthenticationException.class})
  public void rethrowSecurityExceptions(Exception ex) throws Exception {
    throw ex;
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public Object handleNotFound(
      NoResourceFoundException ex, HttpServletRequest request, HttpServletResponse response) {
    if (isApiRequest(request)) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .contentType(MediaType.APPLICATION_JSON)
          .body(Map.of("error", "No encontramos el recurso solicitado."));
    }
    response.setStatus(HttpStatus.NOT_FOUND.value());
    ModelAndView mav = new ModelAndView("error");
    mav.addObject("errorTitle", "Página no encontrada");
    mav.addObject(
        "errorMessage",
        "No pudimos encontrar la sección que estabas buscando. Podés volver al inicio.");
    mav.addObject("statusCode", 404);
    return mav;
  }

  @ExceptionHandler(Exception.class)
  public Object handleUnhandledException(
      Exception ex, HttpServletRequest request, HttpServletResponse response) {
    log.error("Error no controlado en la web ({}): {}", request.getRequestURI(), ex.getMessage(), ex);
    if (isApiRequest(request)) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .contentType(MediaType.APPLICATION_JSON)
          .body(Map.of("error", GENERIC_ERROR_MESSAGE));
    }
    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    ModelAndView mav = new ModelAndView("error");
    mav.addObject("errorTitle", "Tuvimos un inconveniente inesperado");
    mav.addObject("errorMessage", GENERIC_ERROR_MESSAGE);
    mav.addObject("statusCode", 500);
    return mav;
  }

  private boolean isApiRequest(HttpServletRequest request) {
    String uri = request.getRequestURI();
    String accept = request.getHeader("Accept");
    return (uri != null && uri.startsWith("/api/"))
        || (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE));
  }
}
