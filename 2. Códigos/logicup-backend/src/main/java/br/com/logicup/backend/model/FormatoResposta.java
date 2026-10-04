package br.com.logicup.backend.model;

/**
 * Define COMO o aluno responde e COMO o backend corrige a questão.
 * Ex.: um PREVER_SAIDA pode ser ESCOLHA_UNICA (tem alternativas) ou RESPOSTA_CURTA (aluno digita).
 */
public enum FormatoResposta {
    ESCOLHA_UNICA,   // escolhe 1 alternativa
    ORDENACAO,       // coloca itens na ordem certa
    ASSOCIACAO,      // liga itens a opções
    RESPOSTA_CURTA,  // digita um valor curto, comparado com respostas aceitas
    TEXTO_LIVRE,     // texto/código aberto, corrigido por palavras-chave (até existir o JUnit)
    BLOCOS           // montagem de blocos (estilo Scratch)
}