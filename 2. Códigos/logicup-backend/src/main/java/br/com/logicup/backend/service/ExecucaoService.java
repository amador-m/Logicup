package br.com.logicup.backend.service;

import br.com.logicup.backend.dto.ExecucaoExercicioRequest;
import br.com.logicup.backend.dto.ExecucaoRequest;
import br.com.logicup.backend.dto.ExecucaoResponse;
import br.com.logicup.backend.exception.MuitasTentativasException;
import br.com.logicup.backend.exception.RecursoNaoEncontradoException;
import br.com.logicup.backend.exception.RequisicaoInvalidaException;
import br.com.logicup.backend.execucao.ExecutorDeCodigo;
import br.com.logicup.backend.execucao.ProgramaJava;
import br.com.logicup.backend.execucao.ResultadoExecucao;
import br.com.logicup.backend.execucao.SituacaoExecucao;
import br.com.logicup.backend.model.Exercicio;
import br.com.logicup.backend.repository.ExercicioRepository;
import br.com.logicup.backend.security.LimitadorTentativas;
import br.com.logicup.backend.security.UsuarioLogado;
import br.com.logicup.backend.service.correcao.CorretorCodigo;
import br.com.logicup.backend.service.correcao.DicasDeErro;
import br.com.logicup.backend.service.correcao.MontadorPrograma.ProgramaMontado;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;

/**
 * Sprint 9 (ADR-001): botão "Executar" (execução livre, SEM XP e SEM correção).
 *
 * Regras:
 *  - limite por usuário (padrão: 20 execuções por minuto). Cada execução ocupa o servidor por
 *    alguns segundos; sem limite, um único usuário poderia ocupar a fila inteira;
 *  - não grava nada no banco (por isso não tem @Transactional);
 *  - se o executor estiver indisponível, a ExecutorIndisponivelException sobe até o
 *    GlobalExceptionHandler e vira HTTP 503.
 *
 * Dois usos:
 *  - laboratório: o aluno escreve um programa COMPLETO (executar);
 *  - dentro de uma questão de código: o trecho do aluno roda no programa de EXEMPLO da questão,
 *    com os exemplos públicos (executarExercicio). Os casos ocultos só rodam no "Enviar".
 */
@Service
public class ExecucaoService {

    private final ExecutorDeCodigo executor;
    private final LimitadorTentativas limite;
    private final ExercicioRepository exercicioRepository;
    private final CorretorCodigo corretorCodigo;

    public ExecucaoService(ExecutorDeCodigo executor, ExercicioRepository exercicioRepository,
                           CorretorCodigo corretorCodigo, Clock clock,
                           @Value("${logicup.limites.execucao.maximo:20}") int maximo,
                           @Value("${logicup.limites.execucao.janela:1m}") Duration janela) {
        this.executor = executor;
        this.exercicioRepository = exercicioRepository;
        this.corretorCodigo = corretorCodigo;
        // Reaproveita o limitador do login: aqui TODA execução conta (não só erros)
        this.limite = new LimitadorTentativas(maximo, janela, clock);
    }

    /** Laboratório: programa completo. */
    public ExecucaoResponse executar(ExecucaoRequest pedido, UsuarioLogado usuario) {
        contarExecucao(usuario);
        ResultadoExecucao resultado = executor.executar(new ProgramaJava(pedido.codigo(), pedido.entrada()));
        return new ExecucaoResponse(resultado.situacao(), mensagem(resultado.situacao()),
                resultado.saida(), resultado.erros(), resultado.tempoMs(), DicasDeErro.para(resultado.erros()));
    }

    /**
     * Botão "Executar" de uma questão de código (sem XP, sem correção, sem gravar nada).
     * Só exercícios no formato CODIGO ou BLOCOS (os blocos chegam aqui já como Java, gerado no navegador);
     * o gabarito nunca sai daqui (só a saída do programa de exemplo).
     */
    public ExecucaoResponse executarExercicio(Integer exercicioId, ExecucaoExercicioRequest pedido, UsuarioLogado usuario) {
        Exercicio exercicio = exercicioRepository.findById(exercicioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Exercício não encontrado."));
        if (!exercicio.getFormatoResposta().executaCodigo()) {
            throw new RequisicaoInvalidaException("Este exercício não é de escrever código.");
        }
        contarExecucao(usuario);
        ProgramaMontado programa = corretorCodigo.montarExemplo(exercicio, pedido.codigo()); // 400 se a estrutura estiver errada
        ResultadoExecucao resultado = executor.executar(ProgramaJava.semEntrada(programa.codigo()));
        return new ExecucaoResponse(resultado.situacao(), mensagem(resultado.situacao()), resultado.saida(),
                programa.traduzirLinhas(resultado.erros()), resultado.tempoMs(), DicasDeErro.para(resultado.erros()));
    }

    private void contarExecucao(UsuarioLogado usuario) {
        String chave = String.valueOf(usuario.id());
        // Sprint 10: confere e conta de uma vez (atômico): cliques simultâneos não escapam do limite
        limite.tentar(chave).ifPresent(espera -> {
            throw new MuitasTentativasException(
                    "Você executou muitos programas em pouco tempo. Aguarde alguns segundos e tente novamente.", espera);
        });
    }

    /** Uma frase por situação, pensada para quem está aprendendo. */
    static String mensagem(SituacaoExecucao situacao) {
        return switch (situacao) {
            case SUCESSO -> "Programa executado com sucesso.";
            case ERRO_COMPILACAO -> "O código tem um erro e não pôde ser compilado. Veja a linha indicada na mensagem.";
            case ERRO_EXECUCAO -> "O programa começou a rodar, mas parou por causa de um erro.";
            case TEMPO_ESGOTADO -> "O programa demorou demais e foi interrompido. Verifique se há um laço que nunca termina "
                    + "ou um método que chama a si mesmo sem parar.";
            case SAIDA_EXCEDIDA -> "O programa escreveu texto demais e foi interrompido. Verifique se há um laço que nunca termina.";
            case INTERROMPIDO -> "O programa foi interrompido por usar recursos demais (por exemplo, memória).";
        };
    }
}
