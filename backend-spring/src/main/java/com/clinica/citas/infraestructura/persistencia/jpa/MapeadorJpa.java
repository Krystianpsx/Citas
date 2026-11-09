package com.clinica.citas.infraestructura.persistencia.jpa;

import com.clinica.citas.dominio.modelo.Cita;
import com.clinica.citas.dominio.modelo.DocumentoIdentidad;
import com.clinica.citas.dominio.modelo.Email;
import com.clinica.citas.dominio.modelo.IntervaloDeTiempo;
import com.clinica.citas.dominio.modelo.Medico;
import com.clinica.citas.dominio.modelo.Paciente;

/** Traducción entre filas de la base de datos y entidades del dominio. Solo hace eso. */
final class MapeadorJpa {

    private MapeadorJpa() {
    }

    static PacienteJpa aFila(Paciente paciente) {
        return new PacienteJpa(
                paciente.id(),
                paciente.nombre(),
                paciente.documento().numero(),
                paciente.email().valor(),
                paciente.telefono().orElse(null),
                paciente.fechaNacimiento().orElse(null));
    }

    static Paciente aDominio(PacienteJpa fila) {
        return new Paciente(
                fila.getId(),
                fila.getNombre(),
                new DocumentoIdentidad(fila.getDocumento()),
                new Email(fila.getEmail()),
                fila.getTelefono(),
                fila.getFechaNacimiento());
    }

    static MedicoJpa aFila(Medico medico) {
        return new MedicoJpa(
                medico.id(),
                medico.nombre(),
                medico.especialidad(),
                medico.registroProfesional(),
                medico.email().valor());
    }

    static Medico aDominio(MedicoJpa fila) {
        return new Medico(
                fila.getId(),
                fila.getNombre(),
                fila.getEspecialidad(),
                fila.getRegistroProfesional(),
                new Email(fila.getEmail()));
    }

    static CitaJpa aFila(Cita cita) {
        return new CitaJpa(
                cita.id(),
                cita.pacienteId(),
                cita.medicoId(),
                cita.inicio(),
                cita.fin(),
                cita.motivo(),
                cita.estado(),
                cita.motivoCancelacion().orElse(null));
    }

    static Cita aDominio(CitaJpa fila) {
        return new Cita(
                fila.getId(),
                fila.getPacienteId(),
                fila.getMedicoId(),
                new IntervaloDeTiempo(fila.getInicio(), fila.getFin()),
                fila.getMotivo(),
                fila.getEstado(),
                fila.getMotivoCancelacion());
    }
}
