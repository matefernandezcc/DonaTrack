package com.donatrack.donaciones.domain.model;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class BeneficiarioTest {

  @Test
  public void testErrorDonacionNoAsignada() {

    Categoria categoriaPrueba = new Categoria("Ropa", "esta Bien");
    Donacion donacionPrueba = new Donacion(categoriaPrueba);

    List<Foto> fotos = new ArrayList<>();

    Beneficiario beneficiario = new Beneficiario();

    org.junit.jupiter.api.Assertions.assertFalse(beneficiario.confirmarRecepcion(donacionPrueba, fotos));
  }
}
