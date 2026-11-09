import type { Cita, EstadoCita } from "./modelos";
import { desdeFechaHoraIso } from "./fechas";

export type AccionSobreCita = "confirmar" | "atender" | "reprogramar" | "cancelar";

export const ETIQUETA_DE_ESTADO: Record<EstadoCita, string> = {
  PROGRAMADA: "Programada",
  CONFIRMADA: "Confirmada",
  ATENDIDA: "Atendida",
  CANCELADA: "Cancelada",
};

// Misma tabla de transiciones que el backend. La interfaz solo ofrece lo que el
// servidor va a aceptar; el servidor sigue siendo quien valida de verdad.
const ACCIONES_POR_ESTADO: Record<EstadoCita, readonly AccionSobreCita[]> = {
  PROGRAMADA: ["confirmar", "reprogramar", "cancelar"],
  CONFIRMADA: ["atender", "reprogramar", "cancelar"],
  ATENDIDA: [],
  CANCELADA: [],
};

type Momento = "siempre" | "antes del inicio" | "desde el inicio";

const MOMENTO_PERMITIDO: Record<AccionSobreCita, Momento> = {
  confirmar: "siempre",
  atender: "desde el inicio",
  reprogramar: "antes del inicio",
  cancelar: "antes del inicio",
};

export function accionesDisponibles(cita: Cita, ahora: Date): AccionSobreCita[] {
  const yaEmpezo = ahora >= desdeFechaHoraIso(cita.inicio);
  return ACCIONES_POR_ESTADO[cita.estado].filter((accion) => {
    const momento = MOMENTO_PERMITIDO[accion];
    if (momento === "antes del inicio") return !yaEmpezo;
    if (momento === "desde el inicio") return yaEmpezo;
    return true;
  });
}

export const ocupaAgenda = (cita: Cita) => cita.estado !== "CANCELADA";

export function resumenDelDia(citas: Cita[]): string {
  const vigentes = citas.filter(ocupaAgenda);
  if (vigentes.length === 0) return "Sin citas agendadas";
  const confirmadas = vigentes.filter((cita) => cita.estado === "CONFIRMADA").length;
  const atendidas = vigentes.filter((cita) => cita.estado === "ATENDIDA").length;
  const partes = [`${vigentes.length} ${vigentes.length === 1 ? "cita" : "citas"}`];
  if (confirmadas) partes.push(`${confirmadas} ${confirmadas === 1 ? "confirmada" : "confirmadas"}`);
  if (atendidas) partes.push(`${atendidas} ${atendidas === 1 ? "atendida" : "atendidas"}`);
  return partes.join(", ");
}
