from abc import ABC, abstractmethod
from datetime import date, datetime


class Reloj(ABC):
    """Abstrae la hora actual. En producción es el reloj del sistema; en las
    pruebas, un reloj fijo que hace los resultados repetibles."""

    @abstractmethod
    def ahora(self) -> datetime: ...

    def hoy(self) -> date:
        return self.ahora().date()
