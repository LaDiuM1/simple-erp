package io.github.ladium1.erp.customer.internal.repository;

import io.github.ladium1.erp.customer.internal.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface CustomerRepository extends JpaRepository<Customer, Long>, CustomerRepositoryCustom {

    boolean existsByCode(String code);

    boolean existsByBizRegNo(String bizRegNo);

    long countByCreatedAtGreaterThanEqual(LocalDateTime since);
}
