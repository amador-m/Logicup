import * as Blockly from "blockly/core";

/*
 * Sprint 9 (ADR-001): o GERADOR DE JAVA dos blocos. O Blockly traz geradores para JavaScript, Python,
 * PHP, Lua e Dart, mas não para Java, então este é nosso.
 *
 * Como funciona (é a mesma ideia de um compilador, só que de blocos para texto):
 *  - os blocos formam uma ÁRVORE: o "if" tem uma condição, a condição tem uma comparação, a comparação
 *    tem dois valores...;
 *  - para cada TIPO de bloco existe uma função em gerador.forBlock[tipo] que devolve o trecho de Java dele;
 *  - para os blocos de dentro, a função pede ao gerador o código do filho (valueToCode para valores,
 *    statementToCode para comandos), e assim a árvore inteira é percorrida (recursão).
 *
 * PRECEDÊNCIA: os blocos de valor devolvem [código, ordem]. A "ordem" diz quão forte o operador "gruda"
 * (multiplicação gruda mais que soma). O Blockly coloca parênteses SÓ quando são necessários:
 * o bloco "(2 + 3) * 4" gera "(2 + 3) * 4", e o bloco "2 + (3 * 4)" gera "2 + 3 * 4".
 * Números menores = gruda mais (a tabela segue a precedência de operadores do Java). Os números andam de
 * 2 em 2 para sobrar o "ímpar" do meio, usado logo abaixo.
 */
export const ORDEM = {
    ATOMICO: 0,         // literal, variável
    POS_FIXO: 2,        // x++
    UNARIO: 4,          // !x, -5
    MULTIPLICATIVO: 6,  // * / %
    ADITIVO: 8,         // + -
    RELACIONAL: 10,     // < <= > >=
    IGUALDADE: 12,      // == !=
    E_LOGICO: 14,       // &&
    OU_LOGICO: 16,      // ||
    NENHUMA: 99,
};

/*
 * Associatividade à ESQUERDA: "a - b - c" é "(a - b) - c". Então o operando da ESQUERDA pode ter a mesma
 * força sem parênteses, mas o da DIREITA precisa deles: "a - (b - c)" é outra conta.
 * O Blockly coloca parênteses quando ordemDeFora <= ordemDeDentro (comparando números inteiros);
 * somar 1 do lado esquerdo evita os parênteses que não mudam nada.
 */
const ESQUERDA = 1;

const ORDEM_DO_OPERADOR = {
    "*": ORDEM.MULTIPLICATIVO, "/": ORDEM.MULTIPLICATIVO, "%": ORDEM.MULTIPLICATIVO,
    "+": ORDEM.ADITIVO, "-": ORDEM.ADITIVO,
    "<": ORDEM.RELACIONAL, "<=": ORDEM.RELACIONAL, ">": ORDEM.RELACIONAL, ">=": ORDEM.RELACIONAL,
    "==": ORDEM.IGUALDADE, "!=": ORDEM.IGUALDADE,
    "&&": ORDEM.E_LOGICO, "||": ORDEM.OU_LOGICO,
};

/** O que aparece no lugar de um encaixe vazio (o Java não compila, e a tela avisa antes). */
export const BURACO = "___";

export const gerador = new Blockly.CodeGenerator("Java");
gerador.INDENT = "    ";

/*
 * Os encaixes vazios são anotados aqui durante a geração (ids dos blocos), para a tela destacar o bloco
 * e pedir para o aluno completar ANTES de gastar uma execução.
 */
let faltando = [];

/** Código de um encaixe de valor; se estiver vazio, anota o bloco e devolve o BURACO. */
function valor(bloco, nome, ordem, opcional = false) {
    const codigo = gerador.valueToCode(bloco, nome, ordem);
    if (codigo === "" && !opcional) {
        faltando.push(bloco.id);
        return BURACO;
    }
    return codigo;
}

const nomeDa = (bloco, campo = "VAR") => bloco.getField(campo).getText();

