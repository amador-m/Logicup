import { Oval, FlowArrow, Parallelogram, Rectangle, Diamond, DocumentShape } from "../styles";

/*
 * Sprint 7: UM lugar só para "qual forma desenha cada símbolo de fluxograma".
 * Usado pela teoria (SimbolosFluxograma) e pelas alternativas das questões (EscolhaUnica),
 * para a questão mostrar exatamente o mesmo desenho que o aluno estudou.
 *
 * A chave ("decisao", "processamento"...) é o valor do campo opcional "icone"
 * de cada alternativa no alternativas_json do banco.
 */
const FORMAS = {
    "inicio-fim": Oval,
    fluxo: () => <FlowArrow>↓</FlowArrow>,
    entrada: Parallelogram,
    processamento: Rectangle,
    decisao: Diamond,
    saida: DocumentShape,
};

/** Desenha o símbolo; se o tipo não existir (erro de digitação no banco), não desenha nada. */
const FormaFluxograma = ({ tipo }) => {
    const Forma = FORMAS[tipo];
    return Forma ? <Forma aria-hidden="true" /> : null;
};

export default FormaFluxograma;
