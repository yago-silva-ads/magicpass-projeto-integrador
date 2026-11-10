package br.senac.pi.magicpass.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Sessoes de login por token (Bearer). Expiram apos 8 horas. */
@Service
public class SessaoService {

    private static final Duration VALIDADE = Duration.ofHours(8);
    private final SecureRandom random = new SecureRandom();
    private final Map<String, Sessao> sessoes = new ConcurrentHashMap<>();

    private record Sessao(Long usuarioId, Instant expiraEm) {}

    public String abrir(Long usuarioId) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sessoes.put(token, new Sessao(usuarioId, Instant.now().plus(VALIDADE)));
        return token;
    }

    public Optional<Long> usuarioDoToken(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        Sessao s = sessoes.get(token);
        if (s == null) return Optional.empty();
        if (Instant.now().isAfter(s.expiraEm())) {
            sessoes.remove(token);
            return Optional.empty();
        }
        return Optional.of(s.usuarioId());
    }

    public void encerrar(String token) {
        if (token != null) sessoes.remove(token);
    }

    public void encerrarDoUsuario(Long usuarioId) {
        sessoes.values().removeIf(s -> s.usuarioId().equals(usuarioId));
    }
}
