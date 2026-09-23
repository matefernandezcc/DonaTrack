# Mappers en Java y su Uso en Arquitectura Hexagonal

## ¿Qué es un Mapper?

Un **Mapper** es un componente de software (generalmente una clase o interfaz) cuya única responsabilidad es transferir y convertir datos entre dos objetos diferentes que no deben tener conocimiento el uno del otro. 

En Java, esto suele verse como una clase con métodos estáticos o de instancia que reciben un objeto "A" y devuelven un objeto "B" mapeando sus atributos uno a uno (ej: de `UsuarioEntity` a `UsuarioDomain`). Este concepto tiene sus raíces en el patrón **Data Mapper** introducido por Martin Fowler.

## ¿Para qué se usa? (El "Por Qué")

En arquitecturas limpias como la **Arquitectura Hexagonal** o *Ports and Adapters*, el núcleo de tu aplicación (la lógica de negocio o dominio) debe estar completamente aislado de las herramientas externas (bases de datos, APIs web, colas de mensajes).

- Si la base de datos cambia (ej: pasás de SQL a MongoDB), tu lógica de negocio no debería enterarse.
- Si la interfaz web cambia de JSON a XML, tu lógica de negocio tampoco debería enterarse.

**Ahí entra el Mapper:** 
Sirve como un "traductor" en las fronteras de tu sistema. Toma los objetos que vienen de la base de datos (Entidades JPA) y los convierte en objetos puros de Java (Entidades de Dominio) para que tu lógica trabaje tranquila. Luego, hace el camino inverso para guardar. 

> [!TIP]
> **Data Mapper vs Active Record**: En frameworks como Ruby on Rails (Active Record), el objeto de negocio sabe cómo guardarse a sí mismo en la base de datos (`usuario.save()`). Con el patrón Data Mapper, el objeto de negocio es un simple POJO (Plain Old Java Object) y el Mapper se encarga del trabajo sucio de traducción para aislar responsabilidades.

---

## Mappers en DonaTrack (Ejemplo Práctico)

En el TP de DonaTrack, los mappers se usan intensivamente en los **Adaptadores de Persistencia** para aislar los Casos de Uso de las anotaciones de Hibernate/JPA (`@Entity`, `@Table`, etc.).

### Ejemplo: `DonacionMapper.java`

Si revisamos el módulo de Donaciones, vas a ver que el repositorio de persistencia nunca devuelve una `DonacionEntity` a los casos de uso, sino que usa un Mapper en el medio:

```java
// Archivo: donatrack-donaciones/src/main/java/com/donatrack/donaciones/infrastructure/adapters/out/persistence/mappers/DonacionMapper.java

public class DonacionMapper {

  // Convierte desde Dominio puro hacia la Entidad de Base de Datos (JPA)
  public static DonacionEntity toEntity(Donacion domain) {
    if (domain == null) return null;

    DonacionEntity entity = new DonacionEntity();
    entity.setId(domain.getId());
    
    if (domain.getEstado() != null) {
      entity.setEstado(domain.getEstado().name()); // Enum a String para la DB
    }

    return entity;
  }

  // Convierte desde la Base de Datos (JPA) hacia el Dominio puro
  public static Donacion toDomain(DonacionEntity entity) {
    if (entity == null) return null;

    // Se crea un objeto puro del negocio sin anotaciones @Entity
    Donacion domain = new Donacion(null);
    domain.setId(entity.getId());
    
    if (entity.getEstado() != null) {
      domain.setEstado(EstadoDonacion.valueOf(entity.getEstado())); // String a Enum
    }

    // Incluso mapea objetos anidados usando otros Mappers (RolMapper)
    if (entity.getNecesidad() != null && entity.getNecesidad().getBeneficiario() != null) {
      domain.setEntidadAsignada((Beneficiario) RolMapper.toDomain(entity.getNecesidad().getBeneficiario()));
    }

    return domain;
  }
}
```

### ¿Cómo se invoca este Mapper en el TP?

El adaptador (`JpaDonacionRepository`) invoca este mapper justo antes de devolver los datos al caso de uso:

```java
@Override
public Optional<Donacion> buscarPorId(UUID id) {
    // 1. Busca la Entity en la DB
    // 2. Ejecuta el Mapper con .map()
    // 3. Devuelve un objeto de Dominio puro
    return jpaRepository.findById(id).map(DonacionMapper::toDomain);
}
```

## Trade-offs de usar Mappers

- **Ventajas**: Desacoplamiento total. Podés reestructurar completamente tus tablas de base de datos (`DonacionEntity`) sin tener que tocar ni una sola línea de tu lógica de negocio (`Donacion`), solo actualizás el Mapper.
- **Desventajas**: Exceso de código repetitivo (Boilerplate). Por cada entidad, tenés que escribir una clase extra que copia atributo por atributo. 

*(Nota: En sistemas más grandes, para evitar escribir los mappers a mano, se suelen usar librerías de autogeneración de código como **MapStruct** o **ModelMapper**, pero en DonaTrack están armados manualmente para mayor control).*

---

## Referencias

- Fowler, M. (2003). *Data Mapper Pattern*. Patterns of Enterprise Application Architecture. https://martinfowler.com/eaaCatalog/dataMapper.html
- Base de Código DonaTrack: [`DonacionMapper.java`]
- Base de Código DonaTrack: [`JpaDonacionRepository.java`]
