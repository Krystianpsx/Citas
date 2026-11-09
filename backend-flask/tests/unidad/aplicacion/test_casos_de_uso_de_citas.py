from datetime import date

import pytest

from app.aplicacion.comandos import (
    AgendarCitaComando,
    CancelarCitaComando,
    ConsultarDisponibilidadComando,
    ReprogramarCitaComando,
)
from app.aplicacion.criterios import CriteriosDeBusquedaDeCitas
from app.dominio.estado_cita import EstadoCita
from app.dominio.excepciones import EntidadNoEncontrada, ReglaDeNegocioIncumplida
from tests.conftest import LUNES, a_las


def agendar(contenedor, paciente, medico, inicio, motivo="Control general"):
    return contenedor.agendar_cita.ejecutar(
        AgendarCitaComando(paciente_id=paciente.id, medico_id=medico.id, inicio=inicio, motivo=motivo)
    )


class TestAgendarCita:
    def test_agenda_guarda_y_notifica(self, contenedor, notificador, paciente, medico):
        detalle = agendar(contenedor, paciente, medico, a_las(9))

        assert detalle.cita.estado is EstadoCita.PROGRAMADA
        assert detalle.paciente.nombre == "Ana Torres"
        assert contenedor.buscar_citas.ejecutar(CriteriosDeBusquedaDeCitas(dia=LUNES))[0].cita.id == detalle.cita.id
        assert notificador.enviadas == [("agendamiento", detalle.cita.id)]

    def test_la_cita_dura_lo_que_indica_el_horario(self, contenedor, paciente, medico):
        detalle = agendar(contenedor, paciente, medico, a_las(9))
        assert detalle.cita.fin == a_las(9, 30)

    def test_no_permite_dos_citas_del_mismo_medico_a_la_misma_hora(
        self, contenedor, notificador, paciente, otro_paciente, medico
    ):
        agendar(contenedor, paciente, medico, a_las(9))
        with pytest.raises(ReglaDeNegocioIncumplida, match="médico"):
            agendar(contenedor, otro_paciente, medico, a_las(9))
        assert len(notificador.enviadas) == 1

    def test_no_permite_que_el_paciente_este_en_dos_consultas_a_la_vez(
        self, contenedor, paciente, medico, otro_medico
    ):
        agendar(contenedor, paciente, medico, a_las(9))
        with pytest.raises(ReglaDeNegocioIncumplida, match="paciente"):
            agendar(contenedor, paciente, otro_medico, a_las(9))

    def test_falla_si_el_medico_no_existe(self, contenedor, paciente):
        comando = AgendarCitaComando(paciente_id=paciente.id, medico_id="no-existe", inicio=a_las(9), motivo="X")
        with pytest.raises(EntidadNoEncontrada):
            contenedor.agendar_cita.ejecutar(comando)


class TestCambiosDeEstado:
    def test_confirmar_y_atender(self, contenedor, reloj, paciente, medico):
        cita_id = agendar(contenedor, paciente, medico, a_las(9)).cita.id
        contenedor.confirmar_cita.ejecutar(cita_id)
        reloj.momento = a_las(9, 5)

        detalle = contenedor.atender_cita.ejecutar(cita_id)

        assert detalle.cita.estado is EstadoCita.ATENDIDA

    def test_cancelar_libera_la_franja_y_notifica(self, contenedor, notificador, paciente, otro_paciente, medico):
        cita_id = agendar(contenedor, paciente, medico, a_las(9)).cita.id

        contenedor.cancelar_cita.ejecutar(CancelarCitaComando(cita_id=cita_id, motivo="Viaje"))
        nueva = agendar(contenedor, otro_paciente, medico, a_las(9))

        assert nueva.cita.estado is EstadoCita.PROGRAMADA
        assert ("cancelacion", cita_id) in notificador.enviadas

    def test_cita_inexistente(self, contenedor):
        with pytest.raises(EntidadNoEncontrada):
            contenedor.confirmar_cita.ejecutar("no-existe")


class TestReprogramarCita:
    def test_mueve_la_cita_a_una_franja_libre(self, contenedor, notificador, paciente, medico):
        cita_id = agendar(contenedor, paciente, medico, a_las(9)).cita.id

        detalle = contenedor.reprogramar_cita.ejecutar(ReprogramarCitaComando(cita_id, a_las(11)))

        assert detalle.cita.inicio == a_las(11)
        assert notificador.enviadas[-1] == ("reprogramacion", cita_id)

    def test_puede_moverse_dentro_de_su_propia_franja_sin_chocar_consigo_misma(self, contenedor, paciente, medico):
        cita_id = agendar(contenedor, paciente, medico, a_las(9)).cita.id
        contenedor.reprogramar_cita.ejecutar(ReprogramarCitaComando(cita_id, a_las(9)))

    def test_si_la_nueva_franja_esta_ocupada_la_cita_original_no_cambia(
        self, contenedor, paciente, otro_paciente, medico
    ):
        cita_id = agendar(contenedor, paciente, medico, a_las(9)).cita.id
        agendar(contenedor, otro_paciente, medico, a_las(10))

        with pytest.raises(ReglaDeNegocioIncumplida):
            contenedor.reprogramar_cita.ejecutar(ReprogramarCitaComando(cita_id, a_las(10)))

        guardada = contenedor.buscar_citas.ejecutar(CriteriosDeBusquedaDeCitas(paciente_id=paciente.id))[0]
        assert guardada.cita.inicio == a_las(9)


class TestDisponibilidad:
    def test_excluye_franjas_ocupadas(self, contenedor, paciente, medico):
        agendar(contenedor, paciente, medico, a_las(9))
        libres = contenedor.consultar_disponibilidad.ejecutar(ConsultarDisponibilidadComando(medico.id, LUNES))
        assert a_las(9) not in libres
        assert a_las(9, 30) in libres
        assert len(libres) == 19

    def test_excluye_las_horas_que_ya_pasaron(self, contenedor, reloj, medico):
        reloj.momento = a_las(12, 10)
        libres = contenedor.consultar_disponibilidad.ejecutar(ConsultarDisponibilidadComando(medico.id, LUNES))
        assert libres[0] == a_las(12, 30)

    def test_sabado_sin_disponibilidad(self, contenedor, medico):
        sabado = date(2026, 11, 14)
        assert contenedor.consultar_disponibilidad.ejecutar(ConsultarDisponibilidadComando(medico.id, sabado)) == []
