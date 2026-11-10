package br.senac.pi.magicpass;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Testa a API de verdade (com os dados iniciais), incluindo os casos de erro. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:magicpass_test;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ApiCrudTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    String admin;

    @BeforeEach
    void login() throws Exception {
        admin = token("admin", "admin123");
    }

    String token(String login, String senha) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"" + login + "\",\"senha\":\"" + senha + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("token").asText();
    }

    MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder b, String token) {
        return b.header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON);
    }

    long criarId(String url, String corpo) throws Exception {
        String body = mvc.perform(auth(post(url), admin).content(corpo))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("id").asLong();
    }

    @Test
    void loginInvalidoESemToken() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"admin\",\"senha\":\"errada\"}")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/clientes")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem", containsString("login")));
        mvc.perform(get("/api/publico/vitrine")).andExpect(status().isOk())
                .andExpect(jsonPath("$.ofertas", not(empty())))
                .andExpect(jsonPath("$.campanha.nome", notNullValue()));
    }

    @Test
    void crudCompletoDeCliente() throws Exception {
        long id = criarId("/api/clientes", """
                {"nome":"Diego Teste","cpf":"996.030.824-30","rg":"91.186.252-3","email":"diego@x.com","telefone":"(11) 91234-5678","uf":"SP"}""");
        mvc.perform(auth(get("/api/clientes/" + id), admin)).andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("99603082430"));
        mvc.perform(auth(put("/api/clientes/" + id), admin).content("""
                {"nome":"Diego Alterado","cpf":"99603082430","rg":"911862523","email":"diego@x.com"}"""))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Diego Alterado"))
                .andExpect(jsonPath("$.criadoEm", notNullValue()));
        mvc.perform(auth(delete("/api/clientes/" + id), admin)).andExpect(status().isNoContent());
        mvc.perform(auth(get("/api/clientes/" + id), admin)).andExpect(status().isNotFound());
    }

    @Test
    void idsInvalidosNuncaQuebram() throws Exception {
        for (String id : new String[]{"-1", "0", "999999"}) {
            mvc.perform(auth(get("/api/clientes/" + id), admin)).andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.mensagem", notNullValue()));
            mvc.perform(auth(delete("/api/produtos/" + id), admin)).andExpect(status().isNotFound());
            mvc.perform(auth(put("/api/parques/" + id), admin).content("{\"nome\":\"X\",\"local\":\"Y\"}"))
                    .andExpect(status().isNotFound());
        }
        mvc.perform(auth(get("/api/clientes/abc"), admin)).andExpect(status().isBadRequest());
    }

    @Test
    void documentosInvalidos() throws Exception {
        mvc.perform(auth(post("/api/clientes"), admin).content("""
                {"nome":"Fulano","cpf":"111.111.111-11","rg":"797543236","email":"f@x.com"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.cpf", containsString("CPF")))
                .andExpect(jsonPath("$.campos.rg", containsString("RG")));
        mvc.perform(auth(post("/api/parceiros"), admin).content("""
                {"razaoSocial":"X","cnpj":"90937865000171","inscricaoEstadual":"710497465506","uf":"SP","tipoServico":"Hotel"}"""))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.mensagem", containsString("Inscricao estadual")));
        mvc.perform(auth(post("/api/parceiros"), admin).content("""
                {"razaoSocial":"X","cnpj":"90937865000172","inscricaoEstadual":"ISENTO","uf":"SP","tipoServico":"Hotel"}"""))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.cnpj", notNullValue()));
    }

    @Test
    void valoresNegativosSaoRecusados() throws Exception {
        mvc.perform(auth(post("/api/produtos"), admin).content("""
                {"nome":"Ruim","categoria":"INGRESSO","parque":{"id":1},"preco":-1,"dias":1,"estoque":-1}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.preco", notNullValue()))
                .andExpect(jsonPath("$.campos.estoque", notNullValue()));
        mvc.perform(auth(post("/api/produtos"), admin).content("""
                {"nome":"Sem parque","categoria":"INGRESSO","parque":{"id":-1},"preco":10,"dias":1,"estoque":1}"""))
                .andExpect(status().isNotFound());
    }

    @Test
    void vendaComBlackFridayEstoqueECancelamento() throws Exception {
        String data = LocalDate.now().plusDays(30).toString();
        // produto 7 = Disneyland California (estoque 8)
        String estoqueAntes = mvc.perform(auth(get("/api/produtos/7"), admin)).andReturn().getResponse().getContentAsString();
        int antes = json.readTree(estoqueAntes).get("estoque").asInt();

        String venda = """
                {"clienteId":1,"formaPagamento":"PIX","parcelas":1,"cupom":"blackmagic",
                 "itens":[{"produtoId":7,"quantidade":4,"dataVisita":"%s"}]}""".formatted(data);
        JsonNode cot = json.readTree(mvc.perform(auth(post("/api/vendas/cotacao"), admin).content(venda))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        // 30% Black + 5% familia + 10% cupom = 45%, depois 5% PIX
        org.junit.jupiter.api.Assertions.assertEquals(0, cot.get("itens").get(0).get("percentualDesconto").decimalValue()
                .compareTo(new java.math.BigDecimal("45")));
        org.junit.jupiter.api.Assertions.assertTrue(cot.get("total").decimalValue().compareTo(cot.get("subtotal").decimalValue()) < 0);

        long id = json.readTree(mvc.perform(auth(post("/api/vendas"), admin).content(venda))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asLong();
        mvc.perform(auth(get("/api/produtos/7"), admin)).andExpect(jsonPath("$.estoque").value(antes - 4));

        // nao deixa vender mais do que o estoque
        mvc.perform(auth(post("/api/vendas"), admin).content(venda.replace("\"quantidade\":4", "\"quantidade\":20")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.mensagem", containsString("Estoque insuficiente")));
        mvc.perform(auth(get("/api/produtos/7"), admin)).andExpect(jsonPath("$.estoque").value(antes - 4));

        // quantidade zero/negativa
        mvc.perform(auth(post("/api/vendas"), admin).content(venda.replace("\"quantidade\":4", "\"quantidade\":-1")))
                .andExpect(status().isBadRequest());

        // alterar venda (troca quantidade) ajusta o estoque
        mvc.perform(auth(put("/api/vendas/" + id), admin).content(venda.replace("\"quantidade\":4", "\"quantidade\":2")))
                .andExpect(status().isOk());
        mvc.perform(auth(get("/api/produtos/7"), admin)).andExpect(jsonPath("$.estoque").value(antes - 2));

        // produto vendido nao pode ser apagado; cliente com venda tambem nao
        mvc.perform(auth(delete("/api/produtos/7"), admin)).andExpect(status().isConflict());
        mvc.perform(auth(delete("/api/clientes/1"), admin)).andExpect(status().isConflict());

        mvc.perform(auth(put("/api/vendas/" + id + "/cancelar"), admin)).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));
        mvc.perform(auth(get("/api/produtos/7"), admin)).andExpect(jsonPath("$.estoque").value(antes));
        mvc.perform(auth(put("/api/vendas/" + id + "/cancelar"), admin)).andExpect(status().isBadRequest());
        mvc.perform(auth(delete("/api/vendas/" + id), admin)).andExpect(status().isNoContent());
        mvc.perform(auth(get("/api/produtos/7"), admin)).andExpect(jsonPath("$.estoque").value(antes));

        mvc.perform(auth(post("/api/vendas/cotacao"), admin).content(venda.replace("blackmagic", "NAOEXISTE")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.mensagem", containsString("Cupom")));
    }

    @Test
    void usuariosSoAdminESenhaNaoVaza() throws Exception {
        String vendedor = token("vendedor", "vendedor123");
        mvc.perform(auth(get("/api/usuarios"), vendedor)).andExpect(status().isForbidden());
        mvc.perform(auth(post("/api/parques"), vendedor).content("{\"nome\":\"X\",\"local\":\"Y\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(auth(get("/api/parques"), vendedor)).andExpect(status().isOk());

        long id = criarId("/api/usuarios", "{\"nome\":\"Novo\",\"login\":\"novo.user\",\"senha\":\"segredo1\",\"perfil\":\"VENDEDOR\"}");
        mvc.perform(auth(get("/api/usuarios/" + id), admin)).andExpect(jsonPath("$.senha").doesNotExist());
        token("novo.user", "segredo1");
        // altera sem informar senha: senha antiga continua valendo
        mvc.perform(auth(put("/api/usuarios/" + id), admin)
                .content("{\"nome\":\"Novo 2\",\"login\":\"novo.user\",\"senha\":\"\",\"perfil\":\"VENDEDOR\",\"ativo\":true}"))
                .andExpect(status().isOk());
        token("novo.user", "segredo1");
        mvc.perform(auth(post("/api/usuarios"), admin)
                .content("{\"nome\":\"Dup\",\"login\":\"novo.user\",\"senha\":\"segredo1\",\"perfil\":\"VENDEDOR\"}"))
                .andExpect(status().isConflict());
        // ultimo admin protegido
        mvc.perform(auth(delete("/api/usuarios/1"), admin)).andExpect(status().isBadRequest());
        mvc.perform(auth(delete("/api/usuarios/" + id), admin)).andExpect(status().isNoContent());
    }

    @Test
    void promocaoComPeriodoInvalido() throws Exception {
        mvc.perform(auth(post("/api/promocoes"), admin).content("""
                {"nome":"Errada","percentual":90,"inicio":"2026-12-10T00:00:00","fim":"2026-12-01T00:00:00","alvo":"TODOS"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.percentual", notNullValue()))
                .andExpect(jsonPath("$.campos.periodoValido", notNullValue()));
    }
}
