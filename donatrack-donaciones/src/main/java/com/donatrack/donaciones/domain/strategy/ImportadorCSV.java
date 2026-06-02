package com.donatrack.donaciones.domain.strategy;

import com.donatrack.donaciones.domain.enums.MedioContacto;
import com.donatrack.donaciones.domain.enums.TipoDocumento;
import com.donatrack.donaciones.domain.model.Contacto;
import com.donatrack.donaciones.domain.model.DocumentoIdentidad;
import com.donatrack.donaciones.domain.model.Persona;
import com.donatrack.donaciones.domain.model.PersonaHumana;
import com.donatrack.donaciones.domain.model.PersonaJuridica;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import com.donatrack.donaciones.domain.factory.PersonaFactory;
import lombok.Getter;

public class ImportadorCSV implements ImportadorStrategy {
  // Simulamos la base de datos (el "RepositorioDonantes") con una lista en memoria
  @Getter private List<Persona> registroPersonas;
  private final PersonaFactory personaFactory = new PersonaFactory();

  public ImportadorCSV(List<Persona> registroPersonas) {
    this.registroPersonas = registroPersonas;
  }

  @Override
  public void importar(String rutaArchivo) {
    String linea;
    String separador = ","; // Formato CSV separado por comas

    try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
      br.readLine();

      while ((linea = br.readLine()) != null) {
        // Según el formato: TipoPersona, TipoDoc, Documento, Nombre/Razón Social, Email, Teléfono
        String[] datos = linea.split(separador);
        String email = datos[4];

        // 1. Validar si el registro ya existe buscando por correo electrónico
        Persona personaExistente = buscarPorEmail(email);

        if (personaExistente != null) {
          // 2. Si ya existe, se deberá actualizar su información
          System.out.println("El email " + email + " ya existe. Actualizando información...");
        } else {
          // 3. En caso contrario, crearlo y enviarle sus credenciales de acceso
          System.out.println("Creando nuevo donante para: " + email);
          
          Persona nuevaPersona = personaFactory.crearDesdeCSV(datos);
          this.registroPersonas.add(nuevaPersona);
          
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
