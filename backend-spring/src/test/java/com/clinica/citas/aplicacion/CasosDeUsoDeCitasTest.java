package com.clinica.citas.aplicacion;

import static com.clinica.citas.soporte.EscenarioDePrueba.LUNES;
import static com.clinica.citas.soporte.EscenarioDePrueba.aLas;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.clinica.citas.aplicacion.consultas.CitaDetallada;
import com.clinica.citas.aplicacion.consultas.CriteriosDeBusquedaDeCitas;
import com.clinica.citas.aplicacion.puertos.entrada.AgendarCitaCasoDeUso;
import com.clinica.citas.aplicacion.puertos.entrada.CancelarCitaCasoDeUso;
import com.clinica.citas.aplicacion.puertos.entrada.ConsultarDisponibilidadCasoDeUso;
import com.clinica.citas.aplicacion.puertos.entrada.ReprogramarCitaCasoDeUso;
import com.clinica.citas.dominio.excepciones.EntidadNoEncontrada;
import com.clinica.citas.dominio.excepciones.ReglaDeNegocioIncumplida;
import com.clinica.citas.dominio.modelo.EstadoCita;
import com.clinica.citas.dominio.modelo.Medico;
import com.clinica.citas.dominio.modelo.Paciente;
import com.clinica.citas.infraestructura.configuracion.CasosDeUso;
import com.clinica.citas.soporte.EscenarioDePrueba;
import com.clinica.citas.soporte.NotificadorEspia.Envio;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CasosDeUsoDeCitasTest {

    private EscenarioDePrueba escenario;
    private CasosDeUso casos;
    private Medico lucia;
    private Paciente ana;

    @BeforeEach
    void prepararEscenario() {
        escenario = new EscenarioDePrueba();
        casos = escenario.casos();
        lucia = escenario.doctoraLucia();
        ana = escenario.pacienteAna();
    }

    private List<LocalDateTime> horasLibresDeLucia(LocalDate dia) {
        return casos.consultarDisponibilidad().ejecutar(new ConsultarDisponibilidadCasoDeUso.Comando(lucia.id(), dia));
    }

    @Nested
    class AgendarCita {

        @Test
        void agenda_guarda_y_notifica() {
            CitaDetallada detalle = escenario.agendar(ana, lucia, aLas(9, 0));

            assertEquals(EstadoCita.PROGRAMADA, detalle.cita().estado());
            assertEquals("Ana Torres", detalle.paciente().nombre());
            var delDia = casos.buscarCitas().ejecutar(CriteriosDeBusquedaDeCitas.todas().delDia(LUNES));
            assertEquals(detalle.cita().id(), delDia.get(0).cita().id());
            assertEquals(List.of(new Envio("agendamiento", detalle.cita().id())), escenario.notificador().enviados());
        }

        @Test
        void la_cita_dura_lo_que_indica_el_horario() {
            assertEquals(aLas(9, 30), escenario.agendar(ana, lucia, aLas(9, 0)).cita().fin());
        }

        @Test
        void no_permite_dos_citas_del_mismo_medico_a_la_misma_hora() {
            Paciente jorge = escenario.pacienteJorge();
            escenario.agendar(ana, lucia, aLas(9, 0));

            var error = assertThrows(ReglaDeNegocioIncumplida.class, () -> escenario.agendar(jorge, lucia, aLas(9, 0)));

            assertTrue(error.getMessage().contains("médico"));
            assertEquals(1, escenario.notificador().enviados().size());
        }

        @Test
        void no_permite_que_el_paciente_este_en_dos_consultas_a_la_vez() {
            Medico andres = escenario.doctorAndres();
            escenario.agendar(ana, lucia, aLas(9, 0));

            var error = assertThrows(ReglaDeNegocioIncumplida.class, () -> escenario.agendar(ana, andres, aLas(9, 0)));

            assertTrue(error.getMessage().contains("paciente"));
        }

        @Test
        void falla_si_el_medico_no_existe() {
            var comando = new AgendarCitaCasoDeUso.Comando(ana.id(), "no-existe", aLas(9, 0), "Control");
            assertThrows(EntidadNoEncontrada.class, () -> casos.agendarCita().ejecutar(comando));
        }
    }

    @Nested
    class CambiosDeEstado {

        @Test
        void confirmar_y_atender() {
            String citaId = escenario.agendar(ana, lucia, aLas(9, 0)).cita().id();
            casos.confirmarCita().ejecutar(citaId);
            escenario.reloj().ajustarA(aLas(9, 5));

            CitaDetallada atendida = casos.atenderCita().ejecutar(citaId);

            assertEquals(EstadoCita.ATENDIDA, atendida.cita().estado());
        }

        @Test
        void cancelar_libera_la_franja_y_notifica() {
            Paciente jorge = escenario.pacienteJorge();
            String citaId = escenario.agendar(ana, lucia, aLas(9, 0)).cita().id();

            casos.cancelarCita().ejecutar(new CancelarCitaCasoDeUso.Comando(citaId, "Viaje"));
            CitaDetallada nueva = escenario.agendar(jorge, lucia, aLas(9, 0));

            assertEquals(EstadoCita.PROGRAMADA, nueva.cita().estado());
            assertTrue(escenario.notificador().enviados().contains(new Envio("cancelacion", citaId)));
        }

        @Test
        void cita_inexistente() {
            assertThrows(EntidadNoEncontrada.class, () -> casos.confirmarCita().ejecutar("no-existe"));
        }
    }

    @Nested
    class ReprogramarCita {

        @Test
        void mueve_la_cita_a_una_franja_libre() {
            String citaId = escenario.agendar(ana, lucia, aLas(9, 0)).cita().id();

            CitaDetallada detalle = casos.reprogramarCita().ejecutar(new ReprogramarCitaCasoDeUso.Comando(citaId, aLas(11, 0)));

            assertEquals(aLas(11, 0), detalle.cita().inicio());
            List<Envio> enviados = escenario.notificador().enviados();
            assertEquals(new Envio("reprogramacion", citaId), enviados.get(enviados.size() - 1));
        }

        @Test
        void puede_quedarse_en_su_propia_franja_sin_chocar_consigo_misma() {
            String citaId = escenario.agendar(ana, lucia, aLas(9, 0)).cita().id();
            var comando = new ReprogramarCitaCasoDeUso.Comando(citaId, aLas(9, 0));
            assertDoesNotThrow(() -> casos.reprogramarCita().ejecutar(comando));
        }

        @Test
        void si_la_nueva_franja_esta_ocupada_la_cita_original_no_cambia() {
            String citaId = escenario.agendar(ana, lucia, aLas(9, 0)).cita().id();
            escenario.agendar(escenario.pacienteJorge(), lucia, aLas(10, 0));

            var comando = new ReprogramarCitaCasoDeUso.Comando(citaId, aLas(10, 0));
            assertThrows(ReglaDeNegocioIncumplida.class, () -> casos.reprogramarCita().ejecutar(comando));

            var guardada = casos.buscarCitas().ejecutar(CriteriosDeBusquedaDeCitas.todas().delPaciente(ana.id())).get(0);
            assertEquals(aLas(9, 0), guardada.cita().inicio());
        }
    }

    @Nested
    class Disponibilidad {

        @Test
        void excluye_franjas_ocupadas() {
            escenario.agendar(ana, lucia, aLas(9, 0));

            List<LocalDateTime> libres = horasLibresDeLucia(LUNES);

            assertFalse(libres.contains(aLas(9, 0)));
            assertTrue(libres.contains(aLas(9, 30)));
            assertEquals(19, libres.size());
        }

        @Test
        void excluye_las_horas_que_ya_pasaron() {
            escenario.reloj().ajustarA(aLas(12, 10));
            assertEquals(aLas(12, 30), horasLibresDeLucia(LUNES).get(0));
        }

        @Test
        void sabado_sin_disponibilidad() {
            assertTrue(horasLibresDeLucia(LocalDate.of(2026, 11, 14)).isEmpty());
        }
    }
}
