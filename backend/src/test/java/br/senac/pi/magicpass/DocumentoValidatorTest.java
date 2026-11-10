package br.senac.pi.magicpass;

import br.senac.pi.magicpass.validation.DocumentoValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DocumentoValidatorTest {

    @Test
    void cpf() {
        assertTrue(DocumentoValidator.cpfValido("526.018.159-06"));
        assertTrue(DocumentoValidator.cpfValido("08301661305"));
        assertFalse(DocumentoValidator.cpfValido("52601815907"));
        assertFalse(DocumentoValidator.cpfValido("11111111111"));
        assertFalse(DocumentoValidator.cpfValido("123"));
        assertFalse(DocumentoValidator.cpfValido(null));
    }

    @Test
    void cnpj() {
        assertTrue(DocumentoValidator.cnpjValido("62.819.482/0001-63"));
        assertTrue(DocumentoValidator.cnpjValido("19935181000176"));
        assertFalse(DocumentoValidator.cnpjValido("19935181000177"));
        assertFalse(DocumentoValidator.cnpjValido("00000000000000"));
        // CNPJ alfanumerico (exemplo oficial da Receita Federal)
        assertTrue(DocumentoValidator.cnpjValido("12.ABC.345/01DE-35"));
        assertFalse(DocumentoValidator.cnpjValido("12.ABC.345/01DE-36"));
    }

    @Test
    void rg() {
        assertTrue(DocumentoValidator.rgValido("79.754.323-5"));
        assertTrue(DocumentoValidator.rgValido("19.487.574-X"));
        assertFalse(DocumentoValidator.rgValido("797543236"));
        assertFalse(DocumentoValidator.rgValido("12345"));
    }

    @Test
    void inscricaoEstadual() {
        assertTrue(DocumentoValidator.ieValida("110.042.490.114", "SP"));
        assertTrue(DocumentoValidator.ieValida("710497465505", "SP"));
        assertFalse(DocumentoValidator.ieValida("710497465506", "SP"));
        assertTrue(DocumentoValidator.ieValida("ISENTO", "MG"));
        assertFalse(DocumentoValidator.ieValida("123", "XX"));
    }
}
