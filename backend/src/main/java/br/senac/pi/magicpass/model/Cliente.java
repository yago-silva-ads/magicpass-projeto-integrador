package br.senac.pi.magicpass.model;

import br.senac.pi.magicpass.validation.Cpf;
import br.senac.pi.magicpass.validation.Rg;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Entity
@Table(name = "clientes")
public class Cliente extends EntidadeBase {

    @NotBlank(message = "Nome e obrigatorio")
    @Size(min = 3, max = 120, message = "Nome deve ter entre 3 e 120 caracteres")
    private String nome;

    @NotBlank(message = "CPF e obrigatorio")
    @Cpf
    @Column(unique = true, nullable = false, length = 11)
    private String cpf;

    @NotBlank(message = "RG e obrigatorio")
    @Rg
    @Column(length = 9)
    private String rg;

    @NotBlank(message = "E-mail e obrigatorio")
    @Email(message = "E-mail invalido")
    private String email;

    @Pattern(regexp = "^$|^\\d{10,11}$", message = "Telefone deve ter DDD + numero (10 ou 11 digitos)")
    private String telefone;

    @Past(message = "Data de nascimento deve estar no passado")
    private LocalDate dataNascimento;

    @Size(max = 60)
    private String cidade;

    @Pattern(regexp = "^$|^[A-Z]{2}$", message = "UF deve ter 2 letras")
    private String uf;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome == null ? null : nome.trim(); }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf == null ? null : cpf.replaceAll("\\D", ""); }
    public String getRg() { return rg; }
    public void setRg(String rg) { this.rg = rg == null ? null : rg.toUpperCase().replaceAll("[^0-9X]", ""); }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email == null ? null : email.trim().toLowerCase(); }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone == null ? null : telefone.replaceAll("\\D", ""); }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf == null ? null : uf.trim().toUpperCase(); }
}
