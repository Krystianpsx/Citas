package com.clinica.citas.infraestructura.web;

/**
 * El cliente envió datos que no se pueden interpretar (campo ausente o fecha ilegible).
 * Es un error del adaptador web, no del dominio. Se responde con 400.
 */
public class SolicitudInvalida extends RuntimeException {

    public SolicitudInvalida(String mensaje) {
        super(mensaje);
    }
}
