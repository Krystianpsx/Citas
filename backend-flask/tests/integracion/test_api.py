"""Pruebas de extremo a extremo de la API HTTP con persistencia en memoria."""
import pytest

from app.infraestructura.configuracion.ajustes import Ajustes
from app.principal import crear_aplicacion
from tests.conftest import LUNES_TEMPRANO
from tests.dobles import NotificadorEspia, RelojFijo


@pytest.fixture
def cliente():
    aplicacion = crear_aplicacion(
        Ajustes(persistencia="memoria"), reloj=RelojFijo(LUNES_TEMPRANO), notificador=NotificadorEspia()
    )
    return aplicacion.test_client()


@pytest.fixture
def ids(cliente):
    medico = cliente.post(
        "/api/medicos",
        json={"nombre": "Dra. Lucía", "especialidad": "PEDIATRIA", "registroProfesional": "rm-1", "email": "l@c.test"},
    ).get_json()
    paciente = cliente.post(
        "/api/pacientes",
        json={"nombre": "Ana Torres", "documento": "1023456789", "email": "ana@correo.test"},
    ).get_json()
    return {"medicoId": medico["id"], "pacienteId": paciente["id"]}


def test_flujo_completo_de_una_cita(cliente, ids):
    creada = cliente.post("/api/citas", json={**ids, "inicio": "2026-11-09T10:00", "motivo": "Fiebre"})
    assert creada.status_code == 201
    cita = creada.get_json()
    assert cita["estado"] == "PROGRAMADA"
    assert cita["fin"] == "2026-11-09T10:30"
    assert cita["medico"]["especialidad"]["nombre"] == "Pediatría"

    confirmada = cliente.post(f"/api/citas/{cita['id']}/confirmar").get_json()
    assert confirmada["estado"] == "CONFIRMADA"

    del_dia = cliente.get("/api/citas?fecha=2026-11-09").get_json()
    assert [c["id"] for c in del_dia] == [cita["id"]]

    disponibilidad = cliente.get(f"/api/medicos/{ids['medicoId']}/disponibilidad?fecha=2026-11-09").get_json()
    assert "2026-11-09T10:00" not in disponibilidad

    cancelada = cliente.post(f"/api/citas/{cita['id']}/cancelar", json={"motivo": "Mejoró"}).get_json()
    assert cancelada["estado"] == "CANCELADA"
    assert cancelada["motivoCancelacion"] == "Mejoró"


def test_regla_de_negocio_responde_422_con_mensaje(cliente, ids):
    respuesta = cliente.post("/api/citas", json={**ids, "inicio": "2026-11-14T10:00", "motivo": "Sábado"})
    assert respuesta.status_code == 422
    assert "lunes a viernes" in respuesta.get_json()["mensaje"]


def test_cruce_de_horario_responde_422(cliente, ids):
    cliente.post("/api/citas", json={**ids, "inicio": "2026-11-09T10:00", "motivo": "Primera"})
    respuesta = cliente.post("/api/citas", json={**ids, "inicio": "2026-11-09T10:00", "motivo": "Segunda"})
    assert respuesta.status_code == 422


def test_datos_mal_formados_responden_400(cliente, ids):
    assert cliente.post("/api/citas", json={**ids, "inicio": "mañana", "motivo": "X"}).status_code == 400
    assert cliente.post("/api/citas", data="no es json").status_code == 400
    assert cliente.post("/api/pacientes", json={"nombre": "X", "documento": "1", "email": "x@y.co"}).status_code == 400


def test_recurso_inexistente_responde_404(cliente):
    respuesta = cliente.post("/api/citas/no-existe/confirmar")
    assert respuesta.status_code == 404
    assert respuesta.get_json()["tipo"] == "EntidadNoEncontrada"


def test_documento_duplicado_responde_409(cliente, ids):
    respuesta = cliente.post(
        "/api/pacientes", json={"nombre": "Otra", "documento": "1023456789", "email": "otra@correo.test"}
    )
    assert respuesta.status_code == 409


def test_lista_especialidades(cliente):
    especialidades = cliente.get("/api/medicos/especialidades").get_json()
    assert {"codigo": "CARDIOLOGIA", "nombre": "Cardiología"} in especialidades


def test_horario_de_atencion(cliente):
    horario = cliente.get("/api/horario").get_json()
    assert horario == {
        "apertura": "08:00",
        "cierre": "18:00",
        "duracionCitaMinutos": 30,
        "diasHabiles": [0, 1, 2, 3, 4],
        "descripcion": "de lunes a viernes, de 08:00 a 18:00",
    }


def test_ruta_inexistente_responde_json_404(cliente):
    respuesta = cliente.get("/api/no-existe")
    assert respuesta.status_code == 404
    assert "mensaje" in respuesta.get_json()
