package io.github.ladium1.erp.supplier.internal.mapper;

import io.github.ladium1.erp.supplier.api.dto.SupplierInfo;
import io.github.ladium1.erp.supplier.internal.dto.SupplierDetailResponse;
import io.github.ladium1.erp.supplier.internal.dto.SupplierSummaryResponse;
import io.github.ladium1.erp.supplier.internal.entity.Supplier;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SupplierMapper {

    SupplierInfo toSupplierInfo(Supplier supplier);

    List<SupplierInfo> toSupplierInfos(List<Supplier> suppliers);

    SupplierSummaryResponse toSummaryResponse(Supplier supplier);

    SupplierDetailResponse toDetailResponse(Supplier supplier);
}
