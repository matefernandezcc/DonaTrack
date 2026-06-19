package com.donatrack.donaciones.application.service;

import com.donatrack.donaciones.application.port.in.BienDTO;
import com.donatrack.donaciones.application.port.in.CargaBienesRequestDTO;
import com.donatrack.donaciones.application.port.in.RecepcionDonacionesUseCase;
import com.donatrack.donaciones.domain.model.donacion.Bien;
import com.donatrack.donaciones.domain.model.donacion.Categoria;
import com.donatrack.donaciones.domain.model.donacion.Donacion;
import com.donatrack.donaciones.domain.model.donacion.RecepcionDonacion;
import com.donatrack.donaciones.domain.model.persona.Persona;
import com.donatrack.donaciones.domain.model.roles.Administrador;
import com.donatrack.donaciones.domain.model.roles.Donante;
import com.donatrack.donaciones.domain.repository.DonacionRepository;
import com.donatrack.donaciones.domain.repository.PersonaRepository;
import com.donatrack.donaciones.domain.repository.RecepcionDonacionRepository;
import com.donatrack.donaciones.domain.service.ProcesadorCargaInicial;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecepcionDonacionesService implements RecepcionDonacionesUseCase {

    private final PersonaRepository personaRepository;
    private final ProcesadorCargaInicial procesadorCargaInicial;
    private final DonacionRepository donacionRepository;
    private final RecepcionDonacionRepository recepcionDonacionRepository;

    public RecepcionDonacionesService(
            PersonaRepository personaRepository,
            ProcesadorCargaInicial procesadorCargaInicial,
            DonacionRepository donacionRepository,
            RecepcionDonacionRepository recepcionDonacionRepository) {
        this.personaRepository = personaRepository;
        this.procesadorCargaInicial = procesadorCargaInicial;
        this.donacionRepository = donacionRepository;
        this.recepcionDonacionRepository = recepcionDonacionRepository;
    }

    @Override
    public RecepcionDonacion recibir(CargaBienesRequestDTO requestDTO) {
        // 1. Obtener entidades
        Persona donantePersona = personaRepository.buscarPorId(requestDTO.idDonante())
                .orElseThrow(() -> new IllegalArgumentException("Donante no encontrado"));
        Persona adminPersona = personaRepository.buscarPorId(requestDTO.idAdministrador())
                .orElseThrow(() -> new IllegalArgumentException("Administrador no encontrado"));

        Donante donante = donantePersona.getRoles().stream()
                .filter(r -> r instanceof Donante)
                .map(r -> (Donante) r)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("La persona no tiene rol Donante"));

        Administrador administrador = adminPersona.getRoles().stream()
                .filter(r -> r instanceof Administrador)
                .map(r -> (Administrador) r)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("La persona no tiene rol Administrador"));

        // 2. Mapear DTOs a Entidades de Dominio
        List<Bien> bienesBrutos = requestDTO.bienesBrutos().stream().map(this::mapearBien).collect(Collectors.toList());

        // 3. Procesar Carga Inicial (Dominio orquesta estrategias)
        List<Donacion> donacionesResultantes = procesadorCargaInicial.procesar(bienesBrutos);

        // 4. Crear acta de Recepción (Entidad)
        RecepcionDonacion recepcion = new RecepcionDonacion(donante, administrador, bienesBrutos, donacionesResultantes);

        // 5. Persistir (Orquestación de infraestructura)
        donacionesResultantes.forEach(donacionRepository::guardar);
        recepcionDonacionRepository.guardar(recepcion);

        return recepcion;
    }

    private Bien mapearBien(BienDTO dto) {
        Bien bien = new Bien(
                dto.descripcion(),
                dto.cantidad(),
                dto.unidadMedicion(),
                dto.esUsado(),
                dto.fechaVencimiento()
        );
        // En una app real, buscaríamos la Categoria en un repositorio
        Categoria subcategoriaMock = new Categoria(dto.nombreSubcategoria(), "Subcategoría generada");
        bien.setSubcategoria(subcategoriaMock);
        return bien;
    }
}
