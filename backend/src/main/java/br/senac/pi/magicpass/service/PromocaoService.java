package br.senac.pi.magicpass.service;

import br.senac.pi.magicpass.exception.Excecoes;
import br.senac.pi.magicpass.model.AlvoPromocao;
import br.senac.pi.magicpass.model.Promocao;
import br.senac.pi.magicpass.repository.PromocaoRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PromocaoService extends CrudService<Promocao> {

    private final PromocaoRepository promocoes;
    private final ProdutoService produtoService;

    public PromocaoService(PromocaoRepository promocoes, @Lazy ProdutoService produtoService) {
        super(promocoes);
        this.promocoes = promocoes;
        this.produtoService = produtoService;
    }

    @Override
    protected String nomeEntidade() { return "Promocao"; }

    @Override
    @Transactional(readOnly = true)
    public List<Promocao> listar() { return promocoes.findAllByOrderByInicioDesc(); }

    @Override
    protected void validar(Promocao novo, Promocao atual) {
        if (novo.getAlvo() == AlvoPromocao.PRODUTO) {
            novo.setProduto(produtoService.buscar(novo.getProduto().getId()));
        } else {
            novo.setProduto(null);
        }
        if (novo.getCupom() != null) {
            boolean emUso = atual == null
                    ? promocoes.existsByCupom(novo.getCupom())
                    : promocoes.existsByCupomAndIdNot(novo.getCupom(), atual.getId());
            if (emUso) throw new Excecoes.Conflito("Ja existe uma promocao com o cupom " + novo.getCupom());
        }
        boolean temQtd = novo.getQtdMinimaProgressivo() != null && novo.getQtdMinimaProgressivo() > 0;
        boolean temPct = novo.getPercentualProgressivo() != null && novo.getPercentualProgressivo().signum() > 0;
        if (temQtd != temPct) {
            throw new Excecoes.RegraNegocio("Desconto progressivo: informe a quantidade minima E o percentual extra (ou deixe os dois zerados)");
        }
    }
}
