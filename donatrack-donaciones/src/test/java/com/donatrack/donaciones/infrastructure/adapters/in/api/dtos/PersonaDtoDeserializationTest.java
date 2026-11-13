package com.donatrack.donaciones.infrastructure.adapters.in.api.dtos;

import static org.junit.jupiter.api.Assertions.*;

import com.donatrack.donaciones.domain.entities.enums.TipoPersonaJuridica;
import com.donatrack.donaciones.domain.entities.persona.Persona;
import com.donatrack.donaciones.domain.entities.persona.PersonaJuridica;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PersonaDTO — Deserialización polimórfica HUMANA/JURIDICA")
class PersonaDtoDeserializationTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @DisplayName(
      "Deserializa PersonaJuridicaDTO con tipo='JURIDICA' y tipoJuridica='EMPRESA' sin colisión Jackson")
  void testDeserializaPersonaJuridicaSinColision() throws Exception {
    String json =
        """
        {
          "tipo": "JURIDICA",
          "email": "empresa@ejemplo.org",
          "razonSocial": "Alimentos Sur SRL",
          "tipoJuridica": "EMPRESA",
          "rubro": "Alimentos",
          "documento": {"tipo": "CUIT", "numero": "30-71234567-9"},
          "contacto": {"correoElectronico": "empresa@ejemplo.org", "medioPredeterminado": "CORREO"}
        }
        """;

    PersonaDTO dto = objectMapper.readValue(json, PersonaDTO.class);
    assertInstanceOf(PersonaJuridicaDTO.class, dto);

    PersonaJuridicaDTO juridicaDto = (PersonaJuridicaDTO) dto;
    assertEquals("EMPRESA", juridicaDto.getTipoJuridica());

    Persona domain = PersonaDtoMapper.toDomain(dto);
    assertInstanceOf(PersonaJuridica.class, domain);
    assertEquals(TipoPersonaJuridica.EMPRESA, ((PersonaJuridica) domain).getTipo());
  }
}
