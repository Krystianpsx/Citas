package com.clinica.citas.dominio.reglas;

import com.clinica.citas.dominio.modelo.Cita;

/**
 * Una condición que debe cumplir toda cita antes de entrar a la agenda (patrón Strategy).
 *
 * <p>Principio Abierto/Cerrado: para una regla nueva se crea otra implementación y se
 * agrega a {@link ReglasDeAgendamiento#estandar}; ningún caso de uso cambia.
 */
@FunctionalInterface
public interface ReglaDeAgendamiento {

    /** Lanza ReglaDeNegocioIncumplida si la cita no cumple la regla. */
    void verificar(Cita cita, ContextoDeAgendamiento contexto);
}
