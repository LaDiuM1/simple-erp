package io.github.ladium1.erp.global.storage.internal.repository;

import io.github.ladium1.erp.global.storage.internal.entity.StoredFile;
import io.github.ladium1.erp.global.storage.internal.entity.StoredFileStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface StoredFileRepository extends JpaRepository<StoredFile, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from StoredFile f where f.id in :fileIds order by f.id")
    List<StoredFile> findAllByIdForUpdate(Collection<Long> fileIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select f from StoredFile f
            where f.status = :status and f.createdAt < :createdBefore
            order by f.id
            """)
    List<StoredFile> findCreatedBeforeForUpdate(
            StoredFileStatus status,
            LocalDateTime createdBefore,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from StoredFile f where f.status = :status order by f.id")
    List<StoredFile> findByStatusForUpdate(StoredFileStatus status, Pageable pageable);
}
