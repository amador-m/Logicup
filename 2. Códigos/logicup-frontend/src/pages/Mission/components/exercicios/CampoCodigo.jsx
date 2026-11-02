import { useState } from "react";
import EditorCodigo from "../../../../components/ui/EditorCodigo";
import CodigoJava from "../../../../components/ui/CodigoJava";
import { execucaoApi } from "../../../../services/api";
import { AcoesEditor, BotaoExecutar, Situacao, Dica } from "./estilosCodigo";
import { StatusMessage } from "../../styles";

/*
 * Sprint 9 (ADR-001): CODIGO — o aluno escreve código Java de verdade.
 * Resposta enviada ao backend: {"codigo": "..."}.
 *
 * Dois botões com papéis diferentes (decisão de 01/11):
 *   "Executar"  (aqui)            -> roda com os EXEMPLOS públicos da questão; sem XP, sem correção, sem gravar.
 *   "Verificar" (FluxoQuestoes)   -> envia; o backend roda os casos OCULTOS, corrige e dá XP.
 *
 * O que vem do backend em exercicio.alternativas (público, nunca o gabarito):
 *   codigoInicial    -> o que aparece no editor quando o aluno abre a questão
 *   exemplos         -> descrição dos exemplos do Executar (ex.: ["energia = 0", "energia = 7"])
 *   saidaDosExemplos -> o que o programa deveria exibir nesses exemplos (para o aluno comparar)
 */
const SITUACOES_OK = ["SUCESSO"];

const CampoCodigo = ({ exercicio, valor, aoMudar, desabilitado }) => {
    const { codigoInicial = "", exemplos = [], saidaDosExemplos } = exercicio.alternativas ?? {};
    const codigo = valor?.codigo ?? codigoInicial;
    const [execucao, setExecucao] = useState(null);   // resposta do POST /execucoes
    const [erro, setErro] = useState(null);           // 503, 429, 400...
    const [executando, setExecutando] = useState(false);

    const executar = async () => {
        setExecutando(true);
        setErro(null);
        try {
            setExecucao(await execucaoApi.executarExercicio(exercicio.id, codigo));
        } catch (e) {
            setExecucao(null);
            setErro(e.message); // o backend já manda a mensagem em português
        } finally {
            setExecutando(false);
        }
    };

    const rodou = execucao && SITUACOES_OK.includes(execucao.situacao);

    return (
        <div>
            <EditorCodigo
                valor={codigo}
                aoMudar={(texto) => aoMudar({ codigo: texto })}
                desabilitado={desabilitado}
                rotulo={`Seu código para a questão ${exercicio.titulo}`}
            />

            <AcoesEditor>
                <BotaoExecutar type="button" onClick={executar} disabled={executando || codigo.trim() === ""}>
                    <span aria-hidden="true">▶</span> {executando ? "Executando..." : "Executar"}
                </BotaoExecutar>
                {exemplos.length > 0 && (
                    <span className="exemplos">
                        Testa com: {exemplos.join(" · ")} — não vale XP. Para valer, use <strong>Verificar</strong>.
                    </span>
                )}
            </AcoesEditor>

            {erro && <StatusMessage role="alert" className="error">{erro}</StatusMessage>}

            {execucao && (
                <section aria-live="polite" aria-label="Resultado da execução">
                    <Situacao className={rodou ? "ok" : "erro"}>
                        {rodou ? "▶ " : "⚠ "}
                        {execucao.mensagem}
                        {execucao.tempoMs != null && <small>({(execucao.tempoMs / 1000).toFixed(1)} s)</small>}
                    </Situacao>

                    {execucao.saida && <CodigoJava codigo={execucao.saida.replace(/\n$/, "")} variante="console" titulo="Seu programa exibiu" />}
                    {execucao.erros && <CodigoJava codigo={execucao.erros.replace(/\n$/, "")} variante="texto" titulo="Mensagem do Java" />}
                    {execucao.dica && <Dica>💡 {execucao.dica}</Dica>}

                    {/* comparação lado a lado não cabe no celular: a esperada vem logo abaixo (decisão 2 de 02/11) */}
                    {saidaDosExemplos && (
                        <CodigoJava codigo={saidaDosExemplos} variante="console" titulo="Era esperado" />
                    )}
                </section>
            )}
        </div>
    );
};

export default CampoCodigo;
