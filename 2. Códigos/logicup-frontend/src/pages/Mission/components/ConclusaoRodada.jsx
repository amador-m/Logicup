import MascoteReacao from "./MascoteReacao";
import VideoWebm from "../../../assets/images/MascoteComemora.webm";
import VideoMp4 from "../../../assets/images/MascoteComemora.mp4";
import MascoteImg from "../../../assets/images/MascoteRecortado.png";
import {
    RoundSummary,
    SummaryStats,
    Celebracao,
    AcoesConclusao,
    PrimaryButton,
    OutlineButton,
} from "../styles";

/*
 * Mesmo percentual da regra PR5 do backend (RegrasTrilha.PERCENTUAL_APROVACAO).
 * ATENÇÃO: aqui ele serve SÓ para EXPLICAR ao aluno quanto falta. Quem DECIDE se a rodada foi
 * aprovada é o backend (campo rodadaAprovada / status da rodada) — nunca este cálculo.
 */
const PERCENTUAL_APROVACAO = 80;

const plural = (n, singular, pluralTexto) => `${n} ${n === 1 ? singular : pluralTexto}`;

function textoProxima(proximaRodada) {
    return proximaRodada.mesmaFase
        ? `Ir para o Round ${proximaRodada.ordem} →`
        : `Ir para a Fase ${proximaRodada.faseOrdem} →`;
}

/* Vídeo de comemoração; com "reduzir movimento" ativado, mostra a imagem parada. */
const prefereMenosMovimento = () =>
    typeof window !== "undefined" && window.matchMedia?.("(prefers-reduced-motion: reduce)").matches;

/*
 * Resultado da rodada (fim da passagem pelas questões).
 *   - aprovada: comemora, mostra XP real e leva para a próxima rodada/fase;
 *   - não aprovada: diz objetivamente quanto falta (e se falta o Boss) e oferece refazer as erradas;
 *   - aprovou AGORA a última rodada da fase: destaque "Fase concluída" com o vídeo do mascote.
 */
