package br.com.logicup.backend.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

// classe criada pois a JPA exige que chaves compostas/contatenadas sejam combinadas em uma classe própria
@Embeddable // não é uma classe própria, mas sim um pedaço que vai ficar embutido em outra entidade
public class AlunoTurmaId implements Serializable {

    private Integer idUsuario;
    private Integer idTurma;

    public AlunoTurmaId() {}

    public AlunoTurmaId(Integer idUsuario, Integer idTurma) {
        this.idUsuario = idUsuario;
        this.idTurma = idTurma;
    }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public Integer getIdTurma() { return idTurma; }
    public void setIdTurma(Integer idTurma) { this.idTurma = idTurma; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AlunoTurmaId)) return false;
        AlunoTurmaId that = (AlunoTurmaId) o;
        return Objects.equals(idUsuario, that.idUsuario) && Objects.equals(idTurma, that.idTurma);
    }
    // checar se a linha buscada é igual a que estou tentando salvar
    @Override
    public int hashCode() { // retorna um número inteiro representando um objeto
        return Objects.hash(idUsuario, idTurma);
    }
}