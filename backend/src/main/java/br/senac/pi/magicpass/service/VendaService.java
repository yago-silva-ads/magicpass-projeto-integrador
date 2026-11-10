package br.senac.pi.magicpass.service;

import br.senac.pi.magicpass.dto.Dtos.*;
import br.senac.pi.magicpass.exception.Excecoes;
import br.senac.pi.magicpass.model.*;
import br.senac.pi.magicpass.repository.VendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** CRUD de vendas com controle de estoque transacional (tudo ou nada). */
@Service
public class VendaService {

    private final VendaRepository vendas;
    private final ClienteService clienteService;
    private final ProdutoService produtoService;
    private final PrecoService precoService;

    public VendaService(VendaRepository vendas, ClienteService clienteService, ProdutoService produtoService,
                        PrecoService precoService) {
        this.vendas = vendas;
        this.clienteService = clienteService;
        this.produtoService = produtoService;
        this.precoService = precoService;
    }

    @Transactional(readOnly = true)
    public List<Venda> listar() { return vendas.findAllByOrderByDataVendaDesc(); }

    @Transactional(readOnly = true)
    public Venda buscar(Long id) {
        if (id == null || id <= 0) throw new Excecoes.NaoEncontrado("Venda nao encontrada: id invalido (" + id + ")");
        return vendas.findById(id).orElseThrow(() -> new Excecoes.NaoEncontrado("Venda id " + id + " nao encontrada"));
    }

    @Transactional(readOnly = true)
    public Cotacao cotar(VendaRequest req) { return precoService.cotar(req); }

    @Transactional
    public Venda criar(VendaRequest req, Usuario vendedor) {
        Venda venda = new Venda();
        venda.setVendedor(vendedor);
        venda.setDataVenda(LocalDateTime.now());
        preencher(venda, req);
        return vendas.saveAndFlush(venda);
    }

    /** Alterar venda: devolve o estoque dos itens antigos e aplica os novos (se faltar estoque, nada muda). */
    @Transactional
    public Venda atualizar(Long id, VendaRequest req) {
        Venda venda = buscar(id);
        if (venda.isCancelada()) throw new Excecoes.RegraNegocio("Venda cancelada nao pode ser alterada");
        devolverEstoque(venda);
        venda.getItens().clear();
        vendas.flush();
        preencher(venda, req);
        return vendas.saveAndFlush(venda);
    }

    @Transactional
    public Venda cancelar(Long id) {
        Venda venda = buscar(id);
        if (venda.isCancelada()) throw new Excecoes.RegraNegocio("Esta venda ja esta cancelada");
        devolverEstoque(venda);
        venda.setStatus(StatusVenda.CANCELADA);
        return vendas.saveAndFlush(venda);
    }

    /** Excluir: se ainda estava confirmada, o estoque volta antes de apagar. */
    @Transactional
    public void excluir(Long id) {
        Venda venda = buscar(id);
        if (!venda.isCancelada()) devolverEstoque(venda);
        vendas.delete(venda);
        vendas.flush();
    }

    private void preencher(Venda venda, VendaRequest req) {
        Cotacao cot = precoService.cotar(req);
        venda.setCliente(clienteService.buscar(req.clienteId()));
        venda.setFormaPagamento(req.formaPagamento());
        venda.setParcelas(cot.parcelas());
        venda.setCupom(cot.cupom());
        venda.setObservacao(req.observacao());
        venda.setSubtotal(cot.subtotal());
        venda.setDesconto(cot.desconto());
        venda.setTotal(cot.total());
        venda.setValorParcela(cot.valorParcela());
        venda.setPromocoesAplicadas(String.join("; ", cot.promocoesAplicadas()));
        venda.setStatus(StatusVenda.CONFIRMADA);

        for (ItemCotacao ic : cot.itens()) {
            Produto produto = produtoService.buscar(ic.produtoId());
            try {
                produto.baixarEstoque(ic.quantidade());
            } catch (IllegalStateException e) {
                throw new Excecoes.RegraNegocio(e.getMessage());
            }
            ItemVenda item = new ItemVenda();
            item.setProduto(produto);
            item.setQuantidade(ic.quantidade());
            item.setDataVisita(ic.dataVisita());
            item.setPrecoUnitario(ic.precoUnitario());
            item.setPrecoComDesconto(ic.precoComDesconto());
            item.setTotalItem(ic.totalItem());
            venda.adicionarItem(item);
        }
    }

    private void devolverEstoque(Venda venda) {
        for (ItemVenda item : venda.getItens()) {
            item.getProduto().devolverEstoque(item.getQuantidade());
        }
    }
}
