package com.clinica.citas.aplicacion.servicios;

import com.clinica.citas.aplicacion.puertos.entrada.RegistrarMedicoCasoDeUso;
import com.clinica.citas.aplicacion.puertos.salida.RepositorioDeMedicos;
import com.clinica.citas.dominio.excepciones.EntidadDuplicada;
import com.clinica.citas.dominio.modelo.Email;
import com.clinica.citas.dominio.modelo.Especialidad;
import com.clinica.citas.dominio.modelo.Medico;

public class RegistrarMedicoServicio implements RegistrarMedicoCasoDeUso {

    private final RepositorioDeMedicos medicos;

    public RegistrarMedicoServicio(RepositorioDeMedicos medicos) {
        this.medicos = medicos;
    }

    @Override
    public Medico ejecutar(Comando comando) {
        Medico medico = Medico.registrar(
                comando.nombre(),
                Especialidad.desdeCodigo(comando.especialidad()),
                comando.registroProfesional(),
                new Email(comando.email()));
        exigirRegistroLibre(medico.registroProfesional());
        medicos.guardar(medico);
        return medico;
    }

    private void exigirRegistroLibre(String registro) {
        if (medicos.buscarPorRegistro(registro).isPresent()) {
            throw new EntidadDuplicada("Ya existe un médico con el registro profesional " + registro + ".");
        }
    }
}