const ConclusaoRodada = ({
    exercicios,
    estados,
    aprovada,          // decisão do BACKEND
    aprovadaAgora,     // a aprovação aconteceu nesta visita (para comemorar uma vez só)
    ehUltimaDaFase,
    fase,              // { ordem, titulo, xpGanho, xpTotal, totalRodadas }
    rodadaResumo,      // { xpGanho, xpTotal } reais da rodada (vindos da trilha)
    proximaRodada,     // { id, ordem, faseOrdem, mesmaFase, status } ou null
    aoRefazerErradas,
    aoContinuar,
    aoVerQuestoes,
    aoReverTeoria,
    aoIrParaProxima,
    aoVoltarTrilha,
}) => {
    const total = exercicios.length;
    const concluidas = exercicios.filter((e) => estados[e.id] === "correta").length;
    const erradas = exercicios.filter((e) => estados[e.id] === "errada").length;
    const pendentes = exercicios.filter((e) => estados[e.id] === "pendente").length;
    const minimo = Math.ceil((total * PERCENTUAL_APROVACAO) / 100);
    const faltam = Math.max(0, minimo - concluidas);
    const bossPendente = exercicios.some((e) => e.tipo === "BOSS_CHALLENGE" && estados[e.id] !== "correta");
    const podeAvancar = proximaRodada && proximaRodada.status !== "BLOQUEADA";

    const botaoRefazer = erradas > 0 && (
        <OutlineButton type="button" onClick={aoRefazerErradas}>
            Refazer as que errei ({erradas})
        </OutlineButton>
    );

    // ---------- Fase concluída agora: o grande momento ----------
    if (aprovada && aprovadaAgora && ehUltimaDaFase) {
        return (
            <Celebracao role="status">
                {prefereMenosMovimento() ? (
                    <img src={MascoteImg} alt="" aria-hidden="true" />
                ) : (
                    // o navegador usa o primeiro formato que souber tocar: WebM (VP9) ou, se não, MP4 (H.264)
                    <video autoPlay loop muted playsInline aria-hidden="true">
                        <source src={VideoWebm} type="video/webm" />
                        <source src={VideoMp4} type="video/mp4" />
                    </video>
                )}
                <span>Missão concluída</span>
                <h3>Fase {fase.ordem} concluída!</h3>
                <p>Você completou todas as rodadas de <strong>{fase.titulo}</strong>.</p>

                <SummaryStats>
                    <div>
                        <strong>+{fase.xpGanho}</strong>
                        <span>XP conquistado na fase (de {fase.xpTotal})</span>
                    </div>
                    <div>
                        <strong>{fase.totalRodadas}/{fase.totalRodadas}</strong>
                        <span>rodadas aprovadas</span>
                    </div>
                </SummaryStats>

                {fase.xpGanho < fase.xpTotal && (
                    <p className="note">Questões acertadas depois de um erro valem metade do XP.</p>
                )}

                <AcoesConclusao>
                    {podeAvancar ? (
                        <PrimaryButton type="button" onClick={aoIrParaProxima}>{textoProxima(proximaRodada)}</PrimaryButton>
                    ) : (
                        <PrimaryButton type="button" onClick={aoVoltarTrilha}>Voltar à trilha</PrimaryButton>
                    )}
                    {botaoRefazer}
                </AcoesConclusao>
            </Celebracao>
        );
    }

    // ---------- Rodada aprovada ----------
    if (aprovada) {
        return (
            <RoundSummary className="success" role="status">
                <MascoteReacao humor={aprovadaAgora ? "acerto" : "neutro"} fala={aprovadaAgora ? "Round aprovado!" : "Rodada já concluída."} />
                <h3>{ehUltimaDaFase ? `✓ Fase ${fase.ordem} concluída` : "Rodada concluída!"}</h3>

                <SummaryStats>
                    <div>
                        <strong>{concluidas}/{total}</strong>
                        <span>questões concluídas</span>
                    </div>
                    <div>
                        <strong>{rodadaResumo?.xpGanho ?? 0}</strong>
                        <span>XP ganho (de {rodadaResumo?.xpTotal ?? 0})</span>
                    </div>
                </SummaryStats>

                {erradas > 0 && (
                    <p>Ainda dá para refazer {plural(erradas, "questão", "questões")} e ganhar XP (metade, porque você já viu a resposta).</p>
                )}

                <AcoesConclusao>
                    {podeAvancar && (
                        <PrimaryButton type="button" onClick={aoIrParaProxima}>{textoProxima(proximaRodada)}</PrimaryButton>
                    )}
                    {!podeAvancar && ehUltimaDaFase && (
                        <PrimaryButton type="button" onClick={aoVoltarTrilha}>Voltar à trilha</PrimaryButton>
                    )}
                    {botaoRefazer}
                    <OutlineButton type="button" onClick={aoVerQuestoes}>Ver questões</OutlineButton>
                    <OutlineButton type="button" onClick={aoReverTeoria}>Rever teoria</OutlineButton>
                </AcoesConclusao>
            </RoundSummary>
        );
    }

    // ---------- Ainda não aprovada: o que falta, de forma objetiva ----------
    return (
        <RoundSummary role="status">
            <MascoteReacao humor="neutro" fala="Falta pouco. Bora de novo?" />
            <h3>Rodada ainda não concluída</h3>
            <p>Você acertou <strong>{concluidas} de {total}</strong> questões.</p>

            {faltam > 0 && (
                <p>Falta acertar mais <strong>{plural(faltam, "questão", "questões")}</strong> para avançar.</p>
            )}
            {bossPendente && (
                <p>Falta vencer o <strong>Boss Challenge</strong>: ele é obrigatório para concluir a rodada.</p>
            )}

            <AcoesConclusao>
                {pendentes > 0 && (
                    <PrimaryButton type="button" onClick={aoContinuar}>Continuar de onde parei</PrimaryButton>
                )}
                {erradas > 0 && (
                    <PrimaryButton type="button" onClick={aoRefazerErradas}>
                        Refazer as que errei ({erradas})
                    </PrimaryButton>
                )}
                <OutlineButton type="button" onClick={aoReverTeoria}>Rever teoria</OutlineButton>
            </AcoesConclusao>
        </RoundSummary>
    );
};

export default ConclusaoRodada;
