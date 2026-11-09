// Puertos del frontend: lo que la interfaz NECESITA, sin decir cómo se obtiene.
// Los componentes dependen de estas interfaces (Inversión de Dependencias);
// la implementación HTTP vive en infraestructura y se inyecta con un contexto de React.
import type {
  Cita,
  Especialidad,
  HorarioDeAtencion,
  Medico,
  NuevaCita,
  NuevoMedico,
  NuevoPaciente,
  Paciente,
} from "../dominio/modelos";

export interface RepositorioDePacientes {
  listar(): Promise<Paciente[]>;
  registrar(datos: NuevoPaciente): Promise<Paciente>;
}

export interface RepositorioDeMedicos {
  listar(): Promise<Medico[]>;
  registrar(datos: NuevoMedico): Promise<Medico>;
  especialidades(): Promise<Especialidad[]>;
  horasLibres(medicoId: string, fechaIso: string): Promise<string[]>;
}

export interface RepositorioDeCitas {
  delDia(fechaIso: string): Promise<Cita[]>;
  agendar(datos: NuevaCita): Promise<Cita>;
  confirmar(citaId: string): Promise<Cita>;
  atender(citaId: string): Promise<Cita>;
  cancelar(citaId: string, motivo: string): Promise<Cita>;
  reprogramar(citaId: string, nuevoInicio: string): Promise<Cita>;
}

export interface InformacionDeLaClinica {
  horario(): Promise<HorarioDeAtencion>;
}

export interface Puertos {
  pacientes: RepositorioDePacientes;
  medicos: RepositorioDeMedicos;
  citas: RepositorioDeCitas;
  clinica: InformacionDeLaClinica;
}
