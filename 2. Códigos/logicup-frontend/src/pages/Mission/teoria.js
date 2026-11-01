/*
 * Sprint 9: regras de APRESENTAÇÃO da teoria (funções puras, sem React — fáceis de testar).
 * O conteúdo vem do banco como blocos simples (parágrafo, lista, código, tabela); aqui decidimos
 * como cada um aparece, sem precisar mudar o Banco de Questões nem o seed.
 */

// Parágrafos que viram um balão do Gattuccino. "estilo" escolhe a pose e a cor do balão.
const DESTAQUES = [
    { prefixo: /^(Atenção|Cuidado|Importante)\b/i, estilo: "alerta", rotulo: "Atenção!" },
    { prefixo: /^(Dica|Lembre-se|Lembre)\b/i, estilo: "dica", rotulo: "Dica do Gattuccino" },
    { prefixo: /^(Por enquanto|Não se preocupe|Observe|Note que|Repare)\b/i, estilo: "dica", rotulo: "Gattuccino explica" },
];

/** Se o parágrafo deve virar um balão do Gattuccino, devolve {estilo, rotulo}; senão, null. */
export function destaqueDoParagrafo(texto) {
    const achado = DESTAQUES.find((d) => d.prefixo.test(texto.trim()));
    return achado ? { estilo: achado.estilo, rotulo: achado.rotulo } : null;
}

/**
 * "Exemplo: Ao escovar os dentes..." -> {rotulo: "Exemplo", resto: "Ao escovar os dentes..."}
 * "Exemplo:" sozinho (apresenta o bloco seguinte) -> {rotulo: "Exemplo", resto: ""}
 */
export function exemploDoParagrafo(texto) {
    const m = /^(Exemplos?(?: de [^:]{1,30})?):\s*(.*)$/s.exec(texto.trim());
    return m ? { rotulo: m[1], resto: m[2] } : null;
}

/** O parágrafo apresenta uma SAÍDA de programa? ("Resultado no console:", "Exemplo de interação:") */
export function apresentaConsole(texto) {
    return Boolean(texto) && /(console|intera[çc][ãa]o|sa[íi]da)[^.]*:\s*$/i.test(texto.trim());
}

// Algo "parece código" se tem símbolos típicos de Java ou palavras-chave da linguagem
const PARECE_CODIGO = /[(){};[\]"=<>+]|\/\/|\b(public|private|static|void|import|class|int|double|boolean|String|System|new|return)\b/;

/**
 * Itens do tipo "termo: explicação" (muito usados para explicar o código linha a linha).
 * "System.out.println(...);: exibe..." -> {termo: "System.out.println(...);", resto: "exibe...", codigo: true}
 * "Decomposição: dividir um problema..." -> {termo: "Decomposição", ..., codigo: false}
 * O termo precisa ser curto (até 80 caracteres); frases comuns com ":" no meio não são cortadas.
 */
export function separarTermo(texto) {
    const t = texto.trim();
    // procura o primeiro ": " (dois-pontos seguido de espaço) — evita cortar "String[] args" ou URLs
    let posicao = -1;
    for (let i = 0; i < t.length - 1 && i <= 80; i++) {
        if (t[i] === ":" && t[i + 1] === " ") { posicao = i; break; }
    }
    if (posicao <= 0) return null;
    const termo = t.slice(0, posicao);
    const codigo = PARECE_CODIGO.test(termo);
    // termo comum (não código) só vale se for curto, tipo um rótulo ("Decomposição", "Compilação")
    if (!codigo && termo.split(/\s+/).length > 5) return null;
    return { termo, resto: t.slice(posicao + 2), codigo };
}

/**
 * Marca trechos de código DENTRO de um parágrafo, para aparecerem em fonte monoespaçada:
 *   chamadas de método -> System.out.println(), input.nextLine()
 *   arquivos e extensões -> Main.java, .java, .class
 * e as siglas com definição ("JDK (Java Development Kit)") para aparecerem em negrito.
 * Devolve [{texto, codigo: boolean}, ...], na ordem (juntando tudo, volta o texto original).
 */
const INLINE = /\b[A-Za-z_]\w*(?:\.[A-Za-z_]\w*)*\([^()\n]{0,40}\)|\b[A-Za-z_]\w*\.(?:java|class)\b|(?<![\w])\.(?:java|class)\b/g;

// Siglas com a definição entre parênteses: "JDK (Java Development Kit)" -> em negrito, para facilitar a leitura
const SIGLA = /\b[A-Z]{2,5}\s\([^()\n]{4,60}\)/g;

export function codigoNoParagrafo(texto) {
    const achados = [
        ...[...texto.matchAll(INLINE)].map((m) => ({ inicio: m.index, fim: m.index + m[0].length, tipo: "codigo" })),
        ...[...texto.matchAll(SIGLA)].map((m) => ({ inicio: m.index, fim: m.index + m[0].length, tipo: "sigla" })),
    ].sort((a, b) => a.inicio - b.inicio);

    const partes = [];
    let ultimo = 0;
    for (const achado of achados) {
        if (achado.inicio < ultimo) continue; // sobrepõe um trecho já marcado: ignora
        if (achado.inicio > ultimo) partes.push({ texto: texto.slice(ultimo, achado.inicio), codigo: false });
        partes.push({ texto: texto.slice(achado.inicio, achado.fim), codigo: achado.tipo === "codigo", sigla: achado.tipo === "sigla" });
        ultimo = achado.fim;
    }
    if (ultimo < texto.length) partes.push({ texto: texto.slice(ultimo), codigo: false });
    return partes;
}

/** Tempo de leitura aproximado (200 palavras por minuto), no mínimo 1 minuto. */
export function minutosDeLeitura(secoes) {
    const palavras = secoes
        .flatMap((s) => (s.blocos?.length ? s.blocos : [{ tipo: "paragrafo", texto: s.texto ?? "" }]))
        .map((b) => b.texto ?? b.itens?.join(" ") ?? "")
        .join(" ")
        .split(/\s+/)
        .filter(Boolean).length;
    return Math.max(1, Math.round(palavras / 200));
}
