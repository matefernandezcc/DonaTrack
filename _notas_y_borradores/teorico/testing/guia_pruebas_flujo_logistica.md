# Guía de Pruebas y Troubleshooting: Flujo de Logística (Planificación e Inicio de Ruta)

Este documento detalla las correcciones realizadas sobre el flujo de mensajería y base de datos entre Logística y Donaciones, y establece el paso a paso riguroso para testear el flujo de principio a fin usando Bruno.

## 1. Problemas Resueltos y Cambios Arquitectónicos

Durante la integración del flujo completo, nos encontramos con errores tipo `400 Bad Request` en los endpoints y fallos silenciosos de persistencia. Se aplicaron los siguientes ajustes a nivel de código para resolver estos "cabos sueltos":

### A. Conflicto en la configuración de RabbitMQ
Habíamos configurado un `MessageConverter` en el módulo `donatrack-donaciones` para confiar en todos los paquetes, pero al existir también otro `MessageConverter` en el módulo `donatrack-common`, la autoconfiguración de Spring Boot se confundió (al haber 2 beans del mismo tipo) y terminó usando el conversor por defecto (que solo soporta `byte[]`). Esto hacía que el evento de inicio de ruta fallara silenciosamente al intentar deserializarse.
**Solución:** Se eliminó el bean duplicado en `RabbitMQConfig.java` de donaciones y se mantuvo el central en `common`.

### B. Fallo en Base de Datos (LazyInitializationException)
Al recibir el mensaje de RabbitMQ e intentar notificar a los donantes, el `ProcesarInicioRutaUseCase` consultaba la donación e intentaba acceder a la colección "perezosa" de bienes. Como los listeners de Rabbit no corren dentro de una sesión de base de datos por defecto, Hibernate tiraba una excepción oculta y no guardaba el estado `EN_TRASLADO`.
**Solución:** Se agregó la anotación `@Transactional` al método `procesar()` del caso de uso.

### C. Error en el Payload JSON de "Callback Planificacion" (Causa del 400)
La documentación en el YAML de Swagger indicaba que la request llevaba una lista `idsDonaciones: [...]`. Sin embargo, el DTO y la entidad de dominio (`RutaDeReparto` > `Parada`) en Java esperaban una lista de objetos `entregas`, donde cada entrega tiene un `idEntrega`. Al mandar `idsDonaciones`, Jackson lo ignoraba y llegaba nulo, dejando la ruta sin entregas.
**Solución:** Se actualizó la colección de Bruno para enviar `"entregas": [{"idEntrega": "TU_UUID_DONACION"}]` dentro de cada parada, coincidiendo de forma estricta con las firmas en Java.

### D. Error en el Payload JSON de "Iniciar Ruta" (Causa del 400)
El YAML indicaba que "Iniciar Ruta" esperaba `camionPatente` y `nombreChofer`. Sin embargo, el controlador real (`LogisticaController.java` en `iniciarRuta`) esperaba un JSON con la propiedad `legajoChofer`. Además, ese endpoint devolvía `400` (Ruta Inexistente) si el "Callback" previo fallaba, porque la ruta nunca se insertaba en la base de datos.
**Solución:** El body del POST a `/iniciar` fue actualizado a `{"legajoChofer": "CH-001"}`.

## 2. Instrucciones de Prueba Paso a Paso (Bruno)

El error más común durante las pruebas manuales es cruzar un **ID generado en la base de datos local** con el **servidor de producción**, lo que deriva en un error `"Solicitud no encontrada"`. Para evitarlo, es fundamental respetar la coherencia del entorno.

### A. Prueba en Entorno Local (Docker/dev)

1. **Seleccionar Entorno:** En la esquina superior derecha de Bruno, elegí **`dev`**. Esto asegura que las peticiones apunten a `http://localhost:8002` y consulten tu Postgres en Docker.
2. **Ejecutar Planificación:** 
   * Dispará el request `POST Ejecutar Planificacion`.
   * Anotá el nuevo UUID (`idSolicitud`) que se generó o buscalo en tu base local en la tabla `logistica.solicitudes_planificacion` (estará en estado `PENDIENTE`).
3. **Simular Callback:**
   * Abrí `POST Callback Planificacion`.
   * En el body JSON, reemplazá el `"idSolicitud"` por el UUID del paso anterior.
   * Inventá un UUID nuevo para `"id"` de la ruta, y poné un `"idEntrega"` que exista en estado `EN_DEPOSITO`.
   * Ejecutá el request. Debe devolver **200 OK**. (La solicitud pasará a `PROCESADA`).
4. **Iniciar Ruta:**
   * Abrí `POST Iniciar Ruta`.
   * En la URL (`:rutaId`), pegá el mismo UUID de ruta que inventaste en el paso 3.
   * Ejecutá el request. Debe devolver **200 OK**.
5. **Validación:** Verificá en tu Postgres local que la donación correspondiente ahora figura en estado `EN_TRASLADO`.

### B. Prueba en Entorno Producción (Render + Supabase)

1. **Seleccionar Entorno:** En Bruno, cambiá el entorno a **`prod`**. (Tus peticiones ahora irán a `https://dona-logistica.onrender.com`).
2. **Ejecutar Planificación (Generar UUID en Prod):** 
   * Dispará el request `POST Ejecutar Planificacion`. *No recicles un ID de tus pruebas locales*.
   * Buscá la nueva solicitud generada conectándote a la base de datos de **Supabase**.
3. **Simular Callback:**
   * Abrí `POST Callback Planificacion`.
   * Pegá el nuevo `idSolicitud` generado en Render/Supabase.
   * Usá un `"idEntrega"` válido de la base de Supabase.
   * Ejecutá el request. Recibirás un **200 OK**.
4. **Iniciar Ruta:** Idéntico al flujo local, pero operando contra la URL de Render.

> [!WARNING]
> Si en el Callback recibís un `400 Bad Request` con el mensaje `"Solicitud no encontrada: [UUID]"`, significa que **el UUID enviado ya fue procesado** o **no existe en la base de datos apuntada por el entorno activo en Bruno**.

## Referencias

- Configuración corregida de RabbitMQ: [RabbitMQCommonConfig.java](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-common/src/main/java/com/donatrack/common/infrastructure/config/RabbitMQCommonConfig.java)
- Persistencia de estado en Donaciones: [ProcesarInicioRutaUseCase.java](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-donaciones/src/main/java/com/donatrack/donaciones/application/usecases/ProcesarInicioRutaUseCase.java)
- Validación de Solicitudes y Excepción 400: [ProcesarCallbackPlanificacionService.java](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/donatrack-logistica/src/main/java/com/donatrack/logistica/application/usecases/ProcesarCallbackPlanificacionService.java)
- Entornos de Bruno: [dev.yml](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/docs/donatrack-api/Donatrack%20-%20API/environments/dev.yml) | [prod.yml](file:///c:/Users/asus/OneDrive/Escritorio/FACULTAD%20MAX/DonaTrack/docs/donatrack-api/Donatrack%20-%20API/environments/prod.yml)
