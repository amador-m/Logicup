import * as Blockly from "blockly/core";

/*
 * Sprint 9 (ADR-001): a CAIXA DE FERRAMENTAS (toolbox) do laboratório: as categorias do lado esquerdo,
 * na ordem das fases da trilha (Variáveis -> ... -> Repetição).
 *
 * "shadow" = um bloco-sombra já encaixado (clarinho). Ele mostra um valor de exemplo e some quando o aluno
 * encaixa outro bloco por cima. Assim "System.out.println" já vem com um texto pronto para editar.
 */
const numero = (valor) => ({ shadow: { type: "java_numero", fields: { NUM: String(valor) } } });
const texto = (valor) => ({ shadow: { type: "java_texto", fields: { TEXTO: valor } } });

/*
 * Visual da categoria: o Blockly deixa trocar as classes CSS de cada parte (cssconfig). Acrescentamos na
 * linha da categoria uma classe com o nome dela; o CSS (AreaBlocos/styles.js) desenha a bolinha colorida.
 */
const visual = (nome) => ({ row: `blocklyToolboxCategory logicup-categoria logicup-categoria-${nome}` });

export const CATEGORIA_VARIAVEIS = "LOGICUP_VARIAVEIS";
export const BOTAO_CRIAR_VARIAVEL = "logicupCriarVariavel";

export const CAIXA_COMPLETA = {
    kind: "categoryToolbox",
    contents: [
        { kind: "category", name: "Variáveis", categorystyle: "categoria_variaveis", cssconfig: visual("variaveis"), custom: CATEGORIA_VARIAVEIS },
        {
            kind: "category", name: "Saída", categorystyle: "categoria_saida", cssconfig: visual("saida"),
            contents: [
                { kind: "block", type: "java_imprimir", inputs: { TEXTO: texto("Olá, LogiCup!") } },
                { kind: "block", type: "java_imprimir", fields: { METODO: "print" }, inputs: { TEXTO: texto("sem pular linha ") } },
            ],
        },
        {
            kind: "category", name: "Valores", categorystyle: "categoria_valores", cssconfig: visual("valores"),
            contents: [
                { kind: "block", type: "java_numero", fields: { NUM: "10" } },
                { kind: "block", type: "java_texto", fields: { TEXTO: "texto" } },
                { kind: "block", type: "java_booleano" },
            ],
        },
        {
            kind: "category", name: "Operadores", categorystyle: "categoria_operadores", cssconfig: visual("operadores"),
            contents: [
                { kind: "block", type: "java_aritmetica", inputs: { A: numero(1), B: numero(1) } },
                { kind: "block", type: "java_aritmetica", fields: { OP: "%" }, inputs: { A: numero(10), B: numero(3) } },
                { kind: "block", type: "java_comparacao", fields: { OP: "<" }, inputs: { A: numero(1), B: numero(2) } },
                { kind: "block", type: "java_logica" },
                { kind: "block", type: "java_nao" },
            ],
        },
        {
            kind: "category", name: "Condicionais", categorystyle: "categoria_condicionais", cssconfig: visual("condicionais"),
            contents: [
                { kind: "block", type: "java_if" },
                { kind: "block", type: "java_if_else" },
                { kind: "block", type: "java_if_elseif_else" },
            ],
        },
        {
            kind: "category", name: "Repetição", categorystyle: "categoria_repeticao", cssconfig: visual("repeticao"),
            contents: [
                { kind: "block", type: "java_for", inputs: { INICIO: numero(1), FIM: numero(5) }, fields: { OP: "<=" } },
                { kind: "block", type: "java_while" },
                { kind: "block", type: "java_do_while" },
            ],
        },
    ],
};

/*
 * A categoria Variáveis é DINÂMICA: o conteúdo depende das variáveis que o aluno já criou.
 * O Blockly chama esta função toda vez que a categoria é aberta.
 */
function conteudoVariaveis(area) {
    const variaveis = area.getVariableMap().getAllVariables()
        .sort((a, b) => a.getName().localeCompare(b.getName()));
    const itens = [{ kind: "button", text: "+ Criar variável", callbackKey: BOTAO_CRIAR_VARIAVEL }];
    if (variaveis.length === 0) {
        itens.push({ kind: "label", text: "Crie uma variável para começar." });
        return itens;
    }
    const primeira = { VAR: { name: variaveis[0].getName() } };
    itens.push(
        { kind: "block", type: "java_declarar", fields: primeira, inputs: { VALOR: numero(0) } },
        { kind: "block", type: "java_atribuir", fields: primeira, inputs: { VALOR: numero(1) } },
        { kind: "block", type: "java_incremento", fields: primeira },
        { kind: "label", text: "Usar o valor:" },
        ...variaveis.map((variavel) => ({ kind: "block", type: "java_variavel", fields: { VAR: { name: variavel.getName() } } })),
    );
    return itens;
}

/*
 * Nome de variável válido no Java: começa com letra, _ ou $, e não pode ser palavra reservada.
 * Recusamos acentos de propósito (o Java aceita, mas é fonte comum de confusão para quem está começando).
 */
const RESERVADAS = new Set((
    "abstract assert boolean break byte case catch char class const continue default do double else enum extends "
    + "final finally float for goto if implements import instanceof int interface long native new package private "
    + "protected public return short static strictfp super switch synchronized this throw throws transient try void "
    + "volatile while true false null var record yield String System"
).split(" "));

export function problemaNoNome(nome) {
    if (!nome) return "Digite um nome.";
    if (!/^[A-Za-z_$][A-Za-z0-9_$]*$/.test(nome)) {
        return "Use só letras sem acento, números e _ (e comece com uma letra). Ex.: baus, totalDePontos.";
    }
    if (RESERVADAS.has(nome)) return `"${nome}" é uma palavra que o Java já usa. Escolha outro nome.`;
    return null;
}

/** Liga a categoria dinâmica e o botão "Criar variável" a uma área de blocos. */
export function ligarCategoriaVariaveis(area) {
    area.registerToolboxCategoryCallback(CATEGORIA_VARIAVEIS, conteudoVariaveis);
    area.registerButtonCallback(BOTAO_CRIAR_VARIAVEL, (botao) =>
        Blockly.Variables.createVariableButtonHandler(botao.getTargetWorkspace()));
}
