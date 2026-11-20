package com.donatrack.web.controller.api;

import com.donatrack.web.dto.EntregaActivaDonanteDTO;
import com.donatrack.web.gateway.DonacionesGateway;
import com.donatrack.web.service.CacheResilienteService;
import com.donatrack.web.service.SolicitudOngService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/donante")
@PreAuthorize("hasAnyRole('DONANTE', 'ADMIN')")
public class DonanteApiController {

  private static final UUID DEFAULT_DONANTE_ID =
      UUID.fromString("a1111111-1111-4111-8111-111111111111");

  private final CacheResilienteService cacheResilienteService;
  private final DonacionesGateway donacionesGateway;
  private final SolicitudOngService solicitudOngService;

  public DonanteApiController(
      CacheResilienteService cacheResilienteService, DonacionesGateway donacionesGateway) {
    this(cacheResilienteService, donacionesGateway, null);
  }

  @Autowired
  public DonanteApiController(
      CacheResilienteService cacheResilienteService,
      DonacionesGateway donacionesGateway,
      @Autowired(required = false) SolicitudOngService solicitudOngService) {
    this.cacheResilienteService = cacheResilienteService;
    this.donacionesGateway = donacionesGateway;
    this.solicitudOngService = solicitudOngService;
  }

  @GetMapping("/mapa-camiones")
  public ResponseEntity<List<EntregaActivaDonanteDTO>> obtenerCamionesEnVivo(Authentication auth) {
    UUID donanteId = resolverDonanteId(auth);
    List<Map<String, Object>> misDonaciones = donacionesGateway.listarDonaciones(null, donanteId);
    if (misDonaciones == null || misDonaciones.isEmpty()) {
      return ResponseEntity.ok(Collections.emptyList());
    }

    Set<String> donacionesIds = new HashSet<>();
    Map<String, Map<String, Object>> donacionesPorId = new HashMap<>();
    for (Map<String, Object> d : misDonaciones) {
      if (d != null && d.get("id") != null) {
        String idLower = d.get("id").toString().toLowerCase();
        donacionesIds.add(idLower);
        donacionesPorId.put(idLower, d);
      }
    }

    if (donacionesIds.isEmpty()) {
      return ResponseEntity.ok(Collections.emptyList());
    }

    Map<String, String> nombresOngPorEmail = cargarNombresOngPorEmail();
    List<Map<String, Object>> rutas = cacheResilienteService.obtenerRutasConCache();
    List<EntregaActivaDonanteDTO> entregasActivas = new ArrayList<>();

    for (Map<String, Object> ruta : rutas) {
      if (ruta == null) continue;

      @SuppressWarnings("unchecked")
      Map<String, Object> camion =
          ruta.get("camion") instanceof Map<?, ?> c ? (Map<String, Object>) c : null;
      @SuppressWarnings("unchecked")
      Map<String, Object> chofer =
          ruta.get("chofer") instanceof Map<?, ?> ch ? (Map<String, Object>) ch : null;

      String patente =
          camion != null && camion.get("patente") != null
              ? camion.get("patente").toString()
              : "Sin patente";
      String choferNombre = resolverNombreChofer(chofer);
      String fecha =
          ruta.get("fechaOperativa") != null ? ruta.get("fechaOperativa").toString() : "Hoy";
      boolean rutaIniciada = Boolean.TRUE.equals(ruta.get("iniciada"));

      if (ruta.get("paradas") instanceof List<?> paradas) {
        for (Object pObj : paradas) {
          if (!(pObj instanceof Map<?, ?> parada)) continue;

          String dirLimpia = formatearDireccionParada(parada);

          double lat = -34.615;
          double lng = -58.425;
          if (parada.get("coordenada") instanceof Map<?, ?> coord) {
            if (coord.get("latitud") instanceof Number nLat) lat = nLat.doubleValue();
            if (coord.get("longitud") instanceof Number nLng) lng = nLng.doubleValue();
          }

          if (parada.get("entregas") instanceof List<?> entregas) {
            for (Object eObj : entregas) {
              if (!(eObj instanceof Map<?, ?> entrega)) continue;

              String idEntregaStr =
                  entrega.get("idEntrega") != null ? entrega.get("idEntrega").toString() : null;
              if (idEntregaStr == null) continue;

              String idEntregaLower = idEntregaStr.toLowerCase();
              if (!donacionesIds.contains(idEntregaLower)) {
                continue;
              }

              UUID idDonacion;
              try {
                idDonacion = UUID.fromString(idEntregaStr);
              } catch (Exception ex) {
                idDonacion = UUID.randomUUID();
              }

              String cleanId = idEntregaStr.replace("-", "").toUpperCase();
              String shortCode = cleanId.length() >= 4 ? cleanId.substring(0, 4) : cleanId;
              String codigoCorto = "DT-" + shortCode;

              String rawEstado =
                  entrega.get("estado") != null ? entrega.get("estado").toString() : "";
              String estado;
              if ("ENTREGADA".equalsIgnoreCase(rawEstado)) {
                estado = "ENTREGADA";
              } else if (rutaIniciada || "EN_TRASLADO".equalsIgnoreCase(rawEstado)) {
                estado = "EN_TRASLADO";
              } else {
                estado = "PLANIFICADA";
              }

              String destinoComedor = resolverDestinoComedor(
                  donacionesPorId.get(idEntregaLower), nombresOngPorEmail, dirLimpia);

              entregasActivas.add(
                  new EntregaActivaDonanteDTO(
                      idDonacion,
                      codigoCorto,
                      estado,
                      patente,
                      choferNombre,
                      destinoComedor,
                      lat,
                      lng,
                      fecha));
            }
          }
        }
      }
    }

    return ResponseEntity.ok(entregasActivas);
  }

