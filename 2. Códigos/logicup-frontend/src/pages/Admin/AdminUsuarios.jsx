import { useEffect, useState } from "react";
import { adminApi } from "../../services/api";
import Confirmacao from "../../components/ui/Confirmacao";
import { TabelaRolavel, BotaoPequeno, Selo, Filtros, Paginacao, Info } from "./styles";

const ROTULO_PERFIL = { ALUNO: "Aluno", PROFESSOR: "Professor", ADMIN: "Admin" };

/** "2026-11-17T10:20:30" -> "17/11/2026" (montado do texto, sem new Date, para não errar o dia pelo fuso). */
const dataCurta = (texto) => {
    if (!texto) return "—";
    const [ano, mes, dia] = texto.slice(0, 10).split("-");
    return `${dia}/${mes}/${ano}`;
};

/*
 * Sprint 11: GESTÃO DE USUÁRIOS (RF25). O admin lista, busca, filtra por perfil e bloqueia/desbloqueia.
 * A busca só é feita ao enviar o formulário (Enter ou "Buscar"), e não a cada letra digitada:
 * menos requisições ao servidor e a lista não "pula" enquanto a pessoa escreve.
 */
const AdminUsuarios = ({ adminId }) => {
    const [filtros, setFiltros] = useState({ busca: "", perfil: "", pagina: 0 });
    const [digitado, setDigitado] = useState("");
    const [perfilEscolhido, setPerfilEscolhido] = useState("");
    const [dados, setDados] = useState(null);
    const [erro, setErro] = useState("");
    const [confirmando, setConfirmando] = useState(null); // usuário que vai ser bloqueado/desbloqueado

    useEffect(() => {
        let ativo = true;
        adminApi.usuarios(filtros)
            .then((resposta) => ativo && (setDados(resposta), setErro("")))
            .catch((e) => ativo && setErro(e.message));
        return () => {
            ativo = false;
        };
    }, [filtros]);

    const buscar = (evento) => {
        evento.preventDefault();
        setFiltros({ busca: digitado, perfil: perfilEscolhido, pagina: 0 });
    };

    const alterarBloqueio = async () => {
        const usuario = confirmando;
        const atualizado = await adminApi.alterarBloqueio(usuario.id, !usuario.bloqueado);
        // troca só a linha alterada (sem buscar a lista inteira de novo)
        setDados((atual) => ({ ...atual, itens: atual.itens.map((u) => (u.id === atualizado.id ? atualizado : u)) }));
        setConfirmando(null);
    };

    return (
        <>
            <Filtros onSubmit={buscar} role="search">
                <label htmlFor="admin-busca">Buscar por nome, nome de usuário ou e-mail</label>
                <input id="admin-busca" type="search" value={digitado} maxLength={100}
                       placeholder="Buscar por nome, usuário ou e-mail" onChange={(e) => setDigitado(e.target.value)} />
                <label htmlFor="admin-perfil">Perfil</label>
                <select id="admin-perfil" value={perfilEscolhido} onChange={(e) => setPerfilEscolhido(e.target.value)}>
                    <option value="">Todos os perfis</option>
                    <option value="ALUNO">Alunos</option>
                    <option value="PROFESSOR">Professores</option>
                    <option value="ADMIN">Administradores</option>
                </select>
                <BotaoPequeno type="submit" className="claro">Buscar</BotaoPequeno>
            </Filtros>

            {erro && <Info className="erro" role="alert">{erro}</Info>}
            {!dados && !erro && <Info role="status">Carregando usuários...</Info>}

            {dados && (
                <>
                    <Info role="status">
                        {dados.total === 1 ? "1 usuário encontrado" : `${dados.total} usuários encontrados`}
                    </Info>
                    <TabelaRolavel>
                        <table>
                            <thead>
                                <tr>
                                    <th scope="col">Nome</th>
                                    <th scope="col">E-mail</th>
                                    <th scope="col">Perfil</th>
                                    <th scope="col">Cadastro</th>
                                    <th scope="col">Situação</th>
                                    <th scope="col">Ações</th>
                                </tr>
                            </thead>
                            <tbody>
                                {dados.itens.map((u) => (
                                    <tr key={u.id}>
                                        <td>
                                            <strong>{u.nome}</strong>
                                            <small>@{u.username}</small>
                                        </td>
                                        <td>
                                            {u.email}
                                            <small>
                                                {u.emailConfirmado ? "e-mail confirmado" : "e-mail não confirmado"}
                                                {u.entraComGoogle ? " • Google" : ""}
                                            </small>
                                        </td>
                                        <td>{ROTULO_PERFIL[u.perfil] ?? u.perfil}</td>
                                        <td>{dataCurta(u.dataCadastro)}</td>
                                        <td>
                                            {u.bloqueado
                                                ? <Selo className="atrasada">Bloqueada desde {dataCurta(u.bloqueadoEm)}</Selo>
                                                : <Selo className="CONCLUIDA">Ativa</Selo>}
                                        </td>
                                        <td>
                                            {u.perfil !== "ADMIN" && u.id !== adminId && (
                                                <BotaoPequeno type="button" className={u.bloqueado ? "claro" : "perigo"}
                                                              onClick={() => setConfirmando(u)}>
                                                    {u.bloqueado ? "Desbloquear" : "Bloquear"}
                                                </BotaoPequeno>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </TabelaRolavel>

                    {dados.totalPaginas > 1 && (
                        <Paginacao aria-label="Páginas">
                            <BotaoPequeno type="button" className="claro" disabled={dados.pagina === 0}
                                          onClick={() => setFiltros((f) => ({ ...f, pagina: f.pagina - 1 }))}>
                                Anterior
                            </BotaoPequeno>
                            <span>Página {dados.pagina + 1} de {dados.totalPaginas}</span>
                            <BotaoPequeno type="button" className="claro" disabled={dados.pagina + 1 >= dados.totalPaginas}
                                          onClick={() => setFiltros((f) => ({ ...f, pagina: f.pagina + 1 }))}>
                                Próxima
                            </BotaoPequeno>
                        </Paginacao>
                    )}
                </>
            )}

            {confirmando && (
                <Confirmacao
                    titulo={confirmando.bloqueado ? "Desbloquear a conta?" : "Bloquear a conta?"}
                    mensagem={confirmando.bloqueado
                        ? `${confirmando.nome} poderá entrar de novo, com todo o progresso.`
                        : `${confirmando.nome} não conseguirá mais entrar e perderá o acesso agora mesmo. Nada é apagado: dá para desbloquear depois.`}
                    textoConfirmar={confirmando.bloqueado ? "Desbloquear" : "Bloquear"}
                    aoConfirmar={alterarBloqueio}
                    aoCancelar={() => setConfirmando(null)}
                />
            )}
        </>
    );
};

export default AdminUsuarios;
