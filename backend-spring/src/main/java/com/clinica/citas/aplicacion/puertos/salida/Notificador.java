package com.clinica.citas.aplicacion.puertos.salida;

import com.clinica.citas.aplicacion.consultas.CitaDetallada;

/**
 * Avisa al paciente sobre cambios en su cita. Hoy se escribe en el log; mañana puede
 * ser correo o SMS creando otra implementación, sin tocar los casos de uso.
 */
public interface Notificador {

    void notificarAgendamiento(CitaDetallada detalle);

    void notificarReprogramacion(CitaDetallada detalle);

    void notificarCancelacion(CitaDetallada detalle);
}
