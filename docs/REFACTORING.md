# Refactorizaciones aplicadas

Refactorizar es mejorar la estructura interna del código **sin cambiar su comportamiento**. Cada cambio de este documento se hizo con las pruebas automatizadas en verde antes y después: si una prueba fallaba, el cambio no era una refactorización sino un error.

Para cada caso se indica el olor de código (*code smell*), la técnica del catálogo de Martin Fowler, el código antes (la versión directa que suele escribirse primero) y el código final que está en el repositorio.

| # | Olor de código | Técnica | Dónde quedó |
|---|---|---|---|
| 1 | Código duplicado | Extract Function | `dominio/validaciones.py` |
| 2 | Obsesión por primitivos | Replace Primitive with Object | `dominio/objetos_valor.py` |
| 3 | Condicionales encadenados | Replace Conditional with Table | `dominio/estado_cita.py` |
| 4 | Método largo | Replace Conditional with Polymorphism (Strategy) | `dominio/reglas_agendamiento.py` |
| 5 | Código duplicado entre clases | Extract Class | `aplicacion/casos_de_uso/comun.py` |
| 6 | Código duplicado en subclases | Pull Up Method | `aplicacion/casos_de_uso/citas.py` |
| 7 | Números mágicos | Replace Magic Literal | `dominio/horario_atencion.py` |
| 8 | Dependencia oculta | Introduce Parameter / puerto | `aplicacion/puertos/salida/reloj.py` |
| 9 | Repositorios casi idénticos | Extract Superclass | `persistencia/sql/repositorios.py` |
| 10 | Cadena de `if isinstance` | Replace Conditional with Table | `infraestructura/web/errores.py` |
| 11 | Nombre poco expresivo | Rename Variable | `app/principal.py` |
| 12 | Lógica de datos dentro de la vista (frontend) | Extract Function / Extract Hook | `frontend/src/ui/hooks/`, `frontend/src/infraestructura/http/` |

---

## 1. Extract Function: validación de textos

**Olor:** cada entidad repetía el mismo bloque para limpiar espacios, verificar que no esté vacío y controlar la longitud.

**Antes**

```python
class Paciente:
    def __post_init__(self):
        if not self.nombre or not self.nombre.strip():
            raise DatoInvalido("El nombre es obligatorio.")
        self.nombre = " ".join(self.nombre.split())
        if len(self.nombre) > 120:
            raise DatoInvalido("El nombre admite como máximo 120 caracteres.")

class Cita:
    def __post_init__(self):
        if not self.motivo or not self.motivo.strip():
            raise DatoInvalido("El motivo es obligatorio.")
        self.motivo = " ".join(self.motivo.split())
        if len(self.motivo) > 250:
            raise DatoInvalido("El motivo admite como máximo 250 caracteres.")
```

**Después**

```python
# dominio/validaciones.py
def texto_requerido(valor: object, campo: str, longitud_maxima: int) -> str: ...

# dominio/paciente.py
self.nombre = texto_requerido(self.nombre, "nombre", LONGITUD_MAXIMA_NOMBRE)

# dominio/cita.py
self.motivo = texto_requerido(self.motivo, "motivo", LONGITUD_MAXIMA_MOTIVO)
```

**Beneficio:** una sola regla de validación de textos. Si cambia (por ejemplo, prohibir caracteres especiales), se cambia en un solo lugar.

---

## 2. Replace Primitive with Object: correo y documento

**Olor:** el correo y el documento eran `str` y cada caso de uso los validaba (o se olvidaba de hacerlo).

**Antes**

```python
def registrar_paciente(nombre: str, documento: str, email: str):
    documento = documento.replace(".", "").replace("-", "")
    if not documento.isdigit() or not 6 <= len(documento) <= 12:
        raise DatoInvalido("Documento inválido")
    if "@" not in email:
        raise DatoInvalido("Correo inválido")
    ...
```

**Después**

```python
@dataclass(frozen=True)
class DocumentoIdentidad:
    numero: str
    def __post_init__(self) -> None:
        limpio = _SEPARADORES_DE_DOCUMENTO.sub("", self.numero)
        if not _PATRON_DOCUMENTO.match(limpio):
            raise DatoInvalido("El documento debe tener entre 6 y 12 dígitos.")
        object.__setattr__(self, "numero", limpio)

# en el caso de uso
documento = DocumentoIdentidad(comando.documento)
```

**Beneficio:** un `DocumentoIdentidad` que existe ya es válido y normalizado. Lo mismo ocurre con `Email` e `IntervaloDeTiempo`, que además encapsula la lógica de solapamiento (`se_solapa_con`).

---

## 3. Replace Conditional with Table: transiciones de estado

