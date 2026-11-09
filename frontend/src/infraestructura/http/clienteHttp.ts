export class ErrorDeApi extends Error {
  readonly estadoHttp: number;

  constructor(mensaje: string, estadoHttp: number) {
    super(mensaje);
    this.name = "ErrorDeApi";
    this.estadoHttp = estadoHttp;
  }
}

type Parametros = Record<string, string | undefined>;

/** Envoltorio mínimo de fetch: arma la URL, envía JSON y convierte los errores
 *  del backend ({ mensaje, tipo }) en ErrorDeApi con un mensaje legible. */
export class ClienteHttp {
  private readonly urlBase: string;

  constructor(urlBase: string) {
    this.urlBase = urlBase.replace(/\/$/, "");
  }

  get<T>(ruta: string, parametros: Parametros = {}): Promise<T> {
    return this.solicitar<T>(`${ruta}${construirConsulta(parametros)}`, { method: "GET" });
  }

  post<T>(ruta: string, cuerpo: unknown = {}): Promise<T> {
    return this.solicitar<T>(ruta, { method: "POST", body: JSON.stringify(cuerpo) });
  }

  private async solicitar<T>(ruta: string, opciones: RequestInit): Promise<T> {
    const respuesta = await this.enviar(ruta, opciones);
    const datos = await respuesta.json().catch(() => null);
    if (!respuesta.ok) {
      throw new ErrorDeApi(datos?.mensaje ?? `El servidor respondió con el código ${respuesta.status}.`, respuesta.status);
    }
    return datos as T;
  }

  private async enviar(ruta: string, opciones: RequestInit): Promise<Response> {
    try {
      return await fetch(`${this.urlBase}${ruta}`, {
        ...opciones,
        headers: { "Content-Type": "application/json", Accept: "application/json" },
      });
    } catch {
      throw new ErrorDeApi("No hay conexión con el servidor. Revisa que el backend esté en ejecución.", 0);
    }
  }
}

function construirConsulta(parametros: Parametros): string {
  const presentes = Object.entries(parametros).filter((par): par is [string, string] => Boolean(par[1]));
  return presentes.length ? `?${new URLSearchParams(presentes)}` : "";
}
