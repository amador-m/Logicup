package br.com.logicup.backend.dto;

/**
 * Uma linha do ranking. "posicao" é null quando o aluno logado ainda não pontuou (0 XP):
 * ele não entra na classificação, e a tela mostra um convite para começar.
 * "voce" = true na linha de quem está logado (para destacar na tela).
 */
public record PosicaoRankingResponse(Integer posicao, String username, int xp, int nivel, boolean voce) {
}
