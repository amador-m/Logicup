import { useEffect, useMemo, useRef, useState } from "react";
import Header from "../../components/layout/Header";
import Footer from "../../components/layout/Footer";
import AreaBlocos from "../../components/ui/AreaBlocos";
import CodigoJava from "../../components/ui/CodigoJava";
import Confirmacao from "../../components/ui/Confirmacao";
import MascoteReacao from "../Mission/components/MascoteReacao";
import { execucaoApi } from "../../services/api";
import { useAuth } from "../../context/useAuth";
import { EXEMPLOS } from "./exemplos";
import { useTextoExecutando } from "../../util/useTextoExecutando";
import {
    Page, Hero, Painel, Ferramentas, BotaoExecutar, BotaoSecundario, Seletor, Salvo, Abas, Aba, Grade, Coluna,
    Avisos, Aviso, Situacao, Dica, ConsoleVazio,
} from "./styles";

/*
 * Sprint 9 (ADR-001, etapa 1 do Blockly): o LABORATÓRIO DE BLOCOS, funcionalidade permanente.
 *
 * O aluno monta um programa com blocos, vê AO VIVO o Java que os blocos formam e clica em Executar:
 * o programa completo vai para o backend (POST /api/execucoes) e roda no Piston, como no editor de código.
 * É um espaço livre: não vale XP e não grava nada no banco.
 *
 * O programa fica SALVO NESTE NAVEGADOR (localStorage), um por usuário (decisão do ADR: banco fica para
 * depois). O localStorage pode estar bloqueado (aba anônima, configuração do navegador), por isso todo
 * acesso fica em try/catch: sem ele, o laboratório funciona igual, só não lembra o programa.
 *
 * No celular, blocos, Java e saída não cabem lado a lado: viram ABAS. Ao clicar em Executar, a tela
 * vai sozinha para a aba Saída.
 */
const chaveDoUsuario = (usuario) => `logicup.laboratorio.v1.${usuario?.id ?? "anonimo"}`;

function lerSalvo(chave) {
    try {
        const texto = localStorage.getItem(chave);
        return texto ? JSON.parse(texto) : null;
    } catch {
        return null;
    }
}

const FALAS = {
    inicio: "Encaixe os blocos dentro do main e clique em Executar!",
    executando: "Rodando seu programa...",
    sucesso: "Funcionou! Olhe a saída no console.",
    semSaida: "Rodou, mas não exibiu nada. Falta um System.out.println?",
    erroJava: "O Java encontrou um problema. A mensagem diz em que linha.",
    faltando: "Tem bloco com espaço vazio (o do sinal de !). Complete antes de executar.",
    indisponivel: "O executor está fora do ar agora. Tente de novo daqui a pouco.",
};

