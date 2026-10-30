package com.donatrack.logistica.infrastructure.adapters.out.persistence.documents;

import com.donatrack.logistica.domain.entities.planificacion.EstadoPlanificacion;
import java.time.LocalDateTime;
import java.util.List;
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
@Document(collection = "solicitudes_planificacion")
public class SolicitudPlanificacionDocument {
  @Id
  private UUID id;

  private LocalDateTime fechaSolicitud;
  private EstadoPlanificacion estado;
  private List<UUID> idsDonaciones;
}
