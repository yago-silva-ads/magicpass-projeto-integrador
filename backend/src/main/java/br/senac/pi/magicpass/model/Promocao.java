package br.senac.pi.magicpass.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Campanha de desconto (ex.: Black Friday). */
@Entity
@Table(name = "promocoes")
public class Promocao extends EntidadeBase {

    @NotBlank(message = "Nome e obrigatorio")
    @Size(max = 100)
    private String nome;

    @Size(max = 300)
    private String chamada;

    @NotNull(message = "Percentual e obrigatorio")
    @DecimalMin(value = "1", message = "Desconto minimo de 1%")
    @DecimalMax(value = "70", message = "Desconto maximo de 70%")
    private BigDecimal percentual;

    @NotNull(message = "Inicio e obrigatorio")
    private LocalDateTime inicio;

    @NotNull(message = "Fim e obrigatorio")
    private LocalDateTime fim;

    @NotNull(message = "Alvo e obrigatorio")
    @Enumerated(EnumType.STRING)
    private AlvoPromocao alvo = AlvoPromocao.TODOS;

    /** Usado quando alvo = PRODUTO. */
    @ManyToOne
    private Produto produto;

    /** Se preenchido, a promocao so vale com o cupom. */
    @Pattern(regexp = "^[A-Z0-9]{3,20}$", message = "Cupom: 3 a 20 letras maiusculas/numeros")
    private String cupom;

    /** Black Friday: desconto extra para grupos/familias a partir de N itens. */
    @Min(value = 0, message = "Quantidade minima nao pode ser negativa")
    @Max(value = 50)
    private Integer qtdMinimaProgressivo = 0;

    @DecimalMin(value = "0", message = "Desconto progressivo nao pode ser negativo")
    @DecimalMax(value = "20", message = "Desconto progressivo maximo de 20%")
    private BigDecimal percentualProgressivo = BigDecimal.ZERO;

    private boolean ativa = true;

    @JsonIgnore
    @AssertTrue(message = "Fim deve ser depois do inicio")
    public boolean isPeriodoValido() {
        return inicio == null || fim == null || fim.isAfter(inicio);
    }

    @JsonIgnore
    @AssertTrue(message = "Escolha o produto quando o alvo for PRODUTO")
    public boolean isProdutoInformadoQuandoNecessario() {
        return alvo != AlvoPromocao.PRODUTO || produto != null;
    }

    public boolean vigenteEm(LocalDateTime momento) {
        return ativa && inicio != null && fim != null && !momento.isBefore(inicio) && !momento.isAfter(fim);
    }

    public boolean aplicaA(Produto p) {
        if (alvo == null) return true;
        return switch (alvo) {
            case TODOS -> true;
            case INGRESSO -> p.getCategoria() == CategoriaProduto.INGRESSO;
            case PACOTE -> p.getCategoria() == CategoriaProduto.PACOTE;
            case PRODUTO -> produto != null && produto.getId() != null && produto.getId().equals(p.getId());
        };
    }

    public boolean exigeCupom() { return cupom != null && !cupom.isBlank(); }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getChamada() { return chamada; }
    public void setChamada(String chamada) { this.chamada = chamada; }
    public BigDecimal getPercentual() { return percentual; }
    public void setPercentual(BigDecimal percentual) { this.percentual = percentual; }
    public LocalDateTime getInicio() { return inicio; }
    public void setInicio(LocalDateTime inicio) { this.inicio = inicio; }
    public LocalDateTime getFim() { return fim; }
    public void setFim(LocalDateTime fim) { this.fim = fim; }
    public AlvoPromocao getAlvo() { return alvo; }
    public void setAlvo(AlvoPromocao alvo) { this.alvo = alvo; }
    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }
    public String getCupom() { return cupom; }
    public void setCupom(String cupom) { this.cupom = cupom == null || cupom.isBlank() ? null : cupom.trim().toUpperCase(); }
    public Integer getQtdMinimaProgressivo() { return qtdMinimaProgressivo; }
    public void setQtdMinimaProgressivo(Integer v) { this.qtdMinimaProgressivo = v == null ? 0 : v; }
    public BigDecimal getPercentualProgressivo() { return percentualProgressivo; }
    public void setPercentualProgressivo(BigDecimal v) { this.percentualProgressivo = v == null ? BigDecimal.ZERO : v; }
    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }
}
