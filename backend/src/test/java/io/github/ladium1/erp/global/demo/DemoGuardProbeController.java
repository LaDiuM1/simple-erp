package io.github.ladium1.erp.global.demo;

import org.springframework.boot.test.context.TestComponent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** DemoSecurityFilterChainTest 전용 종단점. */
@TestComponent
@RestController
public class DemoGuardProbeController {

    @GetMapping("/api/v1/probe")
    ProbeResponse read() {
        return new ProbeResponse("read");
    }

    @GetMapping("/actuator/health/readiness")
    ProbeResponse readiness() {
        return new ProbeResponse("ready");
    }

    @PostMapping("/api/v1/probe")
    ProbeResponse write() {
        return new ProbeResponse("write");
    }

    @PostMapping("/api/v1/code-rules/{target}/preview")
    ProbeResponse codeRulePreview(@PathVariable String target) {
        return new ProbeResponse("preview:" + target);
    }

    @PostMapping({
            "/api/v1/files",
            "/api/v1/drive/files",
            "/api/v1/customers/excel/upload",
            "/api/v1/sales-contacts/excel/upload"
    })
    ProbeResponse upload() {
        return new ProbeResponse("upload");
    }

    record ProbeResponse(String result) {
    }
}
