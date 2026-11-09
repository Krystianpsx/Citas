"""Dobles de prueba: implementaciones de los puertos de salida hechas para las pruebas."""
from datetime import datetime

from app.aplicacion.puertos.salida import Notificador, Reloj
from app.aplicacion.vistas import CitaDetallada


class RelojFijo(Reloj):
    def __init__(self, momento: datetime) -> None:
        self.momento = momento

    def ahora(self) -> datetime:
        return self.momento


class NotificadorEspia(Notificador):
    """Registra qué notificaciones se enviaron para poder verificarlas."""

    def __init__(self) -> None:
        self.enviadas: list[tuple[str, str]] = []

    def notificar_agendamiento(self, detalle: CitaDetallada) -> None:
        self.enviadas.append(("agendamiento", detalle.cita.id))

    def notificar_reprogramacion(self, detalle: CitaDetallada) -> None:
        self.enviadas.append(("reprogramacion", detalle.cita.id))

    def notificar_cancelacion(self, detalle: CitaDetallada) -> None:
        self.enviadas.append(("cancelacion", detalle.cita.id))
