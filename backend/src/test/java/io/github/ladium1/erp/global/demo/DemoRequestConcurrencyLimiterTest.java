package io.github.ladium1.erp.global.demo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DemoRequestConcurrencyLimiterTest {

    @Test
    void reads_have_a_global_limit_and_release_is_idempotent() {
        DemoProperties properties = new DemoProperties();
        properties.getRateLimit().setMaxConcurrentReads(2);
        DemoRequestConcurrencyLimiter limiter = new DemoRequestConcurrencyLimiter(properties);

        DemoRequestConcurrencyLimiter.Lease first = limiter.tryAcquireRead();
        DemoRequestConcurrencyLimiter.Lease second = limiter.tryAcquireRead();
        assertThat(first).isNotNull();
        assertThat(second).isNotNull();
        assertThat(limiter.tryAcquireRead()).isNull();

        first.close();
        first.close();
        assertThat(limiter.tryAcquireRead()).isNotNull()
                .satisfies(DemoRequestConcurrencyLimiter.Lease::close);
        second.close();
        assertThat(limiter.activeReadCount()).isZero();
    }

    @Test
    void previews_use_their_own_smaller_global_limit() {
        DemoProperties properties = new DemoProperties();
        DemoRequestConcurrencyLimiter limiter = new DemoRequestConcurrencyLimiter(properties);

        DemoRequestConcurrencyLimiter.Lease previewOne = limiter.tryAcquirePreview();
        DemoRequestConcurrencyLimiter.Lease previewTwo = limiter.tryAcquirePreview();
        DemoRequestConcurrencyLimiter.Lease read = limiter.tryAcquireRead();
        assertThat(previewOne).isNotNull();
        assertThat(previewTwo).isNotNull();
        assertThat(read).isNotNull();
        assertThat(limiter.tryAcquirePreview()).isNull();

        previewOne.close();
        assertThat(limiter.tryAcquirePreview()).isNotNull()
                .satisfies(DemoRequestConcurrencyLimiter.Lease::close);
        previewTwo.close();
        read.close();
        assertThat(limiter.activePreviewCount()).isZero();
        assertThat(limiter.activeReadCount()).isZero();
    }

    @Test
    void ingress_and_write_have_independent_global_limits() {
        DemoProperties properties = new DemoProperties();
        properties.getRateLimit().setMaxConcurrentIngress(2);
        properties.getRateLimit().setMaxConcurrentWrites(1);
        DemoRequestConcurrencyLimiter limiter = new DemoRequestConcurrencyLimiter(properties);

        DemoRequestConcurrencyLimiter.Lease ingressOne = limiter.tryAcquireIngress();
        DemoRequestConcurrencyLimiter.Lease ingressTwo = limiter.tryAcquireIngress();
        DemoRequestConcurrencyLimiter.Lease write = limiter.tryAcquireWrite();
        assertThat(ingressOne).isNotNull();
        assertThat(ingressTwo).isNotNull();
        assertThat(write).isNotNull();
        assertThat(limiter.tryAcquireIngress()).isNull();
        assertThat(limiter.tryAcquireWrite()).isNull();

        ingressOne.close();
        ingressTwo.close();
        write.close();
        assertThat(limiter.activeIngressCount()).isZero();
        assertThat(limiter.activeWriteCount()).isZero();
    }
}
