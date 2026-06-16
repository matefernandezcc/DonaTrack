package com.donatrack.donaciones.domain.model.persona;

import com.donatrack.donaciones.domain.enums.TipoJuridica;
import com.donatrack.donaciones.domain.model.roles.Rol;
import com.donatrack.donaciones.domain.model.roles.Representante;
import com.donatrack.donaciones.domain.model.persona.ubicacion.Direccion;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

public class PersonaJuridica extends Persona {
  @Getter @Setter private String razonSocial;
  @Getter @Setter private TipoJuridica tipo;
  @Getter @Setter private String rubro;
  @Getter @Setter private List<Representante> representantes;

  public PersonaJuridica(
      String email,
      Contacto contacto,
      Direccion direccion,
      DocumentoIdentidad documento,
      String razonSocial,
      TipoJuridica tipo,
      String rubro) {
    // Pasamos el documento al constructor padre
    super(email, contacto, direccion, documento);
    this.razonSocial = razonSocial;
    this.tipo = tipo;
    this.rubro = rubro;
    this.representantes = new ArrayList<>();
  }

  public void agregarRepresentante(Representante r) {
    this.representantes.add(r);
  }

  @Override
  protected boolean validarRol(Rol r) {
    if (!r.esValidoParaJuridica()) {
      // Error: Una persona jurídica no puede asumir este rol.
      return false;
    }
    return true;
  }



  @Override
  public void actualizarInformacion(Map<String, Object> datosNuevos) {
    super.actualizarInformacion(datosNuevos);
    if (datosNuevos.containsKey("razonSocial")) {
      this.razonSocial = (String) datosNuevos.get("razonSocial");
    }
    if (datosNuevos.containsKey("rubro")) {
      this.rubro = (String) datosNuevos.get("rubro");
    }
  }
}
