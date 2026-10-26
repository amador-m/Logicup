package br.com.logicup.backend.service;

import br.com.logicup.backend.dto.ConquistaResponse;
import br.com.logicup.backend.dto.ConquistasResponse;
import br.com.logicup.backend.dto.NivelConquistaResponse;
import br.com.logicup.backend.dto.NovaConquistaResponse;
import br.com.logicup.backend.exception.SessaoInvalidaException;
import br.com.logicup.backend.model.Aluno;
import br.com.logicup.backend.model.AlunoConquista;
import br.com.logicup.backend.model.Conquista;
import br.com.logicup.backend.model.Conquista.Meta;
import br.com.logicup.backend.model.NivelConquista;
import br.com.logicup.backend.model.TipoUsuario;
import br.com.logicup.backend.repository.AlunoConquistaRepository;
import br.com.logicup.backend.repository.AlunoRepository;
import br.com.logicup.backend.repository.AlunoTurmaRepository;
import br.com.logicup.backend.security.UsuarioLogado;
import br.com.logicup.backend.service.RegrasConquistas.Alcancada;
import br.com.logicup.backend.service.RegrasConquistas.MetricasAluno;
import br.com.logicup.backend.service.TrilhaService.SituacaoTrilha;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Sprint 9: conquistas e gemas.
 *
 * DECISÃO B1: as conquistas são AVALIADAS só dentro das operações que mudam o progresso
 * (envio de rodada, entrada em turma e login, que cobre quem já tinha progresso antes desta versão).
 * Os GET só leem: por regra do HTTP, GET é "seguro" e pode ser repetido sem efeito colateral.
 *
 * avaliar() é IDEMPOTENTE: pode rodar quantas vezes for, a mesma medalha nunca é gravada nem paga duas vezes
 * (a chave primária + INSERT IGNORE garantem isso no banco).
 */
@Service
public class ConquistaService {

    private final AlunoConquistaRepository conquistaRepository;
    private final AlunoRepository alunoRepository;
    private final AlunoTurmaRepository alunoTurmaRepository;
    private final TrilhaService trilhaService;
    private final Clock clock;

    public ConquistaService(AlunoConquistaRepository conquistaRepository, AlunoRepository alunoRepository,
                            AlunoTurmaRepository alunoTurmaRepository, TrilhaService trilhaService, Clock clock) {
        this.conquistaRepository = conquistaRepository;
        this.alunoRepository = alunoRepository;
        this.alunoTurmaRepository = alunoTurmaRepository;
        this.trilhaService = trilhaService;
        this.clock = clock;
    }

    /**
     * Grava as medalhas que o aluno alcançou e ainda não tinha, e credita as gemas de cada uma.
     * Usada no envio de rodada, que já calculou a situação da trilha (evita recalcular).
     * Precisa de uma transação aberta por quem chama (MANDATORY): ela faz parte da operação maior.
     *
     * @return quantas gemas o aluno ganhou agora (0 se nada novo)
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public int avaliar(Aluno aluno, SituacaoTrilha situacao) {
        MetricasAluno metricas = RegrasConquistas.metricas(situacao, aluno.getPontuacaoAcumulada(),
                aluno.getStreakAtual(), alunoTurmaRepository.existsByAlunoId(aluno.getId()));

        // 1 consulta: o que ele já tem. Só tenta gravar o que falta (na maioria dos envios, nada).
        Map<AlunoConquista.Chave, AlunoConquista> jaTem = conquistaRepository.findByIdUsuario(aluno.getId()).stream()
                .collect(Collectors.toMap(this::chaveDe, Function.identity()));

        LocalDateTime agora = LocalDateTime.now(clock);
        int gemasGanhas = 0;
        for (Alcancada alcancada : RegrasConquistas.alcancadas(metricas)) {
            NivelConquista nivel = alcancada.meta().nivel();
            if (jaTem.containsKey(new AlunoConquista.Chave(aluno.getId(), alcancada.conquista().name(), nivel))) {
                continue;
            }
            int inseridas = conquistaRepository.registrar(aluno.getId(), alcancada.conquista().name(), nivel.name(),
                    nivel.getGemas(), agora);
            if (inseridas == 1) { // 0 = outra requisição gravou essa mesma medalha um instante antes: não paga de novo
                gemasGanhas += nivel.getGemas();
            }
        }
        if (gemasGanhas > 0) {
            alunoRepository.creditarGemas(aluno.getId(), gemasGanhas);
        }
        return gemasGanhas;
    }

    /** Versão para quem ainda não tem a situação da trilha calculada (entrada em turma, login). */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public int avaliar(Aluno aluno) {
        return avaliar(aluno, trilhaService.calcularSituacao(new UsuarioLogado(aluno.getId(), TipoUsuario.ALUNO)));
    }

