package io.github.ladium1.erp.global.demo;

import java.util.concurrent.atomic.AtomicBoolean;

/** 공개 데모의 조회·미리보기 작업이 backend CPU/heap을 동시에 독점하지 않게 한다. */
public final class DemoRequestConcurrencyLimiter {

    private final DemoProperties properties;
    private int activeReads;
    private int activePreviews;
    private int activeIngress;
    private int activeWrites;

    DemoRequestConcurrencyLimiter(DemoProperties properties) {
        this.properties = properties;
    }

    public synchronized Lease tryAcquireRead() {
        if (activeReads >= properties.getRateLimit().getMaxConcurrentReads()) {
            return null;
        }
        activeReads++;
        return new Lease(this, Kind.READ);
    }

    public synchronized Lease tryAcquirePreview() {
        if (activePreviews >= properties.getRateLimit().getMaxConcurrentPreviews()) {
            return null;
        }
        activePreviews++;
        return new Lease(this, Kind.PREVIEW);
    }

    public synchronized Lease tryAcquireIngress() {
        if (activeIngress >= properties.getRateLimit().getMaxConcurrentIngress()) {
            return null;
        }
        activeIngress++;
        return new Lease(this, Kind.INGRESS);
    }

    public synchronized Lease tryAcquireWrite() {
        if (activeWrites >= properties.getRateLimit().getMaxConcurrentWrites()) {
            return null;
        }
        activeWrites++;
        return new Lease(this, Kind.WRITE);
    }

    synchronized int activeReadCount() {
        return activeReads;
    }

    synchronized int activePreviewCount() {
        return activePreviews;
    }

    synchronized int activeIngressCount() {
        return activeIngress;
    }

    synchronized int activeWriteCount() {
        return activeWrites;
    }

    private synchronized void release(Kind kind) {
        if (kind == Kind.READ) {
            if (activeReads <= 0) {
                throw new IllegalStateException("demo read permit이 중복 반환되었습니다.");
            }
            activeReads--;
            return;
        }
        if (kind == Kind.PREVIEW) {
            if (activePreviews <= 0) {
                throw new IllegalStateException("demo preview permit이 중복 반환되었습니다.");
            }
            activePreviews--;
            return;
        }
        if (kind == Kind.INGRESS) {
            if (activeIngress <= 0) {
                throw new IllegalStateException("demo ingress permit이 중복 반환되었습니다.");
            }
            activeIngress--;
            return;
        }
        if (activeWrites <= 0) {
            throw new IllegalStateException("demo write permit이 중복 반환되었습니다.");
        }
        activeWrites--;
    }

    private enum Kind {
        READ,
        PREVIEW,
        INGRESS,
        WRITE
    }

    public static final class Lease implements AutoCloseable {
        private final DemoRequestConcurrencyLimiter owner;
        private final Kind kind;
        private final AtomicBoolean closed = new AtomicBoolean();

        private Lease(DemoRequestConcurrencyLimiter owner, Kind kind) {
            this.owner = owner;
            this.kind = kind;
        }

        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) {
                owner.release(kind);
            }
        }
    }
}
