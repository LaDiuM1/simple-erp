package io.github.ladium1.erp.customer.internal.repository;

import io.github.ladium1.erp.customer.internal.entity.Customer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long>, CustomerRepositoryCustom {

    boolean existsByCode(String code);

    boolean existsByBizRegNo(String bizRegNo);

    long countByIdIn(Collection<Long> ids);

    List<Customer> findByIdIn(Collection<Long> ids, Pageable pageable);
}
