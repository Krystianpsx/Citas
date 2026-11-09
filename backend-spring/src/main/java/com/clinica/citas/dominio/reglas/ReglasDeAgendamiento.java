package com.clinica.citas.dominio.reglas;

import com.clinica.citas.dominio.modelo.HorarioDeAtencion;
import java.util.List;

/** Conjunto de reglas vigente en la clínica. Aquí se agrega una regla nueva. */
public final class ReglasDeAgendamiento {

    private ReglasDeAgendamiento() {
    }

    public static List<ReglaDeAgendamiento> estandar(HorarioDeAtencion horario) {
        return List.of(
                new CitaEnElFuturo(),
                new DentroDelHorarioDeAtencion(horario),
                new AlineadaConLasFranjas(horario),
                new MedicoSinCitasCruzadas(),
                new PacienteSinCitasCruzadas());
    }
}
