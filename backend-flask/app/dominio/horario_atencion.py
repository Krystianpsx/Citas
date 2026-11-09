from dataclasses import dataclass, field
from datetime import date, datetime, time, timedelta

from app.dominio.objetos_valor import IntervaloDeTiempo

_NOMBRES_DE_DIAS = ("lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo")
_LUNES_A_VIERNES = frozenset(range(5))


@dataclass(frozen=True)
class HorarioDeAtencion:
    """Cuándo atiende la clínica y cuánto dura cada cita.

    La jornada se divide en franjas consecutivas del tamaño de una cita
    (08:00, 08:30, 09:00, ...). Toda cita debe empezar al inicio de una franja.
    """

    dias_habiles: frozenset[int] = field(default=_LUNES_A_VIERNES)  # 0 = lunes
    apertura: time = time(8, 0)
    cierre: time = time(18, 0)
    duracion_cita: timedelta = timedelta(minutes=30)

    def intervalo_de_cita(self, inicio: datetime) -> IntervaloDeTiempo:
        return IntervaloDeTiempo.desde(inicio, self.duracion_cita)

    def contiene(self, intervalo: IntervaloDeTiempo) -> bool:
        return (
            self._es_dia_habil(intervalo.inicio.date())
            and intervalo.inicio.date() == intervalo.fin.date()
            and self.apertura <= intervalo.inicio.time()
            and intervalo.fin.time() <= self.cierre
        )

    def es_inicio_de_franja(self, momento: datetime) -> bool:
        minutos_desde_apertura = _minutos_del_dia(momento.time()) - _minutos_del_dia(self.apertura)
        tiene_segundos = momento.second != 0 or momento.microsecond != 0
        return not tiene_segundos and minutos_desde_apertura % self._minutos_por_cita == 0

    def franjas_del_dia(self, dia: date) -> list[IntervaloDeTiempo]:
        if not self._es_dia_habil(dia):
            return []
        fin_de_jornada = datetime.combine(dia, self.cierre)
        inicio = datetime.combine(dia, self.apertura)
        franjas = []
        while inicio + self.duracion_cita <= fin_de_jornada:
            franjas.append(self.intervalo_de_cita(inicio))
            inicio += self.duracion_cita
        return franjas

    def descripcion(self) -> str:
        """Texto para mostrar al usuario, por ejemplo: 'de lunes a viernes, de 08:00 a 18:00'."""
        return f"{self._describir_dias()}, de {self.apertura:%H:%M} a {self.cierre:%H:%M}"

    @property
    def _minutos_por_cita(self) -> int:
        return int(self.duracion_cita.total_seconds() // 60)

    def _es_dia_habil(self, dia: date) -> bool:
        return dia.weekday() in self.dias_habiles

    def _describir_dias(self) -> str:
        dias = sorted(self.dias_habiles)
        son_consecutivos = dias == list(range(dias[0], dias[-1] + 1))
        if son_consecutivos and len(dias) > 2:
            return f"de {_NOMBRES_DE_DIAS[dias[0]]} a {_NOMBRES_DE_DIAS[dias[-1]]}"
        return ", ".join(_NOMBRES_DE_DIAS[dia] for dia in dias)


def _minutos_del_dia(momento: time) -> int:
    return momento.hour * 60 + momento.minute
