package io.github.ladium1.erp.department.internal.service;

import io.github.ladium1.erp.department.api.DepartmentApi;
import io.github.ladium1.erp.department.api.dto.DepartmentInfo;
import io.github.ladium1.erp.department.internal.exception.DepartmentErrorCode;
import io.github.ladium1.erp.department.internal.mapper.DepartmentMapper;
import io.github.ladium1.erp.department.internal.repository.DepartmentRepository;
import io.github.ladium1.erp.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService implements DepartmentApi {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    @Override
    public DepartmentInfo getById(Long id) {
        return departmentRepository.findById(id)
                .map(departmentMapper::toDepartmentInfo)
                .orElseThrow(() -> new BusinessException(DepartmentErrorCode.DEPARTMENT_NOT_FOUND));
    }
}
