package com.clinica.citas.infraestructura.configuracion;

import com.clinica.citas.aplicacion.puertos.entrada.AgendarCitaCasoDeUso;
import com.clinica.citas.aplicacion.puertos.entrada.AtenderCitaCasoDeUso;
import com.clinica.citas.aplicacion.puertos.entrada.BuscarCitasCasoDeUso;
import com.clinica.citas.aplicacion.puertos.entrada.CancelarCitaCasoDeUso;
import com.clinica.citas.aplicacion.puertos.entrada.ConfirmarCitaCasoDeUso;
import com.clinica.citas.aplicacion.puertos.entrada.ConsultarDisponibilidadCasoDeUso;
import com.clinica.citas.aplicacion.puertos.entrada.ListarMedicosCasoDeUso;
import com.clinica.citas.aplicacion.puertos.entrada.ListarPacientesCasoDeUso;
import com.clinica.citas.aplicacion.puertos.entrada.RegistrarMedicoCasoDeUso;
import com.clinica.citas.aplicacion.puertos.entrada.RegistrarPacienteCasoDeUso;
import com.clinica.citas.aplicacion.puertos.entrada.ReprogramarCitaCasoDeUso;
import com.clinica.citas.aplicacion.puertos.salida.Notificador;
import com.clinica.citas.aplicacion.puertos.salida.Reloj;
import com.clinica.citas.aplicacion.puertos.salida.RepositorioDeCitas;
import com.clinica.citas.aplicacion.puertos.salida.RepositorioDeMedicos;
import com.clinica.citas.aplicacion.puertos.salida.RepositorioDePacientes;
import com.clinica.citas.aplicacion.servicios.AgendarCitaServicio;
import com.clinica.citas.aplicacion.servicios.AtenderCitaServicio;
import com.clinica.citas.aplicacion.servicios.BuscarCitasServicio;
import com.clinica.citas.aplicacion.servicios.CancelarCitaServicio;
import com.clinica.citas.aplicacion.servicios.ConfirmarCitaServicio;
import com.clinica.citas.aplicacion.servicios.ConsultarDisponibilidadServicio;
import com.clinica.citas.aplicacion.servicios.DetalladorDeCitas;
import com.clinica.citas.aplicacion.servicios.ListarMedicosServicio;
import com.clinica.citas.aplicacion.servicios.ListarPacientesServicio;
import com.clinica.citas.aplicacion.servicios.RegistrarMedicoServicio;
import com.clinica.citas.aplicacion.servicios.RegistrarPacienteServicio;
import com.clinica.citas.aplicacion.servicios.ReprogramarCitaServicio;
import com.clinica.citas.aplicacion.servicios.VerificadorDeAgenda;
import com.clinica.citas.dominio.modelo.HorarioDeAtencion;
import com.clinica.citas.dominio.reglas.ReglasDeAgendamiento;
import com.clinica.citas.dominio.reglas.ValidadorDeAgendamiento;

/**
 * Raíz de composición en Java puro: el único lugar que une cada puerto con su implementación.
 *
 * <p>No usa Spring a propósito. La configuración de Spring la llama para publicar los
 * casos de uso como beans, y las pruebas la llaman directamente con repositorios en
 * memoria y un reloj fijo. Así el ensamblaje existe una sola vez.
 */
public record CasosDeUso(
        RegistrarPacienteCasoDeUso registrarPaciente,
        ListarPacientesCasoDeUso listarPacientes,
        RegistrarMedicoCasoDeUso registrarMedico,
        ListarMedicosCasoDeUso listarMedicos,
        ConsultarDisponibilidadCasoDeUso consultarDisponibilidad,
        AgendarCitaCasoDeUso agendarCita,
        BuscarCitasCasoDeUso buscarCitas,
        ConfirmarCitaCasoDeUso confirmarCita,
        AtenderCitaCasoDeUso atenderCita,
        CancelarCitaCasoDeUso cancelarCita,
        ReprogramarCitaCasoDeUso reprogramarCita) {

    /** Adaptadores de salida que necesita el núcleo. */
    public record Dependencias(
            RepositorioDePacientes pacientes,
            RepositorioDeMedicos medicos,
            RepositorioDeCitas citas,
            Reloj reloj,
            Notificador notificador,
            HorarioDeAtencion horario) {
    }

    public static CasosDeUso ensamblar(Dependencias dependencias) {
        var pacientes = dependencias.pacientes();
        var medicos = dependencias.medicos();
        var citas = dependencias.citas();
        var reloj = dependencias.reloj();
        var notificador = dependencias.notificador();
        var horario = dependencias.horario();

        var detallador = new DetalladorDeCitas(pacientes, medicos);
        var validador = new ValidadorDeAgendamiento(ReglasDeAgendamiento.estandar(horario));
        var verificador = new VerificadorDeAgenda(citas, validador, reloj);

        return new CasosDeUso(
                new RegistrarPacienteServicio(pacientes, reloj),
                new ListarPacientesServicio(pacientes),
                new RegistrarMedicoServicio(medicos),
                new ListarMedicosServicio(medicos),
                new ConsultarDisponibilidadServicio(medicos, citas, horario, reloj),
                new AgendarCitaServicio(citas, detallador, verificador, horario, notificador),
                new BuscarCitasServicio(citas, detallador),
                new ConfirmarCitaServicio(citas, detallador),
                new AtenderCitaServicio(citas, detallador, reloj),
                new CancelarCitaServicio(citas, detallador, reloj, notificador),
                new ReprogramarCitaServicio(citas, detallador, verificador, horario, reloj, notificador));
    }
}
