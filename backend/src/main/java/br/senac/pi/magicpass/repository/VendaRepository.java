package br.senac.pi.magicpass.repository;

import br.senac.pi.magicpass.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface VendaRepository extends JpaRepository<Venda, Long> {
    boolean existsByClienteId(Long clienteId);
    boolean existsByVendedorId(Long vendedorId);
    List<Venda> findAllByOrderByDataVendaDesc();
}