const Laboratorio = () => {
    const { usuario } = useAuth();
    const chave = chaveDoUsuario(usuario);
    const estadoSalvo = useMemo(() => lerSalvo(chave), [chave]);

    const areaRef = useRef(null);
    const [blocos, setBlocos] = useState(null); // último resultado do gerador (trecho, programa, faltando...)
    const [salvo, setSalvo] = useState(Boolean(estadoSalvo));
    const [aba, setAba] = useState("blocos");
    const [execucao, setExecucao] = useState(null);
    const [erro, setErro] = useState("");
    const [executando, setExecutando] = useState(false);
    const textoExecutar = useTextoExecutando(executando); // Sprint 10: avisa quando está na fila
    const [mascote, setMascote] = useState({ humor: "neutro", fala: FALAS.inicio, vez: 0 });
    const [trocarPor, setTrocarPor] = useState(null); // { nome, estado } aguardando confirmação

    const reagir = (humor, fala) => setMascote((atual) => ({ humor, fala, vez: atual.vez + 1 }));

    // salva no navegador pouco depois da última mudança (não a cada bloco arrastado)
    useEffect(() => {
        if (!blocos?.estado) return undefined;
        const espera = setTimeout(() => {
            try {
                localStorage.setItem(chave, JSON.stringify(blocos.estado));
                setSalvo(true);
            } catch {
                setSalvo(false);
            }
        }, 500);
        return () => clearTimeout(espera);
    }, [blocos, chave]);

    const executar = async () => {
        if (!blocos || executando) return;
        if (blocos.faltando.length > 0) {
            reagir("erro", FALAS.faltando);
            setAba("blocos");
            return;
        }
        setExecutando(true);
        setErro("");
        reagir("neutro", FALAS.executando);
        setAba("saida");
        try {
            const resultado = await execucaoApi.executar(blocos.programa);
            setExecucao(resultado);
            if (resultado.situacao === "SUCESSO") reagir("acerto", resultado.saida ? FALAS.sucesso : FALAS.semSaida);
            else reagir("erro", FALAS.erroJava);
        } catch (e) {
            setExecucao(null);
            setErro(e.message);
            reagir("erro", e.status === 503 ? FALAS.indisponivel : e.message);
        } finally {
            setExecutando(false);
        }
    };

    const confirmarTroca = async () => {
        if (trocarPor.estado) areaRef.current?.carregar(trocarPor.estado);
        else areaRef.current?.limpar();
        setExecucao(null);
        setErro("");
        setTrocarPor(null);
        setAba("blocos");
        reagir("neutro", FALAS.inicio);
    };

    const escolherExemplo = (evento) => {
        const exemplo = EXEMPLOS.find((item) => item.id === evento.target.value);
        evento.target.value = "";
        if (exemplo) setTrocarPor({ nome: `o exemplo "${exemplo.nome}"`, estado: exemplo.estado });
    };

    const rodou = execucao?.situacao === "SUCESSO";

    return (
        <>
            <Header />
            <Page>
                <Hero>
                    <div>
                        <span>Laboratório</span>
                        <h1>Laboratório de Blocos</h1>
                        <p>
                            Monte programas encaixando blocos e veja, ao mesmo tempo, o código Java que eles formam.
                            Aqui é livre: experimente sem medo, não vale XP.
                        </p>
                    </div>
                    <MascoteReacao key={mascote.vez} humor={mascote.humor} fala={mascote.fala} tamanho={92} />
                </Hero>

                <Painel>
                    <Ferramentas>
                        <BotaoExecutar type="button" onClick={executar} disabled={!blocos || executando}>
                            <span aria-hidden="true">▶</span> {textoExecutar}
                        </BotaoExecutar>
                        <Seletor>
                            <span className="rotulo">Exemplos</span>
                            <select defaultValue="" onChange={escolherExemplo} aria-label="Abrir um programa de exemplo">
                                <option value="" disabled>Abrir um exemplo...</option>
                                {EXEMPLOS.map((exemplo) => <option key={exemplo.id} value={exemplo.id}>{exemplo.nome}</option>)}
                            </select>
                        </Seletor>
                        <BotaoSecundario type="button"
                                         onClick={() => setTrocarPor({ nome: "um programa vazio", estado: null })}>
                            ↺ Recomeçar
                        </BotaoSecundario>
                        <Salvo aria-live="polite">{salvo ? "✓ Salvo neste navegador" : "Não salvo (o navegador bloqueou)"}</Salvo>
                    </Ferramentas>

                    {/* só aparecem no celular (CSS) */}
                    <Abas role="tablist" aria-label="Partes do laboratório">
                        {[["blocos", "🧩 Blocos"], ["java", "☕ Java"], ["saida", "▶ Saída"]].map(([valor, texto]) => (
                            <Aba key={valor} type="button" role="tab" aria-selected={aba === valor}
                                 className={aba === valor ? "ativa" : ""} onClick={() => setAba(valor)}>
                                {texto}
                            </Aba>
                        ))}
                    </Abas>

                    {blocos && (blocos.faltando.length > 0 || blocos.foraDoMain > 0) && (
                        <Avisos>
                            {blocos.faltando.length > 0 && (
                                <Aviso className="atencao">
                                    ⚠ {blocos.faltando.length === 1 ? "Um bloco tem" : `${blocos.faltando.length} blocos têm`} espaço
                                    vazio (sinal de !). Encaixe um valor nele para o programa funcionar.
                                </Aviso>
                            )}
                            {blocos.foraDoMain > 0 && (
                                <Aviso>
                                    ℹ {blocos.foraDoMain === 1 ? "Um grupo de blocos está" : `${blocos.foraDoMain} grupos de blocos estão`} fora
                                    do main (apagado). O Java só executa o que está dentro do main.
                                </Aviso>
                            )}
                        </Avisos>
                    )}

                    <Grade data-aba={aba}>
                        <Coluna className="col-blocos">
                            <AreaBlocos
                                ref={areaRef}
                                titulo="Blocos"
                                altura={600}
                                comCaixa
                                estadoInicial={estadoSalvo}
                                aoMudar={setBlocos}
                                rotulo="Área de blocos do laboratório"
                            />
                        </Coluna>

                        <Coluna className="col-codigo">
                            <div className="col-java">
                                <CodigoJava codigo={(blocos?.programa ?? "").replace(/\n$/, "")} titulo="Main.java (gerado pelos blocos)" />
                            </div>
                            <section className="col-saida" aria-live="polite" aria-label="Resultado da execução">
                                {erro && <Situacao role="alert" className="erro">⚠ {erro}</Situacao>}
                                {execucao && (
                                    <>
                                        <Situacao className={rodou ? "ok" : "erro"}>
                                            {rodou ? "▶ " : "⚠ "}{execucao.mensagem}
                                            {execucao.tempoMs != null && <small>({(execucao.tempoMs / 1000).toFixed(1)} s)</small>}
                                        </Situacao>
                                        <CodigoJava codigo={(execucao.saida || "(o programa não exibiu nada)").replace(/\n$/, "")}
                                                    variante="console" titulo="Console" />
                                        {execucao.erros && <CodigoJava codigo={execucao.erros.replace(/\n$/, "")} variante="texto" titulo="Mensagem do Java" />}
                                        {execucao.dica && <Dica>💡 {execucao.dica}</Dica>}
                                    </>
                                )}
                                {!execucao && !erro && (
                                    <ConsoleVazio>A saída do programa aparece aqui depois de clicar em <strong>Executar</strong>.</ConsoleVazio>
                                )}
                            </section>
                        </Coluna>
                    </Grade>
                </Painel>
            </Page>
            <Footer />

            {trocarPor && (
                <Confirmacao
                    titulo="Trocar o programa?"
                    mensagem={`Os blocos que estão na área agora serão substituídos por ${trocarPor.nome}.`}
                    textoConfirmar="Trocar"
                    aoConfirmar={confirmarTroca}
                    aoCancelar={() => setTrocarPor(null)}
                />
            )}
        </>
    );
};

export default Laboratorio;
