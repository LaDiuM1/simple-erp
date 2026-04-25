package io.github.ladium1.erp.department.internal.service;

import io.github.ladium1.erp.department.api.DepartmentApi;
import io.github.ladium1.erp.department.api.DepartmentDeletingEvent;
import io.github.ladium1.erp.department.api.dto.DepartmentInfo;
import io.github.ladium1.erp.department.internal.dto.DepartmentCreateRequest;
import io.github.ladium1.erp.department.internal.dto.DepartmentDetailResponse;
import io.github.ladium1.erp.department.internal.dto.DepartmentSearchCondition;
import io.github.ladium1.erp.department.internal.dto.DepartmentSummaryResponse;
import io.github.ladium1.erp.department.internal.dto.DepartmentUpdateRequest;
import io.github.ladium1.erp.department.internal.entity.Department;
import io.github.ladium1.erp.department.internal.exception.DepartmentErrorCode;
import io.github.ladium1.erp.department.internal.mapper.DepartmentMapper;
import io.github.ladium1.erp.department.internal.repository.DepartmentRepository;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.web.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService implements DepartmentApi {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public DepartmentInfo getById(Long id) {
        return departmentRepository.findById(id)
                .map(departmentMapper::toDepartmentInfo)
                .orElseThrow(() -> new BusinessException(DepartmentErrorCode.DEPARTMENT_NOT_FOUND));
    }

    @Override
    public List<DepartmentInfo> findAll() {
        return departmentMapper.toDepartmentInfos(
                departmentRepository.findAll(Sort.by("name").ascending())
        );
    }

    @Override
    public List<DepartmentInfo> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return departmentMapper.toDepartmentInfos(departmentRepository.findAllById(ids));
    }

    public PageResponse<DepartmentSummaryResponse> search(DepartmentSearchCondition condition, Pageable pageable) {
        Page<Department> page = departmentRepository.search(condition, pageable);
        return PageResponse.of(page.map(departmentMapper::toSummaryResponse));
    }

    public DepartmentDetailResponse getDetail(Long id) {
        return departmentRepository.findById(id)
                .map(departmentMapper::toDetailResponse)
                .orElseThrow(() -> new BusinessException(DepartmentErrorCode.DEPARTMENT_NOT_FOUND));
    }

    @Transactional
    public Long create(DepartmentCreateRequest request) {
        if (departmentRepository.existsByCode(request.code())) {
            throw new BusinessException(DepartmentErrorCode.DUPLICATE_CODE);
        }
        Department parent = resolveParent(request.parentId(), null);

        Department department = Department.builder()
                .code(request.code())
                .name(request.name())
                .parent(parent)
                .build();

        return departmentRepository.save(department).getId();
    }

    @Transactional
    public void update(Long id, DepartmentUpdateRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(DepartmentErrorCode.DEPARTMENT_NOT_FOUND));
        Department parent = resolveParent(request.parentId(), id);

        department.update(request.name(), parent);
    }

    @Transactional
    public void delete(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new BusinessException(DepartmentErrorCode.DEPARTMENT_NOT_FOUND);
        }
        if (departmentRepository.existsByParentId(id)) {
            throw new BusinessException(DepartmentErrorCode.CANNOT_DELETE_HAS_CHILDREN);
        }
        // 다른 모듈 (직원 등) 의 사용 여부는 동기 이벤트로 검사 — 리스너가 throw 하면 트랜잭션 롤백.
        eventPublisher.publishEvent(new DepartmentDeletingEvent(id));
        departmentRepository.deleteById(id);
    }

    private Department resolveParent(Long parentId, Long selfId) {
        if (parentId == null) {
            return null;
        }
        if (selfId != null && parentId.equals(selfId)) {
            throw new BusinessException(DepartmentErrorCode.CANNOT_SET_SELF_AS_PARENT);
        }
        return departmentRepository.findById(parentId)
                .orElseThrow(() -> new BusinessException(DepartmentErrorCode.DEPARTMENT_NOT_FOUND));
    }
}
