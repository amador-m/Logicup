import { useEffect, useState } from "react";
import { authApi } from "../../../services/api";
import { BotaoGoogle, Separador } from "../../../pages/Login/styles";

// pergunta ao backend UMA vez por carregamento da página (o login e o cadastro usam o mesmo resultado)
let provedores = null;
function carregarProvedores() {
    provedores ??= authApi.provedores().catch(() => ({ google: false }));
    return provedores;
}

/*
 * Sprint 10: "Entrar com Google". É um LINK comum, e não um fetch: o navegador inteiro vai para o Google
 * e volta para o backend, que confere tudo e devolve a pessoa para a trilha (ou para completar o cadastro).
 * Só aparece se o backend tiver a chave do Google configurada (GET /api/auth/provedores).
 */
/*
 * O "G" colorido OFICIAL do Google. As regras de marca do botão "Entrar com Google" exigem esse G, nas cores
 * originais e sem alterar, sobre fundo claro, escuro ou neutro (developers.google.com/identity/branding-guidelines).
 * aria-hidden: o leitor de tela já lê o texto do botão; o desenho não precisa ser lido.
 */
const LogoGoogle = () => (
    <svg width="18" height="18" viewBox="0 0 48 48" aria-hidden="true" focusable="false">
        <path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z" />
        <path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z" />
        <path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z" />
        <path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z" />
    </svg>
);

const EntrarComGoogle = () => {
    const [ligado, setLigado] = useState(false);

    useEffect(() => {
        let ativo = true;
        carregarProvedores().then((p) => { if (ativo) setLigado(Boolean(p?.google)); });
        return () => { ativo = false; };
    }, []);

    if (!ligado) return null;
    return (
        <>
            <BotaoGoogle href="/oauth2/authorization/google">
                <LogoGoogle />
                <span>Entrar com Google</span>
            </BotaoGoogle>
            <Separador>ou</Separador>
        </>
    );
};

export default EntrarComGoogle;
