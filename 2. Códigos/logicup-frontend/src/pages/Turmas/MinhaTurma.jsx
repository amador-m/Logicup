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
    Mascote, BotaoPequeno,
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

/*
 * Sprint 9: "Minha turma" do aluno.
 *  - sem turma: campo para digitar o código de convite;
 *  - com turma: tarefas, ranking da turma e o botão de sair.
 */
const MinhaTurma = () => {
    const navigate = useNavigate();
    const { usuario } = useAuth();
    const podeVer = podeEntrarEmTurma(usuario);

    const [turma, setTurma] = useState(undefined); // undefined = carregando; null = sem turma
    const [ranking, setRanking] = useState(null);
    const [erro, setErro] = useState(null);
    const [versao, setVersao] = useState(0);

    const [codigo, setCodigo] = useState("");
    const [erroCodigo, setErroCodigo] = useState("");
    const [entrando, setEntrando] = useState(false);
    const [confirmarSaida, setConfirmarSaida] = useState(false);

    useEffect(() => {
        if (!podeVer) return undefined;
        let ativo = true;
        minhaTurmaApi.buscar()
            .then(async (dados) => {
                if (!ativo) return;
                setTurma(dados); // null quando o backend responde 204 (sem turma)
                setRanking(dados ? await minhaTurmaApi.ranking() : null);
            })
            .catch((e) => ativo && setErro(e.message));
        return () => {
            ativo = false;
        };
    }, [podeVer, versao]);

    const entrar = async (evento) => {
        evento.preventDefault();
        setErroCodigo("");
        setEntrando(true);
        try {
            await minhaTurmaApi.entrar(codigo);
            setCodigo("");
            setVersao((n) => n + 1);
            // Sprint 9: entrar numa turma rende a conquista "Colega de turma"
            avisarProgresso();
            pedirComemoracao();
        } catch (e) {
            setErroCodigo(e.campos?.codigo ?? e.message);
        } finally {
            setEntrando(false);
        }
    };

    const sair = async () => {
        await minhaTurmaApi.sair();
        setConfirmarSaida(false);
        setVersao((n) => n + 1);
    };

    return (
        <>
            <Header />
            <Page>
                <Hero>
                    <div>
                        <span>Minha turma</span>
                        <h1>{turma ? turma.nome : "Entre na sua turma"}</h1>
                        <p>
                            {turma
                                ? `Professor(a): ${turma.professor ?? "—"}. Aqui ficam as tarefas da turma e o ranking só entre os colegas.`
                                : "Com o código que o professor passou, você entra na turma, vê as tarefas e o ranking da turma."}
                        </p>
                    </div>
                </Hero>

                <Quadro aria-label="Minha turma">
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

                    {podeVer && !erro && turma === undefined && <Aviso role="status">Carregando...</Aviso>}

                    {podeVer && turma === null && (
                        <Secao>
                            <Mascote src={MascoteImg} alt="" />
                            <h2>Tem um código de turma?</h2>
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
                    )}

                    {turma && (
                        <>
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
                                {!ranking && <Aviso role="status">Carregando ranking...</Aviso>}
                                {ranking && (
                                    <Lista>
                                        {ranking.posicoes.map((item) => <LinhaRanking key={item.username} item={item} />)}
                                    </Lista>
                                )}
                            </Secao>

                            <Secao>
                                <BotaoPequeno type="button" className="perigo" onClick={() => setConfirmarSaida(true)}>
                                    Sair da turma
                                </BotaoPequeno>
                            </Secao>
                        </>
                    )}
                </Quadro>
            </Page>
            <Footer />

            {confirmarSaida && (
                <Confirmacao titulo="Sair da turma?"
                             mensagem="Você deixa de ver as tarefas e o ranking da turma. Seu progresso na trilha continua. Para voltar, use o código de novo."
                             textoConfirmar="Sair da turma" aoConfirmar={sair} aoCancelar={() => setConfirmarSaida(false)} />
            )}
        </>
    );
};

export default MinhaTurma;
