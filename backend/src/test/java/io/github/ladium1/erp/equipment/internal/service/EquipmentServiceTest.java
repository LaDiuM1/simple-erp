package io.github.ladium1.erp.equipment.internal.service;

import io.github.ladium1.erp.contract.api.ContractApi;
import io.github.ladium1.erp.contract.api.ContractInstalledEvent;
import io.github.ladium1.erp.contract.api.dto.ContractInfo;
import io.github.ladium1.erp.customer.api.CustomerApi;
import io.github.ladium1.erp.customer.api.dto.CustomerInfo;
import io.github.ladium1.erp.equipment.api.EquipmentDeletingEvent;
import io.github.ladium1.erp.equipment.internal.dto.EquipmentCreateRequest;
import io.github.ladium1.erp.equipment.internal.dto.EquipmentDetailResponse;
import io.github.ladium1.erp.equipment.internal.dto.EquipmentReferenceResponse;
import io.github.ladium1.erp.equipment.internal.dto.EquipmentSearchCondition;
import io.github.ladium1.erp.equipment.internal.dto.EquipmentSummaryResponse;
import io.github.ladium1.erp.equipment.internal.dto.EquipmentUpdateRequest;
import io.github.ladium1.erp.equipment.internal.entity.Equipment;
import io.github.ladium1.erp.equipment.internal.entity.OutputUnit;
import io.github.ladium1.erp.equipment.internal.excel.EquipmentExcelExporter;
import io.github.ladium1.erp.equipment.internal.exception.EquipmentErrorCode;
import io.github.ladium1.erp.equipment.internal.repository.EquipmentRepository;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.demo.DemoExcelExportGuard;
import io.github.ladium1.erp.global.demo.DemoErrorCode;
import io.github.ladium1.erp.global.web.PageResponse;
import io.github.ladium1.erp.product.api.ProductApi;
import io.github.ladium1.erp.product.api.dto.ProductInfo;
import io.github.ladium1.erp.supplier.api.SupplierApi;
import io.github.ladium1.erp.supplier.api.dto.SupplierInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EquipmentServiceTest {

    @InjectMocks
    private EquipmentService equipmentService;

    @Mock private EquipmentRepository equipmentRepository;
    @Mock private EquipmentExcelExporter excelExporter;
    @Mock private CustomerApi customerApi;
    @Mock private SupplierApi supplierApi;
    @Mock private ProductApi productApi;
    @Mock private ContractApi contractApi;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private DemoExcelExportGuard demoExcelExportGuard;

    @Test
    @DisplayName("Excel preflight 실패는 설비 전체 목록 materialization 전에 중단")
    void excel_preflight_runs_before_search_all() {
        org.mockito.BDDMockito.willThrow(new BusinessException(DemoErrorCode.DEMO_EXCEL_EXPORT_TOO_LARGE))
                .given(demoExcelExportGuard)
                .assertExportAllowed(DemoExcelExportGuard.Table.EQUIPMENTS);

        assertThatThrownBy(() -> equipmentService.exportExcel(null, org.springframework.data.domain.Sort.unsorted()))
                .isInstanceOf(BusinessException.class);

        verify(equipmentRepository, never()).searchAll(any(), any());
    }

    @Test
    @DisplayName("search 성공 — 참조 이름 + 계약번호 enrich 된 Summary 페이지 반환")
    void search_success() {
        // given
        Equipment equipment = mockEquipment(10L);
        ReflectionTestUtils.setField(equipment, "id", 1L);
        Pageable pageable = PageRequest.of(0, 20);
        given(equipmentRepository.search(any(EquipmentSearchCondition.class), eq(pageable)))
                .willReturn(new PageImpl<>(List.of(equipment), pageable, 1));
        stubRefNames();

        // when
        PageResponse<EquipmentSummaryResponse> page =
                equipmentService.search(emptyCondition(), pageable);

        // then
        EquipmentSummaryResponse summary = page.content().getFirst();
        assertThat(summary.customerName()).isEqualTo("대성상사");
        assertThat(summary.supplierName()).isEqualTo("YAWEI");
        assertThat(summary.productModelName()).isEqualTo("HLA-1530");
        assertThat(summary.contractNo()).isEqualTo("CT2026-001");
    }

    @Test
    @DisplayName("getDetail 성공 — 보증 만료일 파생값 포함")
    void get_detail_success() {
        // given
        Equipment equipment = mockEquipment(10L);
        ReflectionTestUtils.setField(equipment, "id", 1L);
        given(equipmentRepository.findById(1L)).willReturn(Optional.of(equipment));
        stubRefNames();

        // when
        EquipmentDetailResponse detail = equipmentService.getDetail(1L);

        // then
        assertThat(detail.warrantyStartDate()).isEqualTo(LocalDate.of(2026, 3, 2));
        assertThat(detail.oscillatorWarrantyEndDate()).isEqualTo(LocalDate.of(2029, 3, 2));
        assertThat(detail.generalWarrantyEndDate()).isEqualTo(LocalDate.of(2027, 3, 2));
    }

    @Test
    @DisplayName("getDetail 실패 — EQUIPMENT_NOT_FOUND")
    void get_detail_fail_not_found() {
        given(equipmentRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> equipmentService.getDetail(99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", EquipmentErrorCode.EQUIPMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("AS 설비 참조는 고객사 범위를 벗어난 단건을 숨긴다")
    void get_reference_rejects_other_customer_equipment() {
        Equipment equipment = mockEquipment(null);
        ReflectionTestUtils.setField(equipment, "id", 1L);
        given(equipmentRepository.findById(1L)).willReturn(Optional.of(equipment));

        assertThatThrownBy(() -> equipmentService.getReference(1L, 99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", EquipmentErrorCode.EQUIPMENT_NOT_FOUND);
        verify(productApi, never()).findByIds(anyList());
    }

    @Test
    @DisplayName("AS 설비 참조는 선택과 보증 판단에 필요한 정보만 구성한다")
    void search_reference_returns_minimum_fields() {
        Equipment equipment = mockEquipment(null);
        ReflectionTestUtils.setField(equipment, "id", 1L);
        Pageable pageable = PageRequest.of(0, 20);
        given(equipmentRepository.search(any(EquipmentSearchCondition.class), eq(pageable)))
                .willReturn(new PageImpl<>(List.of(equipment), pageable, 1));
        given(productApi.findByIds(List.of(3L))).willReturn(List.of(productInfo()));

        EquipmentReferenceResponse result = equipmentService.searchReference(
                new EquipmentSearchCondition(1L, null, null, null, null), pageable
        ).content().getFirst();

        assertThat(result.customerId()).isEqualTo(1L);
        assertThat(result.productModelName()).isEqualTo("HLA-1530");
        assertThat(result.generalWarrantyEndDate()).isEqualTo(LocalDate.of(2027, 3, 2));
        verify(customerApi, never()).findByIds(anyList());
        verify(supplierApi, never()).findByIds(anyList());
        verify(contractApi, never()).findByIds(anyList());
    }

    @Test
    @DisplayName("create 성공 — 제품의 공급사를 스냅샷 저장 + 보증 만료일 계산")
    void create_success() {
        // given
        EquipmentCreateRequest request = baseCreateRequest();
        given(customerApi.getById(1L)).willReturn(customerInfo());
        given(productApi.getById(3L)).willReturn(productInfo());
        Equipment saved = mockEquipment(null);
        ReflectionTestUtils.setField(saved, "id", 100L);
        given(equipmentRepository.save(any(Equipment.class))).willReturn(saved);

        // when
        Long id = equipmentService.create(request);

        // then
        assertThat(id).isEqualTo(100L);
        ArgumentCaptor<Equipment> captor = ArgumentCaptor.forClass(Equipment.class);
        verify(equipmentRepository).save(captor.capture());
        assertThat(captor.getValue().getSupplierId()).isEqualTo(7L);
        assertThat(captor.getValue().getOscillatorWarrantyEndDate()).isEqualTo(LocalDate.of(2029, 3, 2));
        assertThat(captor.getValue().getGeneralWarrantyEndDate()).isEqualTo(LocalDate.of(2027, 3, 2));
    }

    @Test
    @DisplayName("create 실패 — 비활성 제품 모델에는 새 설비를 연결할 수 없음")
    void create_rejects_inactive_product() {
        given(customerApi.getById(1L)).willReturn(customerInfo());
        given(productApi.getById(3L)).willReturn(productInfo(3L, false));

        assertThatThrownBy(() -> equipmentService.create(baseCreateRequest()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", EquipmentErrorCode.INACTIVE_PRODUCT);
        verify(equipmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerFromContract — 계약 이벤트로 대장 자동 생성")
    void register_from_contract_success() {
        // given
        ContractInstalledEvent event = new ContractInstalledEvent(
                10L, 1L, 7L, 3L, new BigDecimal("12"), "KW", LocalDate.of(2026, 3, 2));
        given(equipmentRepository.existsByContractId(10L)).willReturn(false);
        given(equipmentRepository.save(any(Equipment.class))).willReturn(mockEquipment(10L));

        // when
        equipmentService.registerFromContract(event);

        // then
        ArgumentCaptor<Equipment> captor = ArgumentCaptor.forClass(Equipment.class);
        verify(equipmentRepository).save(captor.capture());
        assertThat(captor.getValue().getContractId()).isEqualTo(10L);
        assertThat(captor.getValue().getCustomerId()).isEqualTo(1L);
        assertThat(captor.getValue().getOutputUnit()).isEqualTo(OutputUnit.KW);
        assertThat(captor.getValue().getInstalledDate()).isEqualTo(LocalDate.of(2026, 3, 2));
    }

    @Test
    @DisplayName("registerFromContract — 같은 계약의 대장이 이미 있으면 건너뜀 (멱등)")
    void register_from_contract_skips_duplicate() {
        // given
        ContractInstalledEvent event = new ContractInstalledEvent(
                10L, 1L, 7L, 3L, null, null, null);
        given(equipmentRepository.existsByContractId(10L)).willReturn(true);

        // when
        equipmentService.registerFromContract(event);

        // then
        verify(equipmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("update 성공 — 필드 반영 + 보증 만료일 재계산")
    void update_success() {
        // given
        Equipment equipment = mockEquipment(10L);
        given(equipmentRepository.findById(1L)).willReturn(Optional.of(equipment));
        given(customerApi.getById(1L)).willReturn(customerInfo());
        given(productApi.getById(3L)).willReturn(productInfo());
        EquipmentUpdateRequest request = new EquipmentUpdateRequest(
                1L, 3L, new BigDecimal("12"), OutputUnit.KW,
                "SN-001", "김포시 설치공장", LocalDate.of(2026, 3, 2), LocalDate.of(2026, 3, 9),
                LocalDate.of(2026, 4, 1), 24, 12, true, null
        );

        // when
        equipmentService.update(1L, request);

        // then
        assertThat(equipment.getWarrantyStartDate()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(equipment.getOscillatorWarrantyEndDate()).isEqualTo(LocalDate.of(2028, 4, 1));
        assertThat(equipment.getGeneralWarrantyEndDate()).isEqualTo(LocalDate.of(2027, 4, 1));
        assertThat(equipment.isWarrantyInsurance()).isTrue();
    }

    @Test
    @DisplayName("update 성공 — 기존 비활성 제품 모델 참조는 그대로 유지 가능")
    void update_keeps_existing_inactive_product() {
        Equipment equipment = mockEquipment(null);
        given(equipmentRepository.findById(1L)).willReturn(Optional.of(equipment));
        given(customerApi.getById(1L)).willReturn(customerInfo());
        given(productApi.getById(3L)).willReturn(productInfo(3L, false));

        equipmentService.update(1L, baseUpdateRequest(3L, "메모"));

        assertThat(equipment.getProductId()).isEqualTo(3L);
        assertThat(equipment.getNote()).isEqualTo("메모");
    }

    @Test
    @DisplayName("update 실패 — 제품 모델을 비활성 대상으로 변경할 수 없음")
    void update_rejects_new_inactive_product() {
        Equipment equipment = mockEquipment(null);
        given(equipmentRepository.findById(1L)).willReturn(Optional.of(equipment));
        given(customerApi.getById(1L)).willReturn(customerInfo());
        given(productApi.getById(4L)).willReturn(productInfo(4L, false));

        assertThatThrownBy(() -> equipmentService.update(1L, baseUpdateRequest(4L, null)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", EquipmentErrorCode.INACTIVE_PRODUCT);
        assertThat(equipment.getProductId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("update 실패 — 존재하지 않는 설비")
    void update_fail_not_found() {
        given(equipmentRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> equipmentService.update(99L, new EquipmentUpdateRequest(
                1L, 3L, null, null, null, null, null, null, null, null, null, false, null)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", EquipmentErrorCode.EQUIPMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("update 실패 — 계약에서 생성된 설비의 원천 스냅샷 변경")
    void update_rejects_contract_snapshot_change() {
        Equipment equipment = mockEquipment(10L);
        given(equipmentRepository.findById(1L)).willReturn(Optional.of(equipment));
        given(customerApi.getById(1L)).willReturn(customerInfo());
        given(productApi.getById(3L)).willReturn(productInfo());
        EquipmentUpdateRequest request = new EquipmentUpdateRequest(
                1L, 3L, new BigDecimal("13"), OutputUnit.KW,
                null, null, LocalDate.of(2026, 3, 2), null,
                null, null, null, false, null
        );

        assertThatThrownBy(() -> equipmentService.update(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode", EquipmentErrorCode.CONTRACT_SNAPSHOT_IMMUTABLE);
    }

    @Test
    @DisplayName("update 성공 — 계약 연결 설비는 제품 마스터 변경과 무관하게 공급사 스냅샷 보존")
    void update_contract_linked_preserves_supplier_snapshot() {
        Equipment equipment = mockEquipment(10L);
        given(equipmentRepository.findById(1L)).willReturn(Optional.of(equipment));
        given(customerApi.getById(1L)).willReturn(customerInfo());
        given(productApi.getById(3L)).willReturn(ProductInfo.builder()
                .id(3L).supplierId(99L).active(true).build());
        EquipmentUpdateRequest request = new EquipmentUpdateRequest(
                1L, 3L, new BigDecimal("12.0"), OutputUnit.KW,
                "SN-001", null, LocalDate.of(2026, 3, 2), null,
                null, null, null, false, "memo"
        );

        equipmentService.update(1L, request);

        assertThat(equipment.getSupplierId()).isEqualTo(7L);
        assertThat(equipment.getSerialNo()).isEqualTo("SN-001");
    }

    @Test
    @DisplayName("delete 성공 — 삭제 전 EquipmentDeletingEvent 발행")
    void delete_success() {
        given(equipmentRepository.findById(1L)).willReturn(Optional.of(mockEquipment(null)));

        equipmentService.delete(1L);

        verify(eventPublisher).publishEvent(new EquipmentDeletingEvent(1L));
        verify(equipmentRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete 실패 — 존재하지 않는 설비")
    void delete_fail_not_found() {
        given(equipmentRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> equipmentService.delete(99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", EquipmentErrorCode.EQUIPMENT_NOT_FOUND);
        verify(equipmentRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete 실패 — 계약에서 생성된 설비는 직접 삭제 금지")
    void delete_rejects_contract_linked_equipment() {
        given(equipmentRepository.findById(1L)).willReturn(Optional.of(mockEquipment(10L)));

        assertThatThrownBy(() -> equipmentService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode", EquipmentErrorCode.CONTRACT_LINKED_DELETE_FORBIDDEN);
        verify(eventPublisher, never()).publishEvent(any());
        verify(equipmentRepository, never()).deleteById(any());
    }

    private void stubRefNames() {
        given(customerApi.findByIds(anyList())).willReturn(List.of(customerInfo()));
        given(supplierApi.findByIds(anyList())).willReturn(List.of(supplierInfo()));
        given(productApi.findByIds(anyList())).willReturn(List.of(productInfo()));
        given(contractApi.findByIds(anyList())).willReturn(List.of(
                ContractInfo.builder().id(10L).contractNo("CT2026-001").customerId(1L).build()));
    }

    private Equipment mockEquipment(Long contractId) {
        return Equipment.builder()
                .customerId(1L)
                .contractId(contractId)
                .supplierId(7L)
                .productId(3L)
                .outputValue(new BigDecimal("12"))
                .outputUnit(OutputUnit.KW)
                .installedDate(LocalDate.of(2026, 3, 2))
                .warrantyStartDate(LocalDate.of(2026, 3, 2))
                .oscillatorWarrantyMonths(36)
                .generalWarrantyMonths(12)
                .warrantyInsurance(false)
                .build();
    }

    private EquipmentCreateRequest baseCreateRequest() {
        return new EquipmentCreateRequest(
                1L, 3L, new BigDecimal("12"), OutputUnit.KW,
                "SN-001", "김포시 설치공장", LocalDate.of(2026, 3, 2), LocalDate.of(2026, 3, 9),
                LocalDate.of(2026, 3, 2), 36, 12, false, null
        );
    }

    private EquipmentUpdateRequest baseUpdateRequest(Long productId, String note) {
        return new EquipmentUpdateRequest(
                1L, productId, new BigDecimal("12"), OutputUnit.KW,
                "SN-001", "김포시 설치공장", LocalDate.of(2026, 3, 2), LocalDate.of(2026, 3, 9),
                LocalDate.of(2026, 3, 2), 36, 12, false, note
        );
    }

    private EquipmentSearchCondition emptyCondition() {
        return new EquipmentSearchCondition(null, null, null, null, null);
    }

    private CustomerInfo customerInfo() {
        return CustomerInfo.builder().id(1L).code("C0001").name("대성상사").build();
    }

    private SupplierInfo supplierInfo() {
        return SupplierInfo.builder().id(7L).name("YAWEI").active(true).build();
    }

    private ProductInfo productInfo() {
        return productInfo(3L, true);
    }

    private ProductInfo productInfo(Long id, boolean active) {
        return ProductInfo.builder()
                .id(id).categoryId(1L).categoryName("평판 레이저")
                .modelName("HLA-1530").supplierId(7L).active(active)
                .build();
    }
}
