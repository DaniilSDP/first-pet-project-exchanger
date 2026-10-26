package org.example.firstpetprojectexchanger.service;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

import org.example.firstpetprojectexchanger.config.LedgerProperties;
import org.example.firstpetprojectexchanger.exception.ConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;


@Service
public class IdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyService.class);
    private static final String KEY_PREFIX = "idem:";
    private static final String PENDING = "pending";
    private static final int PENDING_WAIT_MS = 3000;
    private static final int PENDING_POLL_MS = 100;

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration ttl;
    private final boolean useRedis;

    public IdempotencyService(RedisTemplate<String, Object> redisTemplate,
                              ObjectMapper objectMapper,
                              LedgerProperties properties) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.ttl = properties.idempotency().ttl();
        this.useRedis = true;
    }

    private record CachedResponse(int status, String body) {
    }

    
    public ResponseEntity<?> execute(Long userId, String idempotencyKey, Supplier<ResponseEntity<?>> supplier) {
        if (idempotencyKey == null || !useRedis) {
            return supplier.get();
        }
        String key = key(userId, idempotencyKey);

        Optional<CachedResponse> cached = read(key);
        if (cached.isPresent() && cached.get().status() != 0) {
            return reconstruct(cached.get());
        }

        Boolean claimed = redisTemplate.opsForValue().setIfAbsent(key, PENDING, ttl);
        if (!Boolean.TRUE.equals(claimed)) {
            // A concurrent duplicate is executing — wait for it to finish.
            CachedResponse result = waitForResult(key).orElseThrow(
                    () -> new ConflictException("A request with this Idempotency-Key is already in progress"));
            return reconstruct(result);
        }

        try {
            ResponseEntity<?> response = supplier.get();
            store(key, new CachedResponse(response.getStatusCode().value(), bodyOf(response)));
            return response;
        } catch (RuntimeException ex) {
            
            redisTemplate.delete(key);
            throw ex;
        }
    }

    private Optional<CachedResponse> read(String key) {
        Object raw = redisTemplate.opsForValue().get(key);
        if (raw == null) {
            return Optional.empty();
        }
        String value = String.valueOf(raw);
        if (PENDING.equals(value)) {
            return Optional.of(new CachedResponse(0, null));
        }
        return Optional.of(deserialize(value));
    }

    private Optional<CachedResponse> waitForResult(String key) {
        long deadline = System.currentTimeMillis() + PENDING_WAIT_MS;
        while (System.currentTimeMillis() < deadline) {
            Optional<CachedResponse> cached = read(key);
            if (cached.isPresent() && cached.get().status() != 0) {
                return cached;
            }
            try {
                Thread.sleep(PENDING_POLL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private ResponseEntity<?> reconstruct(CachedResponse cached) {
        return ResponseEntity.status(cached.status()).body(deserializeBody(cached.body()));
    }

    private String bodyOf(ResponseEntity<?> response) {
        try {
            return objectMapper.writeValueAsString(response.getBody());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void store(String key, CachedResponse cached) {
        try {
            redisTemplate.opsForValue().set(key, serialize(cached), ttl);
        } catch (Exception e) {
            log.warn("Failed to cache idempotency response", e);
        }
    }

    private String serialize(CachedResponse cached) {
        try {
            return objectMapper.writeValueAsString(cached);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private CachedResponse deserialize(String raw) {
        try {
            return objectMapper.readValue(raw, CachedResponse.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse cached idempotency response", e);
        }
    }

    private Object deserializeBody(String body) {
        try {
            return body == null ? null : objectMapper.readValue(body, Object.class);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private String key(Long userId, String idempotencyKey) {
        return KEY_PREFIX + userId + ":" + idempotencyKey;
    }
}
/*
IdempotencyService

-redisTemplate: RedisTemplate<String,Object>
-objectMapper: ObjectMapper
-ttl: Duration
-useRedis: boolean
-KEY_PREFIX: String = "idem:"
-PENDING: String = "pending"
-PENDING_WAIT_MS: int = 3000
-PENDING_POLL_MS: int = 100

+execute(Long, String, Supplier<ResponseEntity<?>>): ResponseEntity<?>

-read(String): Optional<CachedResponse>
-waitForResult(String): Optional<CachedResponse>
-reconstruct(CachedResponse): ResponseEntity<?>
-bodyOf(ResponseEntity<?>): String
-store(String, CachedResponse): void
-serialize(CachedResponse): String
-deserialize(String): CachedResponse
-deserializeBody(String): Object
-key(Long, String): String
 */