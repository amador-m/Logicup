/**
 * NOTA: Professor não possui entidade/tabela própria. Um usuário é professor
 * quando tipoUsuario == TipoUsuario.PROFESSOR. A tabela "professor" existia
 * no MER original mas foi removida na revisão do banco (sem colunas próprias)
 */

package br.com.logicup.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity // tabela
@Table(name = "usuario")
@Inheritance(strategy = InheritanceType.JOINED) // permite a criação de subclasses cujas tabelas estão ligadas por FK
public class Usuario {

    @Id // PK
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // auto increment do bd gera esse número
    @Column(name = "id_usuario") // id do java == id_usuario do bd
    private Integer id;

    @Column(nullable = false, length = 150) // not null e varchar(150) :) (redundante com o banco pro hibernate barrar antes mesmo de chegar no mysql)
    private String nome;

    // Sprint 7: nome público e único (ranking, perfil e login). Regra: 3 a 20 caracteres, a-z 0-9 . _
    // A UNICIDADE é garantida pela UNIQUE do banco (uk_usuario_username); o Service só checa antes
    // para dar uma mensagem amigável.
    @Column(nullable = false, unique = true, length = 20)
    private String username;

    @Column(name = "e_mail", nullable = false, unique = true, length = 150)
    private String email;

    // Sprint 10: pode ser NULL = conta criada só pelo Google (sem senha própria). O login por senha
    // recusa essas contas; a pessoa pode criar uma senha pelo "Esqueci minha senha".
    @Column
    private String senha;

    @Enumerated(EnumType.STRING) // pra salvar o enum tipo user como string e não um número/posição na declaração (0, 1, 2)
    @Column(name = "tipo_usuario", nullable = false)
    private TipoUsuario tipoUsuario;

    @Column(name = "data_cadastro", insertable = false, updatable = false) // o banco preenche na hora do insert automaticamente, não o Java
    private LocalDateTime dataCadastro;

    // Sprint 9: código do avatar em uso (enum Avatar). String no banco (VARCHAR) para que avatares novos
    // não exijam migração. Só muda pelo trocarAvatar(); quem confere se o usuário POSSUI o avatar é o AvatarService.
    @Column(nullable = false, length = 30)
    private String avatar = Avatar.PADRAO.name();

    // Sprint 10: quando a pessoa clicou no link de confirmação. NULL = ainda não confirmou (não entra).
    // Só muda pelo confirmarEmail(): não há setter, para ninguém "confirmar" sem passar pela regra.
    @Column(name = "email_confirmado_em")
    private LocalDateTime emailConfirmadoEm;

    // Sprint 10: identificador fixo da pessoa no Google ("sub"), para o "Entrar com Google".
    @Column(name = "google_id", unique = true)
    private String googleId;

    public Usuario() {} // exigência do hibernate ter um construtor sem argumentos pra instanciar internamente

    public Usuario(String nome, String username, String email, String senha, TipoUsuario tipoUsuario) {
        this.nome = nome;
        this.username = username;
        this.email = email;
        this.senha = senha;
        this.tipoUsuario = tipoUsuario;
    }

    // getters e setters para ler e escrever
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String eMail) { this.email = eMail; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public TipoUsuario getTipoUsuario() { return tipoUsuario; }
    public void setTipoUsuario(TipoUsuario tipoUsuario) { this.tipoUsuario = tipoUsuario; }

    public LocalDateTime getDataCadastro() { return dataCadastro; } // sem set pois ninguém deve sobrescrever aqui manualmente

    /** O avatar em uso. Um código que não existe mais no catálogo vira o padrão (nunca quebra a tela). */
    public Avatar getAvatar() { return Avatar.porCodigo(avatar).orElse(Avatar.PADRAO); }

    // ---------------- Sprint 10: confirmação de e-mail e senha ----------------

    public boolean isEmailConfirmado() { return emailConfirmadoEm != null; }

    public LocalDateTime getEmailConfirmadoEm() { return emailConfirmadoEm; }

    /** Marca o e-mail como confirmado. Confirmar de novo não muda a data original (idempotente). */
    public void confirmarEmail(LocalDateTime quando) {
        if (emailConfirmadoEm == null) {
            emailConfirmadoEm = java.util.Objects.requireNonNull(quando);
        }
    }

    /** true quando a conta tem senha própria (contas criadas só pelo Google não têm). */
    public boolean temSenha() { return senha != null; }

    public String getGoogleId() { return googleId; }

    public void trocarAvatar(Avatar novo) {
        this.avatar = java.util.Objects.requireNonNull(novo, "O avatar é obrigatório").name();
    }
}

