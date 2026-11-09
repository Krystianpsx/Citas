package com.clinica.citas.infraestructura.persistencia.jpa;

import com.clinica.citas.dominio.modelo.EstadoCita;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Tabla de citas. Referencia a paciente y médico por su identificador, igual que el
 * agregado del dominio: cada agregado se carga y guarda por separado.
 */
@Entity
@Table(name = "citas", indexes = {
        @Index(name = "ix_citas_medico_inicio", columnList = "medico_id, inicio"),
        @Index(name = "ix_citas_paciente_inicio", columnList = "paciente_id, inicio")})
public class CitaJpa {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "paciente_id", nullable = false, length = 36)
    private String pacienteId;

    @Column(name = "medico_id", nullable = false, length = 36)
    private String medicoId;

    @Column(name = "inicio", nullable = false)
    private LocalDateTime inicio;

    @Column(nullable = false)
    private LocalDateTime fin;

    @Column(nullable = false, length = 250)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCita estado;

    @Column(length = 250)
    private String motivoCancelacion;

    protected CitaJpa() {
        // requerido por JPA
    }

    CitaJpa(String id, String pacienteId, String medicoId, LocalDateTime inicio, LocalDateTime fin,
            String motivo, EstadoCita estado, String motivoCancelacion) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.medicoId = medicoId;
        this.inicio = inicio;
        this.fin = fin;
        this.motivo = motivo;
        this.estado = estado;
        this.motivoCancelacion = motivoCancelacion;
    }

    String getId() {
        return id;
    }

    String getPacienteId() {
        return pacienteId;
    }

    String getMedicoId() {
        return medicoId;
    }

    LocalDateTime getInicio() {
        return inicio;
    }

    LocalDateTime getFin() {
        return fin;
    }

    String getMotivo() {
        return motivo;
    }

    EstadoCita getEstado() {
        return estado;
    }

    String getMotivoCancelacion() {
        return motivoCancelacion;
    }
}
