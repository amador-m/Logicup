package br.com.logicup.backend.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Sprint 10: controla a COTA DIÁRIA de e-mails (o plano grátis da Brevo envia no máximo 300 por dia).
 *
 * Três números, todos no application.properties (logicup.email.cota.*):
 *  - limite (300): a partir daqui, nenhum e-mail sai. A Brevo recusaria de qualquer jeito, e assim
 *    o LogiCup não fica tentando à toa;
 *  - reserva (270): a partir daqui, só saem os e-mails ESSENCIAIS (confirmar o e-mail e criar senha nova).
 *    As boas-vindas, que são "bônus", param primeiro. Assim, num dia de pico, quem está se cadastrando
 *    ainda consegue confirmar a conta;
 *  - alerta (240 = 80%): ao passar daqui, o LogiCup avisa a equipe UMA vez no dia (log + e-mail para o contato).
 *
 * O dia vira à meia-noite de São Paulo (o mesmo Clock do resto do sistema).
 *
 * Limitação consciente: a contagem fica na MEMÓRIA. Se o backend reiniciar (uma atualização, por exemplo),
 * ela volta a zero naquele dia. Para um servidor só e poucas reinicializações, é aceitável. Guardar no banco
 * resolveria, mas exigiria uma tabela nova só para isso. A própria Brevo é a última barreira.
 *
 * synchronized: os e-mails saem por 2 threads ao mesmo tempo (EmailConfig). Sem isso, duas threads
 * poderiam ler "239" juntas e as duas mandariam o alerta (condição de corrida).
 */
@Component
public class CotaDiariaDeEmails {

    private final Clock clock;
    private final int limite;
    private final int reserva;
    private final int alerta;

    private LocalDate dia;
    private int enviadosHoje;
    private boolean alertaEnviadoHoje;

    public CotaDiariaDeEmails(Clock clock,
                              @Value("${logicup.email.cota.limite:300}") int limite,
                              @Value("${logicup.email.cota.reserva:270}") int reserva,
                              @Value("${logicup.email.cota.alerta:240}") int alerta) {
        if (!(alerta <= reserva && reserva <= limite)) {
            throw new IllegalStateException("logicup.email.cota: precisa alerta <= reserva <= limite");
        }
        this.clock = clock;
        this.limite = limite;
        this.reserva = reserva;
        this.alerta = alerta;
        this.dia = LocalDate.now(clock);
    }

    /** Ainda cabe este tipo de e-mail hoje? */
    public synchronized boolean podeEnviar(TipoEmailConta tipo) {
        virarODiaSePreciso();
        int teto = (tipo == TipoEmailConta.BOAS_VINDAS) ? reserva : limite;
        return enviadosHoje < teto;
    }

    /**
     * Conta um e-mail que acabou de sair.
     * @return true UMA única vez por dia: quando a contagem chega ao número do alerta
     */
    public synchronized boolean registrarEnvio() {
        virarODiaSePreciso();
        enviadosHoje++;
        if (enviadosHoje >= alerta && !alertaEnviadoHoje) {
            alertaEnviadoHoje = true;
            return true;
        }
        return false;
    }

    public synchronized int enviadosHoje() {
        virarODiaSePreciso();
        return enviadosHoje;
    }

    public int limite() {
        return limite;
    }

    private void virarODiaSePreciso() {
        LocalDate hoje = LocalDate.now(clock);
        if (!hoje.equals(dia)) {
            dia = hoje;
            enviadosHoje = 0;
            alertaEnviadoHoje = false;
        }
    }
}
