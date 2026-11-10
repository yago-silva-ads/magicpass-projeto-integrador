package br.senac.pi.magicpass.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Superclasse de todas as entidades: id + auditoria simples (heranca). */
@MappedSuperclass
public abstract class EntidadeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(updatable = false)
    private LocalDateTime criadoEm;

    private LocalDateTime atualizadoEm;

    @PrePersist
    protected void aoCriar() {
        criadoEm = LocalDateTime.now();
        atualizadoEm = criadoEm;
    }

    @PreUpdate
    protected void aoAtualizar() {
        atualizadoEm = LocalDateTime.now();
    }

    /** Na alteracao, preserva a data de criacao do registro original. */
    public void herdarAuditoria(EntidadeBase original) {
        this.criadoEm = original.criadoEm;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
}
