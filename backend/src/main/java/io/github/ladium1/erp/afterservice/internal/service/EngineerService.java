package io.github.ladium1.erp.afterservice.internal.service;

import io.github.ladium1.erp.afterservice.internal.dto.EngineerRequest;
import io.github.ladium1.erp.afterservice.internal.dto.EngineerResponse;
import io.github.ladium1.erp.afterservice.internal.entity.Engineer;
import io.github.ladium1.erp.afterservice.internal.entity.EngineerType;
import io.github.ladium1.erp.afterservice.internal.exception.AfterServiceErrorCode;
import io.github.ladium1.erp.afterservice.internal.repository.AfterServiceRepository;
import io.github.ladium1.erp.afterservice.internal.repository.EngineerRepository;
import io.github.ladium1.erp.afterservice.internal.repository.ServiceExpenseRepository;
import io.github.ladium1.erp.afterservice.internal.repository.ServiceVisitRepository;
import io.github.ladium1.erp.employee.api.EmployeeApi;
import io.github.ladium1.erp.employee.api.dto.EmployeeInfo;
import io.github.ladium1.erp.global.audit.AuditAction;
import io.github.ladium1.erp.global.audit.Auditable;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.menu.Menu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * 엔지니어 마스터 — AS 관리 (AFTER_SERVICES) 의 서브 기능. 독립 메뉴 없이 부모 메뉴 권한을 그대로 쓴다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EngineerService {

    private final EngineerRepository engineerRepository;
    private final AfterServiceRepository afterServiceRepository;
    private final ServiceVisitRepository visitRepository;
    private final ServiceExpenseRepository expenseRepository;
    private final EmployeeApi employeeApi;

    public List<EngineerResponse> findAll() {
        List<Engineer> engineers = engineerRepository.findAllByOrderByTypeAscNameAsc();
        List<Long> employeeIds = engineers.stream()
                .map(Engineer::getEmployeeId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> employeeNames = employeeIds.isEmpty()
                ? Map.of()
                : employeeApi.findByIds(employeeIds).stream()
                        .collect(toMap(EmployeeInfo::id, EmployeeInfo::name));
        return engineers.stream()
                .map(engineer -> toResponse(engineer, employeeNames))
                .toList();
    }

    /**
     * AS 응답 enrichment 용 — id → 이름 매핑. 빈 입력은 빈 맵.
     */
    public Map<Long, String> findNamesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return engineerRepository.findAllById(ids).stream()
                .collect(toMap(Engineer::getId, Engineer::getName));
    }

    /**
     * AS 건 / 일지 / 경비가 참조하는 엔지니어의 존재 검증.
     */
    public void validateId(Long engineerId) {
        if (!engineerRepository.existsById(engineerId)) {
            throw new BusinessException(AfterServiceErrorCode.ENGINEER_NOT_FOUND);
        }
    }

    @Auditable(menu = Menu.AFTER_SERVICES, action = AuditAction.CREATE, targetType = "Engineer", targetIdFromReturn = true)
    @Transactional
    public Long create(EngineerRequest request) {
        Long employeeId = validateEmployeeLink(request);
        Engineer engineer = Engineer.builder()
                .name(request.name().trim())
                .type(request.type())
                .affiliation(request.affiliation())
                .phone(request.phone())
                .employeeId(employeeId)
                .active(request.active())
                .build();
        return engineerRepository.save(engineer).getId();
    }

    @Auditable(menu = Menu.AFTER_SERVICES, action = AuditAction.UPDATE, targetType = "Engineer", targetIdParam = "id")
    @Transactional
    public void update(Long id, EngineerRequest request) {
        Engineer engineer = engineerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(AfterServiceErrorCode.ENGINEER_NOT_FOUND));
        Long employeeId = validateEmployeeLink(request);
        engineer.update(
                request.name().trim(),
                request.type(),
                request.affiliation(),
                request.phone(),
                employeeId,
                request.active()
        );
    }

    /**
     * 삭제 — AS 건 / 일지 / 경비 어디서든 참조 중이면 거부. 참조가 쌓인 엔지니어는 사용 여부로 숨긴다.
     */
    @Auditable(menu = Menu.AFTER_SERVICES, action = AuditAction.DELETE, targetType = "Engineer", targetIdParam = "id")
    @Transactional
    public void delete(Long id) {
        if (!engineerRepository.existsById(id)) {
            throw new BusinessException(AfterServiceErrorCode.ENGINEER_NOT_FOUND);
        }
        if (afterServiceRepository.existsByAssignedEngineerId(id)
                || visitRepository.existsByEngineerId(id)
                || expenseRepository.existsByEngineerId(id)) {
            throw new BusinessException(AfterServiceErrorCode.ENGINEER_IN_USE);
        }
        engineerRepository.deleteById(id);
    }

    private static EngineerResponse toResponse(Engineer engineer, Map<Long, String> employeeNames) {
        return EngineerResponse.builder()
                .id(engineer.getId())
                .name(engineer.getName())
                .type(engineer.getType())
                .affiliation(engineer.getAffiliation())
                .phone(engineer.getPhone())
                .employeeId(engineer.getEmployeeId())
                .employeeName(employeeNames.get(engineer.getEmployeeId()))
                .active(engineer.isActive())
                .build();
    }

    private Long validateEmployeeLink(EngineerRequest request) {
        Long employeeId = request.employeeId();
        if (request.type() != EngineerType.INTERNAL) {
            if (employeeId != null) {
                throw new BusinessException(AfterServiceErrorCode.INVALID_ENGINEER_EMPLOYEE);
            }
            return null;
        }
        if (employeeId == null) {
            throw new BusinessException(AfterServiceErrorCode.INVALID_ENGINEER_EMPLOYEE);
        }
        if (!employeeApi.isEligibleForNewWorkReference(employeeId)) {
            throw new BusinessException(AfterServiceErrorCode.INVALID_ENGINEER_EMPLOYEE);
        }
        return employeeId;
    }
}
