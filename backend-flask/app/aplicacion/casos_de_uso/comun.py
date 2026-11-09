from collections.abc import Callable, Iterable
from typing import TypeVar

from app.aplicacion.criterios import CriteriosDeBusquedaDeCitas
from app.aplicacion.puertos.salida import Reloj, RepositorioDeCitas, RepositorioDeMedicos, RepositorioDePacientes
from app.aplicacion.vistas import CitaDetallada
from app.dominio.cita import Cita
from app.dominio.excepciones import EntidadNoEncontrada
from app.dominio.medico import Medico
from app.dominio.paciente import Paciente
from app.dominio.reglas_agendamiento import ContextoDeAgendamiento, ValidadorDeAgendamiento

T = TypeVar("T")


def exigir_existencia(entidad: T | None, descripcion: str) -> T:
    """Refactorización *Extract Function*: reemplaza el bloque
    `if x is None: raise ...` que se repetía en cada caso de uso."""
    if entidad is None:
        raise EntidadNoEncontrada(f"No existe {descripcion}.")
    return entidad


class DetalladorDeCitas:
    """Une cada cita con su paciente y su médico. Consulta cada persona una sola
    vez aunque aparezca en varias citas del listado."""

    def __init__(self, pacientes: RepositorioDePacientes, medicos: RepositorioDeMedicos) -> None:
        self._pacientes = pacientes
        self._medicos = medicos

    def detallar(self, cita: Cita) -> CitaDetallada:
        return self.detallar_varias([cita])[0]

    def detallar_varias(self, citas: Iterable[Cita]) -> list[CitaDetallada]:
        pacientes: dict[str, Paciente] = {}
        medicos: dict[str, Medico] = {}
        return [
            CitaDetallada(
                cita=cita,
                paciente=_buscar_una_vez(pacientes, cita.paciente_id, self._pacientes.buscar_por_id, "el paciente"),
                medico=_buscar_una_vez(medicos, cita.medico_id, self._medicos.buscar_por_id, "el médico"),
            )
            for cita in citas
        ]


def _buscar_una_vez(cache: dict[str, T], clave: str, buscar: Callable[[str], T | None], descripcion: str) -> T:
    if clave not in cache:
        cache[clave] = exigir_existencia(buscar(clave), f"{descripcion} con id {clave}")
    return cache[clave]


class VerificadorDeAgenda:
    """Reúne la agenda del día del médico y del paciente y aplica las reglas de agendamiento.

    Lo usan AgendarCita y ReprogramarCita; antes de extraer esta clase, ambos
    casos de uso duplicaban la construcción del contexto (*Extract Class*)."""

    def __init__(self, citas: RepositorioDeCitas, validador: ValidadorDeAgendamiento, reloj: Reloj) -> None:
        self._citas = citas
        self._validador = validador
        self._reloj = reloj

    def verificar(self, cita: Cita) -> None:
        dia = cita.inicio.date()
        contexto = ContextoDeAgendamiento(
            ahora=self._reloj.ahora(),
            agenda_del_medico=self._citas.buscar(CriteriosDeBusquedaDeCitas(dia=dia, medico_id=cita.medico_id)),
            agenda_del_paciente=self._citas.buscar(CriteriosDeBusquedaDeCitas(dia=dia, paciente_id=cita.paciente_id)),
        )
        self._validador.validar(cita, contexto)
