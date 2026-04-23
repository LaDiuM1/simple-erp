package io.github.ladium1.erp.department.internal.web;

import io.github.ladium1.erp.department.api.DepartmentApi;
import io.github.ladium1.erp.department.api.dto.DepartmentInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentApi departmentApi;

    @GetMapping
    public List<DepartmentInfo> findAll() {
        return departmentApi.findAll();
    }
}
