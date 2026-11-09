package com.clinica.citas.dominio.reglas;

import com.clinica.citas.dominio.excepciones.ReglaDeNegocioIncumplida;
import com.clinica.citas.dominio.modelo.Cita;
import com.clinica.citas.dominio.modelo.HorarioDeAtencion;

public final class DentroDelHorarioDeAtencion implements ReglaDeAgendamiento {

    private final HorarioDeAtencion horario;

    public DentroDelHorarioDeAtencion(HorarioDeAtencion horario) {
        this.horario = horario;
    }

    @Override
    public void verificar(Cita cita, ContextoDeAgendamiento contexto) {
        if (!horario.contiene(cita.intervalo())) {
            throw new ReglaDeNegocioIncumplida(
                    "La clínica atiende " + horario.descripcion() + ". Elige un horario dentro de ese rango.");
        }
    }
}
