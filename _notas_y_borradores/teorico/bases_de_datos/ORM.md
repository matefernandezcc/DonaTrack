# Guía Práctica: ¿Qué es un ORM? (Object-Relational Mapping)

Esta guía explica el concepto de ORM, cómo se relaciona con herramientas como JPA e Hibernate en el ecosistema Java, y cómo está implementado dentro de la arquitectura de DonaTrack.

## 1. ¿Qué es un ORM?

**ORM (Object-Relational Mapping)** es una técnica de programación que funciona como un "traductor" entre dos mundos que hablan idiomas totalmente distintos:
1.  **Tu código (Orientado a Objetos):** Maneja Clases, Objetos, Herencia y Colecciones (ej. Java).
2.  **Tu base de datos (Relacional):** Maneja Tablas, Filas, Columnas y Claves Foráneas (ej. PostgreSQL, MySQL).

Esta diferencia se conoce técnicamente como *Object-Relational Impedance Mismatch* (Desajuste de impedancia objeto-relacional). 

El ORM soluciona esto permitiéndote interactuar con la base de datos usando objetos nativos de tu lenguaje en lugar de escribir consultas SQL manualmente a cada rato. Cuando hacés un `usuarioRepository.save(usuario)`, el ORM por detrás genera automáticamente el `INSERT INTO usuarios...`.

---

## 2. JPA vs Hibernate: ¿Son lo mismo?

En el mundo de Spring Boot y Java, vas a escuchar estos dos términos constantemente. No son sinónimos, sino que trabajan en conjunto:

*   **JPA (Java Persistence API):** Es la **especificación** (el manual de reglas). Es un estándar de Java que define *cómo* debería funcionar un ORM. Te provee las interfaces y las anotaciones comunes (como `@Entity`, `@Id`, `@OneToMany`), pero no tiene código ejecutable por debajo que hable con la base de datos.
*   **Hibernate:** Es la **implementación** (el motor). Es la librería que agarra las reglas dictadas por JPA y hace el trabajo sucio de generar el SQL y conectarse a la base de datos. 

> [!TIP]
> **Analogía:** JPA es el plano de diseño arquitectónico de una casa. Hibernate es la empresa constructora que pone los ladrillos para hacer esa casa realidad. Podrías cambiar de empresa constructora (ej. usar *EclipseLink* en lugar de *Hibernate*), y como el plano (JPA) es el mismo, tu código no se rompe.

---

## 3. El ORM en DonaTrack

### Aislamiento en Arquitectura Hexagonal

En DonaTrack, al usar Arquitectura Hexagonal, **está estrictamente prohibido que el ORM toque la lógica de negocio (el Dominio)**. 

Si mirás la carpeta `domain`, los objetos son Java puro. El ORM (las anotaciones JPA) vive **únicamente** en la capa de Infraestructura, específicamente en el adaptador de persistencia (`infrastructure/adapters/out/persistence/entities`).

### Ejemplo: `DonanteEntity.java`

Así se ve cómo le decimos al ORM que mapee una clase a una tabla en DonaTrack:

```java
// Archivo: donatrack-donaciones/src/main/java/com/donatrack/donaciones/infrastructure/adapters/out/persistence/entities/DonanteEntity.java

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity // Le dice al ORM: "Esta clase es una tabla"
@Table(name = "donante") // Nombre real de la tabla en Postgres
public class DonanteEntity extends RolEntity {
    
    // ... atributos mapeados a columnas ...
}
```

*Nota: Aquí es donde entra en juego la documentación de **Mappers** que armamos antes. El repositorio busca una `DonanteEntity` con el ORM, y luego la pasa por un Mapper para devolver un objeto `Donante` puro al dominio.*

---

## 4. Trade-offs (Ventajas y Desventajas)

**¿Por qué usarlo?**
*   **Productividad:** Elimina el 80% del código repetitivo de inserción y lectura (boilerplate SQL).
*   **Portabilidad:** Si mañana decidís cambiar de PostgreSQL a MySQL, solo cambiás la configuración del dialecto de Hibernate. El ORM se encarga de re-escribir el SQL con la sintaxis correcta del nuevo motor.

**¿Qué sacrificás?**
*   **Control y Rendimiento:** En consultas muy complejas o reportes masivos, el SQL generado automáticamente por el ORM suele ser menos eficiente que uno escrito a mano.
*   **Problema N+1:** Es un clásico error de los ORMs donde, por no configurar bien la carga de relaciones (Lazy/Eager), terminás ejecutando cientos de micro-consultas a la base de datos en lugar de un solo `JOIN`, destruyendo la performance.

---

## Referencias

- FreeCodeCamp. (2023). *What is an ORM?* https://www.freecodecamp.org/news/what-is-an-orm-the-meaning-of-object-relational-mapping-database-tools/
- Hibernate ORM. (2024). *Official Documentation*. https://hibernate.org/orm/
- Base de Código DonaTrack: [`DonanteEntity.java`]