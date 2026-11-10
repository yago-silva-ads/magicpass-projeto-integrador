package br.senac.pi.magicpass.dto;

import br.senac.pi.magicpass.model.CategoriaProduto;
import br.senac.pi.magicpass.model.FormaPagamento;
import br.senac.pi.magicpass.model.Perfil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Objetos de entrada/saida da API que nao sao entidades. */
public final class Dtos {

    private Dtos() {}

    // ---------- Autenticacao
    public record LoginRequest(@NotBlank(message = "Informe o login") String login,
                               @NotBlank(message = "Informe a senha") String senha) {}

    public record LoginResponse(String token, Long id, String nome, String login, Perfil perfil) {}

    // ---------- Venda
    public record ItemRequest(
            @NotNull(message = "Selecione o produto") @Positive(message = "Produto invalido") Long produtoId,
            @NotNull(message = "Informe a quantidade") @Min(value = 1, message = "Quantidade minima e 1")
            @Max(value = 20, message = "Quantidade maxima por item e 20") Integer quantidade,
            @NotNull(message = "Informe a data da visita/embarque")
            @FutureOrPresent(message = "A data da visita nao pode estar no passado") LocalDate dataVisita) {}

    public record VendaRequest(
            @NotNull(message = "Selecione o cliente") @Positive(message = "Cliente invalido") Long clienteId,
            @NotNull(message = "Selecione a forma de pagamento") FormaPagamento formaPagamento,
            @Min(value = 1, message = "Minimo 1 parcela") @Max(value = 12, message = "Maximo 12 parcelas") Integer parcelas,
            @Size(max = 20) String cupom,
            @Size(max = 500) String observacao,
            @NotEmpty(message = "Adicione pelo menos um item ao carrinho") @Size(max = 30) List<@Valid ItemRequest> itens) {}

    public record ItemCotacao(Long produtoId, String produto, CategoriaProduto categoria, String parque,
                              Integer quantidade, LocalDate dataVisita, BigDecimal precoUnitario,
                              BigDecimal percentualDesconto, BigDecimal precoComDesconto, BigDecimal totalItem) {}

    public record Cotacao(List<ItemCotacao> itens, BigDecimal subtotal, BigDecimal descontoPromocoes,
                          BigDecimal descontoPix, BigDecimal desconto, BigDecimal total, Integer parcelas,
                          BigDecimal valorParcela, List<String> promocoesAplicadas, String cupom) {}

    // ---------- Vitrine publica (Black Friday)
    public record Oferta(Long produtoId, String nome, CategoriaProduto categoria, String parque, String local,
                         String descricao, String imagemUrl, Integer dias, Integer noites, boolean incluiHotel,
                         boolean incluiAereo, Integer estoque, BigDecimal precoOriginal, BigDecimal precoPromocional,
                         BigDecimal percentualDesconto, String promocao, BigDecimal parcela12x) {}

    public record Campanha(Long id, String nome, String chamada, BigDecimal percentual, LocalDateTime inicio,
                           LocalDateTime fim, Integer qtdMinimaProgressivo, BigDecimal percentualProgressivo) {}

    public record CupomPublico(String codigo, BigDecimal percentual, String descricao, LocalDateTime fim) {}

    public record Vitrine(Campanha campanha, List<CupomPublico> cupons, List<Oferta> ofertas,
                          BigDecimal descontoPix, LocalDateTime agora) {}

    // ---------- Dashboard
    public record ProdutoRanking(String produto, long quantidade, BigDecimal faturamento) {}

    public record EstoqueBaixo(Long id, String produto, Integer estoque) {}

    public record Dashboard(BigDecimal faturamento, long vendasConfirmadas, long vendasCanceladas,
                            BigDecimal ticketMedio, BigDecimal economiaClientes, long clientes, long produtosAtivos,
                            long ingressosVendidos, List<ProdutoRanking> maisVendidos, List<EstoqueBaixo> estoqueBaixo,
                            Campanha campanhaAtiva) {}
}
