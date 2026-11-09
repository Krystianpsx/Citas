// Adaptadores HTTP: implementan los puertos hablando con la API REST de Flask.
// Si mañana los datos vinieran de GraphQL o de un mock, se crea otro adaptador
// y ningún componente de la interfaz cambia.
import type {
  InformacionDeLaClinica,
  Puertos,
  RepositorioDeCitas,
  RepositorioDeMedicos,
  RepositorioDePacientes,
} from "../../aplicacion/puertos";
import type {
  Cita,
  Especialidad,
  HorarioDeAtencion,
  Medico,
  NuevaCita,
  NuevoMedico,
  NuevoPaciente,
  Paciente,
} from "../../dominio/modelos";
import { ClienteHttp } from "./clienteHttp";

class PacientesHttp implements RepositorioDePacientes {
  constructor(private readonly http: ClienteHttp) {}

  listar = () => this.http.get<Paciente[]>("/pacientes");

  registrar = (datos: NuevoPaciente) => this.http.post<Paciente>("/pacientes", datos);
}

class MedicosHttp implements RepositorioDeMedicos {
  constructor(private readonly http: ClienteHttp) {}

  listar = () => this.http.get<Medico[]>("/medicos");

  registrar = (datos: NuevoMedico) => this.http.post<Medico>("/medicos", datos);

  especialidades = () => this.http.get<Especialidad[]>("/medicos/especialidades");

  horasLibres = (medicoId: string, fechaIso: string) =>
    this.http.get<string[]>(`/medicos/${encodeURIComponent(medicoId)}/disponibilidad`, { fecha: fechaIso });
}

class CitasHttp implements RepositorioDeCitas {
  constructor(private readonly http: ClienteHttp) {}

  delDia = (fechaIso: string) => this.http.get<Cita[]>("/citas", { fecha: fechaIso });

  agendar = (datos: NuevaCita) => this.http.post<Cita>("/citas", datos);

  confirmar = (citaId: string) => this.http.post<Cita>(this.ruta(citaId, "confirmar"));

  atender = (citaId: string) => this.http.post<Cita>(this.ruta(citaId, "atender"));

  cancelar = (citaId: string, motivo: string) => this.http.post<Cita>(this.ruta(citaId, "cancelar"), { motivo });

  reprogramar = (citaId: string, nuevoInicio: string) =>
    this.http.post<Cita>(this.ruta(citaId, "reprogramar"), { inicio: nuevoInicio });

  private ruta(citaId: string, accion: string): string {
    return `/citas/${encodeURIComponent(citaId)}/${accion}`;
  }
}

class ClinicaHttp implements InformacionDeLaClinica {
  constructor(private readonly http: ClienteHttp) {}

  horario = () => this.http.get<HorarioDeAtencion>("/horario");
}

export function crearPuertosHttp(urlBase: string): Puertos {
  const http = new ClienteHttp(urlBase);
  return {
    pacientes: new PacientesHttp(http),
    medicos: new MedicosHttp(http),
    citas: new CitasHttp(http),
    clinica: new ClinicaHttp(http),
  };
}
