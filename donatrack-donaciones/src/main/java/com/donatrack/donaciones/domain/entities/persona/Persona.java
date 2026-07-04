package com.donatrack.donaciones.domain.entities.persona;

import com.donatrack.donaciones.domain.entities.persona.ubicacion.Direccion;
import com.donatrack.donaciones.domain.entities.persona.validador.PersonaValidator;
import com.donatrack.donaciones.domain.entities.roles.Rol;
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
@Getter
@Setter
public abstract class Persona {
  private UUID id;
  private String email;
  private Contacto contacto;
  private Direccion direccion;
  private List<Rol> roles;
  private DocumentoIdentidad documento;

  protected Persona(
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

  public boolean validarDocumentacion(DocumentoIdentidad d) {
    if (d == null) return false;
    return this.documento != null 
        && this.documento.getNumero().equals(d.getNumero()) 
        && this.documento.getTipo() == d.getTipo();
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
