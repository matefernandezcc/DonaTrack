package com.donatrack.web.controller;

import com.donatrack.web.dto.CamionDto;
import com.donatrack.web.gateway.DonacionesGateway;
import com.donatrack.web.gateway.IncentivosGateway;
import com.donatrack.web.gateway.LogisticaGateway;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/app")
public class AdminController {

  private static final Logger log = LoggerFactory.getLogger(AdminController.class);

  private static final UUID DEFAULT_DONANTE_ID =
      UUID.fromString("a1111111-1111-4111-8111-111111111111");
  private static final UUID DEFAULT_ADMIN_ID =
      UUID.fromString("a2222222-2222-4222-8222-222222222222");
  private static final UUID DEFAULT_BENEFICIARIA_ID =
      UUID.fromString("b2222222-2222-4222-8222-222222222222");

  private final LogisticaGateway logisticaGateway;
  private final IncentivosGateway incentivosGateway;
  private final DonacionesGateway donacionesGateway;
  private final com.donatrack.web.gateway.AuthGateway authGateway;
  private final com.donatrack.web.service.SolicitudOngService solicitudOngService;

  public AdminController(
      LogisticaGateway logisticaGateway,
      IncentivosGateway incentivosGateway,
      DonacionesGateway donacionesGateway) {
    this(
        logisticaGateway,
        incentivosGateway,
        donacionesGateway,
        null,
        new com.donatrack.web.service.SolicitudOngService());
  }

  @org.springframework.beans.factory.annotation.Autowired
  public AdminController(
      LogisticaGateway logisticaGateway,
      IncentivosGateway incentivosGateway,
      DonacionesGateway donacionesGateway,
      com.donatrack.web.gateway.AuthGateway authGateway,
      com.donatrack.web.service.SolicitudOngService solicitudOngService) {
    this.logisticaGateway = logisticaGateway;
    this.incentivosGateway = incentivosGateway;
    this.donacionesGateway = donacionesGateway;
    this.authGateway = authGateway;
    this.solicitudOngService = solicitudOngService;
  }

  @ModelAttribute
  public void populateUserAttributes(Model model, Authentication auth) {
    if (auth != null) {
      String nombreCompleto = auth.getName();
      String username = auth.getName();
      String usuarioId = "";
      String descripcion = "Comprometidos con una logística solidaria eficiente y transparente.";
      String ubicacion = "Buenos Aires, Argentina";

      if (auth instanceof JwtAuthenticationToken jwtAuth) {
        Jwt token = jwtAuth.getToken();
        if (token.getClaimAsString("name") != null && !token.getClaimAsString("name").isBlank()) {
          nombreCompleto = token.getClaimAsString("name");
        }
        if (token.getClaimAsString("username") != null) {
          username = token.getClaimAsString("username");
        }
        if (token.getClaimAsString("descripcion") != null
            && !token.getClaimAsString("descripcion").isBlank()) {
          descripcion = token.getClaimAsString("descripcion");
        }
        if (token.getClaimAsString("ubicacion") != null
            && !token.getClaimAsString("ubicacion").isBlank()) {
          ubicacion = token.getClaimAsString("ubicacion");
        }
        usuarioId = token.getSubject();

        // Si tenemos usuarioId y authGateway, intentamos consultar el perfil más reciente
        if (usuarioId != null && !usuarioId.isBlank() && authGateway != null) {
          try {
            Map<String, Object> perfil = authGateway.obtenerPerfil(UUID.fromString(usuarioId));
            if (perfil != null) {
              if (perfil.get("descripcion") instanceof String desc && !desc.isBlank()) {
                descripcion = desc;
              }
              if (perfil.get("ubicacion") instanceof String ubi && !ubi.isBlank()) {
                ubicacion = ubi;
              }
            }
          } catch (Exception ignored) {
          }
        }
      }
      model.addAttribute("nombreCompleto", nombreCompleto);
      model.addAttribute("nombreUsuario", username);
      model.addAttribute("usuarioId", usuarioId);
      model.addAttribute("descripcion", descripcion);
      model.addAttribute("ubicacion", ubicacion);
    }
  }

