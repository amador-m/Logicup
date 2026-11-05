package br.com.logicup.backend.security;

import br.com.logicup.backend.exception.MuitasTentativasException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;

/**
 * Sprint 9: as regras de limite de tentativas do LogiCup, num lugar só (decisões D1, D2 e D4).
 *
 *   LOGIN (D1-A), duas regras ao mesmo tempo:
 *     - conta + IP: 5 erros em 15 min -> aquela conta, naquele IP, espera o fim da janela;
 *       (só pela conta, qualquer um conseguiria TRANCAR a conta de outra pessoa errando 5 vezes)
 *     - só IP:     150 erros em 15 min -> o IP espera (pega quem testa MUITAS contas diferentes).
 *     Só ERROS contam. Login certo zera a regra "conta + IP", mas NÃO a do IP: senão um atacante com
 *     uma conta válida "limparia" o próprio IP no meio do ataque.
 *     Durante o bloqueio, até a senha CERTA recebe 429 — se não, o atacante descobriria a senha
 *     justamente por receber uma resposta diferente.
 *   CÓDIGO DE TURMA (D2): 10 códigos inexistentes em 15 min, por ALUNO (só aluno logado entra em turma).
 *   CADASTRO (D4): 60 tentativas de cadastro por hora, por IP.
 *   Sprint 10: os limites POR IP subiram (30->150, 10->60, 10->60) porque uma turma inteira num laboratório
 *   sai pelo mesmo IP público da escola. Os limites POR CONTA continuam rígidos (5 erros, 3 e-mails).
 *   E-MAILS DE CONTA (Sprint 10): "reenviar confirmação" e "esqueci minha senha", cada um com
 *     - 3 pedidos por hora para o MESMO e-mail (ninguém consegue encher a caixa de outra pessoa);
 *     - 60 pedidos por hora por IP (um robô não consegue disparar e-mails para uma lista de endereços).
 *     Conta TODO pedido, exista ou não a conta: assim o limite também não revela quem está cadastrado.
 *
 * Os números ficam no application.properties (logicup.limites.*) para poderem ser ajustados sem mexer
 * no código. As mensagens são as MESMAS exista ou não a conta (não revelam quem está cadastrado).
 */
@Component
public class ProtecaoForcaBruta {

    private final LimitadorTentativas loginContaIp;
    private final LimitadorTentativas loginIp;
    private final LimitadorTentativas codigoTurma;
    private final LimitadorTentativas cadastro;
    private final LimitadorTentativas emailPorEndereco;
    private final LimitadorTentativas emailPorIp;

    public ProtecaoForcaBruta(
            Clock clock,
            @Value("${logicup.limites.login-conta.maximo:5}") int loginContaMaximo,
            @Value("${logicup.limites.login-conta.janela:15m}") Duration loginContaJanela,
            @Value("${logicup.limites.login-ip.maximo:150}") int loginIpMaximo,
            @Value("${logicup.limites.login-ip.janela:15m}") Duration loginIpJanela,
            @Value("${logicup.limites.codigo-turma.maximo:10}") int turmaMaximo,
            @Value("${logicup.limites.codigo-turma.janela:15m}") Duration turmaJanela,
            @Value("${logicup.limites.cadastro.maximo:60}") int cadastroMaximo,
            @Value("${logicup.limites.cadastro.janela:1h}") Duration cadastroJanela,
            @Value("${logicup.limites.email-endereco.maximo:3}") int emailEnderecoMaximo,
            @Value("${logicup.limites.email-endereco.janela:1h}") Duration emailEnderecoJanela,
            @Value("${logicup.limites.email-ip.maximo:60}") int emailIpMaximo,
            @Value("${logicup.limites.email-ip.janela:1h}") Duration emailIpJanela) {
        this.loginContaIp = new LimitadorTentativas(loginContaMaximo, loginContaJanela, clock);
        this.loginIp = new LimitadorTentativas(loginIpMaximo, loginIpJanela, clock);
        this.codigoTurma = new LimitadorTentativas(turmaMaximo, turmaJanela, clock);
        this.cadastro = new LimitadorTentativas(cadastroMaximo, cadastroJanela, clock);
        this.emailPorEndereco = new LimitadorTentativas(emailEnderecoMaximo, emailEnderecoJanela, clock);
        this.emailPorIp = new LimitadorTentativas(emailIpMaximo, emailIpJanela, clock);
    }

    // ---------------- LOGIN ----------------

