package com.clinica.citas.dominio.excepciones;

/** Un valor no cumple el formato o las restricciones esperadas. */
public class DatoInvalido extends ErrorDeDominio {

    public DatoInvalido(String mensaje) {
        super(mensaje);
    }
}
