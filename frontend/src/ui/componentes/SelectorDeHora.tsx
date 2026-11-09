import { horaDe } from "../../dominio/fechas";
import { usePuertos } from "../contexto/ContextoDePuertos";
import { useConsulta } from "../hooks/useConsulta";
import { AvisoDeError } from "./Avisos";

interface Props {
  medicoId: string;
  fechaIso: string;
  valor: string; // AAAA-MM-DDTHH:MM o vacío
  alCambiar: (inicio: string) => void;
  horaActual?: string; // en reprogramaciones, la hora que ya tiene la cita
}

/** Muestra solo las horas libres del médico, consultadas al backend. */
export function SelectorDeHora({ medicoId, fechaIso, valor, alCambiar, horaActual }: Props) {
  const { medicos } = usePuertos();
  const consulta = useConsulta(
    () => (medicoId && fechaIso ? medicos.horasLibres(medicoId, fechaIso) : Promise.resolve([])),
    [medicoId, fechaIso],
  );

  if (!medicoId || !fechaIso) return <p className="campo__ayuda">Elige médico y fecha para ver las horas libres.</p>;
  if (consulta.cargando) return <p className="campo__ayuda">Buscando horas libres…</p>;
  if (consulta.error) return <AvisoDeError mensaje={consulta.error} />;

  const horas = (consulta.datos ?? []).filter((inicio) => inicio !== horaActual);
  if (horas.length === 0) {
    return <p className="campo__ayuda">No quedan horas libres ese día. Prueba con otra fecha.</p>;
  }

  return (
    <div className="horas" role="radiogroup" aria-label="Horas libres">
      {horas.map((inicio) => (
        <label key={inicio} className="horas__opcion">
          <input
            type="radio"
            name="hora"
            value={inicio}
            checked={valor === inicio}
            onChange={() => alCambiar(inicio)}
          />
          <span>{horaDe(inicio)}</span>
        </label>
      ))}
    </div>
  );
}
