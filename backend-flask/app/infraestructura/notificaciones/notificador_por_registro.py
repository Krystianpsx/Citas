import logging

from app.aplicacion.puertos.salida import Notificador
from app.aplicacion.vistas import CitaDetallada
from app.infraestructura.notificaciones.formato import fecha_y_hora_legibles


class NotificadorPorRegistro(Notificador):
    """Simula el envío de correos escribiéndolos en el registro de la aplicación.

    Para enviar correos reales basta con crear otra clase que implemente
    Notificador (por ejemplo, NotificadorSmtp) y cambiarla en el contenedor."""

    def __init__(self, registro: logging.Logger | None = None) -> None:
        self._registro = registro or logging.getLogger("citas.notificaciones")

    def notificar_agendamiento(self, detalle: CitaDetallada) -> None:
        self._enviar(detalle, "Cita agendada", f"Tu cita {self._describir(detalle)} quedó agendada.")

    def notificar_reprogramacion(self, detalle: CitaDetallada) -> None:
        self._enviar(detalle, "Cita reprogramada", f"Tu cita se movió: ahora es {self._describir(detalle)}.")

    def notificar_cancelacion(self, detalle: CitaDetallada) -> None:
        motivo = detalle.cita.motivo_cancelacion
        self._enviar(detalle, "Cita cancelada", f"Tu cita {self._describir(detalle)} fue cancelada. Motivo: {motivo}.")

    @staticmethod
    def _describir(detalle: CitaDetallada) -> str:
        return f"con {detalle.medico.nombre} el {fecha_y_hora_legibles(detalle.cita.inicio)}"

    def _enviar(self, detalle: CitaDetallada, asunto: str, mensaje: str) -> None:
        self._registro.info("Para: %s | Asunto: %s | %s", detalle.paciente.email, asunto, mensaje)
