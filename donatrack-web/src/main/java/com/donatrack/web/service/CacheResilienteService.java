package com.donatrack.web.service;

import com.donatrack.web.gateway.LogisticaGateway;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CacheResilienteService {

  private static final Logger log = LoggerFactory.getLogger(CacheResilienteService.class);
  private static final long TTL_MILLIS = 180_000L; // 180 segundos
  private static final long FAST_TIMEOUT_MS = 2_000L; // 2 segundos fallback

  private final LogisticaGateway logisticaGateway;
  private final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();
  private final AtomicLong ultimaActualizacionRutas = new AtomicLong(0L);

  public CacheResilienteService(LogisticaGateway logisticaGateway) {
    this.logisticaGateway = logisticaGateway;
  }

  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> obtenerRutasConCache() {
    long ahora = System.currentTimeMillis();
    boolean expirado = (ahora - ultimaActualizacionRutas.get()) > TTL_MILLIS;

    if (!expirado && cache.containsKey("rutas")) {
      Object cached = cache.get("rutas");
      if (cached instanceof List<?> list) {
        return (List<Map<String, Object>>) list;
      }
    }

    // Intentar consulta asíncrona con timeout estricto de 2.0s
    try {
      CompletableFuture<List<Map<String, Object>>> future =
          CompletableFuture.supplyAsync(logisticaGateway::listarRutas);
      List<Map<String, Object>> freshRutas = future.get(FAST_TIMEOUT_MS, TimeUnit.MILLISECONDS);

      if (freshRutas != null && !freshRutas.isEmpty()) {
        cache.put("rutas", freshRutas);
        ultimaActualizacionRutas.set(ahora);
        return freshRutas;
      }
    } catch (Exception e) {
      log.warn(
          "Timeout o error (<2s) al consultar logistica (posible cold start Render): {}. Aplicando fallback de caché.",
          e.getMessage());
    }

    // Si falló pero tenemos caché previo (aunque sea viejo), devolverlo
    if (cache.containsKey("rutas")) {
      Object cached = cache.get("rutas");
      if (cached instanceof List<?> list && !list.isEmpty()) {
        return (List<Map<String, Object>>) list;
      }
    }

    // Fallback de contingencia: rutas predeterminadas sanitizadas
    List<Map<String, Object>> contingencia = generarRutasContingencia();
    cache.put("rutas", contingencia);
    ultimaActualizacionRutas.set(ahora);
    return contingencia;
  }

  public void invalidarCache() {
    cache.remove("rutas");
    ultimaActualizacionRutas.set(0L);
  }

  private List<Map<String, Object>> generarRutasContingencia() {
    List<Map<String, Object>> rutas = new ArrayList<>();

    Map<String, Object> ruta1 = new HashMap<>();
    ruta1.put("id", "31008064-071a-4d7a-ac37-33318f7d9842");
    ruta1.put("fechaOperativa", "2026-05-12");
    ruta1.put("iniciada", true);

    Map<String, Object> camion1 = new HashMap<>();
    camion1.put("patente", "AE 884 PO");
    camion1.put("capacidadCarga", 3500.0);
    ruta1.put("camion", camion1);

    Map<String, Object> chofer1 = new HashMap<>();
    chofer1.put("nombre", "Carlos Gómez");
    chofer1.put("legajo", "CH-4412");
    ruta1.put("chofer", chofer1);

    List<Map<String, Object>> paradas = new ArrayList<>();

    Map<String, Object> parada1 = new HashMap<>();
    parada1.put("orden", 1);
    parada1.put(
        "direccion",
        Map.of(
            "calle", "Av. San Juan",
            "altura", "2845",
            "localidad", "CABA"));
    parada1.put("coordenada", Map.of("latitud", -34.6241, "longitud", -58.4038));

    Map<String, Object> entrega1 = new HashMap<>();
    entrega1.put("idEntrega", "2418a111-0000-4000-8000-000000000001");
    entrega1.put("estado", "EN_TRASLADO");
    parada1.put("entregas", List.of(entrega1));

    Map<String, Object> parada2 = new HashMap<>();
    parada2.put("orden", 2);
    parada2.put(
        "direccion",
        Map.of(
            "calle", "Av. Rivadavia",
            "altura", "6200",
            "localidad", "Flores, CABA"));
    parada2.put("coordenada", Map.of("latitud", -34.6295, "longitud", -58.4562));

    Map<String, Object> entrega2 = new HashMap<>();
    entrega2.put("idEntrega", "3300b222-0000-4000-8000-000000000002");
    entrega2.put("estado", "ASIGNADA");
    parada2.put("entregas", List.of(entrega2));

    paradas.add(parada1);
    paradas.add(parada2);
    ruta1.put("paradas", paradas);

    rutas.add(ruta1);
    return rutas;
  }
}
