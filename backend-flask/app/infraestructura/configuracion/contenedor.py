"""Raíz de composición: el ÚNICO lugar que conoce las clases concretas.

Aquí se decide qué adaptador implementa cada puerto. Cambiar SQLite por memoria,
o el notificador por uno de correo, es cambiar una línea en este archivo.
"""
from collections.abc import Callable

from app.aplicacion.casos_de_uso.citas import (
    AgendarCitaServicio,
    AtenderCitaServicio,
    BuscarCitasServicio,
    CancelarCitaServicio,
    ConfirmarCitaServicio,
    ReprogramarCitaServicio,
)
from app.aplicacion.casos_de_uso.comun import DetalladorDeCitas, VerificadorDeAgenda
from app.aplicacion.casos_de_uso.medicos import (
    ConsultarDisponibilidadServicio,
    ListarMedicosServicio,
    RegistrarMedicoServicio,
)
from app.aplicacion.casos_de_uso.pacientes import ListarPacientesServicio, RegistrarPacienteServicio
from app.aplicacion.puertos.salida import (
    Notificador,
    Reloj,
    RepositorioDeCitas,
    RepositorioDeMedicos,
    RepositorioDePacientes,
)
from app.dominio.horario_atencion import HorarioDeAtencion
from app.dominio.reglas_agendamiento import ValidadorDeAgendamiento, reglas_estandar
from app.infraestructura.configuracion.ajustes import PERSISTENCIA_EN_MEMORIA, PERSISTENCIA_SQL, Ajustes
from app.infraestructura.notificaciones.notificador_por_registro import NotificadorPorRegistro
from app.infraestructura.persistencia.memoria.repositorios import (
    RepositorioDeCitasEnMemoria,
    RepositorioDeMedicosEnMemoria,
    RepositorioDePacientesEnMemoria,
)
from app.infraestructura.persistencia.sql.conexion import crear_fabrica_de_sesiones
from app.infraestructura.persistencia.sql.repositorios import (
    RepositorioDeCitasSql,
    RepositorioDeMedicosSql,
    RepositorioDePacientesSql,
)
from app.infraestructura.reloj.reloj_del_sistema import RelojDelSistema

Repositorios = tuple[RepositorioDePacientes, RepositorioDeMedicos, RepositorioDeCitas]


def _repositorios_sql(ajustes: Ajustes) -> Repositorios:
    sesiones = crear_fabrica_de_sesiones(ajustes.url_base_de_datos)
    return RepositorioDePacientesSql(sesiones), RepositorioDeMedicosSql(sesiones), RepositorioDeCitasSql(sesiones)


def _repositorios_en_memoria(_: Ajustes) -> Repositorios:
    return RepositorioDePacientesEnMemoria(), RepositorioDeMedicosEnMemoria(), RepositorioDeCitasEnMemoria()


_FABRICAS_DE_REPOSITORIOS: dict[str, Callable[[Ajustes], Repositorios]] = {
    PERSISTENCIA_SQL: _repositorios_sql,
    PERSISTENCIA_EN_MEMORIA: _repositorios_en_memoria,
}


class Contenedor:
    def __init__(
        self,
        ajustes: Ajustes,
        *,
        reloj: Reloj | None = None,
        notificador: Notificador | None = None,
        horario: HorarioDeAtencion | None = None,
    ) -> None:
        self.reloj = reloj or RelojDelSistema()
        self.horario = horario or HorarioDeAtencion()
        notificador = notificador or NotificadorPorRegistro()
        pacientes, medicos, citas = _crear_repositorios(ajustes)

        detallador = DetalladorDeCitas(pacientes, medicos)
        verificador = VerificadorDeAgenda(citas, ValidadorDeAgendamiento(reglas_estandar(self.horario)), self.reloj)

        self.registrar_paciente = RegistrarPacienteServicio(pacientes, self.reloj)
        self.listar_pacientes = ListarPacientesServicio(pacientes)

        self.registrar_medico = RegistrarMedicoServicio(medicos)
        self.listar_medicos = ListarMedicosServicio(medicos)
        self.consultar_disponibilidad = ConsultarDisponibilidadServicio(medicos, citas, self.horario, self.reloj)

        self.agendar_cita = AgendarCitaServicio(citas, detallador, verificador, self.horario, notificador)
        self.buscar_citas = BuscarCitasServicio(citas, detallador)
        self.confirmar_cita = ConfirmarCitaServicio(citas, detallador)
        self.atender_cita = AtenderCitaServicio(citas, detallador, self.reloj)
        self.cancelar_cita = CancelarCitaServicio(citas, detallador, self.reloj, notificador)
        self.reprogramar_cita = ReprogramarCitaServicio(
            citas, detallador, verificador, self.horario, self.reloj, notificador
        )


def _crear_repositorios(ajustes: Ajustes) -> Repositorios:
    fabrica = _FABRICAS_DE_REPOSITORIOS.get(ajustes.persistencia)
    if fabrica is None:
        opciones = ", ".join(_FABRICAS_DE_REPOSITORIOS)
        raise ValueError(f"PERSISTENCIA='{ajustes.persistencia}' no es válida. Opciones: {opciones}.")
    return fabrica(ajustes)
