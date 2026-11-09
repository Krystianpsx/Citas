import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { crearPuertosHttp } from "./infraestructura/http/adaptadoresHttp";
import { App } from "./ui/App";
import { ProveedorDePuertos } from "./ui/contexto/ContextoDePuertos";
import "./ui/estilos.css";

// Raíz de composición del frontend: aquí, y solo aquí, se eligen los adaptadores.
const puertos = crearPuertosHttp(import.meta.env.VITE_API_URL ?? "/api");

createRoot(document.getElementById("raiz")!).render(
  <StrictMode>
    <ProveedorDePuertos puertos={puertos}>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </ProveedorDePuertos>
  </StrictMode>,
);
