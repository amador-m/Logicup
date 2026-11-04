import * as Blockly from "blockly/core";
import * as PtBr from "blockly/msg/pt-br";

/*
 * Sprint 9 (ADR-001, etapa 1 do Blockly): os BLOCOS do LogiCup, até a categoria "Repetição".
 *
 * Decisões (02/11):
 *  - o texto dos blocos usa as PALAVRAS DO JAVA (if, for, System.out.println), e a explicação em
 *    português fica no balão de ajuda (tooltip). Assim o aluno já se acostuma com o código de verdade;
 *  - variáveis têm bloco de DECLARAÇÃO explícito com o tipo ("int baus = 0;"), como o Java exige.
 *
 * Cada bloco é descrito em JSON (formato do próprio Blockly):
 *   message0  -> o texto do bloco; %1, %2... são os "furos" (args0) na ordem
 *   args0     -> o que vai em cada furo: field_* (valor fixo no bloco) ou input_* (encaixe de outro bloco)
 *   output    -> o bloco É UM VALOR (encaixa dentro de outro): número, texto, condição...
 *   previousStatement / nextStatement -> o bloco é um COMANDO (empilha em cima/embaixo de outro)
 *   style     -> a cor, definida no tema (temaLogicup.js) por categoria
 *
 * Quem transforma os blocos em Java é o geradorJava.js.
 */

export const TIPOS = [
    ["int", "int"],
    ["double", "double"],
    ["String", "String"],
    ["boolean", "boolean"],
];

