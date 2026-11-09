export function AvisoDeError({ mensaje }: { mensaje?: string | null }) {
  if (!mensaje) return null;
  return (
    <p className="aviso aviso--error" role="alert">
      {mensaje}
    </p>
  );
}

export function AvisoDeExito({ mensaje }: { mensaje?: string | null }) {
  return (
    <p className="aviso aviso--exito" role="status">
      {mensaje}
    </p>
  );
}

export function Cargando({ texto = "Cargando…" }: { texto?: string }) {
  return <p className="cargando">{texto}</p>;
}
