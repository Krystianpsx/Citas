// Modelos del dominio en el frontend. Reflejan el contrato JSON del backend,
// pero ningún componente depende de cómo se obtienen (eso es infraestructura).

export type EstadoCita = "PROGRAMADA" | "CONFIRMADA" | "ATENDIDA" | "CANCELADA";

export interface Especialidad {
  codigo: string;
  nombre: string;
}

export interface Paciente {
  id: string;
  nombre: string;
  documento: string;
  email: string;
  telefono: string | null;
  fechaNacimiento: string | null; // AAAA-MM-DD
}

export interface Medico {
  id: string;
  nombre: string;
  especialidad: Especialidad;
  registroProfesional: string;
  email: string;
}

export interface Cita {
  id: string;
  inicio: string; // AAAA-MM-DDTHH:MM, hora local de la clínica
  fin: string;
  motivo: string;
  estado: EstadoCita;
  motivoCancelacion: string | null;
  paciente: { id: string; nombre: string };
  medico: { id: string; nombre: string; especialidad: Especialidad };
}

export interface HorarioDeAtencion {
  apertura: string; // HH:MM
  cierre: string;
  duracionCitaMinutos: number;
  diasHabiles: number[]; // 0 = lunes, igual que en el backend
  descripcion: string;
}

export interface NuevoPaciente {
  nombre: string;
  documento: string;
  email: string;
  telefono?: string;
  fechaNacimiento?: string;
}

export interface NuevoMedico {
  nombre: string;
  especialidad: string;
  registroProfesional: string;
  email: string;
}

export interface NuevaCita {
  pacienteId: string;
  medicoId: string;
  inicio: string;
  motivo: string;
}
