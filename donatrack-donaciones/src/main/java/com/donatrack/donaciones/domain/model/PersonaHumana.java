package com.donatrack.donaciones.domain.model;
import com.donatrack.donaciones.domain.enums.TipoDocumento;
import java.util.Map;

public class PersonaHumana extends Persona {
    private String nombre;
    private String apellido;
    private TipoDocumento tipoDocumento;
    private String documento;
    private int edad;

    public PersonaHumana(String email, Contacto contacto, Direccion direccion, String nombre, String apellido, TipoDocumento tipoDocumento, String documento, int edad) {
        super(email, contacto, direccion);
        this.nombre = nombre;
        this.apellido = apellido;
        this.tipoDocumento = tipoDocumento;
        this.documento = documento;
        this.edad = edad;
    }

    @Override protected void validarRol(Rol r) {
        // Regla de Negocio: Tira excepción si el rol es Beneficiario
        if (r instanceof Beneficiario) {
            throw new IllegalArgumentException("Error: Una persona humana no puede ser una Entidad Beneficiaria.");
        }
    }


    @Override public void actualizarInformacion(Map<String, Object> datosNuevos) {
        super.actualizarInformacion(datosNuevos); 
        
        if (datosNuevos.containsKey("nombre")) {
            this.nombre = (String) datosNuevos.get("nombre");
        }
        if (datosNuevos.containsKey("apellido")) {
            this.apellido = (String) datosNuevos.get("apellido");
        }
        if (datosNuevos.containsKey("documento")) {
            this.documento = (String) datosNuevos.get("documento");
        }
        if (datosNuevos.containsKey("edad")) {
            this.edad = (Integer) datosNuevos.get("edad");
        }
    }

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public TipoDocumento getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(TipoDocumento tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }
}
