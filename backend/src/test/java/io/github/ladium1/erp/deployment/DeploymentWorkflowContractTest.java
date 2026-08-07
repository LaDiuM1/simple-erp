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
}
