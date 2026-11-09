package com.clinica.citas.aplicacion.servicios;

import com.clinica.citas.aplicacion.consultas.CriteriosDeBusquedaDeCitas;
import com.clinica.citas.aplicacion.puertos.salida.Reloj;
import com.clinica.citas.aplicacion.puertos.salida.RepositorioDeCitas;
import com.clinica.citas.dominio.modelo.Cita;
import com.clinica.citas.dominio.reglas.ContextoDeAgendamiento;
import com.clinica.citas.dominio.reglas.ValidadorDeAgendamiento;
import java.time.LocalDate;

/**
 * Reúne la agenda del día del médico y del paciente y aplica las reglas de agendamiento.
 *
 * <p>Refactorización <em>Extract Class</em>: AgendarCita y ReprogramarCita
 * duplicaban la construcción de este contexto.
 */
public class VerificadorDeAgenda {

    private final RepositorioDeCitas citas;
    private final ValidadorDeAgendamiento validador;
    private final Reloj reloj;

    public VerificadorDeAgenda(RepositorioDeCitas citas, ValidadorDeAgendamiento validador, Reloj reloj) {
        this.citas = citas;
        this.validador = validador;
        this.reloj = reloj;
    }

    public void verificar(Cita cita) {
        LocalDate dia = cita.inicio().toLocalDate();
        var contexto = new ContextoDeAgendamiento(
                reloj.ahora(),
                citas.buscar(CriteriosDeBusquedaDeCitas.todas().delDia(dia).delMedico(cita.medicoId())),
                citas.buscar(CriteriosDeBusquedaDeCitas.todas().delDia(dia).delPaciente(cita.pacienteId())));
        validador.validar(cita, contexto);
    }
}
