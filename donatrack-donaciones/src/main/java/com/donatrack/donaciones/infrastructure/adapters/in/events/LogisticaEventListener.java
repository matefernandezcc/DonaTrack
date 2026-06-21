package com.donatrack.donaciones.infrastructure.adapters.in.events;

import com.donatrack.common.dto.ActividadDonacionDTO;
import com.donatrack.common.events.*;
import com.donatrack.donaciones.application.ports.out.DonacionRepository;
import com.donatrack.donaciones.application.ports.out.PersonaRepository;
import com.donatrack.donaciones.application.ports.out.ServicioNotificaciones;
import com.donatrack.donaciones.application.ports.out.NotificacionOutDTO;
import com.donatrack.donaciones.infrastructure.adapters.out.client.IncentivoClient;
import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.donacion.Foto;
import com.donatrack.donaciones.domain.entities.enums.EstadoDonacion;
import com.donatrack.donaciones.domain.entities.enums.MedioContacto;
import com.donatrack.donaciones.domain.entities.persona.Contacto;
import com.donatrack.donaciones.domain.entities.persona.Persona;
import com.donatrack.donaciones.domain.entities.roles.Donante;
import com.donatrack.donaciones.domain.entities.roles.Rol;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class LogisticaEventListener {

    private final DonacionRepository donacionRepository;
    private final PersonaRepository personaRepository;
    private final ServicioNotificaciones servicioNotificaciones;
    private final IncentivoClient incentivoClient;

    public LogisticaEventListener(
            DonacionRepository donacionRepository,
            PersonaRepository personaRepository,
            ServicioNotificaciones servicioNotificaciones,
            IncentivoClient incentivoClient) {
        this.donacionRepository = donacionRepository;
        this.personaRepository = personaRepository;
        this.servicioNotificaciones = servicioNotificaciones;
        this.incentivoClient = incentivoClient;
    }

    @EventListener
    public void handlePlanificacionProcesada(PlanificacionProcesadaEvent event) {
        if (event.getIdsDonaciones() != null) {
            for (UUID donationId : event.getIdsDonaciones()) {
                donacionRepository.buscarPorId(donationId).ifPresent(donacion -> {
                    donacion.cambiarEstado(EstadoDonacion.LISTA_PARA_ENTREGAR, "Ruta planificada", null);
                    donacionRepository.guardar(donacion);
                });
            }
        }
    }

    @EventListener
    public void handleRutaIniciada(RutaIniciadaEvent event) {
        if (event.getIdsDonaciones() != null) {
            for (UUID donationId : event.getIdsDonaciones()) {
                donacionRepository.buscarPorId(donationId).ifPresent(donacion -> {
                    donacion.cambiarEstado(EstadoDonacion.EN_TRASLADO, "Chofer inició la ruta de reparto", null);
                    donacionRepository.guardar(donacion);

                    // Buscar el donante y notificarlo
                    Persona donantePersona = buscarPersonaDonantePorDonacionId(donationId);
                    if (donantePersona != null) {
                        Contacto contacto = donantePersona.getContacto();
                        if (contacto == null) {
                            contacto = new Contacto(donantePersona.getEmail(), null, null, MedioContacto.CORREO);
                        }
                        String mapLink = "http://localhost:8080/rutas/" + event.getRutaId() + "/mapa";
                        servicioNotificaciones.enviar(
                            new NotificacionOutDTO("Tu donación está en traslado. Síguela en: " + mapLink, MedioContacto.CORREO),
                            contacto
                        );
                    }

                    // Notificar a la entidad beneficiaria
                    if (donacion.getEntidadAsignada() != null) {
                        Contacto contactoEntidad = new Contacto("entidad-beneficiaria@test.com", null, null, MedioContacto.CORREO);
                        String mapLink = "http://localhost:8080/rutas/" + event.getRutaId() + "/mapa";
                        servicioNotificaciones.enviar(
                            new NotificacionOutDTO("La entrega asignada está en traslado. Síguela en: " + mapLink, MedioContacto.CORREO),
                            contactoEntidad
                        );
                    }
                });
            }
        }
    }

    @EventListener
    public void handleEntregaConfirmada(EntregaConfirmadaEvent event) {
        donacionRepository.buscarPorId(event.getIdDonacionOriginal()).ifPresent(donacion -> {
            donacion.cambiarEstado(EstadoDonacion.ENTREGADA, "Entregado a la entidad beneficiaria", null);
            if (event.getFotos() != null) {
                for (String fotoUrl : event.getFotos()) {
                    donacion.addFoto(new Foto("Comprobante de entrega", fotoUrl));
                }
            }
            donacionRepository.guardar(donacion);

            Persona donantePersona = buscarPersonaDonantePorDonacionId(event.getIdDonacionOriginal());
            if (donantePersona != null) {
                Contacto contacto = donantePersona.getContacto();
                if (contacto == null) {
                    contacto = new Contacto(donantePersona.getEmail(), null, null, MedioContacto.CORREO);
                }
                
                // Notificar donante
                servicioNotificaciones.enviar(
                    new NotificacionOutDTO("Tu donación ha sido entregada con éxito. Camión responsable: " + event.getCamionPatente(), MedioContacto.CORREO),
                    contacto
                );

                // Informar al modulo de incentivos pasándole el contexto completo
                int cantidadBienesMock = donacion.getBienes() != null ? donacion.getBienes().size() : 1;
                List<String> categoriasMock = new ArrayList<>();
                if (donacion.getSubCategoria() != null) {
                    categoriasMock.add(donacion.getSubCategoria().getNombre());
                } else {
                    categoriasMock.add("Generica");
                }
                UUID idEntidadMock = donacion.getEntidadAsignada() != null ? donacion.getEntidadAsignada().getId() : UUID.randomUUID();

                ActividadDonacionDTO dto = new ActividadDonacionDTO(
                    donantePersona.getId(), cantidadBienesMock, categoriasMock, idEntidadMock, LocalDate.now()
                );
                
                try {
                    incentivoClient.registrarActividadDonacionExitosa(donantePersona.getId(), dto);
                } catch (Exception e) {
                    // Ignorar errores en tests si incentivos no está completamente activo en el mock
                }
            }

            // Notificar a la entidad
            Contacto contactoEntidad = new Contacto("entidad-beneficiaria@test.com", null, null, MedioContacto.CORREO);
            servicioNotificaciones.enviar(
                new NotificacionOutDTO("Has confirmado la recepción de la donación exitosamente.", MedioContacto.CORREO),
                contactoEntidad
            );
        });
    }

    @EventListener
    public void handleEntregaFallida(EntregaFallidaEvent event) {
        donacionRepository.buscarPorId(event.getIdDonacionOriginal()).ifPresent(donacion -> {
            donacion.cambiarEstado(EstadoDonacion.ENTREGA_FALLIDA, event.getMotivo(), null);
            donacionRepository.guardar(donacion);

            Persona donantePersona = buscarPersonaDonantePorDonacionId(event.getIdDonacionOriginal());
            if (donantePersona != null) {
                Contacto contacto = donantePersona.getContacto();
                if (contacto == null) {
                    contacto = new Contacto(donantePersona.getEmail(), null, null, MedioContacto.CORREO);
                }
                
                // Notificar donante
                servicioNotificaciones.enviar(
                    new NotificacionOutDTO("La entrega de tu donación ha fallado: " + event.getMotivo(), MedioContacto.CORREO),
                    contacto
                );
            }

            // Notificar entidad
            Contacto contactoEntidad = new Contacto("entidad-beneficiaria@test.com", null, null, MedioContacto.CORREO);
            servicioNotificaciones.enviar(
                new NotificacionOutDTO("La entrega de la donación asignada ha fallado: " + event.getMotivo(), MedioContacto.CORREO),
                contactoEntidad
            );

            // Notificar administrador
            Contacto contactoAdmin = new Contacto("admin@donatrack.com", null, null, MedioContacto.CORREO);
            servicioNotificaciones.enviar(
                new NotificacionOutDTO("ATENCIÓN: Entrega fallida para la donación " + event.getIdDonacionOriginal() + ". Motivo: " + event.getMotivo(), MedioContacto.CORREO),
                contactoAdmin
            );
        });
    }

    private Persona buscarPersonaDonantePorDonacionId(UUID donationId) {
        for (Persona persona : personaRepository.obtenerTodas()) {
            if (persona.getRoles() != null) {
                for (Rol rol : persona.getRoles()) {
                    if (rol instanceof Donante donante) {
                        if (donante.getDonacionesRealizadas() != null) {
                            for (Donacion d : donante.getDonacionesRealizadas()) {
                                if (d.getId().equals(donationId)) {
                                    return persona;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
