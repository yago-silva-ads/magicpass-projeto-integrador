package br.senac.pi.magicpass.controller;

import br.senac.pi.magicpass.model.Produto;
import br.senac.pi.magicpass.service.ProdutoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController extends CrudController<Produto> {

    public ProdutoController(ProdutoService service) {
        super(service);
    }
}
