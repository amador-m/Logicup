import { conteudoExtraDa } from "../conteudoExtra";
import { TheoryContent, ObjectivesBox, TheoryText, ExampleBox, TheoryTable } from "../styles";
import { Cabecalho, Secao, Paragrafo, CodigoInline, Lista, Balao, RotuloExemplo, FimDaTeoria } from "../estilosTeoria";
import CodigoJava from "../../../components/ui/CodigoJava";
import {
    destaqueDoParagrafo, exemploDoParagrafo, apresentaConsole, separarTermo, codigoNoParagrafo, minutosDeLeitura,
} from "../teoria";
import GattuccinoDica from "../../../assets/images/MascoteRecortado.png";
import GattuccinoAlerta from "../../../assets/images/onboarding/gattuccino-erro.png";
import GattuccinoPronto from "../../../assets/images/onboarding/gattuccino-acerto.png";

/*
 * Mostra o conteudo_teorico da rodada, no formato do banco:
 *   { objetivos: [...], teoria: [{titulo, texto, blocos?}], exemplos: [{titulo, texto, codigo}] }
 *
 * Sprint 9 (C4): cada seção pode ter "blocos" (parágrafo, lista, código, tabela).
 * Sprint 9 (visual da teoria): seções em cartões numerados com sumário, código colorido como nas IDEs,
 * listas com marcador e "termo: explicação" em destaque, balões do Gattuccino em dicas e alertas.
 * As regras de "o que vira o quê" ficam em ../teoria.js (funções puras, testadas).
 * Seções sem "blocos" (conteúdo antigo) continuam sendo mostradas pelo "texto".
 */

/** Texto com os trechos de código (ex.: System.out.println()) em fonte monoespaçada e siglas em negrito. */
function TextoComCodigo({ texto }) {
    return codigoNoParagrafo(texto).map((parte, i) => {
        if (parte.codigo) return <CodigoInline key={i}>{parte.texto}</CodigoInline>;
        if (parte.sigla) return <strong key={i}>{parte.texto}</strong>;
        return parte.texto;
    });
}

function ParagrafoTeoria({ texto }) {
    const destaque = destaqueDoParagrafo(texto);
    if (destaque) {
        return (
            <Balao className={destaque.estilo}>
                <img src={destaque.estilo === "alerta" ? GattuccinoAlerta : GattuccinoDica} alt="" />
                <div>
                    <strong>{destaque.rotulo}</strong>
                    <p><TextoComCodigo texto={texto} /></p>
                </div>
            </Balao>
        );
    }
    const exemplo = exemploDoParagrafo(texto);
    if (exemplo) {
        return (
            <>
                <RotuloExemplo>💡 {exemplo.rotulo}</RotuloExemplo>
                {exemplo.resto && <Paragrafo><TextoComCodigo texto={exemplo.resto} /></Paragrafo>}
            </>
        );
    }
    return <Paragrafo><TextoComCodigo texto={texto} /></Paragrafo>;
}

function ItemDaLista({ texto }) {
    const termo = separarTermo(texto);
    if (!termo) return <li><TextoComCodigo texto={texto} /></li>;
    return (
        <li>
            <span className={termo.codigo ? "termo codigo" : "termo"}>{termo.termo}</span>
            {termo.codigo ? " " : ": "}
            {termo.codigo ? "→ " : ""}
            <TextoComCodigo texto={termo.resto} />
        </li>
    );
}

function BlocoTeoria({ bloco, anterior }) {
    switch (bloco.tipo) {
        case "paragrafo":
            return <ParagrafoTeoria texto={bloco.texto} />;
        case "lista":
            return (
                <Lista>
                    {bloco.itens.map((item, indice) => <ItemDaLista key={indice} texto={item} />)}
                </Lista>
            );
        case "codigo":
            // "Resultado no console:" / "Exemplo de interação:" logo antes -> é a SAÍDA do programa
            return <CodigoJava codigo={bloco.codigo} variante={apresentaConsole(anterior?.texto) ? "console" : "java"} />;
        case "tabela": {
            const [cabecalho = [], ...linhas] = bloco.linhas ?? [];
            return (
                <TheoryTable>
                    <table>
                        <thead>
                            <tr>{cabecalho.map((celula, i) => <th key={i}>{celula}</th>)}</tr>
                        </thead>
                        <tbody>
                            {linhas.map((linha, i) => (
                                <tr key={i}>{linha.map((celula, j) => <td key={j}>{celula}</td>)}</tr>
                            ))}
                        </tbody>
                    </table>
                </TheoryTable>
            );
        }
        default:
            return null; // tipo desconhecido: ignora em vez de quebrar a tela inteira
    }
}

const TeoriaRodada = ({ rodada }) => {
    const { objetivos = [], teoria = [], exemplos = [] } = rodada.conteudo ?? {};
    const conteudoExtra = conteudoExtraDa(rodada.faseOrdem, rodada.ordem);
    const prefixoId = `teoria-${rodada.faseOrdem}-${rodada.ordem}`;

    return (
        <TheoryContent>
            {objetivos.length > 0 && (
                <ObjectivesBox>
                    <h3>Nesta rodada você vai</h3>
                    <ul>
                        {objetivos.map((objetivo) => <li key={objetivo}>{objetivo}</li>)}
                    </ul>
                </ObjectivesBox>
            )}

            {teoria.length > 0 && (
                <Cabecalho>
                    <span className="chip">📖 {teoria.length} {teoria.length === 1 ? "tópico" : "tópicos"}</span>
                    <span className="chip">⏱️ cerca de {minutosDeLeitura(teoria)} min de leitura</span>
                    {teoria.length > 1 && (
                        <nav aria-label="Tópicos da teoria">
                            {teoria.map((secao, i) => (
                                <a key={secao.titulo} href={`#${prefixoId}-${i + 1}`} title={secao.titulo}>{i + 1}</a>
                            ))}
                        </nav>
                    )}
                </Cabecalho>
            )}

            {teoria.map((secao, i) => (
                <Secao key={secao.titulo} id={`${prefixoId}-${i + 1}`}>
                    <h3>
                        <span className="numero" aria-hidden="true">{i + 1}</span>
                        {secao.titulo}
                    </h3>
                    {Array.isArray(secao.blocos) && secao.blocos.length > 0 ? (
                        secao.blocos.map((bloco, j) => (
                            <BlocoTeoria key={j} bloco={bloco} anterior={secao.blocos[j - 1]} />
                        ))
                    ) : (
                        <TheoryText>{secao.texto}</TheoryText>
                    )}
                </Secao>
            ))}

            {conteudoExtra}

            {exemplos.map((exemplo) => (
                <ExampleBox key={exemplo.titulo}>
                    <h3>{exemplo.titulo}</h3>
                    {exemplo.texto && <TheoryText>{exemplo.texto}</TheoryText>}
                    {exemplo.codigo && <CodigoJava codigo={exemplo.codigo} />}
                </ExampleBox>
            ))}

            {teoria.length > 0 && (
                <FimDaTeoria>
                    <img src={GattuccinoPronto} alt="" />
                    <span>Teoria concluída! Agora é com você: mostre o que aprendeu nas questões.</span>
                </FimDaTeoria>
            )}
        </TheoryContent>
    );
};

export default TeoriaRodada;
