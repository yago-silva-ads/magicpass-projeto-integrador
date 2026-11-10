package br.senac.pi.magicpass.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "parques")
public class Parque extends EntidadeBase {

    @NotBlank(message = "Nome do parque e obrigatorio")
    @Size(max = 100)
    @Column(unique = true)
    private String nome;

    @NotBlank(message = "Local e obrigatorio")
    private String local;

    @Size(max = 500)
    private String descricao;

    @Size(max = 500)
    private String imagemUrl;

    private boolean ativo = true;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getLocal() { return local; }
    public void setLocal(String local) { this.local = local; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getImagemUrl() { return imagemUrl; }
    public void setImagemUrl(String imagemUrl) { this.imagemUrl = imagemUrl; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
