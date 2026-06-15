export const phaseOneRounds = [
    {
        id: 1,
        title: "Algoritmos e fluxogramas",
        theory: {
            concept:
                "Um algoritmo é uma sequência de instruções usada para resolver um problema ou realizar uma tarefa. Já o fluxograma é uma representação visual desse algoritmo, usando símbolos para mostrar o caminho da solução.",
            exampleTitle: "Exemplo: fazer café",
            exampleText:
                "Para fazer café, seguimos uma ordem: ferver a água, adicionar o pó, coar e servir. Essa sequência organizada é um algoritmo.",
            code: `Início
  Ferver a água
  Adicionar o pó de café
  Coar o café
  Servir
Fim`,
        },
        exercises: [
            {
                type: "V ou F",
                question:
                    "Um fluxograma é uma forma visual de representar um algoritmo.",
                options: ["Verdadeiro", "Falso"],
                answer: "Verdadeiro",
                correction:
                    "Verdadeiro. O fluxograma representa visualmente os passos de um algoritmo.",
            },
            {
                type: "Quiz",
                question: "Qual das opções representa um algoritmo?",
                options: [
                    "Uma imagem qualquer",
                    "Uma sequência de passos para resolver um problema",
                    "Um computador",
                    "Uma linguagem de programação",
                ],
                answer: "Uma sequência de passos para resolver um problema",
                correction:
                    "Um algoritmo é uma sequência de passos para resolver um problema.",
            },
            {
                type: "Quiz",
                question: "Qual símbolo costuma representar uma decisão em um fluxograma?",
                options: ["Retângulo", "Losango", "Círculo", "Seta"],
                answer: "Losango",
                correction:
                    "O losango representa decisões, como perguntas do tipo sim/não.",
            },
        ],
    },
    {
        id: 2,
        title: "Sequência lógica",
        theory: {
            concept:
                "Sequência lógica é a ordem correta em que as ações devem acontecer. Quando a ordem está errada, o resultado pode não fazer sentido.",
            exampleTitle: "Exemplo: atravessar a rua",
            exampleText:
                "Antes de atravessar, é necessário parar, olhar para os dois lados, verificar se há veículos e só depois atravessar com segurança.",
            code: `1. Chegar à faixa de pedestres
2. Olhar para os dois lados
3. Verificar se há veículos
4. Aguardar, se necessário
5. Atravessar com segurança`,
        },
        exercises: [
            {
                type: "Ordem correta",
                question: "Qual sequência faz mais sentido para fazer café?",
                options: [
                    "Servir → Coar → Ferver água → Adicionar pó",
                    "Ferver água → Adicionar pó → Coar → Servir",
                    "Coar → Servir → Ferver água → Adicionar pó",
                ],
                answer: "Ferver água → Adicionar pó → Coar → Servir",
                correction:
                    "Primeiro é necessário ferver a água, depois adicionar o pó, coar e servir.",
            },
            {
                type: "Ache o erro",
                question: "Observe: Escovar os dentes → Acordar → Tomar café. Qual é o erro?",
                options: [
                    "Acordar deveria ser o primeiro passo",
                    "Tomar café deveria ser removido",
                    "Escovar os dentes nunca faz parte de uma sequência",
                ],
                answer: "Acordar deveria ser o primeiro passo",
                correction:
                    "A ação de acordar precisa acontecer antes das outras atividades.",
            },
            {
                type: "Puzzle lógico",
                question: "Maria precisa tomar café, escovar os dentes e vestir o uniforme. Qual sequência faz mais sentido?",
                options: [
                    "Vestir uniforme → Tomar café → Escovar os dentes",
                    "Tomar café → Escovar os dentes → Vestir uniforme",
                    "Escovar os dentes → Tomar café → Vestir uniforme",
                ],
                answer: "Tomar café → Escovar os dentes → Vestir uniforme",
                correction:
                    "Faz mais sentido tomar café antes de escovar os dentes e depois vestir o uniforme.",
            },
        ],
    },
    {
        id: 3,
        title: "Pensamento computacional",
        theory: {
            concept:
                "Pensamento computacional é uma forma de resolver problemas usando estratégias como decomposição, reconhecimento de padrões, abstração e criação de algoritmos.",
            exampleTitle: "Exemplo: organizar uma festa",
            exampleText:
                "Organizar uma festa pode parecer complexo, mas podemos dividir em partes menores: escolher local, convidar pessoas, comprar comida e preparar a decoração.",
            code: `Problema: organizar uma festa

Decomposição:
  - Escolher local
  - Convidar pessoas
  - Comprar comida
  - Preparar decoração`,
        },
        exercises: [
            {
                type: "Quiz",
                question: "O que é decomposição?",
                options: [
                    "Dividir um problema grande em partes menores",
                    "Ignorar informações importantes",
                    "Desenhar qualquer símbolo",
                ],
                answer: "Dividir um problema grande em partes menores",
                correction:
                    "Decomposição é dividir um problema maior em partes menores e mais fáceis de resolver.",
            },
            {
                type: "Associação",
                question: "Qual associação está correta?",
                options: [
                    "Algoritmo = representação gráfica",
                    "Fluxograma = sequência de instruções",
                    "Pensamento computacional = resolução estruturada de problemas",
                ],
                answer: "Pensamento computacional = resolução estruturada de problemas",
                correction:
                    "Pensamento computacional ajuda a resolver problemas de forma estruturada.",
            },
            {
                type: "Desafio relâmpago",
                question: "Você tem uma lâmpada apagada. Qual é o primeiro passo lógico?",
                options: [
                    "Apagar a luz",
                    "Verificar o interruptor",
                    "Comprar outra lâmpada",
                    "Trocar a tomada",
                ],
                answer: "Verificar o interruptor",
                correction:
                    "Antes de trocar lâmpada ou tomada, o primeiro passo lógico é verificar o interruptor.",
            },
        ],
    },
    {
        id: 4,
        title: "Mini Boss: atravessando a rua",
        theory: {
            concept:
                "Agora é hora de aplicar tudo: algoritmo, sequência lógica e pensamento computacional em um desafio final.",
            exampleTitle: "Desafio",
            exampleText:
                "Crie um algoritmo simples para atravessar uma rua com segurança.",
            code: `Dica:
Pense na ordem correta das ações.
Use passos claros e objetivos.`,
        },
        exercises: [
            {
                type: "Boss Challenge",
                question:
                    "Escreva um algoritmo para atravessar uma rua com segurança.",
                textAnswer: true,
                keywords: ["faixa", "olhar", "veículos", "aguardar", "atravessar"],
                correction:
                    "Uma boa resposta seria: chegar à faixa de pedestres, olhar para os dois lados, verificar se há veículos, aguardar se necessário e atravessar com segurança.",
            },
        ],
    },
];