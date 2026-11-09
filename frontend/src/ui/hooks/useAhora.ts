import { useEffect, useState } from "react";

/** La hora actual, refrescada cada minuto (para la línea de "ahora" en la agenda). */
export function useAhora(intervaloMs = 60_000): Date {
  const [ahora, setAhora] = useState(() => new Date());
  useEffect(() => {
    const temporizador = window.setInterval(() => setAhora(new Date()), intervaloMs);
    return () => window.clearInterval(temporizador);
  }, [intervaloMs]);
  return ahora;
}
