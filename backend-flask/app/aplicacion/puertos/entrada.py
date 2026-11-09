"""Puertos de entrada. Cada interfaz expone un único caso de uso (Segregación de Interfaces):
un controlador que solo agenda citas no depende de métodos para cancelarlas."""
from abc import ABC, abstractmethod
from datetime import datetime

from app.aplicacion.comandos import (
    AgendarCitaComando,
    CancelarCitaComando,
    ConsultarDisponibilidadComando,
    RegistrarMedicoComando,
    RegistrarPacienteComando,
    ReprogramarCitaComando,
)
from app.aplicacion.criterios import CriteriosDeBusquedaDeCitas
from app.aplicacion.vistas import CitaDetallada
from app.dominio.medico import Medico
from app.dominio.paciente import Paciente


class RegistrarPacienteCasoDeUso(ABC):
    @abstractmethod
    def ejecutar(self, comando: RegistrarPacienteComando) -> Paciente: ...


class ListarPacientesCasoDeUso(ABC):
    @abstractmethod
    def ejecutar(self) -> list[Paciente]: ...


class RegistrarMedicoCasoDeUso(ABC):
    @abstractmethod
    def ejecutar(self, comando: RegistrarMedicoComando) -> Medico: ...


class ListarMedicosCasoDeUso(ABC):
    @abstractmethod
    def ejecutar(self, especialidad: str | None = None) -> list[Medico]: ...


class ConsultarDisponibilidadCasoDeUso(ABC):
    @abstractmethod
    def ejecutar(self, comando: ConsultarDisponibilidadComando) -> list[datetime]: ...


class AgendarCitaCasoDeUso(ABC):
    @abstractmethod
    def ejecutar(self, comando: AgendarCitaComando) -> CitaDetallada: ...


class ReprogramarCitaCasoDeUso(ABC):
    @abstractmethod
    def ejecutar(self, comando: ReprogramarCitaComando) -> CitaDetallada: ...


class CancelarCitaCasoDeUso(ABC):
    @abstractmethod
    def ejecutar(self, comando: CancelarCitaComando) -> CitaDetallada: ...


class ConfirmarCitaCasoDeUso(ABC):
    @abstractmethod
    def ejecutar(self, cita_id: str) -> CitaDetallada: ...


class AtenderCitaCasoDeUso(ABC):
    @abstractmethod
    def ejecutar(self, cita_id: str) -> CitaDetallada: ...


class BuscarCitasCasoDeUso(ABC):
    @abstractmethod
    def ejecutar(self, criterios: CriteriosDeBusquedaDeCitas) -> list[CitaDetallada]: ...
