from datetime import date, datetime

import pytest

from app.aplicacion.comandos import RegistrarMedicoComando, RegistrarPacienteComando
from app.infraestructura.configuracion.ajustes import Ajustes
from app.infraestructura.configuracion.contenedor import Contenedor
from tests.dobles import NotificadorEspia, RelojFijo

# Lunes 9 de noviembre de 2026, 07:00: la clínica aún no abre.
LUNES_TEMPRANO = datetime(2026, 11, 9, 7, 0)
LUNES = LUNES_TEMPRANO.date()


def a_las(hora: int, minuto: int = 0, dia: date = LUNES) -> datetime:
    return datetime(dia.year, dia.month, dia.day, hora, minuto)


@pytest.fixture
def reloj() -> RelojFijo:
    return RelojFijo(LUNES_TEMPRANO)


@pytest.fixture
def notificador() -> NotificadorEspia:
    return NotificadorEspia()


@pytest.fixture
def contenedor(reloj, notificador) -> Contenedor:
    return Contenedor(Ajustes(persistencia="memoria"), reloj=reloj, notificador=notificador)


@pytest.fixture
def medico(contenedor):
    return contenedor.registrar_medico.ejecutar(
        RegistrarMedicoComando("Dra. Lucía Fernández", "MEDICINA_GENERAL", "RM-1001", "lucia@clinica.test")
    )


@pytest.fixture
def otro_medico(contenedor):
    return contenedor.registrar_medico.ejecutar(
        RegistrarMedicoComando("Dr. Andrés Salazar", "CARDIOLOGIA", "RM-2002", "andres@clinica.test")
    )


@pytest.fixture
def paciente(contenedor):
    return contenedor.registrar_paciente.ejecutar(
        RegistrarPacienteComando("Ana Torres", "1023456789", "ana@correo.test")
    )


@pytest.fixture
def otro_paciente(contenedor):
    return contenedor.registrar_paciente.ejecutar(
        RegistrarPacienteComando("Jorge Ramírez", "80123456", "jorge@correo.test")
    )
