package br.senac.pi.magicpass.controller;

import br.senac.pi.magicpass.model.Parque;
import br.senac.pi.magicpass.service.ParqueService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/parques")
public class ParqueController extends CrudController<Parque> {

    public ParqueController(ParqueService service) {
        super(service);
    }
}
