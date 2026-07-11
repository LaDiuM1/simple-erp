package io.github.ladium1.erp.drive.internal.repository;

import io.github.ladium1.erp.drive.internal.entity.DriveFolder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriveFolderRepository extends JpaRepository<DriveFolder, Long> {

    List<DriveFolder> findAllByParentId(Long parentId);

    List<DriveFolder> findAllByParentIdIsNull();

    boolean existsByParentId(Long parentId);

    boolean existsByParentIdAndName(Long parentId, String name);

    boolean existsByParentIdIsNullAndName(String name);
}
