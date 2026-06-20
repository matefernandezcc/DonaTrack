package com.donatrack.donaciones.domain.model.persona;

import com.donatrack.donaciones.domain.model.roles.Rol;
import com.donatrack.donaciones.domain.model.persona.ubicacion.Direccion;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonaHumana extends Persona {
  private String nombre;
  private String apellido;
  private int edad;

  public PersonaHumana(
      String email,
      Contacto contacto,
      Direccion direccion,
      DocumentoIdentidad documento,
      String nombre,
      String apellido,
      int edad) {
    // Pasamos el documento al constructor padre
    super(email, contacto, direccion, documento);
    this.nombre = nombre;
    this.apellido = apellido;
    this.edad = edad;
  }

  @Override
  protected boolean validarRol(Rol r) {
    if (!r.esValidoParaHumana()) {
      // Error: Una persona humana no puede asumir este rol.
      return false;
    }
    return true;
  }



  @Override
  public void actualizarInformacion(Map<String, Object> datosNuevos) {
    // Esto ya se encarga de delegar al padre y actualizar de forma segura el documento si viene en
    // el mapa
    super.actualizarInformacion(datosNuevos);

    if (datosNuevos.containsKey("nombre")) {
      this.nombre = (String) datosNuevos.get("nombre");
    }
    if (datosNuevos.containsKey("apellido")) {
      this.apellido = (String) datosNuevos.get("apellido");
    }
    if (datosNuevos.containsKey("edad")) {
      this.edad = (Integer) datosNuevos.get("edad");
    }
  }
}