const DEFINICOES = [
    // ------------------------------------------------------------------ programa
    {
        type: "java_main",
        message0: "public static void main(String[] args) {",
        message1: "%1",
        args1: [{ type: "input_statement", name: "CORPO" }],
        message2: "}",
        style: "estilo_programa",
        tooltip: "O main é onde o programa COMEÇA. O Java executa, de cima para baixo, só os blocos encaixados aqui dentro.",
    },

    // ------------------------------------------------------------------ variáveis (comandos)
    {
        type: "java_declarar",
        message0: "%1 %2 = %3 ;",
        args0: [
            { type: "field_dropdown", name: "TIPO", options: TIPOS },
            { type: "field_variable", name: "VAR", variable: null },
            { type: "input_value", name: "VALOR" },
        ],
        inputsInline: true,
        previousStatement: null,
        nextStatement: null,
        style: "estilo_variaveis",
        extensions: ["logicup_valor_do_tipo"],
        tooltip: "DECLARAR: cria a variável com um tipo (int = inteiro, double = com vírgula, String = texto, "
            + "boolean = verdadeiro/falso) e guarda o primeiro valor. Cada variável é declarada uma vez só.",
    },
    {
        type: "java_atribuir",
        message0: "%1 %2 %3 ;",
        args0: [
            { type: "field_variable", name: "VAR", variable: null },
            { type: "field_dropdown", name: "OP", options: [["=", "="], ["+=", "+="], ["-=", "-="], ["*=", "*="], ["/=", "/="]] },
            { type: "input_value", name: "VALOR" },
        ],
        inputsInline: true,
        previousStatement: null,
        nextStatement: null,
        style: "estilo_variaveis",
        tooltip: "ATRIBUIR: troca o valor de uma variável que já foi declarada. "
            + "\"x += 2\" é o mesmo que \"x = x + 2\".",
    },
    {
        type: "java_incremento",
        message0: "%1 %2 ;",
        args0: [
            { type: "field_variable", name: "VAR", variable: null },
            { type: "field_dropdown", name: "OP", options: [["++", "++"], ["--", "--"]] },
        ],
        previousStatement: null,
        nextStatement: null,
        style: "estilo_variaveis",
        tooltip: "\"x++\" soma 1 na variável; \"x--\" tira 1. Muito usado em contadores.",
    },

    // ------------------------------------------------------------------ saída
    {
        type: "java_imprimir",
        message0: "System.out. %1 ( %2 ) ;",
        args0: [
            { type: "field_dropdown", name: "METODO", options: [["println", "println"], ["print", "print"]] },
            { type: "input_value", name: "TEXTO" },
        ],
        inputsInline: true,
        previousStatement: null,
        nextStatement: null,
        style: "estilo_saida",
        tooltip: "Mostra um valor no console. println pula para a próxima linha depois; print continua na mesma linha.",
    },

    // ------------------------------------------------------------------ valores
    {
        type: "java_numero",
        message0: "%1",
        args0: [{ type: "field_input", name: "NUM", text: "0" }],
        output: "Number",
        style: "estilo_valores",
        extensions: ["logicup_validar_numero"],
        tooltip: "Um número. Sem ponto é inteiro (int); com ponto é decimal (double), como 2.5. "
            + "Atenção: no Java, 7 / 2 dá 3 (divisão de inteiros), e 7 / 2.0 dá 3.5.",
    },
    {
        type: "java_texto",
        message0: "\" %1 \"",
        args0: [{ type: "field_input", name: "TEXTO", text: "" }],
        output: "String",
        style: "estilo_valores",
        tooltip: "Um texto (String). No Java, todo texto fica entre aspas duplas.",
    },
    {
        type: "java_booleano",
        message0: "%1",
        args0: [{ type: "field_dropdown", name: "VALOR", options: [["true", "true"], ["false", "false"]] }],
        output: "Boolean",
        style: "estilo_valores",
        tooltip: "Um valor lógico: true (verdadeiro) ou false (falso).",
    },
    {
        type: "java_variavel",
        message0: "%1",
        args0: [{ type: "field_variable", name: "VAR", variable: null }],
        output: null,
        style: "estilo_variaveis",
        tooltip: "Usa o valor guardado na variável.",
    },

    // ------------------------------------------------------------------ operadores
    {
        type: "java_aritmetica",
        message0: "%1 %2 %3",
        args0: [
            { type: "input_value", name: "A" },
            { type: "field_dropdown", name: "OP", options: [["+", "+"], ["-", "-"], ["*", "*"], ["/", "/"], ["%", "%"]] },
            { type: "input_value", name: "B" },
        ],
        inputsInline: true,
        output: null,
        style: "estilo_operadores",
        tooltip: "Conta: + - * /. O % dá o RESTO da divisão (10 % 3 = 1). "
            + "Com texto, o + JUNTA: \"Baús: \" + 6 vira \"Baús: 6\".",
    },
    {
        type: "java_comparacao",
        message0: "%1 %2 %3",
        args0: [
            { type: "input_value", name: "A" },
            {
                type: "field_dropdown", name: "OP",
                options: [["==", "=="], ["!=", "!="], ["<", "<"], ["<=", "<="], [">", ">"], [">=", ">="]],
            },
            { type: "input_value", name: "B" },
        ],
        inputsInline: true,
        output: "Boolean",
        style: "estilo_operadores",
        tooltip: "Compara dois valores e dá true ou false. == é \"igual a\" (um = só é atribuição); != é \"diferente de\".",
    },
    {
        type: "java_logica",
        message0: "%1 %2 %3",
        args0: [
            { type: "input_value", name: "A", check: "Boolean" },
            { type: "field_dropdown", name: "OP", options: [["&&", "&&"], ["||", "||"]] },
            { type: "input_value", name: "B", check: "Boolean" },
        ],
        inputsInline: true,
        output: "Boolean",
        style: "estilo_operadores",
        tooltip: "&& (E): true só se as DUAS condições forem true. || (OU): true se PELO MENOS UMA for true.",
    },
    {
        type: "java_nao",
        message0: "! %1",
        args0: [{ type: "input_value", name: "A", check: "Boolean" }],
        output: "Boolean",
        style: "estilo_operadores",
        tooltip: "! (NÃO): inverte a condição. !true é false.",
    },

    // ------------------------------------------------------------------ condicionais
    {
        type: "java_if",
        message0: "if ( %1 ) {",
        args0: [{ type: "input_value", name: "COND", check: "Boolean" }],
        message1: "%1",
        args1: [{ type: "input_statement", name: "FAZER" }],
        message2: "}",
        previousStatement: null,
        nextStatement: null,
        style: "estilo_condicionais",
        tooltip: "if (SE): executa os blocos de dentro só quando a condição for true.",
    },
    {
        type: "java_if_else",
        message0: "if ( %1 ) {",
        args0: [{ type: "input_value", name: "COND", check: "Boolean" }],
        message1: "%1",
        args1: [{ type: "input_statement", name: "FAZER" }],
        message2: "} else {",
        message3: "%1",
        args3: [{ type: "input_statement", name: "SENAO" }],
        message4: "}",
        previousStatement: null,
        nextStatement: null,
        style: "estilo_condicionais",
        tooltip: "if / else (SE / SENÃO): um dos dois caminhos SEMPRE acontece, conforme a condição.",
    },
    {
        type: "java_if_elseif_else",
        message0: "if ( %1 ) {",
        args0: [{ type: "input_value", name: "COND", check: "Boolean" }],
        message1: "%1",
        args1: [{ type: "input_statement", name: "FAZER" }],
        message2: "} else if ( %1 ) {",
        args2: [{ type: "input_value", name: "COND2", check: "Boolean" }],
        message3: "%1",
        args3: [{ type: "input_statement", name: "FAZER2" }],
        message4: "} else {",
        message5: "%1",
        args5: [{ type: "input_statement", name: "SENAO" }],
        message6: "}",
        previousStatement: null,
        nextStatement: null,
        style: "estilo_condicionais",
        tooltip: "Três caminhos: o Java testa as condições DE CIMA PARA BAIXO e executa só o primeiro que der true. "
            + "Se nenhuma der, executa o else.",
    },

    // ------------------------------------------------------------------ repetição
    {
        type: "java_for",
        message0: "for (int %1 = %2 ; %3 %4 %5 ; %6 %7 ) {",
        args0: [
            { type: "field_variable", name: "VAR", variable: "i" },
            { type: "input_value", name: "INICIO", check: "Number" },
            { type: "field_label", name: "VAR_COND", text: "i" },
            { type: "field_dropdown", name: "OP", options: [["<", "<"], ["<=", "<="], [">", ">"], [">=", ">="], ["!=", "!="]] },
            { type: "input_value", name: "FIM" },
            { type: "field_label", name: "VAR_PASSO", text: "i" },
            { type: "field_dropdown", name: "PASSO", options: [["++", "++"], ["--", "--"]] },
        ],
        message1: "%1",
        args1: [{ type: "input_statement", name: "FAZER" }],
        message2: "}",
        inputsInline: true,
        previousStatement: null,
        nextStatement: null,
        style: "estilo_repeticao",
        extensions: ["logicup_espelhar_variavel"],
        tooltip: "for: repete um número conhecido de vezes. Declara o contador, repete ENQUANTO a condição "
            + "for true e, no fim de cada volta, aplica o ++ (ou --).",
    },
    {
        type: "java_while",
        message0: "while ( %1 ) {",
        args0: [{ type: "input_value", name: "COND", check: "Boolean" }],
        message1: "%1",
        args1: [{ type: "input_statement", name: "FAZER" }],
        message2: "}",
        previousStatement: null,
        nextStatement: null,
        style: "estilo_repeticao",
        tooltip: "while (ENQUANTO): testa a condição ANTES de cada volta. Se ela nunca virar false, o laço não termina!",
    },
    {
        type: "java_do_while",
        message0: "do {",
        message1: "%1",
        args1: [{ type: "input_statement", name: "FAZER" }],
        message2: "} while ( %1 ) ;",
        args2: [{ type: "input_value", name: "COND", check: "Boolean" }],
        previousStatement: null,
        nextStatement: null,
        style: "estilo_repeticao",
        tooltip: "do / while: executa a primeira volta SEMPRE e só depois testa a condição.",
    },

    // ------------------------------------------------------------------ só nas QUESTÕES (03/11)
    /*
     * Fase 1 (F1.Q4): algoritmo em português, antes de o aluno ver Java. O "algoritmo" faz o papel do main
     * e cada "passo" vira um System.out.println("passo") no Java gerado. Assim a correção continua sendo
     * EXECUTAR o programa e comparar a saída (a ordem dos passos aparece na ordem das linhas exibidas),
     * sem nenhuma regra nova no backend. Decisão 2-B: o aluno pode abrir "Ver o Java" como curiosidade.
     */
    {
        type: "java_algoritmo",
        message0: "Algoritmo: %1",
        args0: [{ type: "field_label_serializable", name: "TITULO", text: "" }],
        message1: "%1",
        args1: [{ type: "input_statement", name: "CORPO" }],
        message2: "fim",
        style: "estilo_programa",
        tooltip: "O algoritmo é executado de cima para baixo: encaixe os passos aqui dentro, na ordem certa.",
    },
    {
        type: "java_passo",
        message0: "%1",
        args0: [{ type: "field_label_serializable", name: "TEXTO", text: "" }],
        previousStatement: null,
        nextStatement: null,
        style: "estilo_passo",
        tooltip: "Um passo do algoritmo.",
    },
    /*
     * Decisão 1-A (03/11): "blocos de LINHA" para as questões de montar código que vão além dos blocos da
     * etapa 1 (classe, construtor, método, ArrayList...). É a ideia dos "Parsons problems": o código já vem
     * escrito em pedaços e o aluno monta a ordem e o ANINHAMENTO. O texto é fixo (rótulo), então não há o
     * que digitar; o que conta é onde cada bloco foi encaixado.
     *   java_linha -> uma linha simples (perigosos++;  return 5;  @Override)
     *   java_bloco -> uma linha que ABRE chave (for, if, construtor, método): tem um encaixe por dentro e
     *                 a "}" aparece sozinha embaixo, então o aluno nunca erra a contagem de chaves
     *   java_raiz  -> o bloco de fora (a classe ou o método da questão). Não encaixa em nada: é o "main"
     *                 dessas questões, e o que está fora dele fica apagado e não vai para o Java.
     */
    {
        type: "java_linha",
        message0: "%1",
        args0: [{ type: "field_label_serializable", name: "TEXTO", text: "" }],
        previousStatement: null,
        nextStatement: null,
        style: "estilo_linha",
        tooltip: "Uma linha de código pronta: encaixe no lugar e no nível certos.",
    },
    {
        type: "java_bloco",
        message0: "%1",
        args0: [{ type: "field_label_serializable", name: "TEXTO", text: "" }],
        message1: "%1",
        args1: [{ type: "input_statement", name: "CORPO" }],
        message2: "}",
        previousStatement: null,
        nextStatement: null,
        style: "estilo_bloco",
        tooltip: "Um bloco com chaves { }: o que estiver encaixado aqui dentro fica DENTRO dele no código.",
    },
    {
        type: "java_raiz",
        message0: "%1",
        args0: [{ type: "field_label_serializable", name: "TEXTO", text: "" }],
        message1: "%1",
        args1: [{ type: "input_statement", name: "CORPO" }],
        message2: "}",
        style: "estilo_programa",
        tooltip: "O bloco principal da questão: monte o código aqui dentro.",
    },
];

