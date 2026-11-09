from dataclasses import dataclass
from datetime import datetime

from app.dominio.entidad import Entidad
from app.dominio.estado_cita import EstadoCita
from app.dominio.excepciones import ReglaDeNegocioIncumplida, TransicionDeEstadoInvalida
from app.dominio.objetos_valor import IntervaloDeTiempo
from app.dominio.validaciones import texto_opcional, texto_requerido

LONGITUD_MAXIMA_MOTIVO = 250


@dataclass(eq=False, kw_only=True)
class Cita(Entidad):
    """Agregado principal. Protege sus propias reglas: nadie cambia el estado
    de una cita asignando el atributo directamente, sino a través de sus métodos."""

    paciente_id: str
    medico_id: str
    intervalo: IntervaloDeTiempo
    motivo: str
    estado: EstadoCita = EstadoCita.PROGRAMADA
    motivo_cancelacion: str | None = None

    def __post_init__(self) -> None:
        self.motivo = texto_requerido(self.motivo, "motivo", LONGITUD_MAXIMA_MOTIVO)
        self.motivo_cancelacion = texto_opcional(
            self.motivo_cancelacion, "motivo de cancelación", LONGITUD_MAXIMA_MOTIVO
        )

    @classmethod
    def programar(
        cls, *, paciente_id: str, medico_id: str, intervalo: IntervaloDeTiempo, motivo: str
    ) -> "Cita":
        return cls(paciente_id=paciente_id, medico_id=medico_id, intervalo=intervalo, motivo=motivo)

    @property
    def inicio(self) -> datetime:
        return self.intervalo.inicio

    @property
    def fin(self) -> datetime:
        return self.intervalo.fin

    @property
    def ocupa_agenda(self) -> bool:
        return self.estado.ocupa_agenda

    def se_cruza_con(self, otra: "Cita") -> bool:
        """True si `otra` es una cita distinta, vigente y que comparte tiempo con esta."""
        return otra.id != self.id and otra.ocupa_agenda and self.intervalo.se_solapa_con(otra.intervalo)

    def confirmar(self) -> None:
        self._cambiar_estado(EstadoCita.CONFIRMADA)

    def marcar_como_atendida(self, ahora: datetime) -> None:
        if ahora < self.inicio:
            raise ReglaDeNegocioIncumplida("No se puede marcar como atendida una cita que aún no ha comenzado.")
        self._cambiar_estado(EstadoCita.ATENDIDA)

    def cancelar(self, motivo: str, ahora: datetime) -> None:
        motivo_validado = texto_requerido(motivo, "motivo de cancelación", LONGITUD_MAXIMA_MOTIVO)
        self._exigir_que_no_haya_comenzado(ahora, "cancelar")
        self._cambiar_estado(EstadoCita.CANCELADA)
        self.motivo_cancelacion = motivo_validado

    def reprogramar(self, nuevo_intervalo: IntervaloDeTiempo, ahora: datetime) -> None:
        """Mueve la cita a otro horario. Vuelve a PROGRAMADA porque el paciente
        debe confirmar de nuevo la asistencia en la nueva fecha."""
        if self.estado.es_final:
            raise TransicionDeEstadoInvalida(f"Una cita {self.estado.etiqueta} no se puede reprogramar.")
        self._exigir_que_no_haya_comenzado(ahora, "reprogramar")
        self.intervalo = nuevo_intervalo
        self.estado = EstadoCita.PROGRAMADA

    def _cambiar_estado(self, destino: EstadoCita) -> None:
        if not self.estado.puede_cambiar_a(destino):
            raise TransicionDeEstadoInvalida(
                f"Una cita {self.estado.etiqueta} no puede pasar a {destino.etiqueta}."
            )
        self.estado = destino

    def _exigir_que_no_haya_comenzado(self, ahora: datetime, accion: str) -> None:
        if ahora >= self.inicio:
            raise ReglaDeNegocioIncumplida(f"Solo se pueden {accion} citas que aún no han comenzado.")
