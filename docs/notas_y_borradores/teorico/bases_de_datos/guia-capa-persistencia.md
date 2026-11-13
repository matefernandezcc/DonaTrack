# Guía: Capa de Persistencia en Arquitectura Hexagonal (DonaTrack)

## ¿Por qué hay tantos archivos para guardar un dato?

En una arquitectura hexagonal, la capa de dominio (tu lógica de negocio) **no sabe nada** de base de datos. Esto es intencional: si mañana cambiás de PostgreSQL a MongoDB, o de JPA a JDBC puro, **el dominio no cambia ni una línea**. Para lograr eso, se necesitan varias piezas intermedias.

---

## Las 6 piezas de la persistencia (de adentro hacia afuera)

### 1. 🧠 Domain Entity (Objeto de Dominio)
**Ubicación:** `domain/entities/`  
**Ejemplo:** `Camion.java`

```java
public class Camion {
    private String patente;
    private Double capacidadVolumen;
    private Double altura;
    private Double capacidadCarga;
}
```

**¿Qué es?** Es tu objeto de negocio puro. No tiene ninguna anotación de base de datos (`@Entity`, `@Table`, `@Column`). Solo tiene atributos, getters/setters y métodos de negocio (como `iniciarRuta()`).

**Analogía:** Es el "concepto" real. Si le explicás a un camionero qué es un Camión, le decís esto: tiene patente, capacidad, etc.

---

### 2. 🔌 Port (Interfaz del Repositorio)
**Ubicación:** `application/ports/out/`  
**Ejemplo:** `CamionRepositoryPort.java`

```java
public interface CamionRepositoryPort {
    void guardar(Camion camion);
    Optional<Camion> buscarPorPatente(String patente);
    List<Camion> obtenerTodos();
    void eliminar(String patente);
}
```

**¿Qué es?** Es un **contrato** (interfaz) que dice "yo necesito poder guardar camiones y buscarlos, pero **no me importa cómo**". El dominio solo conoce esta interfaz, nunca sabe si detrás hay una base de datos, un archivo, o un servicio web.

**Analogía:** Es como un enchufe de la pared. El enchufe (Port) dice "dame electricidad", no le importa si viene de una central nuclear o un panel solar.

---

### 3. 🗄️ JPA Entity (Entidad de Base de Datos)
**Ubicación:** `infrastructure/adapters/out/persistence/entities/`  
**Ejemplo:** `CamionEntity.java`

```java
@Entity
@Table(name = "camiones", schema = "logistica")
public class CamionEntity {
    @Id
    @Column(name = "patente", length = 10)
    private String patente;

    @Column(name = "capacidad_volumen")
    private Double capacidadVolumen;
    // ...
}
```

**¿Qué es?** Es la **representación del dato en la base de datos**. Tiene todas las anotaciones JPA (`@Entity`, `@Table`, `@Column`, `@ManyToOne`, etc.) que le dicen a Hibernate cómo crear las tablas y columnas.

**¿Por qué no usar la Domain Entity directamente?** Porque si le ponés `@Entity` a `Camion.java`, tu dominio queda **acoplado** a JPA. Si mañana cambiás de tecnología, tenés que tocar el dominio. Tener dos clases separadas mantiene la independencia.

**Analogía:** Si el Domain Entity es el "concepto de camión", la JPA Entity es el "formulario de la base de datos" que dice cómo se guarda ese camión en una tabla con columnas específicas.

**Variante - Embeddable:** Algunos objetos no son tablas propias sino que se embeben dentro de otra tabla. Se anotan con `@Embeddable`:
- `DireccionEmbeddable.java` → las columnas `calle`, `altura_dir`, `localidad` se agregan directamente a la tabla que lo usa
- `CoordenadaEmbeddable.java` → las columnas `latitud`, `longitud` van en la tabla padre
- `ComprobanteRecepcionEmbeddable.java` → `comprobante_fecha_hora`, `comprobante_fotos`, etc. van en la tabla `entregas`

---

### 4. 📡 Spring Data Repository (JpaRepository)
**Ubicación:** `infrastructure/adapters/out/persistence/repositories/`  
**Ejemplo:** `CamionJpaRepository.java`

```java
@Repository
public interface CamionJpaRepository extends JpaRepository<CamionEntity, String> {
}
```

**¿Qué es?** Es una interfaz de **Spring Data** que hereda de `JpaRepository`. Spring genera automáticamente toda la implementación: `save()`, `findById()`, `findAll()`, `deleteById()`, etc. Vos no escribís ni una línea de SQL.

