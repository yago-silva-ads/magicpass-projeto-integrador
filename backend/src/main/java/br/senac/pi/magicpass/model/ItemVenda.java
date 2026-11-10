package br.senac.pi.magicpass.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "itens_venda")
public class ItemVenda extends EntidadeBase {

    @JsonIgnore
    @ManyToOne(optional = false)
    private Venda venda;

    @ManyToOne(optional = false)
    private Produto produto;

    private Integer quantidade;
    private LocalDate dataVisita;
    private BigDecimal precoUnitario;
    private BigDecimal precoComDesconto;
    private BigDecimal totalItem;

    public Venda getVenda() { return venda; }
    public void setVenda(Venda venda) { this.venda = venda; }
    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
    public LocalDate getDataVisita() { return dataVisita; }
    public void setDataVisita(LocalDate dataVisita) { this.dataVisita = dataVisita; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(BigDecimal precoUnitario) { this.precoUnitario = precoUnitario; }
    public BigDecimal getPrecoComDesconto() { return precoComDesconto; }
    public void setPrecoComDesconto(BigDecimal precoComDesconto) { this.precoComDesconto = precoComDesconto; }
    public BigDecimal getTotalItem() { return totalItem; }
    public void setTotalItem(BigDecimal totalItem) { this.totalItem = totalItem; }
}
