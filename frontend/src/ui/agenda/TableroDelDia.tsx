import { useEffect, useRef, type CSSProperties } from "react";
import { ETIQUETA_DE_ESTADO } from "../../dominio/cita";
import { aFechaIso, aHora, desdeFechaHoraIso, fechaLarga, horaDe, unirFechaYHora } from "../../dominio/fechas";
import { franjasDelDia, franjasDesdeLaApertura } from "../../dominio/horario";
import type { Cita, HorarioDeAtencion, Medico } from "../../dominio/modelos";

interface Props {
  fechaIso: string;
  horario: HorarioDeAtencion;
  medicos: Medico[];
  citas: Cita[]; // solo las que ocupan agenda
  ahora: Date;
  alElegirCita: (cita: Cita) => void;
  alElegirFranja: (medicoId: string, inicio: string) => void;
}

/** Agenda del día: una columna por médico y una fila por franja horaria. */
export function TableroDelDia({ fechaIso, horario, medicos, citas, ahora, alElegirCita, alElegirFranja }: Props) {
  const franjas = franjasDelDia(horario);
  const posicionDeAhora = calcularPosicionDeAhora(fechaIso, ahora, horario, franjas.length);
  const estilo = { "--columnas": medicos.length, "--franjas": franjas.length } as CSSProperties;
  const contenedor = useDesplazarHastaAhora(fechaIso, posicionDeAhora);

  return (
    <div ref={contenedor} className="tablero" role="region" aria-label={`Agenda del ${fechaLarga(fechaIso)}`} tabIndex={0}>
      <div className="tablero__rejilla" style={estilo}>
        <div className="tablero__esquina" aria-hidden="true" />
        {medicos.map((medico) => (
          <div key={medico.id} className="tablero__medico">
            <span className="tablero__nombre">{medico.nombre}</span>
            <span className="tablero__especialidad">{medico.especialidad.nombre}</span>
          </div>
        ))}

        <ol className="tablero__horas" aria-hidden="true">
          {franjas.map((hora) => (
            <li key={hora} className={hora.endsWith(":00") ? "en-punto" : undefined}>
              {hora}
            </li>
          ))}
        </ol>

        {medicos.map((medico) => (
          <ColumnaDelMedico
            key={medico.id}
            medico={medico}
            citas={citas.filter((cita) => cita.medico.id === medico.id)}
            franjas={franjas}
            fechaIso={fechaIso}
            horario={horario}
            ahora={ahora}
            posicionDeAhora={posicionDeAhora}
            alElegirCita={alElegirCita}
            alElegirFranja={alElegirFranja}
          />
        ))}
      </div>
    </div>
  );
}

interface PropsDeColumna extends Omit<Props, "medicos"> {
  medico: Medico;
  franjas: string[];
  posicionDeAhora: number | null;
}

function ColumnaDelMedico(props: PropsDeColumna) {
  const { medico, citas, franjas, fechaIso, horario, ahora, posicionDeAhora, alElegirCita, alElegirFranja } = props;
  const ocupadas = filasOcupadas(citas, horario);

  return (
    <div className="columna" role="group" aria-label={`Agenda de ${medico.nombre}`}>
      {franjas.map((hora, fila) => {
        if (ocupadas.has(fila)) return null;
        const inicio = unirFechaYHora(fechaIso, hora);
        const yaPaso = desdeFechaHoraIso(inicio) <= ahora;
        return yaPaso ? (
          <div key={hora} className="franja franja--pasada" style={{ gridRow: fila + 1 }} />
        ) : (
          <button
            key={hora}
            type="button"
            className="franja franja--libre"
            style={{ gridRow: fila + 1 }}
            onClick={() => alElegirFranja(medico.id, inicio)}
            aria-label={`Agendar con ${medico.nombre} a las ${hora}`}
          >
            <span>Agendar {hora}</span>
          </button>
        );
      })}

      {citas.map((cita) => (
        <BloqueDeCita key={cita.id} cita={cita} horario={horario} alElegir={() => alElegirCita(cita)} />
      ))}

      {posicionDeAhora !== null && (
        <div className="linea-ahora" style={{ "--posicion": posicionDeAhora } as CSSProperties} aria-hidden="true" />
      )}
    </div>
  );
}

function BloqueDeCita({ cita, horario, alElegir }: { cita: Cita; horario: HorarioDeAtencion; alElegir: () => void }) {
  const desde = franjasDesdeLaApertura(horaDe(cita.inicio), horario);
  const hasta = franjasDesdeLaApertura(horaDe(cita.fin), horario);
  const estado = ETIQUETA_DE_ESTADO[cita.estado];

  return (
    <button
      type="button"
      className={`bloque bloque--${cita.estado.toLowerCase()}`}
      style={{ gridRow: `${Math.floor(desde) + 1} / span ${Math.max(1, Math.ceil(hasta - desde))}` }}
      onClick={alElegir}
      aria-label={`${cita.paciente.nombre}, ${horaDe(cita.inicio)}, ${estado}`}
      title={`${estado}: ${cita.motivo}`}
    >
      <span className="bloque__paciente">{cita.paciente.nombre}</span>
      <span className="bloque__detalle">
        <span className="bloque__hora">{horaDe(cita.inicio)}</span> {cita.motivo}
      </span>
    </button>
  );
}

/** Al abrir el día de hoy, deja la hora actual cerca del borde superior del tablero. */
function useDesplazarHastaAhora(fechaIso: string, posicionDeAhora: number | null) {
  const contenedor = useRef<HTMLDivElement>(null);
  useEffect(() => {
    const elemento = contenedor.current;
    const primeraFranja = elemento?.querySelector(".tablero__horas li");
    if (!elemento || !primeraFranja || posicionDeAhora === null) return;
    const altoDeFranja = primeraFranja.getBoundingClientRect().height;
    elemento.scrollTop = Math.max(0, (posicionDeAhora - 1) * altoDeFranja);
    // Solo al cambiar de día: no se mueve la vista mientras el usuario trabaja.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [fechaIso]);
  return contenedor;
}

function filasOcupadas(citas: Cita[], horario: HorarioDeAtencion): Set<number> {
  const filas = new Set<number>();
  for (const cita of citas) {
    const desde = Math.floor(franjasDesdeLaApertura(horaDe(cita.inicio), horario));
    const hasta = Math.ceil(franjasDesdeLaApertura(horaDe(cita.fin), horario));
    for (let fila = desde; fila < hasta; fila++) filas.add(fila);
  }
  return filas;
}

function calcularPosicionDeAhora(fechaIso: string, ahora: Date, horario: HorarioDeAtencion, totalDeFranjas: number) {
  if (fechaIso !== aFechaIso(ahora)) return null;
  const posicion = franjasDesdeLaApertura(aHora(ahora.getHours() * 60 + ahora.getMinutes()), horario);
  return posicion >= 0 && posicion <= totalDeFranjas ? posicion : null;
}
