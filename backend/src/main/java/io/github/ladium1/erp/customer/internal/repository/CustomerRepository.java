package io.github.ladium1.erp.customer.internal.repository;

import io.github.ladium1.erp.customer.internal.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long>, CustomerRepositoryCustom {

    boolean existsByCode(String code);

    boolean existsByBizRegNo(String bizRegNo);
}
