package com.clinica.citas.infraestructura.configuracion;

import com.clinica.citas.aplicacion.puertos.entrada.AgendarCitaCasoDeUso;
import com.clinica.citas.aplicacion.puertos.entrada.ListarMedicosCasoDeUso;
import com.clinica.citas.aplicacion.puertos.entrada.RegistrarMedicoCasoDeUso;
import com.clinica.citas.aplicacion.puertos.entrada.RegistrarPacienteCasoDeUso;
import com.clinica.citas.aplicacion.puertos.salida.Reloj;
import com.clinica.citas.dominio.modelo.HorarioDeAtencion;
import com.clinica.citas.dominio.modelo.Medico;
import com.clinica.citas.dominio.modelo.Paciente;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Carga médicos, pacientes y citas de demostración al arrancar, solo si la base está vacía.
 * Usa los mismos casos de uso que la API, así los datos respetan todas las reglas del negocio.
 * Se desactiva con {@code citas.datos-de-ejemplo=false}.
 */
@Component
@ConditionalOnProperty(name = "citas.datos-de-ejemplo", havingValue = "true")
public class CargaDeDatosDeEjemplo implements CommandLineRunner {

    private static final Logger REGISTRO = LoggerFactory.getLogger(CargaDeDatosDeEjemplo.class);

    private static final List<RegistrarMedicoCasoDeUso.Comando> MEDICOS = List.of(
            new RegistrarMedicoCasoDeUso.Comando("Dra. Lucía Fernández", "MEDICINA_GENERAL", "RM-10234", "lucia.fernandez@clinica.test"),
            new RegistrarMedicoCasoDeUso.Comando("Dr. Andrés Salazar", "CARDIOLOGIA", "RM-20871", "andres.salazar@clinica.test"),
            new RegistrarMedicoCasoDeUso.Comando("Dra. Camila Rojas", "PEDIATRIA", "RM-31560", "camila.rojas@clinica.test"),
            new RegistrarMedicoCasoDeUso.Comando("Dr. Mateo Vargas", "DERMATOLOGIA", "RM-44102", "mateo.vargas@clinica.test"));

    private static final List<RegistrarPacienteCasoDeUso.Comando> PACIENTES = List.of(
            new RegistrarPacienteCasoDeUso.Comando("Ana María Torres", "1023456789", "ana.torres@correo.test", "3001234567", LocalDate.of(1990, 4, 12)),
            new RegistrarPacienteCasoDeUso.Comando("Jorge Luis Ramírez", "80123456", "jorge.ramirez@correo.test", "3109876543", LocalDate.of(1978, 9, 3)),
            new RegistrarPacienteCasoDeUso.Comando("Valentina Gómez", "1098765432", "valentina.gomez@correo.test", null, LocalDate.of(2015, 1, 22)),
            new RegistrarPacienteCasoDeUso.Comando("Carlos Mejía", "79555123", "carlos.mejia@correo.test", "3205551234", LocalDate.of(1965, 11, 30)),
            new RegistrarPacienteCasoDeUso.Comando("Sofía Herrera", "1012345678", "sofia.herrera@correo.test", "3157778899", LocalDate.of(2001, 6, 8)));

    /** Cada cita indica la posición del paciente y del médico en las listas anteriores. */
    private record CitaDeEjemplo(int paciente, int medico, LocalTime hora, String motivo) {
    }

    private static final List<CitaDeEjemplo> CITAS = List.of(
            new CitaDeEjemplo(0, 0, LocalTime.of(8, 30), "Control anual y revisión de exámenes"),
            new CitaDeEjemplo(1, 1, LocalTime.of(9, 0), "Dolor en el pecho al hacer ejercicio"),
            new CitaDeEjemplo(2, 2, LocalTime.of(9, 0), "Control de crecimiento y vacunas"),
            new CitaDeEjemplo(3, 1, LocalTime.of(10, 30), "Seguimiento de hipertensión"),
            new CitaDeEjemplo(4, 3, LocalTime.of(11, 0), "Revisión de lunar en la espalda"),
            new CitaDeEjemplo(0, 3, LocalTime.of(14, 0), "Brote en las manos"),
            new CitaDeEjemplo(3, 0, LocalTime.of(15, 30), "Renovación de fórmula médica"));

    private final RegistrarMedicoCasoDeUso registrarMedico;
    private final RegistrarPacienteCasoDeUso registrarPaciente;
    private final ListarMedicosCasoDeUso listarMedicos;
    private final AgendarCitaCasoDeUso agendarCita;
    private final Reloj reloj;
    private final HorarioDeAtencion horario;

    public CargaDeDatosDeEjemplo(RegistrarMedicoCasoDeUso registrarMedico, RegistrarPacienteCasoDeUso registrarPaciente,
                                 ListarMedicosCasoDeUso listarMedicos, AgendarCitaCasoDeUso agendarCita,
                                 Reloj reloj, HorarioDeAtencion horario) {
        this.registrarMedico = registrarMedico;
        this.registrarPaciente = registrarPaciente;
        this.listarMedicos = listarMedicos;
        this.agendarCita = agendarCita;
        this.reloj = reloj;
        this.horario = horario;
    }

    @Override
    public void run(String... argumentos) {
        if (!listarMedicos.todos().isEmpty()) {
            REGISTRO.info("La base de datos ya tiene información; no se cargaron datos de ejemplo.");
            return;
        }
        List<Medico> medicos = MEDICOS.stream().map(registrarMedico::ejecutar).toList();
        List<Paciente> pacientes = PACIENTES.stream().map(registrarPaciente::ejecutar).toList();
        LocalDate dia = proximoDiaHabil(reloj.hoy());
        CITAS.forEach(cita -> agendarCita.ejecutar(new AgendarCitaCasoDeUso.Comando(
                pacientes.get(cita.paciente()).id(),
                medicos.get(cita.medico()).id(),
                dia.atTime(cita.hora()),
                cita.motivo())));
        REGISTRO.info("Se cargaron {} médicos, {} pacientes y {} citas para el {}.",
                medicos.size(), pacientes.size(), CITAS.size(), dia);
    }

    private LocalDate proximoDiaHabil(LocalDate hoy) {
        LocalDate dia = hoy.plusDays(1);
        while (!horario.esDiaHabil(dia)) {
            dia = dia.plusDays(1);
        }
        return dia;
    }
}
