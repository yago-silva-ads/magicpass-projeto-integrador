package br.senac.pi.magicpass.controller;

import br.senac.pi.magicpass.config.AuthInterceptor;
import br.senac.pi.magicpass.dto.Dtos.Dashboard;
import br.senac.pi.magicpass.dto.Dtos.LoginRequest;
import br.senac.pi.magicpass.dto.Dtos.LoginResponse;
import br.senac.pi.magicpass.dto.Dtos.Vitrine;
import br.senac.pi.magicpass.model.Usuario;
import br.senac.pi.magicpass.service.SessaoService;
import br.senac.pi.magicpass.service.UsuarioService;
import br.senac.pi.magicpass.service.VitrineService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Login/logout, vitrine publica da Black Friday e painel. */
@RestController
@RequestMapping("/api")
public class AuthController {

    private final UsuarioService usuarios;
    private final SessaoService sessoes;
    private final VitrineService vitrine;

    public AuthController(UsuarioService usuarios, SessaoService sessoes, VitrineService vitrine) {
        this.usuarios = usuarios;
        this.sessoes = sessoes;
        this.vitrine = vitrine;
    }

    @PostMapping("/auth/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        Usuario u = usuarios.autenticar(req.login(), req.senha());
        return new LoginResponse(sessoes.abrir(u.getId()), u.getId(), u.getNome(), u.getLogin(), u.getPerfil());
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String header) {
        if (header != null && header.startsWith("Bearer ")) sessoes.encerrar(header.substring(7).trim());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/auth/me")
    public Usuario eu(@RequestAttribute(AuthInterceptor.ATRIBUTO_USUARIO) Usuario logado) {
        return logado;
    }

    @GetMapping("/publico/vitrine")
    public Vitrine vitrine() { return vitrine.vitrine(); }

    @GetMapping("/dashboard")
    public Dashboard dashboard() { return vitrine.dashboard(); }
}
