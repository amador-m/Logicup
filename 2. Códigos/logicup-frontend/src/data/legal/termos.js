import { VERSAO_TERMOS, ATUALIZADO_EM } from "./versoes";

/*
 * Sprint 8: Termos de Uso, versão preliminar 0.1.
 * Base: minuta da Luciana (21/10/2026), com as lacunas preenchidas a partir do que o sistema FAZ hoje.
 * (Os pontos que ficaram genéricos na v0.1 foram definidos nas versões seguintes.)
 *
 * v0.3 (Sprint 9): conquistas, gemas (moeda virtual, sem valor em dinheiro) e loja de avatares.
 * v0.4 (Sprint 10): confirmação de e-mail obrigatória, recuperação de senha e canal de contato.
 * v0.5 (Sprint 10): entrar com o Google e exclusão da conta pelo próprio perfil (também pela janela de aceite).
 * v0.6 (Sprint 11): teste de nivelamento, bloqueio de conta pelo administrador, "fase atual" no lugar de
 *   "nível" (é o nome que as telas usam), o aviso de que o perfil de professor não é verificado, a responsável
 *   pela disponibilização (controladora, na Política) e o encerramento das outras sessões ao trocar a senha.
 *
 * Formato de cada bloco: texto = parágrafo; { lista: [...] } = lista; { tabela: {...} } = tabela.
 */
