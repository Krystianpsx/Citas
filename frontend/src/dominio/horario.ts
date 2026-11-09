import type { HorarioDeAtencion } from "./modelos";
import { aHora, diaDeLaSemana, minutosDelDia, sumarDias } from "./fechas";

/** Horas de inicio de cada franja: ["08:00", "08:30", ...]. */
export function franjasDelDia(horario: HorarioDeAtencion): string[] {
  const franjas: string[] = [];
  const cierre = minutosDelDia(horario.cierre);
  for (let inicio = minutosDelDia(horario.apertura); inicio + horario.duracionCitaMinutos <= cierre; inicio += horario.duracionCitaMinutos) {
    franjas.push(aHora(inicio));
  }
  return franjas;
}

export function esDiaHabil(fechaIso: string, horario: HorarioDeAtencion): boolean {
  return horario.diasHabiles.includes(diaDeLaSemana(fechaIso));
}

export function proximoDiaHabil(fechaIso: string, horario: HorarioDeAtencion): string {
  let candidato = sumarDias(fechaIso, 1);
  while (!esDiaHabil(candidato, horario)) candidato = sumarDias(candidato, 1);
  return candidato;
}

/** Cuántas franjas hay entre la apertura y una hora dada (puede tener decimales). */
export function franjasDesdeLaApertura(hora: string, horario: HorarioDeAtencion): number {
  return (minutosDelDia(hora) - minutosDelDia(horario.apertura)) / horario.duracionCitaMinutos;
}
