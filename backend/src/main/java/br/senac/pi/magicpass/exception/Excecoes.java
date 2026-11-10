package br.senac.pi.magicpass.exception;

/** Excecoes de negocio da aplicacao (cada uma vira um status HTTP no GlobalExceptionHandler). */
public final class Excecoes {

    private Excecoes() {}

    /** 404 - registro nao existe (inclui ids invalidos como 0 ou -1). */
    public static class NaoEncontrado extends RuntimeException {
        public NaoEncontrado(String mensagem) { super(mensagem); }
    }

    /** 400 - regra de negocio violada (estoque, cupom, IE etc.). */
    public static class RegraNegocio extends RuntimeException {
        public RegraNegocio(String mensagem) { super(mensagem); }
    }

    /** 409 - conflito (duplicidade, registro em uso). */
    public static class Conflito extends RuntimeException {
        public Conflito(String mensagem) { super(mensagem); }
    }

    /** 401 - sem login ou sessao expirada. */
    public static class NaoAutenticado extends RuntimeException {
        public NaoAutenticado(String mensagem) { super(mensagem); }
    }

    /** 403 - logado, mas sem permissao. */
    public static class AcessoNegado extends RuntimeException {
        public AcessoNegado(String mensagem) { super(mensagem); }
    }
}