/** Liga os comandos empilhados: depois do código de um bloco, vem o do bloco de baixo. */
gerador.scrub_ = function (bloco, codigo, soEste) {
    const proximo = bloco.nextConnection?.targetBlock();
    return soEste || !proximo ? codigo : codigo + gerador.blockToCode(proximo);
};

/** Texto Java: escapa barra invertida, aspas e quebras de linha. */
export function literalTexto(texto) {
    const escapado = texto.replace(/\\/g, "\\\\").replace(/"/g, "\\\"").replace(/\n/g, "\\n").replace(/\t/g, "\\t");
    return `"${escapado}"`;
}

const f = gerador.forBlock;

f.java_main = (bloco) => gerador.statementToCode(bloco, "CORPO");

f.java_declarar = (bloco) => {
    const inicial = valor(bloco, "VALOR", ORDEM.NENHUMA, true); // "int x;" também é Java válido
    const tipo = bloco.getFieldValue("TIPO");
    return inicial ? `${tipo} ${nomeDa(bloco)} = ${inicial};\n` : `${tipo} ${nomeDa(bloco)};\n`;
};
f.java_atribuir = (bloco) => `${nomeDa(bloco)} ${bloco.getFieldValue("OP")} ${valor(bloco, "VALOR", ORDEM.NENHUMA)};\n`;
f.java_incremento = (bloco) => `${nomeDa(bloco)}${bloco.getFieldValue("OP")};\n`;

f.java_imprimir = (bloco) => {
    const metodo = bloco.getFieldValue("METODO");
    // println() sem nada é uma linha em branco; print() sem nada não existe no Java
    const texto = valor(bloco, "TEXTO", ORDEM.NENHUMA, metodo === "println");
    return `System.out.${metodo}(${texto});\n`;
};

f.java_numero = (bloco) => {
    // o texto como o aluno digitou: "2.0" continua "2.0" (no Java, 7 / 2 = 3, mas 7 / 2.0 = 3.5)
    const codigo = bloco.getFieldValue("NUM");
    return [codigo, codigo.startsWith("-") ? ORDEM.UNARIO : ORDEM.ATOMICO];
};
f.java_texto = (bloco) => [literalTexto(bloco.getFieldValue("TEXTO")), ORDEM.ATOMICO];
f.java_booleano = (bloco) => [bloco.getFieldValue("VALOR"), ORDEM.ATOMICO];
f.java_variavel = (bloco) => [nomeDa(bloco), ORDEM.ATOMICO];

function binario(bloco) {
    const operador = bloco.getFieldValue("OP");
    const ordem = ORDEM_DO_OPERADOR[operador];
    const a = valor(bloco, "A", ordem + ESQUERDA);
    const b = valor(bloco, "B", ordem);
    return [`${a} ${operador} ${b}`, ordem];
}
f.java_aritmetica = binario;
f.java_comparacao = binario;
f.java_logica = binario;
f.java_nao = (bloco) => [`!${valor(bloco, "A", ORDEM.UNARIO)}`, ORDEM.UNARIO];

/** Bloco de comandos entre chaves: o corpo vem indentado pelo statementToCode. */
const corpo = (bloco, nome) => gerador.statementToCode(bloco, nome);

f.java_if = (bloco) =>
    `if (${valor(bloco, "COND", ORDEM.NENHUMA)}) {\n${corpo(bloco, "FAZER")}}\n`;
f.java_if_else = (bloco) =>
    `if (${valor(bloco, "COND", ORDEM.NENHUMA)}) {\n${corpo(bloco, "FAZER")}} else {\n${corpo(bloco, "SENAO")}}\n`;
f.java_if_elseif_else = (bloco) =>
    `if (${valor(bloco, "COND", ORDEM.NENHUMA)}) {\n${corpo(bloco, "FAZER")}`
    + `} else if (${valor(bloco, "COND2", ORDEM.NENHUMA)}) {\n${corpo(bloco, "FAZER2")}`
    + `} else {\n${corpo(bloco, "SENAO")}}\n`;

