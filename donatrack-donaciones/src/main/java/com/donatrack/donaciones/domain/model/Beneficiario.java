package com.donatrack.donaciones.domain.model;
import java.util.ArrayList;
import java.util.List;

import com.donatrack.donaciones.domain.enums.EstadoDonacion;


public class Beneficiario extends Rol {
    private List<Donacion> donacionesAsignadas;
    private List<Necesidad> necesidadesDeclaradas;

    public Beneficiario() {
        super();
        this.donacionesAsignadas = new ArrayList<>(); 
        this.necesidadesDeclaradas = new ArrayList<>();
    }

    //se registra la necesidad del beneficiario
    public void registrarNecesidad(Necesidad n) { this.necesidadesDeclaradas.add(n); }

    //Se confirma que el beneficiario recibió la donación asignada que le fue asignada
    public void confirmarRecepcion(Donacion d, List<Foto> fotosComprobante) {
        //Verificamos que la donación estuviera asignada a este beneficiario
        if (this.donacionesAsignadas.contains(d)) {
            d.setEstado(EstadoDonacion.ENTREGADA);
            //(Opcional) Las fotos se podrían guardar en la donación como comprobante de que el beneficiario firmó la recepción.
        } else {
            throw new IllegalStateException("Error: La donación no pertenece a este beneficiario.");
        }
    }

    public void verEstadoDonaciones() {
        // Cuando haya base de datos, este método hará un SELECT filtrando por la entidad.
    }

    public void verUbicacionCamion() {
        // Queda vacío. Requerirá integración con API de mapas en el frontend.
    }

    // --- Getters y Setters ---
    public List<Donacion> getDonacionesAsignadas() { return donacionesAsignadas; }
    public void setDonacionesAsignadas(List<Donacion> donacionesAsignadas) {this.donacionesAsignadas = donacionesAsignadas; }
    
    public List<Necesidad> getNecesidadesDeclaradas() {return necesidadesDeclaradas;}
    public void setNecesidadesDeclaradas(List<Necesidad> necesidadesDeclaradas) {this.necesidadesDeclaradas = necesidadesDeclaradas;}
}