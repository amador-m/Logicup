package br.com.logicup.backend.legal;

import br.com.logicup.backend.model.DocumentoLegal;

/**
 * Sprint 8: versões VIGENTES dos documentos legais. É o único lugar do backend com esses números.
 *
 * Ao publicar uma versão nova de um texto:
 *   1) troque a versão aqui e no frontend (src/data/legal), no mesmo deploy;
 *   2) todo usuário passa a ser "pendente" automaticamente (o token dele traz a versão antiga)
 *      e o sistema pede o novo aceite no próximo acesso.
 * São constantes (e não configuração) porque mudam junto com o TEXTO, que também está no código.
 */
public final class VersoesLegais {

    // Sprint 9: 0.2 — turmas (o professor passa a ver dados dos alunos da turma dele)
    // Sprint 9: 0.3 — conquistas, gemas (moeda virtual) e avatares
    public static final String TERMOS = "0.3";
    public static final String PRIVACIDADE = "0.3";

    /** O que vai DENTRO do token de quem aceitou a versão vigente dos dois documentos. Ex.: "T0.1|P0.1". */
    public static final String IDENTIFICADOR = "T" + TERMOS + "|P" + PRIVACIDADE;

    private VersoesLegais() {}

    public static String vigente(DocumentoLegal documento) {
        return switch (documento) {
            case TERMOS -> TERMOS;
            case PRIVACIDADE -> PRIVACIDADE;
        };
    }

    /** true se o token traz o aceite das versões VIGENTES (token antigo ou sem aceite -> false). */
    public static boolean aceiteVigente(String identificadorNoToken) {
        return IDENTIFICADOR.equals(identificadorNoToken);
    }
}
