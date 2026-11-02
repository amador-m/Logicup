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
 *     - só IP:      30 erros em 15 min -> o IP espera (pega quem testa MUITAS contas diferentes).
 *     Só ERROS contam. Login certo zera a regra "conta + IP", mas NÃO a do IP: senão um atacante com
 *     uma conta válida "limparia" o próprio IP no meio do ataque.
 *     Durante o bloqueio, até a senha CERTA recebe 429 — se não, o atacante descobriria a senha
 *     justamente por receber uma resposta diferente.
 *   CÓDIGO DE TURMA (D2): 10 códigos inexistentes em 15 min, por ALUNO (só aluno logado entra em turma).
 *   CADASTRO (D4): 10 tentativas de cadastro por hora, por IP.
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

    public ProtecaoForcaBruta(
            Clock clock,
            @Value("${logicup.limites.login-conta.maximo:5}") int loginContaMaximo,
            @Value("${logicup.limites.login-conta.janela:15m}") Duration loginContaJanela,
            @Value("${logicup.limites.login-ip.maximo:30}") int loginIpMaximo,
            @Value("${logicup.limites.login-ip.janela:15m}") Duration loginIpJanela,
            @Value("${logicup.limites.codigo-turma.maximo:10}") int turmaMaximo,
            @Value("${logicup.limites.codigo-turma.janela:15m}") Duration turmaJanela,
            @Value("${logicup.limites.cadastro.maximo:10}") int cadastroMaximo,
            @Value("${logicup.limites.cadastro.janela:1h}") Duration cadastroJanela) {
        this.loginContaIp = new LimitadorTentativas(loginContaMaximo, loginContaJanela, clock);
        this.loginIp = new LimitadorTentativas(loginIpMaximo, loginIpJanela, clock);
        this.codigoTurma = new LimitadorTentativas(turmaMaximo, turmaJanela, clock);
        this.cadastro = new LimitadorTentativas(cadastroMaximo, cadastroJanela, clock);
    }

    // ---------------- LOGIN ----------------

    /** Chamar ANTES de conferir a senha (requisição bloqueada nem gasta tempo com o BCrypt). */
    public void verificarLogin(String loginNormalizado, String ip) {
        Duration espera = maior(loginContaIp.bloqueioRestante(chave(loginNormalizado, ip)).orElse(null),
                loginIp.bloqueioRestante(ip).orElse(null));
        if (espera != null) {
            throw new MuitasTentativasException(
                    "Muitas tentativas de login. Por segurança, aguarde " + minutos(espera) + " e tente novamente.",
                    espera);
        }
    }

    public void registrarErroDeLogin(String loginNormalizado, String ip) {
        loginContaIp.registrarErro(chave(loginNormalizado, ip));
        loginIp.registrarErro(ip);
    }

    public void registrarLoginCerto(String loginNormalizado, String ip) {
        loginContaIp.limpar(chave(loginNormalizado, ip)); // a regra do IP continua valendo (ver comentário da classe)
    }

    // ---------------- CÓDIGO DE TURMA ----------------

    public void verificarCodigoTurma(Integer alunoId) {
        codigoTurma.bloqueioRestante(String.valueOf(alunoId)).ifPresent(espera -> {
            throw new MuitasTentativasException(
                    "Muitos códigos de turma incorretos. Confira o código com o professor e tente novamente em "
                            + minutos(espera) + ".", espera);
        });
    }

    public void registrarCodigoTurmaInexistente(Integer alunoId) {
        codigoTurma.registrarErro(String.valueOf(alunoId));
    }

    // ---------------- CADASTRO ----------------

    /**
     * Conta TODA tentativa de cadastro que chega ao Service (com sucesso ou não), por IP.
     * Contar também as que falham (ex.: e-mail já cadastrado) limita robôs que usam o cadastro para
     * descobrir quais e-mails existem. Dados inválidos (400) nem chegam aqui: são barrados pelo @Valid.
     */
    public void verificarERegistrarCadastro(String ip) {
        cadastro.bloqueioRestante(ip).ifPresent(espera -> {
            throw new MuitasTentativasException(
                    "Muitas tentativas de cadastro a partir desta rede. Tente novamente em " + minutos(espera) + ".",
                    espera);
        });
        cadastro.registrarErro(ip);
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
