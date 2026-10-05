package org.example.firstpetprojectexchanger.service;

public class IdempotencyService {
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