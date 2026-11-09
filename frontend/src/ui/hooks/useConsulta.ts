import { useCallback, useEffect, useState, type DependencyList } from "react";
import { mensajeDeError } from "../mensajes";

interface EstadoDeConsulta<T> {
  datos?: T;
  error?: string;
  cargando: boolean;
}

/** Carga datos asíncronos y expone { datos, error, cargando, recargar }.
 *  Ignora respuestas que llegan tarde si las dependencias ya cambiaron. */
export function useConsulta<T>(consultar: () => Promise<T>, dependencias: DependencyList) {
  const [estado, setEstado] = useState<EstadoDeConsulta<T>>({ cargando: true });
  const [version, setVersion] = useState(0);

  useEffect(() => {
    let vigente = true;
    setEstado((anterior) => ({ ...anterior, cargando: true, error: undefined }));
    consultar()
      .then((datos) => vigente && setEstado({ datos, cargando: false }))
      .catch((error) => vigente && setEstado({ cargando: false, error: mensajeDeError(error) }));
    return () => {
      vigente = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [...dependencias, version]);

  const recargar = useCallback(() => setVersion((v) => v + 1), []);
  return { ...estado, recargar };
}
