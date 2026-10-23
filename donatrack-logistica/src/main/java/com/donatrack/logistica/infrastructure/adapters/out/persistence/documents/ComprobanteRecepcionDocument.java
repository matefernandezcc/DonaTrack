package com.donatrack.logistica.infrastructure.adapters.out.persistence.documents;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComprobanteRecepcionDocument {
  private LocalDateTime fechaHora;
  private List<String> fotos;
  private String camionPatente;
}
