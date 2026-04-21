package io.github.ladium1.erp.position.internal.service;

import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.position.api.PositionApi;
import io.github.ladium1.erp.position.api.dto.PositionInfo;
import io.github.ladium1.erp.position.internal.exception.PositionErrorCode;
import io.github.ladium1.erp.position.internal.mapper.PositionMapper;
import io.github.ladium1.erp.position.internal.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PositionService implements PositionApi {

    private final PositionRepository positionRepository;
    private final PositionMapper positionMapper;

    @Override
    public PositionInfo getById(Long id) {
        return positionRepository.findById(id)
                .map(positionMapper::toPositionInfo)
                .orElseThrow(() -> new BusinessException(PositionErrorCode.POSITION_NOT_FOUND));
    }
}
