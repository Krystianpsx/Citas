package com.clinica.citas.infraestructura.web;

import com.clinica.citas.aplicacion.consultas.CriteriosDeBusquedaDeCitas;
import com.clinica.citas.aplicacion.puertos.entrada.AgendarCitaCasoDeUso;
import com.clinica.citas.aplicacion.puertos.entrada.AtenderCitaCasoDeUso;
import com.clinica.citas.aplicacion.puertos.entrada.BuscarCitasCasoDeUso;
import com.clinica.citas.aplicacion.puertos.entrada.CancelarCitaCasoDeUso;
import com.clinica.citas.aplicacion.puertos.entrada.ConfirmarCitaCasoDeUso;
import com.clinica.citas.aplicacion.puertos.entrada.ReprogramarCitaCasoDeUso;
import com.clinica.citas.dominio.modelo.EstadoCita;
import com.clinica.citas.infraestructura.web.dto.AgendarCitaSolicitud;
import com.clinica.citas.infraestructura.web.dto.CancelarCitaSolicitud;
import com.clinica.citas.infraestructura.web.dto.CitaRespuesta;
import com.clinica.citas.infraestructura.web.dto.ReprogramarCitaSolicitud;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/citas")
public class CitasControlador {

    private final AgendarCitaCasoDeUso agendarCita;
    private final BuscarCitasCasoDeUso buscarCitas;
    private final ConfirmarCitaCasoDeUso confirmarCita;
    private final AtenderCitaCasoDeUso atenderCita;
    private final CancelarCitaCasoDeUso cancelarCita;
    private final ReprogramarCitaCasoDeUso reprogramarCita;

    public CitasControlador(AgendarCitaCasoDeUso agendarCita, BuscarCitasCasoDeUso buscarCitas,
                            ConfirmarCitaCasoDeUso confirmarCita, AtenderCitaCasoDeUso atenderCita,
                            CancelarCitaCasoDeUso cancelarCita, ReprogramarCitaCasoDeUso reprogramarCita) {
        this.agendarCita = agendarCita;
        this.buscarCitas = buscarCitas;
        this.confirmarCita = confirmarCita;
        this.atenderCita = atenderCita;
        this.cancelarCita = cancelarCita;
        this.reprogramarCita = reprogramarCita;
    }

    @GetMapping
    public List<CitaRespuesta> buscar(@RequestParam(name = "fecha", required = false) String fecha,
                                      @RequestParam(name = "medicoId", required = false) String medicoId,
                                      @RequestParam(name = "pacienteId", required = false) String pacienteId,
                                      @RequestParam(name = "estado", required = false) String estado) {
        var criterios = CriteriosDeBusquedaDeCitas.todas()
                .delDia(CamposDeEntrada.fechaOpcional(fecha, "fecha"))
                .delMedico(vacioComoNulo(medicoId))
                .delPaciente(vacioComoNulo(pacienteId))
                .conEstado(estadoOpcional(estado));
        return buscarCitas.ejecutar(criterios).stream().map(CitaRespuesta::desde).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CitaRespuesta agendar(@RequestBody AgendarCitaSolicitud solicitud) {
        return CitaRespuesta.desde(agendarCita.ejecutar(solicitud.aComando()));
    }

    @PostMapping("/{citaId}/confirmar")
    public CitaRespuesta confirmar(@PathVariable("citaId") String citaId) {
        return CitaRespuesta.desde(confirmarCita.ejecutar(citaId));
    }

    @PostMapping("/{citaId}/atender")
    public CitaRespuesta atender(@PathVariable("citaId") String citaId) {
        return CitaRespuesta.desde(atenderCita.ejecutar(citaId));
    }

    @PostMapping("/{citaId}/cancelar")
    public CitaRespuesta cancelar(@PathVariable("citaId") String citaId, @RequestBody CancelarCitaSolicitud solicitud) {
        return CitaRespuesta.desde(cancelarCita.ejecutar(solicitud.aComando(citaId)));
    }

    @PostMapping("/{citaId}/reprogramar")
    public CitaRespuesta reprogramar(@PathVariable("citaId") String citaId,
                                     @RequestBody ReprogramarCitaSolicitud solicitud) {
        return CitaRespuesta.desde(reprogramarCita.ejecutar(solicitud.aComando(citaId)));
    }

    private static String vacioComoNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor;
    }

    private static EstadoCita estadoOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return EstadoCita.valueOf(valor);
        } catch (IllegalArgumentException error) {
            throw new SolicitudInvalida("El estado '" + valor + "' no existe.");
        }
    }
}
