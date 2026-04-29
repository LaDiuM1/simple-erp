package io.github.ladium1.erp.salescontact.internal.repository;

import io.github.ladium1.erp.salescontact.internal.entity.AcquisitionSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AcquisitionSourceRepository extends JpaRepository<AcquisitionSource, Long> {

    boolean existsByName(String name);

    List<AcquisitionSource> findAllByNameIn(Collection<String> names);
}
