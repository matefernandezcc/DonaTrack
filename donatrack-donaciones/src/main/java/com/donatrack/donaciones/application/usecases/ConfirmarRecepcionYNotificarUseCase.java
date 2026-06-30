package com.donatrack.donaciones.application.usecases;

import com.donatrack.common.events.EntregaRealizadaEvent;
import com.donatrack.common.events.NotificacionEntregaExitosaEvent;
import com.donatrack.common.events.NotificacionInicioRutaEvent;
import com.donatrack.donaciones.application.ports.out.DonacionRepository;
import com.donatrack.donaciones.application.ports.out.PersonaRepository;
import com.donatrack.donaciones.application.ports.out.RecepcionDonacionRepository;
import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.donacion.Foto;
import com.donatrack.donaciones.domain.entities.donacion.RecepcionDonacion;
import com.donatrack.donaciones.domain.entities.persona.Contacto;
import com.donatrack.donaciones.domain.entities.persona.Persona;
import com.donatrack.donaciones.domain.entities.roles.Beneficiario;
import com.donatrack.donaciones.domain.entities.roles.Donante;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ConfirmarRecepcionYNotificarUseCase {

    private final DonacionRepository donacionRepository;
    private final RecepcionDonacionRepository recepcionRepository;
    private final PersonaRepository personaRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ConfirmarRecepcionYNotificarUseCase(DonacionRepository donacionRepository,
                                               RecepcionDonacionRepository recepcionRepository,
                                               PersonaRepository personaRepository,
                                               ApplicationEventPublisher eventPublisher) {
        this.donacionRepository = donacionRepository;
        this.recepcionRepository = recepcionRepository;
        this.personaRepository = personaRepository;
        this.eventPublisher = eventPublisher;
    }

    public void procesar(EntregaRealizadaEvent event) {
        Optional<Donacion> donacionOpt = donacionRepository.buscarPorId(event.getIdDonacion());
        if (donacionOpt.isEmpty()) return;

        Donacion donacion = donacionOpt.get();
        Beneficiario beneficiario = donacion.getEntidadAsignada();

        if (beneficiario != null) {
            // Convertimos las URLs de fotos a objetos Foto del dominio
            List<Foto> fotosComprobante = event.getFotos().stream()
                .map(url -> new Foto("Comprobante de entrega", url))
                .toList();

            // Actualizamos el estado a ENTREGADA mediante la lógica de dominio
            boolean confirmado = beneficiario.confirmarRecepcion(donacion, fotosComprobante);
            if (confirmado) {
                donacionRepository.guardar(donacion);
            }
        }

        List<NotificacionInicioRutaEvent.ContactoInfo> contactosDonantes = extraerContactosDonante(donacion);
        List<NotificacionInicioRutaEvent.ContactoInfo> contactosEntidades = extraerContactosEntidad(beneficiario);

        NotificacionEntregaExitosaEvent notificacionEvent = new NotificacionEntregaExitosaEvent(
                event.getIdDonacion(),
                contactosDonantes,
                contactosEntidades,
                event.getFechaHora(),
                event.getPatenteCamion(),
                event.getFotos()
        );

        eventPublisher.publishEvent(notificacionEvent);
    }

    private List<NotificacionInicioRutaEvent.ContactoInfo> extraerContactosDonante(Donacion donacion) {
        List<NotificacionInicioRutaEvent.ContactoInfo> contactos = new ArrayList<>();
        Optional<RecepcionDonacion> recepcionOpt = recepcionRepository.buscarPorIdDonacion(donacion.getId());
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
            infos.add(new NotificacionInicioRutaEvent.ContactoInfo(contacto.getCorreoElectronico(), "EMAIL", rol));
        }
        return infos;
    }
}
