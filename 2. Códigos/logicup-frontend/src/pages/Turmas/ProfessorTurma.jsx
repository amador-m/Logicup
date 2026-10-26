import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import Header from "../../components/layout/Header";
import Footer from "../../components/layout/Footer";
import Confirmacao from "../../components/ui/Confirmacao";
import LinhaRanking from "../../components/ui/LinhaRanking";
import { useAuth } from "../../context/useAuth";
import { trilhaApi, turmaApi } from "../../services/api";
import { podeGerenciarTurmas } from "../../util/papeis";
import AvatarUsuario from "../../components/ui/AvatarUsuario";
import EstadoVazio from "../../components/ui/EstadoVazio";
import { copiar, formatarData, hojeIso, plural } from "./formatos";
import {
    Page, Hero, Quadro, Aviso, BotaoAcao, Lista, Secao, Formulario, ErroCampo, CodigoConvite, BotaoPequeno,
    Abas, TabelaRolavel, ListaTarefas, ItemTarefa, Selo,
} from "./styles";

const ABAS = [
    { id: "alunos", rotulo: "Alunos" },
    { id: "tarefas", rotulo: "Tarefas" },
    { id: "ranking", rotulo: "Ranking" },
    { id: "config", rotulo: "Configurações" },
];

// ---------------------------------------------------------------- abas

