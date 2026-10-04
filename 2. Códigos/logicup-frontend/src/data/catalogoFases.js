/*
 * Catálogo VISUAL das 10 fases planejadas do LogicUp.
 * Serve só para desenhar o mapa completo: o que é real (status, rodadas, XP) vem da API.
 * Fases que ainda não existem no banco aparecem como "Em breve" usando estes títulos e tópicos.
 */
export const catalogoFases = [
    { ordem: 1, titulo: "Lógica de programação", topicos: ["Algoritmos e fluxogramas", "Sequência lógica", "Pensamento computacional"] },
    { ordem: 2, titulo: "Explorando o universo Java", topicos: ["Sintaxe básica", "Estrutura dos códigos", "Entrada e saída de dados"] },
    { ordem: 3, titulo: "Variáveis e tipos de dados", topicos: ["Tipos de variáveis", "Operadores matemáticos", "Operadores lógicos"] },
    { ordem: 4, titulo: "Estruturas condicionais", topicos: ["if/else", "switch case"] },
    { ordem: 5, titulo: "Estruturas de repetição", topicos: ["for", "while", "do while"] },
    { ordem: 6, titulo: "Vetores e matrizes", topicos: ["Vetores", "Matrizes", "Percorrer arrays"] },
    { ordem: 7, titulo: "Collections", topicos: ["ArrayList", "Vetor x ArrayList", "CRUD em ArrayList"] },
    { ordem: 8, titulo: "Métodos", topicos: ["Criação de métodos", "Parâmetros", "Retorno"] },
    { ordem: 9, titulo: "Programação Orientada a Objetos", topicos: ["Classes", "Objetos", "Atributos", "Métodos"] },
    { ordem: 10, titulo: "Desafio final & Boss Battle", topicos: ["Missões finais", "Desafios cronometrados", "Batalha contra o Chefão"], boss: true },
];
