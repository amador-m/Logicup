import { ShortAnswerInput } from "../../styles";

/* RESPOSTA_CURTA — resposta {"texto": "..."} (a comparação com o gabarito é feita no backend) */
const RespostaCurta = ({ exercicio, valor, aoMudar, desabilitado }) => (
    <ShortAnswerInput
        type="text"
        aria-label={`Resposta da questão ${exercicio.titulo}`}
        placeholder="Digite sua resposta"
        maxLength={200}
        value={valor?.texto ?? ""}
        disabled={desabilitado}
        onChange={(evento) => aoMudar({ texto: evento.target.value })}
    />
);

export default RespostaCurta;
