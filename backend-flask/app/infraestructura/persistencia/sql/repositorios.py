from collections.abc import Callable, Iterator
from datetime import datetime, time, timedelta
from typing import Generic, TypeVar

from sqlalchemy import ColumnElement, Select, select
from sqlalchemy.orm import Session, sessionmaker

from app.aplicacion.criterios import CriteriosDeBusquedaDeCitas
from app.aplicacion.puertos.salida import RepositorioDeCitas, RepositorioDeMedicos, RepositorioDePacientes
from app.dominio.cita import Cita
from app.dominio.medico import Especialidad, Medico
from app.dominio.objetos_valor import DocumentoIdentidad
from app.dominio.paciente import Paciente
from app.infraestructura.persistencia.sql.mapeadores import (
    cita_a_fila,
    cita_desde_fila,
    medico_a_fila,
    medico_desde_fila,
    paciente_a_fila,
    paciente_desde_fila,
)
from app.infraestructura.persistencia.sql.modelos import Base, CitaFila, MedicoFila, PacienteFila

E = TypeVar("E")
F = TypeVar("F", bound=Base)


class _RepositorioSql(Generic[E, F]):
    """Operaciones comunes. Cada repositorio concreto solo declara su tabla y
    sus mapeadores (refactorización *Extract Superclass*)."""

    def __init__(
        self,
        sesiones: sessionmaker[Session],
        tabla: type[F],
        a_fila: Callable[[E], F],
        desde_fila: Callable[[F], E],
    ) -> None:
        self._sesiones = sesiones
        self._tabla = tabla
        self._a_fila = a_fila
        self._desde_fila = desde_fila

    def guardar(self, entidad: E) -> None:
        with self._sesiones.begin() as sesion:
            sesion.merge(self._a_fila(entidad))

    def _por_clave_primaria(self, clave: str) -> E | None:
        with self._sesiones() as sesion:
            fila = sesion.get(self._tabla, clave)
            return self._desde_fila(fila) if fila is not None else None

    def _uno(self, consulta: Select) -> E | None:
        with self._sesiones() as sesion:
            fila = sesion.scalars(consulta).first()
            return self._desde_fila(fila) if fila is not None else None

    def _varios(self, consulta: Select) -> list[E]:
        with self._sesiones() as sesion:
            return [self._desde_fila(fila) for fila in sesion.scalars(consulta)]


class RepositorioDePacientesSql(_RepositorioSql[Paciente, PacienteFila], RepositorioDePacientes):
    def __init__(self, sesiones: sessionmaker[Session]) -> None:
        super().__init__(sesiones, PacienteFila, paciente_a_fila, paciente_desde_fila)

    def buscar_por_id(self, paciente_id: str) -> Paciente | None:
        return self._por_clave_primaria(paciente_id)

    def buscar_por_documento(self, documento: DocumentoIdentidad) -> Paciente | None:
        return self._uno(select(PacienteFila).where(PacienteFila.documento == documento.numero))

    def listar(self) -> list[Paciente]:
        return self._varios(select(PacienteFila).order_by(PacienteFila.nombre))


class RepositorioDeMedicosSql(_RepositorioSql[Medico, MedicoFila], RepositorioDeMedicos):
    def __init__(self, sesiones: sessionmaker[Session]) -> None:
        super().__init__(sesiones, MedicoFila, medico_a_fila, medico_desde_fila)

    def buscar_por_id(self, medico_id: str) -> Medico | None:
        return self._por_clave_primaria(medico_id)

    def buscar_por_registro(self, registro_profesional: str) -> Medico | None:
        return self._uno(select(MedicoFila).where(MedicoFila.registro_profesional == registro_profesional))

    def listar(self, especialidad: Especialidad | None = None) -> list[Medico]:
        consulta = select(MedicoFila).order_by(MedicoFila.nombre)
        if especialidad is not None:
            consulta = consulta.where(MedicoFila.especialidad == especialidad.value)
        return self._varios(consulta)


class RepositorioDeCitasSql(_RepositorioSql[Cita, CitaFila], RepositorioDeCitas):
    def __init__(self, sesiones: sessionmaker[Session]) -> None:
        super().__init__(sesiones, CitaFila, cita_a_fila, cita_desde_fila)

    def buscar_por_id(self, cita_id: str) -> Cita | None:
        return self._por_clave_primaria(cita_id)

    def buscar(self, criterios: CriteriosDeBusquedaDeCitas) -> list[Cita]:
        consulta = select(CitaFila).where(*_condiciones(criterios)).order_by(CitaFila.inicio)
        return self._varios(consulta)


def _condiciones(criterios: CriteriosDeBusquedaDeCitas) -> Iterator[ColumnElement[bool]]:
    """Traduce cada criterio presente a una condición SQL."""
    if criterios.dia is not None:
        comienzo_del_dia = datetime.combine(criterios.dia, time.min)
        yield CitaFila.inicio >= comienzo_del_dia
        yield CitaFila.inicio < comienzo_del_dia + timedelta(days=1)
    if criterios.medico_id is not None:
        yield CitaFila.medico_id == criterios.medico_id
    if criterios.paciente_id is not None:
        yield CitaFila.paciente_id == criterios.paciente_id
    if criterios.estado is not None:
        yield CitaFila.estado == criterios.estado.value
