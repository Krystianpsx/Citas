"""Sustitución de Liskov en la práctica: las MISMAS pruebas se ejecutan contra el
repositorio en memoria y contra el de SQLAlchemy. Si ambos las pasan, cualquiera
de los dos puede usarse sin que los casos de uso noten la diferencia."""
from datetime import datetime, timedelta

import pytest

from app.aplicacion.criterios import CriteriosDeBusquedaDeCitas
from app.dominio.cita import Cita
from app.dominio.estado_cita import EstadoCita
from app.dominio.medico import Especialidad, Medico
from app.dominio.objetos_valor import DocumentoIdentidad, Email, IntervaloDeTiempo
from app.dominio.paciente import Paciente
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


def _en_memoria():
    return RepositorioDePacientesEnMemoria(), RepositorioDeMedicosEnMemoria(), RepositorioDeCitasEnMemoria()


def _sql():
    sesiones = crear_fabrica_de_sesiones("sqlite:///:memory:")
    return RepositorioDePacientesSql(sesiones), RepositorioDeMedicosSql(sesiones), RepositorioDeCitasSql(sesiones)


@pytest.fixture(params=[_en_memoria, _sql], ids=["memoria", "sql"])
def repositorios(request):
    return request.param()


def _paciente(nombre="Ana Torres", documento="1023456789"):
    return Paciente(nombre=nombre, documento=DocumentoIdentidad(documento), email=Email("ana@correo.test"))


def _medico(nombre="Dra. Lucía", especialidad=Especialidad.PEDIATRIA, registro="RM-1001"):
    return Medico(nombre=nombre, especialidad=especialidad, registro_profesional=registro, email=Email("l@c.test"))


def _cita(paciente, medico, inicio):
    return Cita.programar(
        paciente_id=paciente.id,
        medico_id=medico.id,
        intervalo=IntervaloDeTiempo.desde(inicio, timedelta(minutes=30)),
        motivo="Control",
    )


def test_guarda_y_recupera_un_paciente(repositorios):
    pacientes, _, _ = repositorios
    original = _paciente()
    pacientes.guardar(original)

    recuperado = pacientes.buscar_por_id(original.id)

    assert recuperado == original
    assert recuperado.documento == original.documento
    assert pacientes.buscar_por_documento(DocumentoIdentidad("1023456789")) == original
    assert pacientes.buscar_por_id("no-existe") is None


def test_lista_pacientes_ordenados_por_nombre(repositorios):
    pacientes, _, _ = repositorios
    pacientes.guardar(_paciente("Zoe", "11111111"))
    pacientes.guardar(_paciente("Andrés", "22222222"))
    assert [p.nombre for p in pacientes.listar()] == ["Andrés", "Zoe"]


def test_filtra_medicos_por_especialidad(repositorios):
    _, medicos, _ = repositorios
    medicos.guardar(_medico("Dra. A", Especialidad.PEDIATRIA, "RM-1"))
    medicos.guardar(_medico("Dr. B", Especialidad.CARDIOLOGIA, "RM-2"))
    assert [m.nombre for m in medicos.listar(Especialidad.CARDIOLOGIA)] == ["Dr. B"]
    assert medicos.buscar_por_registro("RM-1").nombre == "Dra. A"


def test_guardar_dos_veces_actualiza(repositorios):
    pacientes, medicos, citas = repositorios
    paciente, medico = _paciente(), _medico()
    pacientes.guardar(paciente)
    medicos.guardar(medico)
    cita = _cita(paciente, medico, datetime(2026, 11, 10, 9, 0))
    citas.guardar(cita)

    cita.confirmar()
    citas.guardar(cita)

    assert citas.buscar_por_id(cita.id).estado is EstadoCita.CONFIRMADA
    assert len(citas.buscar(CriteriosDeBusquedaDeCitas())) == 1


def test_modificar_lo_recuperado_no_altera_lo_guardado(repositorios):
    pacientes, medicos, citas = repositorios
    paciente, medico = _paciente(), _medico()
    pacientes.guardar(paciente)
    medicos.guardar(medico)
    cita = _cita(paciente, medico, datetime(2026, 11, 10, 9, 0))
    citas.guardar(cita)

    citas.buscar_por_id(cita.id).confirmar()  # sin guardar

    assert citas.buscar_por_id(cita.id).estado is EstadoCita.PROGRAMADA


def test_busca_citas_por_dia_medico_y_estado_ordenadas_por_hora(repositorios):
    pacientes, medicos, citas = repositorios
    paciente = _paciente()
    medico_a, medico_b = _medico("A", registro="RM-A"), _medico("B", registro="RM-B")
    pacientes.guardar(paciente)
    medicos.guardar(medico_a)
    medicos.guardar(medico_b)
    tarde = _cita(paciente, medico_a, datetime(2026, 11, 10, 15, 0))
    manana = _cita(paciente, medico_a, datetime(2026, 11, 10, 8, 0))
    otro_dia = _cita(paciente, medico_a, datetime(2026, 11, 11, 9, 0))
    otro_medico = _cita(paciente, medico_b, datetime(2026, 11, 10, 10, 0))
    for cita in (tarde, manana, otro_dia, otro_medico):
        citas.guardar(cita)

    del_dia = citas.buscar(CriteriosDeBusquedaDeCitas(dia=datetime(2026, 11, 10).date(), medico_id=medico_a.id))
    confirmadas = citas.buscar(CriteriosDeBusquedaDeCitas(estado=EstadoCita.CONFIRMADA))

    assert [c.id for c in del_dia] == [manana.id, tarde.id]
    assert confirmadas == []
