package io.github.ladium1.erp.global.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DemoTransferLimiterTest {

    @Test
    @DisplayName("upload와 download가 공유하는 global 2개 permit을 넘지 않음")
    void upload_and_download_share_global_capacity() {
        DemoTransferLimiter limiter = new DemoTransferLimiter(new DemoProperties());

        DemoTransferLimiter.Lease upload = limiter.tryAcquireUpload("demo.staff");
        DemoTransferLimiter.Lease download = limiter.tryAcquireDownload("demo.manager");

        assertThat(upload).isNotNull();
        assertThat(download).isNotNull();
        assertThat(limiter.tryAcquireDownload("demo.staff")).isNull();
        assertThat(limiter.activeTransferCount()).isEqualTo(2);

        download.close();
        assertThat(limiter.tryAcquireDownload("demo.manager")).isNotNull()
                .satisfies(DemoTransferLimiter.Lease::close);
        upload.close();
        assertThat(limiter.activeTransferCount()).isZero();
    }

    @Test
    @DisplayName("같은 계정은 upload 한 건만 진행하고 lease 반환은 멱등")
    void account_upload_capacity_and_release_are_safe() {
        DemoTransferLimiter limiter = new DemoTransferLimiter(new DemoProperties());

        DemoTransferLimiter.Lease first = limiter.tryAcquireUpload("demo.staff");
        assertThat(first).isNotNull();
        assertThat(limiter.tryAcquireUpload("demo.staff")).isNull();
        assertThat(limiter.tryAcquireUpload("demo.manager")).isNotNull()
                .satisfies(DemoTransferLimiter.Lease::close);

        first.close();
        first.close();
        assertThat(limiter.activeTransferCount()).isZero();
        assertThat(limiter.tryAcquireUpload("demo.staff")).isNotNull()
                .satisfies(DemoTransferLimiter.Lease::close);
    }

    @Test
    @DisplayName("같은 계정 download는 두 건까지만 진행")
    void account_download_capacity_is_bounded() {
        DemoProperties properties = new DemoProperties();
        properties.getUpload().setMaxConcurrentTransfers(3);
        DemoTransferLimiter limiter = new DemoTransferLimiter(properties);

        DemoTransferLimiter.Lease first = limiter.tryAcquireDownload("demo.staff");
        DemoTransferLimiter.Lease second = limiter.tryAcquireDownload("demo.staff");
        assertThat(first).isNotNull();
        assertThat(second).isNotNull();
        assertThat(limiter.tryAcquireDownload("demo.staff")).isNull();
        assertThat(limiter.tryAcquireDownload("demo.manager")).isNotNull()
                .satisfies(DemoTransferLimiter.Lease::close);

        first.close();
        second.close();
        assertThat(limiter.activeTransferCount()).isZero();
    }
}
