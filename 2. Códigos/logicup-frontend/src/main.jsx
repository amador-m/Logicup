import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import App from "./App.jsx";

/*
 * Sprint 8: fonte Poppins servida pelo PRÓPRIO site (pacote @fontsource/poppins, licença SIL OFL 1.1).
 * Antes vinha do Google Fonts: cada visita fazia o navegador do usuário contatar um servidor de terceiro.
 * Mesmos pesos que eram pedidos ao Google (300 a 800), para a aparência não mudar.
 */
import "@fontsource/poppins/300.css";
import "@fontsource/poppins/400.css";
import "@fontsource/poppins/500.css";
import "@fontsource/poppins/600.css";
import "@fontsource/poppins/700.css";
import "@fontsource/poppins/800.css";

createRoot(document.getElementById("root")).render(
    <StrictMode>
        <App />
    </StrictMode>
);