# Flujo de Ejecución: Iniciar Ruta de Reparto

**Actor:** Chofer
**Acción:** Hace clic en el botón "Iniciar Recorrido" para la Ruta con ID `1234`.

---

### 1. El Frontend dispara la petición
Tu componente frontend (`donatrack-server` o aplicación web) detecta el clic y envía una petición HTTP por la red hacia el contenedor Docker del Servicio de Logística:
* **Request:** `POST http://localhost:8082/api/rutas/1234/iniciar`

### 2. Infraestructura: El Adaptador de Entrada (Controller)
La petición entra a tu hexágono por la capa de `infrastructure/adapters/in/api`.
* El `LogisticaController` recibe el `POST`.
* Valida cuestiones web básicas (ej. que tengas un token de seguridad, que el ID no esté vacío).
* El Controller tiene inyectado el **Puerto de Entrada** (`IniciarRutaPort`) y lo llama pasándole el ID de la ruta.

### 3. Aplicación: El Caso de Uso (La orquestación)
El flujo llega al centro del sistema: `infrastructure/usecases/IniciarRutaUseCase` (que implementa el `IniciarRutaPort`). Este es el "director de orquesta" y hace lo siguiente:
1. **Busca la ruta:** Llama a su **Puerto de Salida** `RutaRepositoryPort.buscarPorId("1234")`.
2. **Infraestructura actúa:** El adaptador `JpaRutaRepositoryAdapter` va a PostgreSQL, busca la ruta y se la devuelve al Caso de Uso convertida en una entidad de Dominio pura.
3. **Ejecuta el negocio:** El Caso de Uso llama al método de tu entidad: `ruta.iniciarRuta()`.

### 4. Dominio: La Entidad (La regla de negocio)
Acá no hay nada de base de datos ni HTTP. Tu clase `RutaDeReparto` ejecuta su lógica pura:
* Verifica si ya no estaba iniciada previamente.
* Marca su variable `iniciada = true`.
* Recorre todas las `Parada`s y las `Entrega`s asociadas, y les cambia el estado a `EN_TRASLADO`.

### 5. Aplicación: Persistir el cambio
Volvemos al `IniciarRutaUseCase`. Como la entidad ya actualizó su estado en memoria, el Caso de Uso manda a guardar los cambios.
* Llama nuevamente a `RutaRepositoryPort.guardar(ruta)`.
* El adaptador de base de datos (`JpaRutaRepositoryAdapter`) ejecuta el `UPDATE` en las tablas.

### 6. Aplicación: Eventos Asincrónicos (El requerimiento clave)
La consigna prohíbe que Logística se comunique directamente con Notificaciones. Entonces, el Caso de Uso no hace un HTTP POST a notificaciones.
* El Caso de Uso llama a otro **Puerto de Salida**: `EventoNotificacionPort.publicarInicioRuta(...)`.
* **Infraestructura actúa:** El adaptador `RabbitMqEventAdapter` toma ese mensaje y lo tira a una cola de mensajes (ej. RabbitMQ o Kafka). El servicio de Logística se olvida del tema. (Luego, el Servicio de Notificaciones leerá esa cola por su cuenta y mandará los emails).

### 7. Respuesta al Usuario
El Caso de Uso termina de ejecutarse sin errores. El `LogisticaController` le responde a tu `donatrack-server`:
* **Response:** `200 OK` (o un JSON confirmando el estado).
* El navegador del chofer actualiza la pantalla y le muestra "Ruta en curso".