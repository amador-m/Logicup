import { useState } from "react";
import { Link } from "react-router-dom";
import { CHAVE_AVISO_COOKIES } from "../../../data/legal/cookies";
import { VERSAO_COOKIES } from "../../../data/legal/versoes";
import { Faixa, BotaoEntendi } from "./styles";

/*
 * Sprint 8: aviso de cookies. É INFORMATIVO (não pede autorização), porque o LogiCup só usa cookies
 * estritamente necessários: não há nada opcional para a pessoa aceitar ou recusar.
 *
 * "Entendi" grava no localStorage a VERSÃO da Política de Cookies que a pessoa viu.
 * Se a política mudar de versão, o aviso volta a aparecer.
 * O localStorage pode falhar (aba anônima, armazenamento bloqueado): aí o aviso só some até recarregar.
 */
function jaViuEstaVersao() {
    try {
        return localStorage.getItem(CHAVE_AVISO_COOKIES) === VERSAO_COOKIES;
    } catch {
        return false;
    }
}

const AvisoCookies = () => {
    const [visivel, setVisivel] = useState(() => !jaViuEstaVersao());

    if (!visivel) return null;

    const entendi = () => {
        try {
            localStorage.setItem(CHAVE_AVISO_COOKIES, VERSAO_COOKIES);
        } catch {
            // sem armazenamento: esconde só nesta visita
        }
        setVisivel(false);
    };

    return (
        <Faixa role="region" aria-label="Aviso de cookies">
            <p>
                O LogiCup usa apenas cookies necessários para você entrar na conta com segurança.
                Não usamos cookies de publicidade nem de rastreamento.{" "}
                <Link to="/cookies">Saiba mais</Link>
            </p>
            <BotaoEntendi type="button" onClick={entendi}>Entendi</BotaoEntendi>
        </Faixa>
    );
};

export default AvisoCookies;
