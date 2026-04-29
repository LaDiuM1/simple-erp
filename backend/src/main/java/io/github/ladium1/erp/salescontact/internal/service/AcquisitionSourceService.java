package io.github.ladium1.erp.salescontact.internal.service;

import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.salescontact.internal.dto.AcquisitionSourceCreateRequest;
import io.github.ladium1.erp.salescontact.internal.dto.AcquisitionSourceInfo;
import io.github.ladium1.erp.salescontact.internal.entity.AcquisitionSource;
import io.github.ladium1.erp.salescontact.internal.exception.AcquisitionSourceErrorCode;
import io.github.ladium1.erp.salescontact.internal.mapper.AcquisitionSourceMapper;
import io.github.ladium1.erp.salescontact.internal.repository.AcquisitionSourceRepository;
import io.github.ladium1.erp.salescontact.internal.repository.SalesContactSourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 컨택 경로 sub-master CRUD. salescontact 모듈 내부 — 외부 노출 없음.
 * 영업 명부 목록 페이지의 보조 모달이 호출.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AcquisitionSourceService {

    private final AcquisitionSourceRepository sourceRepository;
    private final SalesContactSourceRepository contactSourceRepository;
    private final AcquisitionSourceMapper sourceMapper;

    public List<AcquisitionSourceInfo> findAll() {
        return sourceMapper.toInfos(
                sourceRepository.findAll(Sort.by("type").ascending().and(Sort.by("name").ascending()))
        );
    }

    public List<AcquisitionSourceInfo> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return sourceMapper.toInfos(sourceRepository.findAllById(ids));
    }

    public List<AcquisitionSourceInfo> findByNames(List<String> names) {
        if (names == null || names.isEmpty()) {
            return List.of();
        }
        return sourceMapper.toInfos(sourceRepository.findAllByNameIn(names));
    }

    /**
     * 누락된 id 가 있으면 SOURCE_NOT_FOUND 로 실패.
     */
    public void validateIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        Set<Long> distinct = new HashSet<>(ids);
        long found = sourceRepository.findAllById(distinct).size();
        if (found != distinct.size()) {
            throw new BusinessException(AcquisitionSourceErrorCode.SOURCE_NOT_FOUND);
        }
    }

    @Transactional
    public Long create(AcquisitionSourceCreateRequest request) {
        String name = request.name().trim();
        if (sourceRepository.existsByName(name)) {
            throw new BusinessException(AcquisitionSourceErrorCode.DUPLICATE_NAME);
        }
        AcquisitionSource source = AcquisitionSource.builder()
                .name(name)
                .type(request.type())
                .description(trimToNull(request.description()))
                .build();
        return sourceRepository.save(source).getId();
    }

    /**
     * 마스터 삭제 + 참조하는 영업 명부 정션도 함께 정리.
     * 텍스트 마스터라 명부에서 이미 사용 중이어도 큰 영향 없는 정책.
     */
    @Transactional
    public void delete(Long id) {
        if (!sourceRepository.existsById(id)) {
            throw new BusinessException(AcquisitionSourceErrorCode.SOURCE_NOT_FOUND);
        }
        contactSourceRepository.deleteBySourceId(id);
        sourceRepository.deleteById(id);
    }

    private static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
