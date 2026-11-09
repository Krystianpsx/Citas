package com.clinica.citas.aplicacion.servicios;

import com.clinica.citas.aplicacion.consultas.CitaDetallada;
import com.clinica.citas.aplicacion.puertos.entrada.AgendarCitaCasoDeUso;
import com.clinica.citas.aplicacion.puertos.salida.Notificador;
import com.clinica.citas.aplicacion.puertos.salida.RepositorioDeCitas;
import com.clinica.citas.dominio.modelo.Cita;
import com.clinica.citas.dominio.modelo.HorarioDeAtencion;

public class AgendarCitaServicio implements AgendarCitaCasoDeUso {

    private final RepositorioDeCitas citas;
    private final DetalladorDeCitas detallador;
    private final VerificadorDeAgenda verificador;
    private final HorarioDeAtencion horario;
    private final Notificador notificador;

    public AgendarCitaServicio(RepositorioDeCitas citas, DetalladorDeCitas detallador, VerificadorDeAgenda verificador,
                               HorarioDeAtencion horario, Notificador notificador) {
        this.citas = citas;
        this.detallador = detallador;
        this.verificador = verificador;
        this.horario = horario;
        this.notificador = notificador;
    }

    @Override
    public CitaDetallada ejecutar(Comando comando) {
        Cita cita = Cita.programar(
                comando.pacienteId(),
                comando.medicoId(),
                horario.intervaloDeCita(comando.inicio()),
                comando.motivo());
        CitaDetallada detalle = detallador.detallar(cita); // además confirma que paciente y médico existen
        verificador.verificar(cita);
        citas.guardar(cita);
        notificador.notificarAgendamiento(detalle);
        return detalle;
    }
}
