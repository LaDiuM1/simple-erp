package io.github.ladium1.erp.equipment.internal.service;

import io.github.ladium1.erp.contract.api.ContractApi;
import io.github.ladium1.erp.contract.api.ContractInstalledEvent;
import io.github.ladium1.erp.contract.api.dto.ContractInfo;
import io.github.ladium1.erp.customer.api.CustomerApi;
import io.github.ladium1.erp.customer.api.dto.CustomerInfo;
import io.github.ladium1.erp.equipment.api.EquipmentApi;
import io.github.ladium1.erp.equipment.api.EquipmentDeletingEvent;
import io.github.ladium1.erp.equipment.api.dto.EquipmentInfo;
import io.github.ladium1.erp.equipment.api.dto.ExpiringWarrantyInfo;
import io.github.ladium1.erp.equipment.internal.dto.EquipmentCreateRequest;
import io.github.ladium1.erp.equipment.internal.dto.EquipmentDetailResponse;
import io.github.ladium1.erp.equipment.internal.dto.EquipmentExcelRow;
import io.github.ladium1.erp.equipment.internal.dto.EquipmentSearchCondition;
import io.github.ladium1.erp.equipment.internal.dto.EquipmentSummaryResponse;
import io.github.ladium1.erp.equipment.internal.dto.EquipmentUpdateRequest;
import io.github.ladium1.erp.equipment.internal.entity.Equipment;
import io.github.ladium1.erp.equipment.internal.entity.OutputUnit;
import io.github.ladium1.erp.equipment.internal.excel.EquipmentExcelExporter;
import io.github.ladium1.erp.equipment.internal.exception.EquipmentErrorCode;
import io.github.ladium1.erp.equipment.internal.repository.EquipmentRepository;
import io.github.ladium1.erp.global.audit.AuditAction;
import io.github.ladium1.erp.global.audit.Auditable;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.menu.Menu;
import io.github.ladium1.erp.global.web.PageResponse;
import io.github.ladium1.erp.product.api.ProductApi;
import io.github.ladium1.erp.product.api.dto.ProductInfo;
import io.github.ladium1.erp.supplier.api.SupplierApi;
import io.github.ladium1.erp.supplier.api.dto.SupplierInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toMap;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EquipmentService implements EquipmentApi {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentExcelExporter excelExporter;
    private final CustomerApi customerApi;
    private final SupplierApi supplierApi;
    private final ProductApi productApi;
    private final ContractApi contractApi;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public EquipmentInfo getById(Long id) {
        return equipmentRepository.findById(id)
                .map(EquipmentService::toEquipmentInfo)
                .orElseThrow(() -> new BusinessException(EquipmentErrorCode.EQUIPMENT_NOT_FOUND));
    }

    @Override
    public List<EquipmentInfo> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return equipmentRepository.findAllById(ids).stream()
                .map(EquipmentService::toEquipmentInfo)
                .toList();
    }

    @Override
    public List<ExpiringWarrantyInfo> findExpiringWarranties(int days, int limit) {
        return equipmentRepository.findExpiringWarranties(days, limit).stream()
                .map(e -> ExpiringWarrantyInfo.builder()
                        .id(e.getId())
                        .customerId(e.getCustomerId())
                        .productId(e.getProductId())
                        .serialNo(e.getSerialNo())
                        .oscillatorWarrantyEndDate(e.getOscillatorWarrantyEndDate())
                        .generalWarrantyEndDate(e.getGeneralWarrantyEndDate())
                        .build())
                .toList();
    }

    private static EquipmentInfo toEquipmentInfo(Equipment equipment) {
        return EquipmentInfo.builder()
                .id(equipment.getId())
                .customerId(equipment.getCustomerId())
                .productId(equipment.getProductId())
                .serialNo(equipment.getSerialNo())
                .oscillatorWarrantyEndDate(equipment.getOscillatorWarrantyEndDate())
                .generalWarrantyEndDate(equipment.getGeneralWarrantyEndDate())
                .build();
    }

    public PageResponse<EquipmentSummaryResponse> search(EquipmentSearchCondition condition, Pageable pageable) {
        Page<Equipment> page = equipmentRepository.search(condition, pageable);
        RefNames refs = loadRefNames(page.getContent());
        return PageResponse.of(page.map(e -> toSummaryResponse(e, refs)));
    }

    /**
     * 검색 조건 + 정렬 그대로 전체 페이지를 .xlsx 바이트로 직렬화. 페이지네이션 무시 — 필터링된 전체.
     */
    public byte[] exportExcel(EquipmentSearchCondition condition, Sort sort) {
        List<Equipment> equipments = equipmentRepository.searchAll(condition, sort);
        RefNames refs = loadRefNames(equipments);
        List<EquipmentExcelRow> rows = equipments.stream()
                .map(e -> {
                    ProductInfo product = refs.product().get(e.getProductId());
                    return EquipmentExcelRow.builder()
                            .customerName(refs.customer().get(e.getCustomerId()))
                            .installAddress(e.getInstallAddress())
                            .supplierName(refs.supplier().get(e.getSupplierId()))
                            .categoryName(product == null ? null : product.categoryName())
                            .productModelName(product == null ? null : product.modelName())
                            .outputValue(e.getOutputValue())
                            .outputUnit(e.getOutputUnit())
                            .serialNo(e.getSerialNo())
                            .installedDate(e.getInstalledDate())
                            .confirmedDate(e.getConfirmedDate())
                            .warrantyStartDate(e.getWarrantyStartDate())
                            .oscillatorWarrantyMonths(e.getOscillatorWarrantyMonths())
                            .oscillatorWarrantyEndDate(e.getOscillatorWarrantyEndDate())
                            .generalWarrantyMonths(e.getGeneralWarrantyMonths())
                            .generalWarrantyEndDate(e.getGeneralWarrantyEndDate())
                            .warrantyInsurance(e.isWarrantyInsurance())
                            .contractNo(e.getContractId() == null ? null : refs.contractNo().get(e.getContractId()))
                            .note(e.getNote())
                            .build();
                })
                .toList();
        return excelExporter.export(rows);
    }

    public EquipmentDetailResponse getDetail(Long id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(EquipmentErrorCode.EQUIPMENT_NOT_FOUND));
        RefNames refs = loadRefNames(List.of(equipment));
        ProductInfo product = refs.product().get(equipment.getProductId());

        return EquipmentDetailResponse.builder()
                .id(equipment.getId())
                .customerId(equipment.getCustomerId())
                .customerName(refs.customer().get(equipment.getCustomerId()))
                .contractId(equipment.getContractId())
                .contractNo(equipment.getContractId() == null
                        ? null
                        : refs.contractNo().get(equipment.getContractId()))
                .supplierId(equipment.getSupplierId())
                .supplierName(refs.supplier().get(equipment.getSupplierId()))
                .productId(equipment.getProductId())
                .productModelName(product == null ? null : product.modelName())
                .categoryName(product == null ? null : product.categoryName())
                .outputValue(equipment.getOutputValue())
                .outputUnit(equipment.getOutputUnit())
                .serialNo(equipment.getSerialNo())
                .installAddress(equipment.getInstallAddress())
                .installedDate(equipment.getInstalledDate())
                .confirmedDate(equipment.getConfirmedDate())
                .warrantyStartDate(equipment.getWarrantyStartDate())
                .oscillatorWarrantyMonths(equipment.getOscillatorWarrantyMonths())
                .generalWarrantyMonths(equipment.getGeneralWarrantyMonths())
                .oscillatorWarrantyEndDate(equipment.getOscillatorWarrantyEndDate())
                .generalWarrantyEndDate(equipment.getGeneralWarrantyEndDate())
                .warrantyInsurance(equipment.isWarrantyInsurance())
                .note(equipment.getNote())
                .build();
    }

    @Auditable(menu = Menu.EQUIPMENTS, action = AuditAction.CREATE, targetType = "Equipment", targetIdFromReturn = true)
    @Transactional
    public Long create(EquipmentCreateRequest request) {
        // 참조 존재 검증 — 없으면 각 모듈이 NOT_FOUND 를 던진다.
        customerApi.getById(request.customerId());
        ProductInfo product = productApi.getById(request.productId());

        Equipment equipment = Equipment.builder()
                .customerId(request.customerId())
                .supplierId(product.supplierId())
                .productId(request.productId())
                .outputValue(request.outputValue())
                .outputUnit(request.outputUnit())
                .serialNo(request.serialNo())
                .installAddress(request.installAddress())
                .installedDate(request.installedDate())
                .confirmedDate(request.confirmedDate())
                .warrantyStartDate(request.warrantyStartDate())
                .oscillatorWarrantyMonths(request.oscillatorWarrantyMonths())
                .generalWarrantyMonths(request.generalWarrantyMonths())
                .warrantyInsurance(request.warrantyInsurance())
                .note(request.note())
                .build();
        return equipmentRepository.save(equipment).getId();
    }

    /**
     * 계약 설치완료 (INSTALLED) 전이 이벤트로부터 설비 대장 자동 생성.
     * 같은 계약으로 이미 생성된 행이 있으면 건너뜀 (상태를 되돌렸다 다시 전이하는 케이스 멱등 처리).
     * 보증 개월 / 시리얼 등 계약에 없는 정보는 담당자가 대장에서 보완 입력한다.
     */
    @Transactional
    public void registerFromContract(ContractInstalledEvent event) {
        if (equipmentRepository.existsByContractId(event.contractId())) {
            log.info("설비 대장 자동 생성 건너뜀 — 계약 {} 의 대장이 이미 존재", event.contractId());
            return;
        }
        Equipment equipment = Equipment.builder()
                .customerId(event.customerId())
                .contractId(event.contractId())
                .supplierId(event.supplierId())
                .productId(event.productId())
                .outputValue(event.outputValue())
                .outputUnit(toOutputUnit(event.outputUnit()))
                .installedDate(event.installedDate())
                .warrantyInsurance(false)
                .build();
        equipmentRepository.save(equipment);
        log.info("계약 {} 설치완료 — 설비 대장 자동 생성 (id={})", event.contractId(), equipment.getId());
    }

    @Auditable(menu = Menu.EQUIPMENTS, action = AuditAction.UPDATE, targetType = "Equipment", targetIdParam = "id")
    @Transactional
    public void update(Long id, EquipmentUpdateRequest request) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(EquipmentErrorCode.EQUIPMENT_NOT_FOUND));
        customerApi.getById(request.customerId());
        ProductInfo product = productApi.getById(request.productId());

        equipment.update(
                request.customerId(),
                product.supplierId(),
                request.productId(),
                request.outputValue(),
                request.outputUnit(),
                request.serialNo(),
                request.installAddress(),
                request.installedDate(),
                request.confirmedDate(),
                request.warrantyStartDate(),
                request.oscillatorWarrantyMonths(),
                request.generalWarrantyMonths(),
                request.warrantyInsurance(),
                request.note()
        );
    }

    @Auditable(menu = Menu.EQUIPMENTS, action = AuditAction.DELETE, targetType = "Equipment", targetIdParam = "id")
    @Transactional
    public void delete(Long id) {
        if (!equipmentRepository.existsById(id)) {
            throw new BusinessException(EquipmentErrorCode.EQUIPMENT_NOT_FOUND);
        }
        // 다른 모듈 (AS 등) 의 사용 여부는 동기 이벤트로 검사 — 리스너가 throw 하면 트랜잭션 롤백.
        eventPublisher.publishEvent(new EquipmentDeletingEvent(id));
        equipmentRepository.deleteById(id);
    }

    /**
     * 일괄 삭제 — 단일 트랜잭션에서 ID 별 단건 delete 호출.
     * 한 건이라도 실패하면 전체 롤백.
     */
    @Auditable(menu = Menu.EQUIPMENTS, action = AuditAction.DELETE, targetType = "Equipment")
    @Transactional
    public void deleteAll(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        for (Long id : ids) {
            delete(id);
        }
    }

    private EquipmentSummaryResponse toSummaryResponse(Equipment e, RefNames refs) {
        ProductInfo product = refs.product().get(e.getProductId());
        return EquipmentSummaryResponse.builder()
                .id(e.getId())
                .customerId(e.getCustomerId())
                .customerName(refs.customer().get(e.getCustomerId()))
                .contractId(e.getContractId())
                .contractNo(e.getContractId() == null ? null : refs.contractNo().get(e.getContractId()))
                .supplierId(e.getSupplierId())
                .supplierName(refs.supplier().get(e.getSupplierId()))
                .productId(e.getProductId())
                .productModelName(product == null ? null : product.modelName())
                .categoryName(product == null ? null : product.categoryName())
                .outputValue(e.getOutputValue())
                .outputUnit(e.getOutputUnit())
                .serialNo(e.getSerialNo())
                .installAddress(e.getInstallAddress())
                .installedDate(e.getInstalledDate())
                .oscillatorWarrantyEndDate(e.getOscillatorWarrantyEndDate())
                .generalWarrantyEndDate(e.getGeneralWarrantyEndDate())
                .warrantyInsurance(e.isWarrantyInsurance())
                .build();
    }

    /**
     * 이벤트로 전달된 출력 단위 문자열을 enum 으로 변환 — 알 수 없는 값은 null (담당자 보완).
     */
    private static OutputUnit toOutputUnit(String name) {
        if (name == null) {
            return null;
        }
        try {
            return OutputUnit.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 참조 이름 일괄 enrichment — 목록 / 상세 / 엑셀이 공유.
     * 참조 대상이 지워진 경우 이름은 null 로 남기고 응답은 유지한다.
     */
    private RefNames loadRefNames(List<Equipment> equipments) {
        if (equipments.isEmpty()) {
            return new RefNames(Map.of(), Map.of(), Map.of(), Map.of());
        }
        Map<Long, String> customers = customerApi.findByIds(
                        equipments.stream().map(Equipment::getCustomerId).distinct().toList()).stream()
                .collect(toMap(CustomerInfo::id, CustomerInfo::name));
        Map<Long, String> suppliers = supplierApi.findByIds(
                        equipments.stream().map(Equipment::getSupplierId).distinct().toList()).stream()
                .collect(toMap(SupplierInfo::id, SupplierInfo::name));
        Map<Long, ProductInfo> products = productApi.findByIds(
                        equipments.stream().map(Equipment::getProductId).distinct().toList()).stream()
                .collect(toMap(ProductInfo::id, p -> p));
        Map<Long, String> contractNos = contractApi.findByIds(
                        equipments.stream().map(Equipment::getContractId).filter(Objects::nonNull).distinct().toList())
                .stream()
                .collect(toMap(ContractInfo::id, ContractInfo::contractNo));
        return new RefNames(customers, suppliers, products, contractNos);
    }

    private record RefNames(Map<Long, String> customer,
                            Map<Long, String> supplier,
                            Map<Long, ProductInfo> product,
                            Map<Long, String> contractNo) {
    }
}
