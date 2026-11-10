package br.senac.pi.magicpass.service;

import br.senac.pi.magicpass.exception.Excecoes;
import br.senac.pi.magicpass.model.Parceiro;
import br.senac.pi.magicpass.repository.ParceiroRepository;
import br.senac.pi.magicpass.repository.ProdutoRepository;
import br.senac.pi.magicpass.validation.DocumentoValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ParceiroService extends CrudService<Parceiro> {

    private final ParceiroRepository parceiros;
    private final ProdutoRepository produtos;

    public ParceiroService(ParceiroRepository parceiros, ProdutoRepository produtos) {
        super(parceiros);
        this.parceiros = parceiros;
        this.produtos = produtos;
    }

    @Override
    protected String nomeEntidade() { return "Parceiro"; }

    @Override
    @Transactional(readOnly = true)
    public List<Parceiro> listar() { return parceiros.findAllByOrderByRazaoSocialAsc(); }

    @Override
    protected void validar(Parceiro novo, Parceiro atual) {
        boolean cnpjEmUso = atual == null
                ? parceiros.existsByCnpj(novo.getCnpj())
                : parceiros.existsByCnpjAndIdNot(novo.getCnpj(), atual.getId());
        if (cnpjEmUso) throw new Excecoes.Conflito("Ja existe um parceiro com este CNPJ");

        String ie = novo.getInscricaoEstadual();
        if (!"ISENTO".equals(ie) && !DocumentoValidator.UFS_IE_SUPORTADAS.contains(novo.getUf())) {
            throw new Excecoes.RegraNegocio("Validacao de IE disponivel para " + DocumentoValidator.UFS_IE_SUPORTADAS
                    + ". Para outra UF informe ISENTO.");
        }
        if (!DocumentoValidator.ieValida(ie, novo.getUf())) {
            throw new Excecoes.RegraNegocio("Inscricao estadual invalida para a UF " + novo.getUf()
                    + " (digitos verificadores nao conferem)");
        }
    }

    @Override
    protected void antesDeExcluir(Parceiro p) {
        if (produtos.existsByParceiroId(p.getId())) {
            throw new Excecoes.Conflito("Parceiro vinculado a pacotes. Remova o vinculo dos produtos antes de excluir.");
        }
    }
}
