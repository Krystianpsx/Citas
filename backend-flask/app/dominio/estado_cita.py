from enum import Enum


class EstadoCita(str, Enum):
    """Ciclo de vida de una cita.

        PROGRAMADA ──► CONFIRMADA ──► ATENDIDA
             │               │
             └──────►  CANCELADA  ◄──┘

    Refactorización aplicada: *Replace Conditional with Table*. Las transiciones
    vivían en una cadena de if/elif dentro de Cita; ahora están declaradas en una
    tabla y agregar un estado no obliga a tocar la lógica (Abierto/Cerrado).
    """

    PROGRAMADA = "PROGRAMADA"
    CONFIRMADA = "CONFIRMADA"
    ATENDIDA = "ATENDIDA"
    CANCELADA = "CANCELADA"

    def puede_cambiar_a(self, destino: "EstadoCita") -> bool:
        return destino in _TRANSICIONES_PERMITIDAS[self]

    @property
    def es_final(self) -> bool:
        return not _TRANSICIONES_PERMITIDAS[self]

    @property
    def ocupa_agenda(self) -> bool:
        """Una cita cancelada libera su franja; cualquier otra la mantiene ocupada."""
        return self is not EstadoCita.CANCELADA

    @property
    def etiqueta(self) -> str:
        return self.value.lower()


_TRANSICIONES_PERMITIDAS: dict[EstadoCita, frozenset[EstadoCita]] = {
    EstadoCita.PROGRAMADA: frozenset({EstadoCita.CONFIRMADA, EstadoCita.CANCELADA}),
    EstadoCita.CONFIRMADA: frozenset({EstadoCita.ATENDIDA, EstadoCita.CANCELADA}),
    EstadoCita.ATENDIDA: frozenset(),
    EstadoCita.CANCELADA: frozenset(),
}
