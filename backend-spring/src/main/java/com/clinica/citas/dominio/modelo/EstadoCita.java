package com.clinica.citas.dominio.modelo;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Ciclo de vida de una cita.
 *
 * <pre>
 *     PROGRAMADA ──► CONFIRMADA ──► ATENDIDA
 *          │               │
 *          └──────►  CANCELADA  ◄──┘
 * </pre>
 *
 * Refactorización <em>Replace Conditional with Table</em>: las transiciones se declaran
 * en una tabla en lugar de una cadena de if/else, así agregar un estado no obliga a tocar
 * la lógica (principio Abierto/Cerrado).
 */
public enum EstadoCita {
    PROGRAMADA,
    CONFIRMADA,
    ATENDIDA,
    CANCELADA;

    private static final Map<EstadoCita, Set<EstadoCita>> TRANSICIONES_PERMITIDAS = Map.of(
            PROGRAMADA, Set.of(CONFIRMADA, CANCELADA),
            CONFIRMADA, Set.of(ATENDIDA, CANCELADA),
            ATENDIDA, Set.of(),
            CANCELADA, Set.of());

    public boolean puedeCambiarA(EstadoCita destino) {
        return TRANSICIONES_PERMITIDAS.get(this).contains(destino);
    }

    public boolean esFinal() {
        return TRANSICIONES_PERMITIDAS.get(this).isEmpty();
    }

    /** Una cita cancelada libera su franja; cualquier otra la mantiene ocupada. */
    public boolean ocupaAgenda() {
        return this != CANCELADA;
    }

    public String etiqueta() {
        return name().toLowerCase(Locale.ROOT);
    }
}
