import { createContext, useContext, type ReactNode } from "react";
import type { Puertos } from "../../aplicacion/puertos";

const ContextoDePuertos = createContext<Puertos | null>(null);

/** Inyección de dependencias al estilo React: la raíz decide qué adaptadores
 *  usar y los componentes solo piden "los puertos" con usePuertos(). */
export function ProveedorDePuertos({ puertos, children }: { puertos: Puertos; children: ReactNode }) {
  return <ContextoDePuertos.Provider value={puertos}>{children}</ContextoDePuertos.Provider>;
}

export function usePuertos(): Puertos {
  const puertos = useContext(ContextoDePuertos);
  if (!puertos) throw new Error("usePuertos debe usarse dentro de <ProveedorDePuertos>.");
  return puertos;
}
