package io.github.ladium1.erp.deployment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DeploymentWorkflowContractTest {

    @Test
    @DisplayName("앱 이미지 시작 실패 시 이전 이미지 조합으로 롤백")
    void compose_up_failure_triggers_rollback() throws IOException {
        String workflow = readWorkflow();
        int guardedUp = workflow.indexOf("if ! docker compose up -d --remove-orphans backend web; then");
        int rollback = workflow.indexOf("if rollback; then", guardedUp);

        assertThat(guardedUp).isGreaterThanOrEqualTo(0);
        assertThat(rollback).isGreaterThan(guardedUp);
        assertThat(workflow).contains("Deployment start failed and rollback failed");
        assertThat(workflow).doesNotContain("rollback || true");
    }

    @Test
    @DisplayName("배포는 앱 이미지만 pull하고 backend와 web을 각각 검증")
    void pulls_and_probes_application_pair_only() throws IOException {
        String workflow = readWorkflow();

        assertThat(workflow).contains("docker compose pull backend web");
        assertThat(workflow).doesNotContain("docker compose pull\n");
        assertThat(workflow).contains("docker compose ps -q", "docker exec \"${backend_container}\"");
        assertThat(workflow).contains("\"status\":\"UP\"", "probe_web_origin", "--resolve");
        assertThat(workflow).contains("[[ \"${status}\" == \"200\" ]]");
        assertThat(workflow).contains("if ! wait_for_pair \"Previous\"; then");
    }

    @Test
    @DisplayName("Caddy는 exact upload 4개만 32MB이고 나머지 API는 1MB")
    void caddy_splits_upload_and_json_body_limits() throws IOException {
        String caddyfile = readCaddyfile();
        int uploadMatcher = caddyfile.indexOf("@uploadApi {");
        int uploadLimit = caddyfile.indexOf("max_size {$API_REQUEST_BODY_MAX_SIZE:32MB}", uploadMatcher);
        int genericApi = caddyfile.indexOf("handle /api/* {", uploadLimit);
        int jsonLimit = caddyfile.indexOf("max_size {$API_JSON_REQUEST_BODY_MAX_SIZE:1MB}", genericApi);

        assertThat(uploadMatcher).isGreaterThanOrEqualTo(0);
        assertThat(caddyfile.substring(uploadMatcher, uploadLimit)).contains(
                "method POST",
                "/api/v1/files",
                "/api/v1/drive/files",
                "/api/v1/customers/excel/upload",
                "/api/v1/sales-contacts/excel/upload"
        );
        assertThat(uploadLimit).isGreaterThan(uploadMatcher);
        assertThat(genericApi).isGreaterThan(uploadLimit);
        assertThat(jsonLimit).isGreaterThan(genericApi);
        assertThat(caddyfile).containsOnlyOnce("max_size {$API_REQUEST_BODY_MAX_SIZE:32MB}");
        assertThat(caddyfile).containsOnlyOnce("max_size {$API_JSON_REQUEST_BODY_MAX_SIZE:1MB}");
    }

    @Test
    @DisplayName("Caddy는 status를 GET/HEAD로 제한하고 모든 backend proxy의 XFF를 peer 주소로 덮어씀")
    void caddy_pins_status_methods_and_forwarded_ip_contract() throws IOException {
        String caddyfile = readCaddyfile();
        int statusMatcher = caddyfile.indexOf("@demoStatus {");
        int statusHandle = caddyfile.indexOf("handle @demoStatus", statusMatcher);

        assertThat(caddyfile.substring(statusMatcher, statusHandle))
                .contains("method GET HEAD", "path /api/v1/demo/status");
        assertThat(countOccurrences(caddyfile, "reverse_proxy backend:8080 {")).isEqualTo(3);
        assertThat(countOccurrences(caddyfile, "header_up X-Forwarded-For {remote_host}")).isEqualTo(3);
    }

    private static String readWorkflow() throws IOException {
        Path workflow = Stream.of(
                        Path.of(".github", "workflows", "deploy.yml"),
                        Path.of("..", ".github", "workflows", "deploy.yml")
                )
                .filter(Files::isRegularFile)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("deploy.yml not found"));
        return Files.readString(workflow);
    }

    private static String readCaddyfile() throws IOException {
        Path caddyfile = Stream.of(
                        Path.of("frontend", "Caddyfile"),
                        Path.of("..", "frontend", "Caddyfile")
                )
                .filter(Files::isRegularFile)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("frontend/Caddyfile not found"));
        return Files.readString(caddyfile);
    }

    private static int countOccurrences(String source, String needle) {
        int count = 0;
        int offset = 0;
        while ((offset = source.indexOf(needle, offset)) >= 0) {
            count++;
            offset += needle.length();
        }
        return count;
    }
}
