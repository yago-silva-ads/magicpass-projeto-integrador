package br.senac.pi.magicpass.config;

import br.senac.pi.magicpass.exception.Excecoes;
import br.senac.pi.magicpass.model.Perfil;
import br.senac.pi.magicpass.model.Usuario;
import br.senac.pi.magicpass.repository.UsuarioRepository;
import br.senac.pi.magicpass.service.SessaoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

/**
 * Valida o token em toda chamada a /api (exceto login e vitrine publica).
 * Regras de perfil: usuarios = so ADMIN; cadastros de catalogo (parques, produtos,
 * parceiros, promocoes) = leitura para todos, escrita so ADMIN.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String ATRIBUTO_USUARIO = "usuarioLogado";
    private static final List<String> CATALOGO = List.of("/api/parques", "/api/produtos", "/api/parceiros", "/api/promocoes");

    private final SessaoService sessoes;
    private final UsuarioRepository usuarios;

    public AuthInterceptor(SessaoService sessoes, UsuarioRepository usuarios) {
        this.sessoes = sessoes;
        this.usuarios = usuarios;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) return true;

        String header = req.getHeader("Authorization");
        String token = header != null && header.startsWith("Bearer ") ? header.substring(7).trim() : null;
        Long usuarioId = sessoes.usuarioDoToken(token)
                .orElseThrow(() -> new Excecoes.NaoAutenticado("Sessao expirada ou inexistente. Faca login novamente."));
        Usuario usuario = usuarios.findById(usuarioId).filter(Usuario::isAtivo)
                .orElseThrow(() -> {
                    sessoes.encerrar(token);
                    return new Excecoes.NaoAutenticado("Usuario desativado ou removido. Faca login novamente.");
                });

        String caminho = req.getRequestURI();
        boolean admin = usuario.getPerfil() == Perfil.ADMIN;
        if (!admin && caminho.startsWith("/api/usuarios")) {
            throw new Excecoes.AcessoNegado("Apenas administradores podem gerenciar usuarios");
        }
        boolean escrita = !"GET".equalsIgnoreCase(req.getMethod());
        if (!admin && escrita && CATALOGO.stream().anyMatch(caminho::startsWith)) {
            throw new Excecoes.AcessoNegado("Apenas administradores podem alterar o catalogo e as promocoes");
        }
        req.setAttribute(ATRIBUTO_USUARIO, usuario);
        return true;
    }
}
