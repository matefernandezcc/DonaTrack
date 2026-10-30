package com.donatrack.logistica.infrastructure.adapters.out.persistence.documents;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "choferes")
public class ChoferDocument {
  @Id private UUID id;

  @Indexed(unique = true)
  private String legajo;

  private String nombre;
}
