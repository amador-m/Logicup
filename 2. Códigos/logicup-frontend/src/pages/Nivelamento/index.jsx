import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import Header from "../../components/layout/Header";
import Footer from "../../components/layout/Footer/index.jsx";
import CodigoJava from "../../components/ui/CodigoJava";
import { nivelamentoApi } from "../../services/api";
import MascoteImg from "../../assets/images/MascoteRecortado.png";
import { Page, OptionButton, PrimaryButton, FeedbackBox } from "../Mission/styles";
import { Topo, Cartao, Progresso, Acoes, Mascote, Placar, ListaCorrecoes, BotaoTexto } from "./styles";

/** "Fases 1 a 3" / "Fase 1" — para o texto do resultado. */
const textoFases = (n) => (n === 1 ? "a Fase 1" : `as Fases 1 a ${n}`);

/*
 * Sprint 11: QUIZ DE NIVELAMENTO (só no primeiro acesso do aluno).
 *  - uma questão por vez (menos cansativo e funciona bem no celular);
 *  - a correção é feita no BACKEND: a página nem conhece as respostas certas antes de enviar;
 *  - no fim, o resultado mostra quantas fases foram liberadas e a explicação de cada questão.
 * Se o nivelamento não estiver mais disponível (já fez, recusou ou já começou a trilha), a página avisa
 * e leva para a trilha.
 */
