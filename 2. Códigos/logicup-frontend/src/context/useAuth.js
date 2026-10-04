import { useContext } from "react";
import { AuthContext } from "./authContextObject";

/** Hook para qualquer componente acessar { usuario, carregando, login, cadastrar, logout }. */
export function useAuth() {
    const contexto = useContext(AuthContext);
    if (!contexto) {
        throw new Error("useAuth precisa ser usado dentro de <AuthProvider>.");
    }
    return contexto;
}
