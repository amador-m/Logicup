import { Navigate, useLocation } from "react-router-dom";
import styled from "styled-components";
import { useAuth } from "../context/useAuth";

const Carregando = styled.p`
    min-height: 60vh;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    color: var(--brown-dark);
`;

/*
 * Protege rotas que exigem login.
 * ATENÇÃO: isto é só experiência do usuário. A proteção REAL é o backend respondendo 401/403.
 */
const PrivateRoute = ({ children }) => {
    const { usuario, carregando } = useAuth();
    const location = useLocation();

    if (carregando) {
        return <Carregando>Carregando...</Carregando>;
    }
    if (!usuario) {
        // guarda de onde a pessoa veio para devolvê-la lá depois do login
        return <Navigate to="/login" replace state={{ from: location }} />;
    }
    if (usuario.aceitePendente) {
        // Sprint 8: com o aceite pendente, o backend recusa a API desta página (403).
        // Não montamos a página para ela não carregar dados que vão falhar: a janela de aceite
        // (ModalAceite, no AppRoutes) aparece por cima e, ao aceitar, a página carrega normalmente.
        return <Carregando>Aguardando o aceite dos Termos de Uso...</Carregando>;
    }
    return children;
};

export default PrivateRoute;
