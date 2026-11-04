/*
 * Função CENTRAL de comunicação com o backend. Todas as telas chamam a API por aqui.
 *
 * Responsabilidades:
 *  - montar a URL (/api/...) e o JSON;
 *  - anexar o token CSRF (cabeçalho X-XSRF-TOKEN) em POST/PUT/PATCH/DELETE (PE3);
 *  - transformar respostas de erro em ApiError, com a mensagem que o backend enviou;
 *  - avisar o AuthContext quando a sessão expirar (401 fora das rotas de autenticação);
 *  - Sprint 8: avisar o AuthContext quando o backend exigir o aceite dos Termos (403 ACEITE_PENDENTE).
 *
 * O token de login NÃO passa por aqui: ele fica no cookie httpOnly e o navegador o envia sozinho.
 */

const BASE_URL = "/api";
// Tarefa 6: prefixo __Host- (Secure, Path=/, sem Domain): nenhum subdomínio consegue criar este cookie
const NOME_COOKIE_CSRF = "__Host-XSRF-TOKEN";
const CABECALHO_CSRF = "X-XSRF-TOKEN";
const METODOS_SEM_CSRF = ["GET", "HEAD", "OPTIONS"];

/** Erro padronizado da API: status HTTP + mensagem + erros por campo + código (quando houver). */
export class ApiError extends Error {
    constructor(status, mensagem, campos = {}, codigo = null) {
        super(mensagem);
        this.name = "ApiError";
        this.status = status;
        this.campos = campos;
        this.codigo = codigo; // Sprint 8: ex.: "ACEITE_PENDENTE" (o frontend decide pelo código, não pelo texto)
    }
}

/** Sprint 8: código que o AceitePendenteFilter do backend devolve no 403. */
export const CODIGO_ACEITE_PENDENTE = "ACEITE_PENDENTE";
/** Sprint 10: senha certa, mas o e-mail ainda não foi confirmado (403 no login). */
export const CODIGO_EMAIL_NAO_CONFIRMADO = "EMAIL_NAO_CONFIRMADO";
/** Sprint 10: link de e-mail inexistente, vencido ou já usado (400). */
export const CODIGO_LINK_INVALIDO = "LINK_INVALIDO";

// função registrada pelo AuthContext para ser chamada quando a sessão expirar
let aoPerderSessao = null;

export function definirAoPerderSessao(callback) {
    aoPerderSessao = callback;
}

// Sprint 8: função registrada pelo AuthContext para quando o backend disser que falta o aceite
let aoExigirAceite = null;

export function definirAoExigirAceite(callback) {
    aoExigirAceite = callback;
}

/** Lê um cookie NÃO-httpOnly (o __Host-XSRF-TOKEN é legível de propósito; o __Host-LOGICUP_TOKEN não é). */
function lerCookie(nome) {
    const par = document.cookie
        .split("; ")
        .find((cookie) => cookie.startsWith(`${nome}=`));
    return par ? decodeURIComponent(par.substring(nome.length + 1)) : null;
}

/** Garante que o cookie __Host-XSRF-TOKEN exista, pedindo-o ao backend na primeira vez. */
async function obterTokenCsrf() {
    let token = lerCookie(NOME_COOKIE_CSRF);
    if (!token) {
        await fetch(`${BASE_URL}/auth/csrf`, { credentials: "same-origin" });
        token = lerCookie(NOME_COOKIE_CSRF);
    }
    return token;
}

/** Lê o corpo da resposta: JSON quando houver, null quando vier vazio (ex.: 204). */
async function lerCorpo(resposta) {
    const tipo = resposta.headers.get("Content-Type") ?? "";
    if (resposta.status === 204 || !tipo.includes("application/json")) {
        return null;
    }
    return resposta.json();
}

export async function requisicao(caminho, { method = "GET", body } = {}) {
    const headers = { Accept: "application/json" };

    if (body !== undefined) {
        headers["Content-Type"] = "application/json";
    }
    if (!METODOS_SEM_CSRF.includes(method)) {
        const tokenCsrf = await obterTokenCsrf();
        if (tokenCsrf) {
            headers[CABECALHO_CSRF] = tokenCsrf;
        }
    }

    let resposta;
    try {
        resposta = await fetch(`${BASE_URL}${caminho}`, {
            method,
            headers,
            credentials: "same-origin", // envia os cookies (mesmo site, graças ao proxy do Vite)
            body: body !== undefined ? JSON.stringify(body) : undefined,
        });
    } catch {
        // fetch só lança erro quando nem chegou a ter resposta (servidor fora do ar, sem internet)
        throw new ApiError(0, "Não foi possível conectar ao servidor. Tente novamente em instantes.");
    }

    const dados = await lerCorpo(resposta);

    if (!resposta.ok) {
        // 401 em rotas de autenticação é resultado normal (ex.: senha errada, ninguém logado);
        // em qualquer outra rota significa que a sessão expirou.
        if (resposta.status === 401 && !caminho.startsWith("/auth/") && aoPerderSessao) {
            aoPerderSessao();
        }
        // Sprint 8: a sessão existe, mas falta aceitar a versão vigente dos Termos -> abre a janela de aceite.
        // (normalmente o /auth/me já avisou; isto cobre a troca de versão com o site aberto)
        if (resposta.status === 403 && dados?.codigo === CODIGO_ACEITE_PENDENTE && aoExigirAceite) {
            aoExigirAceite();
        }
        throw new ApiError(
            resposta.status,
            dados?.mensagem ?? "Ocorreu um erro inesperado. Tente novamente.",
            dados?.campos ?? {},
            dados?.codigo ?? null
        );
    }

    return dados;
}

