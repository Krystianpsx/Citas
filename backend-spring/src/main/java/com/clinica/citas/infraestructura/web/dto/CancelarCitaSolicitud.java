package com.clinica.citas.infraestructura.web.dto;

import com.clinica.citas.aplicacion.puertos.entrada.CancelarCitaCasoDeUso;

public record CancelarCitaSolicitud(String motivo) {

    public CancelarCitaCasoDeUso.Comando aComando(String citaId) {
        return new CancelarCitaCasoDeUso.Comando(citaId, motivo);
    }
}
