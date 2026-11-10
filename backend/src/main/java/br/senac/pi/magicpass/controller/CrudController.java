package br.senac.pi.magicpass.controller;

import br.senac.pi.magicpass.model.EntidadeBase;
import br.senac.pi.magicpass.service.CrudService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller generico (camada C do MVC): expoe o CRUD REST de qualquer entidade.
 * GET lista | GET /{id} busca | POST cria | PUT /{id} altera | DELETE /{id} apaga.
 */
public abstract class CrudController<T extends EntidadeBase> {

    protected final CrudService<T> service;

    protected CrudController(CrudService<T> service) {
        this.service = service;
    }

    @GetMapping
    public List<T> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public T buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<T> criar(@Valid @RequestBody T entidade) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(entidade));
    }

    @PutMapping("/{id}")
    public T atualizar(@PathVariable Long id, @Valid @RequestBody T entidade) {
        return service.atualizar(id, entidade);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