/** Endpoints de autenticação (contrato combinado com o backend). */
export const authApi = {
    me: () => requisicao("/auth/me"),
    // Sprint 7: "login" aceita e-mail OU nome de usuário (o backend decide pelo "@")
    login: (login, senha) => requisicao("/auth/login", { method: "POST", body: { login, senha } }),
    // Sprint 10: o cadastro NÃO loga mais; devolve { email, mensagem } e o link de confirmação vai por e-mail
    cadastrar: (dados) => requisicao("/auth/cadastro", { method: "POST", body: dados }),
    logout: () => requisicao("/auth/logout", { method: "POST" }),
    // Sprint 10: o código do link vai no CORPO (nunca na URL da API): não fica em log nem em histórico
    confirmarEmail: (token) => requisicao("/auth/confirmar-email", { method: "POST", body: { token } }),
    // Sprint 10: as duas respondem 202 "pedido recebido" exista ou não a conta (não revelam quem é cadastrado)
    reenviarConfirmacao: (email) => requisicao("/auth/reenviar-confirmacao", { method: "POST", body: { email } }),
    esqueciSenha: (email) => requisicao("/auth/esqueci-senha", { method: "POST", body: { email } }),
    redefinirSenha: (token, novaSenha) =>
        requisicao("/auth/redefinir-senha", { method: "POST", body: { token, novaSenha } }),
};

/*
 * Endpoints da trilha (Sprint 6). O backend decide TUDO que é regra:
 * o que está liberado, se a resposta está certa, quanto XP o aluno ganhou.
 * O frontend só exibe o resultado — por isso o gabarito nunca chega aqui.
 */
export const trilhaApi = {
    /** Fases -> rodadas com status, e o resumo do aluno (xp, nível, streak; null para professor/admin). */
    trilha: () => requisicao("/trilha"),
    /** Teoria, exemplos e exercícios de uma rodada (sem gabarito). 403 se a rodada estiver bloqueada. */
    rodada: (id) => requisicao(`/rodadas/${id}`),
    /** respostas = [{ exercicioId, resposta: {...} }]. Só aluno pode enviar. */
    enviarRespostas: (rodadaId, respostas) =>
        requisicao(`/rodadas/${rodadaId}/respostas`, { method: "POST", body: { respostas } }),
};

/*
 * Sprint 9 (ADR-001): botão "Executar" (sem XP, sem correção). O código roda no executor (Piston) do servidor.
 * Resposta: { situacao, mensagem, saida, erros, tempoMs, dica }
 *   situacao = SUCESSO | ERRO_COMPILACAO | ERRO_EXECUCAO | TEMPO_ESGOTADO | SAIDA_EXCEDIDA | INTERROMPIDO
 * Erros HTTP: 503 = executor indisponível; 429 = muitas execuções seguidas; 400 = código inválido.
 */
export const execucaoApi = {
    /** Dentro de uma questão de código: roda com os EXEMPLOS públicos da questão. */
    executarExercicio: (exercicioId, codigo) =>
        requisicao(`/exercicios/${exercicioId}/execucoes`, { method: "POST", body: { codigo } }),
    /** Laboratório (programa completo). */
    executar: (codigo, entrada = "") => requisicao("/execucoes", { method: "POST", body: { codigo, entrada } }),
};

/*
 * Sprint 7: ranking global por XP (só alunos). O backend devolve:
 *   topo = até 10 alunos com XP > 0  -> [{posicao, username, xp, nivel, voce}]
 *   voce = a linha de quem está logado (posicao null se ainda não pontuou; null para professor/admin)
 */
export const rankingApi = {
    ranking: () => requisicao("/ranking"),
};

/*
 * Sprint 7: edição do PRÓPRIO perfil (o backend descobre quem é pelo token, nunca por um id enviado aqui).
 * Para LER o perfil usamos o authApi.me(). O e-mail não é editável.
 */
