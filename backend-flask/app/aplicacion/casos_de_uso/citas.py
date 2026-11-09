from app.aplicacion.casos_de_uso.comun import DetalladorDeCitas, VerificadorDeAgenda, exigir_existencia
from app.aplicacion.comandos import AgendarCitaComando, CancelarCitaComando, ReprogramarCitaComando
from app.aplicacion.criterios import CriteriosDeBusquedaDeCitas
from app.aplicacion.puertos.entrada import (
    AgendarCitaCasoDeUso,
    AtenderCitaCasoDeUso,
    BuscarCitasCasoDeUso,
    CancelarCitaCasoDeUso,
    ConfirmarCitaCasoDeUso,
    ReprogramarCitaCasoDeUso,
)
from app.aplicacion.puertos.salida import Notificador, Reloj, RepositorioDeCitas
from app.aplicacion.vistas import CitaDetallada
from app.dominio.cita import Cita
from app.dominio.horario_atencion import HorarioDeAtencion


class AgendarCitaServicio(AgendarCitaCasoDeUso):
    def __init__(
        self,
        citas: RepositorioDeCitas,
        detallador: DetalladorDeCitas,
        verificador: VerificadorDeAgenda,
        horario: HorarioDeAtencion,
        notificador: Notificador,
    ) -> None:
        self._citas = citas
        self._detallador = detallador
        self._verificador = verificador
        self._horario = horario
        self._notificador = notificador

    def ejecutar(self, comando: AgendarCitaComando) -> CitaDetallada:
        cita = Cita.programar(
            paciente_id=comando.paciente_id,
            medico_id=comando.medico_id,
            intervalo=self._horario.intervalo_de_cita(comando.inicio),
            motivo=comando.motivo,
        )
        detalle = self._detallador.detallar(cita)  # además confirma que paciente y médico existen
        self._verificador.verificar(cita)
        self._citas.guardar(cita)
        self._notificador.notificar_agendamiento(detalle)
        return detalle


class _OperacionSobreCitaExistente:
    """Comportamiento común de los casos de uso que parten de una cita guardada.

    Refactorización *Pull Up Method*: buscar la cita, fallar si no existe,
    guardarla y devolverla detallada estaba copiado en cuatro clases."""

    def __init__(self, citas: RepositorioDeCitas, detallador: DetalladorDeCitas) -> None:
        self._citas = citas
        self._detallador = detallador

    def _obtener_cita(self, cita_id: str) -> Cita:
        return exigir_existencia(self._citas.buscar_por_id(cita_id), f"la cita con id {cita_id}")

    def _guardar_y_detallar(self, cita: Cita) -> CitaDetallada:
        self._citas.guardar(cita)
        return self._detallador.detallar(cita)


class ConfirmarCitaServicio(_OperacionSobreCitaExistente, ConfirmarCitaCasoDeUso):
    def ejecutar(self, cita_id: str) -> CitaDetallada:
        cita = self._obtener_cita(cita_id)
        cita.confirmar()
        return self._guardar_y_detallar(cita)


class AtenderCitaServicio(_OperacionSobreCitaExistente, AtenderCitaCasoDeUso):
    def __init__(self, citas: RepositorioDeCitas, detallador: DetalladorDeCitas, reloj: Reloj) -> None:
        super().__init__(citas, detallador)
        self._reloj = reloj

    def ejecutar(self, cita_id: str) -> CitaDetallada:
        cita = self._obtener_cita(cita_id)
        cita.marcar_como_atendida(self._reloj.ahora())
        return self._guardar_y_detallar(cita)


class CancelarCitaServicio(_OperacionSobreCitaExistente, CancelarCitaCasoDeUso):
    def __init__(
        self,
        citas: RepositorioDeCitas,
        detallador: DetalladorDeCitas,
        reloj: Reloj,
        notificador: Notificador,
    ) -> None:
        super().__init__(citas, detallador)
        self._reloj = reloj
        self._notificador = notificador

    def ejecutar(self, comando: CancelarCitaComando) -> CitaDetallada:
        cita = self._obtener_cita(comando.cita_id)
        cita.cancelar(comando.motivo, self._reloj.ahora())
        detalle = self._guardar_y_detallar(cita)
        self._notificador.notificar_cancelacion(detalle)
        return detalle


class ReprogramarCitaServicio(_OperacionSobreCitaExistente, ReprogramarCitaCasoDeUso):
    def __init__(
        self,
        citas: RepositorioDeCitas,
        detallador: DetalladorDeCitas,
        verificador: VerificadorDeAgenda,
        horario: HorarioDeAtencion,
        reloj: Reloj,
        notificador: Notificador,
    ) -> None:
        super().__init__(citas, detallador)
        self._verificador = verificador
        self._horario = horario
        self._reloj = reloj
        self._notificador = notificador

    def ejecutar(self, comando: ReprogramarCitaComando) -> CitaDetallada:
        cita = self._obtener_cita(comando.cita_id)
        cita.reprogramar(self._horario.intervalo_de_cita(comando.nuevo_inicio), self._reloj.ahora())
        self._verificador.verificar(cita)
        detalle = self._guardar_y_detallar(cita)
        self._notificador.notificar_reprogramacion(detalle)
        return detalle


class BuscarCitasServicio(BuscarCitasCasoDeUso):
    def __init__(self, citas: RepositorioDeCitas, detallador: DetalladorDeCitas) -> None:
        self._citas = citas
        self._detallador = detallador

    def ejecutar(self, criterios: CriteriosDeBusquedaDeCitas) -> list[CitaDetallada]:
        return self._detallador.detallar_varias(self._citas.buscar(criterios))
