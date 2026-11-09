import { useState } from "react";
import { AvisoDeError, AvisoDeExito, Cargando } from "../componentes/Avisos";
import { CabeceraDePagina } from "../componentes/CabeceraDePagina";
import { Cajon } from "../componentes/Cajon";
import { usePuertos } from "../contexto/ContextoDePuertos";
import { useConsulta } from "../hooks/useConsulta";
import { FormularioDeMedico } from "./FormularioDeMedico";

export function PaginaMedicos() {
  const { medicos } = usePuertos();
  const lista = useConsulta(() => medicos.listar(), []);
  const [especialidad, setEspecialidad] = useState("");
  const [registrando, setRegistrando] = useState(false);
  const [aviso, setAviso] = useState<string | null>(null);

  const todos = lista.datos ?? [];
  const especialidadesPresentes = [...new Map(todos.map((m) => [m.especialidad.codigo, m.especialidad])).values()];
  const visibles = especialidad ? todos.filter((m) => m.especialidad.codigo === especialidad) : todos;

  return (
    <section className="pagina">
      <CabeceraDePagina
        titulo="Médicos"
        descripcion="Cada médico registrado tiene su propia columna en la agenda."
        accion={
          <button className="boton boton--primario" onClick={() => setRegistrando(true)}>
            Registrar médico
          </button>
        }
      />
      {aviso && <AvisoDeExito mensaje={aviso} />}

      {especialidadesPresentes.length > 1 && (
        <select
          className="buscador"
          value={especialidad}
          onChange={(e) => setEspecialidad(e.target.value)}
          aria-label="Filtrar por especialidad"
        >
          <option value="">Todas las especialidades</option>
          {especialidadesPresentes.map((e) => (
            <option key={e.codigo} value={e.codigo}>
              {e.nombre}
            </option>
          ))}
        </select>
      )}

      {lista.cargando && <Cargando />}
      <AvisoDeError mensaje={lista.error} />
      {lista.datos && visibles.length === 0 && <p className="vacio">Aún no hay médicos registrados.</p>}
      {visibles.length > 0 && (
        <div className="tabla">
          <table>
            <thead>
              <tr>
                <th scope="col">Nombre</th>
                <th scope="col">Especialidad</th>
                <th scope="col">Registro profesional</th>
                <th scope="col">Correo</th>
              </tr>
            </thead>
            <tbody>
              {visibles.map((medico) => (
                <tr key={medico.id}>
                  <td>{medico.nombre}</td>
                  <td>{medico.especialidad.nombre}</td>
                  <td>{medico.registroProfesional}</td>
                  <td>{medico.email}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Cajon titulo="Registrar médico" abierto={registrando} alCerrar={() => setRegistrando(false)}>
        <FormularioDeMedico
          alRegistrar={(medico) => {
            setRegistrando(false);
            setAviso(`${medico.nombre} quedó registrado y ya aparece en la agenda.`);
            lista.recargar();
          }}
        />
      </Cajon>
    </section>
  );
}