**Lo importante:** trabaja con `CamionEntity` (la JPA Entity), **no** con `Camion` (el dominio). Habla el idioma de la base de datos.

**¿Por qué se llama `CamionJpaRepository`?** Para distinguirlo del Adapter (que se llama `JpaCamionRepository`). Los nombres son parecidos a propósito pero no son lo mismo:

| Archivo | Tipo | Trabaja con | ¿Quién lo usa? |
|---------|------|-------------|----------------|
| `CamionJpaRepository` | Interfaz de Spring Data | `CamionEntity` | Solo el Adapter |
| `JpaCamionRepository` | Clase Adapter | `Camion` (dominio) | Los Use Cases |

---

### 5. 🔄 Mapper (Traductor)
**Ubicación:** `infrastructure/adapters/out/persistence/mappers/`  
**Ejemplo:** `CamionMapper.java`

```java
public final class CamionMapper {
    // Dominio → Base de datos
    public static CamionEntity toEntity(Camion domain) {
        return new CamionEntity(
            domain.getPatente(),
            domain.getCapacidadVolumen(),
            domain.getAltura(),
            domain.getCapacidadCarga());
    }

    // Base de datos → Dominio
    public static Camion toDomain(CamionEntity entity) {
        return new Camion(
            entity.getPatente(),
            entity.getCapacidadVolumen(),
            entity.getAltura(),
            entity.getCapacidadCarga());
    }
}
```

**¿Qué es?** Es el **traductor** entre los dos mundos. Convierte un `Camion` (dominio) en un `CamionEntity` (base de datos) y viceversa. Tiene dos métodos:
- `toEntity()`: cuando querés **guardar** algo → traducís de dominio a base
- `toDomain()`: cuando querés **leer** algo → traducís de base a dominio

**Analogía:** Si tenés un libro en español (dominio) y necesitás guardarlo en una biblioteca alemana (base de datos), el Mapper es el traductor que lo pasa a alemán para guardarlo y lo traduce al español cuando lo leés.

---

### 6. 🔌 Adapter (Implementación del Port)
**Ubicación:** `infrastructure/adapters/out/persistence/`  
**Ejemplo:** `JpaCamionRepository.java`

```java
@Repository
public class JpaCamionRepository implements CamionRepositoryPort {

    private final CamionJpaRepository jpaRepository;

    @Override
    public void guardar(Camion camion) {
        jpaRepository.save(CamionMapper.toEntity(camion));
        //                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
        //                 1. Traduce Camion → CamionEntity
        //                 2. Llama a Spring Data para guardar
    }

    @Override
    public Optional<Camion> buscarPorPatente(String patente) {
        return jpaRepository.findById(patente).map(CamionMapper::toDomain);
        //                                        ^^^^^^^^^^^^^^^^^^^^^^^
        //                                        Traduce CamionEntity → Camion
    }
}
```

**¿Qué es?** Es la clase que **conecta** todo. Implementa el Port del dominio (`CamionRepositoryPort`) usando las herramientas de infraestructura (Spring Data + Mapper). Es la pieza que "enchufa" la base de datos al dominio.

**¿Por qué `implements CamionRepositoryPort`?** Porque el dominio solo conoce la interfaz (el Port). Cuando un Use Case pide `camionRepository.guardar(camion)`, Spring le inyecta automáticamente este Adapter sin que el Use Case sepa que hay JPA detrás.

---

## ¿Y qué era un Mock?

### 🎭 Mock Repository (ya eliminados)
**Ejemplo (eliminado):** `MockCamionRepository.java`

```java
@Repository
public class MockCamionRepository implements CamionRepositoryPort {
    private final List<Camion> baseDeDatosMock = new ArrayList<>();

    @Override
    public void guardar(Camion camion) {
        baseDeDatosMock.add(camion);
    }
}
```

**¿Qué era?** Una implementación **falsa** del Port que guardaba todo en una lista en memoria (RAM). Cuando reiniciabas la app, perdías todos los datos.

**¿Para qué servía?** Para poder desarrollar y testear la lógica de negocio **sin necesitar una base de datos real**. Mientras se resolvía la persistencia, los Mocks permitían que todo funcione.

**¿Por qué los eliminamos?** Porque ahora tenemos la implementación real con JPA (el Adapter). Si existieran ambos, Spring no sabría cuál usar (conflicto de beans: dos clases implementando la misma interfaz con `@Repository`).

---

## El flujo completo: ¿Qué pasa cuando guardás un Camión?

