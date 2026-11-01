import { destacarJava, nomeDoArquivo } from "../../../util/destacarJava";
import { Janela, Barra, Corpo, Linhas, Codigo } from "./styles";

/*
 * Sprint 9: bloco de código com cara de IDE — barra com a "aba" do arquivo, números de linha e cores
 * (palavras-chave, textos, comentários, números, tipos e métodos).
 *
 * variante:
 *   "java"    (padrão) -> código Java colorido
 *   "console" -> saída do programa: sem cores de sintaxe, com a aba "Console"
 *   "texto"   -> texto monoespaçado sem cores (ex.: fluxograma em texto da F1.Q8)
 * titulo: opcional; se não vier, usa o nome da classe ("public class Main" -> "Main.java")
 *
 * Acessibilidade: a numeração de linhas é aria-hidden (o leitor de tela lê só o código).
 */
const CodigoJava = ({ codigo, variante = "java", titulo }) => {
    const linhas = codigo.split("\n");
    const ehConsole = variante === "console";
    const semCores = variante !== "java";
    const rotulo = titulo ?? (ehConsole ? "Console" : variante === "texto" ? "Texto" : nomeDoArquivo(codigo));

    return (
        <Janela className={ehConsole ? "console" : ""}>
            <Barra aria-hidden="true">
                <span className="bolinhas"><i /><i /><i /></span>
                <span className="aba">{ehConsole ? "▶ " : ""}{rotulo}</span>
            </Barra>
            <Corpo>
                <Linhas aria-hidden="true">
                    {linhas.map((_, i) => <span key={i}>{i + 1}</span>)}
                </Linhas>
                <Codigo>
                    {semCores
                        ? codigo
                        : destacarJava(codigo).map((token, i) =>
                              token.tipo === "simples" ? token.texto : <span key={i} className={token.tipo}>{token.texto}</span>
                          )}
                </Codigo>
            </Corpo>
        </Janela>
    );
};

export default CodigoJava;
