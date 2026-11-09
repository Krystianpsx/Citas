package com.clinica.citas.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.clinica.citas.dominio.excepciones.DatoInvalido;
import com.clinica.citas.dominio.excepciones.ReglaDeNegocioIncumplida;
import com.clinica.citas.dominio.excepciones.TransicionDeEstadoInvalida;
import com.clinica.citas.dominio.modelo.Cita;
import com.clinica.citas.dominio.modelo.EstadoCita;
import com.clinica.citas.dominio.modelo.IntervaloDeTiempo;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CitaTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 10, 9, 0);
    private static final LocalDateTime ANTES = INICIO.minusHours(2);
    private static final LocalDateTime DESPUES = INICIO.plusMinutes(10);

    private static Cita nuevaCita(LocalDateTime inicio) {
        return Cita.programar("p-1", "m-1", IntervaloDeTiempo.desde(inicio, Duration.ofMinutes(30)), "Control");
    }

    private static Cita nuevaCita() {
        return nuevaCita(INICIO);
    }

    @Test
    void una_cita_nueva_queda_programada() {
        assertEquals(EstadoCita.PROGRAMADA, nuevaCita().estado());
    }

    @Test
    void el_motivo_es_obligatorio() {
        var intervalo = IntervaloDeTiempo.desde(INICIO, Duration.ofMinutes(30));
        assertThrows(DatoInvalido.class, () -> Cita.programar("p", "m", intervalo, "   "));
    }

    @Test
    void flujo_completo_programada_confirmada_atendida() {
        Cita cita = nuevaCita();
        cita.confirmar();
        cita.marcarComoAtendida(DESPUES);
        assertEquals(EstadoCita.ATENDIDA, cita.estado());
    }

    @Test
    void no_se_atiende_una_cita_sin_confirmar() {
        assertThrows(TransicionDeEstadoInvalida.class, () -> nuevaCita().marcarComoAtendida(DESPUES));
    }

    @Test
    void no_se_atiende_antes_de_la_hora() {
        Cita cita = nuevaCita();
        cita.confirmar();
        assertThrows(ReglaDeNegocioIncumplida.class, () -> cita.marcarComoAtendida(ANTES));
    }

    @Test
    void cancelar_guarda_el_motivo_y_libera_la_agenda() {
        Cita cita = nuevaCita();
        cita.cancelar("Viaje imprevisto", ANTES);
        assertEquals(EstadoCita.CANCELADA, cita.estado());
        assertEquals(Optional.of("Viaje imprevisto"), cita.motivoCancelacion());
        assertFalse(cita.ocupaAgenda());
    }

    @Test
    void no_se_cancela_una_cita_que_ya_empezo() {
        assertThrows(ReglaDeNegocioIncumplida.class, () -> nuevaCita().cancelar("Tarde", DESPUES));
    }

    @Test
    void una_cita_cancelada_no_se_puede_confirmar() {
        Cita cita = nuevaCita();
        cita.cancelar("No puede asistir", ANTES);
        assertThrows(TransicionDeEstadoInvalida.class, cita::confirmar);
    }

    @Test
    void si_el_motivo_de_cancelacion_es_invalido_la_cita_no_cambia() {
        Cita cita = nuevaCita();
        assertThrows(DatoInvalido.class, () -> cita.cancelar("", ANTES));
        assertEquals(EstadoCita.PROGRAMADA, cita.estado());
    }

    @Test
    void reprogramar_mueve_la_cita_y_pide_nueva_confirmacion() {
        Cita cita = nuevaCita();
        cita.confirmar();
        var nuevoIntervalo = IntervaloDeTiempo.desde(INICIO.plusDays(1), Duration.ofMinutes(30));

        cita.reprogramar(nuevoIntervalo, ANTES);

        assertEquals(nuevoIntervalo, cita.intervalo());
        assertEquals(EstadoCita.PROGRAMADA, cita.estado());
    }

    @Test
    void una_cita_atendida_no_se_reprograma() {
        Cita cita = nuevaCita();
        cita.confirmar();
        cita.marcarComoAtendida(DESPUES);
        var otroDia = IntervaloDeTiempo.desde(INICIO.plusDays(1), Duration.ofMinutes(30));
        assertThrows(TransicionDeEstadoInvalida.class, () -> cita.reprogramar(otroDia, DESPUES));
    }

    @ParameterizedTest(name = "{0} -> {1}: {2}")
    @CsvSource({
            "PROGRAMADA, CONFIRMADA, true",
            "PROGRAMADA, ATENDIDA, false",
            "CONFIRMADA, CANCELADA, true",
            "ATENDIDA, CANCELADA, false",
            "CANCELADA, PROGRAMADA, false"})
    void tabla_de_transiciones(EstadoCita origen, EstadoCita destino, boolean permitido) {
        assertEquals(permitido, origen.puedeCambiarA(destino));
    }

    @Test
    void una_cita_no_se_cruza_consigo_misma() {
        Cita cita = nuevaCita();
        assertFalse(cita.seCruzaCon(cita));
    }
}