const AbaAlunos = ({ turma, aoRemover }) => {
    if (turma.alunos.length === 0) {
        return (
            <EstadoVazio titulo="Nenhum aluno na turma ainda.">
                Compartilhe o código <strong>{turma.codigo}</strong>: o aluno entra em "Minha turma" e digita o código.
            </EstadoVazio>
        );
    }
    return (
        <TabelaRolavel tabIndex={0} role="region" aria-label="Alunos da turma (role para os lados no celular)">
            <table>
                <thead>
                    <tr>
                        <th scope="col">Aluno</th>
                        <th scope="col">E-mail</th>
                        <th scope="col">XP</th>
                        <th scope="col">Fase</th>
                        <th scope="col">Rodadas aprovadas</th>
                        <th scope="col">Última atividade</th>
                        <th scope="col">Ações</th>
                    </tr>
                </thead>
                <tbody>
                    {turma.alunos.map((aluno) => (
                        <tr key={aluno.id}>
                            <td>
                                {/* Sprint 9: avatar escolhido pelo aluno ao lado do nome */}
                                <span className="aluno">
                                    <AvatarUsuario codigo={aluno.avatar} tamanho={36} />
                                    <span>{aluno.nome}<small>@{aluno.username}</small></span>
                                </span>
                            </td>
                            <td>{aluno.email}</td>
                            <td className="numero">{aluno.xp}</td>
                            <td className="numero">{aluno.nivel}</td>
                            <td className="numero">{aluno.rodadasAprovadas}</td>
                            <td>{formatarData(aluno.ultimaAtividade) ?? "Ainda não estudou"}</td>
                            <td>
                                <BotaoPequeno type="button" className="perigo" onClick={() => aoRemover(aluno)}
                                              aria-label={`Remover ${aluno.nome} da turma`}>
                                    Remover
                                </BotaoPequeno>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </TabelaRolavel>
    );
};

const AbaTarefas = ({ turma, fases, aoCriar, aoRemover }) => {
    const [rodadaId, setRodadaId] = useState("");
    const [prazo, setPrazo] = useState("");
    const [erro, setErro] = useState("");
    const [enviando, setEnviando] = useState(false);

    const jaAtribuidas = new Set(turma.tarefas.map((tarefa) => tarefa.rodadaId));
    const total = turma.alunos.length;

    const criar = async (evento) => {
        evento.preventDefault();
        if (!rodadaId) {
            setErro("Escolha a rodada.");
            return;
        }
        setErro("");
        setEnviando(true);
        try {
            await aoCriar(Number(rodadaId), prazo || null);
            setRodadaId("");
            setPrazo("");
        } catch (e) {
            setErro(e.message);
        } finally {
            setEnviando(false);
        }
    };

    return (
        <>
            <Secao>
                <h2>Atribuir rodada</h2>
                <p className="ajuda">
                    A tarefa aponta para uma rodada da trilha. Ela aparece como concluída quando o aluno aprova a
                    rodada, e como bloqueada se ele ainda não chegou nela. O prazo é só um lembrete.
                </p>
                <Formulario onSubmit={criar} noValidate>
                    <label htmlFor="tarefa-rodada">
                        Rodada
                        <select id="tarefa-rodada" value={rodadaId} onChange={(e) => setRodadaId(e.target.value)}>
                            <option value="">Escolha...</option>
                            {fases.map((fase) => (
                                <optgroup key={fase.id} label={`Fase ${fase.ordem} — ${fase.titulo}`}>
                                    {fase.rodadas.map((rodada) => (
                                        <option key={rodada.id} value={rodada.id} disabled={jaAtribuidas.has(rodada.id)}>
                                            Rodada {rodada.ordem}: {rodada.titulo}{jaAtribuidas.has(rodada.id) ? " (já atribuída)" : ""}
                                        </option>
                                    ))}
                                </optgroup>
                            ))}
                        </select>
                    </label>
                    <label htmlFor="tarefa-prazo">
                        Prazo (opcional)
                        <input id="tarefa-prazo" type="date" min={hojeIso()} value={prazo}
                               onChange={(e) => setPrazo(e.target.value)} />
                    </label>
                    <BotaoAcao type="submit" disabled={enviando}>{enviando ? "Atribuindo..." : "Atribuir"}</BotaoAcao>
                </Formulario>
                {erro && <ErroCampo role="alert">{erro}</ErroCampo>}
            </Secao>

            <Secao>
                <h2>Tarefas da turma</h2>
                {turma.tarefas.length === 0 && <Aviso>Nenhuma tarefa atribuída.</Aviso>}
                {turma.tarefas.length > 0 && (
                    <ListaTarefas>
                        {turma.tarefas.map((tarefa) => (
                            <ItemTarefa key={tarefa.id}>
                                <div className="texto">
                                    <span className="fase">{tarefa.fase}</span>
                                    <h3>{tarefa.rodada}</h3>
                                    <span className="detalhe">
                                        {tarefa.prazo ? `Prazo: ${formatarData(tarefa.prazo)}` : "Sem prazo"}
                                    </span>
                                </div>
                                <Selo className={total > 0 && tarefa.concluiram === total ? "CONCLUIDA" : ""}>
                                    {tarefa.concluiram} de {plural(total, "aluno concluiu", "alunos concluíram")}
                                </Selo>
                                <BotaoPequeno type="button" className="perigo" onClick={() => aoRemover(tarefa)}
                                              aria-label={`Remover a tarefa ${tarefa.rodada}`}>
                                    Remover
                                </BotaoPequeno>
                            </ItemTarefa>
                        ))}
                    </ListaTarefas>
                )}
            </Secao>
        </>
    );
};

const AbaRanking = ({ turmaId }) => {
    const [ranking, setRanking] = useState(null);
    const [erro, setErro] = useState(null);

    useEffect(() => {
        let ativo = true;
        turmaApi.ranking(turmaId)
            .then((dados) => ativo && setRanking(dados))
            .catch((e) => ativo && setErro(e.message));
        return () => {
            ativo = false;
        };
    }, [turmaId]);

    if (erro) return <Aviso role="alert" className="error">{erro}</Aviso>;
    if (!ranking) return <Aviso role="status">Carregando ranking...</Aviso>;
    if (ranking.posicoes.length === 0) {
        return <EstadoVazio titulo="Ranking vazio por enquanto.">Quando os alunos entrarem, eles aparecem aqui.</EstadoVazio>;
    }
    return (
        <Lista>
            {ranking.posicoes.map((item) => <LinhaRanking key={item.username} item={item} />)}
        </Lista>
    );
};

const AbaConfiguracoes = ({ turma, aoRenomear, aoExcluir }) => {
    const [nome, setNome] = useState(turma.nome);
    const [erro, setErro] = useState("");
    const [salvo, setSalvo] = useState(false);

    const renomear = async (evento) => {
        evento.preventDefault();
        setErro("");
        setSalvo(false);
        try {
            await aoRenomear(nome);
            setSalvo(true);
        } catch (e) {
            setErro(e.campos?.nome ?? e.message);
        }
    };

    return (
        <>
            <Secao>
                <h2>Nome da turma</h2>
                <Formulario onSubmit={renomear} noValidate>
                    <label htmlFor="turma-nome">
                        Nome
                        <input id="turma-nome" value={nome} maxLength={100}
                               onChange={(e) => { setNome(e.target.value); setSalvo(false); }} />
                    </label>
                    <BotaoAcao type="submit">Salvar</BotaoAcao>
                </Formulario>
                {erro && <ErroCampo role="alert">{erro}</ErroCampo>}
                {salvo && <p role="status">Nome atualizado.</p>}
            </Secao>

            <Secao>
                <h2>Excluir turma</h2>
                <p className="ajuda">
                    Apaga a turma, as matrículas e as tarefas. O progresso dos alunos na trilha continua com eles,
                    e eles poderão entrar em outra turma.
                </p>
                <BotaoPequeno type="button" className="perigo" onClick={aoExcluir}>Excluir turma</BotaoPequeno>
            </Secao>
        </>
    );
};

// ---------------------------------------------------------------- página

/*
 * Sprint 9: uma turma do professor. Mostra o código de convite e quatro abas.
 * Toda ação que não tem volta passa pela janela de Confirmacao.
 * Depois de qualquer mudança, recarrega o detalhe do backend (fonte da verdade), em vez de
 * tentar "adivinhar" o novo estado na tela.
 */
const ProfessorTurma = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const { usuario } = useAuth();
    const podeVer = podeGerenciarTurmas(usuario);

    const [turma, setTurma] = useState(null);
    const [fases, setFases] = useState([]);
    const [erro, setErro] = useState(null);
    const [naoEncontrada, setNaoEncontrada] = useState(false);
    const [versao, setVersao] = useState(0);
    const [aba, setAba] = useState("alunos");
    const [confirmacao, setConfirmacao] = useState(null); // { titulo, mensagem, textoConfirmar, exigirTexto, acao }
    const [copiado, setCopiado] = useState("");

    useEffect(() => {
        if (!podeVer) return undefined;
        let ativo = true;
        turmaApi.detalhar(id)
            .then((dados) => ativo && setTurma(dados))
            .catch((e) => {
                if (!ativo) return;
                if (e.status === 404) setNaoEncontrada(true);
                else setErro(e.message);
            });
        return () => {
            ativo = false;
        };
    }, [podeVer, id, versao]);

    // as rodadas da trilha, para o professor escolher a tarefa (para professor, todas vêm liberadas)
    useEffect(() => {
        if (!podeVer) return undefined;
        let ativo = true;
        trilhaApi.trilha()
            .then((dados) => ativo && setFases(dados.fases))
            .catch(() => {}); // sem a lista, só o formulário de tarefa fica vazio
        return () => {
            ativo = false;
        };
    }, [podeVer]);

    const recarregar = () => setVersao((n) => n + 1);

    const pedirConfirmacao = (config) => setConfirmacao(config);
    const executar = async () => {
        await confirmacao.acao();
        setConfirmacao(null);
    };

    /*
     * turma?.codigo (e não turma.codigo): o React Compiler do projeto memoriza esta função e lê as
     * dependências dela JÁ na renderização — inclusive na primeira, quando a turma ainda é null.
     */
    const codigoAtual = turma?.codigo;
    const copiarCodigo = async () => {
        setCopiado((await copiar(codigoAtual)) ? "Código copiado!" : "Não foi possível copiar. Selecione e copie o código.");
    };

    if (!podeVer || naoEncontrada || erro) {
        return (
            <>
                <Header />
                <Page>
                    <Hero><div><span>Área do professor</span><h1>Turma</h1></div></Hero>
                    <Quadro>
                        <Aviso role={erro ? "alert" : undefined} className={erro ? "error" : ""}>
                            {!podeVer && <strong>Esta área é exclusiva para professores.</strong>}
                            {podeVer && naoEncontrada && <strong>Turma não encontrada.</strong>}
                            {podeVer && erro && <p>{erro}</p>}
                            <div>
                                <BotaoAcao type="button" onClick={() => navigate(podeVer ? "/professor/turmas" : "/trilha")}>
                                    {podeVer ? "Voltar para minhas turmas" : "Ir para a trilha"}
                                </BotaoAcao>
                            </div>
                        </Aviso>
                    </Quadro>
                </Page>
                <Footer />
            </>
        );
    }

    return (
        <>
            <Header />
            <Page>
                <Hero>
                    <div>
                        <span><Link to="/professor/turmas">Minhas turmas</Link> / Turma</span>
                        <h1>{turma?.nome ?? "Carregando..."}</h1>
                        {turma && <p>{plural(turma.alunosAtivos, "aluno na turma", "alunos na turma")}.</p>}
                    </div>
                    {turma && (
                        <CodigoConvite aria-label="Código de convite">
                            <span>Código de convite</span>
                            <code>{turma.codigo}</code>
                            <div className="acoes">
                                <BotaoPequeno type="button" onClick={copiarCodigo}>Copiar</BotaoPequeno>
                                <BotaoPequeno type="button" onClick={() => pedirConfirmacao({
                                    titulo: "Gerar um novo código?",
                                    mensagem: "O código atual deixa de funcionar na hora. Quem já está na turma continua nela.",
                                    textoConfirmar: "Gerar novo código",
                                    acao: async () => { await turmaApi.novoCodigo(turma.id); setCopiado(""); recarregar(); },
                                })}>
                                    Gerar outro
                                </BotaoPequeno>
                            </div>
                            <p role="status" aria-live="polite">{copiado}</p>
                        </CodigoConvite>
                    )}
                </Hero>

                <Quadro aria-label="Detalhes da turma">
                    {!turma && <Aviso role="status">Carregando turma...</Aviso>}

                    {turma && (
                        <>
                            <Abas role="tablist" aria-label="Seções da turma">
                                {ABAS.map((item) => (
                                    <button key={item.id} type="button" role="tab" id={`aba-${item.id}`}
                                            aria-selected={aba === item.id} aria-controls={`painel-${item.id}`}
                                            onClick={() => setAba(item.id)}>
                                        {item.rotulo}
                                    </button>
                                ))}
                            </Abas>

                            <div role="tabpanel" id={`painel-${aba}`} aria-labelledby={`aba-${aba}`}>
                                {aba === "alunos" && (
                                    <AbaAlunos turma={turma} aoRemover={(aluno) => pedirConfirmacao({
                                        titulo: `Remover ${aluno.nome}?`,
                                        mensagem: "O aluno sai da turma e não consegue voltar pelo código. O progresso dele na trilha não muda.",
                                        textoConfirmar: "Remover aluno",
                                        acao: async () => { await turmaApi.removerAluno(turma.id, aluno.id); recarregar(); },
                                    })} />
                                )}
                                {aba === "tarefas" && (
                                    <AbaTarefas turma={turma} fases={fases}
                                                aoCriar={async (rodadaId, prazo) => {
                                                    await turmaApi.criarTarefa(turma.id, rodadaId, prazo);
                                                    recarregar();
                                                }}
                                                aoRemover={(tarefa) => pedirConfirmacao({
                                                    titulo: "Remover a tarefa?",
                                                    mensagem: `"${tarefa.rodada}" deixa de ser tarefa da turma. O que os alunos já fizeram na rodada continua valendo.`,
                                                    textoConfirmar: "Remover tarefa",
                                                    acao: async () => { await turmaApi.removerTarefa(turma.id, tarefa.id); recarregar(); },
                                                })} />
                                )}
                                {aba === "ranking" && <AbaRanking turmaId={turma.id} key={versao} />}
                                {aba === "config" && (
                                    <AbaConfiguracoes turma={turma}
                                                      aoRenomear={async (nome) => { await turmaApi.renomear(turma.id, nome); recarregar(); }}
                                                      aoExcluir={() => pedirConfirmacao({
                                                          titulo: "Excluir a turma?",
                                                          mensagem: "Isso apaga a turma, as matrículas e as tarefas, e não pode ser desfeito.",
                                                          textoConfirmar: "Excluir turma",
                                                          exigirTexto: turma.nome,
                                                          acao: async () => {
                                                              await turmaApi.excluir(turma.id);
                                                              navigate("/professor/turmas", { replace: true });
                                                          },
                                                      })} />
                                )}
                            </div>
                        </>
                    )}
                </Quadro>
            </Page>
            <Footer />

            {confirmacao && (
                <Confirmacao titulo={confirmacao.titulo} mensagem={confirmacao.mensagem}
                             textoConfirmar={confirmacao.textoConfirmar} exigirTexto={confirmacao.exigirTexto}
                             aoConfirmar={executar} aoCancelar={() => setConfirmacao(null)} />
            )}
        </>
    );
};

export default ProfessorTurma;
