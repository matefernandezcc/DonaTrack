package com.donatrack.donaciones.domain.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import com.donatrack.donaciones.domain.enums.MedioContacto;
import com.donatrack.donaciones.domain.enums.TipoDocumento;

import lombok.Getter;
public class ImportadorCSV implements ImportadorStrategy {
    //Simulamos la base de datos (el "RepositorioDonantes") con una lista en memoria
    @Getter private List<Persona> registroPersonas;

    public ImportadorCSV(List<Persona> registroPersonas) {
        this.registroPersonas = registroPersonas;
    }

    @Override public void importar(String rutaArchivo) {
        String linea;
        String separador = ","; // Formato CSV separado por comas

        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            br.readLine();

            while ((linea = br.readLine()) != null) {
                // Según el formato: TipoPersona, TipoDoc, Documento, Nombre/Razón Social, Email, Teléfono
                String[] datos = linea.split(separador);

                String tipoPersona = datos[0];
                String tipoDoc = datos[1];
                String documento = datos[2];
                String nombreRazonSocial = datos[3];
                String email = datos[4];
                String telefono = datos[5];

                // 1. Validar si el registro ya existe buscando por correo electrónico
                Persona personaExistente = buscarPorEmail(email);

                if (personaExistente != null) {
                    // 2. Si ya existe, se deberá actualizar su información
                    System.out.println("El email " + email + " ya existe. Actualizando información...");
                    // personaExistente.actualizarInformacion(nuevosDatos);
                    
                } else {
                    // 3. En caso contrario, crearlo y enviarle sus credenciales de acceso
                    System.out.println("Creando nuevo donante para: " + email);
                    
                    if(tipoPersona == "HUMANA"){
                        PersonaHumana nuevaPersona = new PersonaHumana(
                            email,
                            new Contacto(email, telefono, null, MedioContacto.CORREO),
                            null, // Dirección no proporcionada en el CSV
                            nombreRazonSocial,
                            null, // Apellido se debe separar del nombre completo
                            TipoDocumento.valueOf(tipoDoc.toUpperCase()),
                            documento,
                            0 // Edad no proporcionada en el CSV
                        );
                        this.registroPersonas.add(nuevaPersona);
                    }else{
                        PersonaJuridica nuevaPersona = new PersonaJuridica(
                            email,
                            new Contacto(email, telefono, null, MedioContacto.CORREO),
                            null, // Dirección no proporcionada en el CSV
                            nombreRazonSocial,
                            TipoDocumento.valueOf(tipoDoc.toUpperCase()),
                            documento,
                            null, // Tipo de persona jurídica no proporcionado en el CSV
                            null  // Rubro no proporcionado en el CSV
                        );
                        this.registroPersonas.add(nuevaPersona);
                    }
                    System.out.println("Enviando credenciales de acceso a " + email);
                }
            }
        } catch (IOException e) {
            System.err.println("Error al intentar leer el archivo CSV: " + e.getMessage());
        }
    }

    // Método de soporte para buscar en nuestra lista en memoria
    private Persona buscarPorEmail(String email) {
        for (Persona p : this.registroPersonas) {
            if (p.getEmail().equalsIgnoreCase(email.trim())) {
                return p;
            }
        }
        return null;
    }
}