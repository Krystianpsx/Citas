package com.clinica.citas.dominio.reglas;

import com.clinica.citas.dominio.excepciones.ReglaDeNegocioIncumplida;
import com.clinica.citas.dominio.modelo.Cita;

public final class MedicoSinCitasCruzadas implements ReglaDeAgendamiento {

    @Override
    public void verificar(Cita cita, ContextoDeAgendamiento contexto) {
        if (cita.seCruzaConAlgunaDe(contexto.agendaDelMedico())) {
            throw new ReglaDeNegocioIncumplida("El médico ya tiene una cita en ese horario.");
        }
    }
}
