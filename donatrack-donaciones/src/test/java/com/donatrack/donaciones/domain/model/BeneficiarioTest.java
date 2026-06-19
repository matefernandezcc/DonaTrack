package com.donatrack.donaciones.domain.model;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import com.donatrack.donaciones.domain.model.donacion.Subcategoria;
import com.donatrack.donaciones.domain.model.donacion.Donacion;
import com.donatrack.donaciones.domain.model.donacion.Foto;
import com.donatrack.donaciones.domain.model.roles.Beneficiario;

public class BeneficiarioTest {

  @Test
  public void testErrorDonacionNoAsignada() {

    Subcategoria categoriaPrueba = new Subcategoria("Ropa", "esta Bien");
    Donacion donacionPrueba = new Donacion(categoriaPrueba);

    List<Foto> fotos = new ArrayList<>();

    Beneficiario beneficiario = new Beneficiario();

    org.junit.jupiter.api.Assertions.assertFalse(beneficiario.confirmarRecepcion(donacionPrueba, fotos));
  }
}