export const termos = {
    slug: "termos",
    titulo: "Termos de Uso",
    versao: VERSAO_TERMOS,
    atualizadoEm: ATUALIZADO_EM,
    resumo:
        "As regras para usar o LogiCup: o que a plataforma oferece, o que esperamos de quem a usa " +
        "e como lidamos com conta, pontuação e mudanças.",
    secoes: [
        {
            titulo: "Sobre o LogiCup",
            blocos: [
                "O LogiCup é uma plataforma web educacional desenvolvida como Trabalho de Conclusão de Curso. " +
                "Seu objetivo é apoiar o aprendizado de lógica de programação e da linguagem Java por meio de " +
                "atividades interativas e recursos de gamificação.",
                "O projeto é desenvolvido por uma equipe acadêmica. Sua disponibilização não implica, por si só, " +
                "a constituição de uma empresa ou a existência de uma pessoa jurídica responsável pela operação.",
                "Responsável pela disponibilização: Maria Eduarda Amador Mota, integrante da equipe do projeto. " +
                "Canal oficial de contato: contato@logicup.dev.br.",
            ],
        },
        {
            titulo: "Aceitação dos Termos",
            blocos: [
                "Para criar uma conta, você precisa ler estes Termos e aceitá-los marcando a caixa de aceite no " +
                "formulário de cadastro. A caixa nunca vem marcada: o aceite depende de uma ação sua.",
                "Se você não concordar com os Termos, não conclua o cadastro. Quem já tem conta e ainda não aceitou " +
                "a versão vigente verá um pedido de aceite ao entrar; até aceitar, só consegue ler os documentos, sair " +
                "da conta ou excluí-la.",
                "Estes Termos devem ser lidos junto com a Política de Privacidade. Aceitar os Termos não significa " +
                "consentir com usos opcionais de dados pessoais: se algum dia houver um uso opcional, ele será " +
                "pedido separadamente.",
                "Registramos qual versão de cada documento você aceitou e quando, para que seja possível comprovar o aceite.",
            ],
        },
        {
            titulo: "Finalidade e funcionalidades",
            blocos: [
                "O LogiCup tem finalidade educacional. Nesta versão, a plataforma oferece:",
                {
                    lista: [
                        "cadastro com confirmação do e-mail, entrada e saída da conta e criação de uma senha nova por e-mail (\"esqueci minha senha\");",
                        "entrada com a sua conta Google, se você preferir;",
                        "perfil, com edição de nome, nome de usuário e senha, e a exclusão da conta;",
                        "uma trilha de fases e rodadas, com teoria, exemplos e exercícios;",
                        "exercícios de diferentes formatos (por exemplo, múltipla escolha, ordenação e associação), com feedback sobre cada resposta;",
                        "um teste de nivelamento opcional no primeiro acesso do aluno, que pode liberar as primeiras fases para quem já domina o conteúdo delas (sem dar XP);",
                        "pontos de experiência (XP), a indicação da fase atual e a sequência de dias de estudo;",
                        "ranking com as pessoas que mais pontuaram;",
                        "conquistas (medalhas) que rendem gemas, e uma loja onde as gemas são trocadas por avatares;",
                        "turmas: o professor cria uma turma e compartilha um código; o aluno entra com o código e vê as tarefas e o ranking da turma;",
                        "para professores, o acompanhamento dos alunos da turma, a atribuição de rodadas como tarefas e a consulta da ementa da trilha.",
                    ],
                },
                "Por ser um projeto em desenvolvimento, recursos podem mudar, ser incluídos ou retirados. " +
                "Funcionalidades anunciadas como previstas não devem ser consideradas garantidas.",
                "O LogiCup é uma ferramenta complementar de aprendizagem. Concluir fases ou acumular pontos não " +
                "equivale a diploma, certificação profissional ou comprovação formal de competência.",
            ],
        },
        {
            titulo: "Cadastro e conta",
            blocos: [
                "Ao se cadastrar, informe dados verdadeiros e compatíveis com os campos pedidos.",
                "Use um e-mail que seja seu: a conta só pode ser usada depois que você abrir o link de confirmação " +
                "enviado para ele, e é por esse e-mail que você cria uma senha nova se esquecer a atual.",
                "Você é responsável por:",
                {
                    lista: [
                        "manter sua senha em sigilo (se trocar ou redefinir a senha, as sessões abertas em outros aparelhos são encerradas);",
                        "não compartilhar sua conta de forma que comprometa a segurança dela ou da plataforma;",
                        "avisar a equipe se suspeitar de uso não autorizado da sua conta;",
                        "manter atualizados os dados que você pode editar no perfil;",
                        "usar a plataforma de acordo com estes Termos e com a legislação aplicável.",
                    ],
                },
                "A conta é de uso individual.",
                "Qualquer pessoa pode escolher o perfil de professor no cadastro. Nesta versão, a plataforma não verifica " +
                "se a pessoa é de fato professora. Por isso, entre em uma turma só com o código de um professor que você " +
                "conhece: o professor da turma vê alguns dados seus, listados na Política de Privacidade.",
                "Se você entrar com o Google, o Google confirma quem você é. Nesse caso, a segurança da sua conta no " +
                "LogiCup depende também da segurança da sua conta Google. Se a sua conta Google tiver o mesmo e-mail de " +
                "uma conta que já existe no LogiCup, as duas são ligadas e você passa a poder entrar das duas formas.",
                "Você pode excluir a sua conta a qualquer momento, pelo Perfil (veja \"Suspensão, encerramento e " +
                "exclusão de conta\").",
            ],
        },
        {
            titulo: "Uso aceitável",
            blocos: [
                "Use o LogiCup para fins legítimos e educacionais. Não é permitido:",
                {
                    lista: [
                        "tentar acessar contas, dados ou áreas sem autorização;",
                        "explorar vulnerabilidades ou interferir de propósito no funcionamento da plataforma;",
                        "usar automações ou scripts que sobrecarreguem o sistema, manipulem resultados ou prejudiquem outras pessoas;",
                        "fraudar respostas, pontuação ou progresso;",
                        "inserir código malicioso ou tentar comprometer a aplicação;",
                        "usar a plataforma para atividades ilícitas;",
                        "copiar, redistribuir ou explorar comercialmente conteúdos protegidos sem autorização.",
                    ],
                },
                "Uma possível violação pode ser analisada e, quando necessário e proporcional, levar à restrição " +
                "temporária ou ao encerramento do acesso, respeitados os seus direitos.",
            ],
        },
        {
            titulo: "Exercícios, pontuação e progresso",
            blocos: [
                "Exercícios, feedbacks, fases, XP e demais elementos de jogo existem para apoiar a aprendizagem.",
                "O XP é um recurso interno da plataforma. Ele não tem valor em dinheiro, não é crédito financeiro " +
                "e não dá direito a recompensa econômica.",
                "As gemas também são um recurso interno (uma moeda virtual do jogo). Elas são ganhas apenas com " +
                "conquistas, não podem ser compradas com dinheiro, não têm valor em dinheiro, não podem ser " +
                "transferidas para outra conta e não são reembolsáveis. A troca de gemas por um avatar não pode ser " +
                "desfeita.",
                "A equipe pode ajustar os valores das conquistas e os preços dos avatares durante a evolução do projeto, " +
                "preservando os avatares que você já tiver obtido. O avatar que você escolher aparece para outras " +
                "pessoas junto com o seu nome de usuário (por exemplo, no ranking).",
                "Seu nome de usuário, seu XP e a sua fase atual aparecem no ranking para outras pessoas com conta na plataforma " +
                "quando você estiver entre as que mais pontuaram.",
                "A equipe pode corrigir erros de cálculo, registros inconsistentes ou falhas técnicas, procurando " +
                "preservar os resultados legítimos. Fases, questões e critérios de pontuação podem mudar durante a " +
                "evolução do projeto; mudanças relevantes serão comunicadas.",
                "Ao excluir a conta, o XP, as gemas, as conquistas e os avatares são apagados junto com ela e não podem " +
                "ser recuperados.",
                "Por ser um projeto em desenvolvimento, não garantimos que o progresso seja preservado indefinidamente, " +
                "especialmente em migrações ou versões experimentais. Isso não afasta responsabilidades legais aplicáveis.",
            ],
        },
        {
            titulo: "Disponibilidade e limitações técnicas",
            blocos: [
                "O LogiCup pode passar por manutenção, atualizações, interrupções ou períodos fora do ar. Algumas " +
                "funcionalidades podem ser experimentais, incompletas ou mudar com o tempo.",
                "A equipe busca corrigir as falhas encontradas dentro de suas possibilidades. Não há garantia de " +
                "disponibilidade contínua, de ausência total de erros ou de compatibilidade com todos os dispositivos " +
                "e navegadores.",
                "Esta cláusula não exclui nem limita responsabilidades que a legislação não permita afastar.",
            ],
        },
        {
            titulo: "Propriedade intelectual",
            blocos: [
                "Os elementos originais criados para o LogiCup (textos, questões, identidade visual, ilustrações, " +
                "mascote, interface e código-fonte) podem estar protegidos pela legislação de propriedade intelectual, " +
                "conforme sua autoria e eventuais licenças. Usar a plataforma não transfere a você a propriedade desses elementos.",
                "Você pode usar os recursos para sua aprendizagem pessoal. Reproduzir ou redistribuir materiais deve " +
                "respeitar as licenças aplicáveis e as exceções previstas em lei.",
                "Bibliotecas, frameworks, fontes e demais componentes de terceiros continuam sujeitos às suas próprias licenças.",
            ],
        },
        {
            titulo: "Privacidade e proteção de dados",
            blocos: [
                "O tratamento de dados pessoais no LogiCup segue a Lei Geral de Proteção de Dados Pessoais " +
                "(Lei nº 13.709/2018). A Política de Privacidade explica quais dados são tratados, para quê, por " +
                "quanto tempo e como exercer seus direitos.",
                "Aceitar estes Termos não é uma autorização irrestrita para qualquer uso de dados. Quando uma " +
                "finalidade depender de consentimento, ele será pedido separadamente, de forma específica.",
            ],
        },
        {
            titulo: "Suspensão, encerramento e exclusão de conta",
            blocos: [
                "Você pode excluir a sua conta a qualquer momento, no Perfil, na área \"Excluir conta\". A confirmação é " +
                "feita com a sua senha ou, se você entra só com o Google, por um link enviado ao seu e-mail. A exclusão " +
                "acontece na hora e não pode ser desfeita.",
                "Professores precisam excluir as suas turmas antes de excluir a conta. Contas de administrador, e " +
                "qualquer pessoa que prefira, podem pedir a exclusão pelo canal de contato (contato@logicup.dev.br).",
                "A equipe pode suspender ou encerrar contas em casos de abuso, fraude, risco à segurança ou violação " +
                "destes Termos, de forma proporcional e respeitando os seus direitos. A suspensão é feita pelo bloqueio " +
                "da conta: enquanto ela estiver bloqueada, não é possível entrar, mas nenhum dado é apagado, e o " +
                "desbloqueio devolve a conta como estava.",
                "Ao excluir a conta, apagamos os dados ligados a ela. Cópias de segurança e registros técnicos ainda " +
                "podem conter esses dados por um tempo limitado, até serem substituídos; os prazos estão na Política de " +
                "Privacidade.",
            ],
        },
        {
            titulo: "Alterações dos Termos",
            blocos: [
                "Estes Termos podem ser atualizados para refletir mudanças na plataforma ou na legislação. A versão " +
                "vigente fica sempre disponível nesta página, com número de versão e data.",
                "Quando houver uma alteração relevante, pediremos um novo aceite no seu próximo acesso.",
            ],
        },
        {
            titulo: "Contato e disposições finais",
            blocos: [
                "Dúvidas, reclamações e solicitações sobre o uso da plataforma podem ser encaminhadas à equipe do " +
                "projeto pelo canal oficial: contato@logicup.dev.br.",
                "Estes Termos são interpretados conforme a legislação brasileira. Nenhuma disposição pretende afastar " +
                "direitos garantidos por normas de aplicação obrigatória.",
            ],
        },
    ],
};
