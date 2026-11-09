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
import com.clinica.citas.dominio.modelo.HorarioDeAtencion;
import com.clinica.citas.infraestructura.notificaciones.NotificadorPorRegistro;
import com.clinica.citas.infraestructura.reloj.RelojDelSistema;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Publica el núcleo en Spring. Las clases del dominio y de la aplicación no llevan
 * anotaciones (@Service, @Component): no saben que Spring existe. Aquí se crean y se
 * conectan (Inversión de Dependencias).
 *
 * <p>Cada caso de uso se publica como bean de su puerto de entrada, así cada controlador
 * recibe solo la interfaz que usa (Segregación de Interfaces).
 */
@Configuration
public class ConfiguracionDelNucleo {

    @Bean
    HorarioDeAtencion horarioDeAtencion() {
        return HorarioDeAtencion.deLunesAViernes();
    }

    @Bean
    Reloj reloj(@Value("${citas.zona-horaria:}") String zonaHoraria) {
        return new RelojDelSistema(zonaHoraria.isBlank() ? ZoneId.systemDefault() : ZoneId.of(zonaHoraria));
    }

    @Bean
    Notificador notificador() {
        return new NotificadorPorRegistro();
    }

    @Bean
    CasosDeUso casosDeUso(RepositorioDePacientes pacientes, RepositorioDeMedicos medicos, RepositorioDeCitas citas,
                          Reloj reloj, Notificador notificador, HorarioDeAtencion horario) {
        return CasosDeUso.ensamblar(new CasosDeUso.Dependencias(pacientes, medicos, citas, reloj, notificador, horario));
    }

    @Bean
    RegistrarPacienteCasoDeUso registrarPaciente(CasosDeUso casos) {
        return casos.registrarPaciente();
    }

    @Bean
    ListarPacientesCasoDeUso listarPacientes(CasosDeUso casos) {
        return casos.listarPacientes();
    }

    @Bean
    RegistrarMedicoCasoDeUso registrarMedico(CasosDeUso casos) {
        return casos.registrarMedico();
    }

    @Bean
    ListarMedicosCasoDeUso listarMedicos(CasosDeUso casos) {
        return casos.listarMedicos();
    }

    @Bean
    ConsultarDisponibilidadCasoDeUso consultarDisponibilidad(CasosDeUso casos) {
        return casos.consultarDisponibilidad();
    }

    @Bean
    AgendarCitaCasoDeUso agendarCita(CasosDeUso casos) {
        return casos.agendarCita();
    }

    @Bean
    BuscarCitasCasoDeUso buscarCitas(CasosDeUso casos) {
        return casos.buscarCitas();
    }

    @Bean
    ConfirmarCitaCasoDeUso confirmarCita(CasosDeUso casos) {
        return casos.confirmarCita();
    }

    @Bean
    AtenderCitaCasoDeUso atenderCita(CasosDeUso casos) {
        return casos.atenderCita();
    }

    @Bean
    CancelarCitaCasoDeUso cancelarCita(CasosDeUso casos) {
        return casos.cancelarCita();
    }

    @Bean
    ReprogramarCitaCasoDeUso reprogramarCita(CasosDeUso casos) {
        return casos.reprogramarCita();
    }
}
