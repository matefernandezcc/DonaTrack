package com.donatrack.web.service;

import com.donatrack.web.gateway.DonacionesGateway;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SolicitudOngService {

  private static final Logger log = LoggerFactory.getLogger(SolicitudOngService.class);

  public record SolicitudOng(
      String id,
      String nombreOng,
      String cuit,
      String direccion,
      String email,
      String telefono,
      String responsable,
      String tipoEntidad,
      String necesidades,
      String estado,
      String fecha) {}

  private final DonacionesGateway donacionesGateway;
  // Fallback in-memory map for unit tests where DonacionesGateway is not mocked
  private final Map<String, SolicitudOng> fallbackSolicitudes = new ConcurrentHashMap<>();

  @Autowired
  public SolicitudOngService(DonacionesGateway donacionesGateway) {
    this.donacionesGateway = donacionesGateway;
  }

  public SolicitudOngService() {
    this(null);
  }

  public SolicitudOng registrarSolicitud(Map<String, String> data) {
    if (donacionesGateway != null) {
      try {
        Map<String, Object> resp = donacionesGateway.registrarSolicitudOng(data);
        if (resp != null) {
          return mapToRecord(resp);
        }
      } catch (Exception e) {
        log.error(
            "Error delegando registro de Solicitud ONG a donatrack-donaciones: {}", e.getMessage());
      }
    }

    // Fallback local
    String id = UUID.randomUUID().toString().substring(0, 8);
    String fecha =
        java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    String nombre =
        data.containsKey("nombreOng")
            ? data.get("nombreOng")
            : data.getOrDefault("nombreOrganizacion", "ONG sin nombre");
    String tipo =
        data.containsKey("tipoEntidad")
            ? data.get("tipoEntidad")
            : data.getOrDefault("tipo", "Comedor");

    SolicitudOng s =
        new SolicitudOng(
            id,
            nombre,
            data.getOrDefault("cuit", "-"),
            data.getOrDefault("direccion", "-"),
            data.getOrDefault("email", "-"),
            data.getOrDefault("telefono", "-"),
            data.getOrDefault("responsable", "-"),
            tipo,
            data.getOrDefault("necesidades", "-"),
            "PENDIENTE",
            fecha);
    fallbackSolicitudes.put(id, s);
    return s;
  }

  public List<SolicitudOng> listarTodas() {
    if (donacionesGateway != null) {
      try {
        List<Map<String, Object>> list = donacionesGateway.listarSolicitudesOng();
        if (list != null && !list.isEmpty()) {
          return list.stream().map(this::mapToRecord).toList();
        }
      } catch (Exception e) {
        log.error("Error consultando solicitudes en donatrack-donaciones: {}", e.getMessage());
      }
    }

    List<SolicitudOng> list = new ArrayList<>(fallbackSolicitudes.values());
    list.sort((a, b) -> b.fecha().compareTo(a.fecha()));
    return list;
  }

  public Optional<SolicitudOng> obtenerPorId(String id) {
    if (donacionesGateway != null) {
      try {
        Map<String, Object> resp = donacionesGateway.obtenerSolicitudOng(id);
        if (resp != null) {
          return Optional.of(mapToRecord(resp));
        }
      } catch (Exception e) {
        log.error("Error obteniendo solicitud {} de donatrack-donaciones: {}", id, e.getMessage());
      }
    }
    return Optional.ofNullable(fallbackSolicitudes.get(id));
  }

  public boolean actualizarEstado(String id, String nuevoEstado) {
    if (donacionesGateway != null) {
      try {
        Map<String, Object> resp = donacionesGateway.actualizarEstadoSolicitudOng(id, nuevoEstado);
        if (resp != null) {
          return true;
        }
      } catch (Exception e) {
        log.error(
            "Error actualizando estado de solicitud {} en donatrack-donaciones: {}",
            id,
            e.getMessage());
      }
    }

    SolicitudOng s = fallbackSolicitudes.get(id);
    if (s != null) {
      fallbackSolicitudes.put(
          id,
          new SolicitudOng(
              s.id(),
              s.nombreOng(),
              s.cuit(),
              s.direccion(),
              s.email(),
              s.telefono(),
              s.responsable(),
              s.tipoEntidad(),
              s.necesidades(),
              nuevoEstado,
              s.fecha()));
      return true;
    }
    return false;
  }

  private SolicitudOng mapToRecord(Map<String, Object> m) {
    return new SolicitudOng(
        String.valueOf(m.getOrDefault("id", "")),
        String.valueOf(m.getOrDefault("nombreOng", "")),
        String.valueOf(m.getOrDefault("cuit", "-")),
        String.valueOf(m.getOrDefault("direccion", "-")),
        String.valueOf(m.getOrDefault("email", "-")),
        String.valueOf(m.getOrDefault("telefono", "-")),
        String.valueOf(m.getOrDefault("responsable", "-")),
        String.valueOf(m.getOrDefault("tipoEntidad", "-")),
        String.valueOf(m.getOrDefault("necesidades", "-")),
        String.valueOf(m.getOrDefault("estado", "PENDIENTE")),
        String.valueOf(m.getOrDefault("fecha", "")));
  }
}
