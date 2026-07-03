package com.donatrack.donaciones.domain.entities.persona;

import com.donatrack.donaciones.domain.entities.roles.Rol;
import com.donatrack.donaciones.domain.entities.roles.Representante;
import com.donatrack.donaciones.domain.entities.enums.TipoPersonaJuridica;
import com.donatrack.donaciones.domain.entities.persona.ubicacion.Direccion;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonaJuridica extends Persona {
  private String razonSocial;
  private TipoPersonaJuridica tipo;
  private String rubro;
  private List<Representante> representantes;

  public PersonaJuridica(
      String email,
      Contacto contacto,
      Direccion direccion,
      DocumentoIdentidad documento,
      String razonSocial,
      TipoPersonaJuridica tipo,
      String rubro) {
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
