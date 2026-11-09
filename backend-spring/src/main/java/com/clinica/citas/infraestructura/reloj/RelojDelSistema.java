package com.clinica.citas.infraestructura.reloj;

import com.clinica.citas.aplicacion.puertos.salida.Reloj;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/** Hora real en la zona horaria de la clínica (configurable para servidores en UTC). */
public class RelojDelSistema implements Reloj {

    private final ZoneId zonaDeLaClinica;

    public RelojDelSistema(ZoneId zonaDeLaClinica) {
        this.zonaDeLaClinica = zonaDeLaClinica;
    }

    @Override
    public LocalDateTime ahora() {
        return LocalDateTime.now(zonaDeLaClinica).truncatedTo(ChronoUnit.SECONDS);
    }
}
