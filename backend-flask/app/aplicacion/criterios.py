from dataclasses import dataclass
from datetime import date

from app.dominio.cita import Cita
from app.dominio.estado_cita import EstadoCita


@dataclass(frozen=True)
class CriteriosDeBusquedaDeCitas:
    """Filtros opcionales. Un criterio en None significa "sin filtrar por ese campo"."""

    dia: date | None = None
    medico_id: str | None = None
    paciente_id: str | None = None
    estado: EstadoCita | None = None

    def coincide_con(self, cita: Cita) -> bool:
        return (
            (self.dia is None or cita.inicio.date() == self.dia)
            and (self.medico_id is None or cita.medico_id == self.medico_id)
            and (self.paciente_id is None or cita.paciente_id == self.paciente_id)
            and (self.estado is None or cita.estado is self.estado)
        )
