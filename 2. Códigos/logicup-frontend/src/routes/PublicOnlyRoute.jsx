import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/useAuth";

/*
 * F2: telas só para quem NÃO está logado (login e cadastro).
 * Se a pessoa já estiver logada, vai para onde queria ir (ou para a trilha).
 * Também é o que leva a pessoa à trilha logo depois de fazer login ou cadastro.
 */
const PublicOnlyRoute = ({ children }) => {
    const { usuario, carregando } = useAuth();
    const location = useLocation();

    if (carregando) {
        return null;
    }
    if (usuario) {
        const destino = location.state?.from?.pathname ?? "/trilha";
        return <Navigate to={destino} replace />;
    }
    return children;
};

export default PublicOnlyRoute;
