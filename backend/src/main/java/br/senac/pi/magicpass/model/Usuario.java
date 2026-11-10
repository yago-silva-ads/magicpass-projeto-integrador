package br.senac.pi.magicpass.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/** Usuario do sistema. So o ADMIN cadastra usuarios (tela propria). */
@Entity
@Table(name = "usuarios")
public class Usuario extends EntidadeBase {

    @NotBlank(message = "Nome e obrigatorio")
    @Size(max = 100)
    private String nome;

    @NotBlank(message = "Login e obrigatorio")
    @Pattern(regexp = "^[a-zA-Z0-9._-]{3,30}$", message = "Login deve ter 3 a 30 letras, numeros, ponto, _ ou -")
    @Column(unique = true, nullable = false)
    private String login;

    /** Recebida em texto puro, gravada como hash BCrypt. Nunca volta no JSON. */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String senha;

    @NotNull(message = "Perfil e obrigatorio")
    @Enumerated(EnumType.STRING)
    private Perfil perfil = Perfil.VENDEDOR;

    private boolean ativo = true;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login == null ? null : login.trim().toLowerCase(); }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
