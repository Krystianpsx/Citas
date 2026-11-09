import { useState } from "react";
import { accionesDisponibles } from "../../dominio/cita";
import { aFechaIso, fechaDe, fechaLarga, horaDe } from "../../dominio/fechas";
import type { Cita } from "../../dominio/modelos";
import { AvisoDeError, AvisoDeExito } from "../componentes/Avisos";
import { Campo } from "../componentes/Campo";
import { InsigniaDeEstado } from "../componentes/InsigniaDeEstado";
import { SelectorDeHora } from "../componentes/SelectorDeHora";
import { usePuertos } from "../contexto/ContextoDePuertos";
import { useAccion } from "../hooks/useAccion";

interface Props {
  cita: Cita;
  aviso?: string;
  ahora: Date;
  alActualizar: (cita: Cita, aviso: string) => void;
}

type Modo = "detalle" | "cancelar" | "reprogramar";

export function PanelDeCita({ cita, aviso, ahora, alActualizar }: Props) {
  const { citas } = usePuertos();
  const [modo, setModo] = useState<Modo>("detalle");
  const cambio = useAccion(async (operacion: () => Promise<Cita>, mensajeDeExito: string) => {
    alActualizar(await operacion(), mensajeDeExito);
    setModo("detalle");
  });
  const acciones = accionesDisponibles(cita, ahora);

  return (
    <div className="panel">
      <InsigniaDeEstado estado={cita.estado} />
      <h3 className="panel__paciente">{cita.paciente.nombre}</h3>
      <FichaDeCita cita={cita} />

      {aviso && modo === "detalle" && <AvisoDeExito mensaje={aviso} />}
      <AvisoDeError mensaje={cambio.error} />

      {modo === "detalle" && (
        <div className="acciones">
          {acciones.includes("confirmar") && (
            <button
              className="boton boton--primario"
              disabled={cambio.enCurso}
              onClick={() => cambio.ejecutar(() => citas.confirmar(cita.id), "Asistencia confirmada.")}
            >
              Confirmar asistencia
            </button>
          )}
          {acciones.includes("atender") && (
            <button
              className="boton boton--primario"
              disabled={cambio.enCurso}
              onClick={() => cambio.ejecutar(() => citas.atender(cita.id), "Cita marcada como atendida.")}
            >
              Marcar como atendida
            </button>
          )}
          {acciones.includes("reprogramar") && (
            <button className="boton" onClick={() => setModo("reprogramar")}>
              Reprogramar
            </button>
          )}
          {acciones.includes("cancelar") && (
            <button className="boton boton--peligro" onClick={() => setModo("cancelar")}>
              Cancelar cita
            </button>
          )}
          <NotaSobreAcciones cita={cita} puedeAtender={acciones.includes("atender")} />
        </div>
      )}

      {modo === "cancelar" && (
        <FormularioDeCancelacion
          enCurso={cambio.enCurso}
          alVolver={() => setModo("detalle")}
          alConfirmar={(motivo) => cambio.ejecutar(() => citas.cancelar(cita.id, motivo), "Cita cancelada.")}
        />
      )}

      {modo === "reprogramar" && (
        <FormularioDeReprogramacion
          cita={cita}
          enCurso={cambio.enCurso}
          alVolver={() => setModo("detalle")}
          alConfirmar={(inicio) => cambio.ejecutar(() => citas.reprogramar(cita.id, inicio), "Cita reprogramada.")}
        />
      )}
    </div>
  );
}

function FichaDeCita({ cita }: { cita: Cita }) {
  return (
    <dl className="ficha">
      <dt>Cuándo</dt>
      <dd>
        {fechaLarga(fechaDe(cita.inicio))}, de {horaDe(cita.inicio)} a {horaDe(cita.fin)}
      </dd>
      <dt>Médico</dt>
      <dd>
        {cita.medico.nombre}, {cita.medico.especialidad.nombre.toLowerCase()}
      </dd>
      <dt>Motivo de la consulta</dt>
      <dd>{cita.motivo}</dd>
      {cita.motivoCancelacion && (
        <>
          <dt>Motivo de la cancelación</dt>
          <dd>{cita.motivoCancelacion}</dd>
        </>
      )}
    </dl>
  );
}

function NotaSobreAcciones({ cita, puedeAtender }: { cita: Cita; puedeAtender: boolean }) {
  const notas: Partial<Record<Cita["estado"], string>> = {
    ATENDIDA: "La cita ya fue atendida y no admite más cambios.",
    CANCELADA: "La cita está cancelada. Para atender al paciente, agenda una nueva.",
    CONFIRMADA: puedeAtender ? undefined : `Podrás marcarla como atendida a partir de las ${horaDe(cita.inicio)}.`,
  };
  const nota = notas[cita.estado];
  return nota ? <p className="panel__nota">{nota}</p> : null;
}

interface PropsDeSubformulario<T> {
  enCurso: boolean;
  alVolver: () => void;
  alConfirmar: (valor: T) => void;
}

function FormularioDeCancelacion({ enCurso, alVolver, alConfirmar }: PropsDeSubformulario<string>) {
  const [motivo, setMotivo] = useState("");
  return (
    <form
      className="formulario"
      onSubmit={(evento) => {
        evento.preventDefault();
        alConfirmar(motivo);
      }}
    >
      <Campo etiqueta="Motivo de la cancelación" ayuda="Se incluye en el aviso que recibe el paciente.">
        <textarea value={motivo} onChange={(e) => setMotivo(e.target.value)} required maxLength={250} rows={3} autoFocus />
      </Campo>
      <div className="acciones">
        <button type="submit" className="boton boton--peligro-lleno" disabled={enCurso}>
          Cancelar cita
        </button>
        <button type="button" className="boton boton--fantasma" onClick={alVolver}>
          Volver
        </button>
      </div>
    </form>
  );
}

function FormularioDeReprogramacion({ cita, enCurso, alVolver, alConfirmar }: PropsDeSubformulario<string> & { cita: Cita }) {
  const [fechaIso, setFechaIso] = useState(fechaDe(cita.inicio));
  const [inicio, setInicio] = useState("");
  return (
    <form
      className="formulario"
      onSubmit={(evento) => {
        evento.preventDefault();
        alConfirmar(inicio);
      }}
    >
      <Campo etiqueta="Nueva fecha">
        <input
          type="date"
          value={fechaIso}
          min={aFechaIso(new Date())}
          onChange={(e) => {
            setFechaIso(e.target.value);
            setInicio("");
          }}
          required
        />
      </Campo>
      <fieldset className="campo">
        <legend className="campo__etiqueta">Nueva hora con {cita.medico.nombre}</legend>
        <SelectorDeHora
          medicoId={cita.medico.id}
          fechaIso={fechaIso}
          valor={inicio}
          alCambiar={setInicio}
          horaActual={cita.inicio}
        />
      </fieldset>
      <div className="acciones">
        <button type="submit" className="boton boton--primario" disabled={enCurso || !inicio}>
          Reprogramar
        </button>
        <button type="button" className="boton boton--fantasma" onClick={alVolver}>
          Volver
        </button>
      </div>
    </form>
  );
}
