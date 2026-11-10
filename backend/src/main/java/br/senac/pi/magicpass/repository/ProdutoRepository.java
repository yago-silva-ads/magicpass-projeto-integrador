package br.senac.pi.magicpass.repository;

import br.senac.pi.magicpass.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    boolean existsByParqueId(Long parqueId);
    boolean existsByParceiroId(Long parceiroId);
    List<Produto> findAllByOrderByNomeAsc();
    List<Produto> findByAtivoTrueOrderByCategoriaAscPrecoAsc();
}
