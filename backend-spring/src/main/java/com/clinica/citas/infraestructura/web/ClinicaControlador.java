package com.clinica.citas.infraestructura.web;

import com.clinica.citas.dominio.modelo.HorarioDeAtencion;
import com.clinica.citas.infraestructura.web.dto.HorarioRespuesta;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Información general de la clínica. El frontend dibuja la agenda con el horario que
 * entrega este controlador, así el horario está definido en un solo lugar (el dominio).
 */
@RestController
@RequestMapping("/api")
public class ClinicaControlador {

    private final HorarioDeAtencion horario;

    public ClinicaControlador(HorarioDeAtencion horario) {
        this.horario = horario;
    }

    @GetMapping("/horario")
    public HorarioRespuesta horario() {
        return HorarioRespuesta.desde(horario);
    }

    @GetMapping("/salud")
    public Map<String, String> salud() {
        return Map.of("estado", "ok");
    }
}
