import { describe, expect, it } from "vitest";
import { accionesDisponibles, resumenDelDia } from "./cita";
import { diaDeLaSemana, fechaLarga, sumarDias } from "./fechas";
import { esDiaHabil, franjasDelDia, franjasDesdeLaApertura, proximoDiaHabil } from "./horario";
import type { Cita, EstadoCita, HorarioDeAtencion } from "./modelos";
import { coincideConBusqueda, edadEnAnios } from "./paciente";

const HORARIO: HorarioDeAtencion = {
  apertura: "08:00",
  cierre: "18:00",
  duracionCitaMinutos: 30,
  diasHabiles: [0, 1, 2, 3, 4],
  descripcion: "de lunes a viernes, de 08:00 a 18:00",
};

function cita(estado: EstadoCita, inicio = "2026-11-10T09:00"): Cita {
  return {
    id: "c-1",
    inicio,
    fin: "2026-11-10T09:30",
    motivo: "Control",
    estado,
    motivoCancelacion: null,
    paciente: { id: "p-1", nombre: "Ana" },
    medico: { id: "m-1", nombre: "Dra. Lucía", especialidad: { codigo: "PEDIATRIA", nombre: "Pediatría" } },
  };
}

const ANTES = new Date(2026, 10, 10, 8, 0);
const DESPUES = new Date(2026, 10, 10, 9, 10);

describe("acciones disponibles sobre una cita", () => {
  it("una cita programada se confirma, reprograma o cancela antes de empezar", () => {
    expect(accionesDisponibles(cita("PROGRAMADA"), ANTES)).toEqual(["confirmar", "reprogramar", "cancelar"]);
  });

  it("una cita confirmada solo se atiende cuando ya empezó", () => {
    expect(accionesDisponibles(cita("CONFIRMADA"), ANTES)).toEqual(["reprogramar", "cancelar"]);
    expect(accionesDisponibles(cita("CONFIRMADA"), DESPUES)).toEqual(["atender"]);
  });

  it("las citas atendidas o canceladas no admiten acciones", () => {
    expect(accionesDisponibles(cita("ATENDIDA"), DESPUES)).toEqual([]);
    expect(accionesDisponibles(cita("CANCELADA"), ANTES)).toEqual([]);
  });

  it("resume el día sin contar las canceladas", () => {
    expect(resumenDelDia([cita("CONFIRMADA"), cita("PROGRAMADA"), cita("CANCELADA")])).toBe("2 citas, 1 confirmada");
    expect(resumenDelDia([])).toBe("Sin citas agendadas");
  });
});

describe("horario de atención", () => {
  it("genera veinte franjas de media hora", () => {
    const franjas = franjasDelDia(HORARIO);
    expect(franjas).toHaveLength(20);
    expect(franjas[0]).toBe("08:00");
    expect(franjas[franjas.length - 1]).toBe("17:30");
  });

  it("calcula la posición de una hora respecto a la apertura", () => {
    expect(franjasDesdeLaApertura("09:15", HORARIO)).toBe(2.5);
  });

  it("reconoce fines de semana y salta al lunes", () => {
    expect(esDiaHabil("2026-11-14", HORARIO)).toBe(false);
    expect(proximoDiaHabil("2026-11-13", HORARIO)).toBe("2026-11-16");
  });
});

describe("fechas", () => {
  it("usa lunes = 0 como el backend", () => {
    expect(diaDeLaSemana("2026-11-09")).toBe(0);
    expect(diaDeLaSemana("2026-11-15")).toBe(6);
  });

  it("cruza cambios de mes", () => {
    expect(sumarDias("2026-11-30", 1)).toBe("2026-12-01");
  });

  it("escribe la fecha en español", () => {
    expect(fechaLarga("2026-11-10")).toBe("Martes 10 de noviembre");
  });
});

describe("pacientes", () => {
  it("calcula la edad considerando si ya cumplió años", () => {
    expect(edadEnAnios("1990-11-20", new Date(2026, 10, 9))).toBe(35);
    expect(edadEnAnios("1990-11-09", new Date(2026, 10, 9))).toBe(36);
  });

  it("busca sin importar tildes ni mayúsculas", () => {
    expect(coincideConBusqueda({ nombre: "José Ramírez", documento: "80123456" }, "jose")).toBe(true);
    expect(coincideConBusqueda({ nombre: "José Ramírez", documento: "80123456" }, "8012")).toBe(true);
    expect(coincideConBusqueda({ nombre: "José Ramírez", documento: "80123456" }, "ana")).toBe(false);
  });
});
