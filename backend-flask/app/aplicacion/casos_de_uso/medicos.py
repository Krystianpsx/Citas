from collections.abc import Sequence
from datetime import datetime

from app.aplicacion.casos_de_uso.comun import exigir_existencia
from app.aplicacion.comandos import ConsultarDisponibilidadComando, RegistrarMedicoComando
from app.aplicacion.criterios import CriteriosDeBusquedaDeCitas
from app.aplicacion.puertos.entrada import (
    ConsultarDisponibilidadCasoDeUso,
    ListarMedicosCasoDeUso,
    RegistrarMedicoCasoDeUso,
)
from app.aplicacion.puertos.salida import Reloj, RepositorioDeCitas, RepositorioDeMedicos
from app.dominio.cita import Cita
from app.dominio.excepciones import EntidadDuplicada
from app.dominio.horario_atencion import HorarioDeAtencion
from app.dominio.medico import Especialidad, Medico
from app.dominio.objetos_valor import Email, IntervaloDeTiempo


class RegistrarMedicoServicio(RegistrarMedicoCasoDeUso):
    def __init__(self, medicos: RepositorioDeMedicos) -> None:
        self._medicos = medicos

    def ejecutar(self, comando: RegistrarMedicoComando) -> Medico:
        medico = Medico(
            nombre=comando.nombre,
            especialidad=Especialidad.desde_codigo(comando.especialidad),
            registro_profesional=comando.registro_profesional,
            email=Email(comando.email),
        )
        self._exigir_registro_libre(medico.registro_profesional)
        self._medicos.guardar(medico)
        return medico

    def _exigir_registro_libre(self, registro: str) -> None:
        if self._medicos.buscar_por_registro(registro) is not None:
            raise EntidadDuplicada(f"Ya existe un médico con el registro profesional {registro}.")


class ListarMedicosServicio(ListarMedicosCasoDeUso):
    def __init__(self, medicos: RepositorioDeMedicos) -> None:
        self._medicos = medicos

    def ejecutar(self, especialidad: str | None = None) -> list[Medico]:
        filtro = Especialidad.desde_codigo(especialidad) if especialidad else None
        return self._medicos.listar(filtro)


class ConsultarDisponibilidadServicio(ConsultarDisponibilidadCasoDeUso):
    """Devuelve las horas de inicio libres de un médico en un día."""

    def __init__(
        self,
        medicos: RepositorioDeMedicos,
        citas: RepositorioDeCitas,
        horario: HorarioDeAtencion,
        reloj: Reloj,
    ) -> None:
        self._medicos = medicos
        self._citas = citas
        self._horario = horario
        self._reloj = reloj

    def ejecutar(self, comando: ConsultarDisponibilidadComando) -> list[datetime]:
        exigir_existencia(self._medicos.buscar_por_id(comando.medico_id), f"el médico con id {comando.medico_id}")
        agenda = self._citas.buscar(CriteriosDeBusquedaDeCitas(dia=comando.dia, medico_id=comando.medico_id))
        ahora = self._reloj.ahora()
        return [
            franja.inicio
            for franja in self._horario.franjas_del_dia(comando.dia)
            if franja.inicio > ahora and _esta_libre(franja, agenda)
        ]


def _esta_libre(franja: IntervaloDeTiempo, agenda: Sequence[Cita]) -> bool:
    return not any(cita.ocupa_agenda and franja.se_solapa_con(cita.intervalo) for cita in agenda)
