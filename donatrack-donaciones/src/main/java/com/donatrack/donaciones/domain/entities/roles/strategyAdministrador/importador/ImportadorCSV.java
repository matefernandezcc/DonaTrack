package com.donatrack.donaciones.domain.entities.roles.strategyAdministrador.importador;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import com.donatrack.donaciones.domain.entities.persona.PersonaFactory;
import com.donatrack.donaciones.domain.entities.persona.Persona;
import com.donatrack.donaciones.domain.entities.persona.PersonaHumana;
import com.donatrack.donaciones.domain.entities.persona.PersonaJuridica;
import com.donatrack.donaciones.application.ports.out.PersonaRepository;
import com.donatrack.donaciones.domain.entities.donacion.Archivo;

public class ImportadorCSV implements ImportadorStrategy {
  
  private final PersonaRepository personaRepository;
  private final PersonaFactory personaFactory = new PersonaFactory();

  public ImportadorCSV(PersonaRepository personaRepository) {
    this.personaRepository = personaRepository;
  }

  public List<Persona> getRegistroPersonas() {
    return this.personaRepository.obtenerTodas();
  }

  @Override
  public void importar(Archivo archivo) {
    if (archivo == null || archivo.getContenido() == null) {
        return;
    }

    String linea;
    String separador = ",";

    try (BufferedReader br = new BufferedReader(
            new InputStreamReader(new ByteArrayInputStream(archivo.getContenido())))) {
      br.readLine(); // Saltar encabezado

      while ((linea = br.readLine()) != null) {
        String[] datos = linea.split(separador);
        if (datos.length < 5) continue;
        String email = datos[4];

        Optional<Persona> personaExistenteOpt = personaRepository.buscarPorEmail(email);

        if (personaExistenteOpt.isPresent()) {
          System.out.println("El email " + email + " ya existe. Actualizando información...");
          Persona personaExistente = personaExistenteOpt.get();
          Persona datosNuevos = personaFactory.crearDesdeCSV(datos);
          
          java.util.Map<String, Object> map = new java.util.HashMap<>();
          map.put("contacto", datosNuevos.getContacto());
          map.put("documento", datosNuevos.getDocumento());
          if (datosNuevos instanceof PersonaHumana humana) {
            map.put("nombre", humana.getNombre());
            map.put("apellido", humana.getApellido());
            map.put("edad", humana.getEdad());
          } else if (datosNuevos instanceof PersonaJuridica juridica) {
            map.put("razonSocial", juridica.getRazonSocial());
            map.put("rubro", juridica.getRubro());
          }
          personaExistente.actualizarInformacion(map);
          personaRepository.guardar(personaExistente);
        } else {
          System.out.println("Creando nuevo donante para: " + email);
          Persona nuevaPersona = personaFactory.crearDesdeCSV(datos);
          personaRepository.guardar(nuevaPersona);
          System.out.println("Enviando credenciales de acceso a " + email);
        }
      }
    } catch (IOException e) {
      System.err.println("Error al intentar leer el contenido CSV: " + e.getMessage());
    }
  }
}
