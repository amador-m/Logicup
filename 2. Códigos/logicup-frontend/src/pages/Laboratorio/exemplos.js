/*
 * Sprint 9 (ADR-001): programas de exemplo do Laboratório, um por categoria de blocos.
 * Ficam no formato JSON do Blockly (o mesmo que o laboratório salva no navegador). As funções abaixo só
 * deixam esse JSON mais curto de escrever; o resultado é o mesmo que o Blockly salvaria.
 */
let contador = 0;
const id = () => `ex${++contador}`;

const num = (n) => ({ block: { type: "java_numero", id: id(), fields: { NUM: String(n) } } });
const txt = (t) => ({ block: { type: "java_texto", id: id(), fields: { TEXTO: t } } });
const v = (nome) => ({ block: { type: "java_variavel", id: id(), fields: { VAR: { id: `var_${nome}` } } } });
const op = (tipo, a, operador, b) => ({ block: { type: tipo, id: id(), fields: { OP: operador }, inputs: { A: a, B: b } } });
const conta = (a, operador, b) => op("java_aritmetica", a, operador, b);
const compara = (a, operador, b) => op("java_comparacao", a, operador, b);

/** Empilha comandos: cada um vira o "next" do anterior. */
function pilha(comandos) {
    if (comandos.length === 0) return undefined;
    const [primeiro, ...resto] = comandos;
    const proximo = pilha(resto);
    return { block: proximo ? { ...primeiro, next: proximo } : primeiro };
}

const declarar = (tipo, nome, valor) => ({ type: "java_declarar", id: id(), fields: { TIPO: tipo, VAR: { id: `var_${nome}` } }, inputs: { VALOR: valor } });
const atribuir = (nome, operador, valor) => ({ type: "java_atribuir", id: id(), fields: { VAR: { id: `var_${nome}` }, OP: operador }, inputs: { VALOR: valor } });
const incremento = (nome) => ({ type: "java_incremento", id: id(), fields: { VAR: { id: `var_${nome}` }, OP: "++" } });
const imprimir = (valor, metodo = "println") => ({ type: "java_imprimir", id: id(), fields: { METODO: metodo }, inputs: { TEXTO: valor } });

function programa(variaveis, comandos) {
    return {
        blocks: {
            languageVersion: 0,
            blocks: [{ type: "java_main", id: id(), x: 24, y: 24, deletable: false, inputs: { CORPO: pilha(comandos) } }],
        },
        variables: variaveis.map((nome) => ({ name: nome, id: `var_${nome}` })),
    };
}

export const EXEMPLOS = [
    {
        id: "ola",
        nome: "Olá, mundo",
        estado: programa([], [imprimir(txt("Olá, LogiCup!"))]),
    },
    {
        id: "par-impar",
        nome: "Par ou ímpar (if / else)",
        estado: programa(["numero"], [
            declarar("int", "numero", num(7)),
            {
                type: "java_if_else", id: id(),
                inputs: {
                    COND: compara(conta(v("numero"), "%", num(2)), "==", num(0)),
                    FAZER: pilha([imprimir(conta(v("numero"), "+", txt(" é par")))]),
                    SENAO: pilha([imprimir(conta(v("numero"), "+", txt(" é ímpar")))]),
                },
            },
        ]),
    },
    {
        id: "contagem",
        nome: "Contagem regressiva (for)",
        estado: programa(["i"], [
            {
                type: "java_for", id: id(), fields: { VAR: { id: "var_i" }, OP: ">=", PASSO: "--" },
                inputs: { INICIO: num(5), FIM: num(1), FAZER: pilha([imprimir(conta(v("i"), "+", txt("... ")), "print")]) },
            },
            imprimir(txt("Decolar!")),
        ]),
    },
    {
        id: "pontos",
        nome: "Somando pontos (while)",
        estado: programa(["pontos", "rodada"], [
            declarar("int", "pontos", num(0)),
            declarar("int", "rodada", num(1)),
            {
                type: "java_while", id: id(),
                inputs: {
                    COND: compara(v("rodada"), "<=", num(3)),
                    FAZER: pilha([
                        atribuir("pontos", "+=", num(10)),
                        imprimir(conta(conta(conta(conta(txt("Rodada "), "+", v("rodada")), "+", txt(": ")), "+", v("pontos")), "+", txt(" pontos"))),
                        incremento("rodada"),
                    ]),
                },
            },
        ]),
    },
];
