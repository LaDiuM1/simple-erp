package io.github.ladium1.erp.supplier.internal.service;

import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.supplier.api.SupplierDeletingEvent;
import io.github.ladium1.erp.supplier.api.dto.SupplierInfo;
import io.github.ladium1.erp.supplier.internal.dto.SupplierCreateRequest;
import io.github.ladium1.erp.supplier.internal.dto.SupplierDetailResponse;
import io.github.ladium1.erp.supplier.internal.dto.SupplierUpdateRequest;
import io.github.ladium1.erp.supplier.internal.entity.Supplier;
import io.github.ladium1.erp.supplier.internal.exception.SupplierErrorCode;
import io.github.ladium1.erp.supplier.internal.mapper.SupplierMapper;
import io.github.ladium1.erp.supplier.internal.repository.SupplierRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @InjectMocks
    private SupplierService supplierService;

    @Mock private SupplierRepository supplierRepository;
    @Mock private SupplierMapper supplierMapper;
    @Mock private ApplicationEventPublisher eventPublisher;

    @Test
    @DisplayName("getById 성공 — Mapper 가 변환한 Info 반환")
    void get_by_id_success() {
        // given
        Supplier supplier = mockSupplier("YAWEI", "야웨이");
        SupplierInfo info = SupplierInfo.builder().id(1L).name("YAWEI").nameKo("야웨이").active(true).build();
        given(supplierRepository.findById(1L)).willReturn(Optional.of(supplier));
        given(supplierMapper.toSupplierInfo(supplier)).willReturn(info);

        // when
        SupplierInfo actual = supplierService.getById(1L);

        // then
        assertThat(actual).isEqualTo(info);
    }

    @Test
    @DisplayName("getById 실패 — SUPPLIER_NOT_FOUND")
    void get_by_id_fail_not_found() {
        // given
        given(supplierRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> supplierService.getById(99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", SupplierErrorCode.SUPPLIER_NOT_FOUND);
    }

    @Test
    @DisplayName("findByIds — 빈 리스트 입력 시 빈 결과 반환 (DB 미조회)")
    void find_by_ids_empty() {
        // when
        List<SupplierInfo> result = supplierService.findByIds(List.of());

        // then
        assertThat(result).isEmpty();
        verify(supplierRepository, never()).findAllById(any());
    }

    @Test
    @DisplayName("getDetail 성공")
    void get_detail_success() {
        // given
        Supplier supplier = mockSupplier("YAWEI", "야웨이");
        SupplierDetailResponse detail = SupplierDetailResponse.builder()
                .id(1L).name("YAWEI").nameKo("야웨이").country("중국").active(true).build();
        given(supplierRepository.findById(1L)).willReturn(Optional.of(supplier));
        given(supplierMapper.toDetailResponse(supplier)).willReturn(detail);

        // when
        SupplierDetailResponse actual = supplierService.getDetail(1L);

        // then
        assertThat(actual).isEqualTo(detail);
    }

    @Test
    @DisplayName("create 성공 — 이름 trim 후 저장")
    void create_success() {
        // given
        SupplierCreateRequest request = new SupplierCreateRequest(" ACME ", null, "중국", null, true);
        given(supplierRepository.existsByName("ACME")).willReturn(false);
        Supplier saved = mockSupplier("ACME", null);
        ReflectionTestUtils.setField(saved, "id", 10L);
        given(supplierRepository.save(any(Supplier.class))).willReturn(saved);

        // when
        Long id = supplierService.create(request);

        // then
        assertThat(id).isEqualTo(10L);
    }

    @Test
    @DisplayName("create 실패 — 중복 공급사명 시 DUPLICATE_NAME")
    void create_fail_duplicate_name() {
        // given
        SupplierCreateRequest request = new SupplierCreateRequest("YAWEI", null, null, null, true);
        given(supplierRepository.existsByName("YAWEI")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> supplierService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", SupplierErrorCode.DUPLICATE_NAME);
        verify(supplierRepository, never()).save(any());
    }

    @Test
    @DisplayName("update 성공 — 엔티티의 update 호출")
    void update_success() {
        // given
        Supplier supplier = mockSupplier("YAWEI", null);
        given(supplierRepository.findById(1L)).willReturn(Optional.of(supplier));
        given(supplierRepository.existsByNameAndIdNot("YAWEI", 1L)).willReturn(false);
        SupplierUpdateRequest request = new SupplierUpdateRequest("YAWEI", "야웨이", "중국", "메모", false);

        // when
        supplierService.update(1L, request);

        // then
        assertThat(supplier.getNameKo()).isEqualTo("야웨이");
        assertThat(supplier.getNote()).isEqualTo("메모");
        assertThat(supplier.isActive()).isFalse();
    }

    @Test
    @DisplayName("update 실패 — 다른 공급사가 쓰는 이름으로 변경 시 DUPLICATE_NAME")
    void update_fail_duplicate_name() {
        // given
        Supplier supplier = mockSupplier("ACME", null);
        given(supplierRepository.findById(1L)).willReturn(Optional.of(supplier));
        given(supplierRepository.existsByNameAndIdNot("YAWEI", 1L)).willReturn(true);
        SupplierUpdateRequest request = new SupplierUpdateRequest("YAWEI", null, null, null, true);

        // when & then
        assertThatThrownBy(() -> supplierService.update(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", SupplierErrorCode.DUPLICATE_NAME);
    }

    @Test
    @DisplayName("update 실패 — 존재하지 않는 공급사")
    void update_fail_not_found() {
        // given
        given(supplierRepository.findById(99L)).willReturn(Optional.empty());
        SupplierUpdateRequest request = new SupplierUpdateRequest("YAWEI", null, null, null, true);

        // when & then
        assertThatThrownBy(() -> supplierService.update(99L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", SupplierErrorCode.SUPPLIER_NOT_FOUND);
    }

    @Test
    @DisplayName("delete 성공 — SupplierDeletingEvent 발행 후 deleteById 호출")
    void delete_success() {
        // given
        given(supplierRepository.existsById(1L)).willReturn(true);

        // when
        supplierService.delete(1L);

        // then
        verify(eventPublisher).publishEvent(new SupplierDeletingEvent(1L));
        verify(supplierRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete 실패 — 존재하지 않는 공급사 (이벤트 미발행)")
    void delete_fail_not_found() {
        // given
        given(supplierRepository.existsById(99L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> supplierService.delete(99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", SupplierErrorCode.SUPPLIER_NOT_FOUND);
        verify(eventPublisher, never()).publishEvent(any());
        verify(supplierRepository, never()).deleteById(any());
    }

    private Supplier mockSupplier(String name, String nameKo) {
        return Supplier.builder()
                .name(name)
                .nameKo(nameKo)
                .country("중국")
                .active(true)
                .build();
    }
}
