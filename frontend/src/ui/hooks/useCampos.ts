import { useState, type ChangeEvent } from "react";

type Elemento = HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement;

/** Estado de un formulario plano. `enlazar("nombre")` devuelve value y onChange
 *  listos para el input, evitando un manejador por campo. */
export function useCampos<T extends Record<string, string>>(iniciales: T) {
  const [valores, setValores] = useState<T>(iniciales);

  const cambiar = <K extends keyof T>(campo: K, valor: T[K]) =>
    setValores((anteriores) => ({ ...anteriores, [campo]: valor }));

  const enlazar = <K extends keyof T>(campo: K) => ({
    name: String(campo),
    value: valores[campo],
    onChange: (evento: ChangeEvent<Elemento>) => cambiar(campo, evento.target.value as T[K]),
  });

  return { valores, cambiar, enlazar };
}
