import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Header from "../../components/layout/Header";
import Footer from "../../components/layout/Footer";
import { useAuth } from "../../context/useAuth";
import { ementaApi } from "../../services/api";
import { catalogoFases } from "../../data/catalogoFases";
import { iconeFase } from "../../assets/fases";
import { ROTULO_TIPO } from "../Mission/rotulos";
import { podeVerEmenta } from "../../util/papeis";
import {
    Page, Hero, Resumo, Quadro, ListaFases, CartaoFase, CartaoEmBreve, Rodada, Objetivos,
    ListaQuestoes, Questao, SemHabilidade, Aviso, BotaoAcao,
} from "./styles";

/*
 * Junta o catálogo visual (10 fases) com as fases reais da ementa, pelo NÚMERO da fase,
 * a mesma ideia do montarMapa da trilha: fase que ainda não existe no banco aparece como "Em breve".
 */
function montarFases(fasesDaApi) {
    const porOrdem = new Map(fasesDaApi.map((fase) => [fase.ordem, fase]));
    return catalogoFases.map((itemCatalogo) => {
        const fase = porOrdem.get(itemCatalogo.ordem);
        const temConteudo = Boolean(fase && fase.rodadas.length > 0);
        return { ...itemCatalogo, ...(fase ?? {}), icone: itemCatalogo.icone, temConteudo };
    });
}

function contar(fases) {
    const questoes = fases.flatMap((fase) => (fase.temConteudo ? fase.rodadas.flatMap((r) => r.questoes) : []));
    return {
        fases: fases.filter((fase) => fase.temConteudo).length,
        questoes: questoes.length,
        habilidades: questoes.filter((questao) => questao.habilidade).length,
    };
}

const QuestaoDaEmenta = ({ questao }) => (
    <Questao>
        <div className="topo">
            <span className="codigo">{questao.codigo}</span>
            <span className="tipo">{ROTULO_TIPO[questao.tipo] ?? questao.tipo}</span>
            <span className="xp">{questao.pontos} XP</span>
        </div>
        <h5>{questao.titulo}</h5>
        {questao.habilidade
            ? <p>{questao.habilidade}</p>
            : <SemHabilidade>Habilidade ainda não definida.</SemHabilidade>}
    </Questao>
);

const RodadaDaEmenta = ({ rodada }) => (
    <Rodada>
        <h4><span>Rodada {rodada.ordem}</span> {rodada.titulo}</h4>

        {rodada.objetivos.length > 0 && (
            <Objetivos>
                <h5>Objetivos da rodada</h5>
                <ul>
                    {rodada.objetivos.map((objetivo) => <li key={objetivo}>{objetivo}</li>)}
                </ul>
            </Objetivos>
        )}

        <h5 className="titulo-questoes">Habilidades por questão</h5>
        <ListaQuestoes>
            {rodada.questoes.map((questao) => <QuestaoDaEmenta key={questao.id} questao={questao} />)}
        </ListaQuestoes>
    </Rodada>
);

/*
 * <details>/<summary> é o "acordeão" nativo do HTML: abre e fecha com clique, Enter ou espaço,
 * e o leitor de tela já anuncia "expandido/recolhido", sem JavaScript nosso.
 */
const FaseDaEmenta = ({ fase, aberta }) => {
    const totalQuestoes = fase.rodadas.reduce((total, rodada) => total + rodada.questoes.length, 0);
    return (
        <CartaoFase open={aberta}>
            <summary>
                <img src={iconeFase(fase.icone, "yellow")} alt="" aria-hidden="true" />
                <div className="info">
                    <span className="numero">Fase {fase.ordem}</span>
                    <h3>{fase.titulo}</h3>
                    <span className="contagem">
                        {fase.rodadas.length} {fase.rodadas.length === 1 ? "rodada" : "rodadas"} • {totalQuestoes} {totalQuestoes === 1 ? "questão" : "questões"}
                    </span>
                </div>
                <span className="seta" aria-hidden="true">▾</span>
            </summary>

            <div className="conteudo">
                {fase.objetivoGeral && (
                    <p className="objetivo-geral"><strong>Objetivo geral:</strong> {fase.objetivoGeral}</p>
                )}
                {fase.rodadas.map((rodada) => <RodadaDaEmenta key={rodada.id} rodada={rodada} />)}
            </div>
        </CartaoFase>
    );
};

