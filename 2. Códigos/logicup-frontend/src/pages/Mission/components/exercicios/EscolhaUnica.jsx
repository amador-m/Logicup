import { OptionButton } from "../../styles";

/* ESCOLHA_UNICA — alternativas [{id, texto}] | resposta {"alternativa": "a"} */
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
                    className={marcada ? "selected" : ""}
                    disabled={desabilitado}
                    onClick={() => aoMudar({ alternativa: alternativa.id })}
                >
                    {alternativa.texto}
                </OptionButton>
            );
        })}
    </div>
);

export default EscolhaUnica;
