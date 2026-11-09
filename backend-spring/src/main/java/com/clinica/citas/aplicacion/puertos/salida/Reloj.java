package com.clinica.citas.aplicacion.puertos.salida;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Abstrae la hora actual. En producción es el reloj del sistema; en las pruebas,
 * un reloj fijo que vuelve repetibles los resultados.
 */
public interface Reloj {

    LocalDateTime ahora();

    default LocalDate hoy() {
        return ahora().toLocalDate();
    }
}
