package com.clinica.citas.dominio.reglas;

import com.clinica.citas.dominio.excepciones.ReglaDeNegocioIncumplida;
import com.clinica.citas.dominio.modelo.Cita;

public final class CitaEnElFuturo implements ReglaDeAgendamiento {

    @Override
    public void verificar(Cita cita, ContextoDeAgendamiento contexto) {
        if (!cita.inicio().isAfter(contexto.ahora())) {
            throw new ReglaDeNegocioIncumplida("La cita debe programarse para una fecha y hora futuras.");
        }
    }
}
