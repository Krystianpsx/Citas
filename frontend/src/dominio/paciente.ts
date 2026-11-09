import { desdeFechaIso } from "./fechas";

export function edadEnAnios(fechaNacimiento: string, hoy: Date): number {
  const nacimiento = desdeFechaIso(fechaNacimiento);
  const yaCumplioEsteAnio =
    hoy.getMonth() > nacimiento.getMonth() ||
    (hoy.getMonth() === nacimiento.getMonth() && hoy.getDate() >= nacimiento.getDate());
  return hoy.getFullYear() - nacimiento.getFullYear() - (yaCumplioEsteAnio ? 0 : 1);
}

export function coincideConBusqueda(paciente: { nombre: string; documento: string }, busqueda: string): boolean {
  const termino = normalizar(busqueda);
  return normalizar(paciente.nombre).includes(termino) || paciente.documento.includes(termino);
}

/** Ignora mayúsculas y tildes: "jose" encuentra a "José". */
function normalizar(texto: string): string {
  return texto.normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase().trim();
}
