# Citas Médicas: aplicación web con Arquitectura Hexagonal

Sistema para la recepción de una clínica: registra pacientes y médicos, muestra la agenda del día por médico y permite agendar, confirmar, atender, reprogramar y cancelar citas respetando las reglas del negocio.

![Agenda del día: una columna por médico y una fila por franja de 30 minutos](docs/captura-agenda.png)

| Capa | Tecnología |
|---|---|
| Backend | Dos implementaciones intercambiables del mismo diseño: **Flask 3** (Python 3.11+, SQLAlchemy 2) en `backend-flask/` y **Spring Boot 3.3** (Java 21, Spring Data JPA) en `backend-spring/`. Exponen la misma API en el mismo puerto. |
| Frontend | **React 18** + TypeScript, Vite, React Router |
| Base de datos | SQLite (Flask) o H2 (Spring) por defecto, PostgreSQL cambiando la configuración, o persistencia en memoria sin base de datos. |
| Pruebas | pytest (88 pruebas), JUnit 5 + ArchUnit (92 pruebas) y Vitest (12 pruebas del dominio en el frontend) |

---

## 1. El problema

Una clínica pequeña agenda citas por teléfono y en papel. Eso produce dos médicos con el mismo paciente a la misma hora, citas fuera del horario, cancelaciones que nadie registra y pacientes que no saben que su cita cambió.

La aplicación resuelve eso con reglas que se aplican siempre, sin importar desde dónde llegue la petición:

1. La cita debe ser en el futuro.
2. Solo de lunes a viernes, de 08:00 a 18:00.
3. Las citas duran 30 minutos y empiezan en bloques exactos (08:00, 08:30, 09:00…).
4. Un médico no puede tener dos citas que se crucen.
5. Un paciente no puede estar en dos consultas al mismo tiempo, aunque sean con médicos distintos.
6. Ciclo de vida controlado: `PROGRAMADA → CONFIRMADA → ATENDIDA`, y desde las dos primeras se puede pasar a `CANCELADA`.
7. Solo se cancela o reprograma una cita que no ha comenzado; solo se marca como atendida una que ya comenzó.
8. Reprogramar devuelve la cita a `PROGRAMADA`, porque el paciente debe confirmar la nueva fecha.
9. No se repiten documentos de pacientes ni registros profesionales de médicos.
10. Cada vez que se agenda, reprograma o cancela, se notifica al paciente.

### ¿Por qué Arquitectura Hexagonal?

Estas reglas son el corazón del sistema y no deberían depender de Flask, de Spring, de la base de datos ni de cómo se envían los correos. Con la arquitectura hexagonal (puertos y adaptadores):

- Las reglas viven en un núcleo que no importa ningún framework y se prueba en milisegundos.
- La base de datos se puede cambiar (SQLite, PostgreSQL o memoria) sin tocar una línea del núcleo. Las pruebas lo demuestran ejecutando el mismo contrato contra dos implementaciones.
- El notificador hoy escribe en el log; mañana puede enviar correos o SMS creando otro adaptador.
- La hora actual es un puerto (`Reloj`), así las pruebas fijan la fecha y son repetibles.
- La demostración más clara: el mismo diseño está implementado dos veces, en Flask y en Spring Boot, y el mismo frontend funciona con cualquiera de los dos sin cambiar nada. Lo único que cambia entre ambos son los adaptadores.

---

## 2. Arquitectura

```mermaid
flowchart LR
    subgraph FE["Frontend (React)"]
        UI["Componentes y hooks"] --> PF["Puertos TS<br/>(interfaces)"]
        AH["Adaptadores HTTP"] -. implementa .-> PF
    end

    AH -- "JSON / HTTP" --> WEB

    subgraph BE["Backend (Flask o Spring Boot)"]
        WEB["Adaptador web<br/>Blueprints / @RestController"] --> PE["Puertos de entrada<br/>(casos de uso)"]
        subgraph NUCLEO["Núcleo: no depende de nada externo"]
            PE --> CU["Servicios de aplicación"]
            CU --> DOM["Dominio<br/>Cita, Paciente, Médico,<br/>reglas de agendamiento"]
            CU --> PS["Puertos de salida<br/>Repositorios, Reloj, Notificador"]
        end
        SQL["Repositorios SQL<br/>SQLAlchemy / Spring Data JPA"] -. implementa .-> PS
        MEM["Repositorios en memoria"] -. implementa .-> PS
        NOT["Notificador por registro"] -. implementa .-> PS
        REL["Reloj del sistema"] -. implementa .-> PS
        SQL --> DB[("SQLite / H2 / PostgreSQL")]
    end
```

