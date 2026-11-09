package com.clinica.citas.aplicacion.servicios;

import com.clinica.citas.aplicacion.puertos.entrada.RegistrarPacienteCasoDeUso;
import com.clinica.citas.aplicacion.puertos.salida.Reloj;
import com.clinica.citas.aplicacion.puertos.salida.RepositorioDePacientes;
import com.clinica.citas.dominio.excepciones.EntidadDuplicada;
import com.clinica.citas.dominio.modelo.DocumentoIdentidad;
import com.clinica.citas.dominio.modelo.Email;
import com.clinica.citas.dominio.modelo.Paciente;

public class RegistrarPacienteServicio implements RegistrarPacienteCasoDeUso {

    private final RepositorioDePacientes pacientes;
    private final Reloj reloj;

    public RegistrarPacienteServicio(RepositorioDePacientes pacientes, Reloj reloj) {
        this.pacientes = pacientes;
        this.reloj = reloj;
    }

    @Override
    public Paciente ejecutar(Comando comando) {
        var documento = new DocumentoIdentidad(comando.documento());
        exigirDocumentoLibre(documento);
        Paciente paciente = Paciente.registrar(
                comando.nombre(),
                documento,
                new Email(comando.email()),
                comando.telefono(),
                comando.fechaNacimiento(),
                reloj.hoy());
        pacientes.guardar(paciente);
        return paciente;
    }

    private void exigirDocumentoLibre(DocumentoIdentidad documento) {
        if (pacientes.buscarPorDocumento(documento).isPresent()) {
            throw new EntidadDuplicada("Ya existe un paciente con el documento " + documento + ".");
        }
    }
}
