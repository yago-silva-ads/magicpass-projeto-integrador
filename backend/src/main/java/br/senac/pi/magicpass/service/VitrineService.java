package br.senac.pi.magicpass.service;

import br.senac.pi.magicpass.dto.Dtos.*;
import br.senac.pi.magicpass.model.*;
import br.senac.pi.magicpass.repository.ItemVendaRepository;
import br.senac.pi.magicpass.repository.ClienteRepository;
import br.senac.pi.magicpass.repository.ProdutoRepository;
import br.senac.pi.magicpass.repository.VendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/** Vitrine publica da Black Friday e indicadores do painel. */
@Service
public class VitrineService {

    private final ProdutoRepository produtos;
    private final VendaRepository vendas;
    private final ClienteRepository clientes;
    private final PrecoService precoService;

    public VitrineService(ProdutoRepository produtos, VendaRepository vendas, ClienteRepository clientes,
                          ItemVendaRepository itens, PrecoService precoService) {
        this.produtos = produtos;
        this.vendas = vendas;
        this.clientes = clientes;
        this.precoService = precoService;
    }

    private Campanha campanha(List<Promocao> vigentes) {
        return vigentes.stream().filter(p -> !p.exigeCupom())
                .max(Comparator.comparing(Promocao::getPercentual))
                .map(p -> new Campanha(p.getId(), p.getNome(), p.getChamada(), p.getPercentual(), p.getInicio(),
                        p.getFim(), p.getQtdMinimaProgressivo(), p.getPercentualProgressivo()))
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Vitrine vitrine() {
        LocalDateTime agora = LocalDateTime.now();
        List<Promocao> vigentes = precoService.vigentes(agora);
        List<Oferta> ofertas = new ArrayList<>();
        for (Produto p : produtos.findByAtivoTrueOrderByCategoriaAscPrecoAsc()) {
            if (p.getEstoque() == null || p.getEstoque() <= 0) continue;
            Optional<Promocao> promo = precoService.melhorAutomatica(p, vigentes);
            BigDecimal pct = promo.map(Promocao::getPercentual).orElse(BigDecimal.ZERO);
            BigDecimal precoPromo = PrecoService.aplicarPercentual(p.getPreco(), pct);
            BigDecimal parcela = precoPromo.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
            ofertas.add(new Oferta(p.getId(), p.getNome(), p.getCategoria(), p.getParque().getNome(),
                    p.getParque().getLocal(), p.getDescricao(),
                    p.getImagemUrl() != null && !p.getImagemUrl().isBlank() ? p.getImagemUrl() : p.getParque().getImagemUrl(), p.getDias(), p.getNoites(),
                    p.isIncluiHotel(), p.isIncluiAereo(), p.getEstoque(), PrecoService.dinheiro(p.getPreco()),
                    precoPromo, pct, promo.map(Promocao::getNome).orElse(null), parcela));
        }
        ofertas.sort(Comparator.comparing(Oferta::percentualDesconto).reversed());
        List<CupomPublico> cupons = vigentes.stream().filter(Promocao::exigeCupom)
                .map(p -> new CupomPublico(p.getCupom(), p.getPercentual(), p.getChamada(), p.getFim())).toList();
        return new Vitrine(campanha(vigentes), cupons, ofertas, PrecoService.DESCONTO_PIX, agora);
    }

    @Transactional(readOnly = true)
    public Dashboard dashboard() {
        List<Venda> todas = vendas.findAll();
        List<Venda> confirmadas = todas.stream().filter(v -> !v.isCancelada()).toList();
        BigDecimal faturamento = confirmadas.stream().map(Venda::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal economia = confirmadas.stream().map(Venda::getDesconto).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal ticket = confirmadas.isEmpty() ? BigDecimal.ZERO
                : faturamento.divide(BigDecimal.valueOf(confirmadas.size()), 2, RoundingMode.HALF_UP);

        Map<String, long[]> qtd = new HashMap<>();
        Map<String, BigDecimal> fat = new HashMap<>();
        long ingressos = 0;
        for (Venda v : confirmadas) {
            for (ItemVenda i : v.getItens()) {
                String nome = i.getProduto().getNome();
                qtd.computeIfAbsent(nome, k -> new long[1])[0] += i.getQuantidade();
                fat.merge(nome, i.getTotalItem(), BigDecimal::add);
                ingressos += i.getQuantidade();
            }
        }
        List<ProdutoRanking> ranking = qtd.entrySet().stream()
                .map(e -> new ProdutoRanking(e.getKey(), e.getValue()[0], fat.get(e.getKey())))
                .sorted(Comparator.comparing(ProdutoRanking::quantidade).reversed())
                .limit(5).collect(Collectors.toList());
        List<Produto> ativos = produtos.findByAtivoTrueOrderByCategoriaAscPrecoAsc();
        List<EstoqueBaixo> baixo = ativos.stream().filter(p -> p.getEstoque() < 10)
                .map(p -> new EstoqueBaixo(p.getId(), p.getNome(), p.getEstoque())).toList();

        return new Dashboard(PrecoService.dinheiro(faturamento), confirmadas.size(), todas.size() - confirmadas.size(),
                ticket, PrecoService.dinheiro(economia), clientes.count(), ativos.size(), ingressos, ranking, baixo,
                campanha(precoService.vigentes(LocalDateTime.now())));
    }
}
