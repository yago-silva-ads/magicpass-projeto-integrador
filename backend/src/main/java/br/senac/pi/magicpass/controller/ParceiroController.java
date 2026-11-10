package br.senac.pi.magicpass.controller;

import br.senac.pi.magicpass.model.Parceiro;
import br.senac.pi.magicpass.service.ParceiroService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/parceiros")
public class ParceiroController extends CrudController<Parceiro> {

    public ParceiroController(ParceiroService service) {
        super(service);
    }
}
