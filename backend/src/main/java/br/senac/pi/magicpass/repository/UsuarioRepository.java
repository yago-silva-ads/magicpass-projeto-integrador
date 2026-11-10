package br.senac.pi.magicpass.repository;

import br.senac.pi.magicpass.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByLogin(String login);
    boolean existsByLogin(String login);
    boolean existsByLoginAndIdNot(String login, Long id);
    long countByPerfilAndAtivoTrue(Perfil perfil);
}