La regla de oro: **las dependencias apuntan hacia adentro**. `dominio` no importa nada de `aplicacion` ni de `infraestructura`; `aplicacion` solo conoce al dominio y a sus propios puertos; `infraestructura` conoce a todos y es la única que importa Flask, Spring, SQLAlchemy o JPA. El único lugar que une interfaces con clases concretas es la raíz de composición (`contenedor.py` en Flask, `CasosDeUso.java` en Spring). En la versión Spring, una prueba de ArchUnit falla si alguien rompe esta regla.

### Estructura de carpetas

```
citas-medicas/
├── backend-flask/                      ← backend en Python (detalle abajo)
├── backend-spring/                     ← backend en Java, mismo diseño (ver backend-spring/README.md)
│   └── src/main/java/com/clinica/citas/
│       ├── dominio/                    Java puro: modelo, reglas, excepciones
│       ├── aplicacion/                 puertos de entrada y salida, servicios
│       └── infraestructura/            web, persistencia (jpa, memoria), configuración
├── frontend/                           ← React (detalle abajo)
└── docs/

backend-flask/
├── app/
│   ├── dominio/                    ← núcleo: reglas del negocio puras
│   │   ├── cita.py                 agregado principal con sus transiciones
│   │   ├── estado_cita.py          tabla de transiciones de estado
│   │   ├── paciente.py, medico.py  entidades
│   │   ├── objetos_valor.py        Email, DocumentoIdentidad, IntervaloDeTiempo
│   │   ├── horario_atencion.py     días, horas y duración de las citas
│   │   ├── reglas_agendamiento.py  una clase por regla (Strategy)
│   │   ├── validaciones.py, entidad.py, excepciones.py
│   ├── aplicacion/                 ← casos de uso y puertos
│   │   ├── puertos/entrada.py      una interfaz por caso de uso
│   │   ├── puertos/salida/         repositorios, reloj, notificador
│   │   ├── casos_de_uso/           implementaciones (pacientes, médicos, citas)
│   │   ├── comandos.py             DTO de entrada
│   │   ├── criterios.py, vistas.py
│   └── infraestructura/            ← adaptadores
│       ├── web/                    Flask: rutas, lectura de peticiones, errores, JSON
│       ├── persistencia/sql/       SQLAlchemy: tablas, mapeadores, repositorios
│       ├── persistencia/memoria/   repositorios en memoria
│       ├── notificaciones/         notificador que escribe en el log
│       ├── reloj/                  reloj del sistema
│       └── configuracion/          contenedor de dependencias, ajustes, datos de ejemplo
├── tests/
│   ├── unidad/dominio/             reglas sin base de datos ni HTTP
│   ├── unidad/aplicacion/          casos de uso con repositorios en memoria y reloj fijo
│   └── integracion/                contrato de repositorios (memoria y SQL) y API completa
└── wsgi.py                         punto de entrada

frontend/
└── src/
    ├── dominio/                        tipos, reglas de estados, fechas, horario
    ├── aplicacion/puertos.ts           interfaces que necesita la interfaz
    ├── infraestructura/http/           adaptadores que hablan con la API
    └── ui/                             componentes, páginas y hooks de React

docs/
└── REFACTORING.md                      catálogo de refactorizaciones aplicadas
```

---

## 3. Cómo ejecutarlo

Necesitas **Node.js 18 o superior** para el frontend y, según el backend que elijas, **Python 3.11+** o **JDK 21 con Maven**. Los dos backends usan el puerto 8000, así que ejecuta solo uno a la vez.

### Backend, opción A: Flask

```bash
cd backend-flask
python -m venv .venv
# Windows: .venv\Scripts\activate      macOS/Linux: source .venv/bin/activate
pip install -r requirements-dev.txt

flask --app wsgi cargar-ejemplos        # 4 médicos, 5 pacientes y 7 citas para el próximo día hábil
flask --app wsgi run --debug --port 8000
```

La API queda en `http://localhost:8000/api`. Se usa el puerto 8000 porque en macOS el 5000 lo ocupa AirPlay.

### Backend, opción B: Spring Boot

```bash
cd backend-spring
mvn spring-boot:run                     # carga los datos de ejemplo automáticamente
```

También puedes abrir la carpeta en IntelliJ IDEA y ejecutar `AplicacionCitasMedicas`. Más opciones (PostgreSQL, modo memoria, .jar) en [`backend-spring/README.md`](backend-spring/README.md).

### Frontend

En otra terminal:

```bash
cd frontend
npm install
npm run dev
```

