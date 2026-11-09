package com.clinica.citas.dominio.modelo;

import java.util.Objects;
import java.util.UUID;

/**
 * Objeto con identidad propia: dos entidades son la misma si comparten identificador,
 * aunque cambien sus datos. El identificador se genera en el dominio (UUID) y no en la
 * base de datos, para que el núcleo no dependa de cómo se persiste.
 */
public abstract class Entidad {

    private final String id;

    protected Entidad(String id) {
        this.id = Objects.requireNonNull(id, "El identificador es obligatorio");
    }

    protected static String nuevoIdentificador() {
        return UUID.randomUUID().toString();
    }

    public String id() {
        return id;
    }

    @Override
    public final boolean equals(Object otra) {
        return otra instanceof Entidad entidad && getClass() == entidad.getClass() && id.equals(entidad.id);
    }

    @Override
    public final int hashCode() {
        return id.hashCode();
    }
}
