package com.clinica.citas.aplicacion.servicios;

import com.clinica.citas.aplicacion.consultas.CitaDetallada;
import com.clinica.citas.aplicacion.puertos.entrada.ReprogramarCitaCasoDeUso;
import com.clinica.citas.aplicacion.puertos.salida.Notificador;
import com.clinica.citas.aplicacion.puertos.salida.Reloj;
import com.clinica.citas.aplicacion.puertos.salida.RepositorioDeCitas;
import com.clinica.citas.dominio.modelo.Cita;
import com.clinica.citas.dominio.modelo.HorarioDeAtencion;

public class ReprogramarCitaServicio extends OperacionSobreCitaExistente implements ReprogramarCitaCasoDeUso {

    private final VerificadorDeAgenda verificador;
    private final HorarioDeAtencion horario;
    private final Reloj reloj;
    private final Notificador notificador;

    public ReprogramarCitaServicio(RepositorioDeCitas citas, DetalladorDeCitas detallador,
                                   VerificadorDeAgenda verificador, HorarioDeAtencion horario,
                                   Reloj reloj, Notificador notificador) {
        super(citas, detallador);
        this.verificador = verificador;
        this.horario = horario;
        this.reloj = reloj;
        this.notificador = notificador;
    }

    @Override
    public CitaDetallada ejecutar(Comando comando) {
        Cita cita = obtenerCita(comando.citaId());
        cita.reprogramar(horario.intervaloDeCita(comando.nuevoInicio()), reloj.ahora());
        verificador.verificar(cita);
        CitaDetallada detalle = guardarYDetallar(cita);
        notificador.notificarReprogramacion(detalle);
        return detalle;
    }
}
