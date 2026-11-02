package br.com.logicup.backend.service.correcao;

import br.com.logicup.backend.dto.DetalheCorrecaoResponse;
import br.com.logicup.backend.exception.ExecutorIndisponivelException;
import br.com.logicup.backend.exception.RequisicaoInvalidaException;
import br.com.logicup.backend.execucao.ExecutorDeCodigo;
import br.com.logicup.backend.execucao.ProgramaJava;
import br.com.logicup.backend.execucao.ResultadoExecucao;
import br.com.logicup.backend.model.Exercicio;
import br.com.logicup.backend.model.FormatoResposta;
import br.com.logicup.backend.service.correcao.ComparadorSaida.Comparacao;
import br.com.logicup.backend.service.correcao.MontadorPrograma.Modo;
import br.com.logicup.backend.service.correcao.MontadorPrograma.ProgramaMontado;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

/**
 * Sprint 9 (ADR-001): corrige questões de CÓDIGO executando o código do aluno (formato CODIGO).
 *
 * Passo a passo:
 *  1. lê o gabarito (secreto): programa de teste, casos e saídas esperadas, palavras-chave de reserva;
 *  2. confere a estrutura (ex.: colou um programa inteiro quando era só um trecho);
 *  3. encaixa o código do aluno no programa de teste (MontadorPrograma);
 *  4. executa UMA vez no executor (todos os casos rodam na mesma execução, cerca de 1 s);
 *  5. separa a saída por caso e compara com a esperada (ComparadorSaida);
 *  6. devolve CORRETA, INCORRETA (com o PRIMEIRO caso que falhou) ou PENDENTE (executor fora do ar).
 *
 * Gabarito (gabarito_json):
 *  {"palavrasChave": [...], "minimo": 6, "respostaModelo": "...",      <- reserva + resposta exibida
 *   "modo": "TRECHO|MEMBRO|CLASSES", "comparacao": "EXATA|NUMEROS|ULTIMO_NUMERO",
 *   "programaTeste": "...//@CODIGO_DO_ALUNO...", "programaExemplo": "...",
 *   "casos": [{"descricao": "energia = 0", "saida": "Exausto: hora do café!"}, ...]}
 * Resposta do aluno: {"codigo": "..."}
 *
 * Os programas e as saídas esperadas são gerados e VALIDADOS pelo gerar_questoes_codigo.py
 * (soluções certas precisam passar e erros típicos precisam reprovar).
 */
@Component
public class CorretorCodigo extends CorretorBase {

    /** Mesmo limite do botão Executar (ExecucaoRequest). */
    public static final int TAMANHO_MAXIMO = 10_000;
    /** Esperado/obtido muito grandes são cortados na tela. */
    private static final int LIMITE_TRECHO = 600;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Caso(String descricao, String saida) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Gabarito(List<List<String>> palavrasChave, Integer minimo, String respostaModelo,
                           Modo modo, Comparacao comparacao, String programaTeste, String programaExemplo,
                           List<Caso> casos) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Resposta(String codigo) {}

    private final ExecutorDeCodigo executor;

    public CorretorCodigo(JsonMapper jsonMapper, ExecutorDeCodigo executor) {
        super(jsonMapper);
        this.executor = executor;
    }

    @Override
    public FormatoResposta formato() {
        return FormatoResposta.CODIGO;
    }

    @Override
    public boolean corrigir(Exercicio exercicio, Object resposta) {
        return avaliar(exercicio, resposta).correta();
    }

    @Override
    public Object respostaCorreta(Exercicio exercicio) {
        return gabarito(exercicio).respostaModelo();
    }

    @Override
    public ResultadoCorrecao avaliar(Exercicio exercicio, Object resposta) {
        Gabarito gabarito = gabarito(exercicio);
        String codigo = lerCodigo(resposta);
        int total = gabarito.casos().size();

        // 2) estrutura: explicamos em português, sem gastar uma execução
        try {
            MontadorPrograma.conferirEstrutura(gabarito.modo(), codigo);
        } catch (RequisicaoInvalidaException e) {
            return incorreta("ESTRUTURA", e.getMessage(), 0, total, null, null, null, null, null);
        }

        // 3-4) monta e executa
        ProgramaMontado programa = MontadorPrograma.montar(gabarito.programaTeste(), codigo);
        ResultadoExecucao execucao;
        try {
            execucao = executor.executar(ProgramaJava.semEntrada(programa.codigo()));
        } catch (ExecutorIndisponivelException e) {
            return pendente(gabarito, codigo, total);
        }

        // 5) compara caso a caso
        List<String> saidas = ComparadorSaida.separarCasos(execucao.saida());
        String erros = programa.traduzirLinhas(execucao.erros()).strip();
        String dica = DicasDeErro.para(execucao.erros());

        switch (execucao.situacao()) {
            case ERRO_COMPILACAO -> {
                return incorreta("ERRO_COMPILACAO", "O código não compila. Veja a linha indicada no erro.",
                        0, total, null, null, null, erros, dica);
            }
            case TEMPO_ESGOTADO -> {
                return incorreta("TEMPO_ESGOTADO",
                        "O programa demorou demais e foi interrompido. Verifique se há um laço que nunca termina "
                                + "ou um método que chama a si mesmo sem parar.",
                        aprovadosAntesDe(gabarito, saidas), total, casoEmQueParou(gabarito, saidas), null, null, null, null);
            }
            case SAIDA_EXCEDIDA -> {
                return incorreta("TEMPO_ESGOTADO",
                        "O programa escreveu texto demais e foi interrompido. Verifique se há um laço que nunca termina.",
                        aprovadosAntesDe(gabarito, saidas), total, casoEmQueParou(gabarito, saidas), null, null, null, null);
            }
            case ERRO_EXECUCAO, INTERROMPIDO -> {
                // exceção no meio de um caso: os casos anteriores podem ter passado
                return incorreta("ERRO_EXECUCAO", "O programa parou com um erro durante este caso de teste.",
                        aprovadosAntesDe(gabarito, saidas), total, casoEmQueParou(gabarito, saidas), null, null,
                        erros, dica);
            }
            case SUCESSO -> {
                for (int i = 0; i < total; i++) {
                    Caso caso = gabarito.casos().get(i);
                    if (i >= saidas.size()) { // terminou "normalmente" antes da hora (ex.: System.exit)
                        return incorreta("ERRO_EXECUCAO", "O programa terminou antes de executar todos os casos de teste.",
                                i, total, caso.descricao(), null, null, null, null);
                    }
                    if (!ComparadorSaida.iguais(gabarito.comparacao(), caso.saida(), saidas.get(i))) {
                        return incorreta("SAIDA_DIFERENTE", "O código roda, mas a saída de um caso de teste está diferente da esperada.",
                                i, total, caso.descricao(), cortar(caso.saida()),
                                cortar(ComparadorSaida.normalizar(saidas.get(i))), null, null);
                    }
                }
                return new ResultadoCorrecao(ResultadoCorrecao.Situacao.CORRETA, new DetalheCorrecaoResponse(
                        "TESTES_OK", "Todos os casos de teste passaram!", total, total, null, null, null, null, null));
            }
            default -> throw new IllegalStateException("Situação desconhecida: " + execucao.situacao());
        }
    }

