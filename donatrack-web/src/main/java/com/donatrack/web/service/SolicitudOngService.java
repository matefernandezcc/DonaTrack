package com.donatrack.web.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class SolicitudOngService {

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
      String fecha
  ) {}

  private final Map<String, SolicitudOng> solicitudes = new ConcurrentHashMap<>();

  public SolicitudOngService() {
    // Sembrar 2 solicitudes para que el Administrador tenga casos reales de prueba en el panel
    registrarSolicitud(Map.of(
        "nombreOng", "Comedor Los Bajitos de Quilmes",
        "cuit", "30-71458921-4",
        "direccion", "Av. Calchaquí 1820, Quilmes, Bs. As.",
        "email", "comedor.losbajitos@gmail.com",
        "telefono", "+54 11 4253-9988",
        "responsable", "Marta Gómez",
        "tipoEntidad", "Comedor Comunitario",
        "necesidades", "Alimentos no perecederos (arroz, fideos, leche en polvo)"
    ));
    registrarSolicitud(Map.of(
        "nombreOng", "Hogar Niños del Sol",
        "cuit", "30-68954123-8",
        "direccion", "Calle 14 N° 520, La Plata",
        "email", "contacto@ninosdelsol.org.ar",
        "telefono", "+54 221 482-1144",
        "responsable", "Esteban Rivas",
        "tipoEntidad", "Hogar de Tránsito",
        "necesidades", "Camperas, frazadas y calzado escolar"
    ));
  }

  public SolicitudOng registrarSolicitud(Map<String, String> data) {
    String id = UUID.randomUUID().toString().substring(0, 8);
    String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    String nombre = data.containsKey("nombreOng") ? data.get("nombreOng") : data.getOrDefault("nombreOrganizacion", "ONG sin nombre");
    String tipo = data.containsKey("tipoEntidad") ? data.get("tipoEntidad") : data.getOrDefault("tipo", "Comedor");

    SolicitudOng s = new SolicitudOng(
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
        fecha
    );
    solicitudes.put(id, s);
    return s;
  }

  public List<SolicitudOng> listarTodas() {
    List<SolicitudOng> list = new ArrayList<>(solicitudes.values());
    list.sort((a, b) -> b.fecha().compareTo(a.fecha()));
    return list;
  }

  public Optional<SolicitudOng> obtenerPorId(String id) {
    return Optional.ofNullable(solicitudes.get(id));
  }

  public boolean actualizarEstado(String id, String nuevoEstado) {
    SolicitudOng s = solicitudes.get(id);
    if (s != null) {
      solicitudes.put(id, new SolicitudOng(
          s.id(), s.nombreOng(), s.cuit(), s.direccion(), s.email(), s.telefono(),
          s.responsable(), s.tipoEntidad(), s.necesidades(), nuevoEstado, s.fecha()
      ));
      return true;
    }
    return false;
  }
}
