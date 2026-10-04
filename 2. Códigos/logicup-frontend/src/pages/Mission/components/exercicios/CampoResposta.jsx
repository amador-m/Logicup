import EscolhaUnica from "./EscolhaUnica";
import Ordenacao from "./Ordenacao";
import Associacao from "./Associacao";
import RespostaCurta from "./RespostaCurta";
import TextoLivre from "./TextoLivre";

/*
 * Escolhe o componente de resposta pelo FORMATO da questão — o mesmo enum que o backend
 * usa para escolher o corretor (padrão Strategy dos dois lados).
 * Formato novo = um componente novo + um "case" aqui.
 */
const CampoResposta = ({ exercicio, valor, aoMudar, desabilitado }) => {
    const props = { exercicio, valor, aoMudar, desabilitado };

    switch (exercicio.formato) {
        case "ESCOLHA_UNICA":
            return <EscolhaUnica {...props} />;
        case "ORDENACAO":
            return <Ordenacao {...props} />;
        case "ASSOCIACAO":
            return <Associacao {...props} />;
        case "RESPOSTA_CURTA":
            return <RespostaCurta {...props} />;
        case "TEXTO_LIVRE":
            return <TextoLivre {...props} />;
        default:
            // X4: BLOCOS ainda não é suportado nesta sprint
            return <p>Este tipo de exercício ainda não está disponível.</p>;
    }
};

export default CampoResposta;
