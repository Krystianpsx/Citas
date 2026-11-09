package com.clinica.citas.dominio.excepciones;

/**
 * Raíz de los errores que nacen en el núcleo. Cada subclase nombra una situación
 * del negocio; la capa web las traduce a códigos HTTP sin que el dominio sepa que HTTP existe.
 */
public abstract class ErrorDeDominio extends RuntimeException {

    protected ErrorDeDominio(String mensaje) {
        super(mensaje);
    }
}
