import { useEffect, useRef, type ReactNode } from "react";

interface Props {
  titulo: string;
  abierto: boolean;
  alCerrar: () => void;
  children: ReactNode;
}

/** Panel lateral modal basado en <dialog>: el navegador se encarga del foco,
 *  de la tecla Escape y de dejar inerte el resto de la página. */
export function Cajon({ titulo, abierto, alCerrar, children }: Props) {
  const dialogo = useRef<HTMLDialogElement>(null);

  useEffect(() => {
    const elemento = dialogo.current;
    if (!elemento) return;
    if (abierto && !elemento.open) elemento.showModal();
    if (!abierto && elemento.open) elemento.close();
  }, [abierto]);

  return (
    <dialog
      ref={dialogo}
      className="cajon"
      aria-labelledby="titulo-del-cajon"
      onClose={alCerrar}
      onClick={(evento) => evento.target === dialogo.current && alCerrar()}
    >
      <div className="cajon__contenido">
        <header className="cajon__cabecera">
          <h2 id="titulo-del-cajon">{titulo}</h2>
          <button type="button" className="boton boton--fantasma" onClick={alCerrar}>
            Cerrar
          </button>
        </header>
        {abierto && children}
      </div>
    </dialog>
  );
}
