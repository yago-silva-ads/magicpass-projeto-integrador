package br.senac.pi.magicpass.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Quando o front Angular e empacotado dentro do jar (pasta static), as rotas do Angular
 * (ex.: /clientes) precisam devolver o index.html ao recarregar a pagina.
 */
@Controller
public class SpaController {

    @GetMapping({"/", "/login", "/black-friday", "/painel", "/clientes/**", "/parceiros/**", "/parques/**",
            "/produtos/**", "/promocoes/**", "/vendas/**", "/usuarios/**"})
    public String index() {
        return "forward:/index.html";
    }
}
