package com.donatrack.donaciones.domain.model.persona;

import com.donatrack.donaciones.domain.model.persona.validadorDatosPersona.PersonaValidator;
import com.donatrack.donaciones.domain.model.persona.ubicacion.Direccion;
import com.donatrack.donaciones.domain.model.roles.Rol;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "tipo"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PersonaHumana.class, name = "HUMANA"),
    @JsonSubTypes.Type(value = PersonaJuridica.class, name = "JURIDICA")
})
public abstract class Persona {
  @Getter @Setter private UUID id;
  @Getter @Setter private String email;
  @Getter @Setter private Contacto contacto;
  @Getter @Setter private Direccion direccion;
  @Getter @Setter private List<Rol> roles;
  @Getter private DocumentoIdentidad documento;

  public Persona(
      String email, Contacto contacto, Direccion direccion, DocumentoIdentidad documento) {
    this.id = UUID.randomUUID();
    this.email = email;
    this.contacto = contacto;
    this.direccion = direccion;
    this.roles = new ArrayList<>();
    this.setDocumento(documento);
  }

  public boolean agregarRol(Rol r) {
    if (this.validarRol(r)) {
      this.roles.add(r);
      return true;
    }
    return false;
  }

  protected abstract boolean validarRol(Rol r);

  public boolean setDocumento(DocumentoIdentidad documento) {
    if (documento == null) {
      // Error: El documento de identidad no puede ser nulo.
      return false;
    }
    this.documento = documento;
    return true;
  }

  public boolean validar(PersonaValidator validador) {
    return validador.validar(this);
  }

  public void actualizarInformacion(Map<String, Object> datosNuevos) {
    // La persona sabe cómo actualizarse a sí misma respetando el encapsulamiento.
    if (datosNuevos.containsKey("email")) {
      this.email = (String) datosNuevos.get("email");
    }
    if (datosNuevos.containsKey("contacto")) {
      this.contacto = (Contacto) datosNuevos.get("contacto");
    }
    if (datosNuevos.containsKey("direccion")) {
      this.direccion = (Direccion) datosNuevos.get("direccion");
    }
    if (datosNuevos.containsKey("documento")) {
      // Usamos el método controlado heredado
      this.setDocumento((DocumentoIdentidad) datosNuevos.get("documento"));
    }
    // Las clases hijas (Humana o Jurídica) podrán sobrescribir este método
  }
}
