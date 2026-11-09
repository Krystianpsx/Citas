package com.clinica.citas.infraestructura.web;

import com.clinica.citas.aplicacion.puertos.entrada.ConsultarDisponibilidadCasoDeUso;
import com.clinica.citas.aplicacion.puertos.entrada.ListarMedicosCasoDeUso;
import com.clinica.citas.aplicacion.puertos.entrada.RegistrarMedicoCasoDeUso;
import com.clinica.citas.dominio.modelo.Especialidad;
import com.clinica.citas.dominio.modelo.Medico;
import com.clinica.citas.infraestructura.web.dto.EspecialidadRespuesta;
import com.clinica.citas.infraestructura.web.dto.MedicoRespuesta;
import com.clinica.citas.infraestructura.web.dto.RegistrarMedicoSolicitud;
import java.util.Arrays;
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
@RequestMapping("/api/medicos")
public class MedicosControlador {

    private final RegistrarMedicoCasoDeUso registrarMedico;
    private final ListarMedicosCasoDeUso listarMedicos;
    private final ConsultarDisponibilidadCasoDeUso consultarDisponibilidad;

    public MedicosControlador(RegistrarMedicoCasoDeUso registrarMedico, ListarMedicosCasoDeUso listarMedicos,
                              ConsultarDisponibilidadCasoDeUso consultarDisponibilidad) {
        this.registrarMedico = registrarMedico;
        this.listarMedicos = listarMedicos;
        this.consultarDisponibilidad = consultarDisponibilidad;
    }

    @GetMapping
    public List<MedicoRespuesta> listar(@RequestParam(name = "especialidad", required = false) String especialidad) {
        List<Medico> medicos = especialidad == null || especialidad.isBlank()
                ? listarMedicos.todos()
                : listarMedicos.deLaEspecialidad(especialidad);
        return medicos.stream().map(MedicoRespuesta::desde).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MedicoRespuesta registrar(@RequestBody RegistrarMedicoSolicitud solicitud) {
        return MedicoRespuesta.desde(registrarMedico.ejecutar(solicitud.aComando()));
    }

    @GetMapping("/especialidades")
    public List<EspecialidadRespuesta> especialidades() {
        return Arrays.stream(Especialidad.values()).map(EspecialidadRespuesta::desde).toList();
    }

    @GetMapping("/{medicoId}/disponibilidad")
    public List<String> disponibilidad(@PathVariable("medicoId") String medicoId,
                                       @RequestParam(name = "fecha", required = false) String fecha) {
        var comando = new ConsultarDisponibilidadCasoDeUso.Comando(medicoId, CamposDeEntrada.fecha(fecha, "fecha"));
        return consultarDisponibilidad.ejecutar(comando).stream().map(FechasEnJson::texto).toList();
    }
}
