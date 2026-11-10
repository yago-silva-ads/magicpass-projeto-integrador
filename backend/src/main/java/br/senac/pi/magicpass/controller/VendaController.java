package br.senac.pi.magicpass.controller;

import br.senac.pi.magicpass.config.AuthInterceptor;
import br.senac.pi.magicpass.dto.Dtos.Cotacao;
import br.senac.pi.magicpass.dto.Dtos.VendaRequest;
import br.senac.pi.magicpass.model.Usuario;
import br.senac.pi.magicpass.model.Venda;
import br.senac.pi.magicpass.service.VendaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendas")
public class VendaController {

    private final VendaService service;

    public VendaController(VendaService service) {
        this.service = service;
    }

    @GetMapping
    public List<Venda> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public Venda buscar(@PathVariable Long id) { return service.buscar(id); }

    /** Pre-visualiza precos e descontos sem gravar nem mexer no estoque. */
    @PostMapping("/cotacao")
    public Cotacao cotar(@Valid @RequestBody VendaRequest req) { return service.cotar(req); }

    @PostMapping
    public ResponseEntity<Venda> criar(@Valid @RequestBody VendaRequest req,
                                       @RequestAttribute(AuthInterceptor.ATRIBUTO_USUARIO) Usuario vendedor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(req, vendedor));
    }

    @PutMapping("/{id}")
    public Venda atualizar(@PathVariable Long id, @Valid @RequestBody VendaRequest req) {
        return service.atualizar(id, req);
    }

    @PutMapping("/{id}/cancelar")
    public Venda cancelar(@PathVariable Long id) { return service.cancelar(id); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
