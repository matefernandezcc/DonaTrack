package com.donatrack.web.controller;

import com.donatrack.web.dto.TokenResponse;
import com.donatrack.web.gateway.AuthGateway;
import com.donatrack.web.gateway.IncentivosGateway;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PublicController {

  private static final Logger log = LoggerFactory.getLogger(PublicController.class);

  private final AuthGateway authGateway;
  private final IncentivosGateway incentivosGateway;
  private final JwtDecoder jwtDecoder;
  private final SecurityContextRepository securityContextRepository;

  public PublicController(
      AuthGateway authGateway,
      IncentivosGateway incentivosGateway,
      JwtDecoder jwtDecoder,
      SecurityContextRepository securityContextRepository) {
    this.authGateway = authGateway;
    this.incentivosGateway = incentivosGateway;
    this.jwtDecoder = jwtDecoder;
    this.securityContextRepository = securityContextRepository;
  }

  @GetMapping("/healthcheck")
  @ResponseBody
  public Map<String, String> healthcheck() {
    return Map.of("status", "UP");
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

