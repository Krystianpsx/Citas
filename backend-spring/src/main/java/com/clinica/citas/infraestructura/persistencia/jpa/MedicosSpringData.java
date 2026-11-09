package com.clinica.citas.infraestructura.persistencia.jpa;

import com.clinica.citas.dominio.modelo.Especialidad;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicosSpringData extends JpaRepository<MedicoJpa, String> {

    Optional<MedicoJpa> findByRegistroProfesional(String registroProfesional);

    List<MedicoJpa> findAllByOrderByNombreAsc();

    List<MedicoJpa> findByEspecialidadOrderByNombreAsc(Especialidad especialidad);
}
