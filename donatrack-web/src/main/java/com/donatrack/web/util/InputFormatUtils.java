package com.donatrack.web.util;

import java.util.regex.Pattern;

/** Utilidades compartidas de normalización y validación de identificadores y teléfonos. */
public final class InputFormatUtils {

  private static final Pattern CUIT_PATTERN = Pattern.compile("^\\d{2}-\\d{8}-\\d$");
  private static final Pattern CELULAR_STRICT_PATTERN =
      Pattern.compile("^\\+54 9 \\d{2,4} \\d{2,4}-\\d{4}$");
  private static final Pattern CELULAR_ADMIN_PATTERN =
      Pattern.compile("^\\+54 (?:9 )?\\d{2,4} \\d{2,4}-\\d{4}$");
  private static final Pattern PATENTE_PATTERN =
      Pattern.compile("^([A-Z]{3}[0-9]{3}|[A-Z]{2}[0-9]{3}[A-Z]{2})$");
  private static final Pattern LEGAJO_PATTERN = Pattern.compile("^CH-\\d{3,4}$");

  private InputFormatUtils() {}

  public static String normalizarCuit(String raw) {
    if (raw == null) {
      return "";
    }
    String digits = raw.replaceAll("\\D", "");
    if (digits.length() == 11) {
      return digits.substring(0, 2)
          + "-"
          + digits.substring(2, 10)
          + "-"
          + digits.substring(10);
    }
    return raw.trim();
  }

  public static boolean esCuitValido(String cuit) {
    return cuit != null && CUIT_PATTERN.matcher(cuit.trim()).matches();
  }

  public static String normalizarCelularArg(String raw) {
    if (raw == null) {
      return "";
    }
    String trimmed = raw.trim();
    if (esCelularArgStrictValido(trimmed)) {
      return trimmed;
    }
    String digits = trimmed.replaceAll("\\D", "");
    if (digits.startsWith("549") && digits.length() == 13) {
      digits = digits.substring(3);
    } else if (digits.startsWith("54") && digits.length() == 12) {
      digits = digits.substring(2);
    } else if (digits.startsWith("0") && digits.length() == 11) {
      digits = digits.substring(1);
    }

    // 8 dígitos sin código de área -> asume AMBA (11)
    if (digits.length() == 8) {
      return "+54 9 11 " + digits.substring(0, 4) + "-" + digits.substring(4);
    }
    // 10 dígitos nacionales (código de área de 2, 3 o 4 dígitos + número local)
    if (digits.length() == 10) {
      if (digits.startsWith("11")) {
        return "+54 9 11 " + digits.substring(2, 6) + "-" + digits.substring(6);
      }
      // Si el usuario ya separó un código de área de 4 dígitos, respetarlo; si no, agrupar 3 + 3-4
      String[] tokens = trimmed.replaceFirst("^\\+?54\\s*9?\\s*", "").split("[\\s-]+");
      if (tokens.length >= 2 && tokens[0].replaceAll("\\D", "").length() == 4) {
        return "+54 9 "
            + digits.substring(0, 4)
            + " "
            + digits.substring(4, 6)
            + "-"
            + digits.substring(6);
      }
      return "+54 9 "
          + digits.substring(0, 3)
          + " "
          + digits.substring(3, 6)
          + "-"
          + digits.substring(6);
    }
    return trimmed;
  }

  public static boolean esCelularArgStrictValido(String tel) {
    if (tel == null || !CELULAR_STRICT_PATTERN.matcher(tel.trim()).matches()) {
      return false;
    }
    String digits = tel.replaceAll("\\D", "");
    return digits.startsWith("549") && digits.length() == 13;
  }

  public static boolean esCelularAdminValido(String tel) {
    if (tel == null || !CELULAR_ADMIN_PATTERN.matcher(tel.trim()).matches()) {
      return false;
    }
    String digits = tel.replaceAll("\\D", "");
    return (digits.startsWith("549") && digits.length() == 13)
        || (digits.startsWith("54") && !digits.startsWith("549") && digits.length() == 12);
  }

  public static String normalizarPatente(String raw) {
    if (raw == null) {
      return "";
    }
    return raw.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
  }

  public static boolean esPatenteArgentinaValida(String patente) {
    return patente != null && PATENTE_PATTERN.matcher(patente).matches();
  }

  public static String normalizarLegajo(String raw) {
    if (raw == null) {
      return "";
    }
    String clean = raw.trim().toUpperCase().replaceAll("\\s+", "");
    if (clean.matches("^CH\\d{3,4}$")) {
      return "CH-" + clean.substring(2);
    }
    if (clean.matches("^\\d{3,4}$")) {
      return "CH-" + clean;
    }
    return clean;
  }

  public static boolean esLegajoValido(String legajo) {
    return legajo != null && LEGAJO_PATTERN.matcher(legajo.trim()).matches();
  }
}
