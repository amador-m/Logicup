import Gattuccino from "../../../assets/images/MascoteRecortado.png";
import GattuccinoAcerto from "../../../assets/images/onboarding/gattuccino-acerto.png";
import GattuccinoErro from "../../../assets/images/onboarding/gattuccino-erro.png";
import IconeTrilha from "../../../assets/images/onboarding/area-trilha.png";
import IconeRanking from "../../../assets/images/onboarding/area-ranking.png";
import IconeTurma from "../../../assets/images/onboarding/area-turma.png";
import IconePerfil from "../../../assets/images/onboarding/area-perfil.png";

/*
 * Sprint 9: o conteúdo do onboarding (decisão O3: cada perfil vê as SUAS funcionalidades).
 * Separado do componente para a equipe revisar os textos sem mexer no código da janela.
 * Os ícones das áreas são os da Manuela/Davi ("Ícones LogiCup.png"): mapa = Trilha, xícara com "I" = Ranking,
 * xícara com gatos = Turma, gato = Perfil. As regras citadas aqui são as REAIS do backend
 * (aprovação com 80% + Boss, XP por questão, ranking só com o nome de usuário).
 */
export const PASSOS_ALUNO = [
    {
        imagens: [Gattuccino],
        titulo: "Oi! Eu sou o Gattuccino",
        texto: "Vou te mostrar como o LogiCup funciona. É rapidinho: são só cinco passos.",
    },
    {
        imagens: [IconeTrilha],
        titulo: "A trilha",
        texto: "Você aprende Java avançando por fases. Cada fase tem rodadas, e cada rodada libera a seguinte quando " +
            "você é aprovado: acertar pelo menos 80% das questões, incluindo o desafio do Boss.",
    },
    {
        imagens: [GattuccinoAcerto, GattuccinoErro],
        titulo: "Uma questão por vez",
        texto: "Cada rodada começa com a teoria e exemplos. Depois vem uma questão de cada vez, com resposta na hora. " +
            "Errou? Eu explico o porquê, e você pode refazer a rodada.",
    },
    {
        imagens: [IconeRanking],
        titulo: "XP, ofensiva e ranking",
        texto: "Cada questão certa rende XP. Estudar em dias seguidos aumenta a sua ofensiva. Quem tem mais XP sobe " +
            "no ranking, que mostra só o seu nome de usuário.",
    },
    {
        imagens: [IconeTurma, IconePerfil],
        titulo: "Sua turma e seu perfil",
        texto: "Se o seu professor passar um código, entre em \"Minha turma\": lá ficam as tarefas e o ranking da turma. " +
            "No seu perfil você acompanha o progresso e pode rever este tutorial.",
    },
];

export const PASSOS_PROFESSOR = [
    {
        imagens: [Gattuccino],
        titulo: "Oi, professor(a)! Eu sou o Gattuccino",
        texto: "Vou te mostrar as ferramentas para acompanhar seus alunos no LogiCup. São só quatro passos.",
    },
    {
        imagens: [IconeTurma],
        titulo: "Turmas e código de convite",
        texto: "Em \"Turmas\", crie uma turma e compartilhe o código de 8 caracteres. Os alunos entram com ele em " +
            "\"Minha turma\". Se o código vazar, você gera outro.",
    },
    {
        imagens: [IconeRanking],
        titulo: "Acompanhe e passe tarefas",
        texto: "Na turma você vê o XP, a fase e as rodadas aprovadas de cada aluno, atribui rodadas da trilha como " +
            "tarefas, com prazo opcional, e acompanha o ranking da turma.",
    },
    {
        imagens: [IconeTrilha],
        titulo: "Ementa e trilha",
        texto: "Em \"Ementa\" você vê o objetivo de cada fase e a habilidade que cada questão avalia. A trilha fica " +
            "toda aberta para você explorar o conteúdo que os alunos vão estudar.",
    },
];

/** Para onde o último botão leva, por perfil. */
export const FINAL = {
    ALUNO: { texto: "Começar a trilha", destino: "/trilha" },
    PROFESSOR: { texto: "Criar minha primeira turma", destino: "/professor/turmas" },
};
