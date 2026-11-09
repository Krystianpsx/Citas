package com.clinica.citas.infraestructura.persistencia.jpa;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Consultas que Spring Data genera a partir del nombre del método. */
public interface PacientesSpringData extends JpaRepository<PacienteJpa, String> {

    Optional<PacienteJpa> findByDocumento(String documento);

    List<PacienteJpa> findAllByOrderByNombreAsc();
}