**Olor:** cadena de `if/elif` que había que editar cada vez que aparecía un estado nuevo.

**Antes**

```python
def cambiar_estado(self, destino):
    if self.estado == "PROGRAMADA" and destino in ("CONFIRMADA", "CANCELADA"):
        self.estado = destino
    elif self.estado == "CONFIRMADA" and destino in ("ATENDIDA", "CANCELADA"):
        self.estado = destino
    elif self.estado in ("ATENDIDA", "CANCELADA"):
        raise Exception("No se puede cambiar")
    else:
        raise Exception("Transición inválida")
```

**Después**

```python
_TRANSICIONES_PERMITIDAS = {
    EstadoCita.PROGRAMADA: frozenset({EstadoCita.CONFIRMADA, EstadoCita.CANCELADA}),
    EstadoCita.CONFIRMADA: frozenset({EstadoCita.ATENDIDA, EstadoCita.CANCELADA}),
    EstadoCita.ATENDIDA: frozenset(),
    EstadoCita.CANCELADA: frozenset(),
}

def puede_cambiar_a(self, destino: "EstadoCita") -> bool:
    return destino in _TRANSICIONES_PERMITIDAS[self]
```

**Beneficio:** las reglas de transición se leen como una tabla, las cadenas mágicas pasaron a un `Enum` y agregar un estado no exige tocar la lógica (Abierto/Cerrado). El frontend usa la misma tabla en `dominio/cita.ts`.

---

## 4. Strategy: reglas de agendamiento

**Olor:** método largo en el caso de uso, con todas las validaciones una tras otra y mezclando consultas a la base de datos con reglas del negocio.

**Antes**

```python
def agendar(self, paciente_id, medico_id, inicio, motivo):
    if inicio <= datetime.now():
        raise Error("La cita debe ser futura")
    if inicio.weekday() >= 5 or inicio.hour < 8 or inicio.hour >= 18:
        raise Error("Fuera de horario")
    if inicio.minute not in (0, 30):
        raise Error("Las citas son cada 30 minutos")
    for c in self.repo.citas_del_medico(medico_id, inicio.date()):
        if c.estado != "CANCELADA" and c.inicio < inicio + timedelta(minutes=30) and inicio < c.fin:
            raise Error("Médico ocupado")
    for c in self.repo.citas_del_paciente(paciente_id, inicio.date()):
        if c.estado != "CANCELADA" and c.inicio < inicio + timedelta(minutes=30) and inicio < c.fin:
            raise Error("Paciente ocupado")
    ...
```

**Después**

```python
class ReglaDeAgendamiento(ABC):
    @abstractmethod
    def verificar(self, cita: Cita, contexto: ContextoDeAgendamiento) -> None: ...

class MedicoSinCitasCruzadas(ReglaDeAgendamiento):
    def verificar(self, cita, contexto):
        if _hay_cruce(cita, contexto.agenda_del_medico):
            raise ReglaDeNegocioIncumplida("El médico ya tiene una cita en ese horario.")

class ValidadorDeAgendamiento:
    def __init__(self, reglas: Iterable[ReglaDeAgendamiento]) -> None:
        self._reglas = tuple(reglas)

    def validar(self, cita, contexto):
        for regla in self._reglas:
            regla.verificar(cita, contexto)
```

**Beneficio:** cada regla es una clase pequeña y probada por separado; el bucle de cruces duplicado desapareció (`_hay_cruce`); las reglas no consultan repositorios, reciben el contexto ya cargado y por eso son dominio puro. Agregar una regla no modifica el validador.

---

## 5. Extract Class: VerificadorDeAgenda

**Olor:** `AgendarCita` y `ReprogramarCita` tenían las mismas seis líneas para consultar la agenda del médico y del paciente, armar el contexto y validar.

**Antes**

```python
class AgendarCitaServicio:
    def ejecutar(self, comando):
        ...
        dia = cita.inicio.date()
        contexto = ContextoDeAgendamiento(
            ahora=self._reloj.ahora(),
            agenda_del_medico=self._citas.buscar(Criterios(dia=dia, medico_id=cita.medico_id)),
            agenda_del_paciente=self._citas.buscar(Criterios(dia=dia, paciente_id=cita.paciente_id)),
        )
        self._validador.validar(cita, contexto)

class ReprogramarCitaServicio:
    def ejecutar(self, comando):
        ...
        # el mismo bloque, copiado
```

**Después**

```python
class VerificadorDeAgenda:
    def verificar(self, cita: Cita) -> None: ...

# en ambos casos de uso
self._verificador.verificar(cita)
```

---

## 6. Pull Up Method: operaciones sobre una cita existente

