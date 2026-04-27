package io.github.ladium1.erp.salescontact.internal.repository;

import io.github.ladium1.erp.salescontact.internal.entity.SalesContactSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SalesContactSourceRepository extends JpaRepository<SalesContactSource, Long> {

    List<SalesContactSource> findByContactId(Long contactId);

    List<SalesContactSource> findByContactIdIn(Collection<Long> contactIds);

    void deleteByContactId(Long contactId);

    /** 컨택 경로 마스터 삭제 시 정션 row 정리. */
    void deleteBySourceId(Long sourceId);
}
