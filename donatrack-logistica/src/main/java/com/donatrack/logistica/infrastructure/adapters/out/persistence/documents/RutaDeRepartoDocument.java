package com.donatrack.logistica.infrastructure.adapters.out.persistence.documents;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "rutas_reparto")
@CompoundIndex(name = "idx_entregas_id", def = "{'paradas.entregas.idEntrega': 1}")
public class RutaDeRepartoDocument {
  @Id private UUID id;

  @Indexed private LocalDate fechaOperativa;

  private Boolean iniciada;

  // --- Desnormalización de Camión (Lecturas directas O(1) y Snapshot histórico) ---
  private UUID camionId;
  private String camionPatente;
  private Double camionCapacidadVolumen;
  private Double camionAltura;
  private Double camionCapacidadCarga;

  // --- Desnormalización de Chofer (Lecturas directas O(1) y Snapshot histórico) ---
  @Indexed private UUID choferId;
  private String choferLegajo;
  private String choferNombre;

  // --- Subdocumentos Embebidos (Composición de Agregado) ---
  private List<ParadaDocument> paradas;
}