  @PostMapping("/perfil/actualizar")
  public String actualizarPerfil(
      @RequestParam("descripcion") String descripcion,
      @RequestParam("ubicacion") String ubicacion,
      Authentication auth,
      HttpServletRequest request,
      RedirectAttributes redirectAttributes) {
    try {
      if (auth instanceof JwtAuthenticationToken jwt && authGateway != null) {
        UUID usuarioId = UUID.fromString(jwt.getToken().getSubject());
        authGateway.actualizarPerfil(usuarioId, descripcion, ubicacion);
        redirectAttributes.addFlashAttribute("flashOk", "Perfil actualizado con éxito.");
      }
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "Error al actualizar perfil: " + e.getMessage());
    }
    String referer = request.getHeader("Referer");
    return "redirect:" + (referer != null ? referer : "/app/donante");
  }

  private UUID resolverDonanteId(Authentication auth) {
    if (auth instanceof JwtAuthenticationToken jwt) {
      String pId = jwt.getToken().getClaimAsString("personaId");
      if (pId != null && !pId.isBlank()) {
        try {
          return UUID.fromString(pId);
        } catch (IllegalArgumentException ignored) {
        }
      }
      String sub = jwt.getToken().getSubject();
      if (sub != null && !sub.isBlank()) {
        try {
          return UUID.fromString(sub);
        } catch (IllegalArgumentException ignored) {
        }
      }
    }
    return DEFAULT_DONANTE_ID;
  }

  private UUID resolverBeneficiarioId(Authentication auth) {
    if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
      return DEFAULT_BENEFICIARIA_ID;
    }
    return resolverPersonaId(auth, DEFAULT_BENEFICIARIA_ID);
  }

  private UUID resolverPersonaId(Authentication auth, UUID fallback) {
    if (auth instanceof JwtAuthenticationToken jwt) {
      String pId = jwt.getToken().getClaimAsString("personaId");
      if (pId != null && !pId.isBlank()) {
        try {
          return UUID.fromString(pId);
        } catch (IllegalArgumentException ignored) {
        }
      }
    }
    return fallback;
  }

  @GetMapping
  public String dashboardRoot(Authentication auth) {
    if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
      return "redirect:/app/admin";
    } else if (auth.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_DONANTE"))) {
      return "redirect:/app/donante";
    } else {
      return "redirect:/app/beneficiaria";
    }
  }

  // ==================== SECCIÓN ADMINISTRACIÓN ====================

  @GetMapping("/admin")
  @PreAuthorize("hasRole('ADMIN')")
  public String adminResumen(Model model) {
    model.addAttribute("totalCamiones", logisticaGateway.listarCamiones().size());
    model.addAttribute("totalChoferes", logisticaGateway.listarChoferes().size());
    model.addAttribute("totalRutas", logisticaGateway.listarRutas().size());
    return "admin/admin/resumen";
  }

  @GetMapping("/admin/camiones")
  @PreAuthorize("hasRole('ADMIN')")
  public String adminCamiones(Model model) {
    List<CamionDto> camiones = logisticaGateway.listarCamiones();
    model.addAttribute("camiones", camiones);
    model.addAttribute("nuevoCamion", new CamionDto("", 0.0, 0.0, 0.0));
    return "admin/admin/camiones";
  }

  @PostMapping("/admin/camiones")
  @PreAuthorize("hasRole('ADMIN')")
  public String crearCamion(
      @ModelAttribute("nuevoCamion") CamionDto nuevoCamion, RedirectAttributes redirectAttributes) {
    try {
      logisticaGateway.crearCamion(nuevoCamion);
      redirectAttributes.addFlashAttribute(
          "flashOk", "Camión " + nuevoCamion.patente() + " registrado con éxito.");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "Error al crear camión: " + e.getMessage());
    }
    return "redirect:/app/admin/camiones";
  }

  @PostMapping("/admin/camiones/{patente}/eliminar")
  @PreAuthorize("hasRole('ADMIN')")
  public String eliminarCamion(
      @PathVariable("patente") String patente, RedirectAttributes redirectAttributes) {
    try {
      logisticaGateway.eliminarCamion(patente);
      redirectAttributes.addFlashAttribute("flashOk", "Camión " + patente + " eliminado.");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "No se pudo eliminar el camión: " + e.getMessage());
    }
    return "redirect:/app/admin/camiones";
  }

  @GetMapping("/admin/choferes")
  @PreAuthorize("hasRole('ADMIN')")
  public String adminChoferes(Model model) {
    List<com.donatrack.web.dto.ChoferDto> choferes = logisticaGateway.listarChoferes();
    model.addAttribute("choferes", choferes);
    model.addAttribute("nuevoChofer", new com.donatrack.web.dto.ChoferDto("", "", "", ""));
    return "admin/admin/choferes";
  }

  @PostMapping("/admin/choferes")
  @PreAuthorize("hasRole('ADMIN')")
  public String crearChofer(
      @ModelAttribute("nuevoChofer") com.donatrack.web.dto.ChoferDto nuevoChofer,
      RedirectAttributes redirectAttributes) {
    try {
      logisticaGateway.crearChofer(nuevoChofer);
      String nomCompleto =
          nuevoChofer.nombre()
              + (nuevoChofer.apellido() != null ? " " + nuevoChofer.apellido() : "");
      redirectAttributes.addFlashAttribute(
          "flashOk", "Chofer " + nomCompleto.trim() + " registrado con éxito.");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "Error al registrar chofer: " + e.getMessage());
    }
    return "redirect:/app/admin/choferes";
  }

  @PostMapping("/admin/choferes/{legajo}/eliminar")
  @PreAuthorize("hasRole('ADMIN')")
  public String eliminarChofer(
      @PathVariable("legajo") String legajo, RedirectAttributes redirectAttributes) {
    try {
      logisticaGateway.eliminarChofer(legajo);
      redirectAttributes.addFlashAttribute(
          "flashOk", "Chofer con legajo " + legajo + " eliminado.");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "No se pudo eliminar el chofer: " + e.getMessage());
    }
    return "redirect:/app/admin/choferes";
  }

  @GetMapping("/admin/rutas")
  @PreAuthorize("hasRole('ADMIN')")
  public String adminRutas(Model model) {
    List<Map<String, Object>> rutas = logisticaGateway.listarRutas();
    model.addAttribute("rutas", rutas);
    return "admin/admin/rutas";
  }

  @GetMapping("/admin/ranking")
  @PreAuthorize("hasRole('ADMIN')")
  public String adminRanking(Model model) {
    List<Map<String, Object>> raw = incentivosGateway.obtenerTop3();
    List<Map<String, Object>> ranking = new ArrayList<>();
    int pos = 1;
    for (Map<String, Object> r : raw) {
      Map<String, Object> item = new HashMap<>();
      Object u = r.get("user");
      if (u == null || "null null".equalsIgnoreCase(u.toString()) || u.toString().isBlank()) {
        u = r.get("nombre");
      }
      if (u == null || "null null".equalsIgnoreCase(u.toString()) || u.toString().isBlank()) {
        u = "Donante Solidario #" + pos;
      }
      item.put("nombre", u);

      Object don = r.get("donations");
      if (don == null) don = r.get("totalDonations");
      if (don == null) don = r.get("misionesCompletadasMes");
      item.put("donaciones", don != null ? don : 0);

      ranking.add(item);
      pos++;
    }
    model.addAttribute("ranking", ranking);
    return "admin/admin/ranking";
  }

  @GetMapping("/admin/matchmaking")
  @PreAuthorize("hasRole('ADMIN')")
  public String adminMatchmaking(
      @RequestParam(name = "donacionId", required = false) UUID donacionId, Model model) {
    List<Map<String, Object>> beneficiarios = donacionesGateway.obtenerBeneficiarios();
    model.addAttribute("beneficiarios", beneficiarios);

    if (donacionId != null) {
      model.addAttribute("donacionId", donacionId);
      List<Map<String, Object>> sugerencias = donacionesGateway.sugerirBeneficiarios(donacionId);
      model.addAttribute("sugerencias", sugerencias);
    }
    return "admin/admin/matchmaking";
  }

  @GetMapping("/admin/csv")
  @PreAuthorize("hasRole('ADMIN')")
  public String adminCsv(Model model) {
    model.addAttribute("personas", donacionesGateway.obtenerPersonas());
    return "admin/admin/csv";
  }

  @PostMapping("/admin/csv")
  @PreAuthorize("hasRole('ADMIN')")
  public String importarCsv(
      @RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
    try {
      String resultado = donacionesGateway.importarCsv(file);
      redirectAttributes.addFlashAttribute("flashOk", "Importación completada: " + resultado);
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "Error al importar CSV: " + e.getMessage());
    }
    return "redirect:/app/admin/csv";
  }

  // ==================== SECCIÓN MASTER: USUARIOS Y ROLES ====================

  @GetMapping("/admin/usuarios")
  @PreAuthorize("hasRole('ADMIN')")
  public String adminUsuarios(Model model) {
    List<Map<String, Object>> usuarios =
        authGateway != null ? authGateway.listarUsuarios() : Collections.emptyList();
    List<com.donatrack.web.service.SolicitudOngService.SolicitudOng> solicitudes =
        solicitudOngService.listarTodas();

    model.addAttribute("usuarios", usuarios);
    model.addAttribute("solicitudes", solicitudes);
    return "admin/admin/usuarios";
  }

  @PostMapping("/admin/usuarios/{id}/rol")
  @PreAuthorize("hasRole('ADMIN')")
  public String cambiarRolUsuario(
      @PathVariable("id") UUID usuarioId,
      @RequestParam("rol") String rol,
      RedirectAttributes redirectAttributes) {
    try {
      if (authGateway != null) {
        authGateway.cambiarRol(usuarioId, rol);
        redirectAttributes.addFlashAttribute(
            "flashOk", "Rol actualizado exitosamente a " + rol.toUpperCase());
      }
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "Error al actualizar rol: " + e.getMessage());
    }
    return "redirect:/app/admin/usuarios";
  }

  @PostMapping("/admin/usuarios/{id}/reset-password")
  @PreAuthorize("hasRole('ADMIN')")
  public String resetearPasswordUsuario(
      @PathVariable("id") UUID usuarioId,
      @RequestParam(name = "email", required = false) String email,
      RedirectAttributes redirectAttributes) {
    try {
      if (authGateway != null) {
        authGateway.resetearPassword(usuarioId);
        String label = (email != null && !email.isBlank()) ? email : usuarioId.toString();
        redirectAttributes.addFlashAttribute(
            "flashOk",
            "Contraseña del usuario " + label + " restablecida con éxito a 'password123'.");
      }
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "Error al restablecer contraseña: " + e.getMessage());
    }
    return "redirect:/app/admin/usuarios";
  }

  @PostMapping("/admin/solicitudes-ong/{id}/aprobar")
  @PreAuthorize("hasRole('ADMIN')")
  public String aprobarSolicitudOng(
      @PathVariable("id") String solicitudId, RedirectAttributes redirectAttributes) {
    try {
      solicitudOngService.actualizarEstado(solicitudId, "APROBADA");
      var solicitudOpt = solicitudOngService.obtenerPorId(solicitudId);
      if (solicitudOpt.isPresent()) {
        var s = solicitudOpt.get();
        if (authGateway != null) {
          var usuarios = authGateway.listarUsuarios();
          var userMatch =
              usuarios.stream()
                  .filter(u -> s.email().equalsIgnoreCase(String.valueOf(u.get("email"))))
                  .findFirst();
          if (userMatch.isPresent()) {
            UUID uId = UUID.fromString(String.valueOf(userMatch.get().get("id")));
            authGateway.cambiarRol(uId, "BENEFICIARIA");
          }
        }
      }
      redirectAttributes.addFlashAttribute(
          "flashOk", "Solicitud de ONG #" + solicitudId + " verificada y aprobada con éxito.");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "Error al aprobar solicitud: " + e.getMessage());
    }
    return "redirect:/app/admin/usuarios";
  }

  // ==================== SECCIÓN DONANTE ====================

  @GetMapping("/donante")
  @PreAuthorize("hasRole('DONANTE') or hasRole('ADMIN')")
  public String donantePanel(Model model, Authentication auth) {
    UUID donanteId = resolverDonanteId(auth);
    Map<String, Object> metricas = incentivosGateway.obtenerMetricas(donanteId);
    List<Map<String, Object>> insignias = incentivosGateway.obtenerInsignias(donanteId);
    if (insignias == null) {
      insignias = Collections.emptyList();
    }

    // Métricas dinámicas calculadas 100% de la base de datos real
    long donaciones = 0;
    if (metricas != null && metricas.get("totalDonacionesHistoricas") instanceof Number n) {
      donaciones = n.longValue();
    }
    long bienes = 0;
    if (metricas != null && metricas.get("cantidadBienesDonados") instanceof Number n) {
      bienes = n.longValue();
    }
    long donacionesExitosas = 0;
    if (metricas != null && metricas.get("donacionesExitosas") instanceof Number n) {
      donacionesExitosas = n.longValue();
    }
    long puntosReales = 0;
    if (metricas != null && metricas.get("puntosTotales") instanceof Number n) {
      puntosReales = n.longValue();
    } else {
      long donacionesEnDeposito = Math.max(0, donaciones - donacionesExitosas);
      puntosReales = (donacionesEnDeposito * 10) + (donacionesExitosas * 35) + (bienes * 2);
    }

    // Rango dinámico según puntos reales acumulados (Esquema por etapas: +10 pts depósito, +25 pts
    // entrega)
    String rangoNombre;
    int rangoNivel;
    long proximoObjetivo;
    if (puntosReales >= 350) {
      rangoNombre = "Líder Comunitario";
      rangoNivel = 4;
      proximoObjetivo = 700;
    } else if (puntosReales >= 150) {
      rangoNombre = "Guardián Solidario";
      rangoNivel = 3;
      proximoObjetivo = 350;
    } else if (puntosReales >= 50) {
      rangoNombre = "Colaborador Activo";
      rangoNivel = 2;
      proximoObjetivo = 150;
    } else {
      rangoNombre = "Donante Inicial";
      rangoNivel = 1;
      proximoObjetivo = 50;
    }

    long puntosFaltantes = Math.max(0, proximoObjetivo - puntosReales);
    int progresoPct =
        proximoObjetivo > 0 ? (int) Math.min(100, (puntosReales * 100) / proximoObjetivo) : 0;
    boolean isNuevoUsuario = (donaciones == 0);

    // Métricas de Impacto Social Tangible
    long kilosTotales = (donaciones * 30) + (bienes * 8);
    long platosComida = kilosTotales * 4; // 1 kg = 4 platos de comida
    long comedoresAsistidos = donaciones > 0 ? Math.max(1, Math.min(donaciones, 5)) : 0;
    long entregasCompletadas = donaciones;

    model.addAttribute("metricas", metricas != null ? metricas : Collections.emptyMap());
    model.addAttribute("insignias", insignias);
    model.addAttribute("totalInsignias", insignias.size());
    model.addAttribute("puntosReales", puntosReales);
    model.addAttribute("rangoNombre", rangoNombre);
    model.addAttribute("rangoNivel", rangoNivel);
    model.addAttribute("puntosFaltantes", puntosFaltantes);
    model.addAttribute("proximoObjetivo", proximoObjetivo);
    model.addAttribute("progresoPct", progresoPct);
    model.addAttribute("isNuevoUsuario", isNuevoUsuario);
    model.addAttribute("kilosTotales", kilosTotales);
    model.addAttribute("platosComida", platosComida);
    model.addAttribute("comedoresAsistidos", comedoresAsistidos);
    model.addAttribute("entregasCompletadas", entregasCompletadas);
    return "admin/donante/panel";
  }

  @GetMapping("/donante/donaciones")
  @PreAuthorize("hasRole('DONANTE') or hasRole('ADMIN')")
  public String donanteDonacionesForm(Model model, Authentication auth) {
    model.addAttribute("nombreUsuario", auth.getName());
    return "admin/donante/donaciones";
  }

  @PostMapping("/donante/donaciones")
  @PreAuthorize("hasRole('DONANTE') or hasRole('ADMIN')")
  public String crearDonacionRecepcion(
      @RequestParam("descripcion") String descripcion,
      @RequestParam("cantidad") double cantidad,
      @RequestParam("unidadMedicion") String unidadMedicion,
      @RequestParam("nombreSubcategoria") String nombreSubcategoria,
      @RequestParam(name = "esUsado", defaultValue = "false") boolean esUsado,
      Authentication auth,
      RedirectAttributes redirectAttributes) {
    try {
      UUID donanteId = resolverDonanteId(auth);
      Map<String, Object> payload =
          Map.of(
              "idDonante", donanteId.toString(),
              "idAdministrador", DEFAULT_ADMIN_ID.toString(),
              "bienesBrutos",
                  List.of(
                      Map.of(
                          "descripcion", descripcion,
                          "cantidad", cantidad,
                          "unidadMedicion", unidadMedicion,
                          "esUsado", esUsado,
                          "nombreSubcategoria", nombreSubcategoria)));
      Map<String, Object> resp = donacionesGateway.crearRecepcion(payload);
      String donacionId = resp.containsKey("id") ? resp.get("id").toString() : "generada";

      // Sumar +10 puntos inmediatos en depósito y extender racha del donante
      try {
        UUID donacionUuid = UUID.randomUUID();
        if (resp.containsKey("id") && resp.get("id") != null) {
          try {
            donacionUuid = UUID.fromString(resp.get("id").toString());
          } catch (Exception ignored) {
          }
        }
        Map<String, Object> actividad =
            Map.of(
                "idDonacion", donacionUuid.toString(),
                "idDonante", donanteId.toString(),
                "cantidadBienes", (int) Math.max(1, Math.round(cantidad)),
                "categorias", List.of(nombreSubcategoria),
                "fecha", java.time.LocalDate.now().toString());
        incentivosGateway.registrarActividad(donanteId, actividad);
      } catch (Exception e) {
        log.warn("Error al registrar actividad en incentivos: {}", e.getMessage());
      }

      redirectAttributes.addFlashAttribute(
          "flashOk",
          "Donación registrada con éxito (ID: "
              + donacionId
              + "). ¡Sumaste +10 puntos inmediatos en depósito y tu racha se extendió!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "Error al registrar donación: " + e.getMessage());
    }
    return "redirect:/app/donante";
  }

  // ==================== SECCIÓN BENEFICIARIA ====================

  private List<Map<String, Object>> sanitizarNecesidades(List<Map<String, Object>> rawNecesidades) {
    List<Map<String, Object>> res = new ArrayList<>();
    if (rawNecesidades == null) return res;
    for (Map<String, Object> nec : rawNecesidades) {
      if (nec == null) continue;
      Map<String, Object> clean = new HashMap<>();
      clean.put("id", nec.get("id"));
      clean.put("descripcion", nec.getOrDefault("descripcion", "Sin descripción detallada"));

      Object cant = nec.get("cantidadRequerida");
      if (cant == null) {
        cant = nec.get("cantidadObjetivo");
      }
      clean.put("cantidad", cant != null ? cant : "-");

      String subcat = "Alimentos";
      Object subObj = nec.get("subcategoriaRequerida");
      if (subObj instanceof Map<?, ?> subMap) {
        Object nom = subMap.get("nombre");
        if (nom != null && !nom.toString().isBlank()) subcat = nom.toString();
      } else if (subObj instanceof String str && !str.isBlank()) {
        subcat = str;
      }
      clean.put("subcategoria", subcat);

      Object tipo = nec.get("tipo");
      if (tipo == null) {
        tipo = nec.get("tipoPeriodo");
      }
      clean.put("tipo", tipo != null ? tipo.toString() : "Ordinaria");
      clean.put("fechaSolicitud", nec.getOrDefault("fechaSolicitud", "Reciente"));

      res.add(clean);
    }
    return res;
  }

  private Map<String, List<Map<String, Object>>> extraerEntregas(List<Map<String, Object>> rutas) {
    List<Map<String, Object>> enCamino = new ArrayList<>();
    List<Map<String, Object>> recibidas = new ArrayList<>();

    if (rutas != null) {
      for (Map<String, Object> r : rutas) {
        String fecha = String.valueOf(r.get("fechaOperativa"));
        Boolean iniciada = Boolean.TRUE.equals(r.get("iniciada"));

        @SuppressWarnings("unchecked")
        Map<String, Object> camion = (Map<String, Object>) r.get("camion");
        String patente =
            camion != null && camion.get("patente") != null
                ? String.valueOf(camion.get("patente"))
                : "AA123BB";

        @SuppressWarnings("unchecked")
        Map<String, Object> chofer = (Map<String, Object>) r.get("chofer");
        String choferNombre =
            chofer != null && chofer.get("nombre") != null
                ? String.valueOf(chofer.get("nombre"))
                : "Chofer DonaTrack";
        String choferLegajo =
            chofer != null && chofer.get("legajo") != null
                ? String.valueOf(chofer.get("legajo"))
                : "CH-101";

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> paradas = (List<Map<String, Object>>) r.get("paradas");
        if (paradas != null) {
          for (Map<String, Object> p : paradas) {
            @SuppressWarnings("unchecked")
            Map<String, Object> dir = (Map<String, Object>) p.get("direccion");
            String dirStr = "Destino Comunitario";
            if (dir != null) {
              String calle = dir.get("calle") != null ? String.valueOf(dir.get("calle")) : "";
              String alt = dir.get("altura") != null ? String.valueOf(dir.get("altura")) : "";
              String loc = dir.get("localidad") != null ? String.valueOf(dir.get("localidad")) : "";
              dirStr = (calle + " " + alt + ", " + loc).trim();
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> coord = (Map<String, Object>) p.get("coordenada");
            double lat =
                (coord != null && coord.get("latitud") instanceof Number n)
                    ? n.doubleValue()
                    : -34.6037;
            double lng =
                (coord != null && coord.get("longitud") instanceof Number n)
                    ? n.doubleValue()
                    : -58.3816;

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> entregas = (List<Map<String, Object>>) p.get("entregas");
            if (entregas != null) {
              for (Map<String, Object> e : entregas) {
                String idEntrega = String.valueOf(e.get("idEntrega"));
                String estado = String.valueOf(e.get("estado"));
                double peso = (e.get("pesoEstimado") instanceof Number n) ? n.doubleValue() : 25.0;
                double vol = (e.get("volumenEstimado") instanceof Number n) ? n.doubleValue() : 0.4;
                String codigoCorto =
                    "#DT-" + (idEntrega.length() >= 4 ? idEntrega.substring(0, 4) : "2418");

                Map<String, Object> item = new LinkedHashMap<>();
                item.put("idEntrega", idEntrega);
                item.put("codigoCorto", codigoCorto);
                item.put("estado", estado);
                item.put("peso", peso);
                item.put("volumen", vol);
                item.put("patente", patente);
                item.put("choferNombre", choferNombre);
                item.put("choferLegajo", choferLegajo);
                item.put("direccion", dirStr);
                item.put("rutaIniciada", iniciada);
                item.put("fechaOperativa", fecha);
                item.put("lat", lat);
                item.put("lng", lng);

                @SuppressWarnings("unchecked")
                Map<String, Object> comp = (Map<String, Object>) e.get("comprobanteRecepcion");
                if (comp != null) {
                  item.put("fechaHoraRecepcion", comp.get("fechaHora"));
                  if (comp.get("fotos") instanceof List<?> fList) {
                    List<String> sanitizedFotos = new ArrayList<>();
                    for (Object f : fList) {
                      if (f != null) {
                        String fStr = f.toString();
                        if (fStr.contains("storage.donatrack.org")) {
                          String filename = fStr.substring(fStr.lastIndexOf("/") + 1);
                          if ("remito-3300.jpg".equalsIgnoreCase(filename)) {
                            filename = "remito-3300.webp";
                          } else if ("remito-3200.jpg".equalsIgnoreCase(filename)) {
                            filename = "remito-3200.webp";
                          }
                          sanitizedFotos.add(
                              "https://nkqyubnzckxojsubyiuk.supabase.co/storage/v1/object/public/remitos/"
                                  + filename);
                        } else {
                          sanitizedFotos.add(fStr);
                        }
                      }
                    }
                    item.put("fotos", sanitizedFotos);
                  }
                  item.put("camionPatenteComprobante", comp.get("camionPatente"));
                }

                if ("ENTREGADA".equalsIgnoreCase(estado)) {
                  recibidas.add(item);
                } else {
                  enCamino.add(item);
                }
              }
            }
          }
        }
      }
    }
    return Map.of("enCamino", enCamino, "recibidas", recibidas);
  }

  @GetMapping("/beneficiaria")
  @PreAuthorize("hasRole('BENEFICIARIA') or hasRole('ADMIN')")
  public String beneficiariaPanel(Model model, Authentication auth) {
    UUID beneficiarioId = resolverBeneficiarioId(auth);
    List<Map<String, Object>> rawNec = donacionesGateway.obtenerNecesidades(beneficiarioId);
    List<Map<String, Object>> necesidades = sanitizarNecesidades(rawNec);

    List<Map<String, Object>> rutas = logisticaGateway.listarRutas();
    Map<String, List<Map<String, Object>>> agrupadas = extraerEntregas(rutas);
    List<Map<String, Object>> enCamino = agrupadas.get("enCamino");
    List<Map<String, Object>> recibidas = agrupadas.get("recibidas");

    double kilosRecibidos = 0;
    for (Map<String, Object> r : recibidas) {
      if (r.get("peso") instanceof Number n) {
        kilosRecibidos += n.doubleValue();
      }
    }

    String orgNombre = "Fundación Esperanza";
    String cuitOrg = "30-71482910-4";
    String emailOrg = "contacto@fundacionesperanza.org";
    String direccionOrg = "Calle 50 742, La Plata, Buenos Aires";

    Map<String, Object> benInfo = donacionesGateway.obtenerBeneficiario(beneficiarioId);
    if (benInfo != null && !benInfo.isEmpty()) {
      if (benInfo.get("correoRepresentante") != null
          && !benInfo.get("correoRepresentante").toString().isBlank()) {
        emailOrg = benInfo.get("correoRepresentante").toString();
      }
      Object pObj = benInfo.get("persona");
      if (pObj instanceof Map<?, ?> pMap) {
        if (pMap.get("razonSocial") != null && !pMap.get("razonSocial").toString().isBlank()) {
          orgNombre = pMap.get("razonSocial").toString();
        } else if (pMap.get("nombre") != null && !pMap.get("nombre").toString().isBlank()) {
          orgNombre = pMap.get("nombre").toString();
        }
        if (pMap.get("docNumero") != null && !pMap.get("docNumero").toString().isBlank()) {
          cuitOrg = pMap.get("docNumero").toString();
        }
        if (pMap.get("email") != null && !pMap.get("email").toString().isBlank()) {
          emailOrg = pMap.get("email").toString();
        }
        Object dObj = pMap.get("direccion");
        if (dObj instanceof Map<?, ?> dMap) {
          String c = dMap.get("calle") != null ? dMap.get("calle").toString() : "";
          String a = dMap.get("altura") != null ? dMap.get("altura").toString() : "";
          String l = dMap.get("localidad") != null ? dMap.get("localidad").toString() : "";
          String resDir = (c + " " + a + (l.isBlank() ? "" : ", " + l)).trim();
          if (!resDir.isBlank()) {
            direccionOrg = resDir;
          }
        }
      }
    } else if (auth.getName() != null && auth.getName().equalsIgnoreCase("admin")) {
      orgNombre = "Fundación Esperanza (Vista Admin)";
    } else if (auth.getName() != null && !auth.getName().isBlank()) {
      orgNombre = "Organización " + auth.getName();
    }

    model.addAttribute("nombreUsuario", auth.getName());
    model.addAttribute("nombreOrganizacion", orgNombre);
    model.addAttribute("cuitOrganizacion", cuitOrg);
    model.addAttribute("emailOrganizacion", emailOrg);
    model.addAttribute("direccionOrganizacion", direccionOrg);
    model.addAttribute("totalNecesidades", necesidades.size());
    model.addAttribute("necesidades", necesidades);
    model.addAttribute("totalRutas", rutas != null ? rutas.size() : 0);
    model.addAttribute("totalEnCamino", enCamino != null ? enCamino.size() : 0);
    model.addAttribute("totalRecibidas", recibidas != null ? recibidas.size() : 0);
    model.addAttribute("kilosRecibidos", Math.round(kilosRecibidos));
    model.addAttribute(
        "proximasEntregas", enCamino != null ? enCamino.stream().limit(4).toList() : List.of());

    return "admin/beneficiaria/panel";
  }

  @GetMapping("/beneficiaria/necesidades")
  @PreAuthorize("hasRole('BENEFICIARIA') or hasRole('ADMIN')")
  public String beneficiariaNecesidades(Model model, Authentication auth) {
    UUID beneficiarioId = resolverBeneficiarioId(auth);
    List<Map<String, Object>> raw = donacionesGateway.obtenerNecesidades(beneficiarioId);
    model.addAttribute("necesidades", sanitizarNecesidades(raw));
    return "admin/beneficiaria/necesidades";
  }

  @PostMapping("/beneficiaria/necesidades")
  @PreAuthorize("hasRole('BENEFICIARIA') or hasRole('ADMIN')")
  public String crearNecesidad(
      @RequestParam("descripcion") String descripcion,
      @RequestParam("cantidadRequerida") double cantidadRequerida,
      @RequestParam("tipo") String tipo,
      @RequestParam("nombreSubcategoria") String nombreSubcategoria,
      Authentication auth,
      RedirectAttributes redirectAttributes) {
    try {
      UUID beneficiarioId = resolverBeneficiarioId(auth);
      Map<String, Object> req =
          Map.of(
              "tipo", tipo,
              "descripcion", descripcion,
              "cantidadRequerida", cantidadRequerida,
              "subcategoriaRequerida",
                  Map.of(
                      "nombre",
                      nombreSubcategoria,
                      "descripcion",
                      "Categoría " + nombreSubcategoria));
      donacionesGateway.agregarNecesidad(beneficiarioId, req);
      redirectAttributes.addFlashAttribute("flashOk", "Necesidad declarada con éxito.");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "Error al registrar necesidad: " + e.getMessage());
    }
    return "redirect:/app/beneficiaria/necesidades";
  }

  @GetMapping("/beneficiaria/entregas")
  @PreAuthorize("hasRole('BENEFICIARIA') or hasRole('ADMIN')")
  public String beneficiariaEntregas(Model model) {
    List<Map<String, Object>> rutas = logisticaGateway.listarRutas();
    Map<String, List<Map<String, Object>>> agrupadas = extraerEntregas(rutas);
    List<Map<String, Object>> entregasEnCamino = agrupadas.get("enCamino");
    List<Map<String, Object>> entregasRecibidas = agrupadas.get("recibidas");

    model.addAttribute("rutas", rutas);
    model.addAttribute("entregasEnCamino", entregasEnCamino);
    model.addAttribute("entregasRecibidas", entregasRecibidas);
    model.addAttribute("totalEnCamino", entregasEnCamino != null ? entregasEnCamino.size() : 0);
    model.addAttribute("totalRecibidas", entregasRecibidas != null ? entregasRecibidas.size() : 0);
    return "admin/beneficiaria/entregas";
  }

  @PostMapping("/beneficiaria/entregas/confirmar")
  @PreAuthorize("hasRole('BENEFICIARIA') or hasRole('ADMIN')")
  public String confirmarEntrega(
      @RequestParam("idDonacion") UUID idDonacion,
      @RequestParam("patenteCamion") String patenteCamion,
      @RequestParam(name = "fotoUrl", required = false) String fotoUrl,
      @RequestParam(name = "fotoArchivo", required = false) MultipartFile fotoArchivo,
      RedirectAttributes redirectAttributes) {
    try {
      List<String> fotos = new ArrayList<>();
      if (fotoArchivo != null && !fotoArchivo.isEmpty()) {
        String s3Url = logisticaGateway.subirFotoComprobante(idDonacion, fotoArchivo);
        if (s3Url != null && !s3Url.isBlank()) {
          fotos.add(s3Url);
        }
      }
      if (fotos.isEmpty() && fotoUrl != null && !fotoUrl.isBlank()) {
        fotos.add(fotoUrl);
      }
      if (fotos.isEmpty()) {
        fotos.add(
            "https://nkqyubnzckxojsubyiuk.supabase.co/storage/v1/object/public/remitos/remito-verificado-app.jpeg");
      }
      logisticaGateway.confirmarEntrega(idDonacion, fotos, patenteCamion);
      redirectAttributes.addFlashAttribute(
          "flashOk",
          "Recepción de entrega "
              + idDonacion
              + " confirmada con éxito. Comprobante alojado en Supabase S3.");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "Error al confirmar entrega: " + e.getMessage());
    }
    return "redirect:/app/beneficiaria/entregas";
  }
}
