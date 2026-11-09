import type { FormEvent } from "react";
import type { Medico } from "../../dominio/modelos";
import { AvisoDeError } from "../componentes/Avisos";
import { Campo } from "../componentes/Campo";
import { usePuertos } from "../contexto/ContextoDePuertos";
import { useAccion } from "../hooks/useAccion";
import { useCampos } from "../hooks/useCampos";
import { useConsulta } from "../hooks/useConsulta";

export function FormularioDeMedico({ alRegistrar }: { alRegistrar: (medico: Medico) => void }) {
  const { medicos } = usePuertos();
  const especialidades = useConsulta(() => medicos.especialidades(), []);
  const registrar = useAccion(medicos.registrar);
  const { valores, enlazar } = useCampos({ nombre: "", especialidad: "", registroProfesional: "", email: "" });

  const enviar = async (evento: FormEvent) => {
    evento.preventDefault();
    const medico = await registrar.ejecutar(valores);
    if (medico) alRegistrar(medico);
  };

  return (
    <form className="formulario" onSubmit={enviar}>
      <Campo etiqueta="Nombre completo" ayuda="Tal como debe aparecer en la agenda, por ejemplo: Dra. Lucía Fernández.">
        <input {...enlazar("nombre")} required maxLength={120} autoFocus />
      </Campo>
      <Campo etiqueta="Especialidad">
        <select {...enlazar("especialidad")} required>
          <option value="">Elige una especialidad</option>
          {especialidades.datos?.map((especialidad) => (
            <option key={especialidad.codigo} value={especialidad.codigo}>
              {especialidad.nombre}
            </option>
          ))}
        </select>
      </Campo>
      <Campo etiqueta="Registro profesional" ayuda="Entre 4 y 20 letras, números o guiones. No puede repetirse.">
        <input {...enlazar("registroProfesional")} required maxLength={20} />
      </Campo>
      <Campo etiqueta="Correo electrónico">
        <input {...enlazar("email")} type="email" required />
      </Campo>
      <AvisoDeError mensaje={especialidades.error ?? registrar.error} />
      <div className="acciones">
        <button type="submit" className="boton boton--primario" disabled={registrar.enCurso}>
          Registrar médico
        </button>
      </div>
    </form>
  );
}
