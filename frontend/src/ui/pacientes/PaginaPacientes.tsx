import { useState } from "react";
import { coincideConBusqueda, edadEnAnios } from "../../dominio/paciente";
import { AvisoDeError, AvisoDeExito, Cargando } from "../componentes/Avisos";
import { CabeceraDePagina } from "../componentes/CabeceraDePagina";
import { Cajon } from "../componentes/Cajon";
import { usePuertos } from "../contexto/ContextoDePuertos";
import { useConsulta } from "../hooks/useConsulta";
import { FormularioDePaciente } from "./FormularioDePaciente";

export function PaginaPacientes() {
  const { pacientes } = usePuertos();
  const lista = useConsulta(() => pacientes.listar(), []);
  const [busqueda, setBusqueda] = useState("");
  const [registrando, setRegistrando] = useState(false);
  const [aviso, setAviso] = useState<string | null>(null);

  const hoy = new Date();
  const visibles = (lista.datos ?? []).filter((paciente) => coincideConBusqueda(paciente, busqueda));

  return (
    <section className="pagina">
      <CabeceraDePagina
        titulo="Pacientes"
        descripcion="Personas que pueden recibir citas en la clínica."
        accion={
          <button className="boton boton--primario" onClick={() => setRegistrando(true)}>
            Registrar paciente
          </button>
        }
      />
      {aviso && <AvisoDeExito mensaje={aviso} />}

      <input
        type="search"
        className="buscador"
        placeholder="Buscar por nombre o documento"
        aria-label="Buscar pacientes"
        value={busqueda}
        onChange={(e) => setBusqueda(e.target.value)}
      />

      {lista.cargando && <Cargando />}
      <AvisoDeError mensaje={lista.error} />
      {lista.datos && visibles.length === 0 && (
        <p className="vacio">
          {busqueda ? `Ningún paciente coincide con “${busqueda}”.` : "Aún no hay pacientes registrados."}
        </p>
      )}
      {visibles.length > 0 && (
        <div className="tabla">
          <table>
            <thead>
              <tr>
                <th scope="col">Nombre</th>
                <th scope="col">Documento</th>
                <th scope="col">Correo</th>
                <th scope="col">Teléfono</th>
                <th scope="col" className="numero">Edad</th>
              </tr>
            </thead>
            <tbody>
              {visibles.map((paciente) => (
                <tr key={paciente.id}>
                  <td>{paciente.nombre}</td>
                  <td>{paciente.documento}</td>
                  <td>{paciente.email}</td>
                  <td>{paciente.telefono ?? "Sin registrar"}</td>
                  <td className="numero">
                    {paciente.fechaNacimiento ? edadEnAnios(paciente.fechaNacimiento, hoy) : "Sin registrar"}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Cajon titulo="Registrar paciente" abierto={registrando} alCerrar={() => setRegistrando(false)}>
        <FormularioDePaciente
          alRegistrar={(paciente) => {
            setRegistrando(false);
            setAviso(`${paciente.nombre} quedó registrado.`);
            lista.recargar();
          }}
        />
      </Cajon>
    </section>
  );
}
