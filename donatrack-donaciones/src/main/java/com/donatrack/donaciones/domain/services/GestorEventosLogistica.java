package com.donatrack.donaciones.domain.services;

import com.donatrack.donaciones.application.ports.out.DonacionRepository;
import com.donatrack.donaciones.application.ports.out.PersonaRepository;
import com.donatrack.donaciones.application.ports.out.DonacionOriginalRepository;
import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.donacion.DonacionOriginal;
import com.donatrack.donaciones.domain.entities.persona.Contacto;
import com.donatrack.donaciones.domain.entities.persona.Persona;
import com.donatrack.donaciones.domain.entities.roles.Beneficiario;
import com.donatrack.donaciones.domain.entities.roles.Donante;
import com.donatrack.donaciones.domain.entities.enums.EstadoDonacion;
import com.donatrack.donaciones.domain.entities.enums.MedioContacto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GestorEventosLogistica {

    private final NotificadorPort notificadorPort;
    private final DonacionRepository donacionRepository;
    private final PersonaRepository personaRepository;
    private final DonacionOriginalRepository donacionOriginalRepository;

    public GestorEventosLogistica(NotificadorPort notificadorPort,
                                  DonacionRepository donacionRepository,
                                  PersonaRepository personaRepository,
                                  DonacionOriginalRepository donacionOriginalRepository) {
        this.notificadorPort = notificadorPort;
        this.donacionRepository = donacionRepository;
        this.personaRepository = personaRepository;
        this.donacionOriginalRepository = donacionOriginalRepository;
    }

    public void procesarInicioRuta(UUID idRuta, List<UUID> donacionesEnRuta) {
        for (UUID idDonacion : donacionesEnRuta) {
            Optional<Donacion> donacionOpt = donacionRepository.buscarPorId(idDonacion);
            if (donacionOpt.isEmpty()) continue;

            Donacion donacion = donacionOpt.get();
            donacion.cambiarEstado(EstadoDonacion.EN_TRASLADO, "Ruta iniciada. RutaID: " + idRuta, null);
            donacionRepository.guardar(donacion);

            String mensaje = "¡Tu donación está en camino! Seguila en vivo en la ruta: http://mapa.donatrack.com/ruta/" + idRuta;

            // Notificar a la entidad asignada
            Beneficiario beneficiario = donacion.getEntidadAsignada();
            if (beneficiario != null) {
                Optional<Persona> personaBeneficiario = personaRepository.buscarPorRolId(beneficiario.getId());
                personaBeneficiario.ifPresent(p -> {
                    if (p.getContacto() != null) {
                        notificadorPort.notificar(p.getContacto(), mensaje, p.getContacto().getMedioPredeterminado());
                    }
                });
            }

            // Notificar al donante original
            Optional<DonacionOriginal> originalOpt = donacionOriginalRepository.buscarPorIdDonacion(idDonacion);
            originalOpt.ifPresent(orig -> {
                Donante donante = orig.getDonante();
                if (donante != null) {
                    Optional<Persona> personaDonante = personaRepository.buscarPorRolId(donante.getId());
                    personaDonante.ifPresent(p -> {
                        if (p.getContacto() != null) {
                            notificadorPort.notificar(p.getContacto(), mensaje, p.getContacto().getMedioPredeterminado());
                        }
                    });
                }
            });
        }
    }

    public void procesarEntregaExitosa(UUID idDonacion, String comprobante) {
        Optional<Donacion> donacionOpt = donacionRepository.buscarPorId(idDonacion);
        if (donacionOpt.isEmpty()) return;

        Donacion donacion = donacionOpt.get();
        donacion.cambiarEstado(EstadoDonacion.ENTREGADA, "Entrega exitosa. Comprobante: " + comprobante, null);
        donacionRepository.guardar(donacion);

        String mensaje = "Tu donación con ID " + idDonacion + " ha sido entregada con éxito a la entidad asignada.";

        // Notificar al donante original
        Optional<DonacionOriginal> originalOpt = donacionOriginalRepository.buscarPorIdDonacion(idDonacion);
        originalOpt.ifPresent(orig -> {
            Donante donante = orig.getDonante();
            if (donante != null) {
                Optional<Persona> personaDonante = personaRepository.buscarPorRolId(donante.getId());
                personaDonante.ifPresent(p -> {
                    if (p.getContacto() != null) {
                        notificadorPort.notificar(p.getContacto(), mensaje, p.getContacto().getMedioPredeterminado());
                    }
                });
            }
        });
    }

    public void procesarEntregaFallida(UUID idDonacion, String motivo) {
        Optional<Donacion> donacionOpt = donacionRepository.buscarPorId(idDonacion);
        if (donacionOpt.isEmpty()) return;

        Donacion donacion = donacionOpt.get();
        donacion.cambiarEstado(EstadoDonacion.ENTREGA_FALLIDA, "Entrega fallida: " + motivo, null);
        donacionRepository.guardar(donacion);

        String mensaje = "La entrega de la donación con ID " + idDonacion + " falló debido a: " + motivo;

        // Notificar al donante
        Optional<DonacionOriginal> originalOpt = donacionOriginalRepository.buscarPorIdDonacion(idDonacion);
        originalOpt.ifPresent(orig -> {
            Donante donante = orig.getDonante();
            if (donante != null) {
                Optional<Persona> personaDonante = personaRepository.buscarPorRolId(donante.getId());
                personaDonante.ifPresent(p -> {
                    if (p.getContacto() != null) {
                        notificadorPort.notificar(p.getContacto(), mensaje, p.getContacto().getMedioPredeterminado());
                    }
                });
            }
        });

        // Notificar a la entidad
        Beneficiario beneficiario = donacion.getEntidadAsignada();
        if (beneficiario != null) {
            Optional<Persona> personaBeneficiario = personaRepository.buscarPorRolId(beneficiario.getId());
            personaBeneficiario.ifPresent(p -> {
                if (p.getContacto() != null) {
                    notificadorPort.notificar(p.getContacto(), mensaje, p.getContacto().getMedioPredeterminado());
                }
            });
        }
    }
}
