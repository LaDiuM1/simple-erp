package io.github.ladium1.erp.salescustomer.internal.mapper;

import io.github.ladium1.erp.customer.api.dto.CustomerInfo;
import io.github.ladium1.erp.employee.api.dto.EmployeeInfo;
import io.github.ladium1.erp.salescontact.api.dto.SalesContactInfo;
import io.github.ladium1.erp.salescustomer.internal.dto.SalesActivityResponse;
import io.github.ladium1.erp.salescustomer.internal.dto.SalesAssignmentResponse;
import io.github.ladium1.erp.salescustomer.internal.entity.SalesActivity;
import io.github.ladium1.erp.salescustomer.internal.entity.SalesAssignment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SalesCustomerMapper {

    @Mapping(source = "activity.id", target = "id")
    @Mapping(source = "activity.customerId", target = "customerId")
    @Mapping(source = "customer.name", target = "customerName")
    @Mapping(source = "activity.type", target = "type")
    @Mapping(source = "activity.activityDate", target = "activityDate")
    @Mapping(source = "activity.subject", target = "subject")
    @Mapping(source = "activity.content", target = "content")
    @Mapping(source = "activity.ourEmployeeId", target = "ourEmployeeId")
    @Mapping(source = "ourEmployee.name", target = "ourEmployeeName")
    @Mapping(source = "ourEmployee.departmentName", target = "ourEmployeeDepartmentName")
    @Mapping(source = "activity.customerContactId", target = "customerContactId")
    @Mapping(source = "customerContact.name", target = "customerContactRegisteredName")
    @Mapping(source = "activity.customerContactName", target = "customerContactName")
    @Mapping(source = "activity.customerContactPosition", target = "customerContactPosition")
    SalesActivityResponse toActivityResponse(SalesActivity activity, CustomerInfo customer, EmployeeInfo ourEmployee, SalesContactInfo customerContact);

    @Mapping(source = "assignment.id", target = "id")
    @Mapping(source = "assignment.customerId", target = "customerId")
    @Mapping(source = "assignment.employeeId", target = "employeeId")
    @Mapping(source = "employee.name", target = "employeeName")
    @Mapping(source = "employee.departmentName", target = "employeeDepartmentName")
    @Mapping(source = "employee.positionName", target = "employeePositionName")
    @Mapping(source = "assignment.startDate", target = "startDate")
    @Mapping(source = "assignment.endDate", target = "endDate")
    @Mapping(source = "assignment.primary", target = "primary")
    @Mapping(source = "assignment.reason", target = "reason")
    @Mapping(source = "assignment.active", target = "active")
    SalesAssignmentResponse toAssignmentResponse(SalesAssignment assignment, EmployeeInfo employee);
}