f.java_for = (bloco) => {
    const v = nomeDa(bloco);
    const inicio = valor(bloco, "INICIO", ORDEM.NENHUMA);
    const fim = valor(bloco, "FIM", ORDEM.RELACIONAL);
    return `for (int ${v} = ${inicio}; ${v} ${bloco.getFieldValue("OP")} ${fim}; ${v}${bloco.getFieldValue("PASSO")}) {\n`
        + `${corpo(bloco, "FAZER")}}\n`;
};
f.java_while = (bloco) => `while (${valor(bloco, "COND", ORDEM.NENHUMA)}) {\n${corpo(bloco, "FAZER")}}\n`;
f.java_do_while = (bloco) => `do {\n${corpo(bloco, "FAZER")}} while (${valor(bloco, "COND", ORDEM.NENHUMA)});\n`;

// ---------------------------------------------------------------- blocos só das questões (03/11)
// o algoritmo da Fase 1 é o "main" dessas questões; cada passo vira uma linha exibida
f.java_algoritmo = (bloco) => gerador.statementToCode(bloco, "CORPO");
f.java_passo = (bloco) => `System.out.println(${literalTexto(bloco.getFieldValue("TEXTO"))});\n`;
// blocos de linha: o texto vai como está; o bloco com chaves põe o corpo indentado e fecha com "}"
f.java_linha = (bloco) => `${bloco.getFieldValue("TEXTO")}\n`;
f.java_bloco = (bloco) => `${bloco.getFieldValue("TEXTO")}\n${corpo(bloco, "CORPO")}}\n`;
f.java_raiz = f.java_bloco;

/** Blocos que fazem o papel do main (o trecho é o que está DENTRO deles). */
const TIPOS_MAIN = ["java_main", "java_algoritmo"];

/** Coloca "prefixo" no começo de cada linha não vazia. */
const indentar = (texto, prefixo) => texto.split("\n").map((linha) => (linha ? prefixo + linha : linha)).join("\n");

/**
 * Gera o Java da área de blocos.
 *
 * Devolve:
 *  trecho     -> só os comandos de DENTRO do main, sem indentação (é o que a questão de blocos envia,
 *                igual ao trecho de uma questão de código)
 *  programa   -> o programa COMPLETO, com a classe Main (é o que o laboratório executa e mostra)
 *  faltando   -> ids dos blocos com encaixe vazio
 *  foraDoMain -> quantos grupos de blocos estão soltos, fora do main (o Java não executa esses)
 *  temMain    -> false se o bloco main não existir
 *  principal  -> o tipo do bloco de fora: "java_main", "java_algoritmo" (Fase 1) ou "java_raiz"
 *                (blocos de linha: aí o trecho é o próprio bloco de fora, com a linha dele e a "}")
 */
export function gerarJava(area) {
    faltando = [];
    gerador.init(area);
    const blocosDoTopo = area.getTopBlocks(true);
    const main = blocosDoTopo.find((bloco) => TIPOS_MAIN.includes(bloco.type) || bloco.type === "java_raiz");
    const foraDoMain = blocosDoTopo.filter((bloco) => bloco !== main).length;

    let trecho = "";
    if (main?.type === "java_raiz") {
        trecho = gerador.blockToCode(main);
    } else if (main) {
        // statementToCode indenta um nível; o trecho da questão vai sem indentação
        trecho = gerador.blockToCode(main).split("\n").map((linha) => linha.replace(/^ {4}/, "")).join("\n");
    }
    const programa = "public class Main {\n"
        + "    public static void main(String[] args) {\n"
        + indentar(trecho, "        ")
        + "    }\n"
        + "}\n";

    return {
        trecho, programa, faltando: [...new Set(faltando)], foraDoMain,
        temMain: Boolean(main), principal: main?.type ?? null,
    };
}
