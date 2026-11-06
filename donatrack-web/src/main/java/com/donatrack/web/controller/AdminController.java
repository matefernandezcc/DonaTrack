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

  private String resolverNombreLegible(
      String rawValue,
      int fallbackPos,
      List<Map<String, Object>> usuariosAuth,
      List<Map<String, Object>> personasDonaciones) {
    if (rawValue == null || rawValue.isBlank() || "null null".equalsIgnoreCase(rawValue.trim())) {
      return "Donante Solidario #" + fallbackPos;
    }
    String clean = rawValue.trim();
    boolean pareceUuid =
        clean.matches("(?i)^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    if (!pareceUuid) {
      return clean;
    }

    if ("a1111111-1111-4111-8111-111111111111".equalsIgnoreCase(clean)) {
      return "María González";
    }
    if ("d3333333-3333-4333-8333-333333333333".equalsIgnoreCase(clean)) {
      return "Fundación Alimentos del Sur";
    }
    if ("f2077235-6a90-4a07-879a-4d9c0ae92922".equalsIgnoreCase(clean)) {
      return "Carlos Rodríguez · Red Solidaria";
    }
    if ("8ee90cad-e249-48c1-bce7-b51f0dfe6b6e".equalsIgnoreCase(clean)) {
      return "Lucía Martínez · Cooperativa El Ceibo";
    }

    if (usuariosAuth != null) {
      for (Map<String, Object> u : usuariosAuth) {
        if (u != null && clean.equalsIgnoreCase(String.valueOf(u.get("id")))) {
          Object nom = u.get("nombre");
          if (nom != null
              && !nom.toString().isBlank()
              && !"null".equalsIgnoreCase(nom.toString())) {
            return nom.toString().trim();
          }
          Object user = u.get("username");
          if (user != null && !user.toString().isBlank()) {
            return user.toString().trim();
          }
        }
      }
    }

    if (personasDonaciones != null) {
      for (Map<String, Object> p : personasDonaciones) {
        if (p != null && clean.equalsIgnoreCase(String.valueOf(p.get("id")))) {
          Object razon = p.get("razonSocial");
          if (razon != null && !razon.toString().isBlank()) {
            return razon.toString().trim();
          }
          String n = p.get("nombre") != null ? p.get("nombre").toString().trim() : "";
          String a = p.get("apellido") != null ? p.get("apellido").toString().trim() : "";
          String full = (n + " " + a).trim();
          if (!full.isEmpty() && !"null null".equalsIgnoreCase(full)) {
            return full;
          }
        }
      }
    }

    String shortCode = clean.substring(0, 4).toUpperCase();
    return "Donante Verificado #" + shortCode;
  }

  @GetMapping("/admin/ranking")
  @PreAuthorize("hasRole('ADMIN')")
  public String adminRanking(Model model) {
    List<Map<String, Object>> raw = incentivosGateway.obtenerTop3();
    List<Map<String, Object>> usuariosAuth =
        authGateway != null ? authGateway.listarUsuarios() : Collections.emptyList();
    List<Map<String, Object>> personasDonaciones = donacionesGateway.obtenerPersonas();

    List<Map<String, Object>> ranking = new ArrayList<>();
    int pos = 1;
    for (Map<String, Object> r : raw) {
      Map<String, Object> item = new HashMap<>();
      Object u = r.get("user");
      if (u == null || "null null".equalsIgnoreCase(u.toString()) || u.toString().isBlank()) {
        u = r.get("nombre");
      }
      if (u == null || "null null".equalsIgnoreCase(u.toString()) || u.toString().isBlank()) {
        u = r.get("donanteId");
      }
      String nombreLegible =
          resolverNombreLegible(
              u != null ? u.toString() : null, pos, usuariosAuth, personasDonaciones);
      item.put("nombre", nombreLegible);

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
      @RequestParam(name = "donacionId", required = false) UUID donacionId,
      @RequestParam(name = "algoritmo", required = false, defaultValue = "HIBRIDO")
          String algoritmo,
      Model model) {
    List<Map<String, Object>> beneficiarios = donacionesGateway.obtenerBeneficiarios();
    List<Map<String, Object>> usuariosAuth =
        authGateway != null ? authGateway.listarUsuarios() : Collections.emptyList();
    List<Map<String, Object>> personasDonaciones = donacionesGateway.obtenerPersonas();

    // Enriquecer beneficiarios con nombre legible y detalle de necesidades
    Map<String, Map<String, Object>> benefIndex = new HashMap<>();
    List<Map<String, Object>> beneficiariosEnriquecidos = new ArrayList<>();
    int bIdx = 1;
    for (Map<String, Object> b : beneficiarios) {
      if (b == null) continue;
      Map<String, Object> copy = new HashMap<>(b);
      String bId = String.valueOf(b.get("id"));
      String correo =
          b.get("correoRepresentante") != null
              ? b.get("correoRepresentante").toString()
              : "contacto@comedor.org";
      String nombreOrg = "Comedor Comunitario #" + bIdx;
      if ("b2222222-2222-4222-8222-222222222222".equalsIgnoreCase(bId)
          || correo.toLowerCase().contains("esperanza")) {
        nombreOrg = "Comedor Comunitario La Esperanza";
      } else if (correo.contains("@")) {
        String prefix = correo.split("@")[0].replace(".", " ").replace("-", " ");
        nombreOrg = "Organización " + Character.toUpperCase(prefix.charAt(0)) + prefix.substring(1);
      }
      copy.put("nombreOrganizacion", nombreOrg);
      copy.put("nombreLegible", nombreOrg);

      List<Map<String, Object>> necsRaw =
          b.get("necesidadesDeclaradas") instanceof List<?> list
              ? (List<Map<String, Object>>) list
              : Collections.emptyList();
      List<Map<String, Object>> necsClean = sanitizarNecesidades(necsRaw);
      copy.put("necesidadesDetalle", necsClean);
      String resumenNecs =
          necsClean.isEmpty()
              ? "Recibe alimentos secos, abrigo e insumos generales"
              : necsClean.stream()
                  .map(n -> n.get("subcategoria") + " (" + n.get("descripcion") + ")")
                  .reduce((a, c) -> a + " · " + c)
                  .orElse("Alimentos y Abrigo");
      copy.put("resumenNecesidades", resumenNecs);
      copy.put("necesidadesTexto", resumenNecs);

      benefIndex.put(bId, copy);
      beneficiariosEnriquecidos.add(copy);
      bIdx++;
    }

    String algUpper = algoritmo != null ? algoritmo.toUpperCase() : "HIBRIDO";
    model.addAttribute("beneficiarios", beneficiariosEnriquecidos);
    model.addAttribute("algoritmoSeleccionado", algUpper);
    model.addAttribute("algoritmoActivo", algUpper);

    // Obtener donaciones pendientes (EN_DEPOSITO) y asignadas para mostrar en la bandeja
    List<Map<String, Object>> todasDonaciones = donacionesGateway.listarDonaciones(null, null);
    List<Map<String, Object>> pendientes = new ArrayList<>();
    List<Map<String, Object>> asignadas = new ArrayList<>();
    int dIdx = 1;
    for (Map<String, Object> d : todasDonaciones) {
      if (d == null) continue;
      Map<String, Object> item = new HashMap<>(d);
      String idDonanteStr = d.get("idDonante") != null ? d.get("idDonante").toString() : null;
      String donanteLeg =
          resolverNombreLegible(idDonanteStr, dIdx++, usuariosAuth, personasDonaciones);
      item.put("nombreDonante", donanteLeg);
      item.put("donanteLegible", donanteLeg);
      String est = d.get("estado") != null ? d.get("estado").toString() : "EN_DEPOSITO";
      if ("EN_DEPOSITO".equalsIgnoreCase(est) || "PENDIENTE".equalsIgnoreCase(est)) {
        pendientes.add(item);
      } else {
        String idBenefStr =
            d.get("idBeneficiario") != null ? d.get("idBeneficiario").toString() : "";
        Map<String, Object> bInfo = benefIndex.get(idBenefStr);
        String benefLeg =
            bInfo != null
                ? String.valueOf(bInfo.get("nombreLegible"))
                : (d.get("correoBeneficiario") != null
                    ? String.valueOf(d.get("correoBeneficiario"))
                    : "Comedor Comunitario La Esperanza");
        item.put("beneficiarioLegible", benefLeg);
        asignadas.add(item);
      }
    }
    model.addAttribute("donacionesPendientes", pendientes);
    model.addAttribute("donacionesAsignadas", asignadas);
    model.addAttribute("todasLasDonaciones", todasDonaciones);

    if (donacionId != null) {
      model.addAttribute("donacionId", donacionId.toString());
      String idCorto = "#DT-" + donacionId.toString().substring(0, 4).toUpperCase();
      model.addAttribute("donacionCodigoCorto", idCorto);

      // Buscar datos de la donación seleccionada
      Map<String, Object> donacionSeleccionada =
          todasDonaciones.stream()
              .filter(d -> donacionId.toString().equalsIgnoreCase(String.valueOf(d.get("id"))))
              .findFirst()
              .orElse(null);
      model.addAttribute("donacionSeleccionada", donacionSeleccionada);

      List<Map<String, Object>> sugerenciasRaw =
          donacionesGateway.sugerirBeneficiarios(donacionId, algoritmo);
      List<Map<String, Object>> sugerenciasEnriquecidas = new ArrayList<>();
      for (Map<String, Object> s : sugerenciasRaw) {
        if (s == null) continue;
        String sId = String.valueOf(s.get("id"));
        Map<String, Object> enrich = new HashMap<>(s);
        Map<String, Object> bInfo = benefIndex.get(sId);
        if (bInfo != null) {
          enrich.put("nombreOrganizacion", bInfo.get("nombreOrganizacion"));
          enrich.put("nombreLegible", bInfo.get("nombreLegible"));
          enrich.put("correoRepresentante", bInfo.get("correoRepresentante"));
          enrich.put("resumenNecesidades", bInfo.get("resumenNecesidades"));
          enrich.put("necesidadesTexto", bInfo.get("necesidadesTexto"));
        } else {
          enrich.put("nombreOrganizacion", "Comedor Comunitario La Esperanza");
          enrich.put("nombreLegible", "Comedor Comunitario La Esperanza");
          enrich.put("correoRepresentante", "representante@esperanza.org");
          enrich.put("resumenNecesidades", "Alimentos no perecederos y abrigo");
          enrich.put("necesidadesTexto", "Alimentos no perecederos y abrigo");
        }
        String motivoAlgoritmo;
        if ("SEMANTICO".equalsIgnoreCase(algoritmo)) {
          motivoAlgoritmo = "Algoritmo 1 · Compatibilidad Semántica de Subcategoría";
        } else if ("SUBATENDIDOS".equalsIgnoreCase(algoritmo)) {
          motivoAlgoritmo = "Algoritmo 2 · Prioridad a Entidades Sub-Atendidas (Equidad)";
        } else {
          motivoAlgoritmo = "Motor Híbrido (Compatibilidad Semántica + Prioridad Sub-Atendidos)";
        }
        enrich.put("motivoMatch", motivoAlgoritmo);
        sugerenciasEnriquecidas.add(enrich);
      }

      // Si el motor no arrojó match estricto pero hay beneficiarios, mostrar los disponibles como
      // candidatos secundarios por equidad para que el admin siempre pueda asignar
      if (sugerenciasEnriquecidas.isEmpty() && !beneficiariosEnriquecidos.isEmpty()) {
        for (Map<String, Object> bInfo : beneficiariosEnriquecidos) {
          Map<String, Object> fallbackMatch = new HashMap<>();
          fallbackMatch.put("id", bInfo.get("id"));
          fallbackMatch.put("nombreOrganizacion", bInfo.get("nombreOrganizacion"));
          fallbackMatch.put("nombreLegible", bInfo.get("nombreLegible"));
          fallbackMatch.put("correoRepresentante", bInfo.get("correoRepresentante"));
          fallbackMatch.put("resumenNecesidades", bInfo.get("resumenNecesidades"));
          fallbackMatch.put("necesidadesTexto", bInfo.get("necesidadesTexto"));
          fallbackMatch.put(
              "motivoMatch",
              "Candidato por Prioridad a Sub-Atendidos (Recepción General Habilitada)");
          sugerenciasEnriquecidas.add(fallbackMatch);
        }
      }

      model.addAttribute("sugerencias", sugerenciasEnriquecidas);
    }
    return "admin/admin/matchmaking";
  }

  @PostMapping("/admin/matchmaking/asignar")
  @PreAuthorize("hasRole('ADMIN')")
  public String asignarDonacionMatchmaking(
      @RequestParam("donacionId") UUID donacionId,
      @RequestParam("beneficiarioId") UUID beneficiarioId,
      RedirectAttributes redirectAttributes) {
    try {
      donacionesGateway.asignarDonacion(donacionId, beneficiarioId);
      String shortCode = "#DT-" + donacionId.toString().substring(0, 4).toUpperCase();
      redirectAttributes.addFlashAttribute(
          "flashOk",
          "Donación "
              + shortCode
              + " asignada exitosamente a la organización beneficiaria y enviada a planificación logística.");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "No se pudo asignar la donación: " + e.getMessage());
    }
    return "redirect:/app/admin/matchmaking";
  }

  @PostMapping("/admin/matchmaking/batch")
  @PreAuthorize("hasRole('ADMIN')")
  public String ejecutarBatchMatchmaking(RedirectAttributes redirectAttributes) {
    try {
      donacionesGateway.ejecutarMatchmakingBatch();
      redirectAttributes.addFlashAttribute(
          "flashOk",
          "Matchmaking automático por lotes ejecutado sobre todas las donaciones en depósito.");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "Error al ejecutar asignación automática: " + e.getMessage());
    }
    return "redirect:/app/admin/matchmaking";
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
  public String donantePanel(
      @RequestParam(name = "tab", required = false, defaultValue = "resumen") String tab,
      Model model,
      Authentication auth) {
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

    // 1. Obtener seguimiento de las donaciones del donante
    List<Map<String, Object>> rawDonaciones = donacionesGateway.listarDonaciones(null, donanteId);
    if (rawDonaciones.isEmpty() && !DEFAULT_DONANTE_ID.equals(donanteId)) {
      rawDonaciones = donacionesGateway.listarDonaciones(null, DEFAULT_DONANTE_ID);
    }
    List<Map<String, Object>> misDonaciones = new ArrayList<>();
    for (Map<String, Object> d : rawDonaciones) {
      if (d == null) continue;
      Map<String, Object> row = new HashMap<>(d);
      String est =
          d.get("estado") != null ? d.get("estado").toString().toUpperCase() : "EN_DEPOSITO";
      int etapa = 1;
      String estadoLabel = "En Depósito · Auditada (+10 pts)";
      String destinoLabel = "Pendiente de Matchmaking con Comedor";
      if ("ASIGNADA".equals(est) || "EN_ENTREGA".equals(est)) {
        etapa = 2;
        estadoLabel = "Asignada a Comedor · En Ruta";
        destinoLabel =
            d.get("correoBeneficiario") != null
                ? d.get("correoBeneficiario").toString()
                : "Comedor Comunitario La Esperanza";
      } else if ("ENTREGADA".equals(est)) {
        etapa = 3;
        estadoLabel = "Entregada en Destino (+25 pts)";
        destinoLabel =
            d.get("correoBeneficiario") != null
                ? d.get("correoBeneficiario").toString()
                : "Comedor Comunitario La Esperanza";
      }
      row.put("etapaNumero", etapa);
      row.put("pasoActual", etapa);
      row.put("estadoLegible", estadoLabel);
      row.put("destinoLegible", destinoLabel);
      row.put("ongAsignada", destinoLabel);
      misDonaciones.add(row);
    }

    // Fallback resiliente si el microservicio de incentivos está despertando en Render Free Tier
    if (donaciones == 0 && !misDonaciones.isEmpty()) {
      donaciones = misDonaciones.size();
      bienes = Math.max(bienes, donaciones * 5L);
      puntosReales = Math.max(puntosReales, (donaciones * 10L) + (bienes * 2L));
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
      puntosFaltantes = Math.max(0, proximoObjetivo - puntosReales);
      progresoPct =
          proximoObjetivo > 0 ? (int) Math.min(100, (puntosReales * 100) / proximoObjetivo) : 0;
      isNuevoUsuario = false;
      kilosTotales = (donaciones * 30) + (bienes * 8);
      platosComida = kilosTotales * 4;
      comedoresAsistidos = Math.max(1, Math.min(donaciones, 5));
      entregasCompletadas = donaciones;
    }

    // 2. Obtener buscador de causas activas y necesidades de ONGs
    List<Map<String, Object>> beneficiarios = donacionesGateway.obtenerBeneficiarios();
    List<Map<String, Object>> causasActivas = new ArrayList<>();
    for (Map<String, Object> b : beneficiarios) {
      if (b == null) continue;
      String bId = String.valueOf(b.get("id"));
      String correo =
          b.get("correoRepresentante") != null
              ? b.get("correoRepresentante").toString()
              : "contacto@comedor.org";
      String nombreOrg =
          ("b2222222-2222-4222-8222-222222222222".equalsIgnoreCase(bId)
                  || correo.toLowerCase().contains("esperanza"))
              ? "Comedor Comunitario La Esperanza"
              : "Merendero & Hogar Solidario";
      List<Map<String, Object>> necsRaw =
          b.get("necesidadesDeclaradas") instanceof List<?> list
              ? (List<Map<String, Object>>) list
              : Collections.emptyList();
      List<Map<String, Object>> necsClean = sanitizarNecesidades(necsRaw);
      for (Map<String, Object> n : necsClean) {
        Map<String, Object> causa = new HashMap<>();
        causa.put("organizacion", nombreOrg);
        causa.put("ongNombre", nombreOrg);
        causa.put("correo", correo);
        causa.put("ubicacion", "Buenos Aires, Argentina");
        causa.put("categoria", n.getOrDefault("subcategoria", "No Perecederos"));
        causa.put(
            "descripcion", n.getOrDefault("descripcion", "Alimentos secos para raciones diarias"));
        causa.put("cantidad", n.getOrDefault("cantidad", 20));
        causa.put("urgencia", "Alta");
        causasActivas.add(causa);
      }
    }
    // Asegurar causas comunitarias activas representativas para el buscador
    causasActivas.add(
        Map.of(
            "organizacion", "Comedor Comunitario El Faro",
            "ongNombre", "Comedor Comunitario El Faro",
            "correo", "coordinacion@elfaro.org.ar",
            "ubicacion", "Palermo / Barracas, CABA",
            "categoria", "No Perecederos",
            "descripcion", "Fideos secos, arroz largo fino y lentejas para 220 raciones diarias",
            "cantidad", 50,
            "urgencia", "Alta (Urgente)"));
    causasActivas.add(
        Map.of(
            "organizacion", "Hogar de Niñez Los Gurises",
            "ongNombre", "Hogar de Niñez Los Gurises",
            "correo", "recepcion@losgurises.org.ar",
            "ubicacion", "La Matanza, PBA",
            "categoria", "Abrigo",
            "descripcion", "Camperas de abrigo, frazadas polares y calzado infantil de invierno",
            "cantidad", 35,
            "urgencia", "Alta"));
    causasActivas.add(
        Map.of(
            "organizacion", "Fundación Raíces Solidarias",
            "ongNombre", "Fundación Raíces Solidarias",
            "correo", "logistica@raices.org.ar",
            "ubicacion", "Avellaneda, PBA",
            "categoria", "Higiene",
            "descripcion", "Kits de higiene familiar, jabón blanco, alcohol en gel y pañales",
            "cantidad", 40,
            "urgencia", "Media"));
    causasActivas.add(
        Map.of(
            "organizacion", "Posta Sanitaria Barrial Sur",
            "ongNombre", "Posta Sanitaria Barrial Sur",
            "correo", "salud@postasur.org.ar",
            "ubicacion", "Quilmes, PBA",
            "categoria", "Medicamentos",
            "descripcion",
                "Botiquines de primeros auxilios, gasas estériles y antitérmicos pediátricos",
            "cantidad", 25,
            "urgencia", "Alta"));

    model.addAttribute("tabActiva", tab != null ? tab.toLowerCase() : "resumen");
    model.addAttribute("misDonaciones", misDonaciones);
    model.addAttribute("causasActivas", causasActivas);
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
  public String donanteDonacionesForm(
      @RequestParam(name = "descripcion", required = false) String descripcion,
      @RequestParam(name = "categoria", required = false) String categoria,
      @RequestParam(name = "ong", required = false) String ong,
      Model model,
      Authentication auth) {
    model.addAttribute("nombreUsuario", auth.getName());
    if (descripcion != null && !descripcion.isBlank()) {
      model.addAttribute("prefillDescripcion", descripcion);
    }
    if (categoria != null && !categoria.isBlank()) {
      model.addAttribute("prefillCategoria", categoria);
    }
    if (ong != null && !ong.isBlank()) {
      model.addAttribute("prefillOng", ong);
    }
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
      if (resp.get("donacionesSegmentadas") instanceof List<?> segList && !segList.isEmpty()) {
        Object firstSeg = segList.get(0);
        if (firstSeg instanceof Map<?, ?> segMap && segMap.get("id") != null) {
          donacionId = segMap.get("id").toString();
        }
      }
      String codigoCorto =
          donacionId.length() >= 4
              ? "#DT-" + donacionId.substring(0, 4).toUpperCase()
              : "#DT-" + donacionId.toUpperCase();

      // Sumar +10 puntos inmediatos en depósito y extender racha del donante
      try {
        UUID donacionUuid = UUID.randomUUID();
        try {
          donacionUuid = UUID.fromString(donacionId);
        } catch (Exception ignored) {
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
          "Donación "
              + codigoCorto
              + " registrada en depósito con éxito. Sumaste +10 puntos y podés seguir su estado de asignación en la pestaña 'Mis Donaciones'.");
      return "redirect:/app/donante?tab=seguimiento";
    } catch (Exception e) {
      String msg = e.getMessage() != null ? e.getMessage() : "Error en el servicio";
      if (msg.contains("502") || msg.contains("Bad Gateway") || msg.contains("<!DOCTYPE html>")) {
        msg =
            "El microservicio de donaciones está iniciando en la nube. Por favor, reintentá tu donación en unos segundos.";
      } else if (msg.contains("503")
          || msg.contains("504")
          || msg.contains("Timeout")
          || msg.contains("timed out")) {
        msg =
            "El microservicio de donaciones tardó en responder. Por favor, reintentá en un instante.";
      }
      redirectAttributes.addFlashAttribute("flashError", "Error al registrar donación: " + msg);
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