    /**
     * Chamar ANTES de conferir a senha (requisição bloqueada nem gasta tempo com o BCrypt).
     *
     * Sprint 10: a tentativa já é CONTADA aqui, de forma atômica (LimitadorTentativas.tentar), e não só depois
     * do BCrypt. Antes, tentativas simultâneas escapavam do limite (condição de corrida). Se a senha estiver
     * certa, registrarLoginCerto() devolve a contagem: o resultado final é o mesmo de "só erros contam".
     */
    public void reservarTentativaDeLogin(String loginNormalizado, String ip) {
        String chaveConta = chave(loginNormalizado, ip);
        Duration esperaConta = loginContaIp.tentar(chaveConta).orElse(null);
        Duration esperaIp = loginIp.tentar(ip).orElse(null);
        if (esperaConta != null || esperaIp != null) {
            // uma das duas regras bloqueou: a outra não pode ficar com uma tentativa que nem aconteceu
            if (esperaConta == null) loginContaIp.estornar(chaveConta);
            if (esperaIp == null) loginIp.estornar(ip);
            Duration espera = maior(esperaConta, esperaIp);
            throw new MuitasTentativasException(
                    "Muitas tentativas de login. Por segurança, aguarde " + minutos(espera) + " e tente novamente.",
                    espera);
        }
    }

    /**
     * Senha certa: zera a regra "conta + IP" e devolve a tentativa contada na regra do IP (que só conta ERROS).
     * A regra do IP NÃO é zerada: senão um atacante com uma conta válida "limparia" o próprio IP no meio do ataque.
     */
    public void registrarLoginCerto(String loginNormalizado, String ip) {
        loginContaIp.limpar(chave(loginNormalizado, ip));
        loginIp.estornar(ip);
    }

    // ---------------- CÓDIGO DE TURMA ----------------

    /** Sprint 10: confere e conta de uma vez (atômico, ver reservarTentativaDeLogin). */
    public void reservarTentativaDeCodigoTurma(Integer alunoId) {
        codigoTurma.tentar(String.valueOf(alunoId)).ifPresent(espera -> {
            throw new MuitasTentativasException(
                    "Muitos códigos de turma incorretos. Confira o código com o professor e tente novamente em "
                            + minutos(espera) + ".", espera);
        });
    }

    /** O código existia: a tentativa não conta (só código INEXISTENTE conta, decisão D2). */
    public void registrarCodigoTurmaEncontrado(Integer alunoId) {
        codigoTurma.estornar(String.valueOf(alunoId));
    }

    // ---------------- CADASTRO ----------------

    /**
     * Conta TODA tentativa de cadastro que chega ao Service (com sucesso ou não), por IP.
     * Contar também as que falham (ex.: e-mail já cadastrado) limita robôs que usam o cadastro para
     * descobrir quais e-mails existem. Dados inválidos (400) nem chegam aqui: são barrados pelo @Valid.
     */
    public void verificarERegistrarCadastro(String ip) {
        cadastro.tentar(ip).ifPresent(espera -> { // Sprint 10: atômico (confere e conta de uma vez)
            throw new MuitasTentativasException(
                    "Muitas tentativas de cadastro a partir desta rede. Tente novamente em " + minutos(espera) + ".",
                    espera);
        });
    }

    // ---------------- E-MAILS DE CONTA (Sprint 10) ----------------

    /**
     * @param tipo "confirmacao" ou "senha": cada finalidade tem a sua contagem (pedir 3 reenvios de
     *             confirmação não impede de pedir uma senha nova)
     * @param emailNormalizado já em minúsculas e sem espaços (AuthService.normalizarEmail)
     */
    public void verificarERegistrarPedidoDeEmail(String tipo, String emailNormalizado, String ip) {
        String chaveEmail = tipo + "|" + emailNormalizado;
        String chaveIp = tipo + "|" + ip;
        Duration esperaEmail = emailPorEndereco.tentar(chaveEmail).orElse(null); // Sprint 10: atômico
        Duration esperaIp = emailPorIp.tentar(chaveIp).orElse(null);
        if (esperaEmail != null || esperaIp != null) {
            if (esperaEmail == null) emailPorEndereco.estornar(chaveEmail);
            if (esperaIp == null) emailPorIp.estornar(chaveIp);
            Duration espera = maior(esperaEmail, esperaIp);
            throw new MuitasTentativasException(
                    "Você já pediu vários e-mails em pouco tempo. Confira sua caixa de entrada e o spam, "
                            + "ou tente novamente em " + minutos(espera) + ".", espera);
        }
    }

    // ---------------- utilidades ----------------

    private static String chave(String login, String ip) {
        return login + "|" + ip;
    }

    private static Duration maior(Duration a, Duration b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.compareTo(b) >= 0 ? a : b;
    }

    /** 61 s -> "2 minutos"; 30 s -> "1 minuto" (arredonda para cima: nunca promete menos do que falta). */
    static String minutos(Duration espera) {
        long min = Math.max(1, (espera.toSeconds() + 59) / 60);
        return min == 1 ? "1 minuto" : min + " minutos";
    }
}
