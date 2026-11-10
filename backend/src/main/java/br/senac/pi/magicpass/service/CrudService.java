package br.senac.pi.magicpass.service;

import br.senac.pi.magicpass.exception.Excecoes;
import br.senac.pi.magicpass.model.EntidadeBase;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service generico com o CRUD completo (Template Method).
 * As subclasses so implementam as regras especificas em validar() e antesDeExcluir().
 */
public abstract class CrudService<T extends EntidadeBase> {

    protected final JpaRepository<T, Long> repositorio;

    protected CrudService(JpaRepository<T, Long> repositorio) {
        this.repositorio = repositorio;
    }

    /** Nome usado nas mensagens de erro (ex.: "Cliente"). */
    protected abstract String nomeEntidade();

    /** Regras de negocio antes de gravar. "atual" e null na criacao. */
    protected void validar(T novo, T atual) {}

    /** Bloqueios de exclusao (ex.: registro em uso). */
    protected void antesDeExcluir(T entidade) {}

    @Transactional(readOnly = true)
    public List<T> listar() {
        return repositorio.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    /** Ids nulos, zero ou negativos (ex.: -1) nunca chegam ao banco: viram 404 com mensagem clara. */
    @Transactional(readOnly = true)
    public T buscar(Long id) {
        if (id == null || id <= 0) {
            throw new Excecoes.NaoEncontrado(nomeEntidade() + " nao encontrado(a): id invalido (" + id + ")");
        }
        return repositorio.findById(id)
                .orElseThrow(() -> new Excecoes.NaoEncontrado(nomeEntidade() + " id " + id + " nao encontrado(a)"));
    }

    @Transactional
    public T criar(T entidade) {
        entidade.setId(null); // ignora id enviado pelo front (evita sobrescrever registro existente)
        validar(entidade, null);
        return repositorio.saveAndFlush(entidade);
    }

    @Transactional
    public T atualizar(Long id, T dados) {
        T atual = buscar(id);
        dados.setId(atual.getId());
        dados.herdarAuditoria(atual);
        validar(dados, atual);
        return repositorio.saveAndFlush(dados);
    }

    @Transactional
    public void excluir(Long id) {
        T entidade = buscar(id);
        antesDeExcluir(entidade);
        repositorio.delete(entidade);
        repositorio.flush();
    }
}
