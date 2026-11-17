import { VERSAO_COOKIES, ATUALIZADO_EM } from "./versoes";

/*
 * Sprint 8: Política de Cookies, versão preliminar 0.1.
 * O inventário abaixo foi conferido no código (TokenService, SecurityConfig, api.js e AvisoCookies).
 * Se um cookie ou item de armazenamento novo for criado, ele precisa entrar nesta tabela.
 * v0.2 (Sprint 10): cookies da entrada com o Google (__Host-LOGICUP_OAUTH e __Host-LOGICUP_GOOGLE) e os itens de
 *   armazenamento local do tutorial (Onboarding/controle.js) e do Laboratório, que faltavam na tabela.
 */
export const CHAVE_AVISO_COOKIES = "logicup.aviso-cookies";

export const cookies = {
    slug: "cookies",
    titulo: "Política de Cookies",
    versao: VERSAO_COOKIES,
    atualizadoEm: ATUALIZADO_EM,
    resumo:
        "Os cookies e o armazenamento do navegador que o LogiCup usa: só o necessário para você entrar " +
        "na conta com segurança, sem publicidade nem rastreamento.",
    secoes: [
        {
            titulo: "O que são cookies",
            blocos: [
                "Cookies são pequenos registros que o navegador guarda quando você acessa um site e devolve a ele nos " +
                "acessos seguintes. Servem, por exemplo, para manter você conectado.",
                "Existe também o armazenamento local do navegador (localStorage), que é tecnicamente diferente de um " +
                "cookie: ele fica só no seu navegador e não é enviado automaticamente ao servidor.",
            ],
        },
        {
            titulo: "O que o LogiCup usa",
            blocos: [
                "O LogiCup usa apenas cookies estritamente necessários e alguns itens de armazenamento local que " +
                "guardam escolhas e trabalhos seus neste navegador.",
                {
                    tabela: {
                        colunas: ["Nome", "Tipo", "Para quê", "Duração"],
                        linhas: [
                            [
                                "__Host-LOGICUP_TOKEN",
                                "Cookie necessário",
                                "Manter você conectado depois de entrar. Não pode ser lido por scripts da página e só é enviado ao próprio LogiCup.",
                                "Até 8 horas, ou até você sair da conta",
                            ],
                            [
                                "__Host-XSRF-TOKEN",
                                "Cookie necessário",
                                "Proteger os formulários contra pedidos forjados por outros sites (proteção CSRF). Só é enviado ao próprio LogiCup.",
                                "Até você fechar o navegador; é trocado ao entrar e ao sair",
                            ],
                            [
                                "__Host-LOGICUP_OAUTH",
                                "Cookie necessário (só na entrada com o Google)",
                                "Guardar, durante a ida e a volta da página do Google, os códigos de segurança que confirmam que a resposta do Google é para este pedido. Só é enviado ao próprio LogiCup.",
                                "Até você fechar o navegador. A sessão ligada a ele vale no máximo 10 minutos e é encerrada assim que você volta do Google",
                            ],
                            [
                                "__Host-LOGICUP_GOOGLE",
                                "Cookie necessário (só na 1ª entrada com o Google)",
                                "Lembrar, enquanto você escolhe o nome de usuário e o perfil, que o Google já confirmou o seu e-mail. Não pode ser lido por scripts da página.",
                                "Até 10 minutos; é apagado ao concluir o cadastro",
                            ],
                            [
                                CHAVE_AVISO_COOKIES,
                                "Armazenamento local",
                                "Lembrar que você já clicou em \"Entendi\" no aviso de cookies, para não mostrá-lo de novo.",
                                "Até você limpar os dados do navegador",
                            ],
                            [
                                "logicup.onboarding.(número da conta)",
                                "Armazenamento local",
                                "Lembrar que você já viu o tutorial de boas-vindas, para não mostrá-lo de novo a cada entrada.",
                                "Até você limpar os dados do navegador",
                            ],
                            [
                                "logicup.laboratorio.v1.(número da conta)",
                                "Armazenamento local",
                                "Guardar o programa que você montou no Laboratório, para ele continuar lá quando você voltar. Não é enviado ao servidor, a não ser quando você clica em Executar.",
                                "Até você limpar os dados do navegador",
                            ],
                        ],
                    },
                },
                "O LogiCup não usa cookies de análise de audiência, de publicidade ou de rastreamento, e não carrega " +
                "serviços de terceiros que gravem cookies. A fonte de letras do site é servida pelo próprio LogiCup.",
                "Se você escolher entrar com o Google, a página de entrada é do próprio Google (accounts.google.com), " +
                "que usa os cookies dele, conforme a política do Google. O LogiCup não tem acesso a esses cookies.",
            ],
        },
        {
            titulo: "Por que não pedimos consentimento",
            blocos: [
                "Os itens acima são necessários para o serviço que você pediu (entrar na conta com segurança) ou " +
                "apenas guardam a sua própria escolha sobre o aviso. Por isso o aviso de cookies é informativo, e não " +
                "um pedido de autorização.",
                "Se algum dia o LogiCup passar a usar cookies opcionais, eles serão identificados nesta página e só " +
                "serão ativados com a sua permissão, que você poderá rever a qualquer momento.",
            ],
        },
        {
            titulo: "Como gerenciar no navegador",
            blocos: [
                "Você pode ver e apagar cookies e o armazenamento local nas configurações do seu navegador, e também " +
                "bloquear cookies.",
                "Se bloquear ou apagar os cookies necessários, você sairá da conta e não conseguirá entrar enquanto o " +
                "bloqueio existir. Apagar dados no navegador não apaga os dados guardados no servidor; para isso, " +
                "veja a Política de Privacidade.",
                "Excluir a conta não apaga o armazenamento local deste navegador (o tutorial e o programa do " +
                "Laboratório). Para apagá-lo, limpe os dados do site nas configurações do navegador.",
            ],
        },
        {
            titulo: "Atualizações",
            blocos: [
                "Esta Política pode ser atualizada quando mudarem as tecnologias usadas ou a legislação. A versão " +
                "vigente fica sempre disponível nesta página, com número de versão e data.",
            ],
        },
    ],
};
