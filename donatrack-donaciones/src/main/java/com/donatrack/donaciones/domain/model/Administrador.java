package com.donatrack.donaciones.domain.model;
import com.donatrack.donaciones.domain.enums.EstadoDonacion;
import java.util.List;

public class Administrador extends Rol {
    private Deposito depositoAsignado;
    private AsignacionStrategy estrategiaAsignacion;
    private ImportadorStrategy estrategiaImportador;

    public Administrador(Deposito depositoAsignado) {
        super();
        this.depositoAsignado = depositoAsignado;
    }

    public void importarDonantesMasivos(String rutaArchivo) {
        if (this.estrategiaImportador != null) {
            this.estrategiaImportador.importar(rutaArchivo); 
        } else {
            throw new IllegalStateException("Error: Primero debe asignarle una estrategia de importación al Administrador.");
        }
    }

    public List<Donacion> obtenerRecomendacionAsignacion(List<Donacion> donacionesDisponibles, List<Necesidad> necesidadesDeclaradas) {
        if (this.estrategiaAsignacion != null) {
            return this.estrategiaAsignacion.recomendarNecesidades(donacionesDisponibles, necesidadesDeclaradas);
        } else {
            throw new IllegalStateException("Error: Primero debe asignarle una estrategia de asignación al Administrador.");
        }
    }

    public void asignarDonacionFinal(Donacion d, Beneficiario b) {
        b.getDonacionesAsignadas().add(d);

        HistorialEstado nuevoHistorial = new HistorialEstado(EstadoDonacion.ASIGNADA,
            "Donación asignada a entidad por administrador.", this);

        d.registrarCambioEstado(nuevoHistorial);
    }

    public void actualizarDonacionesVencidas() {
        if (this.depositoAsignado != null) {
            this.depositoAsignado.auditarVencidos();
        }
    }

    public void asociarDonacion(Donacion d, Donante donante) {
        donante.agregarDonacion(d); 
    }

    public void administrarCamiones() {
        // Queda vacío. Requerirá interfaz gráfica de gestión de camiones.
    }

    public void verRankingMensual() {
        // Queda vacío. Requerirá motor de base de datos para procesar métricas de los donantes.
    }

    // --- Getters y Setters ---
    public Deposito getDepositoAsignado() { return depositoAsignado; }
    public void setDepositoAsignado(Deposito depositoAsignado) { this.depositoAsignado = depositoAsignado; }

    public AsignacionStrategy getEstrategiaAsignacion() { return estrategiaAsignacion; }
    public void setEstrategiaAsignacion(AsignacionStrategy estrategiaAsignacion) { this.estrategiaAsignacion = estrategiaAsignacion; }

    public ImportadorStrategy getEstrategiaImportador() { return estrategiaImportador; }
    public void setEstrategiaImportador(ImportadorStrategy estrategiaImportador) { this.estrategiaImportador = estrategiaImportador; }
}