package com.donatrack.donaciones.domain.model;
import com.donatrack.donaciones.domain.enums.MedioContacto;
import java.time.LocalDate;
import java.util.UUID;

public class Notificacion {
    private UUID id;
    private String mensaje;
    private LocalDate fechaEnvio;
    private MedioContacto medio;
    private boolean leida;


    public Notificacion(String mensaje, MedioContacto medio) {
        this.id = UUID.randomUUID();
        this.mensaje = mensaje;
        this.medio = medio;
        this.fechaEnvio = LocalDate.now(); 
        this.leida = false;
    }

    public void marcarComoLeida() {this.leida = true;}

    // --- Getters y Setters ---
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public LocalDate getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDate fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public MedioContacto getMedio() { return medio; }
    public void setMedio(MedioContacto medio) { this.medio = medio; }

    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }
}