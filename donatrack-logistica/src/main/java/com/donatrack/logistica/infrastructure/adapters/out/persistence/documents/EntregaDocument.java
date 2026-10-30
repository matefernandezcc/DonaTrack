package com.donatrack.logistica.infrastructure.adapters.out.persistence.documents;

import com.donatrack.logistica.domain.entities.entregas.EstadoEntrega;
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
@Document(collection = "entregas")
public class EntregaDocument {
  @Id
  private UUID idEntrega;
  private EstadoEntrega estado;
  private Double pesoEstimado;
  private Double volumenEstimado;
  private ComprobanteRecepcionDocument comprobanteRecepcion;
}
