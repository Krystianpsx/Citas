package com.clinica.citas.dominio.excepciones;

/** No existe la entidad solicitada. */
public class EntidadNoEncontrada extends ErrorDeDominio {

    public EntidadNoEncontrada(String mensaje) {
        super(mensaje);
    }

    /** Ejemplo: {@code conId("el paciente", "abc")} produce "No existe el paciente con id abc." */
    public static EntidadNoEncontrada conId(String descripcion, String id) {
        return new EntidadNoEncontrada("No existe " + descripcion + " con id " + id + ".");
    }
}
