package io.github.ladium1.erp.customer.internal.repository;

import io.github.ladium1.erp.customer.internal.dto.CustomerSearchCondition;
import io.github.ladium1.erp.customer.internal.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface CustomerRepositoryCustom {

    Page<Customer> search(CustomerSearchCondition condition, Pageable pageable);

    List<Customer> searchAll(CustomerSearchCondition condition, Sort sort);
}