```
Use Case                     Port                        Adapter                    Mapper              Spring Data         Base de Datos
   |                          |                            |                          |                    |                    |
   |-- guardar(Camion) ------>|                            |                          |                    |                    |
   |                          |-- guardar(Camion) -------->|                          |                    |                    |
   |                          |                            |-- toEntity(Camion) ----->|                    |                    |
   |                          |                            |<---- CamionEntity -------|                    |                    |
   |                          |                            |-- save(CamionEntity) ----|------ INSERT ----->|
   |                          |                            |                          |                    |     ✅ Guardado    |
```

1. El **Use Case** llama a `camionRepository.guardar(camion)` usando el **Port** (interfaz)
2. Spring inyecta el **Adapter** (`JpaCamionRepository`) como implementación
3. El Adapter usa el **Mapper** para traducir `Camion` → `CamionEntity`
4. El Adapter llama a **Spring Data** (`CamionJpaRepository.save()`)
5. Spring Data genera el SQL y lo ejecuta contra la **base de datos**

---

## ¿Por qué los nombres son tan parecidos?

| Nombre | Ubicación | Tipo | Responsabilidad |
|--------|-----------|------|----------------|
| `CamionJpaRepository` | `repositories/` | **Interfaz** Spring Data | Habla con la base de datos usando `CamionEntity` |
| `JpaCamionRepository` | raíz de `persistence/` | **Clase** Adapter | Implementa el Port del dominio, traduce y delega |

**La convención de nombres:**
- `XxxJpaRepository` → interfaz Spring Data (Spring la implementa automáticamente)
- `JpaXxxRepository` → adapter que envuelve al Spring Data (vos la escribís)

**¿Por qué no fusionarlos en uno solo?** Porque `CamionJpaRepository` (Spring Data) trabaja con `CamionEntity` y el dominio trabaja con `Camion`. Necesitás al Adapter en el medio para hacer la traducción con el Mapper. Si el dominio usara directamente el Spring Data Repository, estaría acoplado a JPA.

---

## Estructura de carpetas resumida

```
persistence/
├── entities/                          ← 🗄️ JPA Entities (representación en la base)
│   ├── CamionEntity.java
│   ├── ChoferEntity.java
│   ├── EntregaEntity.java
│   ├── ParadaEntity.java
│   ├── RutaDeRepartoEntity.java
│   ├── ItemPlanificacionEntity.java
│   ├── SolicitudPlanificacionEntity.java
│   ├── ComprobanteRecepcionEmbeddable.java   ← se embebe en EntregaEntity
│   ├── CoordenadaEmbeddable.java             ← se embebe en ParadaEntity
│   └── DireccionEmbeddable.java              ← se embebe en ParadaEntity
│
├── repositories/                      ← 📡 Spring Data Repos (generación automática de SQL)
│   ├── CamionJpaRepository.java
│   ├── ChoferJpaRepository.java
│   ├── EntregaJpaRepository.java
│   ├── RutaDeRepartoJpaRepository.java
│   ├── ItemPlanificacionJpaRepository.java
│   └── SolicitudPlanificacionJpaRepository.java
│
├── mappers/                           ← 🔄 Mappers (traductores dominio ↔ base)
│   ├── CamionMapper.java
│   ├── ChoferMapper.java
│   ├── EntregaMapper.java
│   ├── RutaDeRepartoMapper.java
│   ├── ItemPlanificacionMapper.java
│   └── SolicitudPlanificacionMapper.java
│
├── JpaCamionRepository.java           ← 🔌 Adapters (conectan el dominio con la base)
├── JpaChoferRepository.java
├── JpaEntregaRepository.java
├── JpaRutaDeRepartoRepository.java
├── JpaItemPlanificacionRepository.java
└── JpaSolicitudPlanificacionRepository.java
```

---

## Resumen en una tabla

| Pieza | ¿Qué es? | ¿Quién la escribe? | ¿Con qué trabaja? |
|-------|----------|--------------------|--------------------|
| Domain Entity | Objeto de negocio puro | Nosotros | Nada de infraestructura |
| Port | Interfaz que declara qué operaciones necesita el dominio | Nosotros | Domain Entities |
| JPA Entity | Representación del dato para la base de datos | Nosotros | Anotaciones JPA |
| Spring Data Repo | Interfaz que Spring implementa automáticamente con SQL | Nosotros (solo la interfaz) | JPA Entities |
| Mapper | Traductor entre Domain Entity y JPA Entity | Nosotros | Ambas |
| Adapter | Implementación del Port que conecta todo | Nosotros | Port + Mapper + Spring Data Repo |
| Mock (eliminado) | Implementación falsa del Port con datos en memoria | Nosotros | Domain Entities + ArrayList |
