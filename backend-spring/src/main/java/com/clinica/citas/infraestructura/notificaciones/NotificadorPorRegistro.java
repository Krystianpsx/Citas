package com.clinica.citas.infraestructura.notificaciones;

import com.clinica.citas.aplicacion.consultas.CitaDetallada;
import com.clinica.citas.aplicacion.puertos.salida.Notificador;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simula el envío de correos escribiéndolos en el log de la aplicación.
 * Para enviar correos reales basta con otra implementación de {@link Notificador}
 * (por ejemplo, con JavaMailSender) y cambiarla en la configuración.
 */
public class NotificadorPorRegistro implements Notificador {

    private static final Logger REGISTRO = LoggerFactory.getLogger("citas.notificaciones");

    @Override
    public void notificarAgendamiento(CitaDetallada detalle) {
        enviar(detalle, "Cita agendada", "Tu cita " + describir(detalle) + " quedó agendada.");
    }

    @Override
    public void notificarReprogramacion(CitaDetallada detalle) {
        enviar(detalle, "Cita reprogramada", "Tu cita se movió: ahora es " + describir(detalle) + ".");
    }

    @Override
    public void notificarCancelacion(CitaDetallada detalle) {
        String motivo = detalle.cita().motivoCancelacion().orElse("sin motivo");
        enviar(detalle, "Cita cancelada", "Tu cita " + describir(detalle) + " fue cancelada. Motivo: " + motivo + ".");
    }

    private static String describir(CitaDetallada detalle) {
        return "con " + detalle.medico().nombre() + " el " + FormatoDeFechas.legible(detalle.cita().inicio());
    }

    private static void enviar(CitaDetallada detalle, String asunto, String mensaje) {
        REGISTRO.info("Para: {} | Asunto: {} | {}", detalle.paciente().email(), asunto, mensaje);
    }
}
