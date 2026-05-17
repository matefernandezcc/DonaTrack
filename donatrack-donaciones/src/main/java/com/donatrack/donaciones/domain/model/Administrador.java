package com.donatrack.donaciones.domain.model;
//import com.donatrack.donaciones.Donacion;

public class Administrador extends Rol {

    public Administrador() {
        super();
    }

    // --- Métodos de comportamiento ---

    public void asociarDonacion(Donacion d, Persona p) {
        // Lógica para vincular los bienes ingresados físicamente a un donante registrado
    }

    public void actualizarDonacionesVencidas() {
        // Lógica para recorrer el depósito y dar de baja bienes perecederos vencidos
    }

    public void asignarDonacionFinal(Donacion d, Beneficiario e) {
        // Lógica para entregar formalmente la donación a la entidad que lo necesite
    }

    public void administrarCamiones() {
        // Lógica para gestionar las rutas y la flota de transporte
    }

    public void verRankingMensual() {
        // Lógica para generar el ranking de los donantes con más impacto
    }
}