    /** GET /api/conquistas: o catálogo inteiro com o progresso do aluno. Só lê. */
    @Transactional(readOnly = true)
    public ConquistasResponse listar(Integer alunoId) {
        Aluno aluno = alunoRepository.findById(alunoId).orElseThrow(SessaoInvalidaException::new);
        SituacaoTrilha situacao = trilhaService.calcularSituacao(new UsuarioLogado(alunoId, TipoUsuario.ALUNO));
        MetricasAluno metricas = RegrasConquistas.metricas(situacao, aluno.getPontuacaoAcumulada(),
                aluno.getStreakAtual(), alunoTurmaRepository.existsByAlunoId(alunoId));

        Map<AlunoConquista.Chave, AlunoConquista> ganhas = conquistaRepository.findByIdUsuario(alunoId).stream()
                .collect(Collectors.toMap(this::chaveDe, Function.identity()));

        List<ConquistaResponse> conquistas = java.util.Arrays.stream(Conquista.values())
                .map(conquista -> new ConquistaResponse(conquista.name(), conquista.getTitulo(),
                        metricas.valorDe(conquista.getMetrica()),
                        conquista.getMetas().stream()
                                .map(meta -> nivel(conquista, meta,
                                        ganhas.get(new AlunoConquista.Chave(alunoId, conquista.name(), meta.nivel()))))
                                .toList()))
                .toList();
        return new ConquistasResponse(saldo(alunoId), conquistas);
    }

    /** GET /api/conquistas/novas: as que o aluno ainda não viu (M3). Só lê. */
    @Transactional(readOnly = true)
    public List<NovaConquistaResponse> novas(Integer alunoId) {
        return conquistaRepository.findByIdUsuarioAndVistaEmIsNullOrderByDesbloqueadaEmAscCodigoConquistaAsc(alunoId)
                .stream()
                // código que saiu do catálogo: não há o que comemorar (e a tela não saberia desenhar)
                .flatMap(ganha -> Conquista.porCodigo(ganha.getCodigoConquista()).stream()
                        .map(conquista -> nova(conquista, ganha)))
                .toList();
    }

    /** POST /api/conquistas/vistas: a comemoração foi mostrada. */
    @Transactional
    public void marcarVistas(Integer alunoId) {
        conquistaRepository.marcarVistas(alunoId, LocalDateTime.now(clock));
    }

    // ---------------------------------------------------------------- apoio

    private int saldo(Integer alunoId) {
        Integer saldo = alunoRepository.saldoDeGemas(alunoId);
        return saldo != null ? saldo : 0;
    }

    private AlunoConquista.Chave chaveDe(AlunoConquista ganha) {
        return new AlunoConquista.Chave(ganha.getIdUsuario(), ganha.getCodigoConquista(), ganha.getNivel());
    }

    private static NivelConquistaResponse nivel(Conquista conquista, Meta meta, AlunoConquista ganha) {
        return new NivelConquistaResponse(meta.nivel(), meta.valor(), conquista.descricaoDe(meta),
                meta.nivel().getGemas(), ganha != null ? ganha.getDesbloqueadaEm() : null);
    }

    private static NovaConquistaResponse nova(Conquista conquista, AlunoConquista ganha) {
        Meta meta = conquista.getMetas().stream()
                .filter(m -> m.nivel() == ganha.getNivel())
                .findFirst()
                .orElse(new Meta(ganha.getNivel(), 0));
        return new NovaConquistaResponse(conquista.name(), conquista.getTitulo(), ganha.getNivel(),
                conquista.descricaoDe(meta), ganha.getGemasRecebidas(), ganha.getDesbloqueadaEm());
    }
}
