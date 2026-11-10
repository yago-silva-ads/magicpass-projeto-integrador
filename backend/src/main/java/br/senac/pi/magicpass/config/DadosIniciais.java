package br.senac.pi.magicpass.config;

import br.senac.pi.magicpass.model.*;
import br.senac.pi.magicpass.repository.ParqueRepository;
import br.senac.pi.magicpass.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Popula o banco na primeira execucao (usuarios, parques, ingressos, pacotes e a campanha Black Friday). */
@Component
@Profile("!test")
public class DadosIniciais implements SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(DadosIniciais.class);

    private final UsuarioService usuarios;
    private final ParqueService parques;
    private final ParqueRepository parqueRepo;
    private final ParceiroService parceiros;
    private final ProdutoService produtos;
    private final PromocaoService promocoes;
    private final ClienteService clientes;

    public DadosIniciais(UsuarioService usuarios, ParqueService parques, ParqueRepository parqueRepo,
                         ParceiroService parceiros, ProdutoService produtos, PromocaoService promocoes,
                         ClienteService clientes) {
        this.usuarios = usuarios;
        this.parques = parques;
        this.parqueRepo = parqueRepo;
        this.parceiros = parceiros;
        this.produtos = produtos;
        this.promocoes = promocoes;
        this.clientes = clientes;
    }

    /** Roda antes do Tomcat aceitar requisicoes: ninguem ve o banco vazio. */
    @Override
    public void afterSingletonsInstantiated() {
        usuarios.criarSeNaoExistir("Administrador", "admin", "admin123", Perfil.ADMIN);
        usuarios.criarSeNaoExistir("Vendedor Demo", "vendedor", "vendedor123", Perfil.VENDEDOR);
        if (parqueRepo.count() > 0) return;

        log.info("Carregando dados iniciais do MagicPass...");
        Parque mk = parque("Magic Kingdom", "Orlando - Walt Disney World", "O castelo da Cinderela, desfiles e fogos Happily Ever After.", "magic-kingdom");
        Parque epcot = parque("EPCOT", "Orlando - Walt Disney World", "World Showcase com 11 paises e atracoes de tecnologia.", "epcot");
        Parque hs = parque("Hollywood Studios", "Orlando - Walt Disney World", "Star Wars: Galaxy's Edge e Toy Story Land.", "hollywood-studios");
        Parque ak = parque("Animal Kingdom", "Orlando - Walt Disney World", "Safari africano e o mundo de Pandora - Avatar.", "animal-kingdom");
        Parque dlp = parque("Disneyland Paris", "Paris - Franca", "Dois parques: Disneyland Park e Walt Disney Studios.", "disneyland-paris");
        Parque dlr = parque("Disneyland Resort", "Anaheim - California", "O parque original de Walt Disney e o California Adventure.", "disneyland-california");

        Parceiro operadora = parceiro("Encanto Turismo e Viagens Ltda", "Encanto Turismo", "62819482000163", "710497465505", "SP",
                "Operadora: aereo + hotel");
        parceiro("Castelo Receptivo Internacional SA", "Castelo Receptivo", "19935181000176", "ISENTO", "RJ",
                "Receptivo e traslados em Orlando");

        ingresso("Magic Kingdom - 1 dia", mk, "Acesso de 1 dia ao Magic Kingdom.", "899.90", 1, 120);
        ingresso("EPCOT - 1 dia", epcot, "Acesso de 1 dia ao EPCOT.", "799.90", 1, 100);
        ingresso("Hollywood Studios - 1 dia", hs, "Acesso de 1 dia ao Hollywood Studios.", "829.90", 1, 100);
        ingresso("Animal Kingdom - 1 dia", ak, "Acesso de 1 dia ao Animal Kingdom.", "769.90", 1, 90);
        ingresso("Park Hopper Orlando - 4 dias", mk, "4 dias com troca livre entre os 4 parques de Orlando.", "3299.90", 4, 60);
        ingresso("Disneyland Paris - 2 dias / 2 parques", dlp, "2 dias com acesso aos 2 parques de Paris.", "1499.90", 2, 50);
        ingresso("Disneyland California - 1 dia", dlr, "Acesso de 1 dia ao Disneyland Park.", "949.90", 1, 8);

        pacote("Pacote Orlando Magico - 7 dias", mk, operadora,
                "Aereo ida e volta + 6 noites em hotel Disney + Park Hopper 4 dias + traslados.", "12990.00", 7, 6, true, true, 25);
        pacote("Pacote Familia Encantada - 10 dias", mk, operadora,
                "Aereo + 9 noites + 6 dias de parques + cafe com personagens. Ideal para familias.", "18900.00", 10, 9, true, true, 15);
        pacote("Pacote Paris Encantada - 5 dias", dlp, operadora,
                "Aereo + 4 noites em hotel Disney + 2 dias nos parques de Paris.", "9890.00", 5, 4, true, true, 12);
        pacote("Hospedagem + Ingresso Orlando - 5 dias", epcot, null,
                "4 noites em hotel na area Disney + ingresso 3 dias (sem aereo).", "6490.00", 5, 4, true, false, 20);

        LocalDateTime inicio = LocalDateTime.of(LocalDate.now().getYear(), 11, 1, 0, 0);
        LocalDateTime fim = LocalDateTime.of(LocalDate.now().getYear(), 11, 30, 23, 59, 59);
        if (LocalDateTime.now().isAfter(fim)) { // apresentacao fora de novembro: campanha continua valendo 30 dias
            inicio = LocalDateTime.now().minusDays(1);
            fim = LocalDateTime.now().plusDays(30);
        }
        promocao("Black Friday Magica", "Ate 40% OFF em ingressos e pacotes Disney + 5% extra para familias (4+ itens)",
                "30", AlvoPromocao.INGRESSO, null, 4, "5", inicio, fim);
        promocao("Black Friday Pacotes", "Pacotes de viagem com 40% OFF: aereo + hotel + parques",
                "40", AlvoPromocao.PACOTE, null, 4, "5", inicio, fim);
        promocao("Cupom Black Magic", "Use BLACKMAGIC e ganhe 10% a mais em qualquer produto",
                "10", AlvoPromocao.TODOS, "BLACKMAGIC", 0, "0", inicio, fim);

        cliente("Ana Clara Souza", "52601815906", "797543235", "ana.souza@email.com", "11987654321", "Sao Paulo", "SP");
        cliente("Bruno Henrique Lima", "08301661305", "19487574X", "bruno.lima@email.com", "21998765432", "Rio de Janeiro", "RJ");
        cliente("Carla Mendes Oliveira", "18609139034", "911862523", "carla.mendes@email.com", "41991234567", "Curitiba", "PR");
        log.info("Dados iniciais carregados. Login: admin / admin123 (ou vendedor / vendedor123)");
    }

    private Parque parque(String nome, String local, String descricao, String foto) {
        Parque p = new Parque();
        p.setImagemUrl("img/" + foto + ".jpg");
        p.setNome(nome);
        p.setLocal(local);
        p.setDescricao(descricao);
        return parques.criar(p);
    }

    private Parceiro parceiro(String razao, String fantasia, String cnpj, String ie, String uf, String servico) {
        Parceiro p = new Parceiro();
        p.setRazaoSocial(razao);
        p.setNomeFantasia(fantasia);
        p.setCnpj(cnpj);
        p.setInscricaoEstadual(ie);
        p.setUf(uf);
        p.setTipoServico(servico);
        p.setEmail("contato@" + fantasia.toLowerCase().replaceAll("[^a-z]", "") + ".com.br");
        return parceiros.criar(p);
    }

    private void ingresso(String nome, Parque parque, String desc, String preco, int dias, int estoque) {
        Produto p = new Produto();
        p.setNome(nome);
        p.setCategoria(CategoriaProduto.INGRESSO);
        p.setParque(parque);
        p.setDescricao(desc);
        p.setPreco(new BigDecimal(preco));
        p.setDias(dias);
        p.setEstoque(estoque);
        produtos.criar(p);
    }

    private void pacote(String nome, Parque parque, Parceiro parceiro, String desc, String preco, int dias, int noites,
                        boolean hotel, boolean aereo, int estoque) {
        Produto p = new Produto();
        p.setNome(nome);
        p.setCategoria(CategoriaProduto.PACOTE);
        p.setParque(parque);
        p.setParceiro(parceiro);
        p.setDescricao(desc);
        p.setPreco(new BigDecimal(preco));
        p.setDias(dias);
        p.setNoites(noites);
        p.setIncluiHotel(hotel);
        p.setIncluiAereo(aereo);
        p.setEstoque(estoque);
        produtos.criar(p);
    }

    private void promocao(String nome, String chamada, String pct, AlvoPromocao alvo, String cupom, int qtdMin,
                          String pctProg, LocalDateTime inicio, LocalDateTime fim) {
        Promocao p = new Promocao();
        p.setNome(nome);
        p.setChamada(chamada);
        p.setPercentual(new BigDecimal(pct));
        p.setAlvo(alvo);
        p.setCupom(cupom);
        p.setQtdMinimaProgressivo(qtdMin);
        p.setPercentualProgressivo(new BigDecimal(pctProg));
        p.setInicio(inicio);
        p.setFim(fim);
        promocoes.criar(p);
    }

    private void cliente(String nome, String cpf, String rg, String email, String tel, String cidade, String uf) {
        Cliente c = new Cliente();
        c.setNome(nome);
        c.setCpf(cpf);
        c.setRg(rg);
        c.setEmail(email);
        c.setTelefone(tel);
        c.setCidade(cidade);
        c.setUf(uf);
        c.setDataNascimento(LocalDate.of(1990, 5, 20));
        clientes.criar(c);
    }
}
