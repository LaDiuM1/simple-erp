package io.github.ladium1.erp.global.demo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * byte[] 기반 업로드/다운로드가 동시에 backend heap을 점유하지 않도록 process-local 전송 수를 제한한다.
 * 누적 업로드 한도는 DB에서 별도로 보장하므로 이 객체는 짧은 burst와 OOM 경계만 담당한다.
 */
public final class DemoTransferLimiter {

    private final DemoProperties properties;
    private final Map<String, Integer> activeUploadsByAccount = new HashMap<>();
    private final Map<String, Integer> activeDownloadsByAccount = new HashMap<>();
    private int activeTransfers;

    DemoTransferLimiter(DemoProperties properties) {
        this.properties = properties;
    }

    public synchronized Lease tryAcquireUpload(String identity) {
        if (identity == null || identity.isBlank()) {
            return null;
        }
        int accountTransfers = activeUploadsByAccount.getOrDefault(identity, 0);
        if (activeTransfers >= properties.getUpload().getMaxConcurrentTransfers()
                || accountTransfers >= properties.getUpload().getMaxConcurrentUploadsPerAccount()) {
            return null;
        }

        activeTransfers++;
        activeUploadsByAccount.put(identity, accountTransfers + 1);
        return new Lease(this, identity, null);
    }

    public synchronized Lease tryAcquireDownload(String identity) {
        if (identity == null || identity.isBlank()) {
            return null;
        }
        int accountTransfers = activeDownloadsByAccount.getOrDefault(identity, 0);
        if (activeTransfers >= properties.getUpload().getMaxConcurrentTransfers()
                || accountTransfers >= properties.getUpload().getMaxConcurrentDownloadsPerAccount()) {
            return null;
        }
        activeTransfers++;
        activeDownloadsByAccount.put(identity, accountTransfers + 1);
        return new Lease(this, null, identity);
    }

    synchronized int activeTransferCount() {
        return activeTransfers;
    }

    private synchronized void release(String uploadIdentity, String downloadIdentity) {
        if (activeTransfers <= 0) {
            throw new IllegalStateException("demo transfer permit이 중복 반환되었습니다.");
        }
        activeTransfers--;
        if (uploadIdentity != null) {
            int accountTransfers = activeUploadsByAccount.getOrDefault(uploadIdentity, 0);
            if (accountTransfers <= 1) {
                activeUploadsByAccount.remove(uploadIdentity);
            } else {
                activeUploadsByAccount.put(uploadIdentity, accountTransfers - 1);
            }
        }
        if (downloadIdentity != null) {
            int accountTransfers = activeDownloadsByAccount.getOrDefault(downloadIdentity, 0);
            if (accountTransfers <= 1) {
                activeDownloadsByAccount.remove(downloadIdentity);
            } else {
                activeDownloadsByAccount.put(downloadIdentity, accountTransfers - 1);
            }
        }
    }

    public static final class Lease implements AutoCloseable {
        private final DemoTransferLimiter owner;
        private final String uploadIdentity;
        private final String downloadIdentity;
        private final AtomicBoolean closed = new AtomicBoolean();

        private Lease(
                DemoTransferLimiter owner,
                String uploadIdentity,
                String downloadIdentity
        ) {
            this.owner = owner;
            this.uploadIdentity = uploadIdentity;
            this.downloadIdentity = downloadIdentity;
        }

        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) {
                owner.release(uploadIdentity, downloadIdentity);
            }
        }
    }
}
