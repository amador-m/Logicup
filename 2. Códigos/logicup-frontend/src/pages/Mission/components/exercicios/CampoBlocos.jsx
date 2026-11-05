import { useState } from "react";
import AreaBlocos from "../../../../components/ui/AreaBlocos";
import CodigoJava from "../../../../components/ui/CodigoJava";
import { execucaoApi } from "../../../../services/api";
import { AcoesEditor, BotaoExecutar, Situacao, Dica, AvisoBlocos, VerJava } from "./estilosCodigo";
import { StatusMessage } from "../../styles";
import { useTextoExecutando } from "../../../../util/useTextoExecutando";

/*
 * Sprint 9 (ADR-001, etapa 1 do Blockly): questão de BLOCOS.
 *
 * Decisão 5-A (02/11): os blocos já vêm na área, soltos e embaralhados, e não há caixa de ferramentas.
 * O aluno arrasta cada bloco para DENTRO do main, na ordem certa (os blocos não podem ser apagados).
 *
 * Os blocos viram Java aqui no navegador (geradorJava.js) e a correção é a mesma das questões de código
 * (decisão 4-A): o backend roda esse Java nos casos de teste, então qualquer montagem que FUNCIONE vale.
 *
 * Valor guardado na tela: { codigo, blocos, faltando }   (blocos = desenho, para não perder a montagem)
 * Enviado ao backend:     { codigo }                     (respostas.js -> respostaParaEnvio)
 *
 * 03/11: três jeitos de questão, conforme o bloco principal que vem nos blocos iniciais:
 *   java_main      -> blocos de Java (F4.Q5, F5.Q8)
 *   java_algoritmo -> passos em português (F1.Q4); o Java fica só como curiosidade
 *   java_raiz      -> "blocos de linha" (F2.Q4, F9.R2.Q8, F10.Q5): monta a classe ou o método
 * alternativas.sobram = true: a questão tem blocos que NÃO devem ser usados (F2.Q4); aí o contador não
 * pede para encaixar todos.
 */
const TEXTOS = {
    java_main: {
        aviso: <>🧩 Arraste os blocos para <strong>dentro do main</strong>, na ordem certa. Blocos fora do main ficam
            apagados e não são executados.</>,
        java: "Dentro do main",
    },
    java_algoritmo: {
        aviso: <>🧩 Arraste os passos para <strong>dentro do algoritmo</strong>, na ordem certa. Passos fora dele ficam
            apagados.</>,
        java: "Curiosidade: este algoritmo escrito em Java",
    },
    java_raiz: {
        aviso: <>🧩 Monte o código <strong>dentro do bloco escuro</strong>. Os blocos com <code>{"{ }"}</code> recebem
            outros blocos dentro deles: preste atenção na ordem e em <strong>quem fica dentro de quem</strong>.</>,
        java: "O código que os blocos formam",
    },
};

function contador(soltos, sobram) {
    if (soltos === 0) return null;
    if (sobram) {
        return soltos === 1
            ? "1 bloco está fora do programa (nesta questão, alguns blocos não devem ser usados)."
            : `${soltos} blocos estão fora do programa (nesta questão, alguns blocos não devem ser usados).`;
    }
    return soltos === 1 ? "Falta encaixar 1 bloco." : `Faltam encaixar ${soltos} blocos.`;
}

const CampoBlocos = ({ exercicio, valor, aoMudar, desabilitado }) => {
    const { blocosIniciais, sobram = false } = exercicio.alternativas ?? {};
    const [execucao, setExecucao] = useState(null);
    const [erro, setErro] = useState(null);
    const [executando, setExecutando] = useState(false);
    const textoExecutar = useTextoExecutando(executando); // Sprint 10: avisa quando está na fila
    const [situacao, setSituacao] = useState({ foraDoMain: 0, faltando: 0, principal: "java_main" });

    const codigo = valor?.codigo ?? "";

    const aoMudarBlocos = (resultado) => {
        setSituacao({ foraDoMain: resultado.foraDoMain, faltando: resultado.faltando.length,
                      principal: resultado.principal ?? "java_main" });
        if (!desabilitado) {
            aoMudar({ codigo: resultado.trecho, blocos: resultado.estado, faltando: resultado.faltando.length });
        }
    };

    const executar = async () => {
        setExecutando(true);
        setErro(null);
        try {
            setExecucao(await execucaoApi.executarExercicio(exercicio.id, codigo));
        } catch (e) {
            setExecucao(null);
            setErro(e.message);
        } finally {
            setExecutando(false);
        }
    };

    const rodou = execucao?.situacao === "SUCESSO";
    const vazio = codigo.trim() === "";
    const textos = TEXTOS[situacao.principal] ?? TEXTOS.java_main;
    const avisoSoltos = contador(situacao.foraDoMain, sobram);

    return (
        <div>
            {!desabilitado && (
                <AvisoBlocos>
                    {textos.aviso}
                    {avisoSoltos && <span className="contador">{avisoSoltos}</span>}
                </AvisoBlocos>
            )}

            {/* key: o Blockly é criado uma vez; ao travar a questão (corrigida), ele é recriado em modo leitura */}
            <AreaBlocos
                key={`${exercicio.id}-${desabilitado ? "leitura" : "edicao"}`}
                titulo="Monte o programa"
                altura={460}
                estadoInicial={valor?.blocos ?? blocosIniciais}
                aoMudar={aoMudarBlocos}
                somenteLeitura={desabilitado}
                rotulo={`Blocos da questão ${exercicio.titulo}`}
            />

            <AcoesEditor>
                <BotaoExecutar type="button" onClick={executar} disabled={executando || vazio || situacao.faltando > 0}>
                    <span aria-hidden="true">▶</span> {textoExecutar}
                </BotaoExecutar>
                <span className="exemplos">
                    Testa o programa sem valer XP. Para valer, use <strong>Verificar</strong>.
                </span>
            </AcoesEditor>

            {!vazio && (
                <VerJava>
                    <summary>☕ Ver o Java que os blocos formam</summary>
                    <CodigoJava codigo={codigo.replace(/\n$/, "")} titulo={textos.java} />
                </VerJava>
            )}

            {erro && <StatusMessage role="alert" className="error">{erro}</StatusMessage>}

            {execucao && (
                <section aria-live="polite" aria-label="Resultado da execução">
                    <Situacao className={rodou ? "ok" : "erro"}>
                        {rodou ? "▶ " : "⚠ "}
                        {execucao.mensagem}
                        {execucao.tempoMs != null && <small>({(execucao.tempoMs / 1000).toFixed(1)} s)</small>}
                    </Situacao>
                    <CodigoJava codigo={(execucao.saida || "(o programa não exibiu nada)").replace(/\n$/, "")}
                                variante="console" titulo="Seu programa exibiu" />
                    {execucao.erros && <CodigoJava codigo={execucao.erros.replace(/\n$/, "")} variante="texto" titulo="Mensagem do Java" />}
                    {execucao.dica && <Dica>💡 {execucao.dica}</Dica>}
                </section>
            )}
        </div>
    );
};

export default CampoBlocos;
