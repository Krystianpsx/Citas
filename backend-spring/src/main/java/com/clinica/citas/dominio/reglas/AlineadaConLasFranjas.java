package com.clinica.citas.dominio.reglas;

import com.clinica.citas.dominio.excepciones.ReglaDeNegocioIncumplida;
import com.clinica.citas.dominio.modelo.Cita;
import com.clinica.citas.dominio.modelo.HorarioDeAtencion;

public final class AlineadaConLasFranjas implements ReglaDeAgendamiento {

    private final HorarioDeAtencion horario;

    public AlineadaConLasFranjas(HorarioDeAtencion horario) {
        this.horario = horario;
    }

    @Override
    public void verificar(Cita cita, ContextoDeAgendamiento contexto) {
        if (!horario.esInicioDeFranja(cita.inicio())) {
            throw new ReglaDeNegocioIncumplida(
                    "Las citas empiezan en bloques de " + horario.minutosPorCita() + " minutos.");
        }
    }
}
