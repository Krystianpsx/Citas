import type { ReactNode } from "react";

interface Props {
  etiqueta: string;
  ayuda?: string;
  children: ReactNode;
}

export function Campo({ etiqueta, ayuda, children }: Props) {
  return (
    <label className="campo">
      <span className="campo__etiqueta">{etiqueta}</span>
      {children}
      {ayuda && <span className="campo__ayuda">{ayuda}</span>}
    </label>
  );
}
