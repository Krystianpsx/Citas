package com.clinica.citas.dominio.excepciones;

/** Se intentó llevar una cita a un estado que no es alcanzable desde el actual. */
public class TransicionDeEstadoInvalida extends ReglaDeNegocioIncumplida {

    public TransicionDeEstadoInvalida(String mensaje) {
        super(mensaje);
    }
}
