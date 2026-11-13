# Guía Práctica: ¿Qué es DBeaver?

Esta guía explica qué es DBeaver, para qué sirve y cómo encaja en el flujo de trabajo de desarrollo de DonaTrack.

## 1. ¿Qué es DBeaver?

**DBeaver** es un cliente SQL y una herramienta de administración de bases de datos de interfaz gráfica (GUI) universal. A diferencia de programas que solo sirven para un motor específico (como *pgAdmin* para PostgreSQL o *MySQL Workbench* para MySQL), DBeaver te permite conectarte prácticamente a **cualquier base de datos** del mercado desde un solo lugar.

Es de código abierto (en su versión Community) y está construido sobre Java/Eclipse, lo que lo hace multiplataforma (funciona igual en Windows, Mac y Linux).

---

## 2. ¿Para qué sirve? (El "Por Qué")

Cuando estás desarrollando software, los datos viven adentro de un motor oscuro (como el contenedor de Docker de PostgreSQL en DonaTrack). El ORM (Hibernate) guarda los datos ahí de forma automática, pero vos como desarrollador necesitás ver qué está pasando.

DBeaver sirve como tus "ojos" directos hacia la base de datos. Se usa principalmente para:

1. **Inspección de Datos:** Ver las tablas como si fueran un Excel, confirmar que el ORM guardó el objeto con los datos correctos, o que la fila realmente se actualizó tras correr un test.
2. **Ejecución de SQL Manual:** Probar consultas SQL complejas a mano antes de pasarlas a código Java (JPQL o Native Query) para asegurarte de que la lógica es correcta.
3. **Gestión de Esquemas (DDL):** Crear, alterar o borrar tablas, índices y secuencias visualmente sin tener que escribir comandos de consola.
4. **Exportar/Importar:** Hacer volcados rápidos de datos (dumps) en formato CSV o SQL para compartir con otros desarrolladores.

---

## 3. DBeaver en el ecosistema DonaTrack

Dado que DonaTrack utiliza PostgreSQL y la Arquitectura Hexagonal divide los esquemas (`donaciones`, `logistica`, `incentivos`), usar la terminal para navegar todo esto sería muy lento. DBeaver simplifica el desarrollo de la siguiente forma:

*   **Conexión Local (Docker):** Te conectás a `localhost:5432` con usuario `postgres` y podés desplegar visualmente todos los esquemas. Es vital para debuggear por qué falló un insert en la capa de persistencia (`infrastructure/adapters/out/persistence`).
*   **Conexión a Producción (Supabase):** DBeaver te permite crear una segunda conexión apuntando directamente a Supabase (ej. `aws-1-us-east-1.pooler.supabase.com:5432`) para revisar problemas en producción usando la misma interfaz a la que ya estás acostumbrado en local.

---

## 4. Trade-offs de usar un GUI Universal como DBeaver

**Ventajas:**
*   **Universalidad:** No necesitás aprender 5 programas distintos si mañana el proyecto empieza a usar MySQL para otro servicio o MongoDB; DBeaver los lee todos.
*   **Autocompletado:** Su editor SQL incluye un autocompletado inteligente que lee tu base de datos, lo que acelera mucho escribir queries a mano.

**Desventajas:**
*   **Consumo de Memoria:** Al estar basado en Java (Eclipse RCP), DBeaver es más pesado en memoria RAM que clientes nativos o ligeros (como DBeaver vs psql en terminal).
*   **Sobrecarga de opciones:** Al soportar todas las bases de datos del mundo, a veces la interfaz tiene menús o configuraciones que no aplican a tu motor específico, pudiendo marear al principio.

---

## Referencias

- DBeaver Community. (2024). *Sitio Oficial y Características*. https://dbeaver.io/