package io.github.ladium1.erp.global.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/demo")
public class DemoStatusController {

    private final DemoStateStore demoStateStore;

    @GetMapping("/status")
    public DemoStatusResponse status() {
        return demoStateStore.current();
    }
}
