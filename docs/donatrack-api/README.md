# 🧪 Colección de APIs Bruno — DonaTrack

Este directorio contiene la colección completa de endpoints REST de DonaTrack para ser ejecutada tanto desde la aplicación de escritorio de **Bruno** como desde la terminal mediante el CLI `@usebruno/cli`.

---

## 📂 Estructura

- `bruno-collection/`: Colección ejecutable con carpetas organizadas por Bounded Context (`Auth`, `Donaciones`, `Incentivos`, `Logistica`, `Notificaciones`).
  - Entornos preconfigurados en `bruno-collection/environments/`:
    - `dev.bru`: Apunta al entorno distribuido local en Docker (`localhost:8000`, `localhost:8001`, `localhost:8002`, `localhost:8003`, `localhost:8004`, `localhost:8080`).
    - `prod.bru`: Apunta a los microservicios desplegados en Render y servicios SaaS.

---

## 🚀 Ejecución por Terminal (Bruno CLI)

No requiere instalación global; podés ejecutarlo directamente con `npx`:

```bash
# 1. Ingresar al directorio de la colección
cd docs/donatrack-api/bruno-collection

# 2. Ejecutar la suite completa contra el entorno local (dev)
npx @usebruno/cli run --env dev

# O contra el entorno de producción (prod)
npx @usebruno/cli run --env prod
```
