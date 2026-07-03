package com.donatrack.donaciones.application.usecases;

import com.donatrack.donaciones.application.ports.in.BienDTO;
import com.donatrack.donaciones.application.ports.in.CargaBienesRequestDTO;
import com.donatrack.donaciones.application.ports.in.RecepcionDonacionesUseCase;
import com.donatrack.donaciones.domain.entities.donacion.Bien;
import com.donatrack.donaciones.domain.entities.donacion.Subcategoria;
import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.donacion.DonacionOriginal;
import com.donatrack.donaciones.domain.entities.persona.Persona;
import com.donatrack.donaciones.domain.entities.roles.Administrador;
import com.donatrack.donaciones.domain.entities.roles.Donante;
import com.donatrack.donaciones.application.ports.out.DonacionRepository;
import com.donatrack.donaciones.application.ports.out.PersonaRepository;
import com.donatrack.donaciones.application.ports.out.DonacionOriginalRepository;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;
import java.util.List;

@Service
public class RecepcionDonacionesService implements RecepcionDonacionesUseCase {

    private final PersonaRepository personaRepository;
    private final ProcesadorCargaInicial procesadorCargaInicial;
    private final DonacionRepository donacionRepository;
    private final DonacionOriginalRepository recepcionDonacionRepository;

    public RecepcionDonacionesService(
            PersonaRepository personaRepository,
            ProcesadorCargaInicial procesadorCargaInicial,
            DonacionRepository donacionRepository,
            DonacionOriginalRepository recepcionDonacionRepository) {
        this.personaRepository = personaRepository;
        this.procesadorCargaInicial = procesadorCargaInicial;
        this.donacionRepository = donacionRepository;
        this.recepcionDonacionRepository = recepcionDonacionRepository;
    }

    @Override
    public DonacionOriginal recibir(CargaBienesRequestDTO requestDTO) {
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

        // 3. Crear acta de Recepción (Entidad)
        DonacionOriginal recepcion = new DonacionOriginal("Carga de donación original", donante, requestDTO.idAdministrador().toString());

        // 4. Procesar Carga Inicial delegando en la entidad DonacionOriginal
        recepcion.segmentarBienes(bienesBrutos, procesadorCargaInicial);

        // 5. Persistir
        recepcion.getDonacionesSegmentadas().forEach(donacionRepository::guardar);
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
        Subcategoria subcategoriaMock = new Subcategoria(dto.nombreSubcategoria(), "Subcategoría generada");
        bien.setSubcategoria(subcategoriaMock);
        return bien;
    }
}
