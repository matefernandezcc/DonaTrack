package com.donatrack.donaciones.application.usecases;

import com.donatrack.common.events.EntregaNoSatisfactoriaEvent;
import com.donatrack.common.events.NotificacionEntregaFallidaEvent;
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
import com.donatrack.donaciones.domain.entities.roles.Rol;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProcesarFallaEntregaYNotificarUseCase {

    private final DonacionRepository donacionRepository;
    private final DonacionOriginalRepository recepcionRepository;
    private final PersonaRepository personaRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ProcesarFallaEntregaYNotificarUseCase(DonacionRepository donacionRepository,
                                                 DonacionOriginalRepository recepcionRepository,
                                                 PersonaRepository personaRepository,
                                                 ApplicationEventPublisher eventPublisher) {
        this.donacionRepository = donacionRepository;
        this.recepcionRepository = recepcionRepository;
        this.personaRepository = personaRepository;
        this.eventPublisher = eventPublisher;
    }

    public void procesar(EntregaNoSatisfactoriaEvent event) {
        Optional<Donacion> donacionOpt = donacionRepository.buscarPorId(event.getIdDonacion());
        if (donacionOpt.isEmpty()) return;

        Donacion donacion = donacionOpt.get();
        
        // Cambiar estado a ENTREGA_FALLIDA
        donacion.cambiarEstado(EstadoDonacion.ENTREGA_FALLIDA, event.getMotivo(), null);
        donacionRepository.guardar(donacion);

        Beneficiario beneficiario = donacion.getEntidadAsignada();

        List<NotificacionInicioRutaEvent.ContactoInfo> contactosDonantes = extraerContactosDonante(donacion);
        List<NotificacionInicioRutaEvent.ContactoInfo> contactosEntidades = extraerContactosEntidad(beneficiario);
        List<NotificacionInicioRutaEvent.ContactoInfo> contactosAdmins = extraerContactosAdministradores();

        NotificacionEntregaFallidaEvent notificacionEvent = new NotificacionEntregaFallidaEvent(
                event.getIdDonacion(),
                event.getMotivo(),
                event.isPuedeReplanificarse(),
                contactosDonantes,
                contactosEntidades,
                contactosAdmins
        );

        eventPublisher.publishEvent(notificacionEvent);
    }

    private List<NotificacionInicioRutaEvent.ContactoInfo> extraerContactosDonante(Donacion donacion) {
        List<NotificacionInicioRutaEvent.ContactoInfo> contactos = new ArrayList<>();
        Optional<DonacionOriginal> recepcionOpt = recepcionRepository.buscarPorIdDonacion(donacion.getId());
        if (recepcionOpt.isPresent()) {
            Donante donante = recepcionOpt.get().getDonante();
            if (donante != null) {
                Optional<Persona> personaDonante = personaRepository.buscarPorRolId(donante.getId());
                if (personaDonante.isPresent()) {
                    contactos.addAll(extraerContactos(personaDonante.get(), "DONANTE"));
                }
            }
        }
        return contactos;
    }

    private List<NotificacionInicioRutaEvent.ContactoInfo> extraerContactosEntidad(Beneficiario beneficiario) {
        List<NotificacionInicioRutaEvent.ContactoInfo> contactos = new ArrayList<>();
        if (beneficiario != null) {
            Optional<Persona> personaBeneficiario = personaRepository.buscarPorRolId(beneficiario.getId());
            if (personaBeneficiario.isPresent()) {
                contactos.addAll(extraerContactos(personaBeneficiario.get(), "ENTIDAD_BENEFICIARIA"));
            }
        }
        return contactos;
    }

    private List<NotificacionInicioRutaEvent.ContactoInfo> extraerContactosAdministradores() {
        List<NotificacionInicioRutaEvent.ContactoInfo> contactos = new ArrayList<>();
        List<Persona> todasLasPersonas = personaRepository.obtenerTodas();
        for (Persona persona : todasLasPersonas) {
            if (persona.getEmail() != null && persona.getEmail().toLowerCase().contains("admin")) {
                contactos.addAll(extraerContactos(persona, "ADMINISTRADOR"));
            }
        }
        return contactos;
    }

    private List<NotificacionInicioRutaEvent.ContactoInfo> extraerContactos(Persona persona, String rolStr) {
        List<NotificacionInicioRutaEvent.ContactoInfo> infos = new ArrayList<>();
        Contacto contacto = persona.getContacto();
        if (contacto == null) return infos;

        if (contacto.getMedioPredeterminado() != null) {
            switch (contacto.getMedioPredeterminado()) {
                case CORREO:
                    if (contacto.getCorreoElectronico() != null && !contacto.getCorreoElectronico().isEmpty()) {
                        infos.add(new NotificacionInicioRutaEvent.ContactoInfo(contacto.getCorreoElectronico(), "EMAIL", rolStr));
                    }
                    break;
                case TELEFONO:
                    if (contacto.getTelefono() != null && !contacto.getTelefono().isEmpty()) {
                        infos.add(new NotificacionInicioRutaEvent.ContactoInfo(contacto.getTelefono(), "SMS", rolStr));
                    }
                    break;
                case WHATSAPP:
                    if (contacto.getWhatsapp() != null && !contacto.getWhatsapp().isEmpty()) {
                        infos.add(new NotificacionInicioRutaEvent.ContactoInfo(contacto.getWhatsapp(), "WHATSAPP", rolStr));
                    }
                    break;
            }
        } else if (contacto.getCorreoElectronico() != null && !contacto.getCorreoElectronico().isEmpty()) {
            infos.add(new NotificacionInicioRutaEvent.ContactoInfo(contacto.getCorreoElectronico(), "EMAIL", rolStr));
        }
        return infos;
    }
}
