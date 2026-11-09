import type { ReactNode } from "react";

interface Props {
  titulo: string;
  descripcion: string;
  accion?: ReactNode;
}

export function CabeceraDePagina({ titulo, descripcion, accion }: Props) {
  return (
    <header className="pagina__cabecera">
      <div>
        <h1>{titulo}</h1>
        <p className="pagina__descripcion">{descripcion}</p>
      </div>
      {accion}
    </header>
  );
}
