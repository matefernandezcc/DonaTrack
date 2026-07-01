# API REST
#POST
- [ ] Implementar el endpoint del callback que recibe las rutas y las persiste en tu base
    - `POST /logistica/callback`
recibo las RutasDeReparto -> actualizar SolicitudPlanificacion a PROCESADA

#DOCS

- [ ] Documentar en Swagger los endpoints del Servicio de Logística (el webhook/callback, el inicio de ruta para el chofer, la confirmación para la entidad).

- [ ] Dejar listos los ejemplos de request y response en Postman para la demo.

# Implementación de la Lógica de Negocio:

- [ ] Armar la lógica que divide las donaciones en lotes de 100 para mandarlas al proveedor externo.

# Integración Async

`Ojo con esto: El Servicio de Logística no debe comunicarse directamente con el Servicio de Notificaciones. Tenés que implementar un mecanismo intermedio (ej. dejar los eventos en una cola de mensajes como RabbitMQ, o en una tabla de eventos) para avisar sobre el inicio de ruta, entregas exitosas o fallidas.`

# Infraestructura y Despliegue:

- [ ] Armar el Dockerfile del Servicio de Logística.

- [ ] Actualizar el compose.yaml para levantar todo el ecosistema junto.

# Documentación Final de Arquitectura:

- [ ] Armar el Diagrama de Componentes actualizado (mostrando los 3 servicios y cómo se comunican).

- [ ] Armar el Diagrama de Despliegue actualizado (mostrando los contenedores Docker y los nodos).