/*
 * O "for" mostra o nome do contador três vezes (int i = ...; i < ...; i++), mas no Blockly um campo só
 * aparece uma vez. Solução: os outros dois são rótulos que COPIAM o nome escolhido no primeiro.
 * Uma "extensão" é uma função que o Blockly roda em cada bloco desse tipo assim que ele é criado.
 */
function espelharVariavel() {
    const atualizar = () => {
        const nome = this.getField("VAR")?.getText() ?? "i";
        this.setFieldValue(nome, "VAR_COND");
        this.setFieldValue(nome, "VAR_PASSO");
    };
    this.setOnChange(atualizar);
    atualizar();
}

/*
 * O número é um campo de TEXTO com validação (e não o campo numérico do Blockly) para guardar exatamente o
 * que o aluno digitou: o campo numérico transformaria "2.0" em "2", e no Java isso muda o resultado da conta.
 * Aceita vírgula e troca por ponto (o Java só entende ponto). Devolver null = recusar o que foi digitado.
 */
function validarNumero() {
    this.getField("NUM").setValidator((texto) => {
        const limpo = String(texto).trim().replace(",", ".");
        return /^-?\d{1,9}(\.\d{1,9})?$/.test(limpo) ? limpo : null;
    });
}

/*
 * Quando o aluno troca o TIPO da declaração, o valor de exemplo encaixado (o bloco-sombra, clarinho) troca
 * junto: String -> "texto", boolean -> true, double -> 0.0, int -> 0. Sem isso, ao escolher String o aluno
 * ficava com um 0 que não aceita letras e não sabia que precisava trazer o bloco de texto de "Valores".
 * Só troca se o encaixe estiver vazio ou com a sombra: um bloco que o aluno colocou ali não é mexido.
 */