**Olor:** confirmar, atender, cancelar y reprogramar empezaban buscando la cita, fallando si no existía, y terminaban guardándola y devolviéndola con sus detalles.

**Antes**

```python
class ConfirmarCitaServicio:
    def ejecutar(self, cita_id):
        cita = self._citas.buscar_por_id(cita_id)
        if cita is None:
            raise EntidadNoEncontrada(f"No existe la cita {cita_id}")
        cita.confirmar()
        self._citas.guardar(cita)
        return self._detallador.detallar(cita)

class AtenderCitaServicio:
    def ejecutar(self, cita_id):
        cita = self._citas.buscar_por_id(cita_id)
        if cita is None:
            raise EntidadNoEncontrada(f"No existe la cita {cita_id}")
        ...  # y así en cuatro clases
```

**Después**

```python
class _OperacionSobreCitaExistente:
    def _obtener_cita(self, cita_id: str) -> Cita: ...
    def _guardar_y_detallar(self, cita: Cita) -> CitaDetallada: ...

class ConfirmarCitaServicio(_OperacionSobreCitaExistente, ConfirmarCitaCasoDeUso):
    def ejecutar(self, cita_id: str) -> CitaDetallada:
        cita = self._obtener_cita(cita_id)
        cita.confirmar()
        return self._guardar_y_detallar(cita)
```

Junto con esto se extrajo `exigir_existencia(entidad, descripcion)`, que reemplaza el patrón `if x is None: raise ...` en todos los casos de uso.

---

## 7. Replace Magic Literal: horario de atención

**Olor:** los números `8`, `18`, `30` y `range(5)` aparecían en reglas, en el cálculo de disponibilidad y en los mensajes de error.

**Antes**

```python
if inicio.weekday() >= 5 or inicio.hour < 8 or (inicio + timedelta(minutes=30)).hour > 18:
    raise Error("Atendemos de lunes a viernes de 8 a 18")
...
for minuto in range(8 * 60, 18 * 60, 30):
    ...
```

**Después**

```python
@dataclass(frozen=True)
class HorarioDeAtencion:
    dias_habiles: frozenset[int] = field(default=_LUNES_A_VIERNES)  # 0 = lunes
    apertura: time = time(8, 0)
    cierre: time = time(18, 0)
    duracion_cita: timedelta = timedelta(minutes=30)

    def contiene(self, intervalo) -> bool: ...
    def franjas_del_dia(self, dia) -> list[IntervaloDeTiempo]: ...
    def descripcion(self) -> str: ...  # "de lunes a viernes, de 08:00 a 18:00"
```

**Beneficio:** el horario existe en un solo lugar. El backend lo expone en `GET /api/horario` y el frontend dibuja la agenda con esos datos, así que cambiar el horario de la clínica no exige tocar la interfaz.

---

## 8. Introduce Parameter: el reloj como puerto

**Olor:** `datetime.now()` escondido dentro de las reglas. Las pruebas dependían de la hora en que se ejecutaban y fallaban de noche o en fines de semana.

**Antes**

```python
def cancelar(self, motivo):
    if datetime.now() >= self.inicio:
        raise Error("Ya empezó")
```

**Después**

```python
# dominio: recibe la hora como parámetro
def cancelar(self, motivo: str, ahora: datetime) -> None: ...

# aplicación: la obtiene de un puerto
cita.cancelar(comando.motivo, self._reloj.ahora())

# pruebas: un reloj fijo
reloj = RelojFijo(datetime(2026, 11, 9, 7, 0))
```

---

## 9. Extract Superclass: repositorios

**Olor:** los tres repositorios SQL repetían cómo abrir la sesión, guardar con `merge`, buscar por clave y convertir filas. Lo mismo pasaba con los tres repositorios en memoria.

**Después**

```python
class _RepositorioSql(Generic[E, F]):
    def guardar(self, entidad: E) -> None: ...
    def _por_clave_primaria(self, clave: str) -> E | None: ...
    def _uno(self, consulta: Select) -> E | None: ...
    def _varios(self, consulta: Select) -> list[E]: ...

class RepositorioDePacientesSql(_RepositorioSql[Paciente, PacienteFila], RepositorioDePacientes):
    def buscar_por_documento(self, documento):
        return self._uno(select(PacienteFila).where(PacienteFila.documento == documento.numero))
```

Cada repositorio concreto quedó con solo sus consultas específicas. En memoria se hizo lo mismo con `_AlmacenEnMemoria`, que además guarda copias para comportarse como una base de datos real (Liskov).

---

## 10. Replace Conditional with Table: errores HTTP

**Antes**

