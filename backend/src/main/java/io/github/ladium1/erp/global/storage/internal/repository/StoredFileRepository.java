package io.github.ladium1.erp.global.storage.internal.repository;

import io.github.ladium1.erp.global.storage.internal.entity.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoredFileRepository extends JpaRepository<StoredFile, Long> {
}
