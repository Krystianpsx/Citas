"""Datos de entrada de los casos de uso (DTO).

Llevan tipos primitivos: la capa web solo traduce JSON a estos objetos y el caso
de uso es quien construye los objetos de valor del dominio.
"""
from dataclasses import dataclass
from datetime import date, datetime


@dataclass(frozen=True)
class RegistrarPacienteComando:
    nombre: str
    documento: str
    email: str
    telefono: str | None = None
    fecha_nacimiento: date | None = None


@dataclass(frozen=True)
class RegistrarMedicoComando:
    nombre: str
    especialidad: str
    registro_profesional: str
    email: str


@dataclass(frozen=True)
class ConsultarDisponibilidadComando:
    medico_id: str
    dia: date


@dataclass(frozen=True)
class AgendarCitaComando:
    paciente_id: str
    medico_id: str
    inicio: datetime
    motivo: str


@dataclass(frozen=True)
class ReprogramarCitaComando:
    cita_id: str
    nuevo_inicio: datetime


@dataclass(frozen=True)
class CancelarCitaComando:
    cita_id: str
    motivo: str