```python
@app.errorhandler(Exception)
def manejar(error):
    if isinstance(error, DatoInvalido):
        return {"mensaje": str(error)}, 400
    elif isinstance(error, EntidadNoEncontrada):
        return {"mensaje": str(error)}, 404
    elif isinstance(error, EntidadDuplicada):
        return {"mensaje": str(error)}, 409
    ...
```

**Después**

```python
_CODIGOS_HTTP = {
    SolicitudInvalida: 400,
    DatoInvalido: 400,
    EntidadNoEncontrada: 404,
    EntidadDuplicada: 409,
    ReglaDeNegocioIncumplida: 422,
}

def _codigo_para(error: Exception) -> int:
    for clase in type(error).__mro__:
        if clase in _CODIGOS_HTTP:
            return _CODIGOS_HTTP[clase]
    return 500
```

Al recorrer la jerarquía (`__mro__`), `TransicionDeEstadoInvalida` hereda el 422 de su padre sin una línea extra.

---

## 11. Rename Variable

**Antes**

```python
def _registrar_rutas(aplicacion: Flask, c: Contenedor) -> None:
    aplicacion.register_blueprint(crear_rutas_de_pacientes(registrar=c.registrar_paciente, ...))
```

**Después**

```python
def _registrar_rutas(aplicacion: Flask, contenedor: Contenedor) -> None:
    aplicacion.register_blueprint(crear_rutas_de_pacientes(registrar=contenedor.registrar_paciente, ...))
```

Un cambio pequeño, pero el código se lee sin tener que recordar qué significaba `c`.

---

## 12. Frontend: sacar los datos de la vista

**Olor:** componentes que llamaban a `fetch`, manejaban `loading` y `error` con tres `useState` cada uno y tenían un `onChange` por campo.

**Antes**

```tsx
function PaginaPacientes() {
  const [pacientes, setPacientes] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);
  useEffect(() => {
    fetch("http://localhost:8000/api/pacientes")
      .then((r) => r.json())
      .then(setPacientes)
      .catch((e) => setError(e.message))
      .finally(() => setCargando(false));
  }, []);
  ...
}
```

**Después**

```tsx
function PaginaPacientes() {
  const { pacientes } = usePuertos();                      // puerto inyectado
  const lista = useConsulta(() => pacientes.listar(), []); // hook reutilizable
  ...
}
```

- `infraestructura/http/adaptadoresHttp.ts` es el único lugar que conoce las URL.
- `useConsulta`, `useAccion` y `useCampos` reemplazan el código repetido de carga, envío y formularios.
- `enlazar("nombre")` reemplaza un manejador `onChange` por campo.

---

## Las mismas refactorizaciones en la versión Spring Boot

Los ejemplos de este documento están en Python, pero `backend-spring` aplica exactamente las mismas técnicas. Esta tabla indica dónde encontrar cada una en Java (paquete base `com.clinica.citas`):

| # | Técnica | Clase en Spring Boot |
|---|---|---|
| 1 | Extract Function | `dominio.modelo.Textos` |
| 2 | Replace Primitive with Object | `dominio.modelo.Email`, `DocumentoIdentidad`, `IntervaloDeTiempo` (records) |
| 3 | Replace Conditional with Table | `dominio.modelo.EstadoCita` (`TRANSICIONES_PERMITIDAS`) |
| 4 | Strategy | `dominio.reglas.ReglaDeAgendamiento` y sus cinco implementaciones |
| 5 | Extract Class | `aplicacion.servicios.VerificadorDeAgenda` |
| 6 | Pull Up Method | `aplicacion.servicios.OperacionSobreCitaExistente` |
| 7 | Replace Magic Literal | `dominio.modelo.HorarioDeAtencion` |
| 8 | Introduce Parameter / puerto | `aplicacion.puertos.salida.Reloj` |
| 9 | Extract Superclass | `infraestructura.persistencia.jpa.AdaptadorJpa`, `persistencia.memoria.AlmacenEnMemoria` |
| 10 | Replace Conditional with Table | `infraestructura.web.ManejadorDeErrores` (`CODIGOS_HTTP`) |
| 11 | Rename Variable | `infraestructura.configuracion.CasosDeUso#ensamblar` (el parámetro `d` pasó a llamarse `dependencias`) |

Java permitió dos mejoras que en Python no hacían falta:

- **Optional en lugar de null.** Los repositorios devuelven `Optional<Paciente>` y los casos de uso usan `orElseThrow(() -> EntidadNoEncontrada.conId("el paciente", id))`. Desaparece la función auxiliar `exigir_existencia` porque el lenguaje ya resuelve ese patrón.
- **Comandos anidados en su puerto.** `AgendarCitaCasoDeUso.Comando` vive dentro de la interfaz que lo usa (*Move Class*), así el contrato de entrada y sus datos se leen en un solo archivo.
