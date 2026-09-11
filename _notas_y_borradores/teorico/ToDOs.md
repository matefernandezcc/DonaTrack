# 🏛️ Patrón API Gateway vs. Acceso Directo (Web Server)

Este documento explica la diferencia entre despachar archivos estáticos permitiendo que el cliente acceda directamente a los microservicios, frente a la implementación de un verdadero **API Gateway** o **Reverse Proxy** en el puerto `:8080`.

---

## ❌ Lo que NO debería pasar (Acceso Directo / Anti-Patrón)

En este escenario, el servidor en el puerto `8080` actúa únicamente como un "File Server" (servidor de archivos estáticos). Una vez que le entrega el HTML/JS al navegador, se desentiende. El código JavaScript (Frontend) asume la responsabilidad de saber dónde vive cada microservicio y se comunica directamente con ellos.

### Flujo del Acceso Directo:
1. **Frontend:** El navegador solicita `http://localhost:8080/`.
2. **Server (8080):** Devuelve `index.html` y los archivos estáticos (`.js`, `.css`).
3. **Frontend (JS):** El código JavaScript se ejecuta y necesita hacer un login. Ejecuta un `fetch('http://localhost:8000/api/login')`.
4. **Microservicio (8000):** Recibe la petición directamente desde el navegador del usuario y responde.

### Diagrama:

    [ Navegador del Usuario ]
           │            │             │
           │(1) Get UI  │(3) fetch()  │(3) fetch()
           ▼            ▼             ▼
    ┌────────────┐ ┌────────────┐ ┌────────────┐
    │ Server UI  │ │ Donaciones │ │ Logística  │
    │   :8080    │ │   :8000    │ │   :8002    │
    └────────────┘ └────────────┘ └────────────┘

### Problemas de este enfoque:
* **Problemas de CORS:** Como el navegador está en `localhost:8080` e intenta acceder a `localhost:8000`, los navegadores lo bloquean por seguridad (Cross-Origin Resource Sharing). Obliga a configurar CORS en *cada uno* de los microservicios.
* **Exposición de la Arquitectura:** El cliente (frontend) conoce exactamente cuántos microservicios hay y en qué puertos corren.
* **Seguridad Descentralizada:** Si querés implementar autenticación (ej. validar un token JWT), tenés que escribir el código de validación repetido en *cada* microservicio.

---

## ✅ Lo que SÍ debería pasar (API Gateway / Reverse Proxy)

En este escenario, el servidor en el puerto `8080` actúa como la **única puerta de entrada** (Gateway) para todo el tráfico externo. El navegador del usuario *jamás* se entera de la existencia de los puertos `8000`, `8001` o `8002`.

### Flujo del API Gateway:
1. **Frontend:** El navegador solicita `http://localhost:8080/`.
2. **Gateway (8080):** Devuelve `index.html` y los archivos estáticos.
3. **Frontend (JS):** El código JavaScript necesita hacer un login. Ejecuta un `fetch('/api/donaciones/login')` (Nótese que la petición va al *mismo* servidor `8080`, sin especificar puerto).
4. **Gateway (8080):** Recibe la petición `/api/donaciones/login`. Evalúa la ruta y dice: *"Ah, todo lo que empieza con `/api/donaciones` lo tengo que reenviar (proxear) a mi red interna en el puerto `8000`"*.
5. **Microservicio (8000):** Recibe la petición reenviada por el Gateway de forma interna, la procesa, y le devuelve la respuesta al Gateway.
6. **Gateway (8080):** Le entrega la respuesta final al navegador del usuario.

### Diagrama:

          [ Navegador del Usuario ]
                     │
                     │ (1) Get UI y (3) fetch('/api/...')
                     ▼
          ┌─────────────────────┐
          │  API Gateway :8080  │ (Único punto público)
          └─────────┬───────────┘
                    │
         (4) Enrutamiento Interno (Docker Network)
          ┌─────────┼───────────┐
          ▼         ▼           ▼
    ┌──────────┐ ┌──────────┐ ┌──────────┐
    │Donaciones│ │Incentivos│ │Logística │ (Ocultos al exterior)
    │  :8000   │ │  :8001   │ │  :8002   │
    └──────────┘ └──────────┘ └──────────┘

### Ventajas de este enfoque:
* **Chau CORS:** El navegador siempre habla con el mismo origen (`localhost:8080`). Los microservicios no necesitan lidiar con reglas de orígenes cruzados.
* **Encapsulamiento:** Podés cambiar la arquitectura interna (agregar o quitar microservicios) sin tener que tocar una sola línea de código en el Frontend.
* **Punto de Control Central:** Es el lugar ideal para validar tokens de seguridad, limitar la cantidad de peticiones (Rate Limiting) o unificar logs antes de que el tráfico llegue al *core* de tu negocio.