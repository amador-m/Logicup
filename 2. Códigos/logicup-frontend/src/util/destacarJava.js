/*
 * Sprint 9: "syntax highlighting" de Java, do jeito das IDEs (IntelliJ/VS Code), SEM biblioteca externa.
 *
 * Como funciona (um "tokenizador" simples):
 *   o código é percorrido da esquerda para a direita; em cada posição, tentamos as regras abaixo NA ORDEM.
 *   A primeira que casar "come" aquele trecho e vira um token {tipo, texto}. Se nenhuma casar, o caractere
 *   vira texto comum. A ordem importa: comentário e texto entre aspas vêm primeiro, porque dentro deles
 *   palavras como "public" NÃO devem ser coloridas como palavra-chave.
 *
 * Não é um compilador: é só para COLORIR. Por isso regras simples bastam para o código da trilha.
 */

const PALAVRAS_CHAVE = new Set([
    "abstract", "boolean", "break", "byte", "case", "catch", "char", "class", "continue", "default", "do",
    "double", "else", "enum", "extends", "final", "finally", "float", "for", "if", "implements", "import",
    "instanceof", "int", "interface", "long", "new", "package", "private", "protected", "public", "return",
    "short", "static", "super", "switch", "this", "throw", "throws", "try", "void", "while", "var",
    "true", "false", "null",
]);

// Cada regra: [tipo do token, expressão que precisa casar NO INÍCIO do trecho restante (flag "y" = sticky)]
const REGRAS = [
    ["comentario", /\/\/[^\n]*/y],                 // comentário de linha
    ["comentario", /\/\*[\s\S]*?(?:\*\/|$)/y],     // comentário de bloco /* ... */
    ["texto", /"(?:\\.|[^"\\\n])*"?/y],            // "texto entre aspas"
    ["texto", /'(?:\\.|[^'\\\n])'?/y],             // 'c' (char)
    ["lacuna", /_{3,}/y],                           // ____ espaço que o aluno completa (questões)
    ["anotacao", /@[A-Za-z_]\w*/y],                // @Override
    ["numero", /\b\d+(?:\.\d+)?[fFdDlL]?\b/y],     // 10, 8.5, 2.0f
    ["palavra", /[A-Za-z_$][\w$]*/y],              // identificador: decidido abaixo (palavra-chave, tipo, método...)
];

/** Classifica um identificador olhando o próprio texto e o que vem logo depois dele. */
function tipoDaPalavra(palavra, resto) {
    if (PALAVRAS_CHAVE.has(palavra)) return "palavraChave";
    if (/^\s*\(/.test(resto)) return "metodo";             // seguido de "(" -> chamada/declaração de método
    if (/^[A-Z]/.test(palavra)) return "tipo";             // Começa com maiúscula -> classe (String, Scanner...)
    return "identificador";
}

/**
 * Transforma o código em tokens. Ex.: 'int x = 1;' ->
 *   [{tipo:"palavraChave",texto:"int"}, {tipo:"simples",texto:" x = "}, {tipo:"numero",texto:"1"}, ...]
 * Tokens "simples" vizinhos são juntados (menos elementos para o React desenhar).
 */
export function destacarJava(codigo) {
    const tokens = [];
    const empurrar = (tipo, texto) => {
        const ultimo = tokens[tokens.length - 1];
        if (tipo === "simples" && ultimo?.tipo === "simples") ultimo.texto += texto;
        else tokens.push({ tipo, texto });
    };

    let i = 0;
    while (i < codigo.length) {
        let casou = false;
        for (const [tipo, regra] of REGRAS) {
            regra.lastIndex = i;
            const m = regra.exec(codigo);
            if (m && m[0].length > 0) {
                const texto = m[0];
                empurrar(tipo === "palavra" ? tipoDaPalavra(texto, codigo.slice(i + texto.length)) : tipo, texto);
                i += texto.length;
                casou = true;
                break;
            }
        }
        if (!casou) {
            empurrar("simples", codigo[i]);
            i += 1;
        }
    }
    return tokens;
}

/** Nome de arquivo para a "aba" do editor: "public class OlaMundo" -> "OlaMundo.java"; senão, "Java". */
export function nomeDoArquivo(codigo) {
    const m = /\bclass\s+([A-Z]\w*)/.exec(codigo);
    return m ? `${m[1]}.java` : "Java";
}
