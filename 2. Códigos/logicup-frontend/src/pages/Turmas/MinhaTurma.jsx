import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Header from "../../components/layout/Header";
import Footer from "../../components/layout/Footer";
import Confirmacao from "../../components/ui/Confirmacao";
import LinhaRanking from "../../components/ui/LinhaRanking";
import { useAuth } from "../../context/useAuth";
import { minhaTurmaApi } from "../../services/api";
import { podeEntrarEmTurma } from "../../util/papeis";
import { avisarProgresso, pedirComemoracao } from "../../util/gamificacao";
import EstadoVazio from "../../components/ui/EstadoVazio";
import MascoteImg from "../../assets/images/MascoteRecortado.png";
import { formatarData, ROTULO_STATUS_TAREFA } from "./formatos";
import {
    Page, Hero, Quadro, Aviso, BotaoAcao, Lista, Secao, Formulario, ErroCampo, ListaTarefas, ItemTarefa, Selo,
    Mascote, BotaoPequeno, Abas,
} from "./styles";

/** Uma tarefa na visão do aluno. O status vem do backend (R11); a tela só escolhe o texto e a cor. */
const TarefaDoAluno = ({ tarefa, aoAbrirTrilha }) => {
    const classe = tarefa.status === "CONCLUIDA" ? "concluida" : tarefa.status === "BLOQUEADA" ? "bloqueada" : "";
    return (
        <ItemTarefa className={classe}>
            <div className="texto">
                <span className="fase">{tarefa.fase}</span>
                <h3>{tarefa.rodada}</h3>
                <span className="detalhe">
                    {tarefa.prazo ? `Prazo: ${formatarData(tarefa.prazo)}` : "Sem prazo"}
                    {tarefa.status === "BLOQUEADA" && " • Conclua as rodadas anteriores da trilha para liberar."}
                </span>
            </div>
            {tarefa.atrasada
                ? <Selo className="atrasada">Atrasada</Selo>
                : <Selo className={tarefa.status}>{ROTULO_STATUS_TAREFA[tarefa.status]}</Selo>}
            {tarefa.status === "PENDENTE" && (
                <BotaoPequeno type="button" className="claro" onClick={aoAbrirTrilha}>Ir para a trilha</BotaoPequeno>
            )}
        </ItemTarefa>
    );
};

/** Campo do código de convite: aparece sozinho (sem turma) ou embaixo das turmas ("Entrar em outra turma"). */
const EntrarComCodigo = ({ titulo, aoEntrar, separada = false }) => {
    const [codigo, setCodigo] = useState("");
    const [erroCodigo, setErroCodigo] = useState("");
    const [entrando, setEntrando] = useState(false);

    const entrar = async (evento) => {
        evento.preventDefault();
        setErroCodigo("");
        setEntrando(true);
        try {
            const turma = await minhaTurmaApi.entrar(codigo);
            setCodigo("");
            aoEntrar(turma);
        } catch (e) {
            setErroCodigo(e.campos?.codigo ?? e.message);
        } finally {
            setEntrando(false);
        }
    };

    return (
        <Secao className={separada ? "separada" : undefined}>
            <h2>{titulo}</h2>
            <p className="ajuda">São 8 letras e números, por exemplo AB3K9MZQ. Maiúsculas ou minúsculas, tanto faz.</p>
            <Formulario onSubmit={entrar} noValidate>
                <label htmlFor="codigo-turma">
                    Código da turma
                    <input id="codigo-turma" value={codigo} onChange={(e) => setCodigo(e.target.value)}
                           autoComplete="off" autoCapitalize="characters" spellCheck={false} maxLength={12}
                           aria-invalid={erroCodigo ? true : undefined}
                           aria-describedby={erroCodigo ? "codigo-erro" : undefined} />
                </label>
                <BotaoAcao type="submit" disabled={entrando}>{entrando ? "Entrando..." : "Entrar na turma"}</BotaoAcao>
            </Formulario>
            {erroCodigo && <ErroCampo id="codigo-erro" role="alert">{erroCodigo}</ErroCampo>}
        </Secao>
    );
};

/*
 * Sprint 9: as turmas do aluno. Sprint 11: o aluno pode estar em VÁRIAS turmas.
 *  - sem turma: campo para digitar o código de convite;
 *  - com turmas: uma aba por turma (só aparece com 2 ou mais), com as tarefas, o ranking e o botão de sair
 *    DAQUELA turma; embaixo, o campo para entrar em outra.
 */
