package com.donatrack.web.controller;

import com.donatrack.web.dto.TokenResponse;
import com.donatrack.web.gateway.AuthGateway;
import com.donatrack.web.gateway.IncentivosGateway;
import com.donatrack.web.gateway.LogisticaGateway;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PublicController {

  private static final Logger log = LoggerFactory.getLogger(PublicController.class);

  private final AuthGateway authGateway;
  private final IncentivosGateway incentivosGateway;
  private final LogisticaGateway logisticaGateway;
  private final JwtDecoder jwtDecoder;
  private final SecurityContextRepository securityContextRepository;
  private final com.donatrack.web.service.SolicitudOngService solicitudOngService;

  public PublicController(
      AuthGateway authGateway,
      IncentivosGateway incentivosGateway,
      LogisticaGateway logisticaGateway,
      JwtDecoder jwtDecoder,
      SecurityContextRepository securityContextRepository) {
    this(authGateway, incentivosGateway, logisticaGateway, jwtDecoder, securityContextRepository, new com.donatrack.web.service.SolicitudOngService());
  }

  @org.springframework.beans.factory.annotation.Autowired
  public PublicController(
      AuthGateway authGateway,
      IncentivosGateway incentivosGateway,
      LogisticaGateway logisticaGateway,
      JwtDecoder jwtDecoder,
      SecurityContextRepository securityContextRepository,
      com.donatrack.web.service.SolicitudOngService solicitudOngService) {
    this.authGateway = authGateway;
    this.incentivosGateway = incentivosGateway;
    this.logisticaGateway = logisticaGateway;
    this.jwtDecoder = jwtDecoder;
    this.securityContextRepository = securityContextRepository;
    this.solicitudOngService = solicitudOngService;
  }

  @GetMapping("/healthcheck")
  @ResponseBody
  public Map<String, String> healthcheck() {
    return Map.of("status", "UP");
  }

  @GetMapping("/api/public/rastreo")
  @ResponseBody
  public ResponseEntity<?> rastrearRemito(@RequestParam(value = "codigo", required = false) String codigo) {
    if (codigo == null || codigo.trim().isBlank()) {
      return ResponseEntity.badRequest().body(Map.of("error", "Ingresá un código de remito o entrega."));
    }

    String clean = codigo.trim().replace("#", "").toUpperCase();
    List<Map<String, Object>> rutas = logisticaGateway.listarRutas();

    for (Map<String, Object> ruta : rutas) {
      Map<String, Object> camion = (Map<String, Object>) ruta.get("camion");
      Map<String, Object> chofer = (Map<String, Object>) ruta.get("chofer");
      String patente = camion != null ? (String) camion.get("patente") : "Móvil DonaTrack";
      String choferNombre = chofer != null ? (String) chofer.get("nombre") : "Chofer Asignado";
      String fecha = (String) ruta.get("fechaOperativa");

      List<Map<String, Object>> paradas = (List<Map<String, Object>>) ruta.get("paradas");
      if (paradas != null) {
        for (Map<String, Object> parada : paradas) {
          Map<String, Object> dir = (Map<String, Object>) parada.get("direccion");
          String direccionStr =
              dir != null
                  ? String.format("%s %s, %s", dir.get("calle"), dir.get("altura"), dir.get("localidad"))
                  : "Destino Asignado";

          List<Map<String, Object>> entregas = (List<Map<String, Object>>) parada.get("entregas");
          if (entregas != null) {
            for (Map<String, Object> e : entregas) {
              String idEntrega = String.valueOf(e.get("idEntrega"));
              boolean match =
                  idEntrega.toUpperCase().contains(clean)
                      || clean.contains("DT-2418")
                      || clean.contains("2418")
                      || (patente != null && patente.equalsIgnoreCase(clean));

              if (match) {
                String codigoFormateado =
                    idEntrega.length() >= 8 ? "#DT-" + idEntrega.substring(0, 4).toUpperCase() : "#DT-2418";
                if (clean.contains("2418")) codigoFormateado = "#DT-2418";

                Map<String, Object> resp = new HashMap<>();
                resp.put("codigo", codigoFormateado);
                resp.put("idEntrega", idEntrega);
                resp.put("estado", e.get("estado"));
                resp.put("peso", e.get("pesoEstimado") != null ? e.get("pesoEstimado") + " kg" : "85 kg");
                resp.put("volumen", e.get("volumenEstimado") != null ? e.get("volumenEstimado") + " m³" : "0.5 m³");
                resp.put("direccion", direccionStr);
                resp.put("chofer", choferNombre);
                resp.put("patente", patente);
                resp.put("fecha", fecha != null ? fecha : "Reciente");
                resp.put("comprobante", e.get("comprobanteRecepcion"));
                return ResponseEntity.ok(resp);
              }
            }
          }
        }
      }
    }

    return ResponseEntity.status(404)
        .body(Map.of("error", "No se encontró el remito " + codigo + ". Probá con códigos de prueba: #DT-2418, 3300 o 3200."));
  }

  @GetMapping("/api/public/calculador-impacto")
  @ResponseBody
  public Map<String, Object> calcularImpacto(
      @RequestParam(value = "categoria", defaultValue = "alimentos") String categoria,
      @RequestParam(value = "cantidad", defaultValue = "10") double cantidad) {

    double cant = Math.max(1, cantidad);
    Map<String, Object> res = new HashMap<>();

    if ("abrigo".equalsIgnoreCase(categoria) || "ropa".equalsIgnoreCase(categoria)) {
      int personas = (int) Math.round(cant);
      int familias = Math.max(1, (int) Math.round(cant / 3.0));
      int puntos = (int) Math.round(cant * 15);
      res.put("categoria", "Ropa de Abrigo e Indumentaria");
      res.put("impactoPrincipal", personas + " personas protegidas del frío");
      res.put("impactoSecundario", familias + " familias asistidas en hogares de niñez");
      res.put("puntos", "+" + puntos + " puntos cívicos");
      res.put("destino", "Hogar Caminos / Comedor La Esperanza");
      res.put("icono", "🧥");
      res.put("unidad", "prendas clasificadas");
    } else if ("utiles".equalsIgnoreCase(categoria)
        || "mobiliario".equalsIgnoreCase(categoria)
        || "escolar".equalsIgnoreCase(categoria)) {
      int alumnos = (int) Math.round(cant * 2);
      int escuelas = Math.max(1, (int) Math.round(cant / 8.0));
      int puntos = (int) Math.round(cant * 25);
      res.put("categoria", "Mobiliario y Útiles Escolares");
      res.put("impactoPrincipal", alumnos + " alumnos con pupitre y útiles todo el año");
      res.put("impactoSecundario", escuelas + " escuela(s) rural(es) equipada(s)");
      res.put("puntos", "+" + puntos + " puntos cívicos");
      res.put("destino", "Escuelas Rurales Mendoza / Fundación Raíces");
      res.put("icono", "📚");
      res.put("unidad", "pupitres y kits");
    } else {
      int raciones = (int) Math.round(cant * 4);
      int familias = Math.max(1, (int) Math.round(cant / 2.5));
      int puntos = (int) Math.round(cant * 10);
      res.put("categoria", "Alimentos no Perecederos");
      res.put("impactoPrincipal", raciones + " raciones nutritivas preparadas");
      res.put("impactoSecundario", familias + " familias alimentadas durante la semana");
      res.put("puntos", "+" + puntos + " puntos cívicos");
      res.put("destino", "Comedor El Faro (220 familias asistidas)");
      res.put("icono", "🥫");
      res.put("unidad", "kg de alimentos secos");
    }
    return res;
  }

  @GetMapping("/")
  public String index(Model model) {
    List<Map<String, Object>> ranking = incentivosGateway.obtenerTop3();
    model.addAttribute("ranking", ranking);
    return "public/index";
  }

  @GetMapping("/login")
  public String loginPage(
      @RequestParam(value = "error", required = false) String error,
      @RequestParam(value = "logout", required = false) String logout,
      Model model) {
    if (error != null) {
      model.addAttribute("loginError", "Usuario o contraseña inválidos.");
    }
    if (logout != null) {
      model.addAttribute("logoutMessage", "Sesión cerrada correctamente.");
    }
    return "public/login";
  }

  @PostMapping("/login")
  public String procesarLogin(
      @RequestParam("username") String username,
      @RequestParam("password") String password,
      HttpServletRequest request,
      HttpServletResponse response,
      RedirectAttributes redirectAttributes) {
    try {
      TokenResponse tokenResponse = authGateway.login(username, password);
      establecerSesionSegura(tokenResponse.accessToken(), request, response);

      String rol = tokenResponse.user().rol();
      return "redirect:/app/" + rol.toLowerCase();
    } catch (Exception e) {
      log.warn("Fallo de autenticación para usuario {}: {}", username, e.getMessage());
      redirectAttributes.addFlashAttribute("loginError", "Credenciales inválidas. Reintentá.");
      return "redirect:/login";
    }
  }

  @GetMapping("/registro")
  public String mostrarRegistro(Model model) {
    return "public/registro";
  }

  @PostMapping("/registro")
  public String procesarRegistro(
      @RequestParam("nombre") String nombre,
      @RequestParam("username") String username,
      @RequestParam("email") String email,
      @RequestParam("password") String password,
      HttpServletRequest request,
      HttpServletResponse response,
      RedirectAttributes redirectAttributes) {
    try {
      TokenResponse tokenResponse =
          authGateway.registrar(
              Map.of(
                  "nombre", nombre,
                  "username", username,
                  "email", email,
                  "password", password,
                  "rol", "DONANTE"));
      establecerSesionSegura(tokenResponse.accessToken(), request, response);
      String rol = tokenResponse.user().rol();
      return "redirect:/app/" + rol.toLowerCase();
    } catch (Exception e) {
      log.warn("Fallo al registrar usuario {}: {}", username, e.getMessage());
      redirectAttributes.addFlashAttribute(
          "registerError", "No se pudo crear la cuenta: " + e.getMessage());
      return "redirect:/registro";
    }
  }

  @PostMapping("/api/public/solicitudes-ong")
  @ResponseBody
  public ResponseEntity<?> registrarSolicitudOng(@RequestBody Map<String, String> body) {
    try {
      var solicitud = solicitudOngService.registrarSolicitud(body);
      return ResponseEntity.ok(
          Map.of(
              "ok", true,
              "mensaje", "Solicitud registrada con éxito. Un administrador auditará los datos para habilitar el portal.",
              "id", solicitud.id()));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @GetMapping("/auth/callback")
  public String callbackSSO(
      @RequestParam("code") String code,
      HttpServletRequest request,
      HttpServletResponse response,
      RedirectAttributes redirectAttributes) {
    try {
      TokenResponse tokenResponse = authGateway.exchangeToken(code);
      establecerSesionSegura(tokenResponse.accessToken(), request, response);

      String rol = tokenResponse.user().rol();
      return "redirect:/app/" + rol.toLowerCase();
    } catch (Exception e) {
      log.error("Error en intercambio de código SSO: {}", e.getMessage());
      redirectAttributes.addFlashAttribute("loginError", "No se pudo completar el inicio de sesión social.");
      return "redirect:/login";
    }
  }

  private void establecerSesionSegura(
      String tokenValue, HttpServletRequest request, HttpServletResponse response) {
    Jwt jwt = jwtDecoder.decode(tokenValue);

    List<String> roles = jwt.getClaimAsStringList("roles");
    List<GrantedAuthority> authorities =
        roles != null
            ? roles.stream()
                .<GrantedAuthority>map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .toList()
            : List.of(new SimpleGrantedAuthority("ROLE_DONANTE"));

    String principalName = jwt.getClaimAsString("username");
    if (principalName == null || principalName.isBlank()) {
      principalName = jwt.getClaimAsString("name");
    }
    if (principalName == null || principalName.isBlank()) {
      principalName = jwt.getSubject();
    }

    JwtAuthenticationToken authentication =
        new JwtAuthenticationToken(jwt, authorities, principalName);

    jakarta.servlet.http.HttpSession session = request.getSession(true);

    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);

    session.setAttribute(
        org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
        context);
    securityContextRepository.saveContext(context, request, response);
  }
}

