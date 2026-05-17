package com.donatrack.donaciones.domain.model;
//import com.donatrack.donaciones.Donacion;
//import com.donatrack.necesidades.Necesidad;
import java.util.ArrayList;
import java.util.List;

public class Beneficiario extends Rol {

    private List<Donacion> donacionesAsignadas;

    public Beneficiario() {
        super();
        // ¡Clave! Inicializamos la lista vacía para evitar errores de referencia nula
        this.donacionesAsignadas = new ArrayList<>(); 
    }

    // --- Métodos de comportamiento ---

    public void registrarNecesidad(Necesidad n) {
        // Lógica para dar de alta una nueva necesidad (recurrente o extraordinaria)
    }

    public void confirmarRecepcion(Donacion d, List<String> fotos) {
        // Lógica para asentar que los bienes llegaron, guardando las fotos de comprobante
    }

    public void verEstadoDonaciones() {
        // Lógica para listar y revisar en qué estado se encuentran las donaciones que espera
    }

    public void verUbicacionCamion() {
        // Lógica para ver por dónde va el camión que trae sus donaciones
    }

    // --- Getters y Setters ---

    public List<Donacion> getDonacionesAsignadas() {
        return donacionesAsignadas;
    }

    public void setDonacionesAsignadas(List<Donacion> donacionesAsignadas) {
        this.donacionesAsignadas = donacionesAsignadas;
    }
}