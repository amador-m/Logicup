import { VERSAO_PRIVACIDADE, ATUALIZADO_EM } from "./versoes";

/*
 * Sprint 8: Política de Privacidade, versão preliminar 0.1.
 * Base: minuta da Luciana (21/10/2026). Os dados listados aqui são os que o sistema REALMENTE guarda hoje
 * (tabelas usuario, aluno, progresso_exercicio e aceite_documento; cookies em TokenService/SecurityConfig).
 * Se um campo novo for criado no banco, esta página precisa ser atualizada junto.
 * v0.2 (Sprint 9): turmas — matrícula, tarefas e o que o professor vê dos alunos da turma dele.
 * v0.3 (Sprint 9): conquistas, gemas e avatares (tabelas aluno_conquista e aluno_avatar; usuario.avatar; aluno.gemas).
 * v0.4 (Sprint 10): confirmação de e-mail (usuario.email_confirmado_em), códigos dos links (token_usuario, só o hash),
 *   fornecedores efetivamente usados (Google Cloud, Brevo, ImprovMX) e canal de contato.
 * v0.5 (Sprint 10): entrar com o Google (usuario.google_id), exclusão da conta pelo Perfil (o banco apaga tudo
 *   em cascata), prazos das cópias de segurança (backup.sh: 14 cópias diárias; snapshot do disco: 7 dias),
 *   rotação dos registros técnicos e o código enviado para execução (não é guardado).
 * v0.6 (Sprint 11): resultado do nivelamento (tabela nivelamento), bloqueio da conta (usuario.bloqueado_em),
 *   o que os administradores veem na área de administração e "fase atual" no lugar de "nível".
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
                        "Canal de contato para assuntos de privacidade: contato@logicup.dev.br.",
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
                "Se você escolher entrar com o Google:",
                {
                    lista: [
                        "o Google nos envia o seu nome, o seu e-mail, a confirmação de que esse e-mail é seu e um identificador da sua conta Google (um código que não muda). O Google também pode enviar o endereço da sua foto de perfil, que não usamos nem guardamos;",
                        "guardamos o e-mail e o identificador, para reconhecer você nas próximas entradas. O nome serve só para preencher o formulário de cadastro, e você pode alterá-lo;",
                        "não recebemos a sua senha do Google nem os seus contatos, arquivos, agenda ou qualquer outro dado da sua conta Google;",
                        "quem entra só com o Google não tem senha no LogiCup. Se quiser criar uma, use \"Esqueci minha senha\" com o seu e-mail.",
                    ],
                },
                "Dados gerados pelo uso da plataforma:",
                {
                    lista: [
                        "data de cadastro;",
                        "a data em que você confirmou o seu e-mail;",
                        "códigos dos links enviados por e-mail (confirmação da conta, criação de senha nova e exclusão da conta): guardamos só um resumo criptográfico (hash) de cada código, a finalidade, quando foi criado, quando vence e quando foi usado. O código em si não fica guardado, e os registros são apagados 30 dias depois de vencerem;",
                        "o avatar escolhido: uma ilustração do catálogo da plataforma (não é uma foto sua);",
                        "para alunos: pontos de experiência (XP), fase atual, sequência de dias de estudo e data da última atividade;",
                        "para alunos que fizerem o teste de nivelamento: quantas questões acertou, quantas fases foram liberadas e a data. As respostas de cada questão não são guardadas. Para quem preferir começar do zero, fica registrada só essa escolha e a data;",
                        "para cada exercício respondido: situação (em andamento ou concluído), número de tentativas, pontos ganhos, a última resposta enviada e a data da resposta;",
                        "o código Java que você executa no editor ou no Laboratório é enviado ao servidor de execução do próprio LogiCup, roda isolado e não é guardado. Nos exercícios de código, só a última resposta enviada fica no progresso, como nos outros exercícios. O programa do Laboratório fica salvo apenas no seu navegador;",
                        "para alunos: as conquistas desbloqueadas (qual conquista, o nível, a data e se o aviso de conquista nova já foi exibido), o saldo de gemas e os avatares comprados com gemas (qual avatar, quantas gemas custou e a data);",
                        "se a conta for bloqueada pela equipe: a data do bloqueio;",
                        "registro de aceite: qual documento (Termos ou Política), qual versão e a data e a hora do aceite;",
                        "se você entrar em uma turma: a turma, a situação da matrícula (ativa, saiu ou removida pelo professor), quando entrou e quando a situação mudou;",
                        "para professores: as turmas criadas (nome, código de convite e data de criação) e as tarefas atribuídas (rodada e prazo).",
                    ],
                },
                "Dados técnicos:",
                {
                    lista: [
                        "cookies necessários para manter você conectado e proteger os formulários (detalhes na Política de Cookies);",
                        "registros técnicos de funcionamento do servidor (por exemplo, erros e pedidos bloqueados pela proteção de segurança), que podem conter o endereço da página pedida, informações técnicas do pedido e, em algumas situações de erro, parte dos dados enviados (por exemplo, um e-mail ou nome de usuário que já estava cadastrado). Quando aparece um e-mail no registro de envio de e-mails, ele fica parcialmente escondido. A aplicação não registra o seu endereço IP nesses registros.",
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
                        "confirmar que o e-mail informado no cadastro pertence a você;",
                        "enviar e-mails da própria conta: confirmação do cadastro, boas-vindas, o link de \"esqueci minha senha\", o link e o aviso de exclusão da conta. Não enviamos e-mails de propaganda;",
                        "confirmar a sua identidade ao entrar, inclusive pelo Google, se você escolher essa opção, e manter a sessão aberta;",
                        "excluir a sua conta quando você pedir;",
                        "mostrar exercícios, corrigir respostas e dar feedback;",
                        "registrar XP, fase atual, sequência de estudo e progresso na trilha;",
                        "liberar as primeiras fases para quem mostrar, no teste de nivelamento, que já domina o conteúdo delas;",
                        "montar o ranking geral e o ranking da turma;",
                        "registrar conquistas e gemas e permitir a troca de gemas por avatares;",
                        "permitir que o professor acompanhe o progresso dos alunos da turma dele e atribua tarefas;",
                        "comprovar qual versão dos Termos e desta Política foi aceita;",
                        "permitir que a equipe administre as contas (por exemplo, bloquear uma conta em caso de abuso) e os conteúdos da plataforma;",
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
                "XP, a sua fase atual e a sua posição, quando você estiver entre as pessoas que mais pontuaram. Seu nome e seu " +
                "e-mail não aparecem no ranking.",
                "Suas conquistas e o seu saldo de gemas aparecem só para você. Nem os colegas nem o professor da turma veem " +
                "esses dados.",
                "Se você entrar em uma turma (sempre por um código que o professor compartilha com você), o professor " +
                "dessa turma vê o seu nome, nome de usuário, avatar, e-mail, XP, fase atual, quantas rodadas você aprovou, a data da " +
                "sua última atividade. Nas tarefas, ele vê quantos alunos da turma já concluíram cada uma. Ele não vê a sua " +
                "senha nem as respostas que você enviou.",
                "Os colegas da mesma turma veem, no ranking da turma, o seu nome de usuário, avatar, XP e fase atual. Você vê só o " +
                "nome do professor.",
                "Quando você sai da turma, ou o professor remove você, ou a turma é excluída, o professor deixa de ver os " +
                "seus dados. A ementa da trilha, também disponível aos professores, não contém dados de alunos.",
                "Os administradores da plataforma (membros da equipe) veem, na área de administração, o seu nome, nome de " +
                "usuário, e-mail, perfil, data de cadastro e a situação da conta (se o e-mail foi confirmado, se você entra " +
                "com o Google, se tem senha e se a conta está bloqueada). Eles não veem a sua senha. Bloqueios, " +
                "desbloqueios e edições de conteúdo ficam nos registros técnicos, com a identificação de quem fez.",
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
                            ["Identificador da conta Google (só se você entrar com o Google)", "Reconhecer você nas próximas entradas pelo Google", "Enquanto a conta existir"],
                            ["Data de confirmação do e-mail", "Liberar a entrada na conta", "Enquanto a conta existir"],
                            ["Códigos dos links enviados por e-mail (só o hash)", "Confirmar o e-mail, criar uma senha nova e confirmar a exclusão da conta", "Até 30 dias depois de vencerem (o link em si vale 24 horas ou 30 minutos), ou até a conta ser excluída"],
                            ["Sessão (cookie de login)", "Manter você conectado", "Até 8 horas ou até você sair"],
                            ["Progresso (XP, fase atual, respostas, tentativas)", "Oferecer os exercícios e acompanhar a aprendizagem", "Enquanto a conta existir"],
                            ["Resultado do nivelamento (acertos e fases liberadas, sem as respostas)", "Liberar as fases que você já domina", "Enquanto a conta existir"],
                            ["Bloqueio da conta (data)", "Impedir a entrada em caso de abuso, fraude ou risco à segurança", "Até o desbloqueio ou a exclusão da conta"],
                            ["Conquistas, gemas e avatares", "Gamificação: medalhas, saldo de gemas e loja de avatares", "Enquanto a conta existir"],
                            ["Turma (matrícula e tarefas)", "Acompanhamento pelo professor e ranking da turma", "Enquanto a turma existir; excluir a turma apaga as matrículas e as tarefas, mas não o seu progresso"],
                            ["Registros de aceite", "Comprovar a versão aceita e a data", "Enquanto a conta existir"],
                            ["Registros técnicos do servidor", "Diagnosticar falhas e proteger a aplicação", "Substituídos automaticamente pelos mais novos (o servidor guarda um volume limitado de registros)"],
                            ["Cópias de segurança do banco de dados", "Recuperar a plataforma em caso de falha", "As cópias diárias dos últimos 14 dias"],
                        ],
                    },
                },
                "\"Enquanto a conta existir\" quer dizer: até você excluir a conta (veja \"Retenção e exclusão\").",
            ],
        },
        {
            titulo: "Compartilhamento com terceiros",
            blocos: [
                "O LogiCup não vende dados pessoais e não os compartilha com terceiros para publicidade ou qualquer " +
                "finalidade diferente das descritas aqui.",
                "A fonte de letras usada no site é servida pelo próprio LogiCup, sem requisições a serviços externos.",
                "Para funcionar, o LogiCup usa os fornecedores abaixo. Eles acessam dados apenas para prestar o serviço:",
                {
                    lista: [
                        "Google Cloud (hospedagem): os servidores e o banco de dados ficam na região de São Paulo, no Brasil.",
                        "Google (entrar com o Google, só se você escolher essa opção): a entrada acontece na página do próprio Google, que confirma quem você é e nos envia os dados descritos acima. O Google fica sabendo que você entrou no LogiCup e trata os dados da sua conta Google conforme a política de privacidade dele. O Google é uma empresa dos Estados Unidos; por isso, esses dados podem ser tratados fora do Brasil.",
                        "Brevo (envio de e-mails): recebe o seu e-mail, o seu primeiro nome e o conteúdo da mensagem para entregá-la. A Brevo é uma empresa europeia e informa que guarda os dados em servidores na União Europeia; por isso, esses dados podem ser tratados fora do Brasil.",
                        "ImprovMX (encaminhamento de e-mails): as mensagens que você enviar para endereços @logicup.dev.br, como contato@logicup.dev.br, passam por esse serviço, de uma empresa dos Estados Unidos, antes de chegar à caixa da equipe.",
                    ],
                },
            ],
        },
        {
            titulo: "Segurança",
            blocos: [
                "Adotamos medidas técnicas para proteger os dados, entre elas:",
                {
                    lista: [
                        "senhas guardadas apenas como hash;",
                        "o e-mail precisa ser confirmado antes do primeiro acesso;",
                        "os links enviados por e-mail valem uma única vez e por pouco tempo (24 horas para confirmar a conta, 30 minutos para criar uma senha nova);",
                        "limite de pedidos de e-mail, para impedir que alguém encha a caixa de entrada de outra pessoa;",
                        "na entrada com o Google, só aceitamos e-mails que o próprio Google confirma pertencerem a você;",
                        "a exclusão da conta exige a sua senha ou um link de uso único enviado ao seu e-mail (válido por 30 minutos);",
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
                "Os dados são mantidos enquanto a conta existir e forem necessários para as finalidades descritas.",
                "Você pode excluir a sua conta a qualquer momento, no Perfil, na área \"Excluir conta\". Quem tem senha " +
                "confirma digitando a senha. Quem entra só com o Google recebe um link no e-mail, válido por 30 minutos, " +
                "e confirma por ele. Também é possível excluir a conta pela janela que pede o aceite de uma versão nova " +
                "destes documentos, se você não concordar com ela.",
                "Ao excluir, apagamos na hora a conta e tudo o que está ligado a ela: dados de cadastro, identificador " +
                "do Google, progresso e respostas, resultado do nivelamento, XP, conquistas, gemas, avatares, matrículas em turmas, registros de " +
                "aceite e códigos de e-mail. Em seguida, enviamos um e-mail avisando que a conta foi excluída. A exclusão " +
                "não pode ser desfeita.",
                "Algumas cópias continuam existindo por um tempo limitado:",
                {
                    lista: [
                        "cópias de segurança: o banco de dados é copiado todos os dias, e guardamos as cópias dos últimos 14 dias; o disco do servidor também tem uma cópia diária guardada por 7 dias. Os dados de uma conta excluída podem continuar nessas cópias por até cerca de 14 dias, até elas serem substituídas. As cópias só são usadas para recuperar a plataforma em caso de falha;",
                        "registros técnicos do servidor: são substituídos automaticamente pelos mais novos;",
                        "Brevo: guarda registros dos e-mails que enviou (por exemplo, destinatário, assunto e data) pelo prazo definido por ela.",
                    ],
                },
                "Professores precisam excluir as suas turmas antes de excluir a conta, para nenhum aluno ficar em uma " +
                "turma sem professor. Contas de administrador são excluídas pelo canal de contato " +
                "(contato@logicup.dev.br), que também atende qualquer pedido de exclusão.",
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
                        "anonimização, bloqueio ou eliminação de dados desnecessários, excessivos ou tratados em desconformidade com a lei (a exclusão da conta inteira você mesmo pode fazer no perfil);",
                        "portabilidade, conforme a regulamentação;",
                        "informação sobre com quem seus dados foram compartilhados;",
                        "informação sobre a possibilidade de não consentir e suas consequências, e revogação do consentimento, quando o tratamento se basear nele;",
                        "oposição a tratamento feito em desconformidade com a lei;",
                        "petição à Autoridade Nacional de Proteção de Dados (ANPD).",
                    ],
                },
                "As solicitações são recebidas pelo canal de contato: contato@logicup.dev.br.",
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
