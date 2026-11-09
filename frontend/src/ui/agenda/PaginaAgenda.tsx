import { useState } from "react";
import { Link } from "react-router-dom";
import { ocupaAgenda, resumenDelDia } from "../../dominio/cita";
import { aFechaIso, fechaDe, fechaLarga, horaDe, sumarDias } from "../../dominio/fechas";
import { esDiaHabil, proximoDiaHabil } from "../../dominio/horario";
import type { Cita, HorarioDeAtencion, Medico } from "../../dominio/modelos";
import { AvisoDeError, Cargando } from "../componentes/Avisos";
import { Cajon } from "../componentes/Cajon";
import { usePuertos } from "../contexto/ContextoDePuertos";
import { useAhora } from "../hooks/useAhora";
import { useConsulta } from "../hooks/useConsulta";
import { FormularioDeCita } from "./FormularioDeCita";
import { PanelDeCita } from "./PanelDeCita";
import { TableroDelDia } from "./TableroDelDia";

type Seleccion =
  | { tipo: "cita"; cita: Cita; aviso?: string }
  | { tipo: "nueva"; medicoId?: string; inicio?: string }
  | null;

export function PaginaAgenda() {
  const { citas, medicos, clinica } = usePuertos();
  const ahora = useAhora();
  const [fechaIso, setFechaIso] = useState(() => aFechaIso(new Date()));
  const [seleccion, setSeleccion] = useState<Seleccion>(null);

  const horario = useConsulta(() => clinica.horario(), []);
  const listaDeMedicos = useConsulta(() => medicos.listar(), []);
  const citasDelDia = useConsulta(() => citas.delDia(fechaIso), [fechaIso]);

  const mostrarCita = (cita: Cita, aviso?: string) => {
    setSeleccion({ tipo: "cita", cita, aviso });
    if (fechaDe(cita.inicio) !== fechaIso) setFechaIso(fechaDe(cita.inicio));
    else citasDelDia.recargar();
  };

  const todasLasCitas = citasDelDia.datos ?? [];

  return (
    <section className="agenda">
      <header className="agenda__cabecera">
        <div>
          <h1 className="agenda__fecha">{fechaLarga(fechaIso)}</h1>
          <p className="agenda__resumen">{citasDelDia.cargando ? "Actualizando…" : resumenDelDia(todasLasCitas)}</p>
        </div>
        <nav className="agenda__navegacion" aria-label="Cambiar de día">
          <button className="boton" onClick={() => setFechaIso(sumarDias(fechaIso, -1))} aria-label="Día anterior">
            ‹
          </button>
          <button className="boton" onClick={() => setFechaIso(aFechaIso(new Date()))}>
            Hoy
          </button>
          <button className="boton" onClick={() => setFechaIso(sumarDias(fechaIso, 1))} aria-label="Día siguiente">
            ›
          </button>
          <input
            type="date"
            className="agenda__selector-de-fecha"
            value={fechaIso}
            onChange={(e) => e.target.value && setFechaIso(e.target.value)}
            aria-label="Ir a una fecha"
          />
        </nav>
        <button className="boton boton--primario" onClick={() => setSeleccion({ tipo: "nueva" })}>
          Agendar cita
        </button>
      </header>

      <ContenidoDeLaAgenda
        cargando={horario.cargando || listaDeMedicos.cargando}
        error={horario.error ?? listaDeMedicos.error ?? citasDelDia.error}
        alReintentar={() => [horario, listaDeMedicos, citasDelDia].forEach((consulta) => consulta.recargar())}
        horario={horario.datos}
        medicos={listaDeMedicos.datos}
        fechaIso={fechaIso}
        alCambiarFecha={setFechaIso}
      >
        {(horarioCargado, medicosCargados) => (
          <TableroDelDia
            fechaIso={fechaIso}
            horario={horarioCargado}
            medicos={medicosCargados}
            citas={todasLasCitas.filter(ocupaAgenda)}
            ahora={ahora}
            alElegirCita={(cita) => setSeleccion({ tipo: "cita", cita })}
            alElegirFranja={(medicoId, inicio) => setSeleccion({ tipo: "nueva", medicoId, inicio })}
          />
        )}
      </ContenidoDeLaAgenda>

      <CitasCanceladas citas={todasLasCitas.filter((cita) => !ocupaAgenda(cita))} alElegir={(cita) => setSeleccion({ tipo: "cita", cita })} />

      <Cajon
        titulo={seleccion?.tipo === "nueva" ? "Agendar cita" : "Detalle de la cita"}
        abierto={seleccion !== null}
        alCerrar={() => setSeleccion(null)}
      >
        {seleccion?.tipo === "nueva" && (
          <FormularioDeCita
            key={`${seleccion.medicoId}-${seleccion.inicio}`}
            medicoId={seleccion.medicoId}
            inicio={seleccion.inicio}
            fechaSugerida={fechaIso}
            alAgendar={(cita) => mostrarCita(cita, "Cita agendada.")}
          />
        )}
        {seleccion?.tipo === "cita" && (
          <PanelDeCita
            key={seleccion.cita.id}
            cita={seleccion.cita}
            aviso={seleccion.aviso}
            ahora={ahora}
            alActualizar={mostrarCita}
          />
        )}
      </Cajon>
    </section>
  );
}

interface PropsDeContenido {
  cargando: boolean;
  error?: string;
  alReintentar: () => void;
  horario?: HorarioDeAtencion;
  medicos?: Medico[];
  fechaIso: string;
  alCambiarFecha: (fechaIso: string) => void;
  children: (horario: HorarioDeAtencion, medicos: Medico[]) => JSX.Element;
}

/** Decide qué mostrar antes del tablero: carga, error, sin médicos o día no hábil. */
function ContenidoDeLaAgenda({ cargando, error, alReintentar, horario, medicos, fechaIso, alCambiarFecha, children }: PropsDeContenido) {
  if (error) {
    return (
      <div className="vacio">
        <AvisoDeError mensaje={error} />
        <button className="boton" onClick={alReintentar}>
          Reintentar
        </button>
      </div>
    );
  }
  if (cargando || !horario || !medicos) return <Cargando texto="Cargando la agenda…" />;
  if (medicos.length === 0) {
    return (
      <div className="vacio">
        <p>Todavía no hay médicos registrados, así que no hay agenda que mostrar.</p>
        <Link className="boton boton--primario" to="/medicos">
          Registrar médico
        </Link>
      </div>
    );
  }
  if (!esDiaHabil(fechaIso, horario)) {
    const siguiente = proximoDiaHabil(fechaIso, horario);
    return (
      <div className="vacio">
        <p>Este día no hay atención. La clínica atiende {horario.descripcion}.</p>
        <button className="boton" onClick={() => alCambiarFecha(siguiente)}>
          Ver el {fechaLarga(siguiente).toLowerCase()}
        </button>
      </div>
    );
  }
  return children(horario, medicos);
}

function CitasCanceladas({ citas, alElegir }: { citas: Cita[]; alElegir: (cita: Cita) => void }) {
  if (citas.length === 0) return null;
  return (
    <details className="canceladas">
      <summary>
        {citas.length === 1 ? "1 cita cancelada" : `${citas.length} citas canceladas`} este día
      </summary>
      <ul>
        {citas.map((cita) => (
          <li key={cita.id}>
            <button className="enlace" onClick={() => alElegir(cita)}>
              {horaDe(cita.inicio)} {cita.paciente.nombre} con {cita.medico.nombre}
            </button>
          </li>
        ))}
      </ul>
    </details>
  );
}
