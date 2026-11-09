package com.clinica.citas.infraestructura.web.dto;

import com.clinica.citas.aplicacion.puertos.entrada.RegistrarPacienteCasoDeUso;
import com.clinica.citas.infraestructura.web.CamposDeEntrada;

public record RegistrarPacienteSolicitud(
        String nombre, String documento, String email, String telefono, String fechaNacimiento) {

    public RegistrarPacienteCasoDeUso.Comando aComando() {
        return new RegistrarPacienteCasoDeUso.Comando(
                nombre, documento, email, telefono, CamposDeEntrada.fechaOpcional(fechaNacimiento, "fechaNacimiento"));
    }
}
