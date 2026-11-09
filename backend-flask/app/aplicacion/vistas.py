from dataclasses import dataclass

from app.dominio.cita import Cita
from app.dominio.medico import Medico
from app.dominio.paciente import Paciente


@dataclass(frozen=True)
class CitaDetallada:
    """Modelo de lectura: la cita junto con las personas involucradas.

    Evita que la interfaz tenga que pedir paciente y médico por separado."""

    cita: Cita
    paciente: Paciente
    medico: Medico