Abre `http://localhost:5173`. Vite reenvía las peticiones `/api` al backend, así que no hace falta configurar nada más.

### Pruebas

```bash
cd backend-flask  && pytest      # 88 pruebas
cd backend-spring && mvn test    # 92 pruebas, incluidas las de arquitectura con ArchUnit
cd frontend && npm test        # 12 pruebas
cd frontend && npm run build   # verifica tipos y genera la versión de producción
```

### Cambiar la base de datos

En Flask, copia `backend-flask/.env.example` como variables de entorno. Para PostgreSQL:

```bash
pip install "psycopg[binary]"
export DATABASE_URL="postgresql+psycopg://usuario:clave@localhost:5432/citas"
```

Para una demostración sin base de datos: `export PERSISTENCIA=memoria`.

En Spring Boot, activa el perfil `postgres` (`mvn spring-boot:run -Dspring-boot.run.profiles=postgres`) o usa `--citas.persistencia=memoria`.

---

## 4. API REST

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/horario` | Horario de atención (lo usa el frontend para dibujar la agenda) |
| GET | `/api/pacientes` | Lista de pacientes |
| POST | `/api/pacientes` | Registrar paciente `{nombre, documento, email, telefono?, fechaNacimiento?}` |
| GET | `/api/medicos?especialidad=` | Lista de médicos, con filtro opcional |
| POST | `/api/medicos` | Registrar médico `{nombre, especialidad, registroProfesional, email}` |
| GET | `/api/medicos/especialidades` | Especialidades disponibles |
| GET | `/api/medicos/{id}/disponibilidad?fecha=AAAA-MM-DD` | Horas libres del médico ese día |
| GET | `/api/citas?fecha=&medicoId=&pacienteId=&estado=` | Buscar citas |
| POST | `/api/citas` | Agendar `{pacienteId, medicoId, inicio: "AAAA-MM-DDTHH:MM", motivo}` |
| POST | `/api/citas/{id}/confirmar` | Confirmar asistencia |
| POST | `/api/citas/{id}/atender` | Marcar como atendida |
| POST | `/api/citas/{id}/reprogramar` | Reprogramar `{inicio}` |
| POST | `/api/citas/{id}/cancelar` | Cancelar `{motivo}` |

Los errores siempre responden `{ "mensaje": "...", "tipo": "..." }`: **400** datos mal formados, **404** no existe, **409** duplicado, **422** regla de negocio incumplida.

---

## 5. Principios SOLID en el código

| Principio | Dónde se aplica | Archivo |
|---|---|---|
| **S** Responsabilidad única | Cada caso de uso es una clase con un solo método `ejecutar`. Los controladores solo traducen HTTP ↔ comandos. Los mapeadores solo traducen filas ↔ entidades. `DetalladorDeCitas` solo une citas con personas. | `aplicacion/casos_de_uso/*.py`, `infraestructura/web/rutas_*.py`, `persistencia/sql/mapeadores.py` |
| **O** Abierto/Cerrado | Las reglas de agendamiento son estrategias: para una regla nueva se crea una clase y se agrega a la lista, sin modificar `ValidadorDeAgendamiento` ni `AgendarCita`. Lo mismo con la tabla de códigos HTTP, la tabla de transiciones de estado y la tabla de fábricas de repositorios. | `dominio/reglas_agendamiento.py`, `web/errores.py`, `dominio/estado_cita.py`, `configuracion/contenedor.py` |
| **L** Sustitución de Liskov | Los repositorios en memoria y SQL son intercambiables. La prueba `test_contrato_de_repositorios.py` ejecuta **las mismas** pruebas contra ambos. El repositorio en memoria guarda copias para comportarse igual que una base de datos real. | `tests/integracion/test_contrato_de_repositorios.py` |
| **I** Segregación de interfaces | Un puerto de entrada por caso de uso, un repositorio por agregado y un `Notificador` con solo tres métodos. Un controlador que agenda no depende de métodos para cancelar. | `aplicacion/puertos/entrada.py`, `aplicacion/puertos/salida/` |
| **D** Inversión de dependencias | Los casos de uso reciben interfaces por el constructor (`RepositorioDeCitas`, `Reloj`, `Notificador`), nunca clases concretas. Solo `contenedor.py` conoce las implementaciones. En el frontend, los componentes piden `usePuertos()` y la raíz (`main.tsx`) inyecta los adaptadores HTTP. | `configuracion/contenedor.py`, `frontend/src/main.tsx`, `ui/contexto/ContextoDePuertos.tsx` |

Las rutas de la tabla son de la versión Flask. En Spring Boot los mismos principios están en las clases equivalentes (`dominio.reglas`, `aplicacion.puertos`, `infraestructura.configuracion`); la tabla de equivalencias está en [`backend-spring/README.md`](backend-spring/README.md). Spring añade una garantía extra: `ArquitecturaHexagonalTest` (ArchUnit) verifica en cada compilación que el dominio y la aplicación no dependan de Spring ni de JPA, y que los controladores usen puertos en lugar de servicios concretos.

### Ejemplo de Abierto/Cerrado: agregar una regla

Supongamos que la clínica decide que un paciente no puede tener más de dos citas el mismo día. Solo se agrega esto:

```python
# dominio/reglas_agendamiento.py
class MaximoDosCitasPorDia(ReglaDeAgendamiento):
    def verificar(self, cita, contexto):
        vigentes = [c for c in contexto.agenda_del_paciente if c.ocupa_agenda and c.id != cita.id]
        if len(vigentes) >= 2:
            raise ReglaDeNegocioIncumplida("El paciente ya tiene dos citas ese día.")
```

y se añade `MaximoDosCitasPorDia()` a `reglas_estandar()`. Ningún caso de uso ni controlador cambia. La prueba `TestValidadorAbiertoAExtension` muestra este mecanismo con una regla definida dentro de la propia prueba.

---

## 6. Código limpio

- **Nombres que explican la intención**, en el lenguaje del negocio: `cita.se_cruza_con(otra)`, `horario.franjas_del_dia(dia)`, `exigir_existencia(...)`, `accionesDisponibles(cita, ahora)`. Sin abreviaturas ni variables de una letra.
- **Funciones cortas** que hacen una sola cosa. Los métodos largos se dividieron en pasos con nombre (`_exigir_documento_libre`, `_exigir_que_no_haya_comenzado`).
- **Sin números mágicos**: el horario, la duración de la cita y las longitudes máximas son constantes u objetos con nombre (`HorarioDeAtencion`, `LONGITUD_MAXIMA_MOTIVO`).
- **Objetos de valor en lugar de primitivos**: un `Email` o un `DocumentoIdentidad` que existe ya es válido; nadie vuelve a validarlo.
- **Entidades que protegen sus reglas**: el estado de una cita solo cambia a través de `confirmar()`, `cancelar()`, `reprogramar()` y `marcar_como_atendida()`.
- **Errores con significado**: excepciones del dominio (`ReglaDeNegocioIncumplida`, `EntidadDuplicada`) que la capa web traduce a HTTP; mensajes que dicen qué pasó y cómo corregirlo.
- **Parámetros con nombre** (`kw_only=True` en las entidades) para que las llamadas se lean solas.
- **Sin efectos ocultos**: la hora actual llega por el puerto `Reloj`, no con `datetime.now()` escondido en el dominio.
- **Pruebas legibles** que describen el comportamiento: `test_no_permite_que_el_paciente_este_en_dos_consultas_a_la_vez`.
- **En el frontend**, los componentes no llaman a `fetch`; usan hooks (`useConsulta`, `useAccion`, `useCampos`) y puertos. La lógica de estados y fechas está en `dominio/` y tiene pruebas propias.

## 7. Refactorización

El documento [`docs/REFACTORING.md`](docs/REFACTORING.md) describe cada refactorización aplicada: el olor de código, la técnica (según el catálogo de Martin Fowler), el código antes y después, y el archivo donde quedó. Las pruebas automatizadas son la red de seguridad que permitió refactorizar sin romper el comportamiento.

---

## 8. Subir el proyecto a GitHub o GitLab

1. Crea un repositorio vacío en GitHub (sin README ni .gitignore, porque el proyecto ya los trae).
2. Desde la carpeta `citas-medicas`:

```bash
git init
git add .
git commit -m "Aplicación de citas médicas con arquitectura hexagonal"
git branch -M main
git remote add origin https://github.com/TU_USUARIO/citas-medicas.git
git push -u origin main
```

En GitLab los pasos son iguales; solo cambia la URL del `remote`.

Si tu profesor pide un solo framework de backend, puedes borrar la carpeta del que no vas a entregar. Conservar ambos también es válido: muestra que el núcleo no depende del framework.

**Recomendación para la evaluación:** si quieres que el historial muestre el proceso, haz commits por etapas (por ejemplo: dominio, casos de uso, persistencia, API, frontend, y luego un commit por cada refactorización de `docs/REFACTORING.md` con mensajes como `refactor: extraer VerificadorDeAgenda`). Así el profesor puede ver cada cambio en la pestaña de commits.
