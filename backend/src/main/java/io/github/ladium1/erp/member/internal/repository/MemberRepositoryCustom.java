package io.github.ladium1.erp.member.internal.repository;

import io.github.ladium1.erp.member.internal.dto.MemberSearchCondition;
import io.github.ladium1.erp.member.internal.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface MemberRepositoryCustom {

    Page<Member> search(MemberSearchCondition condition, Pageable pageable);

    List<Member> searchAll(MemberSearchCondition condition, Sort sort);
}
