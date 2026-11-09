package com.clinica.citas.aplicacion.servicios;

import com.clinica.citas.aplicacion.consultas.CitaDetallada;
import com.clinica.citas.aplicacion.puertos.salida.RepositorioDeMedicos;
import com.clinica.citas.aplicacion.puertos.salida.RepositorioDePacientes;
import com.clinica.citas.dominio.excepciones.EntidadNoEncontrada;
import com.clinica.citas.dominio.modelo.Cita;
import com.clinica.citas.dominio.modelo.Medico;
import com.clinica.citas.dominio.modelo.Paciente;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Une cada cita con su paciente y su médico. Consulta cada persona una sola vez
 * aunque aparezca en varias citas del listado.
 */
public class DetalladorDeCitas {

    private final RepositorioDePacientes pacientes;
    private final RepositorioDeMedicos medicos;

    public DetalladorDeCitas(RepositorioDePacientes pacientes, RepositorioDeMedicos medicos) {
        this.pacientes = pacientes;
        this.medicos = medicos;
    }

    /** Además de detallar, confirma que el paciente y el médico existen. */
    public CitaDetallada detallar(Cita cita) {
        return detallarVarias(List.of(cita)).get(0);
    }

    public List<CitaDetallada> detallarVarias(List<Cita> citas) {
        Map<String, Paciente> pacientesYaBuscados = new HashMap<>();
        Map<String, Medico> medicosYaBuscados = new HashMap<>();
        return citas.stream()
                .map(cita -> new CitaDetallada(
                        cita,
                        pacientesYaBuscados.computeIfAbsent(cita.pacienteId(), this::paciente),
                        medicosYaBuscados.computeIfAbsent(cita.medicoId(), this::medico)))
                .toList();
    }

    private Paciente paciente(String id) {
        return pacientes.buscarPorId(id).orElseThrow(() -> EntidadNoEncontrada.conId("el paciente", id));
    }

    private Medico medico(String id) {
        return medicos.buscarPorId(id).orElseThrow(() -> EntidadNoEncontrada.conId("el médico", id));
    }
}
