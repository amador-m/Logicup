import { VERSAO_PRIVACIDADE, ATUALIZADO_EM } from "./versoes";

/*
 * Sprint 8: Política de Privacidade, versão preliminar 0.1.
 * Base: minuta da Luciana (21/10/2026). Os dados listados aqui são os que o sistema REALMENTE guarda hoje
 * (tabelas usuario, aluno, progresso_exercicio e aceite_documento; cookies em TokenService/SecurityConfig).
 * Se um campo novo for criado no banco, esta página precisa ser atualizada junto.
 * v0.2 (Sprint 9): turmas — matrícula, tarefas e o que o professor vê dos alunos da turma dele.
 * v0.3 (Sprint 9): conquistas, gemas e avatares (tabelas aluno_conquista e aluno_avatar; usuario.avatar; aluno.gemas).
 */
export const privacidade = {
    slug: "privacidade",
    titulo: "Política de Privacidade",
    versao: VERSAO_PRIVACIDADE,
    atualizadoEm: ATUALIZADO_EM,
    resumo:
        "Quais dados pessoais o LogiCup guarda, para que servem, com quem são compartilhados " +
        "e como você pode exercer os seus direitos.",
    secoes: [
        {
            titulo: "Apresentação",
            blocos: [
                "O LogiCup é uma plataforma web educacional gamificada voltada ao aprendizado de lógica de " +
                "programação e Java. Esta Política explica como tratamos dados pessoais no cadastro, na entrada " +
                "na conta e no uso dos recursos educacionais.",
                "Esta é uma versão preliminar: ela descreve o funcionamento atual da plataforma e será revisada " +
                "antes da publicação definitiva.",
            ],
        },
        {
            titulo: "Quem é responsável pelo tratamento",
            blocos: [
                "O LogiCup é desenvolvido por uma equipe acadêmica, como Trabalho de Conclusão de Curso.",
                {
                    lista: [
                        "Responsável: equipe do projeto LogiCup. A identificação formal do controlador dos dados será feita na versão definitiva.",
                        "Canal de contato para assuntos de privacidade: será divulgado pela equipe na versão definitiva.",
                        "Encarregado pelo tratamento de dados: não designado nesta versão preliminar.",
                    ],
                },
            ],
        },
        {
            titulo: "Quais dados tratamos",
            blocos: [
                "Dados que você informa no cadastro:",
                {
                    lista: [
                        "nome;",
                        "nome de usuário;",
                        "e-mail;",
                        "senha: não é guardada como você a digitou. Guardamos apenas um resumo criptográfico (hash), que não permite recuperar a senha original;",
                        "tipo de perfil (aluno ou professor).",
                    ],
                },
                "Dados gerados pelo uso da plataforma:",
                {
                    lista: [
                        "data de cadastro;",
                        "o avatar escolhido: uma ilustração do catálogo da plataforma (não é uma foto sua);",
                        "para alunos: pontos de experiência (XP), nível, sequência de dias de estudo e data da última atividade;",
                        "para cada exercício respondido: situação (em andamento ou concluído), número de tentativas, pontos ganhos, a última resposta enviada e a data da resposta;",
                        "para alunos: as conquistas desbloqueadas (qual conquista, o nível, a data e se o aviso de conquista nova já foi exibido), o saldo de gemas e os avatares comprados com gemas (qual avatar, quantas gemas custou e a data);",
                        "registro de aceite: qual documento (Termos ou Política), qual versão e a data e a hora do aceite;",
                        "se você entrar em uma turma: a turma, a situação da matrícula (ativa, saiu ou removida pelo professor), quando entrou e quando a situação mudou;",
                        "para professores: as turmas criadas (nome, código de convite e data de criação) e as tarefas atribuídas (rodada e prazo).",
                    ],
                },
                "Dados técnicos:",
                {
                    lista: [
                        "cookies necessários para manter você conectado e proteger os formulários (detalhes na Política de Cookies);",
                        "registros técnicos de funcionamento do servidor (por exemplo, erros e pedidos bloqueados pela proteção de segurança), que podem conter o endereço da página pedida, informações técnicas do pedido e, em algumas situações de erro, parte dos dados enviados (por exemplo, um e-mail ou nome de usuário que já estava cadastrado). A aplicação não registra o seu endereço IP nesses registros.",
                    ],
                },
                "Não pedimos data de nascimento, documentos, telefone, endereço ou foto, e não usamos ferramentas de " +
                "análise de audiência, publicidade ou rastreamento.",
            ],
        },
        {
            titulo: "Para que usamos os dados",
            blocos: [
                {
                    lista: [
                        "criar e administrar a sua conta;",
                        "confirmar a sua identidade ao entrar e manter a sessão aberta;",
                        "mostrar exercícios, corrigir respostas e dar feedback;",
                        "registrar XP, nível, sequência de estudo e progresso na trilha;",
                        "montar o ranking geral e o ranking da turma;",
                        "registrar conquistas e gemas e permitir a troca de gemas por avatares;",
                        "permitir que o professor acompanhe o progresso dos alunos da turma dele e atribua tarefas;",
                        "comprovar qual versão dos Termos e desta Política foi aceita;",
                        "diagnosticar erros e proteger a plataforma contra acessos não autorizados e abusos.",
                    ],
                },
                "Os dados não são usados para finalidades diferentes dessas. Uma finalidade nova será avaliada antes " +
                "de ser implementada e comunicada quando necessário.",
            ],
        },
        {
            titulo: "Quem pode ver os seus dados",
            blocos: [
                "Outras pessoas com conta na plataforma podem ver, no ranking, o seu nome de usuário, o seu avatar, o seu " +
                "XP, o seu nível e a sua posição, quando você estiver entre as pessoas que mais pontuaram. Seu nome e seu " +
                "e-mail não aparecem no ranking.",
                "Suas conquistas e o seu saldo de gemas aparecem só para você. Nem os colegas nem o professor da turma veem " +
                "esses dados.",
                "Se você entrar em uma turma (sempre por um código que o professor compartilha com você), o professor " +
                "dessa turma vê o seu nome, nome de usuário, avatar, e-mail, XP, nível, quantas rodadas você aprovou, a data da " +
                "sua última atividade. Nas tarefas, ele vê quantos alunos da turma já concluíram cada uma. Ele não vê a sua " +
                "senha nem as respostas que você enviou.",
                "Os colegas da mesma turma veem, no ranking da turma, o seu nome de usuário, avatar, XP e nível. Você vê só o " +
                "nome do professor.",
                "Quando você sai da turma, ou o professor remove você, ou a turma é excluída, o professor deixa de ver os " +
                "seus dados. A ementa da trilha, também disponível aos professores, não contém dados de alunos.",
                "O acesso ao banco de dados é restrito à equipe responsável pela manutenção da plataforma.",
            ],
        },
        {
            titulo: "Bases legais",
            blocos: [
                "A Lei Geral de Proteção de Dados Pessoais prevê diferentes hipóteses legais para o tratamento de " +
                "dados. A hipótese adequada para cada finalidade está sendo avaliada pela equipe e será indicada na " +
                "versão definitiva desta Política.",
                "O consentimento não é usado como base para todas as operações. Se alguma finalidade depender de " +
                "consentimento, ele será pedido de forma livre, informada e específica, e você poderá retirá-lo.",
            ],
        },
        {
            titulo: "Resumo do tratamento",
            blocos: [
                {
                    tabela: {
                        colunas: ["Dados", "Para quê", "Por quanto tempo"],
                        linhas: [
                            ["Cadastro (nome, nome de usuário, e-mail, hash da senha, perfil)", "Criar e administrar a conta", "Enquanto a conta existir"],
                            ["Sessão (cookie de login)", "Manter você conectado", "Até 8 horas ou até você sair"],
                            ["Progresso (XP, nível, respostas, tentativas)", "Oferecer os exercícios e acompanhar a aprendizagem", "Enquanto a conta existir"],
                            ["Conquistas, gemas e avatares", "Gamificação: medalhas, saldo de gemas e loja de avatares", "Enquanto a conta existir"],
                            ["Turma (matrícula e tarefas)", "Acompanhamento pelo professor e ranking da turma", "Enquanto a turma existir; excluir a turma apaga as matrículas e as tarefas, mas não o seu progresso"],
                            ["Registros de aceite", "Comprovar a versão aceita e a data", "Enquanto a conta existir; a guarda após a exclusão será definida na versão definitiva"],
                            ["Registros técnicos do servidor", "Diagnosticar falhas e proteger a aplicação", "Prazo a ser definido na versão definitiva"],
                        ],
                    },
                },
                "Prazos de guarda depois da exclusão de uma conta ainda não foram definidos.",
            ],
        },
        {
            titulo: "Compartilhamento com terceiros",
            blocos: [
                "O LogiCup não vende dados pessoais e não os compartilha com terceiros para publicidade ou qualquer " +
                "finalidade diferente das descritas aqui.",
                "A fonte de letras usada no site é servida pelo próprio LogiCup, sem requisições a serviços externos.",
                "Quando a plataforma for publicada em um serviço de hospedagem, esse fornecedor terá acesso técnico " +
                "aos dados para manter o sistema funcionando. Os fornecedores efetivamente usados, o que cada um acessa " +
                "e se há armazenamento fora do Brasil serão informados na versão definitiva desta Política.",
            ],
        },
        {
            titulo: "Segurança",
            blocos: [
                "Adotamos medidas técnicas para proteger os dados, entre elas:",
                {
                    lista: [
                        "senhas guardadas apenas como hash;",
                        "cookie de login inacessível a scripts da página e enviado apenas ao próprio site;",
                        "proteção contra pedidos forjados por outros sites (CSRF e verificação de origem);",
                        "acesso a cada recurso conforme o tipo de perfil.",
                    ],
                },
                "Nenhum sistema é totalmente imune a falhas. Se houver um incidente de segurança envolvendo dados " +
                "pessoais, a equipe avaliará as medidas de contenção, os riscos e as comunicações exigidas pela legislação.",
            ],
        },
        {
            titulo: "Retenção e exclusão",
            blocos: [
                "Os dados são mantidos enquanto forem necessários para as finalidades descritas.",
                "Nesta versão, ainda não é possível excluir a conta pela própria plataforma. Pedidos de exclusão " +
                "serão atendidos pelo canal de contato, quando ele for divulgado.",
                "Excluir a conta não significa, necessariamente, apagar no mesmo instante todos os registros: alguns " +
                "podem precisar ser guardados quando houver fundamento legal. Procedimentos e prazos serão definidos " +
                "na versão definitiva.",
            ],
        },
        {
            titulo: "Seus direitos",
            blocos: [
                "Nos termos da LGPD, você pode solicitar, conforme o seu caso:",
                {
                    lista: [
                        "confirmação de que tratamos seus dados;",
                        "acesso aos seus dados;",
                        "correção de dados incompletos, inexatos ou desatualizados (nome, nome de usuário e senha você mesmo pode alterar no perfil);",
                        "anonimização, bloqueio ou eliminação de dados desnecessários, excessivos ou tratados em desconformidade com a lei;",
                        "portabilidade, conforme a regulamentação;",
                        "informação sobre com quem seus dados foram compartilhados;",
                        "informação sobre a possibilidade de não consentir e suas consequências, e revogação do consentimento, quando o tratamento se basear nele;",
                        "oposição a tratamento feito em desconformidade com a lei;",
                        "petição à Autoridade Nacional de Proteção de Dados (ANPD).",
                    ],
                },
                "As solicitações serão recebidas pelo canal de contato que a equipe divulgará na versão definitiva.",
            ],
        },
        {
            titulo: "Crianças e adolescentes",
            blocos: [
                "O LogiCup não pede data de nascimento e não verifica a idade de quem se cadastra. A faixa etária " +
                "pretendida e eventuais medidas adicionais para menores de 18 anos serão definidas pela equipe antes " +
                "da publicação definitiva.",
            ],
        },
        {
            titulo: "Alterações desta Política",
            blocos: [
                "Esta Política pode ser atualizada quando mudarem as funcionalidades, os dados tratados, os " +
                "fornecedores ou a legislação. A versão vigente fica sempre disponível nesta página, com número de " +
                "versão e data. Em alterações relevantes, pediremos que você confirme a leitura no próximo acesso.",
            ],
        },
    ],
};