const MinhaTurma = () => {
    const navigate = useNavigate();
    const { usuario } = useAuth();
    const podeVer = podeEntrarEmTurma(usuario);

    const [turmas, setTurmas] = useState(undefined); // undefined = carregando; [] = nenhuma turma
    const [selecionadaId, setSelecionadaId] = useState(null);
    const [ranking, setRanking] = useState({ turmaId: null, dados: null });
    const [erro, setErro] = useState(null);
    const [versao, setVersao] = useState(0);
    const [confirmarSaida, setConfirmarSaida] = useState(false);

    useEffect(() => {
        if (!podeVer) return undefined;
        let ativo = true;
        minhaTurmaApi.listar()
            .then((lista) => ativo && setTurmas(lista))
            .catch((e) => ativo && setErro(e.message));
        return () => {
            ativo = false;
        };
    }, [podeVer, versao]);

    // a turma aberta: a escolhida (se ainda estiver na lista) ou a primeira
    const turma = turmas?.find((t) => t.id === selecionadaId) ?? turmas?.[0] ?? null;
    const turmaId = turma?.id ?? null;

    useEffect(() => {
        if (turmaId === null) return undefined;
        let ativo = true;
        minhaTurmaApi.ranking(turmaId)
            .then((dados) => ativo && setRanking({ turmaId, dados }))
            .catch(() => ativo && setRanking({ turmaId, dados: { posicoes: [] } }));
        return () => {
            ativo = false;
        };
    }, [turmaId, versao]);

    // ranking de OUTRA turma ainda na tela (enquanto o novo carrega) não aparece
    const rankingDaTurma = ranking.turmaId === turmaId ? ranking.dados : null;

    const aoEntrar = (nova) => {
        setSelecionadaId(nova.id); // abre a turma em que acabou de entrar
        setVersao((n) => n + 1);
        // Sprint 9: entrar numa turma rende a conquista "Colega de turma"
        avisarProgresso();
        pedirComemoracao();
    };

    // usa turmaId (que pode ser null), e não turma.id: o React Compiler guarda esta função em cache usando o que
    // ela lê como chave, e ler turma.id com turma = null quebrava a tela de quem ainda não tem turma
    const sair = async () => {
        await minhaTurmaApi.sair(turmaId);
        setConfirmarSaida(false);
        setSelecionadaId(null);
        setVersao((n) => n + 1);
    };

    const varias = turmas?.length > 1;

    return (
        <>
            <Header />
            <Page>
                <Hero>
                    <div>
                        <span>{varias ? "Minhas turmas" : "Minha turma"}</span>
                        <h1>{turma ? turma.nome : "Entre na sua turma"}</h1>
                        <p>
                            {turma
                                ? `Professor(a): ${turma.professor ?? "—"}. Aqui ficam as tarefas da turma e o ranking só entre os colegas.`
                                : "Com o código que o professor passou, você entra na turma, vê as tarefas e o ranking da turma."}
                        </p>
                    </div>
                </Hero>

                <Quadro aria-label="Minhas turmas">
                    {!podeVer && (
                        <Aviso>
                            <strong>Esta área é para alunos.</strong>
                            <div><BotaoAcao type="button" onClick={() => navigate("/")}>Ir para o início</BotaoAcao></div>
                        </Aviso>
                    )}

                    {podeVer && erro && (
                        <Aviso role="alert" className="error">
                            <p>{erro}</p>
                            <BotaoAcao type="button" onClick={() => { setErro(null); setVersao((n) => n + 1); }}>
                                Tentar novamente
                            </BotaoAcao>
                        </Aviso>
                    )}

                    {podeVer && !erro && turmas === undefined && <Aviso role="status">Carregando...</Aviso>}

                    {podeVer && turmas?.length === 0 && (
                        <>
                            <Mascote src={MascoteImg} alt="" />
                            <EntrarComCodigo titulo="Tem um código de turma?" aoEntrar={aoEntrar} />
                        </>
                    )}

                    {turma && (
                        <>
                            {varias && (
                                <Abas role="tablist" aria-label="Suas turmas">
                                    {turmas.map((item) => (
                                        <button key={item.id} type="button" role="tab" id={`aba-turma-${item.id}`}
                                                aria-selected={item.id === turma.id} aria-controls="painel-turma"
                                                onClick={() => setSelecionadaId(item.id)}>
                                            {item.nome}
                                        </button>
                                    ))}
                                </Abas>
                            )}

                            <div id="painel-turma" role={varias ? "tabpanel" : undefined}
                                 aria-labelledby={varias ? `aba-turma-${turma.id}` : undefined}>
                                <Secao>
                                    <h2>Tarefas</h2>
                                    {turma.tarefas.length === 0 && (
                                        <EstadoVazio titulo="Nenhuma tarefa por aqui.">
                                            O professor ainda não passou tarefas. Enquanto isso, a trilha está te esperando!
                                        </EstadoVazio>
                                    )}
                                    {turma.tarefas.length > 0 && (
                                        <ListaTarefas>
                                            {turma.tarefas.map((tarefa) => (
                                                <TarefaDoAluno key={tarefa.id} tarefa={tarefa} aoAbrirTrilha={() => navigate("/trilha")} />
                                            ))}
                                        </ListaTarefas>
                                    )}
                                </Secao>

                                <Secao>
                                    <h2>Ranking da turma</h2>
                                    {!rankingDaTurma && <Aviso role="status">Carregando ranking...</Aviso>}
                                    {rankingDaTurma && (
                                        <Lista>
                                            {rankingDaTurma.posicoes.map((item) => <LinhaRanking key={item.username} item={item} />)}
                                        </Lista>
                                    )}
                                </Secao>

                                <Secao>
                                    <BotaoPequeno type="button" className="perigo" onClick={() => setConfirmarSaida(true)}>
                                        Sair desta turma
                                    </BotaoPequeno>
                                </Secao>
                            </div>

                            <EntrarComCodigo titulo="Entrar em outra turma" aoEntrar={aoEntrar} separada />
                        </>
                    )}
                </Quadro>
            </Page>
            <Footer />

            {confirmarSaida && turma && (
                <Confirmacao titulo={`Sair da turma ${turma.nome}?`}
                             mensagem="Você deixa de ver as tarefas e o ranking desta turma. Seu progresso na trilha e as outras turmas continuam. Para voltar, use o código de novo."
                             textoConfirmar="Sair da turma" aoConfirmar={sair} aoCancelar={() => setConfirmarSaida(false)} />
            )}
        </>
    );
};

export default MinhaTurma;
