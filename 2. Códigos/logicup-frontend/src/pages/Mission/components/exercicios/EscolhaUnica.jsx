import FormaFluxograma from "../FormaFluxograma";
import { OptionButton, IconeAlternativa } from "../../styles";

/*
 * ESCOLHA_UNICA — alternativas [{id, texto, icone?}] | resposta {"alternativa": "a"}
 * Sprint 7: "icone" é OPCIONAL. Quando existe (ex.: F1.Q8), o símbolo de fluxograma aparece
 * ao lado do texto. Questões sem "icone" continuam exatamente como antes.
 */
const EscolhaUnica = ({ exercicio, valor, aoMudar, desabilitado }) => (
    <div role="radiogroup" aria-label={`Alternativas da questão ${exercicio.titulo}`}>
        {exercicio.alternativas.map((alternativa) => {
            const marcada = valor?.alternativa === alternativa.id;
            return (
                <OptionButton
                    key={alternativa.id}
                    type="button"
                    role="radio"
                    aria-checked={marcada}
                    className={[marcada ? "selected" : "", alternativa.icone ? "com-icone" : ""].join(" ").trim()}
                    disabled={desabilitado}
                    onClick={() => aoMudar({ alternativa: alternativa.id })}
                >
                    {alternativa.icone && (
                        <IconeAlternativa aria-hidden="true">
                            <span><FormaFluxograma tipo={alternativa.icone} /></span>
                        </IconeAlternativa>
                    )}
                    {alternativa.texto}
                </OptionButton>
            );
        })}
    </div>
);

export default EscolhaUnica;
