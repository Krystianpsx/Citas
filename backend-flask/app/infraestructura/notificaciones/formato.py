"""Fechas en español sin depender de la configuración regional del servidor."""
from datetime import datetime

_DIAS = ("lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo")
_MESES = (
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre",
)


def fecha_y_hora_legibles(momento: datetime) -> str:
    """Ejemplo: 'martes 10 de noviembre de 2026 a las 09:30'."""
    dia_de_la_semana = _DIAS[momento.weekday()]
    mes = _MESES[momento.month - 1]
    return f"{dia_de_la_semana} {momento.day} de {mes} de {momento.year} a las {momento:%H:%M}"
