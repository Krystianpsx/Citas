package com.clinica.citas.soporte;

import com.clinica.citas.aplicacion.puertos.salida.Reloj;
import java.time.LocalDateTime;

/** Doble de prueba: un reloj que marca siempre la hora que se le indique. */
public class RelojFijo implements Reloj {

    private LocalDateTime momento;

    public RelojFijo(LocalDateTime momento) {
        this.momento = momento;
    }

    public void ajustarA(LocalDateTime nuevoMomento) {
        this.momento = nuevoMomento;
    }

    @Override
    public LocalDateTime ahora() {
        return momento;
    }
}
