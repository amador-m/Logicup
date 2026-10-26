package br.com.logicup.backend.model;

import java.util.Arrays;
import java.util.Optional;

/**
 * Sprint 9: o CATÁLOGO de avatares (decisão M1-A: no código, não no banco).
 * Os dois gatos são gratuitos; os outros são comprados com gemas (opção 3 dos avatares).
 *
 * Preços (decisão M4, 22/10): os 6 pagos somam 760 gemas, pensado para que NÃO dê para comprar
 * todos até a Fase 3 (máximo estimado ≈ 360 gemas). Avatares novos do Davi entram como novas constantes.
 * O código (nome da constante) é o que vai para o banco (usuario.avatar e aluno_avatar.codigo_avatar)
 * e para o frontend, que tem a imagem correspondente. Por isso NÃO renomeie uma constante já em uso.
 */
public enum Avatar {

    GATO_CLARO("Gato", 0),
    GATO_ESCURO("Gato noturno", 0),
    CACHORRO_CLARO("Cachorro", 60),
    CACHORRO_ESCURO("Cachorro noturno", 60),
    BARISTA_CLARO("Barista", 120),
    BARISTA_ESCURO("Barista noturno", 120),
    PASSARO_PAO("Pássaro de pão", 200),
    PEIXE_BISCOITO("Peixe biscoito", 200);

    /** Avatar de quem acabou de se cadastrar (e o DEFAULT da coluna usuario.avatar). */
    public static final Avatar PADRAO = GATO_CLARO;

    private final String nome;
    private final int preco;

    Avatar(String nome, int preco) {
        this.nome = nome;
        this.preco = preco;
    }

    public static Optional<Avatar> porCodigo(String codigo) {
        return Arrays.stream(values()).filter(avatar -> avatar.name().equals(codigo)).findFirst();
    }

    public String getNome() { return nome; }
    public int getPreco() { return preco; }
    public boolean isGratuito() { return preco == 0; }
}
