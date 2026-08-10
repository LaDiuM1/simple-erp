package io.github.ladium1.erp.global.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/** DB 파일 메타데이터와 현재 파일 세대가 한 묶음인지 시작 시 검증한다. */
@Component
class DemoFileGenerationVerifier {

    private static final String FILES_SQL = """
            SELECT id, stored_name, original_name, content_type, size,
                   status, owner_type, owner_id, uploader_id,
                   DATE_FORMAT(created_at, '%Y') AS created_year,
                   DATE_FORMAT(created_at, '%m') AS created_month
            FROM stored_files
            ORDER BY id
            """;
    private static final String REFERENCES_SQL = """
            SELECT storage_file_id AS file_id, 'DRIVE_FILE' AS owner_type,
                   id AS owner_id, uploader_id
            FROM drive_files
            UNION ALL
            SELECT paf.file_id, 'BOARD_POST', p.id, p.author_id
            FROM post_attachment_files paf
            JOIN posts p ON p.id = paf.post_id
            UNION ALL
            SELECT ada.file_id,
                   CASE WHEN ad.doc_type = 'EXPENSE' THEN 'EXPENSE_CLAIM'
                        ELSE 'APPROVAL_DOCUMENT' END,
                   CASE WHEN ad.doc_type = 'EXPENSE' THEN ad.ref_id ELSE ad.id END,
                   ad.drafter_id
            FROM approval_document_attachments ada
            JOIN approval_documents ad ON ad.id = ada.document_id
            UNION ALL
            SELECT ei.receipt_file_id, 'EXPENSE_CLAIM', ec.id, ec.claimant_id
            FROM expense_items ei
            JOIN expense_claims ec ON ec.id = ei.claim_id
            WHERE ei.receipt_file_id IS NOT NULL
            """;
    private static final Set<String> MARKER_KEYS = Set.of(
            "files", "formatVersion", "generation", "seedVersion"
    );
    private static final Set<String> FILE_KEYS = Set.of(
            "contentType", "createdMonth", "createdYear", "id",
            "originalName", "ownerId", "ownerType", "sha256", "size",
            "status", "storedName", "uploaderId"
    );
    private static final Pattern GENERATION_PATTERN = Pattern.compile(
            "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"
    );
    private static final Pattern STORED_NAME_PATTERN = Pattern.compile(
            "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"
    );
    private static final Pattern YEAR_PATTERN = Pattern.compile("^20[0-9]{2}$");
    private static final Pattern MONTH_PATTERN = Pattern.compile("^(0[1-9]|1[0-2])$");
    private static final Pattern SHA256_PATTERN = Pattern.compile("^[0-9a-f]{64}$");

    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;
    private final Path storageBasePath;

    DemoFileGenerationVerifier(
            ObjectMapper objectMapper,
            JdbcTemplate jdbcTemplate,
            @Value("${erp.storage.local.base-path}") String storageBasePath
    ) {
        this.objectMapper = objectMapper;
        this.jdbcTemplate = jdbcTemplate;
        this.storageBasePath = Path.of(storageBasePath);
    }

