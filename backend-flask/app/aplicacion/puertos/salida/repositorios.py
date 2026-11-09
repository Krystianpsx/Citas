"""Puertos de persistencia. Un repositorio por agregado (Segregación de Interfaces).

Contrato que toda implementación debe respetar (Sustitución de Liskov):
- guardar() inserta o actualiza.
- Las búsquedas devuelven copias: modificar lo devuelto no altera lo guardado
  hasta que se llame de nuevo a guardar().
- Los listados vienen ordenados (pacientes y médicos por nombre, citas por inicio).
"""
from abc import ABC, abstractmethod

from app.aplicacion.criterios import CriteriosDeBusquedaDeCitas
from app.dominio.cita import Cita
from app.dominio.medico import Especialidad, Medico
from app.dominio.objetos_valor import DocumentoIdentidad
from app.dominio.paciente import Paciente


class RepositorioDePacientes(ABC):
    @abstractmethod
    def guardar(self, paciente: Paciente) -> None: ...

    @abstractmethod
    def buscar_por_id(self, paciente_id: str) -> Paciente | None: ...

    @abstractmethod
    def buscar_por_documento(self, documento: DocumentoIdentidad) -> Paciente | None: ...

    @abstractmethod
    def listar(self) -> list[Paciente]: ...


class RepositorioDeMedicos(ABC):
    @abstractmethod
    def guardar(self, medico: Medico) -> None: ...

    @abstractmethod
    def buscar_por_id(self, medico_id: str) -> Medico | None: ...

    @abstractmethod
    def buscar_por_registro(self, registro_profesional: str) -> Medico | None: ...

    @abstractmethod
    def listar(self, especialidad: Especialidad | None = None) -> list[Medico]: ...


class RepositorioDeCitas(ABC):
    @abstractmethod
    def guardar(self, cita: Cita) -> None: ...

    @abstractmethod
    def buscar_por_id(self, cita_id: str) -> Cita | None: ...

    @abstractmethod
    def buscar(self, criterios: CriteriosDeBusquedaDeCitas) -> list[Cita]: ...
