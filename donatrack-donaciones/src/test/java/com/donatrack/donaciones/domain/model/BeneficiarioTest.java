package com.donatrack.donaciones.domain.model;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class BeneficiarioTest {

    @Test public void testErrorDonacionNoAsignada(){

        Categoria categoriaPrueba = new Categoria("Ropa", "esta Bien");
        Donacion donacionPrueba = new Donacion(categoriaPrueba);

        List<Foto> fotos = new ArrayList<>();

        Beneficiario beneficiario = new Beneficiario();

        assertThrows(IllegalStateException.class, () -> beneficiario.confirmarRecepcion(donacionPrueba, fotos));
    }

}