const FaseEmBreve = ({ fase }) => (
    <CartaoEmBreve>
        <img src={iconeFase(fase.icone, "block")} alt="" aria-hidden="true" />
        <div className="info">
            <span className="numero">Fase {fase.ordem} • Em breve</span>
            <h3>{fase.titulo}</h3>
            <p>Planejado: {fase.topicos.join(", ")}.</p>
        </div>
    </CartaoEmBreve>
);

/*
 * Sprint 7: ementa do professor. Mostra, para cada fase, o objetivo geral, os objetivos de cada
 * rodada e a habilidade que cada questão desenvolve. Não mostra enunciado, alternativas nem gabarito.
 */
const Ementa = () => {
    const navigate = useNavigate();
    const { usuario } = useAuth();
    const podeVer = podeVerEmenta(usuario); // aluno vê um aviso e a API nem é chamada

    const [ementa, setEmenta] = useState(null);
    const [erro, setErro] = useState(null);
    const [tentativa, setTentativa] = useState(0);

    useEffect(() => {
        if (!podeVer) return undefined; // aluno nem chama a API
        let ativo = true;
        ementaApi.ementa()
            .then((dados) => ativo && setEmenta(dados))
            .catch((e) => ativo && setErro(e.message));
        return () => {
            ativo = false;
        };
    }, [podeVer, tentativa]);

    const fases = ementa ? montarFases(ementa.fases) : [];
    const totais = contar(fases);
    const primeiraComConteudo = fases.find((fase) => fase.temConteudo)?.ordem;

    return (
        <>
            <Header />
            <Page>
                <Hero>
                    <div>
                        <span>Área do professor</span>
                        <h1>Ementa da trilha</h1>
                        <p>
                            O que cada fase, rodada e questão desenvolve no aluno: objetivo geral da fase,
                            objetivos de cada rodada e a habilidade avaliada em cada questão.
                        </p>
                    </div>
                    {ementa && (
                        <Resumo aria-label="Resumo da ementa">
                            <div><strong>{totais.fases}</strong><span>{totais.fases === 1 ? "fase" : "fases"} com conteúdo</span></div>
                            <div><strong>{totais.questoes}</strong><span>questões</span></div>
                            <div><strong>{totais.habilidades}/{totais.questoes}</strong><span>habilidades definidas</span></div>
                        </Resumo>
                    )}
                </Hero>

                <Quadro aria-label="Ementa por fase">
                    {!podeVer && (
                        <Aviso>
                            <strong>A ementa é exclusiva para professores.</strong>
                            <div>
                                <BotaoAcao type="button" onClick={() => navigate("/trilha")}>Ir para a trilha</BotaoAcao>
                            </div>
                        </Aviso>
                    )}

                    {podeVer && erro && (
                        <Aviso role="alert" className="error">
                            <p>{erro}</p>
                            <BotaoAcao type="button" onClick={() => { setErro(null); setTentativa((n) => n + 1); }}>
                                Tentar novamente
                            </BotaoAcao>
                        </Aviso>
                    )}

                    {podeVer && !erro && !ementa && <Aviso role="status">Carregando ementa...</Aviso>}

                    {ementa && (
                        <ListaFases>
                            {fases.map((fase) => (
                                <li key={fase.ordem}>
                                    {fase.temConteudo
                                        ? <FaseDaEmenta fase={fase} aberta={fase.ordem === primeiraComConteudo} />
                                        : <FaseEmBreve fase={fase} />}
                                </li>
                            ))}
                        </ListaFases>
                    )}
                </Quadro>
            </Page>
            <Footer />
        </>
    );
};

export default Ementa;
