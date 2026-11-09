package com.clinica.citas.dominio.modelo;

import com.clinica.citas.dominio.excepciones.DatoInvalido;
import java.time.Duration;
import java.time.LocalDateTime;

/** Rango [inicio, fin). Dos intervalos contiguos (uno termina cuando el otro empieza) no se solapan. */
public record IntervaloDeTiempo(LocalDateTime inicio, LocalDateTime fin) {

    public IntervaloDeTiempo {
        if (inicio == null || fin == null) {
            throw new DatoInvalido("El intervalo necesita inicio y fin.");
        }
        if (!fin.isAfter(inicio)) {
            throw new DatoInvalido("El fin del intervalo debe ser posterior a su inicio.");
        }
    }

    public static IntervaloDeTiempo desde(LocalDateTime inicio, Duration duracion) {
        if (inicio == null) {
            throw new DatoInvalido("La fecha y hora de inicio es obligatoria.");
        }
        return new IntervaloDeTiempo(inicio, inicio.plus(duracion));
    }

    public boolean seSolapaCon(IntervaloDeTiempo otro) {
        return inicio.isBefore(otro.fin) && otro.inicio.isBefore(fin);
    }
}
