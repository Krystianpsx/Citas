package com.clinica.citas.dominio;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.clinica.citas.dominio.excepciones.ReglaDeNegocioIncumplida;
import com.clinica.citas.dominio.modelo.Cita;
import com.clinica.citas.dominio.modelo.HorarioDeAtencion;
import com.clinica.citas.dominio.reglas.AlineadaConLasFranjas;
import com.clinica.citas.dominio.reglas.CitaEnElFuturo;
import com.clinica.citas.dominio.reglas.ContextoDeAgendamiento;
import com.clinica.citas.dominio.reglas.DentroDelHorarioDeAtencion;
import com.clinica.citas.dominio.reglas.MedicoSinCitasCruzadas;
import com.clinica.citas.dominio.reglas.PacienteSinCitasCruzadas;
import com.clinica.citas.dominio.reglas.ReglaDeAgendamiento;
import com.clinica.citas.dominio.reglas.ValidadorDeAgendamiento;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class HorarioYReglasTest {

    private static final HorarioDeAtencion HORARIO = HorarioDeAtencion.deLunesAViernes();
    private static final LocalDate LUNES = LocalDate.of(2026, 11, 9);
    private static final LocalDate SABADO = LocalDate.of(2026, 11, 14);
    private static final LocalDateTime LUNES_TEMPRANO = LUNES.atTime(7, 0);

    private static Cita citaA(LocalDate dia, int hora, int minuto, String pacienteId, String medicoId) {
        return Cita.programar(pacienteId, medicoId, HORARIO.intervaloDeCita(dia.atTime(hora, minuto)), "Control");
    }

    private static Cita citaDelLunesA(int hora, int minuto) {
        return citaA(LUNES, hora, minuto, "p-1", "m-1");
    }

    private static ContextoDeAgendamiento sinOtrasCitas() {
        return new ContextoDeAgendamiento(LUNES_TEMPRANO, List.of(), List.of());
    }

    @Nested
    class Horario {

        @Test
        void un_dia_habil_tiene_veinte_franjas_de_media_hora() {
            var franjas = HORARIO.franjasDelDia(LUNES);
            assertEquals(20, franjas.size());
            assertEquals(LUNES.atTime(8, 0), franjas.get(0).inicio());
            assertEquals(LUNES.atTime(18, 0), franjas.get(19).fin());
        }

        @Test
        void el_fin_de_semana_no_tiene_franjas() {
            assertTrue(HORARIO.franjasDelDia(SABADO).isEmpty());
        }

        @Test
        void la_ultima_cita_puede_terminar_justo_al_cierre() {
            assertTrue(HORARIO.contiene(citaDelLunesA(17, 30).intervalo()));
        }

        @Test
        void describe_el_horario_en_palabras() {
            assertEquals("de lunes a viernes, de 08:00 a 18:00", HORARIO.descripcion());
        }
    }

    @Nested
    class Reglas {

        @Test
        void cita_en_el_pasado() {
            var contexto = new ContextoDeAgendamiento(LUNES.atTime(10, 0), List.of(), List.of());
            assertThrows(ReglaDeNegocioIncumplida.class,
                    () -> new CitaEnElFuturo().verificar(citaDelLunesA(9, 0), contexto));
        }

        @ParameterizedTest
        @ValueSource(ints = {7, 18, 20})
        void fuera_del_horario(int hora) {
            var error = assertThrows(ReglaDeNegocioIncumplida.class,
                    () -> new DentroDelHorarioDeAtencion(HORARIO).verificar(citaDelLunesA(hora, 0), sinOtrasCitas()));
            assertTrue(error.getMessage().contains("lunes a viernes"));
        }

        @Test
        void en_fin_de_semana() {
            Cita sabado = citaA(SABADO, 9, 0, "p-1", "m-1");
            assertThrows(ReglaDeNegocioIncumplida.class,
                    () -> new DentroDelHorarioDeAtencion(HORARIO).verificar(sabado, sinOtrasCitas()));
        }

        @Test
        void inicio_que_no_coincide_con_una_franja() {
            var error = assertThrows(ReglaDeNegocioIncumplida.class,
                    () -> new AlineadaConLasFranjas(HORARIO).verificar(citaDelLunesA(9, 15), sinOtrasCitas()));
            assertTrue(error.getMessage().contains("bloques de 30"));
        }

        @Test
        void medico_ocupado() {
            Cita existente = citaA(LUNES, 9, 0, "otro", "m-1");
            var contexto = new ContextoDeAgendamiento(LUNES_TEMPRANO, List.of(existente), List.of());
            assertThrows(ReglaDeNegocioIncumplida.class,
                    () -> new MedicoSinCitasCruzadas().verificar(citaDelLunesA(9, 0), contexto));
        }

        @Test
        void una_cita_cancelada_libera_la_franja() {
            Cita cancelada = citaA(LUNES, 9, 0, "otro", "m-1");
            cancelada.cancelar("No asistirá", LUNES_TEMPRANO);
            var contexto = new ContextoDeAgendamiento(LUNES_TEMPRANO, List.of(cancelada), List.of());
            assertDoesNotThrow(() -> new MedicoSinCitasCruzadas().verificar(citaDelLunesA(9, 0), contexto));
        }

        @Test
        void paciente_ocupado_con_otro_medico() {
            Cita existente = citaA(LUNES, 9, 0, "p-1", "otro");
            var contexto = new ContextoDeAgendamiento(LUNES_TEMPRANO, List.of(), List.of(existente));
            assertThrows(ReglaDeNegocioIncumplida.class,
                    () -> new PacienteSinCitasCruzadas().verificar(citaDelLunesA(9, 0), contexto));
        }
    }

    /** Principio Abierto/Cerrado: una regla nueva se agrega sin modificar el validador. */
    @Nested
    class ValidadorAbiertoAExtension {

        @Test
        void aplica_una_regla_nueva_definida_fuera_del_dominio() {
            ReglaDeAgendamiento sinCitasLosLunes = (cita, contexto) -> {
                if (cita.inicio().getDayOfWeek() == java.time.DayOfWeek.MONDAY) {
                    throw new ReglaDeNegocioIncumplida("Los lunes no hay agenda.");
                }
            };
            var validador = new ValidadorDeAgendamiento(List.of(new CitaEnElFuturo(), sinCitasLosLunes));

            var error = assertThrows(ReglaDeNegocioIncumplida.class,
                    () -> validador.validar(citaDelLunesA(9, 0), sinOtrasCitas()));
            assertEquals("Los lunes no hay agenda.", error.getMessage());
        }

        @Test
        void sin_reglas_todo_es_valido() {
            Cita delPasado = citaA(LUNES.minusDays(30), 9, 0, "p-1", "m-1");
            assertDoesNotThrow(() -> new ValidadorDeAgendamiento(List.of()).validar(delPasado, sinOtrasCitas()));
        }
    }
}