export const ementaApi = {
    /** Fases -> rodadas (objetivos) -> questões (habilidade). Só professor/admin; aluno recebe 403. */
    ementa: () => requisicao("/ementa"),
};

/*
 * Sprint 8: aceite dos Termos de Uso e ciência da Política de Privacidade por quem JÁ tem conta.
 * Envia as versões que a pessoa viu; o backend confere se são as vigentes (409 se não forem),
 * grava o aceite e troca o cookie de login por um que libera o resto da API.
 */
export const legalApi = {
    aceitar: (versaoTermos, versaoPrivacidade) =>
        requisicao("/legal/aceite", { method: "POST", body: { versaoTermos, versaoPrivacidade } }),
};

/*
 * Sprint 9: turmas do PROFESSOR. O professor é sempre quem está logado (o backend lê do cookie);
 * turma de outro professor responde 404, igual a turma inexistente.
 */
export const turmaApi = {
    listar: () => requisicao("/turmas"),
    criar: (nome) => requisicao("/turmas", { method: "POST", body: { nome } }),
    /** { id, nome, codigo, alunosAtivos, alunos: [...], tarefas: [...] } */
    detalhar: (id) => requisicao(`/turmas/${id}`),
    renomear: (id, nome) => requisicao(`/turmas/${id}`, { method: "PUT", body: { nome } }),
    excluir: (id) => requisicao(`/turmas/${id}`, { method: "DELETE" }),
    novoCodigo: (id) => requisicao(`/turmas/${id}/codigo`, { method: "POST" }),
    removerAluno: (id, alunoId) => requisicao(`/turmas/${id}/alunos/${alunoId}`, { method: "DELETE" }),
    ranking: (id) => requisicao(`/turmas/${id}/ranking`),
    /** prazo = "2026-10-30" ou null */
    criarTarefa: (id, rodadaId, prazo) =>
        requisicao(`/turmas/${id}/tarefas`, { method: "POST", body: { rodadaId, prazo } }),
    removerTarefa: (id, tarefaId) => requisicao(`/turmas/${id}/tarefas/${tarefaId}`, { method: "DELETE" }),
};

/*
 * Sprint 9: "Minha turma", do ALUNO. Sem id na URL: o backend descobre a turma pelo login.
 * buscar() devolve null quando o aluno não tem turma (o backend responde 204, sem corpo).
 */
export const minhaTurmaApi = {
    buscar: () => requisicao("/minha-turma"),
    entrar: (codigo) => requisicao("/minha-turma", { method: "POST", body: { codigo } }),
    ranking: () => requisicao("/minha-turma/ranking"),
    sair: () => requisicao("/minha-turma", { method: "DELETE" }),
};

export const perfilApi = {
    /** {nome, username} -> devolve o usuário atualizado. 409 em campos.username se o username já for de outra pessoa. */
    atualizar: (dados) => requisicao("/perfil", { method: "PUT", body: dados }),
    /** {senhaAtual, novaSenha} -> 204. Senha atual errada = 400 em campos.senhaAtual (não desloga). */
    alterarSenha: (dados) => requisicao("/perfil/senha", { method: "PUT", body: dados }),
    /** Sprint 9: troca o avatar EM USO -> devolve o usuário atualizado. 403 se a pessoa não tem esse avatar. */
    trocarAvatar: (codigo) => requisicao("/perfil/avatar", { method: "PUT", body: { codigo } }),
};

/*
 * Sprint 9: conquistas do ALUNO (professor/admin recebem 403).
 * As medalhas são GRAVADAS pelo backend no envio de rodada, na entrada em turma e no login (decisão B1):
 * aqui o frontend só lê e avisa que já mostrou a comemoração.
 */
export const conquistaApi = {
    /** { gemas, conquistas: [{ codigo, titulo, valorAtual, niveis: [{ nivel, meta, descricao, gemas, desbloqueadaEm }] }] } */
    listar: () => requisicao("/conquistas"),
    /** As ainda não vistas: [{ codigo, titulo, nivel, descricao, gemas, desbloqueadaEm }] */
    novas: () => requisicao("/conquistas/novas"),
    /** A comemoração foi mostrada -> 204 */
    marcarVistas: () => requisicao("/conquistas/vistas", { method: "POST" }),
};

/*
 * Sprint 9: avatares. catalogo() vale para todos (gemas = null para professor/admin);
 * comprar() é só do aluno: 409 se faltar gema ou se já tiver o avatar.
 */
export const avatarApi = {
    /** { gemas, avatarAtual, avatares: [{ codigo, nome, preco, gratuito, possui, emUso }] } */
    catalogo: () => requisicao("/avatares"),
    /** Devolve o catálogo atualizado (saldo novo). */
    comprar: (codigo) => requisicao(`/avatares/${encodeURIComponent(codigo)}/compra`, { method: "POST" }),
};
