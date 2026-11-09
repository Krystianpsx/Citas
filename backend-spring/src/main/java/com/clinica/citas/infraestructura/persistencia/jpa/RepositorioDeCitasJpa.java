package com.clinica.citas.infraestructura.persistencia.jpa;

import com.clinica.citas.aplicacion.consultas.CriteriosDeBusquedaDeCitas;
import com.clinica.citas.aplicacion.puertos.salida.RepositorioDeCitas;
import com.clinica.citas.dominio.modelo.Cita;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

public class RepositorioDeCitasJpa extends AdaptadorJpa<Cita, CitaJpa, CitasSpringData> implements RepositorioDeCitas {

    private static final Sort POR_HORA_DE_INICIO = Sort.by("inicio");

    public RepositorioDeCitasJpa(CitasSpringData datos) {
        super(datos, MapeadorJpa::aFila, MapeadorJpa::aDominio);
    }

    @Override
    public Optional<Cita> buscarPorId(String citaId) {
        return porId(citaId);
    }

    @Override
    public List<Cita> buscar(CriteriosDeBusquedaDeCitas criterios) {
        return aDominio(datos.findAll(segun(criterios), POR_HORA_DE_INICIO));
    }

    /** Traduce cada criterio presente a una condición SQL; los ausentes no filtran. */
    private static Specification<CitaJpa> segun(CriteriosDeBusquedaDeCitas criterios) {
        return (raiz, consulta, constructor) ->
                constructor.and(condiciones(criterios, raiz, constructor).toArray(Predicate[]::new));
    }

    private static List<Predicate> condiciones(CriteriosDeBusquedaDeCitas criterios, Root<CitaJpa> raiz,
                                               CriteriaBuilder constructor) {
        List<Predicate> condiciones = new ArrayList<>();
        if (criterios.dia() != null) {
            LocalDateTime comienzoDelDia = criterios.dia().atStartOfDay();
            condiciones.add(constructor.greaterThanOrEqualTo(raiz.<LocalDateTime>get("inicio"), comienzoDelDia));
            condiciones.add(constructor.lessThan(raiz.<LocalDateTime>get("inicio"), comienzoDelDia.plusDays(1)));
        }
        if (criterios.medicoId() != null) {
            condiciones.add(constructor.equal(raiz.get("medicoId"), criterios.medicoId()));
        }
        if (criterios.pacienteId() != null) {
            condiciones.add(constructor.equal(raiz.get("pacienteId"), criterios.pacienteId()));
        }
        if (criterios.estado() != null) {
            condiciones.add(constructor.equal(raiz.get("estado"), criterios.estado()));
        }
        return condiciones;
    }
}
