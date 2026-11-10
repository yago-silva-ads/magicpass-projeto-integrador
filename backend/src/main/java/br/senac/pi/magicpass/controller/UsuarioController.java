package br.senac.pi.magicpass.controller;

import br.senac.pi.magicpass.config.AuthInterceptor;
import br.senac.pi.magicpass.model.Usuario;
import br.senac.pi.magicpass.service.SessaoService;
import br.senac.pi.magicpass.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/** Tela propria do administrador para cadastrar logins e senhas (o interceptor bloqueia nao-admin). */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController extends CrudController<Usuario> {

    private final UsuarioService usuarioService;
    private final SessaoService sessoes;

    public UsuarioController(UsuarioService service, SessaoService sessoes) {
        super(service);
        this.usuarioService = service;
        this.sessoes = sessoes;
    }

    @Override
    @PutMapping("/{id}")
    public Usuario atualizar(@PathVariable Long id, @Valid @RequestBody Usuario entidade) {
        Usuario salvo = usuarioService.atualizar(id, entidade);
        if (!salvo.isAtivo()) sessoes.encerrarDoUsuario(salvo.getId()); // desativado sai na hora
        return salvo;
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        Usuario logado = (Usuario) RequestContextHolder.currentRequestAttributes()
                .getAttribute(AuthInterceptor.ATRIBUTO_USUARIO, RequestAttributes.SCOPE_REQUEST);
        usuarioService.excluir(id, logado);
        sessoes.encerrarDoUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
