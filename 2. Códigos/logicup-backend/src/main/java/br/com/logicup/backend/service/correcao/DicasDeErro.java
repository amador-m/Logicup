package br.com.logicup.backend.service.correcao;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Sprint 9 (ADR-001): uma dica em português para os erros mais comuns de quem está começando.
 * As mensagens do compilador e da JVM vêm em inglês e técnicas; a dica não substitui a mensagem
 * original (o aluno também precisa aprender a ler o erro real), só explica o que ela quer dizer.
 *
 * A ordem importa: a primeira regra que bater é a usada (as mais específicas vêm primeiro).
 */
public final class DicasDeErro {

    private static final Map<String, String> DICAS = new LinkedHashMap<>();

    static {
        DICAS.put("is already defined",
                "Você declarou de novo uma variável que já existe. Se o enunciado diz que ela já está declarada, "
                        + "use-a sem colocar o tipo na frente (ex.: energia, e não int energia).");
        DICAS.put("variable initializer is 'void'",
                "O método precisa DEVOLVER o valor com return, e não só exibir com println.");
        DICAS.put("'void' type not allowed here",
                "O método precisa DEVOLVER o valor com return, e não só exibir com println.");
        DICAS.put("missing return statement",
                "O método promete devolver um valor, mas em algum caminho ele termina sem return.");
        DICAS.put("';' expected", "Falta um ponto e vírgula (;) no fim de uma instrução, na linha indicada.");
        DICAS.put("reached end of file while parsing",
                "Alguma chave { foi aberta e não foi fechada com }.");
        DICAS.put("class, interface, enum, or record expected",
                "Há uma chave } a mais, ou código fora de uma classe. Confira se as chaves { } estão em pares.");
        DICAS.put("incompatible types",
                "Um valor de um tipo foi usado onde o Java esperava outro (ex.: texto onde deveria ser número).");
        DICAS.put("cannot find symbol",
                "O Java não reconhece um nome usado na linha indicada. Confira se está escrito igual "
                        + "(maiúsculas e minúsculas contam) e se foi declarado antes.");
        DICAS.put("non-static", "Você usou algo de instância (sem static) dentro de um método static.");
        DICAS.put("StackOverflowError",
                "Um método está chamando a si mesmo sem parar. Para usar a versão da superclasse, escreva super.nomeDoMetodo().");
        DICAS.put("ArrayIndexOutOfBoundsException",
                "Você acessou uma posição que não existe no vetor. Os índices vão de 0 até tamanho - 1 "
                        + "(no for, use i < vetor.length, e não <=).");
        DICAS.put("IndexOutOfBoundsException",
                "Você acessou uma posição que não existe na lista. Os índices vão de 0 até size() - 1.");
        DICAS.put("NullPointerException",
                "Você usou um objeto que ainda não foi criado (está valendo null).");
        DICAS.put("ArithmeticException: / by zero", "Houve uma divisão por zero.");
        DICAS.put("NoSuchFieldException",
                "O programa de teste procurou um atributo com o nome pedido no enunciado e não encontrou. Confira o nome.");
        DICAS.put("InputMismatchException",
                "O programa tentou ler um número, mas a entrada tinha outro tipo de valor.");
        DICAS.put("NoSuchElementException",
                "O programa tentou ler mais valores do que a entrada tinha.");
    }

    private DicasDeErro() {}

    /** A dica para a mensagem de erro, ou null se não for um erro conhecido. */
    public static String para(String mensagemDeErro) {
        if (mensagemDeErro == null || mensagemDeErro.isBlank()) return null;
        for (Map.Entry<String, String> regra : DICAS.entrySet()) {
            if (mensagemDeErro.contains(regra.getKey())) return regra.getValue();
        }
        return null;
    }
}
