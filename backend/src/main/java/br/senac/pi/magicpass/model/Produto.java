package br.senac.pi.magicpass.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/** Ingresso de parque ou pacote de viagem (ingresso + hotel + aereo). */
@Entity
@Table(name = "produtos")
public class Produto extends EntidadeBase {

    @NotBlank(message = "Nome e obrigatorio")
    @Size(max = 120)
    private String nome;

    @NotNull(message = "Categoria e obrigatoria")
    @Enumerated(EnumType.STRING)
    private CategoriaProduto categoria;

    @NotNull(message = "Parque e obrigatorio")
    @ManyToOne(optional = false)
    private Parque parque;

    /** Opcional: parceiro que opera o pacote (hotel/aereo). */
    @ManyToOne
    private Parceiro parceiro;

    @Size(max = 600)
    private String descricao;

    @NotNull(message = "Preco e obrigatorio")
    @DecimalMin(value = "0.01", message = "Preco deve ser maior que zero")
    @Digits(integer = 8, fraction = 2, message = "Preco com no maximo 2 casas decimais")
    private BigDecimal preco;

    @NotNull(message = "Quantidade de dias e obrigatoria")
    @Min(value = 1, message = "Dias deve ser no minimo 1")
    @Max(value = 30, message = "Dias deve ser no maximo 30")
    private Integer dias = 1;

    @Min(value = 0, message = "Noites nao pode ser negativo")
    @Max(value = 30, message = "Noites deve ser no maximo 30")
    private Integer noites = 0;

    private boolean incluiHotel;
    private boolean incluiAereo;

    @NotNull(message = "Estoque e obrigatorio")
    @Min(value = 0, message = "Estoque nao pode ser negativo")
    private Integer estoque = 0;

    private boolean ativo = true;

    @Size(max = 500)
    private String imagemUrl;

    /** Regra de negocio no proprio objeto: o estoque nunca fica negativo. */
    public void baixarEstoque(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
        int disponivel = estoque == null ? 0 : estoque;
        if (disponivel < quantidade) {
            throw new IllegalStateException("Estoque insuficiente para '" + nome + "' (disponivel: " + disponivel + ")");
        }
        estoque = disponivel - quantidade;
    }

    public void devolverEstoque(int quantidade) {
        if (quantidade > 0) {
            estoque = (estoque == null ? 0 : estoque) + quantidade;
        }
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public CategoriaProduto getCategoria() { return categoria; }
    public void setCategoria(CategoriaProduto categoria) { this.categoria = categoria; }
    public Parque getParque() { return parque; }
    public void setParque(Parque parque) { this.parque = parque; }
    public Parceiro getParceiro() { return parceiro; }
    public void setParceiro(Parceiro parceiro) { this.parceiro = parceiro; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }
    public Integer getDias() { return dias; }
    public void setDias(Integer dias) { this.dias = dias; }
    public Integer getNoites() { return noites; }
    public void setNoites(Integer noites) { this.noites = noites; }
    public boolean isIncluiHotel() { return incluiHotel; }
    public void setIncluiHotel(boolean incluiHotel) { this.incluiHotel = incluiHotel; }
    public boolean isIncluiAereo() { return incluiAereo; }
    public void setIncluiAereo(boolean incluiAereo) { this.incluiAereo = incluiAereo; }
    public Integer getEstoque() { return estoque; }
    public void setEstoque(Integer estoque) { this.estoque = estoque; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public String getImagemUrl() { return imagemUrl; }
    public void setImagemUrl(String imagemUrl) { this.imagemUrl = imagemUrl; }
}
