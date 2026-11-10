package br.senac.pi.magicpass.service;

import br.senac.pi.magicpass.exception.Excecoes;
import br.senac.pi.magicpass.model.CategoriaProduto;
import br.senac.pi.magicpass.model.Produto;
import br.senac.pi.magicpass.repository.ItemVendaRepository;
import br.senac.pi.magicpass.repository.ProdutoRepository;
import br.senac.pi.magicpass.repository.PromocaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProdutoService extends CrudService<Produto> {

    private final ProdutoRepository produtos;
    private final ParqueService parqueService;
    private final ParceiroService parceiroService;
    private final ItemVendaRepository itens;
    private final PromocaoRepository promocoes;

    public ProdutoService(ProdutoRepository produtos, ParqueService parqueService, ParceiroService parceiroService,
                          ItemVendaRepository itens, PromocaoRepository promocoes) {
        super(produtos);
        this.produtos = produtos;
        this.parqueService = parqueService;
        this.parceiroService = parceiroService;
        this.itens = itens;
        this.promocoes = promocoes;
    }

    @Override
    protected String nomeEntidade() { return "Produto"; }

    @Override
    @Transactional(readOnly = true)
    public List<Produto> listar() { return produtos.findAllByOrderByNomeAsc(); }

    @Override
    protected void validar(Produto novo, Produto atual) {
        // Resolve as referencias pelo id (o front manda so {"id": X}).
        novo.setParque(parqueService.buscar(novo.getParque().getId()));
        if (novo.getParceiro() != null && novo.getParceiro().getId() != null) {
            novo.setParceiro(parceiroService.buscar(novo.getParceiro().getId()));
        } else {
            novo.setParceiro(null);
        }
        if (novo.getNoites() == null) novo.setNoites(0);

        if (novo.getCategoria() == CategoriaProduto.INGRESSO) {
            novo.setIncluiHotel(false);
            novo.setIncluiAereo(false);
            novo.setNoites(0);
        } else {
            if (novo.isIncluiHotel() && novo.getNoites() < 1) {
                throw new Excecoes.RegraNegocio("Pacote com hotel precisa de pelo menos 1 noite");
            }
            if (novo.getNoites() > novo.getDias()) {
                throw new Excecoes.RegraNegocio("Numero de noites nao pode ser maior que o numero de dias");
            }
        }
    }

    @Override
    protected void antesDeExcluir(Produto p) {
        if (itens.existsByProdutoId(p.getId())) {
            throw new Excecoes.Conflito("Produto ja foi vendido e nao pode ser excluido. Desative-o (campo Ativo).");
        }
        if (promocoes.existsByProdutoId(p.getId())) {
            throw new Excecoes.Conflito("Produto esta vinculado a uma promocao. Altere a promocao antes de excluir.");
        }
    }
}
