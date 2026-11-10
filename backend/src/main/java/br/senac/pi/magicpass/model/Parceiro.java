package br.senac.pi.magicpass.model;

import br.senac.pi.magicpass.validation.Cnpj;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/** Agencia/operadora parceira (hotel, aereo, receptivo) usada nos pacotes de viagem. */
@Entity
@Table(name = "parceiros")
public class Parceiro extends EntidadeBase {

    @NotBlank(message = "Razao social e obrigatoria")
    @Size(max = 150)
    private String razaoSocial;

    @Size(max = 100)
    private String nomeFantasia;

    @NotBlank(message = "CNPJ e obrigatorio")
    @Cnpj
    @Column(unique = true, nullable = false, length = 14)
    private String cnpj;

    /** Inscricao estadual (ou ISENTO). Validada no service junto com a UF. */
    @NotBlank(message = "Inscricao estadual e obrigatoria (ou ISENTO)")
    private String inscricaoEstadual;

    @NotBlank(message = "UF e obrigatoria")
    @Pattern(regexp = "^[A-Z]{2}$", message = "UF deve ter 2 letras")
    private String uf;

    @NotBlank(message = "Tipo de servico e obrigatorio")
    private String tipoServico;

    @Email(message = "E-mail invalido")
    private String email;

    private String telefone;

    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }
    public String getNomeFantasia() { return nomeFantasia; }
    public void setNomeFantasia(String nomeFantasia) { this.nomeFantasia = nomeFantasia; }
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj == null ? null : cnpj.toUpperCase().replaceAll("[^0-9A-Z]", ""); }
    public String getInscricaoEstadual() { return inscricaoEstadual; }
    public void setInscricaoEstadual(String ie) {
        if (ie == null) { this.inscricaoEstadual = null; return; }
        String limpo = ie.trim().toUpperCase();
        this.inscricaoEstadual = limpo.equals("ISENTO") ? limpo : limpo.replaceAll("\\D", "");
    }
    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf == null ? null : uf.trim().toUpperCase(); }
    public String getTipoServico() { return tipoServico; }
    public void setTipoServico(String tipoServico) { this.tipoServico = tipoServico; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
}