const VALOR_DE_EXEMPLO = {
    int: { type: "java_numero", fields: { NUM: "0" } },
    double: { type: "java_numero", fields: { NUM: "0.0" } },
    String: { type: "java_texto", fields: { TEXTO: "texto" } },
    boolean: { type: "java_booleano", fields: { VALOR: "true" } },
};

function valorDoTipo() {
    this.setOnChange((evento) => {
        if (evento.type !== Blockly.Events.BLOCK_CHANGE || evento.blockId !== this.id || evento.name !== "TIPO") return;
        const conexao = this.getInput("VALOR")?.connection;
        const encaixado = conexao?.targetBlock();
        const exemplo = VALOR_DE_EXEMPLO[evento.newValue];
        if (!conexao || !exemplo || (encaixado && !encaixado.isShadow())) return;
        // mesmo "grupo" de eventos da troca do tipo: um único Ctrl+Z desfaz as duas coisas
        const grupoAnterior = Blockly.Events.getGroup();
        Blockly.Events.setGroup(evento.group);
        conexao.setShadowState(exemplo);
        Blockly.Events.setGroup(grupoAnterior);
    });
}

let registrado = false;

/** Registra os blocos uma única vez (o Blockly guarda as definições num registro global). */
export function registrarBlocos() {
    if (registrado) return;
    // textos do próprio Blockly (menu do botão direito, "Renomear variável"...) em português
    Blockly.setLocale(PtBr);
    if (!Blockly.Extensions.isRegistered("logicup_espelhar_variavel")) {
        Blockly.Extensions.register("logicup_espelhar_variavel", espelharVariavel);
        Blockly.Extensions.register("logicup_validar_numero", validarNumero);
    }
    if (!Blockly.Extensions.isRegistered("logicup_valor_do_tipo")) {
        Blockly.Extensions.register("logicup_valor_do_tipo", valorDoTipo);
    }
    Blockly.common.defineBlocksWithJsonArray(DEFINICOES);
    registrado = true;
}

export const TIPOS_DE_BLOCO = DEFINICOES.map((definicao) => definicao.type);
