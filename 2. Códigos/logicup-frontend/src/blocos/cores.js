/*
 * Sprint 9 (ADR-001): as cores do Blockly com a identidade do LogiCup (mesmas variáveis do Global.js).
 * Uma cor por categoria, igual na caixa de ferramentas e nos blocos: o aluno associa "laranja = variável".
 * Os tons foram escurecidos onde o texto branco do bloco ficaria difícil de ler (contraste).
 */
export const CORES = {
    programa: "#040423",    // --dark
    variaveis: "#e35c33",   // --orange
    saida: "#288078",       // --green
    valores: "#a4745c",     // --brown-light
    operadores: "#5b6abf",
    condicionais: "#b07b00", // --yellow escurecido
    repeticao: "#764935",   // --brown-dark
    // só nas questões (03/11): passos da Fase 1 e os "blocos de linha"
    passo: "#c4572c",       // --orange escurecido
    linha: "#2c6577",
    bloco: "#5b6abf",
};

/*
 * Arquivo separado do tema de propósito: o CSS da caixa de ferramentas (AreaBlocos/styles.js) usa estas
 * cores, e importar o temaLogicup.js ali puxaria o Blockly inteiro para o pacote principal do site.
 */
