# Backend en Spring Boot

Implementación en **Java 21 + Spring Boot 3.3** del mismo diseño hexagonal que `backend-flask`. Expone exactamente la misma API REST (rutas, JSON y códigos de error) en el mismo puerto 8000, así que el frontend de React funciona con cualquiera de los dos backends sin cambiar una línea.

## Ejecutar

Necesitas **JDK 21** y **Maven 3.9+**. Si usas IntelliJ IDEA o VS Code (con el Extension Pack for Java), basta con abrir esta carpeta: el IDE trae Maven y reconoce el `pom.xml`.

```bash
cd backend-spring
mvn spring-boot:run
```

- La API queda en `http://localhost:8000/api`.
- Al primer arranque se cargan 4 médicos, 5 pacientes y 7 citas para el próximo día hábil (`citas.datos-de-ejemplo=true`). Si la base ya tiene datos, no carga nada.
- Los datos se guardan en H2, en el archivo `./datos/citas.mv.db`. Bórralo para empezar de cero.

Luego, en otra terminal, inicia el frontend (`cd frontend && npm run dev`) y abre `http://localhost:5173`.

### Otras formas de ejecutarlo

```bash
# Sin base de datos (todo en memoria, se pierde al reiniciar)
mvn spring-boot:run -Dspring-boot.run.arguments=--citas.persistencia=memoria

# Con PostgreSQL
export DATABASE_URL=jdbc:postgresql://localhost:5432/citas DATABASE_USER=postgres DATABASE_PASSWORD=postgres
mvn spring-boot:run -Dspring-boot.run.profiles=postgres

# Empaquetar y ejecutar como .jar
mvn package
java -jar target/citas-medicas-1.0.0.jar
```

Para agregar el Maven Wrapper (`./mvnw`) al repositorio y que nadie tenga que instalar Maven: `mvn -N wrapper:wrapper`.

## Pruebas

```bash
mvn test
```

| Grupo | Clase | Qué demuestra |
|---|---|---|
| Dominio (47) | `ObjetosDeValorTest`, `CitaTest`, `HorarioYReglasTest` | Reglas puras, sin Spring ni base de datos |
| Casos de uso (21) | `CasosDeUsoDeCitasTest`, `CasosDeUsoDePersonasTest` | El núcleo completo con repositorios en memoria y un reloj fijo |
| Contrato de repositorios (6 + 6) | `RepositoriosEnMemoriaTest`, `RepositoriosJpaTest` | **Liskov**: las mismas pruebas contra memoria y contra JPA/H2 |
| API REST (9) | `ApiDeCitasTest` | Petición HTTP real con MockMvc hasta el dominio y de vuelta |
| Arquitectura (3) | `ArquitecturaHexagonalTest` | **ArchUnit** falla si el dominio importa Spring o si un controlador usa un servicio concreto |

Las pruebas de dominio y casos de uso no arrancan Spring: se ejecutan en milisegundos.

## Estructura de paquetes

```
com.clinica.citas
├── dominio                      ← Java puro: ni una anotación de Spring o JPA
│   ├── modelo                   Cita, Paciente, Medico, EstadoCita, HorarioDeAtencion,
│   │                            objetos de valor (Email, DocumentoIdentidad, IntervaloDeTiempo)
│   ├── reglas                   ReglaDeAgendamiento + una clase por regla (Strategy)
│   └── excepciones              errores del negocio
├── aplicacion                   ← Java puro
│   ├── puertos/entrada          una interfaz por caso de uso, con su Comando (record) anidado
│   ├── puertos/salida           RepositorioDe*, Reloj, Notificador
│   ├── servicios                implementaciones de los casos de uso
│   └── consultas                CriteriosDeBusquedaDeCitas, CitaDetallada
└── infraestructura              ← el único lugar con Spring
    ├── web                      @RestController, DTO (records), ManejadorDeErrores
    ├── persistencia/jpa         @Entity, Spring Data, mapeador y adaptadores
    ├── persistencia/memoria     adaptadores en memoria
    ├── notificaciones, reloj    adaptadores de salida
    └── configuracion            CasosDeUso (ensamblaje), ConfiguracionDelNucleo,
                                 ConfiguracionDePersistencia, ConfiguracionWeb, datos de ejemplo
```

## Cómo se mantiene Spring fuera del núcleo

Lo habitual en Spring es anotar los servicios con `@Service` y los repositorios con `@Repository`. Aquí no se hace a propósito: el dominio y la aplicación no saben que Spring existe.

1. `CasosDeUso.ensamblar(...)` es Java puro: recibe los puertos de salida y construye los once casos de uso. Es la raíz de composición.
2. `ConfiguracionDelNucleo` llama a ese ensamblador y publica cada caso de uso como un bean **de su interfaz** (`AgendarCitaCasoDeUso`, no `AgendarCitaServicio`). Cada controlador recibe solo los puertos que usa.
3. `ConfiguracionDePersistencia` decide qué adaptador implementa los repositorios según `citas.persistencia`.
4. Las pruebas usan el mismo `CasosDeUso.ensamblar(...)` con repositorios en memoria. El ensamblaje existe una sola vez.

`ArquitecturaHexagonalTest` convierte estas decisiones en reglas que se verifican en cada `mvn test`.

## Equivalencias con la versión Flask

| Concepto | Flask (`backend-flask/app/...`) | Spring Boot (`com.clinica.citas...`) |
|---|---|---|
| Entidades y objetos de valor | `dominio/cita.py`, `objetos_valor.py` | `dominio.modelo.Cita`, `Email`, `IntervaloDeTiempo` |
| Tabla de transiciones | `dominio/estado_cita.py` | `dominio.modelo.EstadoCita` |
| Reglas (Strategy) | `dominio/reglas_agendamiento.py` | `dominio.reglas.*` |
| Puertos de entrada | `aplicacion/puertos/entrada.py` | `aplicacion.puertos.entrada.*CasoDeUso` |
| Casos de uso | `aplicacion/casos_de_uso/*.py` | `aplicacion.servicios.*Servicio` |
| Adaptador web | Blueprints en `infraestructura/web/` | `@RestController` en `infraestructura.web` |
| Persistencia | SQLAlchemy | Spring Data JPA (`Specification` para filtros) |
| Raíz de composición | `configuracion/contenedor.py` | `configuracion.CasosDeUso` + `ConfiguracionDelNucleo` |
| Errores → HTTP | tabla en `web/errores.py` | tabla en `web.ManejadorDeErrores` (`@RestControllerAdvice`) |
| Base de datos por defecto | SQLite | H2 en archivo |
