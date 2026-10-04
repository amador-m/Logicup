import SimbolosFluxograma from "./components/SimbolosFluxograma";

/*
 * I3: conteúdo VISUAL extra de rodadas específicas (algo que não cabe no JSON de teoria do banco,
 * como os símbolos de fluxograma desenhados com CSS).
 *
 * Chave: "<número da fase>-<número da rodada>". Para dar um visual próprio a outra rodada,
 * basta criar o componente e acrescentar UMA linha aqui — a tela de missão não muda.
 */
const CONTEUDO_EXTRA = {
    "1-1": <SimbolosFluxograma />,
};

/** Devolve o conteúdo extra (elemento React) da rodada, ou null se ela não tiver. */
export function conteudoExtraDa(faseOrdem, rodadaOrdem) {
    return CONTEUDO_EXTRA[`${faseOrdem}-${rodadaOrdem}`] ?? null;
}
