package io.github.ladium1.erp.global.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class DemoRateLimiterTest {

    @Test
    @DisplayName("고정 window 한도 이후 거부하고 다음 window에서 카운터 만료")
    void fixed_window_limit_and_expiry() {
        DemoProperties properties = new DemoProperties();
        properties.getRateLimit().setWindow(Duration.ofMinutes(1));
        MutableClock clock = new MutableClock(Instant.parse("2026-08-02T03:00:00Z"));
        DemoRateLimiter limiter = new DemoRateLimiter(properties, clock);

        assertThat(limiter.tryAcquire("login", "203.0.113.10", 2)).isTrue();
        assertThat(limiter.tryAcquire("login", "203.0.113.10", 2)).isTrue();
        assertThat(limiter.tryAcquire("login", "203.0.113.10", 2)).isFalse();

        clock.advance(Duration.ofSeconds(60));
        assertThat(limiter.tryAcquire("login", "203.0.113.10", 2)).isTrue();
    }

    @Test
    @DisplayName("요청이 없어도 정리 주기에는 만료된 가명 식별자를 제거")
    void scheduled_eviction_removes_idle_identifiers() {
        DemoProperties properties = new DemoProperties();
        MutableClock clock = new MutableClock(Instant.parse("2026-08-02T03:00:00Z"));
        DemoRateLimiter limiter = new DemoRateLimiter(properties, clock);
        assertThat(limiter.tryAcquire("login", "203.0.113.10", 2)).isTrue();
        assertThat(limiter.trackedIdentityCount()).isOne();

        clock.advance(Duration.ofMinutes(1));
        limiter.evictExpired();

        assertThat(limiter.trackedIdentityCount()).isZero();
    }

    @Test
    @DisplayName("추적 key 상한에 도달하면 새 식별자를 fail-closed")
    void bounded_key_space() {
        DemoProperties properties = new DemoProperties();
        properties.getRateLimit().setMaxTrackedKeys(1);
        DemoRateLimiter limiter = new DemoRateLimiter(
                properties,
                Clock.fixed(Instant.parse("2026-08-02T03:00:00Z"), ZoneOffset.UTC));

        assertThat(limiter.tryAcquire("login", "first", 10)).isTrue();
        assertThat(limiter.tryAcquire("login", "second", 10)).isFalse();
    }

    @Test
    @DisplayName("식별자·namespace·limit 계약이 잘못되면 fail-closed")
    void invalid_request_contract_is_rejected() {
        DemoRateLimiter limiter = new DemoRateLimiter(new DemoProperties());

        assertThat(limiter.tryAcquire(null, "identity", 1)).isFalse();
        assertThat(limiter.tryAcquire(" ", "identity", 1)).isFalse();
        assertThat(limiter.tryAcquire("login", null, 1)).isFalse();
        assertThat(limiter.tryAcquire("login", " ", 1)).isFalse();
        assertThat(limiter.tryAcquire("login", "identity", 0)).isFalse();
    }

    @Test
    @DisplayName("동시 신규 식별자 요청에서도 추적 key 상한을 넘지 않음")
    void concurrent_requests_respect_key_cap() throws Exception {
        DemoProperties properties = new DemoProperties();
        properties.getRateLimit().setMaxTrackedKeys(10);
        DemoRateLimiter limiter = new DemoRateLimiter(properties);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(100);
        AtomicInteger allowed = new AtomicInteger();

        try (var executor = Executors.newFixedThreadPool(20)) {
            for (int i = 0; i < 100; i++) {
                int identity = i;
                executor.submit(() -> {
                    try {
                        start.await();
                        if (limiter.tryAcquire("login", "identity-" + identity, 10)) {
                            allowed.incrementAndGet();
                        }
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }
            start.countDown();
            assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();
        }

        assertThat(allowed).hasValue(10);
        assertThat(limiter.trackedIdentityCount()).isEqualTo(10);
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
