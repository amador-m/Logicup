import { createContext } from "react";

/*
 * O objeto do contexto fica num arquivo separado do componente AuthProvider
 * para o "Fast Refresh" do Vite funcionar (a regra react-refresh/only-export-components
 * pede que arquivos .jsx exportem apenas componentes).
 */
export const AuthContext = createContext(null);
