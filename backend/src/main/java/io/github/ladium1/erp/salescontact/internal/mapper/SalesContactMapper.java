package io.github.ladium1.erp.salescontact.internal.mapper;

import io.github.ladium1.erp.salescontact.internal.dto.SalesContactEmploymentResponse;
import io.github.ladium1.erp.salescontact.internal.entity.SalesContact;
import io.github.ladium1.erp.salescontact.internal.entity.SalesContactEmployment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SalesContactMapper {

    @Mapping(source = "employment.id", target = "id")
    @Mapping(source = "employment.contactId", target = "contactId")
    @Mapping(source = "contact.name", target = "contactName")
    @Mapping(source = "employment.customerId", target = "customerId")
    @Mapping(source = "customerName", target = "customerName")
    @Mapping(source = "employment.externalCompanyName", target = "externalCompanyName")
    @Mapping(source = "employment.position", target = "position")
    @Mapping(source = "employment.department", target = "department")
    @Mapping(source = "employment.startDate", target = "startDate")
    @Mapping(source = "employment.endDate", target = "endDate")
    @Mapping(source = "employment.departureType", target = "departureType")
    @Mapping(source = "employment.departureNote", target = "departureNote")
    @Mapping(source = "employment.active", target = "active")
    SalesContactEmploymentResponse toEmploymentResponse(SalesContactEmployment employment,
                                                       SalesContact contact,
                                                       String customerName);
}
