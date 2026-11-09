"""Reglas que debe cumplir una cita antes de entrar a la agenda.

Patrón Strategy + principio Abierto/Cerrado: cada regla es una clase pequeña con
una sola responsabilidad. Para agregar una regla nueva (por ejemplo, "máximo dos
citas por paciente al día") se crea otra clase y se agrega a la lista en el
contenedor de dependencias; el caso de uso AgendarCita no se modifica.
"""
from abc import ABC, abstractmethod
from collections.abc import Iterable, Sequence
from dataclasses import dataclass
from datetime import datetime

from app.dominio.cita import Cita
from app.dominio.excepciones import ReglaDeNegocioIncumplida
from app.dominio.horario_atencion import HorarioDeAtencion


@dataclass(frozen=True)
class ContextoDeAgendamiento:
    """Información que las reglas necesitan. Llega ya consultada, así las reglas
    son funciones puras del dominio y no dependen de repositorios."""

    ahora: datetime
    agenda_del_medico: Sequence[Cita]
    agenda_del_paciente: Sequence[Cita]


class ReglaDeAgendamiento(ABC):
    @abstractmethod
    def verificar(self, cita: Cita, contexto: ContextoDeAgendamiento) -> None:
        """Lanza ReglaDeNegocioIncumplida si la cita no cumple la regla."""


class CitaEnElFuturo(ReglaDeAgendamiento):
    def verificar(self, cita: Cita, contexto: ContextoDeAgendamiento) -> None:
        if cita.inicio <= contexto.ahora:
            raise ReglaDeNegocioIncumplida("La cita debe programarse para una fecha y hora futuras.")


class DentroDelHorarioDeAtencion(ReglaDeAgendamiento):
    def __init__(self, horario: HorarioDeAtencion) -> None:
        self._horario = horario

    def verificar(self, cita: Cita, contexto: ContextoDeAgendamiento) -> None:
        if not self._horario.contiene(cita.intervalo):
            raise ReglaDeNegocioIncumplida(
                f"La clínica atiende {self._horario.descripcion()}. Elige un horario dentro de ese rango."
            )


class AlineadaConLasFranjas(ReglaDeAgendamiento):
    def __init__(self, horario: HorarioDeAtencion) -> None:
        self._horario = horario

    def verificar(self, cita: Cita, contexto: ContextoDeAgendamiento) -> None:
        if not self._horario.es_inicio_de_franja(cita.inicio):
            minutos = int(self._horario.duracion_cita.total_seconds() // 60)
            raise ReglaDeNegocioIncumplida(f"Las citas empiezan en bloques de {minutos} minutos.")


class MedicoSinCitasCruzadas(ReglaDeAgendamiento):
    def verificar(self, cita: Cita, contexto: ContextoDeAgendamiento) -> None:
        if _hay_cruce(cita, contexto.agenda_del_medico):
            raise ReglaDeNegocioIncumplida("El médico ya tiene una cita en ese horario.")


class PacienteSinCitasCruzadas(ReglaDeAgendamiento):
    def verificar(self, cita: Cita, contexto: ContextoDeAgendamiento) -> None:
        if _hay_cruce(cita, contexto.agenda_del_paciente):
            raise ReglaDeNegocioIncumplida("El paciente ya tiene otra cita en ese horario.")


def _hay_cruce(cita: Cita, agenda: Iterable[Cita]) -> bool:
    return any(cita.se_cruza_con(otra) for otra in agenda)


class ValidadorDeAgendamiento:
    """Aplica, en orden, todas las reglas que recibe. No conoce ninguna regla concreta."""

    def __init__(self, reglas: Iterable[ReglaDeAgendamiento]) -> None:
        self._reglas = tuple(reglas)

    def validar(self, cita: Cita, contexto: ContextoDeAgendamiento) -> None:
        for regla in self._reglas:
            regla.verificar(cita, contexto)


def reglas_estandar(horario: HorarioDeAtencion) -> list[ReglaDeAgendamiento]:
    return [
        CitaEnElFuturo(),
        DentroDelHorarioDeAtencion(horario),
        AlineadaConLasFranjas(horario),
        MedicoSinCitasCruzadas(),
        PacienteSinCitasCruzadas(),
    ]