  private Map<String, String> cargarNombresOngPorEmail() {
    if (solicitudOngService == null) {
      return Collections.emptyMap();
    }
    Map<String, String> mapa = new HashMap<>();
    for (var s : solicitudOngService.listarTodas()) {
      if (s != null
          && s.email() != null
          && !s.email().isBlank()
          && s.nombreOng() != null
          && !s.nombreOng().isBlank()) {
        mapa.putIfAbsent(s.email().trim().toLowerCase(), s.nombreOng().trim());
      }
    }
    return mapa;
  }

  private String resolverDestinoComedor(
      Map<String, Object> donacion,
      Map<String, String> nombresOngPorEmail,
      String dirLimpia) {
    if (donacion != null && donacion.get("correoBeneficiario") != null) {
      String correo = donacion.get("correoBeneficiario").toString().trim();
      String nombreOng = nombresOngPorEmail.get(correo.toLowerCase());
      if (nombreOng != null && !nombreOng.isBlank()) {
        return !dirLimpia.isBlank() ? nombreOng + " (" + dirLimpia + ")" : nombreOng;
      }
    }
    return !dirLimpia.isBlank() ? dirLimpia : "Organización Beneficiaria";
  }

  private String resolverNombreChofer(Map<String, Object> chofer) {
    if (chofer == null) {
      return "Chofer por asignar";
    }
    String nom = chofer.get("nombre") != null ? chofer.get("nombre").toString().trim() : "";
    String ape = chofer.get("apellido") != null ? chofer.get("apellido").toString().trim() : "";
    String full = (nom + " " + ape).trim();
    return full.isBlank() ? "Chofer por asignar" : full;
  }

  private String formatearDireccionParada(Map<?, ?> parada) {
    if (!(parada.get("direccion") instanceof Map<?, ?> dir)) {
      return "Sede Beneficiaria";
    }
    String calle = dir.get("calle") != null ? dir.get("calle").toString().trim() : "";
    String alt =
        dir.get("altura") != null ? dir.get("altura").toString().replaceAll("\\.0$", "").trim() : "";
    String loc = dir.get("localidad") != null ? dir.get("localidad").toString().trim() : "";

    if (alt.equals("0") || alt.equalsIgnoreCase("S/N")
        || (alt.equals("3200") && calle.matches(".*\\d+$"))) {
      alt = "";
    }
    if ("Argentina".equalsIgnoreCase(loc)) {
      loc = "";
    }

    String calleYAlt = (calle + (alt.isBlank() ? "" : " " + alt)).trim();
    if (!calleYAlt.isBlank() && !loc.isBlank()) {
      return calleYAlt + ", " + loc;
    }
    return !calleYAlt.isBlank() ? calleYAlt : (!loc.isBlank() ? loc : "Sede Beneficiaria");
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
}
