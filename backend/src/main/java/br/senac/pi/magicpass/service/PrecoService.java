package br.senac.pi.magicpass.service;

import br.senac.pi.magicpass.dto.Dtos.*;
import br.senac.pi.magicpass.exception.Excecoes;
import br.senac.pi.magicpass.model.FormaPagamento;
import br.senac.pi.magicpass.model.Produto;
import br.senac.pi.magicpass.model.Promocao;
import br.senac.pi.magicpass.repository.PromocaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Motor de precos da Black Friday.
 * Regras: melhor promocao automatica do produto + cupom (se valido) + desconto progressivo
 * para grupos/familias, limitado a 70%. Pagamento no PIX ganha 5% extra no total.
 */
@Service
public class PrecoService {

    public static final BigDecimal DESCONTO_MAXIMO = new BigDecimal("70");
    public static final BigDecimal DESCONTO_PIX = new BigDecimal("5");
    private static final BigDecimal CEM = new BigDecimal("100");

    private final PromocaoRepository promocoes;
    private final ProdutoService produtoService;

    public PrecoService(PromocaoRepository promocoes, ProdutoService produtoService) {
        this.promocoes = promocoes;
        this.produtoService = produtoService;
    }

    public static BigDecimal dinheiro(BigDecimal v) { return v.setScale(2, RoundingMode.HALF_UP); }

    public static BigDecimal aplicarPercentual(BigDecimal valor, BigDecimal percentual) {
        return dinheiro(valor.multiply(CEM.subtract(percentual)).divide(CEM, 6, RoundingMode.HALF_UP));
    }

    @Transactional(readOnly = true)
    public List<Promocao> vigentes(LocalDateTime agora) {
        return promocoes.findByAtivaTrue().stream().filter(p -> p.vigenteEm(agora)).toList();
    }

    /** Melhor promocao automatica (sem cupom) que vale para o produto. */
    public Optional<Promocao> melhorAutomatica(Produto produto, List<Promocao> vigentes) {
        return vigentes.stream()
                .filter(p -> !p.exigeCupom() && p.aplicaA(produto))
                .max(Comparator.comparing(Promocao::getPercentual));
    }

    /** Calcula carrinho completo. Usado na pre-visualizacao e na gravacao da venda. */
    @Transactional(readOnly = true)
    public Cotacao cotar(VendaRequest req) {
        LocalDateTime agora = LocalDateTime.now();
        List<Promocao> vigentes = vigentes(agora);

        String cupomInformado = req.cupom() == null || req.cupom().isBlank() ? null : req.cupom().trim().toUpperCase();
        Promocao promoCupom = null;
        if (cupomInformado != null) {
            promoCupom = vigentes.stream().filter(p -> cupomInformado.equals(p.getCupom())).findFirst()
                    .orElseThrow(() -> new Excecoes.RegraNegocio("Cupom '" + cupomInformado + "' invalido ou expirado"));
        }

        FormaPagamento forma = req.formaPagamento();
        int parcelas = req.parcelas() == null ? 1 : req.parcelas();
        if (forma != FormaPagamento.CARTAO_CREDITO && parcelas != 1) {
            throw new Excecoes.RegraNegocio("Parcelamento disponivel apenas no cartao de credito");
        }

        int qtdTotal = req.itens().stream().mapToInt(ItemRequest::quantidade).sum();
        Set<String> aplicadas = new LinkedHashSet<>();
        List<ItemCotacao> itens = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalComPromo = BigDecimal.ZERO;
        boolean cupomUsado = false;

        for (ItemRequest item : req.itens()) {
            Produto p = produtoService.buscar(item.produtoId());
            if (!p.isAtivo()) throw new Excecoes.RegraNegocio("O produto '" + p.getNome() + "' esta indisponivel");
            if (item.dataVisita() != null && item.dataVisita().isAfter(agora.toLocalDate().plusYears(2))) {
                throw new Excecoes.RegraNegocio("Data da visita deve ser nos proximos 2 anos");
            }

            BigDecimal pct = BigDecimal.ZERO;
            Optional<Promocao> auto = melhorAutomatica(p, vigentes);
            if (auto.isPresent()) {
                pct = pct.add(auto.get().getPercentual());
                aplicadas.add(auto.get().getNome() + " (-" + auto.get().getPercentual().stripTrailingZeros().toPlainString() + "%)");
                Promocao a = auto.get();
                if (a.getQtdMinimaProgressivo() > 0 && qtdTotal >= a.getQtdMinimaProgressivo()) {
                    pct = pct.add(a.getPercentualProgressivo());
                    aplicadas.add("Familia/grupo " + a.getQtdMinimaProgressivo() + "+ itens (-"
                            + a.getPercentualProgressivo().stripTrailingZeros().toPlainString() + "%)");
                }
            }
            if (promoCupom != null && promoCupom.aplicaA(p)) {
                pct = pct.add(promoCupom.getPercentual());
                cupomUsado = true;
                aplicadas.add("Cupom " + promoCupom.getCupom() + " (-" + promoCupom.getPercentual().stripTrailingZeros().toPlainString() + "%)");
            }
            if (pct.compareTo(DESCONTO_MAXIMO) > 0) pct = DESCONTO_MAXIMO;

            BigDecimal unit = dinheiro(p.getPreco());
            BigDecimal unitDesc = aplicarPercentual(unit, pct);
            BigDecimal qtd = BigDecimal.valueOf(item.quantidade());
            BigDecimal totalItem = dinheiro(unitDesc.multiply(qtd));
            subtotal = subtotal.add(unit.multiply(qtd));
            totalComPromo = totalComPromo.add(totalItem);
            itens.add(new ItemCotacao(p.getId(), p.getNome(), p.getCategoria(), p.getParque().getNome(),
                    item.quantidade(), item.dataVisita(), unit, pct, unitDesc, totalItem));
        }

        if (promoCupom != null && !cupomUsado) {
            throw new Excecoes.RegraNegocio("O cupom " + promoCupom.getCupom() + " nao vale para os itens do carrinho");
        }

        subtotal = dinheiro(subtotal);
        BigDecimal descontoPromo = dinheiro(subtotal.subtract(totalComPromo));
        BigDecimal total = totalComPromo;
        BigDecimal descontoPix = BigDecimal.ZERO;
        if (forma == FormaPagamento.PIX) {
            BigDecimal comPix = aplicarPercentual(totalComPromo, DESCONTO_PIX);
            descontoPix = dinheiro(totalComPromo.subtract(comPix));
            total = comPix;
            aplicadas.add("PIX (-5%)");
        }
        total = dinheiro(total);
        BigDecimal valorParcela = dinheiro(total.divide(BigDecimal.valueOf(parcelas), 6, RoundingMode.HALF_UP));

        return new Cotacao(itens, subtotal, descontoPromo, descontoPix, dinheiro(descontoPromo.add(descontoPix)),
                total, parcelas, valorParcela, new ArrayList<>(aplicadas), cupomUsado ? cupomInformado : null);
    }
}
