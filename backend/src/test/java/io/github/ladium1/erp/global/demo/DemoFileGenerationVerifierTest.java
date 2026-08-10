package io.github.ladium1.erp.global.demo;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class DemoFileGenerationVerifierTest {

    private static final String GENERATION = "11111111-2222-3333-4444-555555555555";
    private static final String OTHER_GENERATION = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";
    private static final String STORED_NAME = "99999999-8888-7777-6666-555555555555";
    private static final String MUTABLE_STORED_NAME = "77777777-6666-5555-4444-333333333333";
    private static final String SEED_VERSION = "demo-v1";
    private static final byte[] CONTENT = "synthetic-file".getBytes(StandardCharsets.UTF_8);
    private static final byte[] MUTABLE_CONTENT = "visitor-file".getBytes(StandardCharsets.UTF_8);

    @TempDir
    private Path tempDir;

    @Mock
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();
    private Path generationRoot;
    private Path objectPath;
    private DemoFileGenerationVerifier verifier;

    @BeforeEach
    void setUp() throws Exception {
        generationRoot = tempDir.resolve(GENERATION);
        objectPath = generationRoot.resolve(Path.of("2026", "08", STORED_NAME));
        Files.createDirectories(objectPath.getParent());
        Files.write(objectPath, CONTENT);
        Files.writeString(generationRoot.resolve(".seed-version"), SEED_VERSION + "\n");
        writeMarker(GENERATION, sha256(CONTENT), CONTENT.length);

        lenient().when(jdbcTemplate.queryForList(contains("FROM stored_files")))
                .thenReturn(validDatabaseRows());
        lenient().when(jdbcTemplate.queryForList(contains("FROM drive_files")))
                .thenReturn(validReferenceRows());
        verifier = new DemoFileGenerationVerifier(
                objectMapper, jdbcTemplate, generationRoot.toString());
    }

    @Test
    @DisplayName("상태·DB·marker·실제 파일이 같은 canonical 세대면 통과")
    void valid_generation_passes() {
        assertThatCode(() -> verifier.verify(readyStatus(GENERATION), SEED_VERSION, 1))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("READY 세대는 DB와 실제 파일이 대응하는 방문자 업로드를 함께 검증")
    void ready_generation_accepts_database_backed_mutable_file() throws Exception {
        writeMutableFile();
        given(jdbcTemplate.queryForList(contains("FROM stored_files")))
                .willReturn(databaseRowsWithMutableFile());
        given(jdbcTemplate.queryForList(contains("FROM drive_files")))
                .willReturn(referenceRowsWithMutableFile());

        assertThatCode(() -> verifier.verify(readyStatus(GENERATION), SEED_VERSION, 1))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("READY 재시작은 업무에 아직 연결되지 않은 PENDING 업로드를 보존")
    void ready_generation_accepts_pending_mutable_file() throws Exception {
        writeMutableFile();
        given(jdbcTemplate.queryForList(contains("FROM stored_files")))
                .willReturn(List.of(validDatabaseRows().getFirst(), mutableDatabaseRow("PENDING", null, null)));

        assertThatCode(() -> verifier.verify(readyStatus(GENERATION), SEED_VERSION, 1))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("READY 재시작은 연결 해제된 DELETE_PENDING 파일을 초기화 전까지 보존")
    void ready_generation_accepts_delete_pending_mutable_file() throws Exception {
        writeMutableFile();
        given(jdbcTemplate.queryForList(contains("FROM stored_files")))
                .willReturn(List.of(validDatabaseRows().getFirst(),
                        mutableDatabaseRow("DELETE_PENDING", "BOARD_POST", 99L)));

        assertThatCode(() -> verifier.verify(readyStatus(GENERATION), SEED_VERSION, 1))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("READY 재시작은 기준 파일의 업무 연결 해제와 DELETE_PENDING 전환을 허용")
    void ready_generation_accepts_delete_pending_canonical_file() {
        Map<String, Object> deletedCanonical = new LinkedHashMap<>(validDatabaseRows().getFirst());
        deletedCanonical.put("status", "DELETE_PENDING");
        given(jdbcTemplate.queryForList(contains("FROM stored_files")))
                .willReturn(List.of(deletedCanonical));
        given(jdbcTemplate.queryForList(contains("FROM drive_files")))
                .willReturn(List.of());

        assertThatCode(() -> verifier.verify(readyStatus(GENERATION), SEED_VERSION, 1))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("VERIFYING 후보 세대는 기준 파일의 상태 변경도 거부")
    void verifying_generation_rejects_mutated_canonical_state() {
        Map<String, Object> deletedCanonical = new LinkedHashMap<>(validDatabaseRows().getFirst());
        deletedCanonical.put("status", "DELETE_PENDING");
        given(jdbcTemplate.queryForList(contains("FROM stored_files")))
                .willReturn(List.of(deletedCanonical));
        given(jdbcTemplate.queryForList(contains("FROM drive_files")))
                .willReturn(List.of());

        assertThatThrownBy(() -> verifier.verify(verifyingStatus(), SEED_VERSION, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("canonical 파일 상태 오류");
    }

    @Test
    @DisplayName("READY 세대도 실제 파일이 없는 방문자 업로드 DB 행은 거부")
    void ready_generation_rejects_mutable_database_row_without_file() {
        given(jdbcTemplate.queryForList(contains("FROM stored_files")))
                .willReturn(databaseRowsWithMutableFile());
        given(jdbcTemplate.queryForList(contains("FROM drive_files")))
                .willReturn(referenceRowsWithMutableFile());

        assertThatThrownBy(() -> verifier.verify(readyStatus(GENERATION), SEED_VERSION, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("materialized file 계약 불일치: fileId=2");
    }

    @Test
    @DisplayName("READY 세대도 DB에 없는 orphan 파일은 거부")
    void ready_generation_rejects_orphan_file() throws Exception {
        writeMutableFile();

        assertThatThrownBy(() -> verifier.verify(readyStatus(GENERATION), SEED_VERSION, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("materialized file 집합 불일치");
    }

    @Test
    @DisplayName("VERIFYING에서는 이전 live 세대가 아니라 candidate 세대를 검증")
    void verifying_state_uses_candidate_generation() {
        assertThatCode(() -> verifier.verify(verifyingStatus(), SEED_VERSION, 1))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("VERIFYING 후보 세대는 방문자 파일이 섞이면 거부")
    void verifying_generation_requires_exact_canonical_files() throws Exception {
        writeMutableFile();
        given(jdbcTemplate.queryForList(contains("FROM stored_files")))
                .willReturn(databaseRowsWithMutableFile());
        assertThatThrownBy(() -> verifier.verify(verifyingStatus(), SEED_VERSION, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DB와 파일 marker의 id 집합 불일치");
    }

    @Test
    @DisplayName("live의 current 상대 symlink는 실제 generation으로 해석해 검증")
    void live_current_symlink_passes() throws Exception {
        Path generations = tempDir.resolve("generations");
        Files.createDirectory(generations);
        Path destination = generations.resolve(GENERATION);
        Files.move(generationRoot, destination);
        Path current = tempDir.resolve("current");
        createSymbolicLinkOrSkip(current, Path.of("generations", GENERATION));
        DemoFileGenerationVerifier liveVerifier = new DemoFileGenerationVerifier(
                objectMapper, jdbcTemplate, current.toString());

        assertThatCode(() -> liveVerifier.verify(readyStatus(GENERATION), SEED_VERSION, 1))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("generation 내부 object symlink는 대상 내용이 같아도 거부")
    void object_symlink_fails() throws Exception {
        Path outside = tempDir.resolve("outside-file");
        Files.write(outside, CONTENT);
        Files.delete(objectPath);
        createSymbolicLinkOrSkip(objectPath, outside);

        assertThatThrownBy(() -> verifier.verify(readyStatus(GENERATION), SEED_VERSION, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("materialized file 계약 불일치");
    }

    @Test
    @DisplayName("파일 볼륨이나 marker가 비어 있으면 시작 검증 실패")
    void empty_file_volume_fails() {
        DemoFileGenerationVerifier emptyVerifier = new DemoFileGenerationVerifier(
                objectMapper, jdbcTemplate, tempDir.resolve("missing").toString());

        assertThatThrownBy(() -> emptyVerifier.verify(readyStatus(GENERATION), SEED_VERSION, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("파일 세대를 읽을 수 없음");
    }

    @Test
    @DisplayName("DB에 대응하는 materialized file이 없으면 시작 검증 실패")
    void missing_materialized_file_fails() throws Exception {
        Files.delete(objectPath);

        assertThatThrownBy(() -> verifier.verify(readyStatus(GENERATION), SEED_VERSION, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("materialized file 계약 불일치");
    }

    @Test
    @DisplayName("READY 세대에서도 canonical 파일 checksum이 marker와 다르면 시작 검증 실패")
    void checksum_mismatch_fails() throws Exception {
        writeMutableFile();
        given(jdbcTemplate.queryForList(contains("FROM stored_files")))
                .willReturn(databaseRowsWithMutableFile());
        given(jdbcTemplate.queryForList(contains("FROM drive_files")))
                .willReturn(referenceRowsWithMutableFile());
        Files.writeString(objectPath, "tampered");

        assertThatThrownBy(() -> verifier.verify(readyStatus(GENERATION), SEED_VERSION, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("materialized file 계약 불일치");
    }

    @Test
    @DisplayName("상태 generation과 mount 세대가 다르면 시작 검증 실패")
    void generation_mismatch_fails() {
        assertThatThrownBy(() -> verifier.verify(readyStatus(OTHER_GENERATION), SEED_VERSION, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("상태와 파일 generation 불일치");
    }

    @Test
    @DisplayName("DB와 marker의 원본명·MIME·크기·생성월이 다르면 시작 검증 실패")
    void database_metadata_mismatch_fails() {
        given(jdbcTemplate.queryForList(contains("FROM stored_files"))).willReturn(List.of(Map.ofEntries(
                Map.entry("id", 1L),
                Map.entry("stored_name", STORED_NAME),
                Map.entry("original_name", "가상_다른문서.txt"),
                Map.entry("content_type", "text/plain"),
                Map.entry("size", (long) CONTENT.length),
                Map.entry("status", "CLAIMED"),
                Map.entry("owner_type", "DRIVE_FILE"),
                Map.entry("owner_id", 1L),
                Map.entry("uploader_id", 1L),
                Map.entry("created_year", "2026"),
                Map.entry("created_month", "08")
        )));

        assertThatThrownBy(() -> verifier.verify(readyStatus(GENERATION), SEED_VERSION, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DB와 파일 marker 메타데이터 불일치");
    }

    @Test
    @DisplayName("설정된 canonical 파일 수와 marker 파일 수가 다르면 시작 검증 실패")
    void required_file_count_mismatch_fails() {
        assertThatThrownBy(() -> verifier.verify(readyStatus(GENERATION), SEED_VERSION, 24))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("canonical file 개수 불일치");
    }

    private void writeMarker(String generation, String checksum, long size) throws Exception {
        Map<String, Object> file = Map.ofEntries(
                Map.entry("contentType", "text/plain"), Map.entry("createdMonth", "08"),
                Map.entry("createdYear", "2026"), Map.entry("id", 1L),
                Map.entry("originalName", "가상_점검문서.txt"), Map.entry("ownerId", 1L),
                Map.entry("ownerType", "DRIVE_FILE"), Map.entry("sha256", checksum),
                Map.entry("size", size), Map.entry("status", "CLAIMED"),
                Map.entry("storedName", STORED_NAME), Map.entry("uploaderId", 1L)
        );
        Map<String, Object> marker = Map.of(
                "files", List.of(file),
                "formatVersion", 1,
                "generation", generation,
                "seedVersion", SEED_VERSION
        );
        Files.writeString(
                generationRoot.resolve(".generation.json"),
                objectMapper.writeValueAsString(marker) + "\n"
        );
    }

    private static List<Map<String, Object>> validDatabaseRows() {
        return List.of(Map.ofEntries(
                Map.entry("id", 1L), Map.entry("stored_name", STORED_NAME),
                Map.entry("original_name", "가상_점검문서.txt"),
                Map.entry("content_type", "text/plain"),
                Map.entry("size", (long) CONTENT.length), Map.entry("status", "CLAIMED"),
                Map.entry("owner_type", "DRIVE_FILE"), Map.entry("owner_id", 1L),
                Map.entry("uploader_id", 1L), Map.entry("created_year", "2026"),
                Map.entry("created_month", "08")
        ));
    }

    private static List<Map<String, Object>> databaseRowsWithMutableFile() {
        return List.of(
                validDatabaseRows().getFirst(),
                mutableDatabaseRow("CLAIMED", "BOARD_POST", 99L)
        );
    }

    private static Map<String, Object> mutableDatabaseRow(
            String status,
            String ownerType,
            Long ownerId
    ) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 2L);
        row.put("stored_name", MUTABLE_STORED_NAME);
        row.put("original_name", "방문자_업로드.txt");
        row.put("content_type", "text/plain");
        row.put("size", (long) MUTABLE_CONTENT.length);
        row.put("status", status);
        row.put("owner_type", ownerType);
        row.put("owner_id", ownerId);
        row.put("uploader_id", 2L);
        row.put("created_year", "2026");
        row.put("created_month", "08");
        return row;
    }

    private static List<Map<String, Object>> validReferenceRows() {
        return List.of(Map.of(
                "file_id", 1L,
                "owner_type", "DRIVE_FILE",
                "owner_id", 1L,
                "uploader_id", 1L
        ));
    }

    private static List<Map<String, Object>> referenceRowsWithMutableFile() {
        return List.of(
                validReferenceRows().getFirst(),
                Map.of(
                        "file_id", 2L,
                        "owner_type", "BOARD_POST",
                        "owner_id", 99L,
                        "uploader_id", 2L
                )
        );
    }

    private void writeMutableFile() throws Exception {
        Files.write(generationRoot.resolve(Path.of("2026", "08", MUTABLE_STORED_NAME)), MUTABLE_CONTENT);
    }

    private static DemoStatusResponse readyStatus(String generation) {
        OffsetDateTime transition = OffsetDateTime.parse("2026-08-02T12:00:00+09:00");
        return new DemoStatusResponse(
                true,
                "DEMO",
                DemoState.READY,
                transition,
                generation,
                null,
                transition,
                transition.plusHours(6),
                300,
                120,
                false,
                "합성 데이터",
                false,
                new DemoSimulatedLocation(37.5663, 126.9779),
                List.of()
        );
    }

    private static DemoStatusResponse verifyingStatus() {
        DemoStatusResponse ready = readyStatus(OTHER_GENERATION);
        return new DemoStatusResponse(
                ready.enabled(), ready.environmentName(), DemoState.VERIFYING,
                ready.stateChangedAt(), ready.generation(), GENERATION,
                ready.lastResetAt(), ready.nextResetAt(),
                ready.warningBeforeSeconds(), ready.writeLockBeforeSeconds(), true,
                ready.notice(), ready.uploadEnabled(), ready.simulatedLocation(), ready.publicAccounts());
    }

    private static String sha256(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    private static void createSymbolicLinkOrSkip(Path link, Path target) {
        try {
            Files.createSymbolicLink(link, target);
        } catch (UnsupportedOperationException | java.io.IOException | SecurityException unavailable) {
            Assumptions.abort("현재 파일시스템에서 symbolic link 생성 불가: " + unavailable.getMessage());
        }
    }
}
