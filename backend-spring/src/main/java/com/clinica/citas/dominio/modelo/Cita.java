package com.clinica.citas.dominio.modelo;

import com.clinica.citas.dominio.excepciones.ReglaDeNegocioIncumplida;
import com.clinica.citas.dominio.excepciones.TransicionDeEstadoInvalida;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * Agregado principal. Protege sus propias reglas: el estado solo cambia a través de
 * {@link #confirmar()}, {@link #marcarComoAtendida}, {@link #cancelar} y {@link #reprogramar}.
 */
public final class Cita extends Entidad {

    public static final int LONGITUD_MAXIMA_MOTIVO = 250;

    private final String pacienteId;
    private final String medicoId;
    private final String motivo;
    private IntervaloDeTiempo intervalo;
    private EstadoCita estado;
    private String motivoCancelacion;

    /** Reconstruye una cita existente (por ejemplo, al leerla de la base de datos). */
    public Cita(String id, String pacienteId, String medicoId, IntervaloDeTiempo intervalo,
                String motivo, EstadoCita estado, String motivoCancelacion) {
        super(id);
        this.pacienteId = Objects.requireNonNull(pacienteId, "El paciente es obligatorio");
        this.medicoId = Objects.requireNonNull(medicoId, "El médico es obligatorio");
        this.intervalo = Objects.requireNonNull(intervalo, "El intervalo es obligatorio");
        this.motivo = Textos.requerido(motivo, "motivo", LONGITUD_MAXIMA_MOTIVO);
        this.estado = Objects.requireNonNull(estado, "El estado es obligatorio");
        this.motivoCancelacion = Textos.opcional(motivoCancelacion, "motivo de cancelación", LONGITUD_MAXIMA_MOTIVO);
    }

    public static Cita programar(String pacienteId, String medicoId, IntervaloDeTiempo intervalo, String motivo) {
        return new Cita(nuevoIdentificador(), pacienteId, medicoId, intervalo, motivo, EstadoCita.PROGRAMADA, null);
    }

    public boolean ocupaAgenda() {
        return estado.ocupaAgenda();
    }

    /** True si {@code otra} es una cita distinta, vigente y que comparte tiempo con esta. */
    public boolean seCruzaCon(Cita otra) {
        return !otra.equals(this) && otra.ocupaAgenda() && intervalo.seSolapaCon(otra.intervalo);
    }

    public boolean seCruzaConAlgunaDe(Collection<Cita> agenda) {
        return agenda.stream().anyMatch(this::seCruzaCon);
    }

    public void confirmar() {
        cambiarEstado(EstadoCita.CONFIRMADA);
    }

    public void marcarComoAtendida(LocalDateTime ahora) {
        if (ahora.isBefore(inicio())) {
            throw new ReglaDeNegocioIncumplida("No se puede marcar como atendida una cita que aún no ha comenzado.");
        }
        cambiarEstado(EstadoCita.ATENDIDA);
    }

    public void cancelar(String motivo, LocalDateTime ahora) {
        String motivoValidado = Textos.requerido(motivo, "motivo de cancelación", LONGITUD_MAXIMA_MOTIVO);
        exigirQueNoHayaComenzado(ahora, "cancelar");
        cambiarEstado(EstadoCita.CANCELADA);
        this.motivoCancelacion = motivoValidado;
    }

    /**
     * Mueve la cita a otro horario. Vuelve a PROGRAMADA porque el paciente debe
     * confirmar de nuevo la asistencia en la nueva fecha.
     */
    public void reprogramar(IntervaloDeTiempo nuevoIntervalo, LocalDateTime ahora) {
        if (estado.esFinal()) {
            throw new TransicionDeEstadoInvalida("Una cita " + estado.etiqueta() + " no se puede reprogramar.");
        }
        exigirQueNoHayaComenzado(ahora, "reprogramar");
        this.intervalo = Objects.requireNonNull(nuevoIntervalo);
        this.estado = EstadoCita.PROGRAMADA;
    }

    private void cambiarEstado(EstadoCita destino) {
        if (!estado.puedeCambiarA(destino)) {
            throw new TransicionDeEstadoInvalida(
                    "Una cita " + estado.etiqueta() + " no puede pasar a " + destino.etiqueta() + ".");
        }
        this.estado = destino;
    }

    private void exigirQueNoHayaComenzado(LocalDateTime ahora, String accion) {
        if (!ahora.isBefore(inicio())) {
            throw new ReglaDeNegocioIncumplida("Solo se pueden " + accion + " citas que aún no han comenzado.");
        }
    }

    public String pacienteId() {
        return pacienteId;
    }

    public String medicoId() {
        return medicoId;
    }

    public IntervaloDeTiempo intervalo() {
        return intervalo;
    }

    public LocalDateTime inicio() {
        return intervalo.inicio();
    }

    public LocalDateTime fin() {
        return intervalo.fin();
    }

    public String motivo() {
        return motivo;
    }

    public EstadoCita estado() {
        return estado;
    }

    public Optional<String> motivoCancelacion() {
        return Optional.ofNullable(motivoCancelacion);
    }
}
