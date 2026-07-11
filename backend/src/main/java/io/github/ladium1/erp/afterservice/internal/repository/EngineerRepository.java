package io.github.ladium1.erp.afterservice.internal.repository;

import io.github.ladium1.erp.afterservice.internal.entity.Engineer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EngineerRepository extends JpaRepository<Engineer, Long> {

    /** 구분 → 이름 순 — 관리 화면 / 선택 옵션 공용 정렬 */
    List<Engineer> findAllByOrderByTypeAscNameAsc();
}
