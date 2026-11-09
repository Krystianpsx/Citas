import { useCallback, useState } from "react";
import { mensajeDeError } from "../mensajes";

/** Ejecuta una operación que modifica datos y lleva la cuenta de si está en
 *  curso y del último error. Devuelve undefined si la operación falla. */
export function useAccion<Argumentos extends unknown[], Resultado>(
  accion: (...argumentos: Argumentos) => Promise<Resultado>,
) {
  const [enCurso, setEnCurso] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const ejecutar = useCallback(
    async (...argumentos: Argumentos): Promise<Resultado | undefined> => {
      setEnCurso(true);
      setError(null);
      try {
        return await accion(...argumentos);
      } catch (causa) {
        setError(mensajeDeError(causa));
        return undefined;
      } finally {
        setEnCurso(false);
      }
    },
    [accion],
  );

  return { ejecutar, enCurso, error, limpiarError: () => setError(null) };
}
