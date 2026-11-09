import type { FormEvent } from "react";
import { aFechaIso } from "../../dominio/fechas";
import type { Paciente } from "../../dominio/modelos";
import { AvisoDeError } from "../componentes/Avisos";
import { Campo } from "../componentes/Campo";
import { usePuertos } from "../contexto/ContextoDePuertos";
import { useAccion } from "../hooks/useAccion";
import { useCampos } from "../hooks/useCampos";

export function FormularioDePaciente({ alRegistrar }: { alRegistrar: (paciente: Paciente) => void }) {
  const { pacientes } = usePuertos();
  const registrar = useAccion(pacientes.registrar);
  const { valores, enlazar } = useCampos({ nombre: "", documento: "", email: "", telefono: "", fechaNacimiento: "" });

  const enviar = async (evento: FormEvent) => {
    evento.preventDefault();
    const paciente = await registrar.ejecutar({
      ...valores,
      telefono: valores.telefono || undefined,
      fechaNacimiento: valores.fechaNacimiento || undefined,
    });
    if (paciente) alRegistrar(paciente);
  };

  return (
    <form className="formulario" onSubmit={enviar}>
      <Campo etiqueta="Nombre completo">
        <input {...enlazar("nombre")} required maxLength={120} autoComplete="off" autoFocus />
      </Campo>
      <Campo etiqueta="Documento de identidad" ayuda="Solo números, entre 6 y 12 dígitos.">
        <input {...enlazar("documento")} required inputMode="numeric" maxLength={15} />
      </Campo>
      <Campo etiqueta="Correo electrónico" ayuda="Aquí llegan los avisos de sus citas.">
        <input {...enlazar("email")} type="email" required />
      </Campo>
      <Campo etiqueta="Teléfono (opcional)">
        <input {...enlazar("telefono")} type="tel" maxLength={20} />
      </Campo>
      <Campo etiqueta="Fecha de nacimiento (opcional)">
        <input {...enlazar("fechaNacimiento")} type="date" max={aFechaIso(new Date())} />
      </Campo>
      <AvisoDeError mensaje={registrar.error} />
      <div className="acciones">
        <button type="submit" className="boton boton--primario" disabled={registrar.enCurso}>
          Registrar paciente
        </button>
      </div>
    </form>
  );
}
