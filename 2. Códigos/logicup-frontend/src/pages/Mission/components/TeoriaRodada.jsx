import { conteudoExtraDa } from "../conteudoExtra";
import { TheoryContent, ObjectivesBox, TheoryText, ExampleBox, CodeBox } from "../styles";

/*
 * Mostra o conteudo_teorico da rodada, no formato do banco:
 *   { objetivos: [...], teoria: [{titulo, texto}], exemplos: [{titulo, texto, codigo}] }
 * O conteúdo extra (I3) entra depois da teoria e antes dos exemplos.
 */
const TeoriaRodada = ({ rodada }) => {
    const { objetivos = [], teoria = [], exemplos = [] } = rodada.conteudo ?? {};
    const conteudoExtra = conteudoExtraDa(rodada.faseOrdem, rodada.ordem);

    return (
        <TheoryContent>
            {objetivos.length > 0 && (
                <ObjectivesBox>
                    <h3>Nesta rodada você vai</h3>
                    <ul>
                        {objetivos.map((objetivo) => (
                            <li key={objetivo}>{objetivo}</li>
                        ))}
                    </ul>
                </ObjectivesBox>
            )}

            {teoria.map((secao) => (
                <section key={secao.titulo}>
                    <h3>{secao.titulo}</h3>
                    <TheoryText>{secao.texto}</TheoryText>
                </section>
            ))}

            {conteudoExtra}

            {exemplos.map((exemplo) => (
                <ExampleBox key={exemplo.titulo}>
                    <h3>{exemplo.titulo}</h3>
                    {exemplo.texto && <TheoryText>{exemplo.texto}</TheoryText>}
                    {exemplo.codigo && <CodeBox>{exemplo.codigo}</CodeBox>}
                </ExampleBox>
            ))}
        </TheoryContent>
    );
};

export default TeoriaRodada;
