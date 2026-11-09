package com.clinica.citas.infraestructura.web.dto;

import com.clinica.citas.aplicacion.puertos.entrada.AgendarCitaCasoDeUso;
import com.clinica.citas.infraestructura.web.CamposDeEntrada;

public record AgendarCitaSolicitud(String pacienteId, String medicoId, String inicio, String motivo) {

    public AgendarCitaCasoDeUso.Comando aComando() {
        return new AgendarCitaCasoDeUso.Comando(
                CamposDeEntrada.requerido(pacienteId, "pacienteId"),
                CamposDeEntrada.requerido(medicoId, "medicoId"),
                CamposDeEntrada.fechaYHora(inicio, "inicio"),
                motivo);
    }
}
