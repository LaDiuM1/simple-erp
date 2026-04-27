package io.github.ladium1.erp.salescontact.internal.repository;

import io.github.ladium1.erp.salescontact.internal.entity.SalesContact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesContactRepository extends JpaRepository<SalesContact, Long>, SalesContactRepositoryCustom {

    boolean existsByMobilePhone(String mobilePhone);

    boolean existsByMobilePhoneAndIdNot(String mobilePhone, Long id);
}