    void verify(DemoStatusResponse status, String expectedSeedVersion, int requiredFileCount) {
        boolean mutableGeneration = status.state() == DemoState.READY;
        String expectedGeneration = status.state() == DemoState.READY
                ? status.generation()
                : status.candidateGeneration();
        if (expectedGeneration == null
                || !GENERATION_PATTERN.matcher(expectedGeneration).matches()) {
            throw invalid("상태의 파일 generation 형식 오류");
        }

        try {
            Path generationRoot = storageBasePath.toRealPath();
            if (!expectedGeneration.equals(generationRoot.getFileName().toString())) {
                throw invalid("상태와 파일 generation 불일치");
            }
            verifySeedVersion(generationRoot, expectedSeedVersion);

            Map<Long, GenerationFile> markerFiles = readGenerationMarker(
                    generationRoot, expectedGeneration, expectedSeedVersion);
            if (markerFiles.size() != requiredFileCount) {
                throw invalid("canonical file 개수 불일치");
            }
            Map<Long, FileMetadata> databaseFiles = readDatabaseFiles();
            if (mutableGeneration
                    ? !databaseFiles.keySet().containsAll(markerFiles.keySet())
                    : !markerFiles.keySet().equals(databaseFiles.keySet())) {
                throw invalid("DB와 파일 marker의 id 집합 불일치");
            }
            verifyOwnership(databaseFiles, markerFiles, readReferences(), mutableGeneration);

            Set<Path> expectedPaths = new HashSet<>();
            expectedPaths.add(Path.of(".seed-version"));
            expectedPaths.add(Path.of(".generation.json"));
            for (Map.Entry<Long, FileMetadata> entry : databaseFiles.entrySet()) {
                FileMetadata databaseFile = entry.getValue();
                GenerationFile canonicalFile = markerFiles.get(entry.getKey());
                if (canonicalFile != null
                        && (mutableGeneration
                        ? !canonicalFile.metadata().hasSameImmutableContent(databaseFile)
                        : !canonicalFile.metadata().equals(databaseFile))) {
                    throw invalid("DB와 파일 marker 메타데이터 불일치: fileId=" + entry.getKey());
                }

                Path relativePath = databaseFile.relativePath();
                expectedPaths.add(relativePath);
                Path objectPath = generationRoot.resolve(relativePath).normalize();
                if (!objectPath.startsWith(generationRoot)
                        || Files.isSymbolicLink(objectPath)
                        || !Files.isRegularFile(objectPath, LinkOption.NOFOLLOW_LINKS)
                        || Files.size(objectPath) != databaseFile.size()
                        || (canonicalFile != null
                        && !canonicalFile.sha256().equals(sha256(objectPath)))) {
                    throw invalid("materialized file 계약 불일치: fileId=" + entry.getKey());
                }
            }
            verifyExactFileSet(generationRoot, expectedPaths);
        } catch (IOException ioException) {
            throw invalid("파일 세대를 읽을 수 없음", ioException);
        }
    }

    private void verifySeedVersion(Path generationRoot, String expectedSeedVersion) throws IOException {
        Path marker = generationRoot.resolve(".seed-version");
        if (Files.isSymbolicLink(marker)
                || !Files.isRegularFile(marker, LinkOption.NOFOLLOW_LINKS)
                || !Files.readString(marker).equals(expectedSeedVersion + "\n")) {
            throw invalid("파일 seedVersion marker 불일치");
        }
    }

    private Map<Long, GenerationFile> readGenerationMarker(
            Path generationRoot,
            String expectedGeneration,
            String expectedSeedVersion
    ) throws IOException {
        Path markerPath = generationRoot.resolve(".generation.json");
        if (Files.isSymbolicLink(markerPath)
                || !Files.isRegularFile(markerPath, LinkOption.NOFOLLOW_LINKS)) {
            throw invalid("파일 generation marker 누락");
        }

        JsonNode marker = objectMapper.readTree(markerPath.toFile());
        requireExactKeys(marker, MARKER_KEYS, "generation marker");
        if (requireLong(marker, "formatVersion") != 1L
                || !expectedGeneration.equals(requireText(marker, "generation"))
                || !expectedSeedVersion.equals(requireText(marker, "seedVersion"))) {
            throw invalid("파일 generation marker header 불일치");
        }

        JsonNode files = marker.get("files");
        if (files == null || !files.isArray()) {
            throw invalid("파일 generation marker files 형식 오류");
        }
        Map<Long, GenerationFile> result = new HashMap<>();
        for (JsonNode file : files) {
            requireExactKeys(file, FILE_KEYS, "generation marker file");
            FileMetadata metadata = new FileMetadata(
                    requireLong(file, "id"),
                    requireText(file, "storedName"),
                    requireText(file, "originalName"),
                    requireText(file, "contentType"),
                    requireLong(file, "size"),
                    requireText(file, "status"),
                    requireText(file, "ownerType"),
                    requireLong(file, "ownerId"),
                    requireLong(file, "uploaderId"),
                    requireText(file, "createdYear"),
                    requireText(file, "createdMonth")
            );
            validateCanonicalMetadata(metadata);
            String sha256 = requireText(file, "sha256");
            if (!SHA256_PATTERN.matcher(sha256).matches()) {
                throw invalid("파일 generation marker SHA-256 형식 오류");
            }
            if (result.putIfAbsent(metadata.id(), new GenerationFile(metadata, sha256)) != null) {
                throw invalid("파일 generation marker id 중복: " + metadata.id());
            }
        }
        return result;
    }

