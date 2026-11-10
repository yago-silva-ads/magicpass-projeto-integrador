package br.senac.pi.magicpass.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** Converte qualquer erro em JSON padronizado. Nenhum erro "cru" (stacktrace) chega ao front. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public record ErroResposta(int status, String erro, String mensagem, Map<String, String> campos,
                               LocalDateTime momento, String caminho) {}

    private ResponseEntity<ErroResposta> resposta(HttpStatus status, String mensagem, Map<String, String> campos,
                                                  HttpServletRequest req) {
        return ResponseEntity.status(status).body(new ErroResposta(status.value(), status.getReasonPhrase(),
                mensagem, campos, LocalDateTime.now(), req.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> validacao(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> campos.putIfAbsent(e.getField(), e.getDefaultMessage()));
        ex.getBindingResult().getGlobalErrors().forEach(e -> campos.putIfAbsent(e.getObjectName(), e.getDefaultMessage()));
        String primeira = campos.values().stream().findFirst().orElse("Dados invalidos");
        return resposta(HttpStatus.BAD_REQUEST, primeira, campos, req);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErroResposta> validacao(ConstraintViolationException ex, HttpServletRequest req) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v -> campos.putIfAbsent(v.getPropertyPath().toString(), v.getMessage()));
        String primeira = campos.values().stream().findFirst().orElse("Dados invalidos");
        return resposta(HttpStatus.BAD_REQUEST, primeira, campos, req);
    }

    @ExceptionHandler(Excecoes.NaoEncontrado.class)
    public ResponseEntity<ErroResposta> naoEncontrado(Excecoes.NaoEncontrado ex, HttpServletRequest req) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage(), null, req);
    }

    @ExceptionHandler(Excecoes.RegraNegocio.class)
    public ResponseEntity<ErroResposta> regra(Excecoes.RegraNegocio ex, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST, ex.getMessage(), null, req);
    }

    @ExceptionHandler(Excecoes.Conflito.class)
    public ResponseEntity<ErroResposta> conflito(Excecoes.Conflito ex, HttpServletRequest req) {
        return resposta(HttpStatus.CONFLICT, ex.getMessage(), null, req);
    }

    @ExceptionHandler(Excecoes.NaoAutenticado.class)
    public ResponseEntity<ErroResposta> naoAutenticado(Excecoes.NaoAutenticado ex, HttpServletRequest req) {
        return resposta(HttpStatus.UNAUTHORIZED, ex.getMessage(), null, req);
    }

    @ExceptionHandler(Excecoes.AcessoNegado.class)
    public ResponseEntity<ErroResposta> acessoNegado(Excecoes.AcessoNegado ex, HttpServletRequest req) {
        return resposta(HttpStatus.FORBIDDEN, ex.getMessage(), null, req);
    }

    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public ResponseEntity<ErroResposta> estadoInvalido(RuntimeException ex, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST, ex.getMessage(), null, req);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> jsonInvalido(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST, "Dados em formato invalido. Confira numeros, datas e opcoes selecionadas.", null, req);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResposta> tipoInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST, "Parametro '" + ex.getName() + "' invalido: " + ex.getValue(), null, req);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResposta> integridade(DataIntegrityViolationException ex, HttpServletRequest req) {
        return resposta(HttpStatus.CONFLICT, "Operacao nao permitida: o registro esta duplicado ou em uso por outro cadastro.", null, req);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResposta> metodo(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return resposta(HttpStatus.METHOD_NOT_ALLOWED, "Metodo nao suportado nesta rota", null, req);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResposta> rota(NoResourceFoundException ex, HttpServletRequest req) {
        return resposta(HttpStatus.NOT_FOUND, "Rota nao encontrada", null, req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> geral(Exception ex, HttpServletRequest req) {
        log.error("Erro inesperado em {}", req.getRequestURI(), ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado. Tente novamente.", null, req);
    }
}
