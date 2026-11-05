package com.donatrack.web.controller;

import com.donatrack.web.dto.CamionDto;
import com.donatrack.web.gateway.DonacionesGateway;
import com.donatrack.web.gateway.IncentivosGateway;
import com.donatrack.web.gateway.LogisticaGateway;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
    this(logisticaGateway, incentivosGateway, donacionesGateway, null, new com.donatrack.web.service.SolicitudOngService());
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
      if (auth instanceof JwtAuthenticationToken jwtAuth) {
        Jwt token = jwtAuth.getToken();
        if (token.getClaimAsString("name") != null && !token.getClaimAsString("name").isBlank()) {
          nombreCompleto = token.getClaimAsString("name");
        }
        if (token.getClaimAsString("username") != null) {
          username = token.getClaimAsString("username");
        }
        usuarioId = token.getSubject();
      }
      model.addAttribute("nombreCompleto", nombreCompleto);
      model.addAttribute("nombreUsuario", username);
      model.addAttribute("usuarioId", usuarioId);
    }
  }

  private UUID resolverDonanteId(Authentication auth) {
    if (auth instanceof JwtAuthenticationToken jwt) {
      String pId = jwt.getToken().getClaimAsString("personaId");
      if (pId != null && !pId.isBlank()) {
        try {
          return UUID.fromString(pId);
        } catch (IllegalArgumentException ignored) {}
      }
      String sub = jwt.getToken().getSubject();
      if (sub != null && !sub.isBlank()) {
        try {
          return UUID.fromString(sub);
        } catch (IllegalArgumentException ignored) {}
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
        } catch (IllegalArgumentException ignored) {}
      }
    }
    return fallback;
  }

  @GetMapping
  public String dashboardRoot(Authentication auth) {
    if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
      return "redirect:/app/admin";
    } else if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DONANTE"))) {
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
      @ModelAttribute("nuevoCamion") CamionDto nuevoCamion,
      RedirectAttributes redirectAttributes) {
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
      @PathVariable("patente") String patente,
      RedirectAttributes redirectAttributes) {
    try {
      logisticaGateway.eliminarCamion(patente);
      redirectAttributes.addFlashAttribute("flashOk", "Camión " + patente + " eliminado.");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "No se pudo eliminar el camión: " + e.getMessage());
    }
    return "redirect:/app/admin/camiones";
  }

  @GetMapping("/admin/ranking")
  @PreAuthorize("hasRole('ADMIN')")
  public String adminRanking(Model model) {
    List<Map<String, Object>> ranking = incentivosGateway.obtenerTop3();
    model.addAttribute("ranking", ranking);
    return "admin/admin/ranking";
  }

  @GetMapping("/admin/matchmaking")
  @PreAuthorize("hasRole('ADMIN')")
  public String adminMatchmaking(
      @RequestParam(name = "donacionId", required = false) UUID donacionId,
      Model model) {
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
      @RequestParam("file") MultipartFile file,
      RedirectAttributes redirectAttributes) {
    try {
      String resultado = donacionesGateway.importarCsv(file);
      redirectAttributes.addFlashAttribute("flashOk", "Importación completada: " + resultado);
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("flashError", "Error al importar CSV: " + e.getMessage());
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

  @PostMapping("/admin/solicitudes-ong/{id}/aprobar")
  @PreAuthorize("hasRole('ADMIN')")
  public String aprobarSolicitudOng(
      @PathVariable("id") String solicitudId,
      RedirectAttributes redirectAttributes) {
    try {
      solicitudOngService.actualizarEstado(solicitudId, "APROBADA");
      var solicitudOpt = solicitudOngService.obtenerPorId(solicitudId);
      if (solicitudOpt.isPresent()) {
        var s = solicitudOpt.get();
        if (authGateway != null) {
          var usuarios = authGateway.listarUsuarios();
          var userMatch = usuarios.stream()
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
    long puntosReales = (donaciones * 500) + (bienes * 75);

    // Rango dinámico según puntos reales acumulados
    String rangoNombre;
    int rangoNivel;
    long proximoObjetivo;
    if (puntosReales >= 5000) {
      rangoNombre = "Trailblazer Ranger";
      rangoNivel = 4;
      proximoObjetivo = 10000;
    } else if (puntosReales >= 1500) {
      rangoNombre = "Guardián Solidario";
      rangoNivel = 3;
      proximoObjetivo = 5000;
    } else if (puntosReales >= 500) {
      rangoNombre = "Colaborador Activo";
      rangoNivel = 2;
      proximoObjetivo = 1500;
    } else {
      rangoNombre = "Donante Inicial";
      rangoNivel = 1;
      proximoObjetivo = 500;
    }

    long puntosFaltantes = Math.max(0, proximoObjetivo - puntosReales);
    int progresoPct = proximoObjetivo > 0 ? (int) Math.min(100, (puntosReales * 100) / proximoObjetivo) : 0;
    boolean isNuevoUsuario = (donaciones == 0);

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
      Map<String, Object> payload = Map.of(
          "idDonante", donanteId.toString(),
          "idAdministrador", DEFAULT_ADMIN_ID.toString(),
          "bienesBrutos", List.of(Map.of(
              "descripcion", descripcion,
              "cantidad", cantidad,
              "unidadMedicion", unidadMedicion,
              "esUsado", esUsado,
              "nombreSubcategoria", nombreSubcategoria
          ))
      );
      Map<String, Object> resp = donacionesGateway.crearRecepcion(payload);
      String donacionId = resp.containsKey("id") ? resp.get("id").toString() : "generada";
      redirectAttributes.addFlashAttribute("flashOk", "Donación registrada con éxito (ID: " + donacionId + ").");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("flashError", "Error al registrar donación: " + e.getMessage());
    }
    return "redirect:/app/donante";
  }

  // ==================== SECCIÓN BENEFICIARIA ====================

  @GetMapping("/beneficiaria")
  @PreAuthorize("hasRole('BENEFICIARIA') or hasRole('ADMIN')")
  public String beneficiariaPanel(Model model, Authentication auth) {
    UUID beneficiarioId = resolverBeneficiarioId(auth);
    model.addAttribute("nombreUsuario", auth.getName());
    model.addAttribute("totalNecesidades", donacionesGateway.obtenerNecesidades(beneficiarioId).size());
    model.addAttribute("totalRutas", logisticaGateway.listarRutas().size());
    return "admin/beneficiaria/panel";
  }

  @GetMapping("/beneficiaria/necesidades")
  @PreAuthorize("hasRole('BENEFICIARIA') or hasRole('ADMIN')")
  public String beneficiariaNecesidades(Model model, Authentication auth) {
    UUID beneficiarioId = resolverBeneficiarioId(auth);
    model.addAttribute("necesidades", donacionesGateway.obtenerNecesidades(beneficiarioId));
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
      Map<String, Object> req = Map.of(
          "tipo", tipo,
          "descripcion", descripcion,
          "cantidadRequerida", cantidadRequerida,
          "subcategoriaRequerida", Map.of(
              "nombre", nombreSubcategoria,
              "descripcion", "Categoría " + nombreSubcategoria
          )
      );
      donacionesGateway.agregarNecesidad(beneficiarioId, req);
      redirectAttributes.addFlashAttribute("flashOk", "Necesidad declarada con éxito.");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("flashError", "Error al registrar necesidad: " + e.getMessage());
    }
    return "redirect:/app/beneficiaria/necesidades";
  }

  @GetMapping("/beneficiaria/entregas")
  @PreAuthorize("hasRole('BENEFICIARIA') or hasRole('ADMIN')")
  public String beneficiariaEntregas(Model model) {
    model.addAttribute("rutas", logisticaGateway.listarRutas());
    return "admin/beneficiaria/entregas";
  }

  @PostMapping("/beneficiaria/entregas/confirmar")
  @PreAuthorize("hasRole('BENEFICIARIA') or hasRole('ADMIN')")
  public String confirmarEntrega(
      @RequestParam("idDonacion") UUID idDonacion,
      @RequestParam("patenteCamion") String patenteCamion,
      @RequestParam(name = "fotoUrl", required = false) String fotoUrl,
      RedirectAttributes redirectAttributes) {
    try {
      List<String> fotos = (fotoUrl != null && !fotoUrl.isBlank()) ? List.of(fotoUrl) : List.of();
      logisticaGateway.confirmarEntrega(idDonacion, fotos, patenteCamion);
      redirectAttributes.addFlashAttribute("flashOk", "Recepción de entrega " + idDonacion + " confirmada con éxito.");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("flashError", "Error al confirmar entrega: " + e.getMessage());
    }
    return "redirect:/app/beneficiaria/entregas";
  }
}
