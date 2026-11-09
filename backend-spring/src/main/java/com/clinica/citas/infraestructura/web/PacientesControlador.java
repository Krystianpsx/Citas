package com.clinica.citas.infraestructura.web;

import com.clinica.citas.aplicacion.puertos.entrada.ListarPacientesCasoDeUso;
import com.clinica.citas.aplicacion.puertos.entrada.RegistrarPacienteCasoDeUso;
import com.clinica.citas.infraestructura.web.dto.PacienteRespuesta;
import com.clinica.citas.infraestructura.web.dto.RegistrarPacienteSolicitud;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador primario HTTP. Solo traduce JSON a comandos y resultados a JSON;
 * no contiene reglas de negocio. Depende de los puertos, no de los servicios.
 */
@RestController
@RequestMapping("/api/pacientes")
public class PacientesControlador {

    private final RegistrarPacienteCasoDeUso registrarPaciente;
    private final ListarPacientesCasoDeUso listarPacientes;

    public PacientesControlador(RegistrarPacienteCasoDeUso registrarPaciente, ListarPacientesCasoDeUso listarPacientes) {
        this.registrarPaciente = registrarPaciente;
        this.listarPacientes = listarPacientes;
    }

    @GetMapping
    public List<PacienteRespuesta> listar() {
        return listarPacientes.ejecutar().stream().map(PacienteRespuesta::desde).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PacienteRespuesta registrar(@RequestBody RegistrarPacienteSolicitud solicitud) {
        return PacienteRespuesta.desde(registrarPaciente.ejecutar(solicitud.aComando()));
    }
}
