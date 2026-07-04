package com.donatrack.donaciones.application.usecases;

import com.donatrack.common.events.NotificacionInicioRutaEvent;
import com.donatrack.donaciones.application.ports.out.DonacionRepository;
import com.donatrack.donaciones.application.ports.out.PersonaRepository;
import com.donatrack.donaciones.application.ports.out.DonacionOriginalRepository;
import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.donacion.DonacionOriginal;
import com.donatrack.donaciones.domain.entities.enums.EstadoDonacion;
import com.donatrack.donaciones.domain.entities.persona.Contacto;
import com.donatrack.donaciones.domain.entities.persona.Persona;
import com.donatrack.donaciones.domain.entities.roles.Beneficiario;
import com.donatrack.donaciones.domain.entities.roles.Donante;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProcesarInicioRutaUseCase {

    private final DonacionRepository donacionRepository;
    private final DonacionOriginalRepository recepcionRepository;
    private final PersonaRepository personaRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ProcesarInicioRutaUseCase(DonacionRepository donacionRepository, 
                                     DonacionOriginalRepository recepcionRepository,
                                     PersonaRepository personaRepository,
                                     ApplicationEventPublisher eventPublisher) {
        this.donacionRepository = donacionRepository;
        this.recepcionRepository = recepcionRepository;
        this.personaRepository = personaRepository;
        this.eventPublisher = eventPublisher;
    }

    public void procesar(UUID rutaId, String patenteCamion, String nombreChofer, List<UUID> idsDonaciones) {
        List<NotificacionInicioRutaEvent.ContactoInfo> contactosDonantes = new ArrayList<>();
        List<NotificacionInicioRutaEvent.ContactoInfo> contactosEntidades = new ArrayList<>();

        for (UUID idDonacion : idsDonaciones) {
            Optional<Donacion> donacionOpt = donacionRepository.buscarPorId(idDonacion);
            if (donacionOpt.isEmpty()) continue;

            Donacion donacion = donacionOpt.get();
            // 1. Update State
            donacion.cambiarEstado(EstadoDonacion.EN_TRASLADO, "Ruta iniciada por chofer " + nombreChofer, null);
            donacionRepository.guardar(donacion);

            // 2. Extract Beneficiary Contacts
            Beneficiario beneficiario = donacion.getEntidadAsignada();
            if (beneficiario != null) {
                Optional<Persona> personaBeneficiario = personaRepository.buscarPorRolId(beneficiario.getId());
                if (personaBeneficiario.isPresent()) {
                    contactosEntidades.addAll(extraerContactos(personaBeneficiario.get(), "ENTIDAD_BENEFICIARIA"));
                }
            }

            // 3. Extract Donor Contacts
            Optional<DonacionOriginal> recepcionOpt = recepcionRepository.buscarPorIdDonacion(idDonacion);
            if (recepcionOpt.isPresent()) {
                Donante donante = recepcionOpt.get().getDonante();
                if (donante != null) {
                    Optional<Persona> personaDonante = personaRepository.buscarPorRolId(donante.getId());
                    if (personaDonante.isPresent()) {
                        contactosDonantes.addAll(extraerContactos(personaDonante.get(), "DONANTE"));
                    }
                }
            }
        }

        // Deduplicate contacts
        contactosDonantes = contactosDonantes.stream().distinct().collect(Collectors.toList());
        contactosEntidades = contactosEntidades.stream().distinct().collect(Collectors.toList());

        // 4. Publish Event
        String linkMap = "http://mapa.donatrack.com/ruta/" + rutaId;
        NotificacionInicioRutaEvent event = new NotificacionInicioRutaEvent(
                rutaId, patenteCamion, nombreChofer, contactosDonantes, contactosEntidades, linkMap);
        
        eventPublisher.publishEvent(event);
    }

    private List<NotificacionInicioRutaEvent.ContactoInfo> extraerContactos(Persona persona, String rol) {
        List<NotificacionInicioRutaEvent.ContactoInfo> infos = new ArrayList<>();
        Contacto contacto = persona.getContacto();
        if (contacto == null) return infos;
        
        if (contacto.getMedioPredeterminado() != null) {
            switch (contacto.getMedioPredeterminado()) {
                case CORREO:
                    if (contacto.getCorreoElectronico() != null && !contacto.getCorreoElectronico().isEmpty()) {
                        infos.add(new NotificacionInicioRutaEvent.ContactoInfo(contacto.getCorreoElectronico(), "EMAIL", rol));
                    }
                    break;
                case TELEFONO:
                    if (contacto.getTelefono() != null && !contacto.getTelefono().isEmpty()) {
                        infos.add(new NotificacionInicioRutaEvent.ContactoInfo(contacto.getTelefono(), "SMS", rol));
                    }
                    break;
                case WHATSAPP:
                    if (contacto.getWhatsapp() != null && !contacto.getWhatsapp().isEmpty()) {
                        infos.add(new NotificacionInicioRutaEvent.ContactoInfo(contacto.getWhatsapp(), "WHATSAPP", rol));
                    }
                    break;
            }
        } else if (contacto.getCorreoElectronico() != null && !contacto.getCorreoElectronico().isEmpty()) {
            // Default fallback
            infos.add(new NotificacionInicioRutaEvent.ContactoInfo(contacto.getCorreoElectronico(), "EMAIL", rol));
        }
        return infos;
    }
}
