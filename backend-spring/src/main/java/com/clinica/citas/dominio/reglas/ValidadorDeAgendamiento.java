package com.clinica.citas.dominio.reglas;

import com.clinica.citas.dominio.modelo.Cita;
import java.util.List;

/** Aplica, en orden, todas las reglas que recibe. No conoce ninguna regla concreta. */
public final class ValidadorDeAgendamiento {

    private final List<ReglaDeAgendamiento> reglas;

    public ValidadorDeAgendamiento(List<ReglaDeAgendamiento> reglas) {
        this.reglas = List.copyOf(reglas);
    }

    public void validar(Cita cita, ContextoDeAgendamiento contexto) {
        reglas.forEach(regla -> regla.verificar(cita, contexto));
    }
}
