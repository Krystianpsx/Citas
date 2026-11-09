from abc import ABC, abstractmethod

from app.aplicacion.vistas import CitaDetallada


class Notificador(ABC):
    """Avisa al paciente sobre cambios en su cita. Hoy se escribe en el registro
    (log); mañana puede ser correo o SMS sin tocar los casos de uso."""

    @abstractmethod
    def notificar_agendamiento(self, detalle: CitaDetallada) -> None: ...

    @abstractmethod
    def notificar_reprogramacion(self, detalle: CitaDetallada) -> None: ...

    @abstractmethod
    def notificar_cancelacion(self, detalle: CitaDetallada) -> None: ...
