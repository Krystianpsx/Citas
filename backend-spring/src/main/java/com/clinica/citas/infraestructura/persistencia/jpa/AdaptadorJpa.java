package com.clinica.citas.infraestructura.persistencia.jpa;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Operaciones comunes de los tres adaptadores JPA. Cada adaptador concreto solo declara
 * sus consultas propias (refactorización <em>Extract Superclass</em>).
 */
abstract class AdaptadorJpa<E, F, R extends JpaRepository<F, String>> {

    protected final R datos;
    private final Function<E, F> convertirAFila;
    private final Function<F, E> convertirADominio;

    protected AdaptadorJpa(R datos, Function<E, F> convertirAFila, Function<F, E> convertirADominio) {
        this.datos = datos;
        this.convertirAFila = convertirAFila;
        this.convertirADominio = convertirADominio;
    }

    public void guardar(E entidad) {
        datos.save(convertirAFila.apply(entidad));
    }

    protected Optional<E> porId(String id) {
        return Optional.ofNullable(id).flatMap(datos::findById).map(convertirADominio);
    }

    protected Optional<E> aDominio(Optional<F> fila) {
        return fila.map(convertirADominio);
    }

    protected List<E> aDominio(List<F> filas) {
        return filas.stream().map(convertirADominio).toList();
    }
}
