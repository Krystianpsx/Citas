from collections.abc import Callable
from copy import deepcopy
from typing import Generic, TypeVar

from app.aplicacion.criterios import CriteriosDeBusquedaDeCitas
from app.aplicacion.puertos.salida import RepositorioDeCitas, RepositorioDeMedicos, RepositorioDePacientes
from app.dominio.cita import Cita
from app.dominio.entidad import Entidad
from app.dominio.medico import Especialidad, Medico
from app.dominio.objetos_valor import DocumentoIdentidad
from app.dominio.paciente import Paciente

E = TypeVar("E", bound=Entidad)


class _AlmacenEnMemoria(Generic[E]):
    """Guarda copias para imitar a una base de datos real: si un caso de uso
    modifica una entidad y luego falla, lo guardado no cambia."""

    def __init__(self, clave_de_orden: Callable[[E], object]) -> None:
        self._entidades: dict[str, E] = {}
        self._clave_de_orden = clave_de_orden

    def guardar(self, entidad: E) -> None:
        self._entidades[entidad.id] = deepcopy(entidad)

    def obtener(self, entidad_id: str) -> E | None:
        return deepcopy(self._entidades.get(entidad_id))

    def primera_que_cumpla(self, condicion: Callable[[E], bool]) -> E | None:
        return next(iter(self.todas_las_que_cumplan(condicion)), None)

    def todas_las_que_cumplan(self, condicion: Callable[[E], bool]) -> list[E]:
        coincidencias = (entidad for entidad in self._entidades.values() if condicion(entidad))
        return [deepcopy(entidad) for entidad in sorted(coincidencias, key=self._clave_de_orden)]


class RepositorioDePacientesEnMemoria(RepositorioDePacientes):
    def __init__(self) -> None:
        self._almacen = _AlmacenEnMemoria[Paciente](clave_de_orden=lambda paciente: paciente.nombre.lower())

    def guardar(self, paciente: Paciente) -> None:
        self._almacen.guardar(paciente)

    def buscar_por_id(self, paciente_id: str) -> Paciente | None:
        return self._almacen.obtener(paciente_id)

    def buscar_por_documento(self, documento: DocumentoIdentidad) -> Paciente | None:
        return self._almacen.primera_que_cumpla(lambda paciente: paciente.documento == documento)

    def listar(self) -> list[Paciente]:
        return self._almacen.todas_las_que_cumplan(lambda _: True)


class RepositorioDeMedicosEnMemoria(RepositorioDeMedicos):
    def __init__(self) -> None:
        self._almacen = _AlmacenEnMemoria[Medico](clave_de_orden=lambda medico: medico.nombre.lower())

    def guardar(self, medico: Medico) -> None:
        self._almacen.guardar(medico)

    def buscar_por_id(self, medico_id: str) -> Medico | None:
        return self._almacen.obtener(medico_id)

    def buscar_por_registro(self, registro_profesional: str) -> Medico | None:
        return self._almacen.primera_que_cumpla(lambda medico: medico.registro_profesional == registro_profesional)

    def listar(self, especialidad: Especialidad | None = None) -> list[Medico]:
        return self._almacen.todas_las_que_cumplan(
            lambda medico: especialidad is None or medico.especialidad is especialidad
        )


class RepositorioDeCitasEnMemoria(RepositorioDeCitas):
    def __init__(self) -> None:
        self._almacen = _AlmacenEnMemoria[Cita](clave_de_orden=lambda cita: cita.inicio)

    def guardar(self, cita: Cita) -> None:
        self._almacen.guardar(cita)

    def buscar_por_id(self, cita_id: str) -> Cita | None:
        return self._almacen.obtener(cita_id)

    def buscar(self, criterios: CriteriosDeBusquedaDeCitas) -> list[Cita]:
        return self._almacen.todas_las_que_cumplan(criterios.coincide_con)
