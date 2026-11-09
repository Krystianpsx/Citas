from datetime import datetime, timedelta

import pytest

from app.dominio.cita import Cita
from app.dominio.estado_cita import EstadoCita
from app.dominio.excepciones import DatoInvalido, ReglaDeNegocioIncumplida, TransicionDeEstadoInvalida
from app.dominio.objetos_valor import IntervaloDeTiempo

INICIO = datetime(2026, 11, 10, 9, 0)
ANTES = INICIO - timedelta(hours=2)
DESPUES = INICIO + timedelta(minutes=10)


def nueva_cita(inicio: datetime = INICIO) -> Cita:
    return Cita.programar(
        paciente_id="p-1",
        medico_id="m-1",
        intervalo=IntervaloDeTiempo.desde(inicio, timedelta(minutes=30)),
        motivo="Control",
    )


def test_una_cita_nueva_queda_programada():
    assert nueva_cita().estado is EstadoCita.PROGRAMADA


def test_el_motivo_es_obligatorio():
    with pytest.raises(DatoInvalido):
        Cita.programar(paciente_id="p", medico_id="m", intervalo=nueva_cita().intervalo, motivo="   ")


def test_flujo_completo_programada_confirmada_atendida():
    cita = nueva_cita()
    cita.confirmar()
    cita.marcar_como_atendida(ahora=DESPUES)
    assert cita.estado is EstadoCita.ATENDIDA


def test_no_se_atiende_una_cita_sin_confirmar():
    with pytest.raises(TransicionDeEstadoInvalida):
        nueva_cita().marcar_como_atendida(ahora=DESPUES)


def test_no_se_atiende_antes_de_la_hora():
    cita = nueva_cita()
    cita.confirmar()
    with pytest.raises(ReglaDeNegocioIncumplida):
        cita.marcar_como_atendida(ahora=ANTES)


def test_cancelar_guarda_el_motivo():
    cita = nueva_cita()
    cita.cancelar("Viaje imprevisto", ahora=ANTES)
    assert cita.estado is EstadoCita.CANCELADA
    assert cita.motivo_cancelacion == "Viaje imprevisto"
    assert not cita.ocupa_agenda


def test_no_se_cancela_una_cita_que_ya_empezo():
    with pytest.raises(ReglaDeNegocioIncumplida):
        nueva_cita().cancelar("Tarde", ahora=DESPUES)


def test_una_cita_cancelada_no_se_puede_confirmar():
    cita = nueva_cita()
    cita.cancelar("No puede asistir", ahora=ANTES)
    with pytest.raises(TransicionDeEstadoInvalida):
        cita.confirmar()


def test_si_el_motivo_de_cancelacion_es_invalido_la_cita_no_cambia():
    cita = nueva_cita()
    with pytest.raises(DatoInvalido):
        cita.cancelar("", ahora=ANTES)
    assert cita.estado is EstadoCita.PROGRAMADA


def test_reprogramar_mueve_la_cita_y_pide_nueva_confirmacion():
    cita = nueva_cita()
    cita.confirmar()
    nuevo = IntervaloDeTiempo.desde(INICIO + timedelta(days=1), timedelta(minutes=30))
    cita.reprogramar(nuevo, ahora=ANTES)
    assert cita.intervalo == nuevo
    assert cita.estado is EstadoCita.PROGRAMADA


def test_una_cita_atendida_no_se_reprograma():
    cita = nueva_cita()
    cita.confirmar()
    cita.marcar_como_atendida(ahora=DESPUES)
    with pytest.raises(TransicionDeEstadoInvalida):
        cita.reprogramar(nueva_cita(INICIO + timedelta(days=1)).intervalo, ahora=DESPUES)


@pytest.mark.parametrize(
    ("origen", "destino", "permitido"),
    [
        (EstadoCita.PROGRAMADA, EstadoCita.CONFIRMADA, True),
        (EstadoCita.PROGRAMADA, EstadoCita.ATENDIDA, False),
        (EstadoCita.CONFIRMADA, EstadoCita.CANCELADA, True),
        (EstadoCita.ATENDIDA, EstadoCita.CANCELADA, False),
        (EstadoCita.CANCELADA, EstadoCita.PROGRAMADA, False),
    ],
)
def test_tabla_de_transiciones(origen, destino, permitido):
    assert origen.puede_cambiar_a(destino) is permitido


def test_una_cita_no_se_cruza_consigo_misma():
    cita = nueva_cita()
    assert not cita.se_cruza_con(cita)
