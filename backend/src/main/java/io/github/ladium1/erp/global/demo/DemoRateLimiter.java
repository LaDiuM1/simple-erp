package io.github.ladium1.erp.global.demo;

import org.springframework.scheduling.annotation.Scheduled;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

/** 단일 인스턴스 데모용 고정 윈도 rate limiter. 식별자는 process-salted hash로만 보관한다. */
public class DemoRateLimiter {

    private final DemoProperties properties;
    private final Clock clock;
    private final byte[] identitySalt;
    private final Map<String, Counter> counters = new HashMap<>();
    private long requests;

    public DemoRateLimiter(DemoProperties properties) {
        this(properties, Clock.systemUTC());
    }

    DemoRateLimiter(DemoProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
        this.identitySalt = createIdentitySalt();
    }

    public synchronized boolean tryAcquire(String namespace, String identity, int limit) {
        if (limit <= 0
                || namespace == null
                || namespace.isBlank()
                || identity == null
                || identity.isBlank()) {
            return false;
        }

        long bucket = currentBucket(properties.getRateLimit().getWindow());
        String key = namespace + ':' + hashIdentity(identity);
        Counter current = counters.get(key);
        if (current == null && counters.size() >= properties.getRateLimit().getMaxTrackedKeys()) {
            removeExpired(bucket);
            if (counters.size() >= properties.getRateLimit().getMaxTrackedKeys()) {
                return false;
            }
        }

        if (current == null || current.bucket() != bucket) {
            counters.put(key, new Counter(bucket, 1));
        } else if (current.count() >= limit) {
            return false;
        } else {
            counters.put(key, new Counter(bucket, current.count() + 1));
        }

        if ((++requests & 255) == 0) {
            removeExpired(bucket);
        }
        return true;
    }

    /** 유휴 시간에도 이전 window의 가명 식별자를 메모리에 남기지 않는다. */
    @Scheduled(fixedDelayString = "${demo.rate-limit.window:PT1M}")
    public synchronized void evictExpired() {
        removeExpired(currentBucket(properties.getRateLimit().getWindow()));
    }

    synchronized int trackedIdentityCount() {
        return counters.size();
    }

    private long currentBucket(Duration window) {
        long seconds = Math.max(1, window.toSeconds());
        return clock.instant().getEpochSecond() / seconds;
    }

    private void removeExpired(long currentBucket) {
        counters.entrySet().removeIf(entry -> entry.getValue().bucket() < currentBucket);
    }

    private String hashIdentity(String identity) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(identitySalt);
            return HexFormat.of().formatHex(digest.digest(identity.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", impossible);
        }
    }

    private static byte[] createIdentitySalt() {
        byte[] salt = new byte[32];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    private record Counter(long bucket, int count) {
    }
}
