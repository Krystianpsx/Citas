package com.clinica.citas.infraestructura.web.dto;

import com.clinica.citas.aplicacion.puertos.entrada.RegistrarMedicoCasoDeUso;
import com.clinica.citas.infraestructura.web.CamposDeEntrada;

public record RegistrarMedicoSolicitud(String nombre, String especialidad, String registroProfesional, String email) {

    public RegistrarMedicoCasoDeUso.Comando aComando() {
        return new RegistrarMedicoCasoDeUso.Comando(
                nombre, CamposDeEntrada.requerido(especialidad, "especialidad"), registroProfesional, email);
    }
}
