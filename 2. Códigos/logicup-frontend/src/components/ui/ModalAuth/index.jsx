import { useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { Dialogo, BotaoFechar } from "./styles";

/*
 * Modal de login/cadastro (Sprint 7). As rotas /login e /cadastro continuam existindo:
 * elas desenham a Home ao fundo e este modal por cima (ver AppRoutes).
 * Assim todos os links para /login e /cadastro, e o redirecionamento do PrivateRoute, continuam iguais.
 *
 * Usa o elemento NATIVO <dialog> com showModal(). O próprio navegador já cuida de:
 *  - prender o foco dentro do modal (Tab não "escapa" para a página de fundo);
 *  - fechar com a tecla Esc (evento "cancel");
 *  - desenhar o fundo escurecido (::backdrop) e deixar o resto da página inerte.
 * Fechar o modal = voltar para a Home ("/").
 */
const ModalAuth = ({ tituloId, children }) => {
    const dialogoRef = useRef(null);
    const navigate = useNavigate();

    useEffect(() => {
        const dialogo = dialogoRef.current;
        if (dialogo && !dialogo.open) {
            dialogo.showModal();
            // sozinho, o showModal() focaria o primeiro elemento focável (um link do painel lateral);
            // levamos o foco direto para o primeiro campo do formulário
            dialogo.querySelector("input")?.focus();
        }
        // trava a rolagem da página de fundo enquanto o modal está aberto
        const overflowAnterior = document.body.style.overflow;
        document.body.style.overflow = "hidden";
        return () => {
            document.body.style.overflow = overflowAnterior;
            if (dialogo?.open) dialogo.close();
        };
    }, []);

    const fechar = () => navigate("/", { replace: true });

    // clique no fundo escuro (fora do conteúdo) fecha; clique dentro do formulário não
    const aoClicar = (evento) => {
        if (evento.target === dialogoRef.current) {
            fechar();
        }
    };

    return (
        <Dialogo
            ref={dialogoRef}
            aria-labelledby={tituloId}
            onCancel={(evento) => { evento.preventDefault(); fechar(); }} // Esc
            onClick={aoClicar}
        >
            {children}
            <BotaoFechar type="button" onClick={fechar} aria-label="Fechar e voltar para a página inicial">
                ✕
            </BotaoFechar>
        </Dialogo>
    );
};

export default ModalAuth;
