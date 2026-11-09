from datetime import date, datetime, timedelta

import pytest

from app.dominio.cita import Cita
from app.dominio.excepciones import ReglaDeNegocioIncumplida
from app.dominio.horario_atencion import HorarioDeAtencion
from app.dominio.reglas_agendamiento import (
    AlineadaConLasFranjas,
    CitaEnElFuturo,
    ContextoDeAgendamiento,
    DentroDelHorarioDeAtencion,
    MedicoSinCitasCruzadas,
    PacienteSinCitasCruzadas,
    ReglaDeAgendamiento,
    ValidadorDeAgendamiento,
)

HORARIO = HorarioDeAtencion()
LUNES = date(2026, 11, 9)
SABADO = date(2026, 11, 14)


def cita_a_las(hora: int, minuto: int = 0, dia: date = LUNES, paciente="p-1", medico="m-1") -> Cita:
    return Cita.programar(
        paciente_id=paciente,
        medico_id=medico,
        intervalo=HORARIO.intervalo_de_cita(datetime(dia.year, dia.month, dia.day, hora, minuto)),
        motivo="Control",
    )


def contexto(ahora=datetime(2026, 11, 9, 7, 0), medico=(), paciente=()) -> ContextoDeAgendamiento:
    return ContextoDeAgendamiento(ahora=ahora, agenda_del_medico=list(medico), agenda_del_paciente=list(paciente))


class TestHorarioDeAtencion:
    def test_un_dia_habil_tiene_veinte_franjas_de_media_hora(self):
        franjas = HORARIO.franjas_del_dia(LUNES)
        assert len(franjas) == 20
        assert franjas[0].inicio.hour == 8 and franjas[-1].fin.hour == 18

    def test_el_fin_de_semana_no_tiene_franjas(self):
        assert HORARIO.franjas_del_dia(SABADO) == []

    def test_la_ultima_cita_puede_terminar_justo_al_cierre(self):
        assert HORARIO.contiene(cita_a_las(17, 30).intervalo)


class TestReglas:
    def test_cita_en_el_pasado(self):
        with pytest.raises(ReglaDeNegocioIncumplida, match="futuras"):
            CitaEnElFuturo().verificar(cita_a_las(9), contexto(ahora=datetime(2026, 11, 9, 10, 0)))

    @pytest.mark.parametrize("hora", [7, 18, 20])
    def test_fuera_del_horario(self, hora):
        with pytest.raises(ReglaDeNegocioIncumplida, match="lunes a viernes"):
            DentroDelHorarioDeAtencion(HORARIO).verificar(cita_a_las(hora), contexto())

    def test_en_fin_de_semana(self):
        with pytest.raises(ReglaDeNegocioIncumplida):
            DentroDelHorarioDeAtencion(HORARIO).verificar(cita_a_las(9, dia=SABADO), contexto())

    def test_inicio_que_no_coincide_con_una_franja(self):
        with pytest.raises(ReglaDeNegocioIncumplida, match="bloques de 30"):
            AlineadaConLasFranjas(HORARIO).verificar(cita_a_las(9, 15), contexto())

    def test_medico_ocupado(self):
        existente = cita_a_las(9, paciente="otro")
        with pytest.raises(ReglaDeNegocioIncumplida, match="médico"):
            MedicoSinCitasCruzadas().verificar(cita_a_las(9), contexto(medico=[existente]))

    def test_una_cita_cancelada_libera_la_franja(self):
        cancelada = cita_a_las(9, paciente="otro")
        cancelada.cancelar("No asistirá", ahora=datetime(2026, 11, 9, 7, 0))
        MedicoSinCitasCruzadas().verificar(cita_a_las(9), contexto(medico=[cancelada]))

    def test_paciente_ocupado_con_otro_medico(self):
        existente = cita_a_las(9, medico="otro")
        with pytest.raises(ReglaDeNegocioIncumplida, match="paciente"):
            PacienteSinCitasCruzadas().verificar(cita_a_las(9), contexto(paciente=[existente]))


class TestValidadorAbiertoAExtension:
    """Demuestra el principio Abierto/Cerrado: se agrega una regla nueva sin modificar el validador."""

    def test_aplica_una_regla_nueva_definida_fuera_del_dominio(self):
        class SinCitasLosLunes(ReglaDeAgendamiento):
            def verificar(self, cita, contexto):
                if cita.inicio.weekday() == 0:
                    raise ReglaDeNegocioIncumplida("Los lunes no hay agenda.")

        validador = ValidadorDeAgendamiento([CitaEnElFuturo(), SinCitasLosLunes()])
        with pytest.raises(ReglaDeNegocioIncumplida, match="lunes"):
            validador.validar(cita_a_las(9), contexto())

    def test_sin_reglas_todo_es_valido(self):
        ValidadorDeAgendamiento([]).validar(cita_a_las(9, dia=LUNES - timedelta(days=30)), contexto())
