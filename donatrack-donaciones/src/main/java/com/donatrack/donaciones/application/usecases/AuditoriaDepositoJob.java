package com.donatrack.donaciones.application.usecases;

import com.donatrack.donaciones.domain.entities.donacion.Donacion;
import com.donatrack.donaciones.domain.entities.enums.EstadoDonacion;
import com.donatrack.donaciones.application.ports.out.DonacionRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class AuditoriaDepositoJob {

    private final DonacionRepository donacionRepository;

    public AuditoriaDepositoJob(DonacionRepository donacionRepository) {
        this.donacionRepository = donacionRepository;
    }

    /**
     * Audita las donaciones en estado EN_DEPOSITO buscando bienes vencidos.
     * Si algún bien de la donación está vencido, marca la donación entera como VENCIDA.
     * Se ejecuta diariamente a la 1 AM.
     */
    @Async
    @Scheduled(cron = "0 0 1 * * ?")
    public void auditarVencidos() {
        List<Donacion> donacionesEnDeposito = donacionRepository.buscarPorEstado(EstadoDonacion.EN_DEPOSITO);
        LocalDate hoy = LocalDate.now();

        for (Donacion donacion : donacionesEnDeposito) {
            if (tieneBienesVencidos(donacion, hoy)) {
                donacion.cambiarEstado(EstadoDonacion.VENCIDA, "Auditoría nocturna", null);
                donacionRepository.guardar(donacion);
            }
        }
    }

    private boolean tieneBienesVencidos(Donacion donacion, LocalDate fechaReferencia) {
        return donacion.getBienes().stream()
                .anyMatch(bien -> bien.getFechaVencimiento() != null
                        && bien.getFechaVencimiento().isBefore(fechaReferencia));
    }
}
