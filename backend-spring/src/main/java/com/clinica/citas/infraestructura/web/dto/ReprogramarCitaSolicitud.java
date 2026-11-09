package com.clinica.citas.infraestructura.web.dto;

import com.clinica.citas.aplicacion.puertos.entrada.ReprogramarCitaCasoDeUso;
import com.clinica.citas.infraestructura.web.CamposDeEntrada;

public record ReprogramarCitaSolicitud(String inicio) {

    public ReprogramarCitaCasoDeUso.Comando aComando(String citaId) {
        return new ReprogramarCitaCasoDeUso.Comando(citaId, CamposDeEntrada.fechaYHora(inicio, "inicio"));
    }
}
