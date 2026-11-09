import type { FormEvent } from "react";
import { Link } from "react-router-dom";
import { aFechaIso, fechaDe } from "../../dominio/fechas";
import type { Cita } from "../../dominio/modelos";
import { AvisoDeError, Cargando } from "../componentes/Avisos";
import { Campo } from "../componentes/Campo";
import { SelectorDeHora } from "../componentes/SelectorDeHora";
import { usePuertos } from "../contexto/ContextoDePuertos";
import { useAccion } from "../hooks/useAccion";
import { useCampos } from "../hooks/useCampos";
import { useConsulta } from "../hooks/useConsulta";

interface Props {
  medicoId?: string;
  inicio?: string;
  fechaSugerida: string;
  alAgendar: (cita: Cita) => void;
}

export function FormularioDeCita({ medicoId = "", inicio = "", fechaSugerida, alAgendar }: Props) {
  const { pacientes, medicos, citas } = usePuertos();
  const listaDePacientes = useConsulta(() => pacientes.listar(), []);
  const listaDeMedicos = useConsulta(() => medicos.listar(), []);
  const { valores, cambiar, enlazar } = useCampos({
    pacienteId: "",
    medicoId,
    fecha: inicio ? fechaDe(inicio) : fechaSugerida,
    inicio,
    motivo: "",
  });
  const agendar = useAccion(citas.agendar);

  if (listaDePacientes.cargando || listaDeMedicos.cargando) return <Cargando />;
  const errorDeCarga = listaDePacientes.error ?? listaDeMedicos.error;
  if (errorDeCarga) return <AvisoDeError mensaje={errorDeCarga} />;
  if (!listaDePacientes.datos?.length) {
    return (
      <p className="panel__nota">
        Para agendar necesitas al menos un paciente. <Link to="/pacientes">Registrar paciente</Link>
      </p>
    );
  }

  const enviar = async (evento: FormEvent) => {
    evento.preventDefault();
    const cita = await agendar.ejecutar({
      pacienteId: valores.pacienteId,
      medicoId: valores.medicoId,
      inicio: valores.inicio,
      motivo: valores.motivo,
    });
    if (cita) alAgendar(cita);
  };

  return (
    <form className="formulario" onSubmit={enviar}>
      <Campo etiqueta="Paciente">
        <select {...enlazar("pacienteId")} required>
          <option value="">Elige un paciente</option>
          {listaDePacientes.datos.map((paciente) => (
            <option key={paciente.id} value={paciente.id}>
              {paciente.nombre} ({paciente.documento})
            </option>
          ))}
        </select>
      </Campo>

      <Campo etiqueta="Médico">
        <select
          {...enlazar("medicoId")}
          onChange={(e) => {
            cambiar("medicoId", e.target.value);
            cambiar("inicio", "");
          }}
          required
        >
          <option value="">Elige un médico</option>
          {listaDeMedicos.datos?.map((medico) => (
            <option key={medico.id} value={medico.id}>
              {medico.nombre}, {medico.especialidad.nombre.toLowerCase()}
            </option>
          ))}
        </select>
      </Campo>

      <Campo etiqueta="Fecha">
        <input
          type="date"
          {...enlazar("fecha")}
          min={aFechaIso(new Date())}
          onChange={(e) => {
            cambiar("fecha", e.target.value);
            cambiar("inicio", "");
          }}
          required
        />
      </Campo>

      <fieldset className="campo">
        <legend className="campo__etiqueta">Hora</legend>
        <SelectorDeHora
          medicoId={valores.medicoId}
          fechaIso={valores.fecha}
          valor={valores.inicio}
          alCambiar={(nuevoInicio) => cambiar("inicio", nuevoInicio)}
        />
      </fieldset>

      <Campo etiqueta="Motivo de la consulta">
        <textarea {...enlazar("motivo")} required maxLength={250} rows={3} />
      </Campo>

      <AvisoDeError mensaje={agendar.error} />
      <div className="acciones">
        <button type="submit" className="boton boton--primario" disabled={agendar.enCurso || !valores.inicio}>
          Agendar cita
        </button>
      </div>
    </form>
  );
}
