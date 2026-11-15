package br.com.logicup.backend.service;

import br.com.logicup.backend.email.EmailDeContaSolicitado;
import br.com.logicup.backend.email.TipoEmailConta;
import br.com.logicup.backend.exception.RequisicaoInvalidaException;
import br.com.logicup.backend.exception.SenhaAtualIncorretaException;
import br.com.logicup.backend.exception.SessaoInvalidaException;
import br.com.logicup.backend.model.FinalidadeToken;
import br.com.logicup.backend.model.TipoUsuario;
import br.com.logicup.backend.model.Usuario;
import br.com.logicup.backend.repository.TurmaRepository;
import br.com.logicup.backend.repository.UsuarioRepository;
import br.com.logicup.backend.security.ProtecaoForcaBruta;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sprint 10 (Etapa 4): a pessoa EXCLUI A PRÓPRIA CONTA (LGPD, art. 18: direito de eliminação dos dados).
 *
 * Decisões da equipe:
 *  - apagar de verdade (não anonimizar): um DELETE na tabela usuario. O resto (aluno, progresso, conquistas,
 *    avatares, matrículas, aceites dos Termos e códigos de e-mail) é apagado PELO BANCO, porque todas
 *    essas tabelas têm "ON DELETE CASCADE" na chave estrangeira. O Java não precisa listar tabela por tabela;
 *  - quem tem senha confirma digitando a senha; quem entra só pelo Google (sem senha) confirma por um
 *    link de uso único enviado por e-mail (30 minutos) — o e-mail é a prova de que é a dona da conta;
 *  - professor com turmas NÃO pode excluir (a turma ficaria sem professor: a FK de turma é ON DELETE SET NULL);
 *  - administrador NÃO pode excluir pelo perfil (evita ficar sem nenhum admin por um clique);
 *  - depois de excluir, sai um e-mail avisando (se não foi a pessoa, ela fica sabendo).
 *
 * O id SEMPRE vem do token (Controller), nunca do JSON: ninguém consegue excluir a conta de outra pessoa.
 */
@Service
public class ExclusaoContaService {

    private final UsuarioRepository usuarioRepository;
    private final TurmaRepository turmaRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenUsuarioService tokens;
    private final ProtecaoForcaBruta protecao;
    private final ApplicationEventPublisher eventos;

    public ExclusaoContaService(UsuarioRepository usuarioRepository, TurmaRepository turmaRepository,
                                PasswordEncoder passwordEncoder, TokenUsuarioService tokens,
                                ProtecaoForcaBruta protecao, ApplicationEventPublisher eventos) {
        this.usuarioRepository = usuarioRepository;
        this.turmaRepository = turmaRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
        this.protecao = protecao;
        this.eventos = eventos;
    }

    /** Quem tem senha: confere a senha e exclui na hora. */
    @Transactional
    public void excluirComSenha(Integer idUsuario, String senhaAtual) {
        Usuario usuario = buscar(idUsuario);
        verificarSePodeExcluir(usuario);
        if (!usuario.temSenha()) {
            throw new RequisicaoInvalidaException("Sua conta entra pelo Google e não tem senha. "
                    + "Peça o link de exclusão: ele chega no seu e-mail.");
        }
        if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
            throw new SenhaAtualIncorretaException(); // 400 em campos.senhaAtual (a pessoa continua logada)
        }
        excluir(usuario);
    }

    /**
     * Quem entra só pelo Google: manda o link de exclusão para o e-mail da conta.
     * Mesmo limite dos outros e-mails (3 por hora por endereço): ninguém enche a caixa de alguém com pedidos.
     */
    @Transactional
    public void pedirLinkDeExclusao(Integer idUsuario, String ip) {
        Usuario usuario = buscar(idUsuario);
        verificarSePodeExcluir(usuario);
        if (usuario.temSenha()) {
            throw new RequisicaoInvalidaException("Sua conta tem senha: confirme a exclusão digitando a sua senha.");
        }
        protecao.verificarERegistrarPedidoDeEmail("exclusao", usuario.getEmail(), ip);
        String codigo = tokens.gerar(usuario, FinalidadeToken.EXCLUIR_CONTA);
        eventos.publishEvent(new EmailDeContaSolicitado(TipoEmailConta.EXCLUIR_CONTA, usuario.getEmail(),
                usuario.getNome(), codigo));
    }

    /**
     * Abre o link do e-mail e exclui. Não exige estar logado: o código de uso único, que só chegou na
     * caixa de e-mail da pessoa, já identifica a conta (é a mesma ideia do link de "nova senha").
     * As regras (admin, professor com turmas) são conferidas de novo: algo pode ter mudado desde o pedido.
     */
    @Transactional
    public void excluirPorLink(String codigo) {
        Usuario usuario = tokens.consumir(codigo, FinalidadeToken.EXCLUIR_CONTA); // ruim -> 400 LINK_INVALIDO
        verificarSePodeExcluir(usuario);
        excluir(usuario);
    }

    private void verificarSePodeExcluir(Usuario usuario) {
        if (usuario.getTipoUsuario() == TipoUsuario.ADMIN) {
            throw new RequisicaoInvalidaException("Contas de administrador não podem ser excluídas pelo perfil.");
        }
        if (usuario.getTipoUsuario() == TipoUsuario.PROFESSOR) {
            long turmas = turmaRepository.countByProfessorId(usuario.getId());
            if (turmas > 0) {
                throw new RequisicaoInvalidaException("Você ainda tem " + turmas
                        + (turmas == 1 ? " turma" : " turmas") + ". Exclua as suas turmas antes de excluir a conta, "
                        + "para que nenhum aluno fique em uma turma sem professor.");
            }
        }
    }

    private void excluir(Usuario usuario) {
        // guarda antes de apagar: depois do delete, o objeto não representa mais nada no banco
        String email = usuario.getEmail();
        String nome = usuario.getNome();

        /*
         * Um DELETE só, na tabela usuario; o banco apaga o resto pelo ON DELETE CASCADE (ver o repositório).
         * O DELETE vai para o banco AGORA, dentro da transação: se algo impedir a exclusão (uma chave
         * estrangeira sem CASCADE criada no futuro, por exemplo), o erro aparece aqui e tudo é desfeito
         * (rollback), antes de o aviso por e-mail ser anunciado.
         */
        usuarioRepository.apagarComTudoQueEhDele(usuario.getId());

        // o aviso só sai DEPOIS do commit (EnvioDeEmailListener): se der rollback, nenhum e-mail é enviado
        eventos.publishEvent(new EmailDeContaSolicitado(TipoEmailConta.CONTA_EXCLUIDA, email, nome, null));
    }

    private Usuario buscar(Integer idUsuario) {
        // token válido de uma conta que não existe mais (já excluída em outra aba, por exemplo) -> 401
        return usuarioRepository.findById(idUsuario).orElseThrow(SessaoInvalidaException::new);
    }
}
