package br.senac.pi.magicpass.repository;

import br.senac.pi.magicpass.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ItemVendaRepository extends JpaRepository<ItemVenda, Long> {
    boolean existsByProdutoId(Long produtoId);
}
