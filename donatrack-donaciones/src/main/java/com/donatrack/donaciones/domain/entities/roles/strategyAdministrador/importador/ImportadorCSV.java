package com.donatrack.donaciones.domain.entities.roles.strategyAdministrador.importador;

import com.donatrack.donaciones.application.ports.out.PersonaRepository;
import com.donatrack.donaciones.domain.entities.donacion.Archivo;
import com.donatrack.donaciones.domain.entities.persona.Persona;
import com.donatrack.donaciones.domain.entities.persona.PersonaFactory;
import com.donatrack.donaciones.domain.entities.persona.PersonaHumana;
import com.donatrack.donaciones.domain.entities.persona.PersonaJuridica;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportadorCSV implements ImportadorStrategy {

  private static final Logger log = LoggerFactory.getLogger(ImportadorCSV.class);

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
    int creados = 0;
    int actualizados = 0;

    try (BufferedReader br =
        new BufferedReader(
            new InputStreamReader(new ByteArrayInputStream(archivo.getContenido())))) {
      br.readLine(); // Saltar encabezado

      while ((linea = br.readLine()) != null) {
        String[] datos = linea.split(separador);
        if (datos.length < 5) continue;
        String email = datos[4];

        Optional<Persona> personaExistenteOpt = personaRepository.buscarPorEmail(email);

        if (personaExistenteOpt.isPresent()) {
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
          actualizados++;
        } else {
          Persona nuevaPersona = personaFactory.crearDesdeCSV(datos);
          if (nuevaPersona.getRoles().isEmpty()) {
            com.donatrack.donaciones.domain.entities.roles.Donante rolDonante =
                new com.donatrack.donaciones.domain.entities.roles.Donante();
            rolDonante.setFechaAlta(java.time.LocalDate.now());
            nuevaPersona.agregarRol(rolDonante);
          }
          personaRepository.guardar(nuevaPersona);
          creados++;
        }
      }
      log.info(
          "Importación CSV completada: {} donantes creados, {} actualizados",
          creados,
          actualizados);
    } catch (IOException e) {
      log.error("Error al intentar leer el contenido CSV: {}", e.getMessage());
    }
  }
}
