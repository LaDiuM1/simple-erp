package io.github.ladium1.erp.salescontact.internal.mapper;

import io.github.ladium1.erp.salescontact.internal.dto.AcquisitionSourceInfo;
import io.github.ladium1.erp.salescontact.internal.entity.AcquisitionSource;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AcquisitionSourceMapper {

    AcquisitionSourceInfo toInfo(AcquisitionSource source);

    List<AcquisitionSourceInfo> toInfos(List<AcquisitionSource> sources);
}
