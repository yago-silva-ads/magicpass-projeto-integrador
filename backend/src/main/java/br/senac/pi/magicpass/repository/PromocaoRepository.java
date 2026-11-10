package br.senac.pi.magicpass.repository;

import br.senac.pi.magicpass.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface PromocaoRepository extends JpaRepository<Promocao, Long> {
    List<Promocao> findByAtivaTrue();
    boolean existsByProdutoId(Long produtoId);
    boolean existsByCupom(String cupom);
    boolean existsByCupomAndIdNot(String cupom, Long id);
    List<Promocao> findAllByOrderByInicioDesc();
}
