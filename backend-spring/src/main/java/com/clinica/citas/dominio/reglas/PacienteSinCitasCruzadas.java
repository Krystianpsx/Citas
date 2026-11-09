package com.clinica.citas.dominio.reglas;

import com.clinica.citas.dominio.excepciones.ReglaDeNegocioIncumplida;
import com.clinica.citas.dominio.modelo.Cita;

public final class PacienteSinCitasCruzadas implements ReglaDeAgendamiento {

    @Override
    public void verificar(Cita cita, ContextoDeAgendamiento contexto) {
        if (cita.seCruzaConAlgunaDe(contexto.agendaDelPaciente())) {
            throw new ReglaDeNegocioIncumplida("El paciente ya tiene otra cita en ese horario.");
        }
    }
}
