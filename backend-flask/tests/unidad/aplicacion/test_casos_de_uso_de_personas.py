from datetime import date

import pytest

from app.aplicacion.comandos import RegistrarMedicoComando, RegistrarPacienteComando
from app.dominio.excepciones import DatoInvalido, EntidadDuplicada


def test_registrar_paciente_normaliza_datos(contenedor):
    paciente = contenedor.registrar_paciente.ejecutar(
        RegistrarPacienteComando("  Ana   Torres ", "1.023.456.789", "ANA@correo.test")
    )
    assert paciente.nombre == "Ana Torres"
    assert paciente.documento.numero == "1023456789"
    assert paciente.email.valor == "ana@correo.test"


def test_no_permite_documentos_repetidos(contenedor, paciente):
    with pytest.raises(EntidadDuplicada):
        contenedor.registrar_paciente.ejecutar(RegistrarPacienteComando("Otra", "1023456789", "otra@correo.test"))


def test_no_acepta_fecha_de_nacimiento_futura(contenedor):
    with pytest.raises(DatoInvalido):
        contenedor.registrar_paciente.ejecutar(
            RegistrarPacienteComando("Bebé", "12345678", "bebe@correo.test", fecha_nacimiento=date(2030, 1, 1))
        )


def test_listar_pacientes_por_orden_alfabetico(contenedor, otro_paciente, paciente):
    assert [p.nombre for p in contenedor.listar_pacientes.ejecutar()] == ["Ana Torres", "Jorge Ramírez"]


def test_no_permite_registros_profesionales_repetidos(contenedor, medico):
    with pytest.raises(EntidadDuplicada):
        contenedor.registrar_medico.ejecutar(
            RegistrarMedicoComando("Otro", "PEDIATRIA", "rm-1001", "otro@clinica.test")
        )


def test_especialidad_inexistente(contenedor):
    with pytest.raises(DatoInvalido, match="ASTROLOGIA"):
        contenedor.registrar_medico.ejecutar(RegistrarMedicoComando("X", "ASTROLOGIA", "RM-9", "x@clinica.test"))


def test_filtrar_medicos_por_especialidad(contenedor, medico, otro_medico):
    cardiologos = contenedor.listar_medicos.ejecutar("CARDIOLOGIA")
    assert [m.nombre for m in cardiologos] == ["Dr. Andrés Salazar"]
