import { useCallback, useEffect, useMemo, useState } from "react";
import { AuthContext } from "./authContextObject";
import { authApi, legalApi, definirAoPerderSessao, definirAoExigirAceite } from "../services/api";
import { VERSAO_TERMOS, VERSAO_PRIVACIDADE } from "../data/legal/versoes";

/*
 * Guarda QUEM está logado para o app inteiro.
 * O React não consegue ler o cookie httpOnly; por isso, ao abrir o site, pergunta ao backend (/auth/me).
 *   usuario    = { id, nome, username, email, role, aceitePendente } ou null
 *                (Sprint 8: aceitePendente = true -> falta aceitar a versão vigente dos Termos/Privacidade)
 *   carregando = true enquanto a primeira verificação não termina (evita "piscar" a tela de login)
 */
export function AuthProvider({ children }) {
    const [usuario, setUsuario] = useState(null);
    const [carregando, setCarregando] = useState(true);

    useEffect(() => {
        let ativo = true; // evita atualizar estado depois que o componente saiu da tela

        definirAoPerderSessao(() => setUsuario(null)); // sessão expirou em qualquer chamada -> desloga
        // Sprint 8: o backend respondeu 403 ACEITE_PENDENTE -> marca a pendência e a janela de aceite aparece
        definirAoExigirAceite(() =>
            setUsuario((atual) => (atual && !atual.aceitePendente ? { ...atual, aceitePendente: true } : atual)));

        authApi.me()
            .then((dados) => { if (ativo) setUsuario(dados); })
            .catch(() => { if (ativo) setUsuario(null); })   // 401 = ninguém logado (normal)
            .finally(() => { if (ativo) setCarregando(false); });

        return () => {
            ativo = false;
            definirAoPerderSessao(null);
            definirAoExigirAceite(null);
        };
    }, []);

    const login = useCallback(async (identificacao, senha) => {
        const dados = await authApi.login(identificacao, senha); // e-mail ou nome de usuário
        setUsuario(dados);
        return dados;
    }, []);

    /*
     * Sprint 10 (decisão C): o cadastro cria a conta mas NÃO loga. Devolve { email, mensagem } para a tela
     * mostrar "verifique seu e-mail". A pessoa entra ao abrir o link (confirmarEmail, abaixo).
     */
    const cadastrar = useCallback((dadosCadastro) => authApi.cadastrar(dadosCadastro), []);

    /* Sprint 10: abriu o link do e-mail -> o backend confirma, devolve o usuário e o cookie de login. */
    const confirmarEmail = useCallback(async (token) => {
        const dados = await authApi.confirmarEmail(token);
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

    /*
     * Sprint 8: aceite de quem já tem conta. O backend devolve o usuário com aceitePendente = false
     * e um cookie novo; trocar o usuário aqui fecha a janela de aceite e libera as páginas.
     */
    const aceitarDocumentos = useCallback(async () => {
        const dados = await legalApi.aceitar(VERSAO_TERMOS, VERSAO_PRIVACIDADE);
        setUsuario(dados);
        return dados;
    }, []);

    const valor = useMemo(
        () => ({ usuario, carregando, login, cadastrar, confirmarEmail, logout, atualizarUsuario, aceitarDocumentos }),
        [usuario, carregando, login, cadastrar, confirmarEmail, logout, atualizarUsuario, aceitarDocumentos]
    );

    return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}
