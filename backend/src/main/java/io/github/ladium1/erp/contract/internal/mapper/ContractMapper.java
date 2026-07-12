package io.github.ladium1.erp.contract.internal.mapper;

import io.github.ladium1.erp.contract.internal.dto.ContractNoteResponse;
import io.github.ladium1.erp.contract.internal.dto.ContractPaymentResponse;
import io.github.ladium1.erp.contract.internal.entity.ContractNote;
import io.github.ladium1.erp.contract.internal.entity.ContractPayment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 자식 행 (대금 회차 / 메모) 매핑 전담 — 계약 목록 / 상세 응답은 참조 이름 enrichment 가 많아
 * service 에서 builder 로 조립한다.
 */
@Mapper(componentModel = "spring")
public interface ContractMapper {

    ContractPaymentResponse toPaymentResponse(ContractPayment payment);

    @Mapping(source = "note.id", target = "id")
    @Mapping(source = "note.createdAt", target = "createdAt")
    ContractNoteResponse toNoteResponse(ContractNote note, String authorName);
}
