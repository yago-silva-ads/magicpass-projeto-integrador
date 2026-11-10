package br.senac.pi.magicpass.validation;

import java.util.Set;

/**
 * Validacao de documentos brasileiros pelos digitos verificadores.
 * Todos os metodos aceitam o valor com ou sem mascara.
 */
public final class DocumentoValidator {

    public static final Set<String> UFS_IE_SUPORTADAS = Set.of("SP", "RJ", "PR", "SC");

    private DocumentoValidator() {}

    // ---------------------------------------------------------------- CPF
    public static boolean cpfValido(String valor) {
        if (valor == null) return false;
        String cpf = valor.replaceAll("\\D", "");
        if (cpf.length() != 11 || todosIguais(cpf)) return false;
        int[] d = digitos(cpf);
        int dv1 = dvCpf(d, 9);
        int dv2 = dvCpf(d, 10);
        return d[9] == dv1 && d[10] == dv2;
    }

    private static int dvCpf(int[] d, int qtd) {
        int soma = 0;
        for (int i = 0; i < qtd; i++) soma += d[i] * (qtd + 1 - i);
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }

    // ---------------------------------------------------------------- CNPJ
    /** Aceita o CNPJ numerico e o novo CNPJ alfanumerico (Receita Federal, a partir de julho/2026). */
    public static boolean cnpjValido(String valor) {
        if (valor == null) return false;
        String cnpj = valor.toUpperCase().replaceAll("[^0-9A-Z]", "");
        if (!cnpj.matches("^[0-9A-Z]{12}\\d{2}$") || todosIguais(cnpj)) return false;
        int[] v = new int[14];
        for (int i = 0; i < 14; i++) v[i] = cnpj.charAt(i) - '0'; // letras: valor ASCII - 48
        int[] p1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] p2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        return v[12] == mod11(v, p1) && v[13] == mod11(v, p2);
    }

    // ---------------------------------------------------------------- RG (padrao SSP-SP)
    /** RG de SP: 8 digitos + DV (0-9 ou X). Pesos 2..9, DV = 11 - (soma % 11); 10 = X, 11 = 0. */
    public static boolean rgValido(String valor) {
        if (valor == null) return false;
        String rg = valor.toUpperCase().replaceAll("[^0-9X]", "");
        if (!rg.matches("^\\d{8}[0-9X]$") || todosIguais(rg.substring(0, 8))) return false;
        int soma = 0;
        for (int i = 0; i < 8; i++) soma += (rg.charAt(i) - '0') * (i + 2);
        int dv = 11 - (soma % 11);
        char esperado = dv == 10 ? 'X' : (dv == 11 ? '0' : (char) ('0' + dv));
        return rg.charAt(8) == esperado;
    }

    // ---------------------------------------------------------------- Inscricao Estadual
    /** IE validada conforme a UF (SP, RJ, PR, SC). "ISENTO" e aceito para qualquer UF. */
    public static boolean ieValida(String valor, String uf) {
        if (valor == null || uf == null) return false;
        if (valor.trim().equalsIgnoreCase("ISENTO")) return true;
        String ie = valor.replaceAll("\\D", "");
        if (ie.isEmpty() || todosIguais(ie)) return false;
        int[] d = digitos(ie);
        return switch (uf.trim().toUpperCase()) {
            case "SP" -> ieSp(d);
            case "RJ" -> d.length == 8 && d[7] == dvPadrao(d, new int[]{2, 7, 6, 5, 4, 3, 2});
            case "PR" -> d.length == 10
                    && d[8] == dvPadrao(d, new int[]{3, 2, 7, 6, 5, 4, 3, 2})
                    && d[9] == dvPadrao(d, new int[]{4, 3, 2, 7, 6, 5, 4, 3, 2});
            case "SC" -> d.length == 9 && d[8] == dvPadrao(d, new int[]{9, 8, 7, 6, 5, 4, 3, 2});
            default -> false;
        };
    }

    /** SP (comercio/industria): 12 digitos, DV na 9a e na 12a posicao. */
    private static boolean ieSp(int[] d) {
        if (d.length != 12) return false;
        int[] p1 = {1, 3, 4, 5, 6, 7, 8, 10};
        int s1 = 0;
        for (int i = 0; i < 8; i++) s1 += d[i] * p1[i];
        if (d[8] != (s1 % 11) % 10) return false;
        int[] p2 = {3, 2, 10, 9, 8, 7, 6, 5, 4, 3, 2};
        int s2 = 0;
        for (int i = 0; i < 11; i++) s2 += d[i] * p2[i];
        return d[11] == (s2 % 11) % 10;
    }

    // ---------------------------------------------------------------- auxiliares
    private static int mod11(int[] v, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) soma += v[i] * pesos[i];
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static int dvPadrao(int[] d, int[] pesos) {
        return mod11(d, pesos);
    }

    private static int[] digitos(String s) {
        int[] d = new int[s.length()];
        for (int i = 0; i < s.length(); i++) d[i] = s.charAt(i) - '0';
        return d;
    }

    private static boolean todosIguais(String s) {
        return s.chars().distinct().count() == 1;
    }
}
