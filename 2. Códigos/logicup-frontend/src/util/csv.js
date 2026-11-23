/*
 * Sprint 11: RELATÓRIO em CSV (planilha) gerado no próprio navegador, a partir dos dados que a tela já tem.
 * Não precisa de endpoint novo: o professor já recebeu esses dados ao abrir a turma.
 *
 * Detalhes que fazem diferença:
 *  - separador ";" e BOM UTF-8 no começo: é o que o Excel em português espera (com "," e sem BOM,
 *    ele junta tudo numa coluna só e estraga os acentos);
 *  - aspas: todo valor vai entre aspas, e aspas internas viram "" (regra do formato CSV);
 *  - INJEÇÃO DE FÓRMULA (CSV injection): um nome digitado como =HYPERLINK(...) viraria uma FÓRMULA ao abrir
 *    a planilha. Por isso, texto que começa com = + - @ (ou tab/enter) ganha um ' na frente e fica só texto.
 */
const INICIO_DE_FORMULA = /^[=+\-@\t\r]/;

export function celulaCsv(valor) {
    if (valor === null || valor === undefined) return '""';
    let texto = String(valor);
    if (typeof valor === "string" && INICIO_DE_FORMULA.test(texto)) {
        texto = `'${texto}`;
    }
    return `"${texto.replaceAll('"', '""')}"`;
}

/** cabecalho = ["Nome", ...]; linhas = [[...], ...] -> texto do arquivo (com BOM). */
export function montarCsv(cabecalho, linhas) {
    const todas = [cabecalho, ...linhas].map((linha) => linha.map(celulaCsv).join(";"));
    return `\uFEFF${todas.join("\r\n")}\r\n`;
}

/** Faz o navegador baixar o texto como arquivo (sem passar pelo servidor). */
export function baixarArquivo(nome, conteudo, tipo = "text/csv;charset=utf-8") {
    const url = URL.createObjectURL(new Blob([conteudo], { type: tipo }));
    const link = document.createElement("a");
    link.href = url;
    link.download = nome;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
}

/** "Turma 1 - Manhã!" -> "turma-1-manha" (nome de arquivo seguro, sem acentos nem símbolos). */
export function nomeDeArquivo(texto) {
    return texto.normalize("NFD").replace(/[̀-ͯ]/g, "").toLowerCase()
        .replace(/[^a-z0-9]+/g, "-").replace(/^-+|-+$/g, "") || "turma";
}
