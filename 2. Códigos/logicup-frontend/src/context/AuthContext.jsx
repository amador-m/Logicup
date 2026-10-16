import { useCallback, useEffect, useMemo, useState } from "react";
import { AuthContext } from "./authContextObject";
import { authApi, definirAoPerderSessao } from "../services/api";

/*
 * Guarda QUEM está logado para o app inteiro.
 * O React não consegue ler o cookie httpOnly; por isso, ao abrir o site, pergunta ao backend (/auth/me).
 *   usuario    = { id, nome, username, email, role } ou null
 *   carregando = true enquanto a primeira verificação não termina (evita "piscar" a tela de login)
 */
export function AuthProvider({ children }) {
    const [usuario, setUsuario] = useState(null);
    const [carregando, setCarregando] = useState(true);

    useEffect(() => {
        let ativo = true; // evita atualizar estado depois que o componente saiu da tela

        definirAoPerderSessao(() => setUsuario(null)); // sessão expirou em qualquer chamada -> desloga

        authApi.me()
            .then((dados) => { if (ativo) setUsuario(dados); })
            .catch(() => { if (ativo) setUsuario(null); })   // 401 = ninguém logado (normal)
            .finally(() => { if (ativo) setCarregando(false); });

        return () => {
            ativo = false;
            definirAoPerderSessao(null);
        };
    }, []);

    const login = useCallback(async (identificacao, senha) => {
        const dados = await authApi.login(identificacao, senha); // e-mail ou nome de usuário
        setUsuario(dados);
        return dados;
    }, []);

    const cadastrar = useCallback(async (dadosCadastro) => {
        const dados = await authApi.cadastrar(dadosCadastro); // C1: o cadastro já deixa logado
        setUsuario(dados);
        return dados;
    }, []);

    const logout = useCallback(async () => {
        try {
            await authApi.logout(); // só o backend consegue apagar o cookie httpOnly
        } finally {
            setUsuario(null); // mesmo se a chamada falhar, a tela deixa de mostrar a pessoa logada
        }
    }, []);

    /*
     * Sprint 7: depois de editar o perfil, o backend devolve o usuário atualizado e trocamos aqui.
     * Como o contexto é compartilhado, o "Olá, ..." do Header muda na hora, sem recarregar a página.
     */
    const atualizarUsuario = useCallback((dados) => setUsuario(dados), []);

    const valor = useMemo(
        () => ({ usuario, carregando, login, cadastrar, logout, atualizarUsuario }),
        [usuario, carregando, login, cadastrar, logout, atualizarUsuario]
    );

    return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}
