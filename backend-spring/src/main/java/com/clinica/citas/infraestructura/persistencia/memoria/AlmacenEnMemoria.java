package com.clinica.citas.infraestructura.persistencia.memoria;

import com.clinica.citas.dominio.modelo.Entidad;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Almacén genérico que comparten los tres repositorios en memoria (refactorización
 * <em>Extract Superclass</em>). Guarda y entrega copias para comportarse como una base de
 * datos real: si un caso de uso modifica una entidad y luego falla, lo guardado no cambia.
 */
final class AlmacenEnMemoria<E extends Entidad> {

    private final Map<String, E> entidades = new ConcurrentHashMap<>();
    private final UnaryOperator<E> copiar;
    private final Comparator<E> orden;

    AlmacenEnMemoria(UnaryOperator<E> copiar, Comparator<E> orden) {
        this.copiar = copiar;
        this.orden = orden;
    }

    void guardar(E entidad) {
        entidades.put(entidad.id(), copiar.apply(entidad));
    }

    Optional<E> obtener(String id) {
        return Optional.ofNullable(id).map(entidades::get).map(copiar);
    }

    Optional<E> primeraQueCumpla(Predicate<E> condicion) {
        return todasLasQueCumplan(condicion).stream().findFirst();
    }

    List<E> todasLasQueCumplan(Predicate<E> condicion) {
        return entidades.values().stream().filter(condicion).sorted(orden).map(copiar).toList();
    }
}
