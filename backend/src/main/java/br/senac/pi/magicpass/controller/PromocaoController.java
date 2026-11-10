package br.senac.pi.magicpass.controller;

import br.senac.pi.magicpass.model.Promocao;
import br.senac.pi.magicpass.service.PromocaoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/promocoes")
public class PromocaoController extends CrudController<Promocao> {

    public PromocaoController(PromocaoService service) {
        super(service);
    }
}
