package com.donatrack.logistica.infrastructure.adapters.out.persistence.documents;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "items_planificacion")
public class ItemPlanificacionDocument {
  @Id
  private UUID id;

  private UUID idDonacionOriginal;
  private Double pesoEstimado;
  private Double volumenEstimado;
  private DireccionDocument destino;
}
