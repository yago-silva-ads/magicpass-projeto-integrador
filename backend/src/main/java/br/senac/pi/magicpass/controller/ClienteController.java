package br.senac.pi.magicpass.controller;

import br.senac.pi.magicpass.model.Cliente;
import br.senac.pi.magicpass.service.ClienteService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController extends CrudController<Cliente> {

    public ClienteController(ClienteService service) {
        super(service);
    }
}
