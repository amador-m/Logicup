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
    BLOCOS,          // Sprint 9 (ADR-001): blocos (Blockly) que viram Java no navegador; corrigido como CODIGO
    CODIGO;          // Sprint 9 (ADR-001): código Java EXECUTADO no Piston e comparado com casos de teste

    /**
     * true = a correção EXECUTA código no executor (leva segundos e pode ficar PENDENTE).
     * Usado para executar fora da transação no Enviar e para liberar o botão Executar da questão.
     */
    public boolean executaCodigo() {
        return this == CODIGO || this == BLOCOS;
    }
}