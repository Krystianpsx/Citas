package com.clinica.citas.soporte;

import com.clinica.citas.aplicacion.consultas.CitaDetallada;
import com.clinica.citas.aplicacion.puertos.salida.Notificador;
import java.util.ArrayList;
import java.util.List;

/** Doble de prueba que registra qué notificaciones se enviaron para poder verificarlas. */
public class NotificadorEspia implements Notificador {

    public record Envio(String tipo, String citaId) {
    }

    private final List<Envio> enviados = new ArrayList<>();

    @Override
    public void notificarAgendamiento(CitaDetallada detalle) {
        enviados.add(new Envio("agendamiento", detalle.cita().id()));
    }

    @Override
    public void notificarReprogramacion(CitaDetallada detalle) {
        enviados.add(new Envio("reprogramacion", detalle.cita().id()));
    }

    @Override
    public void notificarCancelacion(CitaDetallada detalle) {
        enviados.add(new Envio("cancelacion", detalle.cita().id()));
    }

    public List<Envio> enviados() {
        return List.copyOf(enviados);
    }
}
