package com.donatrack.donaciones.domain.entities;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import com.donatrack.donaciones.domain.entities.donacion.Subcategoria;
import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.donacion.Foto;
import com.donatrack.donaciones.domain.entities.roles.Beneficiario;

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
