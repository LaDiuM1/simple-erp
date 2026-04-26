package io.github.ladium1.erp.customer.internal.mapper;

import io.github.ladium1.erp.customer.api.dto.CustomerInfo;
import io.github.ladium1.erp.customer.internal.dto.CustomerDetailResponse;
import io.github.ladium1.erp.customer.internal.dto.CustomerSummaryResponse;
import io.github.ladium1.erp.customer.internal.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerInfo toCustomerInfo(Customer customer);

    List<CustomerInfo> toCustomerInfos(List<Customer> customers);

    @Mapping(source = "address.roadAddress", target = "roadAddress")
    CustomerSummaryResponse toSummaryResponse(Customer customer);

    @Mapping(source = "address.zipCode", target = "zipCode")
    @Mapping(source = "address.roadAddress", target = "roadAddress")
    @Mapping(source = "address.detailAddress", target = "detailAddress")
    CustomerDetailResponse toDetailResponse(Customer customer);
}
