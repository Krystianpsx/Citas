package com.clinica.citas.dominio.excepciones;

/** Ya existe una entidad con el mismo identificador natural (documento, registro profesional). */
public class EntidadDuplicada extends ErrorDeDominio {

    public EntidadDuplicada(String mensaje) {
        super(mensaje);
    }
}
