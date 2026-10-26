package br.com.logicup.backend.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Sprint 9 (R3): gera o código de convite da turma.
 *
 *  - 8 caracteres de um alfabeto de 31, SEM os que se confundem ao ditar ou copiar (0/O, 1/I/L):
 *    31^8 ≈ 852 bilhões de combinações, o que torna adivinhar um código impraticável;
 *  - SecureRandom (e não Random): o código funciona como uma "senha" de entrada na turma,
 *    então precisa ser imprevisível. O Random comum é previsível para quem vê alguns resultados.
 * A unicidade é conferida pelo TurmaService (e garantida pelo UNIQUE do banco).
 */
@Component
public class GeradorCodigoConvite {

    static final String ALFABETO = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    static final int TAMANHO = 8;

    private final SecureRandom aleatorio = new SecureRandom();

    public String gerar() {
        StringBuilder codigo = new StringBuilder(TAMANHO);
        for (int i = 0; i < TAMANHO; i++) {
            codigo.append(ALFABETO.charAt(aleatorio.nextInt(ALFABETO.length())));
        }
        return codigo.toString();
    }
}