const Nivelamento = () => {
    const navigate = useNavigate();
    const tituloRef = useRef(null);

    const [estado, setEstado] = useState({ carregando: true, erro: "", situacao: null, questoes: [] });
    const [indice, setIndice] = useState(0);
    const [escolhas, setEscolhas] = useState({});       // questaoId -> alternativa
    const [enviando, setEnviando] = useState(false);
    const [resultado, setResultado] = useState(null);

    useEffect(() => {
        let ativo = true;
        nivelamentoApi.situacao()
            .then((dados) => ativo && setEstado({ carregando: false, erro: "", situacao: dados.situacao,
                questoes: dados.questoes ?? [] }))
            .catch((e) => ativo && setEstado({ carregando: false, erro: e.message, situacao: null, questoes: [] }));
        return () => {
            ativo = false;
        };
    }, []);

    // a cada questão nova, o foco vai para o enunciado (o leitor de tela lê a questão nova)
    useEffect(() => {
        tituloRef.current?.focus();
    }, [indice, resultado]);

    const { questoes } = estado;
    const questao = questoes[indice];
    const ultima = indice === questoes.length - 1;
    const alternativas = questao?.alternativas ?? [];

    const enviar = async () => {
        setEnviando(true);
        try {
            const respostas = questoes.map((q) => ({ questaoId: q.id, alternativa: escolhas[q.id] }));
            const dados = await nivelamentoApi.responder(respostas);
            setResultado(dados);
        } catch (e) {
            setEstado((atual) => ({ ...atual, erro: e.message }));
        } finally {
            setEnviando(false);
        }
    };

    const conteudo = () => {
        if (estado.carregando) return <p role="status">Carregando o nivelamento...</p>;

        if (resultado) {
            const { acertos, total, fasesLiberadas, correcoes } = resultado;
            return (
                <>
                    <Mascote>
                        <img src={MascoteImg} alt="" />
                        <div>
                            <h2 ref={tituloRef} tabIndex={-1}>Resultado do nivelamento</h2>
                            <Placar>{acertos} de {total} acertos</Placar>
                            <p>
                                {fasesLiberadas > 0
                                    ? `Você mostrou que domina ${textoFases(fasesLiberadas)}. Elas ficaram liberadas e você pode começar pela Fase ${fasesLiberadas + 1}. Se quiser revisar, as fases liberadas continuam abertas, e o XP delas vale normalmente.`
                                    : "Vamos começar pela Fase 1: é o melhor caminho para construir uma base firme. A trilha vai no seu ritmo!"}
                            </p>
                        </div>
                    </Mascote>

                    <ListaCorrecoes aria-label="Correção das questões">
                        {correcoes.map((c, i) => (
                            <li key={c.questaoId} className={c.acertou ? "" : "errou"}>
                                <strong>
                                    {c.acertou ? "✓" : "✗"} Questão {i + 1} (Fase {c.fase})
                                    {c.acertou ? " — certa" : ` — resposta certa: ${c.correta.toUpperCase()}`}
                                </strong>
                                {c.explicacao}
                            </li>
                        ))}
                    </ListaCorrecoes>

                    <Acoes>
                        <span />
                        <PrimaryButton type="button" onClick={() => navigate("/trilha", { replace: true })}>
                            Ir para a trilha
                        </PrimaryButton>
                    </Acoes>
                </>
            );
        }

        if (estado.situacao !== "PENDENTE") {
            return (
                <>
                    <h2 ref={tituloRef} tabIndex={-1}>O nivelamento não está disponível</h2>
                    <p>
                        {estado.erro || "Ele é oferecido só no primeiro acesso, antes de começar a trilha. Siga pela trilha normalmente!"}
                    </p>
                    <Acoes>
                        <span />
                        <PrimaryButton type="button" onClick={() => navigate("/trilha", { replace: true })}>
                            Ir para a trilha
                        </PrimaryButton>
                    </Acoes>
                </>
            );
        }

        return (
            <>
                <Progresso>
                    <span>Questão {indice + 1} de {questoes.length}</span>
                    <div className="barra" aria-hidden="true">
                        <span style={{ width: `${((indice + 1) / questoes.length) * 100}%` }} />
                    </div>
                </Progresso>

                <h2 ref={tituloRef} tabIndex={-1}>{questao.enunciado}</h2>
                {questao.codigo && <CodigoJava codigo={questao.codigo} titulo="Trecho" />}

                <div role="radiogroup" aria-label="Alternativas">
                    {alternativas.map((alternativa) => {
                        const marcada = escolhas[questao.id] === alternativa.id;
                        return (
                            <OptionButton
                                key={alternativa.id}
                                type="button"
                                role="radio"
                                aria-checked={marcada}
                                className={marcada ? "selected" : ""}
                                onClick={() => setEscolhas((atual) => ({ ...atual, [questao.id]: alternativa.id }))}
                                disabled={enviando}
                            >
                                {alternativa.id.toUpperCase()}) {alternativa.texto}
                            </OptionButton>
                        );
                    })}
                </div>

                {estado.erro && <FeedbackBox className="error" role="alert">{estado.erro}</FeedbackBox>}

                <Acoes>
                    <BotaoTexto type="button" onClick={() => setIndice((i) => i - 1)} disabled={indice === 0 || enviando}>
                        Voltar
                    </BotaoTexto>
                    {ultima ? (
                        <PrimaryButton type="button" onClick={enviar}
                                       disabled={enviando || questoes.some((q) => !escolhas[q.id])}>
                            {enviando ? "Corrigindo..." : "Ver resultado"}
                        </PrimaryButton>
                    ) : (
                        <PrimaryButton type="button" onClick={() => setIndice((i) => i + 1)}
                                       disabled={!escolhas[questao.id]}>
                            Próxima
                        </PrimaryButton>
                    )}
                </Acoes>
            </>
        );
    };

    return (
        <>
            <Header />
            <Page>
                <Topo>
                    <span>Primeiro acesso</span>
                    <h1>Nivelamento</h1>
                    <p>
                        10 questões rápidas sobre lógica, os primeiros passos em Java, variáveis, condicionais e
                        laços. Acertando as duas questões de uma fase, ela é liberada e você pode começar mais
                        adiante. Não vale XP: é só para descobrir por onde começar.
                    </p>
                </Topo>
                <Cartao>{conteudo()}</Cartao>
            </Page>
            <Footer />
        </>
    );
};

export default Nivelamento;
