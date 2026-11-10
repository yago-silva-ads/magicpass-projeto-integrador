package br.senac.pi.magicpass.repository;

import br.senac.pi.magicpass.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsByCpf(String cpf);
    boolean existsByCpfAndIdNot(String cpf, Long id);
    List<Cliente> findAllByOrderByNomeAsc();
}
