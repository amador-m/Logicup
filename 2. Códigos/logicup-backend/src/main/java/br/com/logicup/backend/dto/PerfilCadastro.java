package br.com.logicup.backend.dto;

import br.com.logicup.backend.model.TipoUsuario;

/**
 * Perfis que podem ser escolhidos no CADASTRO PÚBLICO (AU3).
 * ADMIN não existe aqui DE PROPÓSITO: se alguém enviar "perfil": "ADMIN" direto na API,
 * o Spring nem consegue converter o valor e responde 400. A proteção é pelo tipo, não por um "if".
 */
public enum PerfilCadastro {
    ALUNO, PROFESSOR;

    public TipoUsuario paraTipoUsuario() {
        return TipoUsuario.valueOf(this.name());
    }
}
