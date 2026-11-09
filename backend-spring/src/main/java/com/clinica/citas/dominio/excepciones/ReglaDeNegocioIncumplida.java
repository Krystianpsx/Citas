package com.clinica.citas.dominio.excepciones;

/** La operación está bien formada, pero el negocio no la permite. */
public class ReglaDeNegocioIncumplida extends ErrorDeDominio {

    public ReglaDeNegocioIncumplida(String mensaje) {
        super(mensaje);
    }
}
