package com.clinica.citas.dominio.modelo;

import com.clinica.citas.dominio.excepciones.DatoInvalido;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Cuándo atiende la clínica y cuánto dura cada cita.
 *
 * <p>La jornada se divide en franjas consecutivas del tamaño de una cita
 * (08:00, 08:30, 09:00...). Toda cita debe empezar al inicio de una franja.
 * Refactorización <em>Replace Magic Literal</em>: los números 8, 18 y 30 vivían
 * repetidos en reglas, consultas y mensajes de error.
 */
public record HorarioDeAtencion(Set<DayOfWeek> diasHabiles, LocalTime apertura, LocalTime cierre, Duration duracionCita) {

    private static final String[] NOMBRES_DE_DIAS =
            {"lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo"};

    public HorarioDeAtencion {
        if (diasHabiles.isEmpty() || !apertura.isBefore(cierre) || duracionCita.isZero() || duracionCita.isNegative()) {
            throw new DatoInvalido("El horario de atención no es coherente.");
        }
        diasHabiles = Set.copyOf(diasHabiles);
    }

    public static HorarioDeAtencion deLunesAViernes() {
        return new HorarioDeAtencion(
                EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
                LocalTime.of(8, 0),
                LocalTime.of(18, 0),
                Duration.ofMinutes(30));
    }

    public IntervaloDeTiempo intervaloDeCita(LocalDateTime inicio) {
        return IntervaloDeTiempo.desde(inicio, duracionCita);
    }

    public boolean contiene(IntervaloDeTiempo intervalo) {
        LocalDate dia = intervalo.inicio().toLocalDate();
        return esDiaHabil(dia)
                && dia.equals(intervalo.fin().toLocalDate())
                && !intervalo.inicio().toLocalTime().isBefore(apertura)
                && !intervalo.fin().toLocalTime().isAfter(cierre);
    }

    public boolean esInicioDeFranja(LocalDateTime momento) {
        long minutosDesdeApertura = Duration.between(apertura, momento.toLocalTime()).toMinutes();
        boolean tieneSegundos = momento.getSecond() != 0 || momento.getNano() != 0;
        return !tieneSegundos && minutosDesdeApertura % minutosPorCita() == 0;
    }

    public List<IntervaloDeTiempo> franjasDelDia(LocalDate dia) {
        List<IntervaloDeTiempo> franjas = new ArrayList<>();
        if (!esDiaHabil(dia)) {
            return franjas;
        }
        LocalDateTime finDeJornada = dia.atTime(cierre);
        for (LocalDateTime inicio = dia.atTime(apertura);
             !inicio.plus(duracionCita).isAfter(finDeJornada);
             inicio = inicio.plus(duracionCita)) {
            franjas.add(intervaloDeCita(inicio));
        }
        return franjas;
    }

    public boolean esDiaHabil(LocalDate dia) {
        return diasHabiles.contains(dia.getDayOfWeek());
    }

    public long minutosPorCita() {
        return duracionCita.toMinutes();
    }

    /** Texto para el usuario, por ejemplo: "de lunes a viernes, de 08:00 a 18:00". */
    public String descripcion() {
        return describirDias() + ", de " + apertura + " a " + cierre;
    }

    private String describirDias() {
        List<Integer> dias = diasHabiles.stream().map(DayOfWeek::getValue).sorted().toList();
        int primero = dias.get(0);
        int ultimo = dias.get(dias.size() - 1);
        boolean sonConsecutivos = ultimo - primero + 1 == dias.size();
        if (sonConsecutivos && dias.size() > 2) {
            return "de " + nombreDeDia(primero) + " a " + nombreDeDia(ultimo);
        }
        return dias.stream().map(HorarioDeAtencion::nombreDeDia).collect(Collectors.joining(", "));
    }

    private static String nombreDeDia(int numeroIso) {
        return NOMBRES_DE_DIAS[numeroIso - 1];
    }
}
