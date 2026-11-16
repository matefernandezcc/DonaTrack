package com.donatrack.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CrearPersonaDonanteRequest(
    @NotBlank(message = "El tipo de persona es obligatorio") String tipo,
    @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "Formato de correo inválido")
        String email,
    @NotBlank(message = "El tipo de documento es obligatorio") String docTipo,
    @NotBlank(message = "El número de documento es obligatorio") String docNumero,
    String nombre,
    String apellido,
    Integer edad,
    String razonSocial,
    String tipoJuridica,
    String rubro,
    String telefono,
    String whatsapp,
    String medioPredeterminado,
    String calle,
    Double altura,
    String localidad,
    String codigoPostal) {}
