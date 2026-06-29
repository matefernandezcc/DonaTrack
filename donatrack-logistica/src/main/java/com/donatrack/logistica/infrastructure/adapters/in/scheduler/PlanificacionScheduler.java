package com.donatrack.logistica.infrastructure.adapters.in.scheduler;
// package com.donatrack.logistica.infrastructure.adapters.in;

// import
// com.donatrack.logistica.application.ports.in.ProcesarPlanificacionesPendientesUseCase;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Component;

// @Component
// public class PlanificacionScheduler {

// private static final Logger log =
// LoggerFactory.getLogger(PlanificacionScheduler.class);
// private final ProcesarPlanificacionesPendientesUseCase
// procesarPlanificacionesUseCase;

// public PlanificacionScheduler(ProcesarPlanificacionesPendientesUseCase
// procesarPlanificacionesUseCase) {
// this.procesarPlanificacionesUseCase = procesarPlanificacionesUseCase;
// }

// // Ejecutar programado (por defecto a las 2 AM todos los días)
// @Scheduled(cron = "${logistica.scheduler.planificacion.cron:0 0 2 * * ?}")
// public void ejecutarPlanificacionDiferida() {
// log.info("Iniciando cron job de planificación de rutas diferida...");
// try {
// procesarPlanificacionesUseCase.procesarPlanificacionesPendientes();
// log.info("Cron job de planificación de rutas diferida finalizado con
// éxito.");
// } catch (Exception e) {
// log.error("Error al ejecutar el cron job de planificación de rutas diferida",
// e);
// }
// }
// }
