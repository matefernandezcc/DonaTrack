# Guía Práctica: JUnit y Testing en DonaTrack

Esta guía detalla cómo utilizar JUnit 5 (junto con Mockito) para realizar pruebas unitarias, cómo está estructurado el testing dentro de la arquitectura hexagonal del proyecto DonaTrack, y dónde ubicar/ejecutar estos tests.

## 1. Conceptos Básicos de JUnit 5 y Mockito

JUnit 5 (también conocido como JUnit Jupiter) es el framework estándar de Java para pruebas unitarias. En aplicaciones Spring Boot empresariales, suele usarse en conjunto con Mockito, una librería para crear objetos simulados ("mocks") y probar componentes de forma aislada.

### Anotaciones principales de JUnit:
*   `@Test`: Indica que el método es un caso de prueba.
*   `@BeforeEach`: Se ejecuta **antes** de cada test. Ideal para inicializar datos o resetear mocks.
*   `@AfterEach`: Se ejecuta **después** de cada test. Útil para limpiar recursos.
*   `@BeforeAll` / `@AfterAll`: Se ejecutan una sola vez antes/después de todos los tests de la clase (deben ser estáticos).

### Aserciones (Assertions):
Son validaciones para comprobar que el resultado es el esperado:
*   `assertEquals(esperado, real)`: Verifica que dos valores sean iguales.
*   `assertTrue(condicion)` / `assertFalse(condicion)`: Verifica valores booleanos.
*   `assertNotNull(objeto)`: Verifica que un objeto no sea nulo.
*   `assertThrows(Excepcion.class, () -> metodo())`: Verifica que un método lance una excepción específica.

### Mockito (Simulación de dependencias):
En arquitectura hexagonal, rara vez probamos todo el sistema junto. Probamos un componente simulando el comportamiento de las dependencias externas (bases de datos, colas, etc.).
*   `mock(Clase.class)` o `@Mock`: Crea una simulación de un objeto.
*   `when(mock.metodo()).thenReturn(valor)`: Configura el mock para que devuelva algo específico cuando se le llame.
*   `verify(mock, times(1)).metodo()`: Verifica que un método del mock haya sido llamado una cantidad exacta de veces.

---

## 2. Testing en la Arquitectura de DonaTrack

DonaTrack utiliza Arquitectura Hexagonal. Esto significa que los tests se enfocan en diferentes capas con distintos propósitos:

### A. Testing del Dominio (Entities y Domain Services)
Son los tests más puros. No tienen mocks complejos, solo instancian objetos y verifican reglas de negocio.
*   *Ejemplo:* Probar que `PerfilDonante.procesarInactividad()` rompe la racha a 0 si pasaron más de 30 días.
*   *Ubicación típica:* `src/test/java/.../domain/entities/`

### B. Testing de Casos de Uso (Application Layer)
Aquí probamos la lógica de orquestación. Como los casos de uso dependen de puertos (interfaces) para comunicarse con BD o colas, **usamos Mockito** para simular esos puertos.
*   *Ejemplo:* Probar `PlanificacionRutasUseCase`. Simulamos el `RutaDeRepartoRepository` y verificamos que el caso de uso llame a `save()` con los datos correctos, sin tocar una BD real.
*   *Ubicación típica:* `src/test/java/.../application/usecases/`

### C. Testing de Infraestructura (Controllers / Listeners)
Probamos que los endpoints mapeen bien JSONs o que los listeners de RabbitMQ reaccionen a eventos.
*   *Ejemplo:* Probar que `LogisticaController` responda `200 OK` al llamar a `/api/rutas/{id}/iniciar`.
*   *Ubicación típica:* `src/test/java/.../infrastructure/adapters/in/api/`

---

## 3. Maven vs JUnit: ¿Cuál es la diferencia?

Antes de avanzar, es fundamental entender la diferencia entre estas dos herramientas, ya que trabajan en conjunto pero tienen propósitos totalmente distintos:

*   **JUnit:** Es un **framework (librería)** que te permite escribir el código de las pruebas. Proporciona las anotaciones (`@Test`) y los métodos (`assertEquals`) para comprobar si tu código hace lo que debe. JUnit *no* sabe cómo construir tu proyecto ni gestionar carpetas.
*   **Maven:** Es una **herramienta de construcción y gestión de proyectos**. Maven dicta la estructura estándar de las carpetas, descarga las dependencias (como el propio JUnit), compila tu código y, finalmente, **orquesta la ejecución** de todas las pruebas de forma automatizada.

En resumen: **Tú escribes los tests usando JUnit, y Maven es el motor que compila el proyecto y los ejecuta.**

---

## 4. ¿Dónde encontrar los tests en el proyecto?

Los tests en los proyectos gestionados por Maven siempre se ubican en la carpeta `src/test/java` paralela a `src/main/java`. En DonaTrack, al ser un proyecto multi-módulo, cada microservicio tiene su propia carpeta de tests.

Aquí tienes ejemplos clave de dónde buscar:

*   **Donaciones:**
    *   `/donatrack-donaciones/src/test/java/com/donatrack/donaciones/...`
    *   Contiene tests para `SegmentadorTest`, `ImportadorCSVTest`, `MatchmakerServiceTest`, etc.
*   **Logística:**
    *   `/donatrack-logistica/src/test/java/com/donatrack/logistica/...`
    *   Contiene tests para rutas, entregas y los Use Cases de planificación (`PlanificacionRutasUseCaseTest`).
*   **Incentivos:**
    *   `/donatrack-incentivos/src/test/java/com/donatrack/incentivos/...`
    *   Contiene tests para misiones, rachas (`PerfilDonanteTest`) y ranking mensual.
*   **Notificaciones:**
    *   `/donatrack-notificaciones/src/test/java/com/donatrack/notificaciones/...`
    *   Contiene tests para fábricas de notificaciones y estrategias (Email, WhatsApp).

---

## 5. ¿Cómo ejecutar los tests?

### Desde la Terminal (Maven)
Situado en la raíz del proyecto (`DonaTrack/`):
1.  **Correr todos los tests de todos los módulos:**
    ```bash
    mvn test
    ```
2.  **Limpiar compilaciones viejas y correr tests:**
    ```bash
    mvn clean test
    ```
3.  **Correr tests de un solo módulo (ej: logística):**
    ```bash
    mvn test -pl donatrack-logistica
    ```

### Desde el IDE (IntelliJ IDEA / Eclipse / VS Code)
1.  Ve a cualquier archivo en `src/test/java`.
2.  Haz clic derecho sobre la clase o el método anotado con `@Test`.
3.  Selecciona **Run 'NombreDelTest'**.
4.  También puedes hacer clic derecho en la carpeta `test` completa y ejecutar todos los tests de ese paquete simultáneamente.
