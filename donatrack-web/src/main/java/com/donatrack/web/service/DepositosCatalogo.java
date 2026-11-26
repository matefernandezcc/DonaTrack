package com.donatrack.web.service;

import com.donatrack.web.dto.DepositoDTO;
import java.util.List;

/**
 * Catálogo canónico de depósitos oficiales habilitados de DonaTrack en CABA.
 * Fuente única compartida entre el mapa público, el formulario de donantes y el copiloto.
 */
public final class DepositosCatalogo {

  public static final String DEFAULT_DEPOSITO_ID = "utn-medrano";

  private static final List<DepositoDTO> DEPOSITOS_ACTIVOS =
      List.of(
          new DepositoDTO(
              "utn-medrano",
              "Depósito Central — UTN FRBA Medrano",
              "Almagro",
              "Av. Medrano 951, CABA",
              "Lun a Vie 9:00 a 18:00 hs · Sáb 9:00 a 13:00 hs",
              -34.5986,
              -58.4201,
              "Alimentos No Perecederos · Ropa y Abrigo · Higiene · Útiles",
              "A 3 cuadras de Estación Medrano (Subte B) · Sede Central DonaTrack",
              "Centro logístico principal junto a UTN FRBA Sede Medrano. Recepción, clasificación y despacho diario hacia comedores.",
              "Disponible"),
          new DepositoDTO(
              "utn-lugano",
              "Depósito Sur — UTN FRBA Campus Lugano",
              "Villa Lugano",
              "Mozart 2300, CABA",
              "Lun a Vie 9:00 a 17:30 hs",
              -34.6625,
              -58.4611,
              "Alimentos No Perecederos · Mobiliario · Equipamiento Escolar",
              "Frente al Parque Roca · Acceso Autopista Dellepiane y Premetro",
              "Base operativa Zona Sur junto al Campus UTN Lugano con dársena de carga para grandes volúmenes y mobiliario.",
              "Disponible"),
          new DepositoDTO(
              "caballito",
              "Depósito Caballito — Parque Rivadavia",
              "Caballito",
              "Av. Rivadavia 5108, CABA",
              "Lun a Vie 10:00 a 19:00 hs · Sáb 10:00 a 14:00 hs",
              -34.6181,
              -58.4333,
              "Alimentos No Perecederos · Ropa y Abrigo · Juguetes",
              "A 1 cuadra de Estación Acoyte (Subte A) y Parque Rivadavia",
              "Punto barrial céntrico de fácil acceso en Caballito para entregas particulares de alimentos, abrigo y útiles.",
              "Disponible"),
          new DepositoDTO(
              "chacarita",
              "Depósito Chacarita — Estación Lacroze",
              "Chacarita",
              "Av. Corrientes 6271, CABA",
              "Lun a Vie 9:30 a 18:00 hs",
              -34.5907,
              -58.4478,
              "Alimentos No Perecederos · Ropa y Abrigo · Higiene",
              "A metros de Estación Federico Lacroze (Subte B / Tren Urquiza)",
              "Nodo de recepción Zona Norte-Oeste con acopio rápido para donaciones de alimentos secos y artículos de higiene.",
              "Disponible"),
          new DepositoDTO(
              "palermo",
              "Depósito Palermo — Av. Santa Fe",
              "Palermo",
              "Av. Santa Fe 3253, CABA",
              "Lun a Sáb 10:00 a 19:00 hs",
              -34.5888,
              -58.4213,
              "Ropa y Abrigo · Alimentos · Medicamentos · Juguetes",
              "A 1 cuadra de Estación Bulnes (Subte D)",
              "Punto de recepción con horario extendido en Palermo ideal para donaciones particulares y campañas solidarias.",
              "Disponible"),
          new DepositoDTO(
              "belgrano",
              "Depósito Belgrano — Av. Cabildo",
              "Belgrano",
              "Av. Cabildo 2040, CABA",
              "Lun a Vie 9:30 a 18:30 hs · Sáb 10:00 a 13:30 hs",
              -34.5627,
              -58.4565,
              "Alimentos No Perecederos · Ropa y Abrigo · Útiles Escolares",
              "A 2 cuadras de Estación Juramento (Subte D)",
              "Punto de recepción Zona Norte para donantes de Belgrano, Núñez y Colegiales con consolidación diaria hacia la red.",
              "Disponible"));

  private DepositosCatalogo() {}

  public static List<DepositoDTO> listarDisponibles() {
    return DEPOSITOS_ACTIVOS;
  }

  public static DepositoDTO buscarPorIdODefault(String id) {
    if (id != null && !id.isBlank()) {
      String clean = id.trim().toLowerCase();
      for (DepositoDTO dep : DEPOSITOS_ACTIVOS) {
        if (dep.id().equalsIgnoreCase(clean)) {
          return dep;
        }
      }
    }
    return DEPOSITOS_ACTIVOS.get(0);
  }
}
