import { ShortAnswerInput, LacunasGrupo } from "../../styles";
import { quantidadeDeLacunas } from "../../respostas";

/*
 * RESPOSTA_CURTA — a comparação com o gabarito é feita no backend.
 *
 * Dois formatos (Sprint 9, D3):
 *   - uma lacuna (padrão):  resposta {"texto": "..."}
 *   - várias lacunas:       alternativas = {"lacunas": 2}  ->  resposta {"lacunas": ["...", "..."]}
 *     O número de lacunas é PÚBLICO (vem em alternativas); as respostas aceitas ficam só no backend.
 *     Os campos aparecem na mesma ordem dos "___" do código (1ª lacuna, 2ª lacuna...).
 */
const RespostaCurta = ({ exercicio, valor, aoMudar, desabilitado }) => {
    const lacunas = quantidadeDeLacunas(exercicio);

    if (lacunas === 0) {
        return (
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
    }

    // Sempre um array com o tamanho certo, mesmo antes de o aluno digitar algo
    const atuais = Array.from({ length: lacunas }, (_, i) => valor?.lacunas?.[i] ?? "");

    const mudarLacuna = (indice, texto) => {
        const novas = [...atuais];
        novas[indice] = texto;
        aoMudar({ lacunas: novas });
    };

    return (
        <LacunasGrupo role="group" aria-label={`Lacunas da questão ${exercicio.titulo}`}>
            {atuais.map((texto, indice) => (
                <label key={indice}>
                    <span>{indice + 1}ª lacuna</span>
                    <ShortAnswerInput
                        type="text"
                        placeholder={`Resposta da ${indice + 1}ª lacuna`}
                        maxLength={100}
                        value={texto}
                        disabled={desabilitado}
                        onChange={(evento) => mudarLacuna(indice, evento.target.value)}
                    />
                </label>
            ))}
        </LacunasGrupo>
    );
};

export default RespostaCurta;