    private Map<Long, FileMetadata> readDatabaseFiles() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(FILES_SQL);
        Map<Long, FileMetadata> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            FileMetadata metadata = new FileMetadata(
                    requireNumber(row.get("id"), "DB file id"),
                    requireString(row.get("stored_name"), "DB storedName"),
                    requireString(row.get("original_name"), "DB originalName"),
                    requireString(row.get("content_type"), "DB contentType"),
                    requireNumber(row.get("size"), "DB file size"),
                    requireString(row.get("status"), "DB status"),
                    optionalString(row.get("owner_type")),
                    optionalNumber(row.get("owner_id"), "DB ownerId"),
                    requireNumber(row.get("uploader_id"), "DB uploaderId"),
                    requireString(row.get("created_year"), "DB createdYear"),
                    requireString(row.get("created_month"), "DB createdMonth")
            );
            validateDatabaseMetadata(metadata);
            if (result.putIfAbsent(metadata.id(), metadata) != null) {
                throw invalid("DB stored_files id 중복: " + metadata.id());
            }
        }
        return result;
    }

    private Map<Long, Set<OwnerTuple>> readReferences() {
        Map<Long, Set<OwnerTuple>> result = new HashMap<>();
        for (Map<String, Object> row : jdbcTemplate.queryForList(REFERENCES_SQL)) {
            long fileId = requireNumber(row.get("file_id"), "업무 참조 fileId");
            OwnerTuple owner = new OwnerTuple(
                    requireString(row.get("owner_type"), "업무 참조 ownerType"),
                    requireNumber(row.get("owner_id"), "업무 참조 ownerId"),
                    requireNumber(row.get("uploader_id"), "업무 참조 uploaderId")
            );
            result.computeIfAbsent(fileId, ignored -> new HashSet<>()).add(owner);
        }
        return result;
    }

    private static void verifyOwnership(
            Map<Long, FileMetadata> databaseFiles,
            Map<Long, GenerationFile> markerFiles,
            Map<Long, Set<OwnerTuple>> references,
            boolean mutableGeneration
    ) {
        if (!databaseFiles.keySet().containsAll(references.keySet())) {
            throw invalid("존재하지 않는 파일을 가리키는 업무 참조 감지");
        }
        databaseFiles.forEach((fileId, metadata) -> {
            Set<OwnerTuple> expected = references.getOrDefault(fileId, Set.of());
            OwnerTuple stored = metadata.ownerTuple();
            switch (metadata.status()) {
                case "CLAIMED" -> {
                    if (expected.size() != 1 || !expected.contains(stored)) {
                        throw invalid("CLAIMED 파일 소유권·업무 참조 불일치: fileId=" + fileId);
                    }
                }
                case "PENDING" -> {
                    if (stored != null || !expected.isEmpty()) {
                        throw invalid("PENDING 파일 소유권·업무 참조 불일치: fileId=" + fileId);
                    }
                }
                case "DELETE_PENDING" -> {
                    if (stored == null || !expected.isEmpty()) {
                        throw invalid("DELETE_PENDING 파일 소유권·업무 참조 불일치: fileId=" + fileId);
                    }
                }
                default -> throw invalid("stored_files 상태 오류: fileId=" + fileId);
            }
            if (!mutableGeneration
                    && markerFiles.containsKey(fileId)
                    && !"CLAIMED".equals(metadata.status())) {
                throw invalid("canonical 파일 상태 오류: fileId=" + fileId);
            }
        });
    }

    private static void verifyExactFileSet(Path generationRoot, Set<Path> expectedPaths)
            throws IOException {
        Set<Path> actualPaths = new HashSet<>();
        try (Stream<Path> paths = Files.walk(generationRoot)) {
            for (Path path : paths.toList()) {
                if (path.equals(generationRoot)) {
                    continue;
                }
                if (Files.isSymbolicLink(path)) {
                    throw invalid("파일 세대 내부 symbolic link 감지");
                }
                if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
                    actualPaths.add(generationRoot.relativize(path));
                } else if (!Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                    throw invalid("파일 세대 내부의 알 수 없는 객체 감지");
                }
            }
        }
        if (!actualPaths.equals(expectedPaths)) {
            throw invalid("materialized file 집합 불일치");
        }
    }

    private static void requireExactKeys(JsonNode node, Set<String> expected, String label) {
        if (node == null || !node.isObject() || !Set.copyOf(node.propertyNames()).equals(expected)) {
            throw invalid(label + " key 계약 불일치");
        }
    }

    private static String requireText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || !value.isString() || value.stringValue().isBlank()) {
            throw invalid("generation marker " + field + " 형식 오류");
        }
        return value.stringValue();
    }

    private static long requireLong(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || !value.isIntegralNumber()) {
            throw invalid("generation marker " + field + " 형식 오류");
        }
        OptionalLong parsed = value.longValueOpt();
        if (parsed.isEmpty()) {
            throw invalid("generation marker " + field + " 범위 오류");
        }
        return parsed.getAsLong();
    }

    private static long requireNumber(Object value, String label) {
        if (!(value instanceof Number number)) {
            throw invalid(label + " 형식 오류");
        }
        return number.longValue();
    }

    private static Long optionalNumber(Object value, String label) {
        return value == null ? null : requireNumber(value, label);
    }

    private static String requireString(Object value, String label) {
        if (value == null || value.toString().isBlank()) {
            throw invalid(label + " 형식 오류");
        }
        return value.toString();
    }

    private static String optionalString(Object value) {
        return value == null ? null : value.toString();
    }

    private static void validateCommonMetadata(FileMetadata metadata) {
        if (metadata.id() <= 0
                || metadata.size() <= 0
                || !STORED_NAME_PATTERN.matcher(metadata.storedName()).matches()
                || metadata.originalName().isBlank()
                || metadata.contentType().isBlank()
                || metadata.uploaderId() == null
                || metadata.uploaderId() <= 0
                || !YEAR_PATTERN.matcher(metadata.createdYear()).matches()
                || !MONTH_PATTERN.matcher(metadata.createdMonth()).matches()) {
            throw invalid("stored_files 메타데이터 형식 오류: fileId=" + metadata.id());
        }
    }

    private static void validateCanonicalMetadata(FileMetadata metadata) {
        validateCommonMetadata(metadata);
        if (!"CLAIMED".equals(metadata.status()) || metadata.ownerTuple() == null) {
            throw invalid("canonical stored_files 소유권 형식 오류: fileId=" + metadata.id());
        }
    }

    private static void validateDatabaseMetadata(FileMetadata metadata) {
        validateCommonMetadata(metadata);
        if (!Set.of("CLAIMED", "PENDING", "DELETE_PENDING").contains(metadata.status())
                || (metadata.ownerType() == null) != (metadata.ownerId() == null)
                || (metadata.ownerType() != null
                && !Set.of("DRIVE_FILE", "BOARD_POST", "APPROVAL_DOCUMENT", "EXPENSE_CLAIM")
                        .contains(metadata.ownerType()))) {
            throw invalid("stored_files 소유권 형식 오류: fileId=" + metadata.id());
        }
    }

    private static String sha256(Path path) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", impossible);
        }
        try (InputStream input = Files.newInputStream(path);
             DigestInputStream ignored = new DigestInputStream(input, digest)) {
            ignored.transferTo(OutputStream.nullOutputStream());
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static IllegalStateException invalid(String reason) {
        return new IllegalStateException("데모 canonical file generation 검증 실패: " + reason);
    }

    private static IllegalStateException invalid(String reason, Exception cause) {
        return new IllegalStateException("데모 canonical file generation 검증 실패: " + reason, cause);
    }

    private record GenerationFile(FileMetadata metadata, String sha256) {
    }

    private record FileMetadata(
            long id,
            String storedName,
            String originalName,
            String contentType,
            long size,
            String status,
            String ownerType,
            Long ownerId,
            Long uploaderId,
            String createdYear,
            String createdMonth
    ) {
        private Path relativePath() {
            return Path.of(createdYear, createdMonth, storedName);
        }

        private OwnerTuple ownerTuple() {
            return ownerType == null || ownerId == null || uploaderId == null
                    ? null
                    : new OwnerTuple(ownerType, ownerId, uploaderId);
        }

        private boolean hasSameImmutableContent(FileMetadata other) {
            return id == other.id
                    && size == other.size
                    && storedName.equals(other.storedName)
                    && originalName.equals(other.originalName)
                    && contentType.equals(other.contentType)
                    && uploaderId.equals(other.uploaderId)
                    && createdYear.equals(other.createdYear)
                    && createdMonth.equals(other.createdMonth);
        }
    }

    private record OwnerTuple(String ownerType, long ownerId, long uploaderId) {
    }

}
