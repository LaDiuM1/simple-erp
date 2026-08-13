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
        return tryAcquire(namespace, identity, 1, limit, properties.getRateLimit().getWindow());
    }

    public synchronized boolean tryAcquire(
            String namespace,
            String identity,
            long weight,
            long limit,
            Duration window
    ) {
        if (weight <= 0
                || limit <= 0
                || weight > limit
                || namespace == null
                || namespace.isBlank()
                || identity == null
                || identity.isBlank()
                || window == null
                || window.toSeconds() < 1) {
            return false;
        }

        long now = clock.instant().getEpochSecond();
        long windowSeconds = window.toSeconds();
        long bucket = now / windowSeconds;
        long expiresAt = Math.multiplyExact(Math.addExact(bucket, 1), windowSeconds);
        String key = namespace + ':' + hashIdentity(identity);
        Counter current = counters.get(key);
        if (current == null && counters.size() >= properties.getRateLimit().getMaxTrackedKeys()) {
            removeExpired(now);
            if (counters.size() >= properties.getRateLimit().getMaxTrackedKeys()) {
                return false;
            }
        }

        if (current == null || current.expiresAtEpochSecond() <= now) {
            counters.put(key, new Counter(expiresAt, weight));
        } else if (current.count() > limit - weight) {
            return false;
        } else {
            counters.put(key, new Counter(current.expiresAtEpochSecond(), current.count() + weight));
        }

        if ((++requests & 255) == 0) {
            removeExpired(now);
        }
        return true;
    }

    /** 계정과 global byte budget을 한 임계 구역에서 함께 승인하거나 함께 거절한다. */
    public synchronized boolean tryAcquireBoth(
            String accountNamespace,
            String accountIdentity,
            long accountLimit,
            String globalNamespace,
            String globalIdentity,
            long globalLimit,
            long weight,
            Duration window
    ) {
        if (!validRequest(accountNamespace, accountIdentity, weight, accountLimit, window)
                || !validRequest(globalNamespace, globalIdentity, weight, globalLimit, window)) {
            return false;
        }

        long now = clock.instant().getEpochSecond();
        long windowSeconds = window.toSeconds();
        long bucket = now / windowSeconds;
        long expiresAt = Math.multiplyExact(Math.addExact(bucket, 1), windowSeconds);
        String accountKey = accountNamespace + ':' + hashIdentity(accountIdentity);
        String globalKey = globalNamespace + ':' + hashIdentity(globalIdentity);
        if (accountKey.equals(globalKey)) {
            return false;
        }

        removeExpired(now);
        int newKeys = (counters.containsKey(accountKey) ? 0 : 1)
                + (counters.containsKey(globalKey) ? 0 : 1);
        if (counters.size() > properties.getRateLimit().getMaxTrackedKeys() - newKeys) {
            return false;
        }

        Counter account = counters.get(accountKey);
        Counter global = counters.get(globalKey);
        if (!canAcquire(account, now, weight, accountLimit)
                || !canAcquire(global, now, weight, globalLimit)) {
            return false;
        }

        counters.put(accountKey, incremented(account, now, expiresAt, weight));
        counters.put(globalKey, incremented(global, now, expiresAt, weight));
        requests++;
        return true;
    }

    /** 유휴 시간에도 이전 window의 가명 식별자를 메모리에 남기지 않는다. */
    @Scheduled(fixedDelayString = "${demo.rate-limit.window:PT1M}")
    public synchronized void evictExpired() {
        removeExpired(clock.instant().getEpochSecond());
    }

    synchronized int trackedIdentityCount() {
        return counters.size();
    }

    synchronized void clear() {
        counters.clear();
        requests = 0;
    }

    private void removeExpired(long nowEpochSecond) {
        counters.entrySet().removeIf(entry -> entry.getValue().expiresAtEpochSecond() <= nowEpochSecond);
    }

    private static boolean validRequest(
            String namespace,
            String identity,
            long weight,
            long limit,
            Duration window
    ) {
        return weight > 0
                && limit > 0
                && weight <= limit
                && namespace != null
                && !namespace.isBlank()
                && identity != null
                && !identity.isBlank()
                && window != null
                && window.toSeconds() >= 1;
    }

    private static boolean canAcquire(Counter current, long now, long weight, long limit) {
        return current == null
                || current.expiresAtEpochSecond() <= now
                || current.count() <= limit - weight;
    }

    private static Counter incremented(
            Counter current,
            long now,
            long expiresAt,
            long weight
    ) {
        if (current == null || current.expiresAtEpochSecond() <= now) {
            return new Counter(expiresAt, weight);
        }
        return new Counter(current.expiresAtEpochSecond(), current.count() + weight);
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

    private record Counter(long expiresAtEpochSecond, long count) {
    }
}
