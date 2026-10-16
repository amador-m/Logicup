/*
 * Função CENTRAL de comunicação com o backend. Todas as telas chamam a API por aqui.
 *
 * Responsabilidades:
 *  - montar a URL (/api/...) e o JSON;
 *  - anexar o token CSRF (cabeçalho X-XSRF-TOKEN) em POST/PUT/PATCH/DELETE (PE3);
 *  - transformar respostas de erro em ApiError, com a mensagem que o backend enviou;
 *  - avisar o AuthContext quando a sessão expirar (401 fora das rotas de autenticação).
 *
 * O token de login NÃO passa por aqui: ele fica no cookie httpOnly e o navegador o envia sozinho.
 */

const BASE_URL = "/api";
const NOME_COOKIE_CSRF = "XSRF-TOKEN";
const CABECALHO_CSRF = "X-XSRF-TOKEN";
const METODOS_SEM_CSRF = ["GET", "HEAD", "OPTIONS"];

/** Erro padronizado da API: status HTTP + mensagem + erros por campo (quando houver). */
export class ApiError extends Error {
    constructor(status, mensagem, campos = {}) {
        super(mensagem);
        this.name = "ApiError";
        this.status = status;
        this.campos = campos;
    }
}

// função registrada pelo AuthContext para ser chamada quando a sessão expirar
let aoPerderSessao = null;

export function definirAoPerderSessao(callback) {
    aoPerderSessao = callback;
}

/** Lê um cookie NÃO-httpOnly (o XSRF-TOKEN é legível de propósito; o LOGICUP_TOKEN não é). */
function lerCookie(nome) {
    const par = document.cookie
        .split("; ")
        .find((cookie) => cookie.startsWith(`${nome}=`));
    return par ? decodeURIComponent(par.substring(nome.length + 1)) : null;
}

/** Garante que o cookie XSRF-TOKEN exista, pedindo-o ao backend na primeira vez. */
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
        throw new ApiError(
            resposta.status,
            dados?.mensagem ?? "Ocorreu um erro inesperado. Tente novamente.",
            dados?.campos ?? {}
        );
    }

    return dados;
}

/** Endpoints de autenticação (contrato combinado com o backend). */
export const authApi = {
    me: () => requisicao("/auth/me"),
    // Sprint 7: "login" aceita e-mail OU nome de usuário (o backend decide pelo "@")
    login: (login, senha) => requisicao("/auth/login", { method: "POST", body: { login, senha } }),
    cadastrar: (dados) => requisicao("/auth/cadastro", { method: "POST", body: dados }),
    logout: () => requisicao("/auth/logout", { method: "POST" }),
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
export const perfilApi = {
    /** {nome, username} -> devolve o usuário atualizado. 409 em campos.username se o username já for de outra pessoa. */
    atualizar: (dados) => requisicao("/perfil", { method: "PUT", body: dados }),
    /** {senhaAtual, novaSenha} -> 204. Senha atual errada = 400 em campos.senhaAtual (não desloga). */
    alterarSenha: (dados) => requisicao("/perfil/senha", { method: "PUT", body: dados }),
};
