package br.com.logicup.backend.execucao;

/**
 * Sprint 9 (ADR-001): o que pedimos para o executor rodar.
 *
 * codigoFonte: um arquivo Java completo. Pode ter várias classes; a PRIMEIRA precisa ter o main
 *              (o Piston usa o "modo de arquivo único" do Java: java Main.java).
 * entrada:     o que o programa vai ler do teclado (System.in). Vazio se o programa não lê nada.
 */
public record ProgramaJava(String codigoFonte, String entrada) {

    public ProgramaJava {
        if (codigoFonte == null || codigoFonte.isBlank()) {
            throw new IllegalArgumentException("O código-fonte não pode ser vazio");
        }
        entrada = entrada == null ? "" : entrada;
    }

    public static ProgramaJava semEntrada(String codigoFonte) {
        return new ProgramaJava(codigoFonte, "");
    }
}
