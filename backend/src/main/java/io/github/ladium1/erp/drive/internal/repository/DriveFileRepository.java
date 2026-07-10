package io.github.ladium1.erp.drive.internal.repository;

import io.github.ladium1.erp.drive.internal.entity.DriveFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriveFileRepository extends JpaRepository<DriveFile, Long> {

    List<DriveFile> findAllByFolderId(Long folderId);

    List<DriveFile> findAllByFolderIsNull();

    boolean existsByFolderId(Long folderId);
}
