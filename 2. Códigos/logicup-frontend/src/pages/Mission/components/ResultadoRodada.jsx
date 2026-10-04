import { RoundSummary, SummaryStats, PrimaryButton } from "../styles";

/*
 * Resumo do envio (I4), com os números calculados pelo BACKEND (ResumoEnvioResponse).
 * Regra de aprovação (PR5): pelo menos 80% dos exercícios concluídos E o Boss concluído.
 */
const ResultadoRodada = ({ resumo, proximaRodada, aoIrParaProxima }) => {
    const aprovada = resumo.rodadaAprovada;

    return (
        <RoundSummary className={aprovada ? "success" : ""} role="status">
            <h3>{aprovada ? "🎉 Round aprovado!" : "Resultado do envio"}</h3>

            <SummaryStats>
                <div>
                    <strong>{resumo.acertos}/{resumo.totalEnviado}</strong>
                    <span>acertos neste envio</span>
                </div>
                <div>
                    <strong>+{resumo.xpGanho}</strong>
                    <span>XP ganho</span>
                </div>
                <div>
                    <strong>{resumo.exerciciosConcluidos}/{resumo.totalExercicios}</strong>
                    <span>concluídos no round</span>
                </div>
            </SummaryStats>

            {!aprovada && (
                <p>
                    Para aprovar o round, conclua pelo menos 80% dos exercícios, incluindo o Boss Challenge.
                </p>
            )}

            {resumo.xpGanho === 0 && resumo.acertos > 0 && (
                <p className="note">
                    Questões que você já tinha acertado antes não dão XP de novo.
                </p>
            )}

            {proximaRodada && (
                <PrimaryButton type="button" onClick={aoIrParaProxima}>
                    {proximaRodada.mesmaFase
                        ? `Ir para o Round ${proximaRodada.ordem} →`
                        : `Ir para a Fase ${proximaRodada.faseOrdem} →`}
                </PrimaryButton>
            )}
        </RoundSummary>
    );
};

export default ResultadoRodada;
