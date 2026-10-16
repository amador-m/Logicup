import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Header from "../../components/layout/Header";
import Footer from "../../components/layout/Footer";
import { rankingApi } from "../../services/api";
import { Page, Hero, SuaPosicao, Quadro, Lista, Linha, Separador, Aviso, BotaoAcao } from "./styles";

const MEDALHAS = { 1: "🥇", 2: "🥈", 3: "🥉" };

/** Uma linha do ranking. Empates aparecem com a mesma posição (a regra é do backend). */
const LinhaRanking = ({ item }) => {
    const medalha = MEDALHAS[item.posicao];
    return (
        <Linha className={`${item.posicao === 1 ? "podio-1" : ""} ${item.voce ? "voce" : ""}`}
               aria-current={item.voce ? "true" : undefined}>
            <span className="posicao" aria-label={`${item.posicao}º lugar`}>
                {medalha ?? `${item.posicao}º`}
            </span>
            <span className="avatar" aria-hidden="true">{item.username.slice(0, 2)}</span>
            <span className="nome">
                {item.username}
                {item.voce && <small>você</small>}
            </span>
            <span className="nivel">Fase {item.nivel}</span>
            <span className="xp">{item.xp} XP</span>
        </Linha>
    );
};

/*
 * Ranking global por XP (Sprint 7): top 10 + a posição de quem está logado.
 * Mostra só o USERNAME (nunca nome ou e-mail). Só alunos aparecem; quem tem 0 XP ainda não entra.
 */
const Ranking = () => {
    const navigate = useNavigate();
    const [ranking, setRanking] = useState(null);
    const [erro, setErro] = useState(null);
    const [tentativa, setTentativa] = useState(0);

    useEffect(() => {
        let ativo = true;
        rankingApi.ranking()
            .then((dados) => ativo && setRanking(dados))
            .catch((e) => ativo && setErro(e.message));
        return () => {
            ativo = false;
        };
    }, [tentativa]);

    const voce = ranking?.voce ?? null; // null para professor/admin
    const voceNoTopo = ranking?.topo.some((item) => item.voce) ?? false;

    let destaque = null;
    if (voce && voce.posicao) {
        destaque = (
            <SuaPosicao>
                <span>Sua posição</span>
                <strong>{voce.posicao}º</strong>
                <p>{voce.xp} XP • Fase {voce.nivel}</p>
            </SuaPosicao>
        );
    } else if (voce) {
        destaque = (
            <SuaPosicao>
                <span>Sua posição</span>
                <p>Você ainda não pontuou. Complete uma questão para entrar no ranking!</p>
            </SuaPosicao>
        );
    }

    return (
        <>
            <Header />
            <Page>
                <Hero>
                    <div>
                        <span>Ranking global</span>
                        <h1>Quem mais está aprendendo Java</h1>
                        <p>
                            Cada questão concluída rende XP. Quem tem mais XP sobe no ranking;
                            empates dividem a mesma posição.
                        </p>
                    </div>
                    {destaque}
                </Hero>

                <Quadro aria-label="Classificação">

                    {erro && (
                        <Aviso role="alert" className="error">
                            <p>{erro}</p>
                            <BotaoAcao type="button" onClick={() => { setErro(null); setTentativa((n) => n + 1); }}>
                                Tentar novamente
                            </BotaoAcao>
                        </Aviso>
                    )}

                    {!erro && !ranking && <Aviso role="status">Carregando ranking...</Aviso>}

                    {ranking && ranking.topo.length === 0 && (
                        <Aviso>
                            <strong>Ninguém pontuou ainda.</strong> Conclua a primeira questão e seja o 1º do ranking!
                            <div>
                                <BotaoAcao type="button" onClick={() => navigate("/trilha")}>Ir para a trilha</BotaoAcao>
                            </div>
                        </Aviso>
                    )}

                    {ranking && ranking.topo.length > 0 && (
                        <Lista>
                            {ranking.topo.map((item) => (
                                <LinhaRanking key={item.username} item={item} />
                            ))}

                            {/* fora do top 10: mostra "..." e a linha do próprio aluno */}
                            {voce && voce.posicao && !voceNoTopo && (
                                <>
                                    <Separador aria-hidden="true">• • •</Separador>
                                    <LinhaRanking item={voce} />
                                </>
                            )}
                        </Lista>
                    )}
                </Quadro>
            </Page>
            <Footer />
        </>
    );
};

export default Ranking;
