// Utilidades de fecha sin dependencias externas. Trabajan con hora local
// porque la clínica atiende en una sola zona horaria.

const DIAS = ["domingo", "lunes", "martes", "miércoles", "jueves", "viernes", "sábado"];
const MESES = [
  "enero", "febrero", "marzo", "abril", "mayo", "junio",
  "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre",
];

const dosDigitos = (numero: number) => String(numero).padStart(2, "0");

export function aFechaIso(fecha: Date): string {
  return `${fecha.getFullYear()}-${dosDigitos(fecha.getMonth() + 1)}-${dosDigitos(fecha.getDate())}`;
}

export function desdeFechaIso(fechaIso: string): Date {
  const [anio, mes, dia] = fechaIso.split("-").map(Number);
  return new Date(anio, mes - 1, dia);
}

/** "2026-11-10T08:30" se interpreta como hora local (así lo define el estándar ECMAScript). */
export function desdeFechaHoraIso(fechaHoraIso: string): Date {
  return new Date(fechaHoraIso);
}

export const unirFechaYHora = (fechaIso: string, hora: string) => `${fechaIso}T${hora}`;

export const horaDe = (fechaHoraIso: string) => fechaHoraIso.slice(11, 16);

export const fechaDe = (fechaHoraIso: string) => fechaHoraIso.slice(0, 10);

export function sumarDias(fechaIso: string, dias: number): string {
  const fecha = desdeFechaIso(fechaIso);
  fecha.setDate(fecha.getDate() + dias);
  return aFechaIso(fecha);
}

export function minutosDelDia(hora: string): number {
  const [horas, minutos] = hora.split(":").map(Number);
  return horas * 60 + minutos;
}

export function aHora(minutosDesdeMedianoche: number): string {
  return `${dosDigitos(Math.floor(minutosDesdeMedianoche / 60))}:${dosDigitos(minutosDesdeMedianoche % 60)}`;
}

/** Día de la semana con lunes = 0, la misma convención que usa Python en el backend. */
export function diaDeLaSemana(fechaIso: string): number {
  return (desdeFechaIso(fechaIso).getDay() + 6) % 7;
}

/** "Martes 10 de noviembre" */
export function fechaLarga(fechaIso: string): string {
  const fecha = desdeFechaIso(fechaIso);
  const dia = DIAS[fecha.getDay()];
  return `${dia[0].toUpperCase()}${dia.slice(1)} ${fecha.getDate()} de ${MESES[fecha.getMonth()]}`;
}
