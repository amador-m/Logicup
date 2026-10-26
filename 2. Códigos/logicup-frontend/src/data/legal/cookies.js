import { VERSAO_COOKIES, ATUALIZADO_EM } from "./versoes";

/*
 * Sprint 8: Política de Cookies, versão preliminar 0.1.
 * O inventário abaixo foi conferido no código (TokenService, SecurityConfig, api.js e AvisoCookies).
 * Se um cookie ou item de armazenamento novo for criado, ele precisa entrar nesta tabela.
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
                "O LogiCup usa apenas cookies estritamente necessários e um item de armazenamento local para lembrar " +
                "que você já viu o aviso de cookies.",
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
                                CHAVE_AVISO_COOKIES,
                                "Armazenamento local",
                                "Lembrar que você já clicou em \"Entendi\" no aviso de cookies, para não mostrá-lo de novo.",
                                "Até você limpar os dados do navegador",
                            ],
                        ],
                    },
                },
                "O LogiCup não usa cookies de análise de audiência, de publicidade ou de rastreamento, e não carrega " +
                "serviços de terceiros que gravem cookies. A fonte de letras do site é servida pelo próprio LogiCup.",
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
