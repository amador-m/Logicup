/*
 * Catálogo VISUAL das 10 fases planejadas do LogicUp.
 * Serve só para desenhar o mapa completo: o que é real (status, rodadas, XP) vem da API.
 * Fases que ainda não existem no banco aparecem como "Em breve" usando estes títulos e tópicos.
 *
 * Sprint 7:
 *  - ordem corrigida conforme o Banco de Questões: 7 = Métodos, 8 = Collections;
 *  - "icone": qual dos ícones do Davi (assets/fases) representa a fase no mapa.
 *    Para trocar o ícone de uma fase, basta mudar este nome.
 */
export const catalogoFases = [
    { ordem: 1, icone: "puzzle", titulo: "Lógica de programação", topicos: ["Algoritmos e fluxogramas", "Sequência lógica", "Pensamento computacional"] },
    { ordem: 2, icone: "code", titulo: "Explorando o universo Java", topicos: ["Sintaxe básica", "Estrutura dos códigos", "Entrada e saída de dados"] },
    { ordem: 3, icone: "complete", titulo: "Variáveis e tipos de dados", topicos: ["Tipos de variáveis", "Operadores matemáticos", "Operadores lógicos"] },
    { ordem: 4, icone: "truefalse", titulo: "Estruturas condicionais", topicos: ["if/else", "switch case"] },
    { ordem: 5, icone: "order", titulo: "Estruturas de repetição", topicos: ["for", "while", "do while"] },
    { ordem: 6, icone: "multichoice", titulo: "Vetores e matrizes", topicos: ["Vetores", "Matrizes", "Percorrer arrays"] },
    { ordem: 7, icone: "predict", titulo: "Métodos", topicos: ["Criação de métodos", "Parâmetros", "Retorno"] },
    { ordem: 8, icone: "associate", titulo: "Collections", topicos: ["ArrayList", "Vetor x ArrayList", "CRUD em ArrayList"] },
    { ordem: 9, icone: "blocks", titulo: "Programação Orientada a Objetos", topicos: ["Classes", "Objetos", "Atributos", "Métodos"] },
    { ordem: 10, icone: "boss", titulo: "Desafio final & Boss Battle", topicos: ["Missões finais", "Desafios cronometrados", "Batalha contra o Chefão"], boss: true },
];
