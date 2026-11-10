package br.senac.pi.magicpass.service;

import br.senac.pi.magicpass.exception.Excecoes;
import br.senac.pi.magicpass.model.Perfil;
import br.senac.pi.magicpass.model.Usuario;
import br.senac.pi.magicpass.repository.UsuarioRepository;
import br.senac.pi.magicpass.repository.VendaRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService extends CrudService<Usuario> {

    private final UsuarioRepository usuarios;
    private final VendaRepository vendas;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UsuarioService(UsuarioRepository usuarios, VendaRepository vendas) {
        super(usuarios);
        this.usuarios = usuarios;
        this.vendas = vendas;
    }

    @Override
    protected String nomeEntidade() { return "Usuario"; }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarios.findAll(org.springframework.data.domain.Sort.by("nome"));
    }

    @Override
    protected void validar(Usuario novo, Usuario atual) {
        boolean loginEmUso = atual == null
                ? usuarios.existsByLogin(novo.getLogin())
                : usuarios.existsByLoginAndIdNot(novo.getLogin(), atual.getId());
        if (loginEmUso) throw new Excecoes.Conflito("Ja existe um usuario com o login '" + novo.getLogin() + "'");

        String senha = novo.getSenha();
        if (atual == null || (senha != null && !senha.isBlank())) {
            if (senha == null || senha.length() < 6 || senha.length() > 60) {
                throw new Excecoes.RegraNegocio("A senha deve ter entre 6 e 60 caracteres");
            }
            novo.setSenha(encoder.encode(senha));
        } else {
            novo.setSenha(atual.getSenha()); // alteracao sem senha: mantem a atual
        }

        // Nunca deixa o sistema sem administrador ativo.
        if (atual != null && atual.getPerfil() == Perfil.ADMIN && atual.isAtivo()
                && (novo.getPerfil() != Perfil.ADMIN || !novo.isAtivo())
                && usuarios.countByPerfilAndAtivoTrue(Perfil.ADMIN) <= 1) {
            throw new Excecoes.RegraNegocio("Nao e possivel remover/desativar o ultimo administrador ativo");
        }
    }

    @Override
    protected void antesDeExcluir(Usuario u) {
        if (u.getPerfil() == Perfil.ADMIN && u.isAtivo() && usuarios.countByPerfilAndAtivoTrue(Perfil.ADMIN) <= 1) {
            throw new Excecoes.RegraNegocio("Nao e possivel excluir o ultimo administrador ativo");
        }
        if (vendas.existsByVendedorId(u.getId())) {
            throw new Excecoes.Conflito("Usuario possui vendas registradas. Desative-o em vez de excluir.");
        }
    }

    @Transactional
    public void excluir(Long id, Usuario logado) {
        if (logado != null && logado.getId().equals(id)) {
            throw new Excecoes.RegraNegocio("Voce nao pode excluir o proprio usuario logado");
        }
        excluir(id);
    }

    /** Valida login + senha. Mensagem generica para nao revelar qual dos dois esta errado. */
    @Transactional(readOnly = true)
    public Usuario autenticar(String login, String senha) {
        if (login == null || senha == null || login.isBlank() || senha.isBlank()) {
            throw new Excecoes.NaoAutenticado("Informe login e senha");
        }
        Usuario u = usuarios.findByLogin(login.trim().toLowerCase())
                .orElseThrow(() -> new Excecoes.NaoAutenticado("Login ou senha invalidos"));
        if (!encoder.matches(senha, u.getSenha())) throw new Excecoes.NaoAutenticado("Login ou senha invalidos");
        if (!u.isAtivo()) throw new Excecoes.NaoAutenticado("Usuario desativado. Procure o administrador.");
        return u;
    }

    /** Usado pelo seed inicial. */
    @Transactional
    public Usuario criarSeNaoExistir(String nome, String login, String senha, Perfil perfil) {
        return usuarios.findByLogin(login).orElseGet(() -> {
            Usuario u = new Usuario();
            u.setNome(nome);
            u.setLogin(login);
            u.setSenha(senha);
            u.setPerfil(perfil);
            return criar(u);
        });
    }
}
