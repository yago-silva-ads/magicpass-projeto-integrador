package br.senac.pi.magicpass.service;

import br.senac.pi.magicpass.exception.Excecoes;
import br.senac.pi.magicpass.model.Cliente;
import br.senac.pi.magicpass.repository.ClienteRepository;
import br.senac.pi.magicpass.repository.VendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService extends CrudService<Cliente> {

    private final ClienteRepository clientes;
    private final VendaRepository vendas;

    public ClienteService(ClienteRepository clientes, VendaRepository vendas) {
        super(clientes);
        this.clientes = clientes;
        this.vendas = vendas;
    }

    @Override
    protected String nomeEntidade() { return "Cliente"; }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> listar() { return clientes.findAllByOrderByNomeAsc(); }

    @Override
    protected void validar(Cliente novo, Cliente atual) {
        boolean cpfEmUso = atual == null
                ? clientes.existsByCpf(novo.getCpf())
                : clientes.existsByCpfAndIdNot(novo.getCpf(), atual.getId());
        if (cpfEmUso) throw new Excecoes.Conflito("Ja existe um cliente com este CPF");
    }

    @Override
    protected void antesDeExcluir(Cliente c) {
        if (vendas.existsByClienteId(c.getId())) {
            throw new Excecoes.Conflito("Cliente possui vendas registradas e nao pode ser excluido. Exclua as vendas antes.");
        }
    }
}