    /**
     * Programa do botão "Executar" dentro da questão: o código do aluno encaixado no programa de EXEMPLO
     * (exemplos públicos, sem casos ocultos). Usado pelo ExecucaoService.
     */
    public ProgramaMontado montarExemplo(Exercicio exercicio, String codigo) {
        Gabarito gabarito = gabarito(exercicio);
        MontadorPrograma.conferirEstrutura(gabarito.modo(), codigo); // RequisicaoInvalida -> 400 com a explicação
        return MontadorPrograma.montar(gabarito.programaExemplo(), codigo);
    }

    // ---------------- auxiliares ----------------

    private Gabarito gabarito(Exercicio exercicio) {
        Gabarito g = lerDoBanco(exercicio.getGabaritoJson(), Gabarito.class, exercicio.getCodigoQuestao());
        if (g.modo() == null || g.comparacao() == null || g.programaTeste() == null || g.casos() == null
                || g.casos().isEmpty() || g.palavrasChave() == null || g.minimo() == null) {
            throw new IllegalStateException("Gabarito de código incompleto na questão " + exercicio.getCodigoQuestao());
        }
        return g;
    }

    private String lerCodigo(Object resposta) {
        Resposta enviada = lerResposta(resposta, Resposta.class);
        exigir(enviada.codigo() != null && !enviada.codigo().isBlank(), "Escreva o seu código antes de enviar.");
        exigir(enviada.codigo().length() <= TAMANHO_MAXIMO, "O código pode ter no máximo 10.000 caracteres.");
        return enviada.codigo();
    }

    /**
     * Decisão 1-A (02/11): executor fora do ar -> PENDENTE. As palavras-chave viram só uma PRÉVIA para o aluno
     * (não dão XP nem contam como erro). O texto deixa claro que a validação por execução não aconteceu.
     */
    private ResultadoCorrecao pendente(Gabarito gabarito, String codigo, int total) {
        boolean pareceCompleto = CorretorTextoLivre.atingeMinimo(gabarito.palavrasChave(), gabarito.minimo(), codigo);
        String previa = pareceCompleto
                ? "Pela leitura do texto, o seu código parece ter os elementos esperados."
                : "Pela leitura do texto, parece que ainda falta algum elemento pedido no enunciado.";
        return new ResultadoCorrecao(ResultadoCorrecao.Situacao.PENDENTE, new DetalheCorrecaoResponse(
                "EXECUTOR_INDISPONIVEL",
                "Não foi possível executar o seu código agora, então ele ainda NÃO foi validado e não vale XP. "
                        + previa + " Envie de novo em alguns minutos.",
                null, total, null, null, null, null, null));
    }

    private static ResultadoCorrecao incorreta(String tipo, String mensagem, int aprovados, int total, String caso,
                                               String esperado, String obtido, String erros, String dica) {
        return new ResultadoCorrecao(ResultadoCorrecao.Situacao.INCORRETA, new DetalheCorrecaoResponse(
                tipo, mensagem, aprovados, total, caso, esperado, obtido,
                erros == null || erros.isEmpty() ? null : cortar(erros), dica));
    }

    /** O programa imprime a marca ANTES de cada caso: o caso em que parou é o da última marca impressa. */
    private static String casoEmQueParou(Gabarito gabarito, List<String> saidas) {
        int indice = Math.max(0, saidas.size() - 1);
        return indice < gabarito.casos().size() ? gabarito.casos().get(indice).descricao() : null;
    }

    /** Quantos casos (antes do caso em que parou) deram a saída certa. */
    private static int aprovadosAntesDe(Gabarito gabarito, List<String> saidas) {
        int aprovados = 0;
        for (int i = 0; i < saidas.size() - 1 && i < gabarito.casos().size(); i++) {
            if (!ComparadorSaida.iguais(gabarito.comparacao(), gabarito.casos().get(i).saida(), saidas.get(i))) break;
            aprovados++;
        }
        return aprovados;
    }

    private static String cortar(String texto) {
        return texto.length() <= LIMITE_TRECHO ? texto : texto.substring(0, LIMITE_TRECHO) + "\n[...]";
    }
}
