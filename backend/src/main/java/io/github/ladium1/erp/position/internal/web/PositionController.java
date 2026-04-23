package io.github.ladium1.erp.position.internal.web;

import io.github.ladium1.erp.position.api.PositionApi;
import io.github.ladium1.erp.position.api.dto.PositionInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionApi positionApi;

    @GetMapping
    public List<PositionInfo> findAll() {
        return positionApi.findAll();
    }
}
