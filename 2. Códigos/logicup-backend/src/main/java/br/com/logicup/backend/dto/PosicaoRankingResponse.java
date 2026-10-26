package br.com.logicup.backend.dto;

import br.com.logicup.backend.model.Avatar;

/**
 * Uma linha do ranking. "posicao" é null quando o aluno logado ainda não pontuou (0 XP):
 * ele não entra na classificação, e a tela mostra um convite para começar.
 * "voce" = true na linha de quem está logado (para destacar na tela).
 * Sprint 9: avatar = o avatar escolhido pelo aluno (escolha pública, como o username).
 */
public record PosicaoRankingResponse(Integer posicao, String username, int xp, int nivel, boolean voce,
                                     Avatar avatar) {
}
