package br.senac.pi.magicpass.service;

import br.senac.pi.magicpass.exception.Excecoes;
import br.senac.pi.magicpass.model.Parque;
import br.senac.pi.magicpass.repository.ParqueRepository;
import br.senac.pi.magicpass.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ParqueService extends CrudService<Parque> {

    private final ParqueRepository parques;
    private final ProdutoRepository produtos;

    public ParqueService(ParqueRepository parques, ProdutoRepository produtos) {
        super(parques);
        this.parques = parques;
        this.produtos = produtos;
    }

    @Override
    protected String nomeEntidade() { return "Parque"; }

    @Override
    @Transactional(readOnly = true)
    public List<Parque> listar() { return parques.findAllByOrderByNomeAsc(); }

    @Override
    protected void validar(Parque novo, Parque atual) {
        boolean nomeEmUso = atual == null
                ? parques.existsByNomeIgnoreCase(novo.getNome())
                : parques.existsByNomeIgnoreCaseAndIdNot(novo.getNome(), atual.getId());
        if (nomeEmUso) throw new Excecoes.Conflito("Ja existe um parque com este nome");
    }

    @Override
    protected void antesDeExcluir(Parque p) {
        if (produtos.existsByParqueId(p.getId())) {
            throw new Excecoes.Conflito("Parque possui ingressos/pacotes cadastrados. Exclua ou altere os produtos antes.");
        }
    }
}
