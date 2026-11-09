import { ETIQUETA_DE_ESTADO } from "../../dominio/cita";
import type { EstadoCita } from "../../dominio/modelos";

export function InsigniaDeEstado({ estado }: { estado: EstadoCita }) {
  return <span className={`insignia insignia--${estado.toLowerCase()}`}>{ETIQUETA_DE_ESTADO[estado]}</span>;
}
