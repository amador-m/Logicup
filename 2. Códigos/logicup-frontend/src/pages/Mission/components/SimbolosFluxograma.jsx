import FormaFluxograma from "./FormaFluxograma";
import {
    ExampleBox,
    FlowchartSymbols,
    SymbolCard,
    FlowArrow,
    FlowExample,
    FlowStep,
} from "../styles";

/*
 * Conteúdo visual extra da Fase 1, Rodada 1 (I3): símbolos de fluxograma desenhados com CSS
 * e o exemplo "fazer café". MOVIDO da tela de missão sem alteração visual.
 * É ligado à rodada pelo registro em ../conteudoExtra.jsx.
 */
const SimbolosFluxograma = () => (
    <>
        <ExampleBox>
            <h3>Principais símbolos dos fluxogramas</h3>

            <p>
                Fluxogramas usam símbolos para representar cada etapa de um
                algoritmo. Cada forma tem um significado diferente.
            </p>

            <FlowchartSymbols>
                <SymbolCard>
                    <FormaFluxograma tipo="inicio-fim" />
                    <div>
                        <h4>Início/Fim</h4>
                        <p>Marca onde o algoritmo começa ou termina.</p>
                    </div>
                </SymbolCard>

                <SymbolCard>
                    <FormaFluxograma tipo="fluxo" />
                    <div>
                        <h4>Fluxo</h4>
                        <p>Mostra a direção em que os passos acontecem.</p>
                    </div>
                </SymbolCard>

                <SymbolCard>
                    <FormaFluxograma tipo="entrada" />
                    <div>
                        <h4>Entrada de dados</h4>
                        <p>Representa informações recebidas pelo algoritmo.</p>
                    </div>
                </SymbolCard>

                <SymbolCard>
                    <FormaFluxograma tipo="processamento" />
                    <div>
                        <h4>Processamento</h4>
                        <p>Indica cálculos, ações ou atribuições de valores.</p>
                    </div>
                </SymbolCard>

                <SymbolCard>
                    <FormaFluxograma tipo="decisao" />
                    <div>
                        <h4>Decisão</h4>
                        <p>
                            Representa escolhas, como perguntas de sim ou não.
                        </p>
                    </div>
                </SymbolCard>

                <SymbolCard>
                    <FormaFluxograma tipo="saida" />
                    <div>
                        <h4>Saída de dados</h4>
                        <p>
                            Mostra uma resposta ou resultado apresentado ao
                            usuário.
                        </p>
                    </div>
                </SymbolCard>
            </FlowchartSymbols>
        </ExampleBox>

        <ExampleBox>
            <h3>Exemplo de fluxograma: fazer café</h3>

            <FlowExample>
                <FlowStep className="terminal">Início</FlowStep>
                <FlowArrow>↓</FlowArrow>
                <FlowStep>Ferver água</FlowStep>
                <FlowArrow>↓</FlowArrow>
                <FlowStep>Adicionar pó</FlowStep>
                <FlowArrow>↓</FlowArrow>
                <FlowStep>Coar café</FlowStep>
                <FlowArrow>↓</FlowArrow>
                <FlowStep className="document">Servir</FlowStep>
                <FlowArrow>↓</FlowArrow>
                <FlowStep className="terminal">Fim</FlowStep>
            </FlowExample>
        </ExampleBox>
    </>
);

export default SimbolosFluxograma;
