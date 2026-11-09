import { NavLink, Navigate, Route, Routes } from "react-router-dom";
import { PaginaAgenda } from "./agenda/PaginaAgenda";
import { PaginaMedicos } from "./medicos/PaginaMedicos";
import { PaginaPacientes } from "./pacientes/PaginaPacientes";

export function App() {
  return (
    <div className="aplicacion">
      <header className="barra">
        <span className="barra__marca">Clínica Santa Clara</span>
        <nav className="barra__navegacion" aria-label="Secciones">
          <NavLink to="/" end>
            Agenda
          </NavLink>
          <NavLink to="/pacientes">Pacientes</NavLink>
          <NavLink to="/medicos">Médicos</NavLink>
        </nav>
      </header>
      <main className="principal">
        <Routes>
          <Route path="/" element={<PaginaAgenda />} />
          <Route path="/pacientes" element={<PaginaPacientes />} />
          <Route path="/medicos" element={<PaginaMedicos />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </div>
  );
}
