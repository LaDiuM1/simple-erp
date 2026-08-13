#!/usr/bin/env python3
"""Fail-closed control-plane helpers for the Simple ERP demo."""

from __future__ import annotations

import argparse
import datetime as dt
from dataclasses import dataclass
import gzip
import hashlib
import io
import json
import os
from pathlib import Path, PurePosixPath
import re
import shutil
import stat
import struct
import sys
import tarfile
import time
import urllib.error
import urllib.parse
import urllib.request
import uuid
import xml.etree.ElementTree as ET
import zipfile
import zlib


KST = dt.timezone(dt.timedelta(hours=9))
XLSX_MAIN_NAMESPACE = "http://schemas.openxmlformats.org/spreadsheetml/2006/main"
XLSX_RELATIONSHIP_NAMESPACE = "http://schemas.openxmlformats.org/officeDocument/2006/relationships"
MAX_UPLOAD_FILE_SIZE_BYTES = 30 * 1024 * 1024
ACCEPTANCE_ATTACHMENT_SIZE_BYTES = MAX_UPLOAD_FILE_SIZE_BYTES
GENERATION_PATTERN = re.compile(
    r"[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"
)
STORED_NAME_PATTERN = re.compile(
    r"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"
)
ALLOWED_STATE_FILES = {"status.json", "preflight.json"}
GENERATION_DIRECTORY_MODE = 0o2775
GENERATION_SEED_FILE_MODE = 0o664
ALLOWED_MANIFEST_KEYS = {
    "compatibleAppVersion",
    "generatedAt",
    "schemaSourceCommit",
    "schemaVersion",
    "scenarioVersion",
    "seedVersion",
    "sourceDateEpoch",
    "timezone",
}
MANIFEST_KEYS = {
    "formatVersion",
    "seedVersion",
    "schemaVersion",
    "scenarioVersion",
    "schemaSourceCommit",
    "compatibleAppVersion",
    "generatedAt",
    "sourceDateEpoch",
    "timezone",
    "schemaSha256",
    "dataSha256",
    "filesSha256",
    "expectedSchemaTableCount",
    "expectedCounts",
    "startupDelta",
    "publicAccounts",
    "files",
    "privacy",
}
FILE_MANIFEST_KEYS = {
    "id",
    "storedName",
    "originalName",
    "contentType",
    "size",
    "sha256",
    "createdAtDaysAgo",
    "status",
    "ownerType",
    "ownerId",
    "uploaderId",
}
EXPECTED_COUNTS: dict[str, object] = {
    "acquisition_sources": 8,
    "after_services": 45,
    "approval_document_attachments": 14,
    "approval_documents": 36,
    "approval_steps": 52,
    "attendances": {"min": 380, "max": 450},
    "audit_logs": 90,
    "code_rule_attribute_mappings": 0,
    "code_rules": 5,
    "code_sequences": {"min": 6, "max": 8},
    "contract_notes": 56,
    "contract_payments": 84,
    "contracts": 42,
    "customers": 48,
    "demo_seed_manifest": 1,
    "departments": 8,
    "drive_files": 12,
    "drive_folders": 10,
    "employees": 22,
    "engineers": 9,
    "equipments": 17,
    "event_publication": 0,
    "expense_claims": 12,
    "expense_items": 30,
    "leave_balances": 20,
    "leave_requests": 16,
    "positions": 7,
    "post_attachment_files": 4,
    "post_comments": 64,
    "posts": 28,
    "product_categories": 10,
    "products": 32,
    "role_menus": 52,
    "roles": 3,
    "sales_activities": 144,
    "sales_assignments": 60,
    "sales_contact_employments": 84,
    "sales_contact_sources": 96,
    "sales_contacts": 72,
    "service_expenses": 90,
    "service_visits": 60,
    "stored_files": 30,
    "suppliers": 8,
}
EXPECTED_PRIVACY = {
    "syntheticOnly": True,
    "operatorCredentialsIncluded": False,
    "supplierNameContract": "curated fictional supplier catalog",
    "contactDomain": ".example",
    "invalidRegistrationPrefix": "000",
    "identityNameContract": "curated fictional identity catalog",
    "invalidPhonePrefix": "000",
    "fileNameContract": "deterministic business fixture catalog",
}
EXPECTED_STARTUP_DELTA = {
    "employees": 1,
    "reason": "EmployeeInitializer creates the recovery operator from APP_ADMIN_*",
}
EXPECTED_PUBLIC_ACCOUNTS = [
    {
        "loginId": "demo.manager",
        "password": "ManagerDemo!2026",
        "role": "DEMO_MANAGER",
        "recommended": True,
    },
    {
        "loginId": "demo.staff",
        "password": "StaffDemo!2026",
        "role": "DEMO_STAFF",
        "recommended": False,
    },
]
FIXTURE_MIME_SUFFIXES = {
    "application/pdf": ".pdf",
    "image/png": ".png",
    "text/plain": ".txt",
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet": ".xlsx",
}
EXPECTED_FIXTURE_NAMES = (
    "영업회의_현황_01.pdf", "장비점검_일정_01.pdf",
    "현장방문_체크리스트_01.pdf", "설치일정_동선도_01.pdf",
    "출장계획_첨부_01.pdf", "기술교육_운영안_01.pdf",
    "고객요구사항_정리_01.xlsx", "고객요구사항_정리_02.xlsx",
    "고객요구사항_정리_03.xlsx", "고객요구사항_정리_04.xlsx",
    "장비점검_안내_01.txt", "장비점검_안내_02.txt",
    "영업회의_회의록_첨부.pdf", "정기점검_일정표.pdf",
    "현장방문_체크리스트.pdf", "설치동선_검토자료.pdf",
    "미르온정밀_출장계획.pdf", "솔누리금속_출장계획.pdf",
    "출장비_증빙묶음_01.png", "출장비_증빙묶음_02.png",
    "출장비_증빙묶음_03.png", "출장비_증빙묶음_04.png",
    "출장비_증빙묶음_05.png", "출장비_증빙묶음_06.png",
    "출장비_증빙묶음_07.png", "출장비_증빙묶음_08.png",
    "출장비_증빙묶음_09.png", "출장비_증빙묶음_10.png",
    "출장비_증빙묶음_11.png", "출장비_증빙묶음_12.png",
)
SHA256_PATTERN = re.compile(r"[0-9a-f]{64}")
MAX_ARCHIVE_BYTES = 4 * 1024 * 1024
MAX_ARCHIVE_EXPANDED_BYTES = 8 * 1024 * 1024
MAX_FIXTURE_BYTES = 1024 * 1024
MAX_XLSX_XML_BYTES = 256 * 1024
MAX_XLSX_TOTAL_BYTES = 1024 * 1024
MAX_PNG_RAW_BYTES = 8 * 1024 * 1024
PRE_RESET_GENERATION_RETENTION = 4
FAILED_WORK_RETENTION = 4
RECOGNIZED_LOG_RETENTION = 20
RECOGNIZED_LOG_PATTERN = re.compile(
    rf"(?:preflight|backend)-({GENERATION_PATTERN.pattern})\.log"
)
STAGING_ARTIFACT_PATTERN = re.compile(rf"\.staging-({GENERATION_PATTERN.pattern})")
CURRENT_TEMP_PATTERN = re.compile(rf"\.current-({GENERATION_PATTERN.pattern})")
STATE_TEMP_PATTERN = re.compile(r"\.(?:status|preflight)\.json(?:\.[1-9][0-9]*)?\.tmp")
RETENTION_FAILURE_LOG_NAME = "retention-failure.log"
RETENTION_FAILURE_TEMP_NAME = ".retention-failure.log.tmp"
MAX_RETENTION_FAILURE_LOG_BYTES = 4096
CONTROL_PLANE_FAILURE_STAGE_PATTERN = re.compile(
    r"[a-z][a-z0-9]*(?:-[a-z0-9]+)*"
)
READ_SMOKE_PATHS = (
    "/api/v1/dashboard/summary",
    "/api/v1/dashboard/sales",
    "/api/v1/dashboard/service",
    "/api/v1/dashboard/warranty",
    "/api/v1/customers?size=1",
    "/api/v1/contracts?size=1",
    "/api/v1/equipments?size=1",
    "/api/v1/after-services?size=1",
    "/api/v1/approvals?box=INVOLVED&size=1",
    "/api/v1/expenses?size=1",
)
ACCEPTANCE_MARKER_PATTERN = re.compile(r"demo-it-[0-9a-f]{32}")


class ControlError(RuntimeError):
    pass


@dataclass(frozen=True)
class StoredFileMapping:
    stored_name: str
    original_name: str
    content_type: str
    size: int
    created_date: dt.date
    reset_date: dt.date
    status: str
    owner_type: str
    owner_id: int
    uploader_id: int

    @property
    def year(self) -> str:
        return f"{self.created_date.year:04d}"

    @property
    def month(self) -> str:
        return f"{self.created_date.month:02d}"


@dataclass(frozen=True)
class CleanupTarget:
    path: Path
    kind: str
    device: int
    inode: int
    parent_device: int
    parent_inode: int


@dataclass(frozen=True)
class CleanupPlan:
    phase: str
    targets: tuple[CleanupTarget, ...]
    preserved_generations: tuple[str, ...]


def strict_json_equal(actual: object, expected: object) -> bool:
    if type(actual) is not type(expected):
        return False
    if isinstance(expected, dict):
        return actual.keys() == expected.keys() and all(
            strict_json_equal(actual[key], value) for key, value in expected.items()
        )
    if isinstance(expected, list):
        return len(actual) == len(expected) and all(
            strict_json_equal(left, right) for left, right in zip(actual, expected)
        )
    return actual == expected


def valid_fixture_name(value: object, content_type: object) -> bool:
    suffix = FIXTURE_MIME_SUFFIXES.get(content_type) if isinstance(content_type, str) else None
    return (
        isinstance(value, str)
        and 1 <= len(value) <= 255
        and suffix is not None
        and value.lower().endswith(suffix)
        and value not in {".", ".."}
        and value == value.strip()
        and not any(character in value for character in ("/", "\\", "\0", "\r", "\n", "\t"))
    )


def reject_duplicate_keys(pairs: list[tuple[str, object]]) -> dict[str, object]:
    result: dict[str, object] = {}
    for key, value in pairs:
        if key in result:
            raise ValueError(f"duplicate JSON key: {key}")
        result[key] = value
    return result


def sha256_bytes(payload: bytes) -> str:
    return hashlib.sha256(payload).hexdigest()


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def load_manifest(seed_dir: Path) -> dict[str, object]:
    try:
        manifest = json.loads(
            (seed_dir / "manifest.json").read_text(encoding="utf-8"),
            object_pairs_hook=reject_duplicate_keys,
        )
    except (OSError, UnicodeError, json.JSONDecodeError, ValueError) as error:
        raise ControlError(f"manifest를 읽을 수 없음: {error}") from error
    if not isinstance(manifest, dict):
        raise ControlError("manifest root는 object여야 함")
    return manifest


def validate_bundle(seed_dir: Path, expected_app_version: str | None = None) -> dict[str, object]:
    seed_dir = seed_dir.resolve(strict=True)
    manifest = load_manifest(seed_dir)
    if set(manifest) != MANIFEST_KEYS:
        missing = sorted(MANIFEST_KEYS.difference(manifest))
        extra = sorted(set(manifest).difference(MANIFEST_KEYS))
        raise ControlError(f"manifest 키 집합 불일치: missing={missing}, extra={extra}")
    if type(manifest["formatVersion"]) is not int or manifest["formatVersion"] != 1:
        raise ControlError("지원하지 않는 manifest formatVersion")
    if type(manifest["expectedSchemaTableCount"]) is not int:
        raise ControlError("manifest expectedSchemaTableCount 형식 오류")
    seed_version = manifest["seedVersion"]
    if not isinstance(seed_version, str) or not re.fullmatch(
        r"[0-9]{4}\.[0-9]{2}\.[0-9]{2}\.[0-9]+", seed_version
    ):
        raise ControlError("manifest seedVersion 형식 오류")
    schema_source_commit = manifest["schemaSourceCommit"]
    if not isinstance(schema_source_commit, str) or not re.fullmatch(
        r"[0-9a-f]{40}", schema_source_commit
    ):
        raise ControlError("manifest schemaSourceCommit 형식 오류")
    for key in ("schemaVersion", "scenarioVersion", "compatibleAppVersion"):
        value = manifest[key]
        if not isinstance(value, str) or not value or len(value) > 100:
            raise ControlError(f"manifest {key} 형식 오류")
    for key in ("schemaSha256", "dataSha256", "filesSha256"):
        value = manifest[key]
        if not isinstance(value, str) or not SHA256_PATTERN.fullmatch(value):
            raise ControlError(f"manifest {key} 형식 오류")
    if not strict_json_equal(manifest["expectedCounts"], EXPECTED_COUNTS):
        raise ControlError("manifest expectedCounts 계약 불일치")
    if not strict_json_equal(manifest["startupDelta"], EXPECTED_STARTUP_DELTA):
        raise ControlError("recovery operator startupDelta 계약 불일치")
    if not strict_json_equal(manifest["publicAccounts"], EXPECTED_PUBLIC_ACCOUNTS):
        raise ControlError("데모 계정 계약 불일치")
    if not strict_json_equal(manifest["privacy"], EXPECTED_PRIVACY):
        raise ControlError("manifest 합성 데이터 계약 불일치")
    validate_manifest_time_contract(manifest)
    if expected_app_version and manifest["compatibleAppVersion"] != expected_app_version:
        raise ControlError(
            "애플리케이션 호환 버전 불일치: "
            f"manifest={manifest['compatibleAppVersion']}, runtime={expected_app_version}"
        )

    artifacts = {
        "schema.sql": "schemaSha256",
        "seed-data.sql": "dataSha256",
        "seed-files.tar.gz": "filesSha256",
    }
    for name, checksum_key in artifacts.items():
        path = seed_dir / name
        if not path.is_file():
            raise ControlError(f"seed artifact 누락: {name}")
        actual = sha256_file(path)
        if actual != manifest[checksum_key]:
            raise ControlError(f"seed artifact checksum 불일치: {name}")

    schema = (seed_dir / "schema.sql").read_text(encoding="utf-8")
    table_count = len(re.findall(r"^CREATE TABLE `", schema, flags=re.MULTILINE))
    if table_count != manifest["expectedSchemaTableCount"]:
        raise ControlError(
            f"schema table count 불일치: actual={table_count}, "
            f"expected={manifest['expectedSchemaTableCount']}"
        )
    if manifest["expectedSchemaTableCount"] != 43:
        raise ControlError("canonical demo schema는 정확히 43개 테이블이어야 함")

    files = manifest["files"]
    if not isinstance(files, list) or len(files) != 30:
        raise ControlError("manifest files는 정확히 30건이어야 함")
    expected_objects: dict[str, dict[str, object]] = {}
    expected_ids: set[int] = set()
    for raw in files:
        if not isinstance(raw, dict):
            raise ControlError("manifest file 항목 형식 오류")
        if set(raw) != FILE_MANIFEST_KEYS:
            raise ControlError(f"manifest file 키 집합 오류: {sorted(raw)}")
        file_id = raw.get("id")
        stored_name = raw.get("storedName")
        original_name = raw.get("originalName")
        content_type = raw.get("contentType")
        size = raw.get("size")
        checksum = raw.get("sha256")
        created_days_ago = raw.get("createdAtDaysAgo")
        status = raw.get("status")
        owner_type = raw.get("ownerType")
        owner_id = raw.get("ownerId")
        uploader_id = raw.get("uploaderId")
        if (
            type(file_id) is not int
            or not 1 <= file_id <= len(EXPECTED_FIXTURE_NAMES)
            or file_id in expected_ids
            or status != "CLAIMED"
            or owner_type not in {"DRIVE_FILE", "BOARD_POST", "APPROVAL_DOCUMENT", "EXPENSE_CLAIM"}
            or type(owner_id) is not int or owner_id < 1
            or type(uploader_id) is not int or uploader_id < 1
        ):
            raise ControlError("manifest file id 중복 또는 형식 오류")
        if not isinstance(stored_name, str) or not STORED_NAME_PATTERN.fullmatch(stored_name):
            raise ControlError(f"manifest storedName 형식 오류: {stored_name}")
        if (
            not valid_fixture_name(original_name, content_type)
            or original_name != EXPECTED_FIXTURE_NAMES[file_id - 1]
        ):
            raise ControlError(f"manifest originalName 형식 오류: {original_name}")
        if content_type not in FIXTURE_MIME_SUFFIXES:
            raise ControlError(f"manifest contentType allowlist 위반: {content_type}")
        if type(size) is not int or not 1 <= size <= MAX_FIXTURE_BYTES:
            raise ControlError(f"manifest file size 형식 오류: {file_id}")
        if not isinstance(checksum, str) or not SHA256_PATTERN.fullmatch(checksum):
            raise ControlError(f"manifest file sha256 형식 오류: {file_id}")
        if type(created_days_ago) is not int or not 0 <= created_days_ago <= 3660:
            raise ControlError(f"manifest createdAtDaysAgo 형식 오류: {file_id}")
        name = f"objects/{stored_name}"
        if name in expected_objects:
            raise ControlError(f"manifest storedName 중복: {stored_name}")
        expected_ids.add(file_id)
        expected_objects[name] = raw
    if expected_ids != set(range(1, 31)):
        raise ControlError("manifest file id는 1..30 연속 집합이어야 함")

    archive_path = seed_dir / "seed-files.tar.gz"
    if archive_path.stat().st_size > MAX_ARCHIVE_BYTES:
        raise ControlError("seed archive 크기 상한 초과")
    archive_bytes = archive_path.read_bytes()
    with gzip.GzipFile(fileobj=io.BytesIO(archive_bytes)) as compressed:
        tar_bytes = compressed.read(MAX_ARCHIVE_EXPANDED_BYTES + 1024 * 1024 + 1)
        if compressed.mtime != 0:
            raise ControlError("GZIP mtime은 0이어야 함")
        if len(tar_bytes) > MAX_ARCHIVE_EXPANDED_BYTES + 1024 * 1024:
            raise ControlError("seed archive TAR size 상한 초과")
    with tarfile.open(fileobj=io.BytesIO(tar_bytes), mode="r:") as archive:
        members = archive.getmembers()
        names = [member.name for member in members]
        if names != sorted(names) or set(names) != set(expected_objects) or len(names) != len(set(names)):
            raise ControlError("archive 객체 집합·순서가 manifest와 다름")
        if sum(member.size for member in members) > MAX_ARCHIVE_EXPANDED_BYTES:
            raise ControlError("seed archive expanded size 상한 초과")
        for member in members:
            path = PurePosixPath(member.name)
            if (
                not member.isfile()
                or path.is_absolute()
                or ".." in path.parts
                or len(path.parts) != 2
                or path.parts[0] != "objects"
                or member.mtime != 0
                or member.uid != 0
                or member.gid != 0
                or member.mode != 0o644
                or member.size < 1
                or member.size > MAX_FIXTURE_BYTES
            ):
                raise ControlError(f"안전하지 않은 archive member: {member.name}")
            extracted = archive.extractfile(member)
            if extracted is None:
                raise ControlError(f"archive member를 읽을 수 없음: {member.name}")
            payload = extracted.read(MAX_FIXTURE_BYTES + 1)
            expected = expected_objects[member.name]
            if len(payload) != expected.get("size") or sha256_bytes(payload) != expected.get("sha256"):
                raise ControlError(f"archive member checksum 불일치: {member.name}")
            validate_fixture(payload, str(expected.get("contentType")), member.name)

    return manifest


def validate_manifest_time_contract(manifest: dict[str, object]) -> None:
    generated_raw = manifest.get("generatedAt")
    timezone = manifest.get("timezone")
    source_epoch = manifest.get("sourceDateEpoch")
    if not isinstance(generated_raw, str):
        raise ControlError("manifest generatedAt 형식 오류")
    if timezone != "Asia/Seoul":
        raise ControlError(f"manifest timezone은 Asia/Seoul이어야 함: {timezone}")
    if isinstance(source_epoch, bool) or not isinstance(source_epoch, int):
        raise ControlError("manifest sourceDateEpoch은 정수 epoch seconds여야 함")
    try:
        generated = dt.datetime.fromisoformat(generated_raw.replace("Z", "+00:00"))
        from_epoch = dt.datetime.fromtimestamp(source_epoch, tz=KST)
    except (ValueError, OverflowError, OSError) as error:
        raise ControlError("manifest 생성 시각을 해석할 수 없음") from error
    if generated.tzinfo is None or generated.utcoffset() != KST.utcoffset(None):
        raise ControlError("manifest generatedAt은 Asia/Seoul +09:00 aware datetime이어야 함")
    if generated != from_epoch or generated.astimezone(KST).isoformat(timespec="seconds") != generated_raw:
        raise ControlError(
            "manifest generatedAt·timezone·sourceDateEpoch이 같은 시각을 가리키지 않음"
        )


def validate_fixture(payload: bytes, content_type: str, name: str) -> None:
    if not 1 <= len(payload) <= MAX_FIXTURE_BYTES:
        raise ControlError(f"fixture 크기 경계 오류: {name}")
    if content_type == "application/pdf":
        validate_pdf_fixture(payload, name)
    elif content_type == "image/png":
        validate_png_fixture(payload, name)
    elif content_type == "text/plain":
        validate_text_fixture(payload, name)
    elif content_type == "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
        validate_xlsx_fixture(payload, name)
    else:
        raise ControlError(f"허용되지 않은 fixture MIME: {content_type}")


def validate_pdf_fixture(payload: bytes, name: str) -> None:
    forbidden = (
        b"/JavaScript",
        b"/JS",
        b"/OpenAction",
        b"/AA",
        b"/Launch",
        b"/EmbeddedFile",
        b"/Filespec",
        b"/AcroForm",
        b"/RichMedia",
        b"/SubmitForm",
        b"/GoToR",
        b"/URI",
        b"/Encrypt",
        b"/ObjStm",
        b"/XRef",
    )
    if not payload.startswith(b"%PDF-1.4\n%\xe2\xe3\xcf\xd3\n"):
        raise ControlError(f"PDF signature/version 오류: {name}")
    if b"DEMO DOCUMENT - NO LEGAL EFFECT" not in payload:
        raise ControlError(f"PDF synthetic marker 오류: {name}")
    if any(marker in payload for marker in forbidden):
        raise ControlError(f"PDF active/external content 금지: {name}")
    if payload.count(b"%%EOF") != 1:
        raise ControlError(f"PDF EOF 경계 오류: {name}")
    footer = re.search(rb"startxref\r?\n([0-9]+)\r?\n%%EOF\s*\Z", payload)
    if footer is None:
        raise ControlError(f"PDF startxref 누락: {name}")
    xref_offset = int(footer.group(1))
    if not 0 < xref_offset < footer.start() or payload[xref_offset:xref_offset + 5] != b"xref\n":
        raise ControlError(f"PDF xref offset 오류: {name}")
    lines = payload[xref_offset:footer.start()].splitlines()
    if len(lines) < 5 or lines[0] != b"xref":
        raise ControlError(f"PDF xref 형식 오류: {name}")
    header = re.fullmatch(rb"0 ([1-9][0-9]{0,2})", lines[1])
    if header is None:
        raise ControlError(f"PDF xref 범위 오류: {name}")
    object_count = int(header.group(1))
    if object_count < 2 or len(lines) != object_count + 3:
        raise ControlError(f"PDF xref 항목 수 오류: {name}")
    if lines[2] != b"0000000000 65535 f ":
        raise ControlError(f"PDF xref free entry 오류: {name}")
    offsets: set[int] = set()
    for object_number, line in enumerate(lines[3:object_count + 2], 1):
        entry = re.fullmatch(rb"([0-9]{10}) 00000 n ", line)
        if entry is None:
            raise ControlError(f"PDF xref entry 오류: {name}")
        offset = int(entry.group(1))
        marker = f"{object_number} 0 obj\n".encode("ascii")
        if offset in offsets or payload[offset:offset + len(marker)] != marker:
            raise ControlError(f"PDF object offset 오류: {name}")
        offsets.add(offset)
    trailer = lines[-1]
    expected_trailer = f"trailer << /Size {object_count} /Root 1 0 R >>".encode("ascii")
    if trailer != expected_trailer or b"/Type /Catalog" not in payload:
        raise ControlError(f"PDF trailer/catalog 오류: {name}")


def validate_png_fixture(payload: bytes, name: str) -> None:
    if not payload.startswith(b"\x89PNG\r\n\x1a\n"):
        raise ControlError(f"PNG signature 오류: {name}")
    position = 8
    chunks: list[tuple[bytes, bytes]] = []
    while position < len(payload):
        if position + 12 > len(payload):
            raise ControlError(f"PNG truncated chunk: {name}")
        length = struct.unpack(">I", payload[position:position + 4])[0]
        kind = payload[position + 4:position + 8]
        end = position + 12 + length
        if length > MAX_FIXTURE_BYTES or end > len(payload) or not re.fullmatch(rb"[A-Za-z]{4}", kind):
            raise ControlError(f"PNG chunk 경계 오류: {name}")
        data = payload[position + 8:position + 8 + length]
        expected_crc = struct.unpack(">I", payload[position + 8 + length:end])[0]
        if zlib.crc32(kind + data) & 0xFFFFFFFF != expected_crc:
            raise ControlError(f"PNG CRC 오류: {name}")
        chunks.append((kind, data))
        position = end
        if kind == b"IEND":
            break
    if position != len(payload) or [kind for kind, _ in chunks] != [b"IHDR", b"tEXt", b"IDAT", b"IEND"]:
        raise ControlError(f"PNG chunk allowlist/order 위반: {name}")
    ihdr = chunks[0][1]
    if len(ihdr) != 13:
        raise ControlError(f"PNG IHDR 길이 오류: {name}")
    width, height, bit_depth, color_type, compression, filtering, interlace = struct.unpack(
        ">IIBBBBB", ihdr
    )
    if (
        not 1 <= width <= 4096
        or not 1 <= height <= 4096
        or (bit_depth, color_type, compression, filtering, interlace) != (8, 2, 0, 0, 0)
    ):
        raise ControlError(f"PNG IHDR 계약 위반: {name}")
    if chunks[1][1] != b"Description\x00SYNTHETIC DEMO DATA - NO REAL RECEIPT":
        raise ControlError(f"PNG synthetic marker 오류: {name}")
    if chunks[-1][1] != b"":
        raise ControlError(f"PNG IEND payload 오류: {name}")
    expected_raw_size = height * (1 + width * 3)
    if expected_raw_size > MAX_PNG_RAW_BYTES:
        raise ControlError(f"PNG decoded size 상한 초과: {name}")
    decompressor = zlib.decompressobj()
    try:
        raw = decompressor.decompress(chunks[2][1], MAX_PNG_RAW_BYTES + 1)
        if len(raw) > MAX_PNG_RAW_BYTES:
            raise ControlError(f"PNG decoded size 상한 초과: {name}")
        raw += decompressor.flush(MAX_PNG_RAW_BYTES + 1 - len(raw))
    except zlib.error as error:
        raise ControlError(f"PNG IDAT 압축 오류: {name}") from error
    if (
        len(raw) != expected_raw_size
        or not decompressor.eof
        or decompressor.unused_data
        or decompressor.unconsumed_tail
    ):
        raise ControlError(f"PNG IDAT 길이/종료 오류: {name}")
    row_size = 1 + width * 3
    if any(raw[offset] > 4 for offset in range(0, len(raw), row_size)):
        raise ControlError(f"PNG scanline filter 오류: {name}")


def validate_text_fixture(payload: bytes, name: str) -> None:
    try:
        text = payload.decode("utf-8")
    except UnicodeDecodeError as error:
        raise ControlError(f"TXT UTF-8 오류: {name}") from error
    if "\0" in text or "데모 데이터" not in text or "실제 효력 없음" not in text or ".example" not in text:
        raise ControlError(f"TXT synthetic marker 오류: {name}")


def validate_xlsx_fixture(payload: bytes, name: str) -> None:
    allowed = (
        "[Content_Types].xml",
        "_rels/.rels",
        "xl/_rels/workbook.xml.rels",
        "xl/workbook.xml",
        "xl/worksheets/sheet1.xml",
    )
    xml_payloads: dict[str, bytes] = {}
    try:
        with zipfile.ZipFile(io.BytesIO(payload)) as workbook:
            infos = workbook.infolist()
            names = [info.filename for info in infos]
            if names != list(allowed) or len(names) != len(set(names)):
                raise ControlError(f"XLSX member allowlist/order 위반: {name}")
            if sum(info.file_size for info in infos) > MAX_XLSX_TOTAL_BYTES:
                raise ControlError(f"XLSX expanded size 상한 초과: {name}")
            for info in infos:
                member_path = PurePosixPath(info.filename)
                if (
                    member_path.is_absolute()
                    or ".." in member_path.parts
                    or info.file_size > MAX_XLSX_XML_BYTES
                    or info.compress_size != info.file_size
                    or info.date_time != (1980, 1, 1, 0, 0, 0)
                    or info.compress_type != zipfile.ZIP_STORED
                    or info.create_system != 3
                    or info.external_attr >> 16 != 0o100644
                    or info.flag_bits & 0x1
                ):
                    raise ControlError(f"XLSX member metadata 경계 위반: {name}/{info.filename}")
                xml_payloads[info.filename] = workbook.read(info)
    except (zipfile.BadZipFile, RuntimeError, OSError) as error:
        raise ControlError(f"XLSX ZIP 형식 오류: {name}") from error

    roots: dict[str, ET.Element] = {}
    for member_name, xml_payload in xml_payloads.items():
        upper = xml_payload.upper()
        if b"<!DOCTYPE" in upper or b"<!ENTITY" in upper:
            raise ControlError(f"XLSX DTD/entity 금지: {name}/{member_name}")
        try:
            roots[member_name] = ET.fromstring(xml_payload)
        except ET.ParseError as error:
            raise ControlError(f"XLSX XML 형식 오류: {name}/{member_name}") from error

    content_ns = "http://schemas.openxmlformats.org/package/2006/content-types"
    relation_ns = "http://schemas.openxmlformats.org/package/2006/relationships"
    sheet_ns = "http://schemas.openxmlformats.org/spreadsheetml/2006/main"
    document_relation_ns = "http://schemas.openxmlformats.org/officeDocument/2006/relationships"
    if roots["[Content_Types].xml"].tag != f"{{{content_ns}}}Types":
        raise ControlError(f"XLSX content types root 오류: {name}")
    if roots["_rels/.rels"].tag != f"{{{relation_ns}}}Relationships":
        raise ControlError(f"XLSX package relationships root 오류: {name}")
    if roots["xl/_rels/workbook.xml.rels"].tag != f"{{{relation_ns}}}Relationships":
        raise ControlError(f"XLSX workbook relationships root 오류: {name}")
    if roots["xl/workbook.xml"].tag != f"{{{sheet_ns}}}workbook":
        raise ControlError(f"XLSX workbook root 오류: {name}")
    worksheet = roots["xl/worksheets/sheet1.xml"]
    if worksheet.tag != f"{{{sheet_ns}}}worksheet":
        raise ControlError(f"XLSX worksheet root 오류: {name}")

    expected_content_types = [
        (
            f"{{{content_ns}}}Default",
            {
                "Extension": "rels",
                "ContentType": "application/vnd.openxmlformats-package.relationships+xml",
            },
        ),
        (
            f"{{{content_ns}}}Default",
            {"Extension": "xml", "ContentType": "application/xml"},
        ),
        (
            f"{{{content_ns}}}Override",
            {
                "PartName": "/xl/workbook.xml",
                "ContentType": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml",
            },
        ),
        (
            f"{{{content_ns}}}Override",
            {
                "PartName": "/xl/worksheets/sheet1.xml",
                "ContentType": "application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml",
            },
        ),
    ]
    content_types_root = roots["[Content_Types].xml"]
    actual_content_types = [
        (element.tag, element.attrib) for element in list(content_types_root)
    ]
    if content_types_root.attrib or actual_content_types != expected_content_types:
        raise ControlError(f"XLSX content types 계약 위반: {name}")

    package_relations = list(roots["_rels/.rels"])
    workbook_relations = list(roots["xl/_rels/workbook.xml.rels"])
    expected_package_relation = {
        "Id": "rId1",
        "Type": f"{document_relation_ns}/officeDocument",
        "Target": "xl/workbook.xml",
    }
    expected_workbook_relation = {
        "Id": "rId1",
        "Type": f"{document_relation_ns}/worksheet",
        "Target": "worksheets/sheet1.xml",
    }
    if len(package_relations) != 1 or package_relations[0].attrib != expected_package_relation:
        raise ControlError(f"XLSX package relationship 계약 위반: {name}")
    if len(workbook_relations) != 1 or workbook_relations[0].attrib != expected_workbook_relation:
        raise ControlError(f"XLSX workbook relationship 계약 위반: {name}")
    if (
        roots["_rels/.rels"].attrib
        or roots["xl/_rels/workbook.xml.rels"].attrib
        or package_relations[0].tag != f"{{{relation_ns}}}Relationship"
        or workbook_relations[0].tag != f"{{{relation_ns}}}Relationship"
    ):
        raise ControlError(f"XLSX relationship element 계약 위반: {name}")

    workbook_root = roots["xl/workbook.xml"]
    workbook_elements = list(workbook_root.iter())
    if (
        workbook_root.attrib
        or [element.tag.rsplit("}", 1)[-1] for element in workbook_elements]
        != ["workbook", "sheets", "sheet"]
    ):
        raise ControlError(f"XLSX workbook 구조 계약 위반: {name}")
    sheet = workbook_elements[-1]
    if sheet.attrib != {
        "name": "고객요구사항",
        "sheetId": "1",
        f"{{{document_relation_ns}}}id": "rId1",
    }:
        raise ControlError(f"XLSX sheet metadata 계약 위반: {name}")

    allowed_worksheet_elements = {"worksheet", "sheetData", "row", "c", "is", "t"}
    for element in worksheet.iter():
        local_name = element.tag.rsplit("}", 1)[-1]
        if local_name not in allowed_worksheet_elements:
            raise ControlError(f"XLSX worksheet element allowlist 위반: {name}/{local_name}")
        allowed_attributes = {
            "worksheet": set(),
            "sheetData": set(),
            "row": {"r"},
            "c": {"r", "t"},
            "is": set(),
            "t": set(),
        }[local_name]
        if set(element.attrib) != allowed_attributes:
            raise ControlError(f"XLSX worksheet attribute 계약 위반: {name}/{local_name}")
        if local_name == "c" and element.attrib.get("t") != "inlineStr":
            raise ControlError(f"XLSX cell type 계약 위반: {name}")

    forbidden_local_names = {
        "f",
        "hyperlinks",
        "hyperlink",
        "externalReferences",
        "externalReference",
        "definedNames",
        "definedName",
        "connections",
        "oleObjects",
    }
    for root in roots.values():
        for element in root.iter():
            local_name = element.tag.rsplit("}", 1)[-1]
            if local_name in forbidden_local_names or element.attrib.get("TargetMode") == "External":
                raise ControlError(f"XLSX active/external content 금지: {name}")
    worksheet_text = "\n".join(text for text in worksheet.itertext() if text)
    if "데모 데이터" not in worksheet_text or "실제 효력 없음" not in worksheet_text or ".example" not in worksheet_text:
        raise ControlError(f"XLSX synthetic marker 오류: {name}")


def iso_now() -> str:
    return dt.datetime.now(tz=KST).isoformat(timespec="seconds")


def parse_aware_datetime(value: str, field: str) -> str:
    try:
        parsed = dt.datetime.fromisoformat(value.replace("Z", "+00:00"))
    except ValueError as error:
        raise ControlError(f"{field} 형식 오류: {value}") from error
    if parsed.tzinfo is None:
        raise ControlError(f"{field}에는 timezone offset이 필요함")
    return parsed.isoformat(timespec="seconds")


def validate_generation(value: str) -> str:
    if not GENERATION_PATTERN.fullmatch(value):
        raise ControlError(f"generation 형식 오류: {value}")
    return value


def read_existing_state(path: Path) -> dict[str, object] | None:
    if not path.is_file():
        return None
    try:
        envelope = json.loads(path.read_text(encoding="utf-8"))
        data = envelope["data"]
    except (OSError, UnicodeError, json.JSONDecodeError, KeyError, TypeError):
        return None
    return data if isinstance(data, dict) else None


def validated_previous_provenance(
    previous: dict[str, object] | None, state_changed_at: str
) -> tuple[str | None, str | None]:
    """Keep the previous canonical generation only when its timestamp proves the same history."""
    if not previous:
        return None, None
    generation = previous.get("generation")
    last_reset_at = previous.get("lastResetAt")
    if not isinstance(generation, str) or not GENERATION_PATTERN.fullmatch(generation):
        return None, None
    if not isinstance(last_reset_at, str):
        return None, None
    try:
        normalized_reset_at = parse_aware_datetime(last_reset_at, "lastResetAt")
        normalized_transition_at = parse_aware_datetime(state_changed_at, "stateChangedAt")
    except ControlError:
        return None, None
    if dt.datetime.fromisoformat(normalized_reset_at) > dt.datetime.fromisoformat(
        normalized_transition_at
    ):
        return None, None
    return generation, normalized_reset_at


def public_accounts(manifest: dict[str, object]) -> list[dict[str, object]]:
    labels = {
        "demo.manager": ("관리자형 계정", "업무 CRUD와 결재자 흐름"),
        "demo.staff": ("직원형 계정", "기안·경비·근태 흐름"),
    }
    result = []
    for account in manifest["publicAccounts"]:
        login_id = account["loginId"]
        label, description = labels[login_id]
        result.append(
            {
                "label": label,
                "description": description,
                "loginId": login_id,
                "password": account["password"],
                "recommended": bool(account["recommended"]),
            }
        )
    return result


def preserved_public_accounts(
    previous: dict[str, object] | None,
) -> list[dict[str, object]]:
    if not previous:
        return []
    accounts = previous.get("publicAccounts")
    if not isinstance(accounts, list) or not all(
        isinstance(account, dict) for account in accounts
    ):
        return []
    account_ids = {
        account.get("loginId")
        for account in accounts
        if isinstance(account.get("loginId"), str)
    }
    return accounts if account_ids == {"demo.manager", "demo.staff"} else []


def validated_state_dir(path: Path) -> Path:
    resolved = path.resolve()
    if resolved != Path("/state"):
        raise ControlError(f"상태 디렉터리 mount allowlist 위반: {resolved}")
    resolved.mkdir(parents=True, exist_ok=True)
    return resolved


def write_state(
    args: argparse.Namespace,
    *,
    state: str | None = None,
    filename: str | None = None,
) -> None:
    state_dir = validated_state_dir(args.state_dir)
    lifecycle_state = state or args.state
    state_filename = filename or args.filename
    if state_filename not in ALLOWED_STATE_FILES:
        raise ControlError(f"상태 파일 allowlist 위반: {state_filename}")
    target = state_dir / state_filename
    candidate = validate_generation(args.candidate)
    next_reset_at = parse_aware_datetime(args.next_reset_at, "nextResetAt")
    previous = read_existing_state(target)
    if lifecycle_state in {"READY", "VERIFYING"}:
        manifest = validate_bundle(args.seed_dir, args.expected_app_version)
        accounts = public_accounts(manifest)
    elif lifecycle_state == "RESETTING":
        accounts = preserved_public_accounts(previous)
    else:
        raise ControlError(f"unsupported lifecycle state write: {lifecycle_state}")
    now = iso_now()

    if lifecycle_state == "READY":
        generation = candidate
        candidate_generation = None
        last_reset_at = now
        message = "데모 상태 조회에 성공했습니다."
    else:
        generation, last_reset_at = validated_previous_provenance(previous, now)
        candidate_generation = candidate
        message = {
            "RESETTING": "데모 기준 데이터를 복원하고 있습니다.",
            "VERIFYING": "복원한 데모 세대를 검증하고 있습니다.",
        }[lifecycle_state]

    data = {
        "enabled": True,
        "environmentName": "DEMO",
        "state": lifecycle_state,
        "generation": generation,
        "candidateGeneration": candidate_generation,
        "stateChangedAt": now,
        "lastResetAt": last_reset_at,
        "nextResetAt": next_reset_at,
        "warningBeforeSeconds": 300,
        "writeLockBeforeSeconds": 120,
        "writeLocked": lifecycle_state != "READY",
        "notice": "모든 데이터는 합성 데이터이며 주기적으로 초기화됩니다.",
        "uploadEnabled": True,
        "simulatedLocation": {"latitude": 37.5663, "longitude": 126.9779},
        "publicAccounts": accounts,
    }
    envelope = {"status": 200, "message": message, "data": data}
    atomic_json_write(target, envelope)
    print(f"state-ok: {target.name}={lifecycle_state} candidate={candidate}")


def write_resetting_state(args: argparse.Namespace) -> None:
    write_state(args, state="RESETTING", filename="status.json")


def write_failed_state(args: argparse.Namespace) -> None:
    """Publish a fail-closed state even when the seed bundle itself is unreadable."""
    state_dir = validated_state_dir(args.state_dir)
    target = state_dir / "status.json"
    candidate = validate_generation(args.candidate)
    next_reset_at = (
        parse_aware_datetime(args.next_reset_at, "nextResetAt")
        if args.next_reset_at
        else None
    )
    previous = read_existing_state(target) or {}
    now = iso_now()

    generation, last_reset_at = validated_previous_provenance(previous, now)
    accounts = preserved_public_accounts(previous)

    data = {
        "enabled": True,
        "environmentName": "DEMO",
        "state": "FAILED",
        "generation": generation,
        "candidateGeneration": candidate,
        "stateChangedAt": now,
        "lastResetAt": last_reset_at,
        "nextResetAt": next_reset_at,
        "warningBeforeSeconds": 300,
        "writeLockBeforeSeconds": 120,
        "writeLocked": True,
        "notice": "모든 데이터는 합성 데이터이며 주기적으로 초기화됩니다.",
        "uploadEnabled": True,
        "simulatedLocation": {"latitude": 37.5663, "longitude": 126.9779},
        "publicAccounts": accounts,
    }
    envelope = {
        "status": 200,
        "message": "데모 복원 또는 검증에 실패해 점검 중입니다.",
        "data": data,
    }
    atomic_json_write(target, envelope)
    print(f"state-ok: {target.name}=FAILED candidate={candidate}")


def atomic_json_write(target: Path, value: dict[str, object]) -> None:
    payload = (json.dumps(value, ensure_ascii=False, sort_keys=True, indent=2) + "\n").encode("utf-8")
    atomic_bytes_write(target, payload, mode=0o644, temporary_name=f".{target.name}.tmp")


def atomic_bytes_write(target: Path, payload: bytes, *, mode: int, temporary_name: str) -> None:
    if len(payload) > MAX_RETENTION_FAILURE_LOG_BYTES and target.name == RETENTION_FAILURE_LOG_NAME:
        raise ControlError("control-plane failure log 크기 상한 초과")
    try:
        target_metadata = target.lstat()
    except FileNotFoundError:
        target_metadata = None
    if target_metadata is not None and (
        stat.S_ISLNK(target_metadata.st_mode) or not stat.S_ISREG(target_metadata.st_mode)
    ):
        raise ControlError(f"atomic target 타입 계약 위반: {target}")

    temporary = target.with_name(temporary_name)
    try:
        stale_metadata = temporary.lstat()
    except FileNotFoundError:
        stale_metadata = None
    if stale_metadata is not None:
        if stat.S_ISLNK(stale_metadata.st_mode) or not stat.S_ISREG(stale_metadata.st_mode):
            raise ControlError(f"atomic temp 타입 계약 위반: {temporary}")
        temporary.unlink()

    created_identity: tuple[int, int] | None = None
    try:
        with temporary.open("xb") as stream:
            metadata = os.fstat(stream.fileno())
            created_identity = (metadata.st_dev, metadata.st_ino)
            stream.write(payload)
            stream.flush()
            os.fchmod(stream.fileno(), mode)
            os.fsync(stream.fileno())
        ready = temporary.lstat()
        if (
            stat.S_ISLNK(ready.st_mode)
            or not stat.S_ISREG(ready.st_mode)
            or (ready.st_dev, ready.st_ino) != created_identity
        ):
            raise ControlError(f"atomic temp inode 변경 감지: {temporary}")
        os.replace(temporary, target)
        try:
            directory_fd = os.open(target.parent, os.O_RDONLY)
        except PermissionError:
            if os.name != "nt":
                raise
        else:
            try:
                os.fsync(directory_fd)
            finally:
                os.close(directory_fd)
    finally:
        if created_identity is not None:
            try:
                remaining = temporary.lstat()
            except FileNotFoundError:
                remaining = None
            if remaining is not None and (
                remaining.st_dev,
                remaining.st_ino,
            ) == created_identity and stat.S_ISREG(remaining.st_mode):
                temporary.unlink()


def write_control_plane_failure_log(args: argparse.Namespace) -> None:
    logs_root = validated_cleanup_mount(args.logs_root, "/logs", "logs root")
    write_control_plane_failure_log_file(
        logs_root,
        args.candidate,
        args.stage,
        args.line,
        args.exit_code,
        args.failed_state_published,
    )
    print(f"control-plane-failure-log-ok: candidate={args.candidate}")


def write_control_plane_failure_log_file(
    logs_root: Path,
    candidate: str,
    stage: str,
    line: int,
    exit_code: int,
    failed_state_published: str,
) -> None:
    logs_root = require_real_directory(logs_root, "logs root")
    candidate = validate_generation(candidate)
    if not CONTROL_PLANE_FAILURE_STAGE_PATTERN.fullmatch(stage):
        raise ControlError(f"control-plane failure stage 형식 오류: {stage}")
    if failed_state_published not in {"true", "false"}:
        raise ControlError("failedStatePublished 형식 오류")
    if line <= 0 or not 1 <= exit_code <= 255:
        raise ControlError("control-plane failure line/exit 형식 오류")
    recorded_at = iso_now()
    lines = (
        f"recordedAt={recorded_at}",
        f"stage={stage}",
        f"line={line}",
        f"exit={exit_code}",
        f"candidate={candidate}",
        f"failedStatePublished={failed_state_published}",
        "generationLogsSuppressed=true",
    )
    payload = ("\n".join(lines) + "\n").encode("utf-8")
    atomic_bytes_write(
        logs_root / RETENTION_FAILURE_LOG_NAME,
        payload,
        mode=0o600,
        temporary_name=RETENTION_FAILURE_TEMP_NAME,
    )


def load_mapping(path: Path) -> dict[int, StoredFileMapping]:
    rows: dict[int, StoredFileMapping] = {}
    try:
        lines = path.read_text(encoding="utf-8").splitlines()
    except (OSError, UnicodeError) as error:
        raise ControlError(f"stored_files mapping을 읽을 수 없음: {error}") from error
    for line_number, line in enumerate(lines, 1):
        fields = line.split("\t")
        if len(fields) != 11:
            raise ControlError(f"mapping {line_number}행 필드 수 오류")
        (raw_id, stored_name, original_name, content_type, raw_size, raw_created_date,
         raw_reset_date, status, owner_type, raw_owner_id, raw_uploader_id) = fields
        try:
            file_id = int(raw_id)
            size = int(raw_size)
            created_date = dt.date.fromisoformat(raw_created_date)
            reset_date = dt.date.fromisoformat(raw_reset_date)
            owner_id = int(raw_owner_id)
            uploader_id = int(raw_uploader_id)
        except ValueError as error:
            raise ControlError(f"mapping {line_number}행 숫자·날짜 필드 오류") from error
        if (
            not 1 <= file_id <= len(EXPECTED_FIXTURE_NAMES)
            or file_id in rows
            or not STORED_NAME_PATTERN.fullmatch(stored_name)
            or not valid_fixture_name(original_name, content_type)
            or original_name != EXPECTED_FIXTURE_NAMES[file_id - 1]
            or not 1 <= size <= MAX_FIXTURE_BYTES
            or not 2000 <= created_date.year <= 2099
            or not 2000 <= reset_date.year <= 2099
            or status != "CLAIMED"
            or owner_type not in {"DRIVE_FILE", "BOARD_POST", "APPROVAL_DOCUMENT", "EXPENSE_CLAIM"}
            or owner_id < 1 or uploader_id < 1
        ):
            raise ControlError(f"mapping {line_number}행 값 오류")
        rows[file_id] = StoredFileMapping(
            stored_name=stored_name,
            original_name=original_name,
            content_type=content_type,
            size=size,
            created_date=created_date,
            reset_date=reset_date,
            status=status,
            owner_type=owner_type,
            owner_id=owner_id,
            uploader_id=uploader_id,
        )
    return rows


def validate_mapping_contract(
    mapping: dict[int, StoredFileMapping], manifest: dict[str, object]
) -> dict[int, dict[str, object]]:
    files = {int(item["id"]): item for item in manifest["files"]}
    if set(mapping) != set(files):
        raise ControlError("DB stored_files와 manifest file id 집합 불일치")
    reset_dates = {row.reset_date for row in mapping.values()}
    if len(reset_dates) != 1:
        raise ControlError("DB stored_files reset 기준일 불일치")
    for file_id, item in files.items():
        row = mapping[file_id]
        expected_date = row.reset_date - dt.timedelta(days=int(item["createdAtDaysAgo"]))
        if (
            row.stored_name != item["storedName"]
            or row.original_name != item["originalName"]
            or row.content_type != item["contentType"]
            or row.size != item["size"]
            or row.created_date != expected_date
            or row.status != item["status"]
            or row.owner_type != item["ownerType"]
            or row.owner_id != item["ownerId"]
            or row.uploader_id != item["uploaderId"]
        ):
            raise ControlError(f"DB/manifest stored_files metadata 불일치: fileId={file_id}")
    return files


def generation_metadata_bytes(
    generation: str,
    manifest: dict[str, object],
    mapping: dict[int, StoredFileMapping],
) -> bytes:
    files_by_id = {int(item["id"]): item for item in manifest["files"]}
    files = []
    for file_id in sorted(mapping):
        row = mapping[file_id]
        item = files_by_id[file_id]
        files.append(
            {
                "contentType": row.content_type,
                "createdMonth": row.month,
                "createdYear": row.year,
                "id": file_id,
                "originalName": row.original_name,
                "ownerId": row.owner_id,
                "ownerType": row.owner_type,
                "sha256": item["sha256"],
                "size": row.size,
                "status": row.status,
                "storedName": row.stored_name,
                "uploaderId": row.uploader_id,
            }
        )
    value = {
        "files": files,
        "formatVersion": 1,
        "generation": generation,
        "seedVersion": manifest["seedVersion"],
    }
    return (json.dumps(value, ensure_ascii=False, sort_keys=True, separators=(",", ":")) + "\n").encode(
        "utf-8"
    )


def validated_files_root(path: Path) -> Path:
    resolved = path.resolve()
    if resolved != Path("/files"):
        raise ControlError(f"파일 볼륨 mount allowlist 위반: {resolved}")
    resolved.mkdir(parents=True, exist_ok=True)
    return resolved


def require_real_directory(path: Path, label: str) -> Path:
    try:
        metadata = path.lstat()
    except FileNotFoundError as error:
        raise ControlError(f"{label} 디렉터리 누락: {path}") from error
    if stat.S_ISLNK(metadata.st_mode) or not stat.S_ISDIR(metadata.st_mode):
        raise ControlError(f"{label}는 실제 디렉터리여야 함: {path}")
    return path.resolve(strict=True)


def validated_cleanup_mount(path: Path, expected: str, label: str) -> Path:
    resolved = require_real_directory(path, label)
    if resolved != Path(expected):
        raise ControlError(f"{label} mount allowlist 위반: {resolved}")
    return resolved


def read_cleanup_state(state_dir: Path, required: bool) -> dict[str, object] | None:
    target = state_dir / "status.json"
    try:
        metadata = target.lstat()
    except FileNotFoundError:
        if required:
            raise ControlError("cleanup에 필요한 status.json 누락")
        return None
    if stat.S_ISLNK(metadata.st_mode) or not stat.S_ISREG(metadata.st_mode):
        raise ControlError("status.json은 실제 일반 파일이어야 함")
    try:
        envelope = json.loads(
            target.read_text(encoding="utf-8"), object_pairs_hook=reject_duplicate_keys
        )
    except (OSError, UnicodeError, json.JSONDecodeError, ValueError) as error:
        raise ControlError("cleanup status.json 파싱 실패") from error
    if not isinstance(envelope, dict) or not isinstance(envelope.get("data"), dict):
        raise ControlError("cleanup status.json envelope 계약 위반")
    data = envelope["data"]
    if data.get("state") not in {"READY", "RESETTING", "VERIFYING", "FAILED"}:
        raise ControlError("cleanup status state 계약 위반")
    for field in ("generation", "candidateGeneration"):
        value = data.get(field)
        if value is not None and (
            not isinstance(value, str) or not GENERATION_PATTERN.fullmatch(value)
        ):
            raise ControlError(f"cleanup status {field} 형식 오류")
    return data


def current_file_generation(files_root: Path, generations: Path) -> str | None:
    current = files_root / "current"
    try:
        metadata = current.lstat()
    except FileNotFoundError:
        return None
    if not stat.S_ISLNK(metadata.st_mode):
        raise ControlError("current는 관리 대상 symlink여야 함")
    target = os.readlink(current)
    prefix = "generations/"
    if not target.startswith(prefix):
        raise ControlError(f"current symlink 상대 경로 계약 위반: {target}")
    generation = target.removeprefix(prefix)
    if not GENERATION_PATTERN.fullmatch(generation) or target != f"generations/{generation}":
        raise ControlError(f"current symlink generation 형식 오류: {target}")
    destination = generations / generation
    require_real_directory(destination, "current generation")
    try:
        resolved_current = current.resolve(strict=True)
        resolved_destination = destination.resolve(strict=True)
    except (OSError, RuntimeError) as error:
        raise ControlError("current symlink 해석 실패") from error
    if resolved_current != resolved_destination:
        raise ControlError("current symlink가 generation 경계를 벗어남")
    return generation


def inventory_generation_directories(
    files_root: Path, *, allow_missing: bool
) -> tuple[Path | None, str | None, dict[str, tuple[Path, os.stat_result]]]:
    generations = files_root / "generations"
    try:
        generations_root = require_real_directory(generations, "generations")
    except ControlError:
        if allow_missing and not generations.exists() and not generations.is_symlink():
            if (files_root / "current").exists() or (files_root / "current").is_symlink():
                raise ControlError("generations 없이 current symlink가 존재함")
            return None, None, {}
        raise
    current_generation = current_file_generation(files_root, generations_root)
    entries: dict[str, tuple[Path, os.stat_result]] = {}
    for entry in sorted(generations_root.iterdir(), key=lambda item: item.name):
        if not GENERATION_PATTERN.fullmatch(entry.name):
            raise ControlError(f"알 수 없는 generation artifact: {entry.name}")
        metadata = entry.lstat()
        if stat.S_ISLNK(metadata.st_mode) or not stat.S_ISDIR(metadata.st_mode):
            raise ControlError(f"generation artifact 타입 계약 위반: {entry.name}")
        entries[entry.name] = (entry, metadata)
    return generations_root, current_generation, entries


def inventory_file_root_transients(files_root: Path) -> list[CleanupTarget]:
    targets: list[CleanupTarget] = []
    for entry in sorted(files_root.iterdir(), key=lambda item: item.name):
        if entry.name in {"generations", "current"}:
            continue
        metadata = entry.lstat()
        if STAGING_ARTIFACT_PATTERN.fullmatch(entry.name):
            if stat.S_ISLNK(metadata.st_mode) or not stat.S_ISDIR(metadata.st_mode):
                raise ControlError(f"staging artifact 타입 계약 위반: {entry.name}")
            targets.append(cleanup_target(entry, "directory"))
        elif CURRENT_TEMP_PATTERN.fullmatch(entry.name):
            if not stat.S_ISLNK(metadata.st_mode):
                raise ControlError(f"current temp artifact 타입 계약 위반: {entry.name}")
            targets.append(cleanup_target(entry, "symlink"))
        else:
            raise ControlError(f"알 수 없는 files root artifact: {entry.name}")
    return targets


def inventory_state_temp_files(state_dir: Path) -> list[CleanupTarget]:
    targets: list[CleanupTarget] = []
    for entry in sorted(state_dir.iterdir(), key=lambda item: item.name):
        metadata = entry.lstat()
        if entry.name in ALLOWED_STATE_FILES:
            if stat.S_ISLNK(metadata.st_mode) or not stat.S_ISREG(metadata.st_mode):
                raise ControlError(f"active state 파일 타입 계약 위반: {entry.name}")
            continue
        if STATE_TEMP_PATTERN.fullmatch(entry.name):
            if stat.S_ISLNK(metadata.st_mode) or not stat.S_ISREG(metadata.st_mode):
                raise ControlError(f"state temp 타입 계약 위반: {entry.name}")
            targets.append(cleanup_target(entry, "file"))
            continue
        raise ControlError(f"알 수 없는 state artifact: {entry.name}")
    return targets


def newest_names(
    entries: dict[str, tuple[Path, os.stat_result]],
    limit: int,
    forced: set[str],
) -> set[str]:
    present_forced = {name for name in forced if name in entries}
    if len(present_forced) > limit:
        raise ControlError("retention 강제 보존 대상이 상한을 초과함")
    ranked = sorted(
        entries,
        key=lambda name: (entries[name][1].st_mtime_ns, name),
        reverse=True,
    )
    kept = set(present_forced)
    for name in ranked:
        if len(kept) >= limit:
            break
        kept.add(name)
    return kept


def validate_removal_tree(path: Path) -> None:
    pending = [path]
    visited = 0
    root_device = path.lstat().st_dev
    while pending:
        directory = pending.pop()
        with os.scandir(directory) as entries:
            for entry in entries:
                visited += 1
                if visited > 100_000:
                    raise ControlError(f"cleanup artifact 항목 상한 초과: {path}")
                metadata = entry.stat(follow_symlinks=False)
                if metadata.st_dev != root_device:
                    raise ControlError(f"cleanup artifact mount 경계 위반: {entry.path}")
                if entry.is_symlink():
                    raise ControlError(f"cleanup artifact 내부 symlink 금지: {entry.path}")
                if entry.is_dir(follow_symlinks=False):
                    pending.append(Path(entry.path))
                elif not entry.is_file(follow_symlinks=False):
                    raise ControlError(f"cleanup artifact 내부 특수 타입 금지: {entry.path}")


def cleanup_target(path: Path, kind: str) -> CleanupTarget:
    metadata = path.lstat()
    parent_metadata = path.parent.lstat()
    if stat.S_ISLNK(parent_metadata.st_mode) or not stat.S_ISDIR(parent_metadata.st_mode):
        raise ControlError(f"cleanup 대상 부모 경계 위반: {path.parent}")
    if kind == "directory":
        if stat.S_ISLNK(metadata.st_mode) or not stat.S_ISDIR(metadata.st_mode):
            raise ControlError(f"cleanup 디렉터리 타입 변경 감지: {path}")
        validate_removal_tree(path)
    elif kind == "file":
        if stat.S_ISLNK(metadata.st_mode) or not stat.S_ISREG(metadata.st_mode):
            raise ControlError(f"cleanup 파일 타입 변경 감지: {path}")
    elif kind == "symlink":
        if not stat.S_ISLNK(metadata.st_mode):
            raise ControlError(f"cleanup symlink 타입 변경 감지: {path}")
    else:
        raise ControlError(f"지원하지 않는 cleanup 대상 타입: {kind}")
    return CleanupTarget(
        path=path,
        kind=kind,
        device=metadata.st_dev,
        inode=metadata.st_ino,
        parent_device=parent_metadata.st_dev,
        parent_inode=parent_metadata.st_ino,
    )


def validate_cleanup_target(target: CleanupTarget) -> None:
    try:
        metadata = target.path.lstat()
        parent_metadata = target.path.parent.lstat()
    except FileNotFoundError as error:
        raise ControlError(f"cleanup 대상이 계획 이후 사라짐: {target.path}") from error
    if (metadata.st_dev, metadata.st_ino) != (target.device, target.inode):
        raise ControlError(f"cleanup 대상 inode 변경 감지: {target.path}")
    if (parent_metadata.st_dev, parent_metadata.st_ino) != (
        target.parent_device,
        target.parent_inode,
    ):
        raise ControlError(f"cleanup 대상 부모 inode 변경 감지: {target.path.parent}")
    if target.kind == "directory":
        if stat.S_ISLNK(metadata.st_mode) or not stat.S_ISDIR(metadata.st_mode):
            raise ControlError(f"cleanup 디렉터리 타입 변경 감지: {target.path}")
        validate_removal_tree(target.path)
    elif target.kind == "file":
        if stat.S_ISLNK(metadata.st_mode) or not stat.S_ISREG(metadata.st_mode):
            raise ControlError(f"cleanup 파일 타입 변경 감지: {target.path}")
    elif target.kind == "symlink":
        if not stat.S_ISLNK(metadata.st_mode):
            raise ControlError(f"cleanup symlink 타입 변경 감지: {target.path}")
    else:
        raise ControlError(f"지원하지 않는 cleanup 대상 타입: {target.kind}")


def inventory_work_directories(
    work_root: Path,
) -> dict[str, tuple[Path, os.stat_result]]:
    entries: dict[str, tuple[Path, os.stat_result]] = {}
    for entry in sorted(work_root.iterdir(), key=lambda item: item.name):
        if not GENERATION_PATTERN.fullmatch(entry.name):
            continue
        metadata = entry.lstat()
        if stat.S_ISLNK(metadata.st_mode) or not stat.S_ISDIR(metadata.st_mode):
            raise ControlError(f"recognized work artifact 타입 계약 위반: {entry.name}")
        entries[entry.name] = (entry, metadata)
    return entries


def inventory_log_files(logs_root: Path) -> dict[str, tuple[Path, os.stat_result]]:
    entries: dict[str, tuple[Path, os.stat_result]] = {}
    for entry in sorted(logs_root.iterdir(), key=lambda item: item.name):
        if not RECOGNIZED_LOG_PATTERN.fullmatch(entry.name):
            continue
        metadata = entry.lstat()
        if stat.S_ISLNK(metadata.st_mode) or not stat.S_ISREG(metadata.st_mode):
            raise ControlError(f"recognized log artifact 타입 계약 위반: {entry.name}")
        entries[entry.name] = (entry, metadata)
    return entries


def inventory_control_log_artifacts(logs_root: Path) -> list[CleanupTarget]:
    targets: list[CleanupTarget] = []
    failure_log = logs_root / RETENTION_FAILURE_LOG_NAME
    failure_temp = logs_root / RETENTION_FAILURE_TEMP_NAME
    for path, removable in ((failure_log, False), (failure_temp, True)):
        try:
            metadata = path.lstat()
        except FileNotFoundError:
            continue
        if stat.S_ISLNK(metadata.st_mode) or not stat.S_ISREG(metadata.st_mode):
            raise ControlError(f"control-plane log 타입 계약 위반: {path.name}")
        if removable:
            targets.append(cleanup_target(path, "file"))
    return targets


def build_cleanup_plan(
    phase: str,
    files_root: Path,
    state_dir: Path,
    work_root: Path,
    logs_root: Path,
    candidate: str | None = None,
) -> CleanupPlan:
    if phase not in {"pre-reset", "post-success"}:
        raise ControlError(f"지원하지 않는 cleanup phase: {phase}")
    files_root = require_real_directory(files_root, "files root")
    state_dir = require_real_directory(state_dir, "state root")
    work_root = require_real_directory(work_root, "work root")
    logs_root = require_real_directory(logs_root, "logs root")
    file_root_targets = inventory_file_root_transients(files_root) if phase == "pre-reset" else []
    state_temp_targets = inventory_state_temp_files(state_dir) if phase == "pre-reset" else []
    control_log_targets = inventory_control_log_artifacts(logs_root) if phase == "pre-reset" else []
    state = read_cleanup_state(state_dir, required=phase == "post-success")
    generations_root, current, generation_entries = inventory_generation_directories(
        files_root, allow_missing=phase == "pre-reset"
    )

    forced_generation: set[str] = set()
    forced_runtime: set[str] = set()
    if state is not None:
        state_generation = state.get("generation")
        if isinstance(state_generation, str):
            forced_generation.add(state_generation)
        state_candidate = state.get("candidateGeneration")
        if isinstance(state_candidate, str):
            forced_generation.add(state_candidate)
            forced_runtime.add(state_candidate)

    if phase == "pre-reset":
        noncurrent_entries = {
            name: value for name, value in generation_entries.items() if name != current
        }
        kept_noncurrent = newest_names(
            noncurrent_entries,
            PRE_RESET_GENERATION_RETENTION,
            forced_generation.difference({current}),
        )
        preserved_generations = kept_noncurrent.union({current} if current else set())
    else:
        if candidate is None:
            raise ControlError("post-success cleanup candidate 누락")
        candidate = validate_generation(candidate)
        if state is None or state.get("state") != "VERIFYING":
            raise ControlError("post-success cleanup은 VERIFYING 상태에서만 허용됨")
        if state.get("candidateGeneration") != candidate or state.get("writeLocked") is not True:
            raise ControlError("post-success candidate/writeLocked 계약 불일치")
        if current != candidate:
            raise ControlError("post-success current generation이 candidate와 다름")
        if state.get("generation") == candidate:
            raise ControlError("post-success previous generation과 candidate가 같음")
        preserved_generations = {candidate}
        missing = preserved_generations.difference(generation_entries)
        if missing:
            raise ControlError(f"보존할 generation 디렉터리 누락: {sorted(missing)}")

    generation_targets = [
        cleanup_target(path, "directory")
        for name, (path, _) in generation_entries.items()
        if name not in preserved_generations
    ]

    work_entries = inventory_work_directories(work_root)
    if phase == "post-success" and candidate not in work_entries:
        raise ControlError("post-success candidate work 디렉터리 누락")
    retained_work = newest_names(
        {
            name: value
            for name, value in work_entries.items()
            if not (phase == "post-success" and name == candidate)
        },
        FAILED_WORK_RETENTION,
        forced_runtime.difference({candidate} if candidate else set()),
    )
    old_work_targets = [
        cleanup_target(path, "directory")
        for name, (path, _) in work_entries.items()
        if name not in retained_work and not (phase == "post-success" and name == candidate)
    ]

    log_entries = inventory_log_files(logs_root)
    forced_logs = {
        name
        for name in log_entries
        if RECOGNIZED_LOG_PATTERN.fullmatch(name).group(1) in forced_runtime
    }
    retained_logs = newest_names(log_entries, RECOGNIZED_LOG_RETENTION, forced_logs)
    log_targets = [
        cleanup_target(path, "file")
        for name, (path, _) in log_entries.items()
        if name not in retained_logs
    ]

    targets = (
        file_root_targets
        + state_temp_targets
        + control_log_targets
        + generation_targets
        + old_work_targets
        + log_targets
    )
    if phase == "post-success" and candidate is not None:
        targets.append(cleanup_target(work_entries[candidate][0], "directory"))
    return CleanupPlan(
        phase=phase,
        targets=tuple(targets),
        preserved_generations=tuple(sorted(preserved_generations)),
    )


def execute_cleanup_plan(plan: CleanupPlan) -> None:
    for target in plan.targets:
        validate_cleanup_target(target)
    for target in plan.targets:
        validate_cleanup_target(target)
        if target.kind == "directory":
            shutil.rmtree(target.path)
        else:
            target.path.unlink()


def cleanup_artifacts(args: argparse.Namespace) -> None:
    files_root = validated_cleanup_mount(args.files_root, "/files", "files root")
    state_dir = validated_cleanup_mount(args.state_dir, "/state", "state root")
    work_root = validated_cleanup_mount(args.work_root, "/work", "work root")
    logs_root = validated_cleanup_mount(args.logs_root, "/logs", "logs root")
    plan = build_cleanup_plan(
        args.phase,
        files_root,
        state_dir,
        work_root,
        logs_root,
        args.candidate,
    )
    execute_cleanup_plan(plan)
    print(
        f"cleanup-ok: phase={plan.phase} removed={len(plan.targets)} "
        f"preservedGenerations={','.join(plan.preserved_generations) or '-'}"
    )


def safe_remove_staging(path: Path, files_root: Path, generation: str) -> None:
    expected = files_root / f".staging-{generation}"
    if path != expected:
        raise ControlError(f"staging 삭제 경계 위반: {path}")
    if path.exists():
        shutil.rmtree(path)


def make_generation_directory(path: Path, *, parents: bool = False) -> None:
    """Create a backend-writable directory whose group is inherited by descendants."""
    path.mkdir(mode=GENERATION_DIRECTORY_MODE, parents=parents, exist_ok=True)
    path.chmod(GENERATION_DIRECTORY_MODE)


def stage_files(args: argparse.Namespace) -> None:
    generation = validate_generation(args.generation)
    files_root = validated_files_root(args.files_root)
    manifest = validate_bundle(args.seed_dir, args.expected_app_version)
    mapping = load_mapping(args.mapping)
    files = validate_mapping_contract(mapping, manifest)

    generations = files_root / "generations"
    make_generation_directory(generations)
    destination = generations / generation
    if destination.exists() or destination.is_symlink():
        raise ControlError(f"generation 파일 경로가 이미 존재함: {generation}")
    staging = files_root / f".staging-{generation}"
    safe_remove_staging(staging, files_root, generation)
    make_generation_directory(staging)

    archive_path = args.seed_dir / "seed-files.tar.gz"
    try:
        with tarfile.open(archive_path, mode="r:gz") as archive:
            members = {member.name: member for member in archive.getmembers()}
            for file_id in sorted(files):
                item = files[file_id]
                row = mapping[file_id]
                stored_name, year, month = row.stored_name, row.year, row.month
                member_name = f"objects/{stored_name}"
                member = members.get(member_name)
                if member is None or not member.isfile():
                    raise ControlError(f"archive 객체 누락: {member_name}")
                source = archive.extractfile(member)
                if source is None:
                    raise ControlError(f"archive 객체를 읽을 수 없음: {member_name}")
                payload = source.read()
                if len(payload) != item["size"] or sha256_bytes(payload) != item["sha256"]:
                    raise ControlError(f"archive 객체 checksum 불일치: {member_name}")
                target_dir = staging / year / month
                make_generation_directory(target_dir, parents=True)
                make_generation_directory(target_dir.parent)
                target = target_dir / stored_name
                target.write_bytes(payload)
                target.chmod(GENERATION_SEED_FILE_MODE)
        (staging / ".seed-version").write_text(str(manifest["seedVersion"]) + "\n", encoding="utf-8")
        (staging / ".seed-version").chmod(0o644)
        (staging / ".generation.json").write_bytes(
            generation_metadata_bytes(generation, manifest, mapping)
        )
        (staging / ".generation.json").chmod(0o644)
        verify_generation(staging, manifest, mapping, generation)
        os.replace(staging, destination)
    except Exception:
        safe_remove_staging(staging, files_root, generation)
        raise
    print(f"files-staged: generation={generation} objects={len(files)}")


def verify_generation(
    generation_dir: Path,
    manifest: dict[str, object],
    mapping: dict[int, StoredFileMapping],
    generation: str,
) -> None:
    validate_mapping_contract(mapping, manifest)
    if generation_dir.stat().st_mode & 0o7777 != GENERATION_DIRECTORY_MODE:
        raise ControlError("materialized generation directory permission 불일치")
    expected_paths = {Path(".seed-version"), Path(".generation.json")}
    for item in manifest["files"]:
        file_id = int(item["id"])
        row = mapping[file_id]
        relative = Path(row.year) / row.month / row.stored_name
        expected_paths.add(relative)
        target = generation_dir / relative
        if not target.is_file() or target.is_symlink():
            raise ControlError(f"materialized file 누락: {relative.as_posix()}")
        if target.stat().st_mode & 0o777 != GENERATION_SEED_FILE_MODE:
            raise ControlError(f"materialized file permission 불일치: {relative.as_posix()}")
        for directory in (target.parent, target.parent.parent):
            if directory.stat().st_mode & 0o7777 != GENERATION_DIRECTORY_MODE:
                raise ControlError(
                    f"materialized directory permission 불일치: "
                    f"{directory.relative_to(generation_dir).as_posix()}"
                )
        if target.stat().st_size != item["size"] or sha256_file(target) != item["sha256"]:
            raise ControlError(f"materialized file checksum 불일치: {relative.as_posix()}")
    actual_paths = {
        path.relative_to(generation_dir)
        for path in generation_dir.rglob("*")
        if path.is_file() or path.is_symlink()
    }
    if actual_paths != expected_paths:
        extras = sorted(path.as_posix() for path in actual_paths.difference(expected_paths))
        missing = sorted(path.as_posix() for path in expected_paths.difference(actual_paths))
        raise ControlError(f"materialized file 집합 불일치: extra={extras}, missing={missing}")
    seed_version_marker = generation_dir / ".seed-version"
    if seed_version_marker.is_symlink() or not seed_version_marker.is_file():
        raise ControlError("materialized seedVersion marker 누락")
    version = seed_version_marker.read_text(encoding="utf-8").strip()
    if version != manifest["seedVersion"]:
        raise ControlError("materialized seedVersion 불일치")
    generation_marker = generation_dir / ".generation.json"
    if generation_marker.is_symlink() or not generation_marker.is_file():
        raise ControlError("materialized generation marker 누락")
    if generation_marker.read_bytes() != generation_metadata_bytes(generation, manifest, mapping):
        raise ControlError("materialized generation metadata 불일치")


def promote_files(args: argparse.Namespace) -> None:
    generation = validate_generation(args.generation)
    files_root = validated_files_root(args.files_root)
    manifest = validate_bundle(args.seed_dir, args.expected_app_version)
    mapping = load_mapping(args.mapping)
    destination = files_root / "generations" / generation
    if not destination.is_dir() or destination.is_symlink():
        raise ControlError(f"승격할 generation 디렉터리 누락: {generation}")
    verify_generation(destination, manifest, mapping, generation)

    current = files_root / "current"
    if current.exists() and not current.is_symlink():
        raise ControlError("current는 관리 대상 symlink여야 함")
    temporary = files_root / f".current-{generation}"
    if temporary.exists() or temporary.is_symlink():
        temporary.unlink()
    os.symlink(f"generations/{generation}", temporary)
    os.replace(temporary, current)
    if current.resolve() != destination.resolve():
        raise ControlError("current symlink 승격 검증 실패")
    print(f"files-promoted: generation={generation}")


def verify_current_files(args: argparse.Namespace) -> None:
    generation = validate_generation(args.generation)
    files_root = validated_files_root(args.files_root)
    current = files_root / "current"
    expected = files_root / "generations" / generation
    if not current.is_symlink() or current.resolve() != expected.resolve():
        raise ControlError("current symlink가 기대 generation을 가리키지 않음")
    manifest = validate_bundle(args.seed_dir, args.expected_app_version)
    mapping = load_mapping(args.mapping)
    verify_generation(expected, manifest, mapping, generation)
    print(f"files-current-ok: generation={generation}")


def assert_generation_absent(args: argparse.Namespace) -> None:
    generation = validate_generation(args.generation)
    files_root = validated_files_root(args.files_root)
    generation_path = files_root / "generations" / generation
    if generation_path.exists() or generation_path.is_symlink():
        raise ControlError(f"제거되어야 할 file generation이 남음: {generation}")
    print(f"files-generation-absent: generation={generation}")


def verify_acceptance_file(args: argparse.Namespace) -> None:
    files_root = validated_files_root(args.files_root)
    generation = validate_generation(args.generation)
    marker = validate_acceptance_marker(args.marker)
    relative_path = PurePosixPath(args.relative_path)
    if (
        relative_path.is_absolute()
        or len(relative_path.parts) != 3
        or not re.fullmatch(r"20[0-9]{2}", relative_path.parts[0])
        or not re.fullmatch(r"0[1-9]|1[0-2]", relative_path.parts[1])
        or not STORED_NAME_PATTERN.fullmatch(relative_path.parts[2])
    ):
        raise ControlError("acceptance stored object relative path contract violation")

    generation_root = require_real_directory(
        files_root / "generations" / generation,
        "acceptance file generation",
    )
    target = generation_root.joinpath(*relative_path.parts)
    if target.is_symlink() or not target.is_file():
        raise ControlError("acceptance stored object is missing or not a regular file")
    if target.resolve(strict=True).parent.parent.parent != generation_root:
        raise ControlError("acceptance stored object escaped its generation")

    expected = acceptance_file_payload(marker, args.kind)
    actual = target.read_bytes()
    if actual != expected:
        raise ControlError(
            f"acceptance stored object bytes mismatch: kind={args.kind} "
            f"expected={len(expected)} actual={len(actual)}"
        )
    print(
        f"acceptance-file-ok: kind={args.kind} generation={generation} "
        f"stored={relative_path.parts[2]} size={len(actual)} sha256={sha256_bytes(actual)}"
    )


def request(
    base_url: str,
    path: str,
    *,
    token: str | None = None,
    method: str = "GET",
    body: bytes | None = None,
    content_type: str | None = None,
    expected_status: int = 200,
    timeout: float = 8.0,
    response_headers: dict[str, str] | None = None,
) -> tuple[bytes, dict[str, object] | None]:
    request_headers = {"Accept": "application/json"}
    if token:
        request_headers["Authorization"] = f"Bearer {token}"
    if content_type:
        request_headers["Content-Type"] = content_type
    req = urllib.request.Request(
        base_url.rstrip("/") + path,
        data=body,
        headers=request_headers,
        method=method,
    )
    try:
        with urllib.request.urlopen(req, timeout=timeout) as response:
            status = response.status
            payload = response.read()
            received_headers = dict(response.headers.items())
    except urllib.error.HTTPError as error:
        status = error.code
        payload = error.read()
        received_headers = dict(error.headers.items())
    except (urllib.error.URLError, TimeoutError, OSError) as error:
        reason = error.reason if isinstance(error, urllib.error.URLError) else error
        raise ControlError(
            f"HTTP {method} {path} transport failure after {timeout:g}s: {reason}"
        ) from error
    if response_headers is not None:
        response_headers.clear()
        response_headers.update(
            {key.lower(): value.strip() for key, value in received_headers.items()}
        )
    if status != expected_status:
        excerpt = payload[:500].decode("utf-8", errors="replace")
        raise ControlError(f"HTTP {path}: expected={expected_status}, actual={status}, body={excerpt}")
    parsed = None
    if payload and payload.lstrip().startswith((b"{", b"[")):
        try:
            value = json.loads(payload)
        except json.JSONDecodeError as error:
            raise ControlError(f"HTTP {path} JSON 파싱 실패") from error
        if isinstance(value, dict):
            parsed = value
    return payload, parsed


def envelope_data(value: dict[str, object] | None, path: str) -> object:
    if not value or value.get("status") != 200 or "data" not in value:
        raise ControlError(f"HTTP {path} 성공 envelope 계약 위반")
    return value["data"]


def wait_for_readiness(base_url: str, timeout_seconds: int) -> None:
    deadline = time.monotonic() + timeout_seconds
    last_error = "응답 없음"
    while time.monotonic() < deadline:
        try:
            _, parsed = request(base_url, "/actuator/health/readiness", timeout=3)
            if parsed and parsed.get("status") == "UP":
                return
            last_error = str(parsed)
        except (ControlError, OSError) as error:
            last_error = str(error)
        time.sleep(2)
    raise ControlError(f"backend readiness timeout: {last_error}")


def login(base_url: str, login_id: str, password: str) -> str:
    payload = json.dumps({"loginId": login_id, "password": password}).encode("utf-8")
    _, parsed = request(
        base_url,
        "/api/v1/auth/login",
        method="POST",
        body=payload,
        content_type="application/json",
    )
    data = envelope_data(parsed, "/api/v1/auth/login")
    if not isinstance(data, dict) or not isinstance(data.get("accessToken"), str):
        raise ControlError(f"로그인 token 누락: {login_id}")
    return data["accessToken"]


def smoke(args: argparse.Namespace) -> None:
    manifest = validate_bundle(args.seed_dir, args.expected_app_version)
    candidate = validate_generation(args.candidate)
    wait_for_readiness(args.base_url, args.timeout_seconds)

    _, status_json = request(args.base_url, "/api/v1/demo/status")
    status = envelope_data(status_json, "/api/v1/demo/status")
    if not isinstance(status, dict) or status.get("state") != args.expected_state:
        raise ControlError(f"demo status state 불일치: {status}")
    if status.get("uploadEnabled") is not True:
        raise ControlError("demo upload capability 계약 불일치")
    if args.expected_state == "READY":
        if status.get("generation") != candidate or status.get("writeLocked") is not False:
            raise ControlError("READY generation/writeLocked 계약 불일치")
    else:
        if status.get("candidateGeneration") != candidate or status.get("writeLocked") is not True:
            raise ControlError("VERIFYING candidateGeneration/writeLocked 계약 불일치")

    accounts = {account["loginId"]: account for account in manifest["publicAccounts"]}
    manager_token = login(args.base_url, "demo.manager", str(accounts["demo.manager"]["password"]))
    staff_token = login(args.base_url, "demo.staff", str(accounts["demo.staff"]["password"]))

    now = dt.datetime.now(tz=KST)
    read_smoke_paths = READ_SMOKE_PATHS + (
        f"/api/v1/attendances/me?year={now.year}&month={now.month}",
    )
    for path in read_smoke_paths:
        _, parsed = request(args.base_url, path, token=manager_token)
        envelope_data(parsed, path)
    _, parsed = request(args.base_url, "/api/v1/dashboard/summary", token=staff_token)
    envelope_data(parsed, "/api/v1/dashboard/summary")

    request(
        args.base_url,
        "/api/v1/roles/code-availability?code=SMOKE_DENIED",
        token=staff_token,
        expected_status=403,
    )
    if args.expected_state == "VERIFYING":
        _, upload_error = request(
            args.base_url,
            "/api/v1/files",
            token=manager_token,
            method="POST",
            body=b"--demo-boundary--\r\n",
            content_type="multipart/form-data; boundary=demo-boundary",
            expected_status=503,
        )
        if not upload_error or upload_error.get("code") != "DEMO_RESET_IN_PROGRESS":
            raise ControlError(f"복원 중 업로드 차단 code 불일치: {upload_error}")

    first_file = min(manifest["files"], key=lambda item: int(item["id"]))
    download_headers: dict[str, str] = {}
    payload, _ = request(
        args.base_url,
        f"/api/v1/drive/files/{first_file['id']}/download",
        token=manager_token,
        response_headers=download_headers,
    )
    if len(payload) != first_file["size"] or sha256_bytes(payload) != first_file["sha256"]:
        raise ControlError("seed file download checksum 불일치")
    content_type = download_headers.get("content-type", "").split(";", 1)[0].strip().lower()
    if content_type != first_file["contentType"]:
        raise ControlError(f"seed file download Content-Type 불일치: {content_type}")
    if download_headers.get("content-length") != str(first_file["size"]):
        raise ControlError("seed file download Content-Length 불일치")
    disposition = download_headers.get("content-disposition", "")
    encoded_name = urllib.parse.quote_plus(str(first_file["originalName"]), safe="")
    if not disposition.lower().startswith("attachment;") or encoded_name not in disposition:
        raise ControlError(f"seed file download Content-Disposition 불일치: {disposition}")
    print(
        f"smoke-ok: state={args.expected_state} generation={candidate} "
        f"reads={len(read_smoke_paths) + 1} accounts=2"
    )


def validate_acceptance_marker(value: str) -> str:
    if not ACCEPTANCE_MARKER_PATTERN.fullmatch(value):
        raise ControlError("acceptance marker 형식 오류")
    return value


def validate_acceptance_base_url(value: str) -> str:
    if value != "http://web:8080":
        raise ControlError("acceptance base URL mount allowlist violation")
    return value


def request_json(
    base_url: str,
    path: str,
    *,
    token: str,
    method: str = "GET",
    body: object | None = None,
    expected_status: int = 200,
) -> dict[str, object] | None:
    encoded = None
    content_type = None
    if body is not None:
        encoded = json.dumps(body, ensure_ascii=False, separators=(",", ":")).encode("utf-8")
        content_type = "application/json"
    _, parsed = request(
        base_url,
        path,
        token=token,
        method=method,
        body=encoded,
        content_type=content_type,
        expected_status=expected_status,
    )
    return parsed


def multipart_file_body(
    field_name: str,
    filename: str,
    content_type: str,
    payload: bytes,
) -> tuple[bytes, str]:
    """Build the single-file multipart form used by every upload acceptance path."""
    for label, value in (("field name", field_name), ("filename", filename), ("content type", content_type)):
        if not value or any(character in value for character in ('\r', '\n', '"')):
            raise ControlError(f"multipart {label} contract violation")
    boundary = f"simple-erp-demo-{uuid.uuid4().hex}"
    body = b"".join(
        (
            f"--{boundary}\r\n".encode("ascii"),
            (
                f'Content-Disposition: form-data; name="{field_name}"; '
                f'filename="{filename}"\r\n'
            ).encode("utf-8"),
            f"Content-Type: {content_type}\r\n\r\n".encode("ascii"),
            payload,
            f"\r\n--{boundary}--\r\n".encode("ascii"),
        )
    )
    return body, f"multipart/form-data; boundary={boundary}"


def request_file_upload(
    base_url: str,
    path: str,
    *,
    token: str,
    filename: str,
    content_type: str,
    payload: bytes,
    expected_status: int = 200,
    timeout: float = 60.0,
) -> dict[str, object] | None:
    body, multipart_content_type = multipart_file_body(
        "file", filename, content_type, payload
    )
    _, parsed = request(
        base_url,
        path,
        token=token,
        method="POST",
        body=body,
        content_type=multipart_content_type,
        expected_status=expected_status,
        timeout=timeout,
    )
    return parsed


def fill_xlsx_template(template: bytes, values: tuple[str | None, ...]) -> bytes:
    """Fill the existing template's first data row without reimplementing its workbook contract."""
    worksheet_name = "xl/worksheets/sheet1.xml"
    try:
        with zipfile.ZipFile(io.BytesIO(template)) as source:
            if worksheet_name not in source.namelist():
                raise ControlError("엑셀 업로드 양식의 첫 worksheet 누락")
            worksheet = ET.fromstring(source.read(worksheet_name))
            namespace = {"x": XLSX_MAIN_NAMESPACE}
            row = worksheet.find(".//x:sheetData/x:row[@r='2']", namespace)
            if row is None:
                raise ControlError("엑셀 업로드 양식의 입력 행 누락")
            cells = row.findall("x:c", namespace)
            if len(cells) != len(values):
                raise ControlError(
                    f"엑셀 업로드 양식 열 개수 불일치: expected={len(values)}, actual={len(cells)}"
                )

            for cell, value in zip(cells, values, strict=True):
                for child in list(cell):
                    cell.remove(child)
                cell.set("t", "inlineStr")
                inline = ET.SubElement(cell, f"{{{XLSX_MAIN_NAMESPACE}}}is")
                text = ET.SubElement(inline, f"{{{XLSX_MAIN_NAMESPACE}}}t")
                text.text = value or ""

            ET.register_namespace("", XLSX_MAIN_NAMESPACE)
            ET.register_namespace("r", XLSX_RELATIONSHIP_NAMESPACE)
            worksheet_bytes = ET.tostring(
                worksheet, encoding="utf-8", xml_declaration=True
            )
            output = io.BytesIO()
            with zipfile.ZipFile(output, "w") as target:
                for info in source.infolist():
                    target.writestr(
                        info,
                        worksheet_bytes
                        if info.filename == worksheet_name
                        else source.read(info),
                    )
            return output.getvalue()
    except (ET.ParseError, zipfile.BadZipFile, OSError) as error:
        raise ControlError("엑셀 업로드 양식을 수정할 수 없음") from error


def require_excel_upload_success(
    value: dict[str, object] | None,
    path: str,
) -> None:
    result = require_object_data(value, path)
    if (
        result.get("totalRows") != 1
        or result.get("successRows") != 1
        or result.get("failedRows") != 0
        or result.get("errors") != []
    ):
        actual = {
            key: result.get(key)
            for key in ("totalRows", "successRows", "failedRows", "errors")
        }
        raise ControlError(
            f"HTTP {path} Excel 1행 원자 업로드 계약 위반: actual={actual!r}"
        )


def require_unique_named_row(
    value: dict[str, object] | None,
    path: str,
    expected_name: str,
) -> dict[str, object]:
    page = require_page_data(value, path)
    matching = [
        row
        for row in page["content"]
        if isinstance(row, dict) and row.get("name") == expected_name
    ]
    if len(matching) != 1:
        raise ControlError(f"HTTP {path} 이름 기준 단일 행 계약 위반")
    return matching[0]


def acceptance_attachment_payload(marker: str) -> bytes:
    seed = (f"{marker}-attachment-body\n").encode("utf-8")
    repeats, remainder = divmod(ACCEPTANCE_ATTACHMENT_SIZE_BYTES, len(seed))
    return seed * repeats + seed[:remainder]


def acceptance_file_payload(marker: str, kind: str) -> bytes:
    if kind == "board":
        return acceptance_attachment_payload(marker)
    if kind not in {"approval", "expense", "pending", "drive"}:
        raise ControlError(f"acceptance file kind contract violation: {kind}")
    return f"{marker}-{kind}-body\n".encode("utf-8")


def require_download_contract(
    base_url: str,
    path: str,
    *,
    token: str,
    expected_name: str,
    expected_content_type: str,
    expected_payload: bytes,
) -> None:
    headers: dict[str, str] = {}
    payload, _ = request(
        base_url,
        path,
        token=token,
        response_headers=headers,
        timeout=60.0,
    )
    content_type = headers.get("content-type", "").split(";", 1)[0].strip().lower()
    disposition = headers.get("content-disposition", "")
    encoded_name = urllib.parse.quote_plus(expected_name, safe="")
    if (
        payload != expected_payload
        or content_type != expected_content_type
        or headers.get("content-length") != str(len(expected_payload))
        or not disposition.lower().startswith("attachment;")
        or encoded_name not in disposition
    ):
        raise ControlError(f"HTTP {path} download bytes/header contract violation")


def request_acceptance_attachment_at_size_boundary(
    context: AcceptanceContext,
    path: str,
    attachment_name: str,
) -> dict[str, object]:
    payload = acceptance_attachment_payload(context.marker)
    oversized_error = request_file_upload(
        context.base_url,
        path,
        token=context.staff_token,
        filename=f"{context.marker}-oversized.txt",
        content_type="text/plain",
        payload=payload + b"x",
        expected_status=413,
        timeout=context.heavy_request_timeout_seconds,
    )
    require_error_response(oversized_error, 413, path)
    return require_object_data(
        request_file_upload(
            context.base_url,
            path,
            token=context.staff_token,
            filename=attachment_name,
            content_type="text/plain",
            payload=payload,
            timeout=context.heavy_request_timeout_seconds,
        ),
        path,
    )


def require_object_data(value: dict[str, object] | None, path: str) -> dict[str, object]:
    data = envelope_data(value, path)
    if not isinstance(data, dict):
        raise ControlError(f"HTTP {path} object data 계약 위반")
    return data


def require_page_data(value: dict[str, object] | None, path: str) -> dict[str, object]:
    data = require_object_data(value, path)
    total_elements = data.get("totalElements")
    if (
        not isinstance(data.get("content"), list)
        or isinstance(total_elements, bool)
        or not isinstance(total_elements, int)
        or total_elements < 0
    ):
        raise ControlError(f"HTTP {path} page data 계약 위반")
    return data


def require_created_id(value: dict[str, object] | None, path: str) -> int:
    created_id = envelope_data(value, path)
    return require_positive_int(created_id, f"HTTP {path} 생성 ID")


def require_positive_int(value: object, label: str) -> int:
    if type(value) is not int or value <= 0:
        raise ControlError(f"{label} 계약 위반")
    return value


def require_error_response(
    value: dict[str, object] | None,
    expected_status: int,
    path: str,
    *,
    expected_code: str | None = None,
    expected_message: str | None = None,
) -> dict[str, object]:
    if (
        not isinstance(value, dict)
        or value.get("status") != expected_status
        or not isinstance(value.get("message"), str)
        or not value["message"].strip()
        or (expected_code is not None and value.get("code") != expected_code)
        or (expected_message is not None and value.get("message") != expected_message)
    ):
        actual = (
            {key: value.get(key) for key in ("status", "code", "message")}
            if isinstance(value, dict)
            else value
        )
        raise ControlError(f"HTTP {path} 오류 envelope 계약 위반: actual={actual!r}")
    return value


def attendance_dates(value: dict[str, object] | None, path: str) -> set[dt.date]:
    rows = envelope_data(value, path)
    if not isinstance(rows, list):
        raise ControlError(f"HTTP {path} 근태 목록 계약 위반")
    dates: set[dt.date] = set()
    for index, row in enumerate(rows):
        if not isinstance(row, dict) or not isinstance(row.get("workDate"), str):
            raise ControlError(f"HTTP {path} 근태 {index} 계약 위반")
        try:
            work_date = dt.date.fromisoformat(row["workDate"])
        except ValueError as error:
            raise ControlError(f"HTTP {path} 근태 {index} 날짜 계약 위반") from error
        dates.add(work_date)
    return dates


def acceptance_marker_queries(
    marker: str,
    manager_token: str,
    staff_token: str,
) -> tuple[tuple[str, str], ...]:
    return (
        (
            manager_token,
            "/api/v1/customers?" + urllib.parse.urlencode({"nameKeyword": marker, "size": 100}),
        ),
        (
            manager_token,
            "/api/v1/sales-contacts?"
            + urllib.parse.urlencode({"nameKeyword": marker, "size": 100}),
        ),
        (
            staff_token,
            "/api/v1/boards?" + urllib.parse.urlencode({"keyword": marker, "size": 100}),
        ),
        (
            staff_token,
            "/api/v1/expenses?"
            + urllib.parse.urlencode({"scope": "MINE", "keyword": marker, "size": 100}),
        ),
        (
            staff_token,
            "/api/v1/approvals?"
            + urllib.parse.urlencode({"box": "INVOLVED", "keyword": marker, "size": 100}),
        ),
    )


def wait_for_contract_equipment(
    base_url: str,
    equipment_path: str,
    token: str,
    contract_id: int,
) -> dict[str, object]:
    for _ in range(40):
        equipment_page = require_page_data(
            request_json(base_url, equipment_path, token=token), equipment_path
        )
        linked = [
            row
            for row in equipment_page["content"]
            if isinstance(row, dict) and row.get("contractId") == contract_id
        ]
        if len(linked) == 1:
            return linked[0]
        if len(linked) > 1:
            raise ControlError("contract created duplicate linked equipment")
        time.sleep(0.25)
    raise ControlError("contract did not create a linked equipment before deadline")


def ready_acceptance_context(
    args: argparse.Namespace,
) -> tuple[dict[str, object], str, str, dict[str, object], dict[str, object]]:
    validate_acceptance_base_url(args.base_url)
    wait_for_readiness(args.base_url, args.timeout_seconds)
    _, status_json = request(args.base_url, "/api/v1/demo/status")
    status = require_object_data(status_json, "/api/v1/demo/status")
    generation = status.get("generation")
    next_reset_at_raw = status.get("nextResetAt")
    try:
        next_reset_at = dt.datetime.fromisoformat(str(next_reset_at_raw).replace("Z", "+00:00"))
    except ValueError as error:
        raise ControlError("acceptance nextResetAt 계약 위반") from error
    if (
        status.get("enabled") is not True
        or status.get("environmentName") != "DEMO"
        or status.get("state") != "READY"
        or status.get("writeLocked") is not False
        or status.get("uploadEnabled") is not True
        or not isinstance(generation, str)
        or not GENERATION_PATTERN.fullmatch(generation)
        or next_reset_at.tzinfo is None
        or next_reset_at <= dt.datetime.now(tz=dt.timezone.utc) + dt.timedelta(minutes=5)
    ):
        raise ControlError("acceptance는 READY generation에서만 실행 가능")

    manifest = validate_bundle(args.seed_dir, args.expected_app_version)
    accounts = {account["loginId"]: account for account in manifest["publicAccounts"]}
    manager_token = login(
        args.base_url,
        "demo.manager",
        str(accounts["demo.manager"]["password"]),
    )
    staff_token = login(
        args.base_url,
        "demo.staff",
        str(accounts["demo.staff"]["password"]),
    )
    manager = require_object_data(
        request_json(args.base_url, "/api/v1/employees/me", token=manager_token),
        "/api/v1/employees/me",
    )
    staff = require_object_data(
        request_json(args.base_url, "/api/v1/employees/me", token=staff_token),
        "/api/v1/employees/me",
    )
    if (
        manager.get("loginId") != "demo.manager"
        or staff.get("loginId") != "demo.staff"
        or manager.get("roleCode") != "DEMO_MANAGER"
        or staff.get("roleCode") != "DEMO_STAFF"
    ):
        raise ControlError("데모 계정 역할 계약 불일치")
    manager["id"] = require_positive_int(manager.get("id"), "관리자형 데모 계정 직원 ID")
    staff["id"] = require_positive_int(staff.get("id"), "직원형 데모 계정 직원 ID")
    return status, manager_token, staff_token, manager, staff


@dataclass(frozen=True)
class AcceptanceContext:
    base_url: str
    marker: str
    generation: str
    manager_token: str
    staff_token: str
    manager_id: int
    staff_id: int
    now: dt.datetime
    verify_operator_protection: bool
    heavy_request_timeout_seconds: int

    @property
    def today(self) -> dt.date:
        return self.now.date()


@dataclass(frozen=True)
class AcceptancePreflightResult:
    reference_customer_count: int
    scoped_activity_count: int
    resigned_employee_id: int


@dataclass(frozen=True)
class CustomerSalesAcceptanceResult:
    customer_id: int


@dataclass(frozen=True)
class OperatorProtectionAcceptanceResult:
    employee_id: int | None


@dataclass(frozen=True)
class ContractEquipmentAcceptanceResult:
    contract_id: int
    equipment_id: int
    settled_contract_id: int
    settled_equipment_id: int


@dataclass(frozen=True)
class AfterServiceAcceptanceResult:
    after_service_id: int


@dataclass(frozen=True)
class UploadAcceptanceResult:
    excel_customer_id: int
    sales_contact_id: int
    board_file_id: int
    approval_file_id: int
    expense_file_id: int
    pending_file_id: int
    drive_file_id: int


@dataclass(frozen=True)
class ExcelUploadAcceptanceResult:
    customer_id: int
    sales_contact_id: int


@dataclass(frozen=True)
class GenericFileUploadAcceptanceResult:
    board_file_id: int
    approval_file_id: int
    expense_file_id: int
    pending_file_id: int


@dataclass(frozen=True)
class DriveFileUploadAcceptanceResult:
    drive_file_id: int


@dataclass(frozen=True)
class StaffWorkflowAcceptanceResult:
    board_id: int
    expense_id: int
    approval_id: int
    attendance_date: dt.date


@dataclass(frozen=True)
class BoardFileAcceptanceResult:
    board_id: int


@dataclass(frozen=True)
class ApprovalFileAcceptanceResult:
    approval_id: int


@dataclass(frozen=True)
class ExpenseFileAcceptanceResult:
    expense_id: int


@dataclass(frozen=True)
class AcceptanceExerciseResult:
    marker: str
    generation: str
    manager_id: int
    staff_id: int
    customer: CustomerSalesAcceptanceResult
    contract: ContractEquipmentAcceptanceResult
    after_service: AfterServiceAcceptanceResult
    upload: UploadAcceptanceResult
    staff_workflow: StaffWorkflowAcceptanceResult

    def summary(self) -> str:
        return (
            "acceptance-exercise-ok: "
            f"marker={self.marker} generation={self.generation} "
            f"customer={self.customer.customer_id} "
            f"contract={self.contract.contract_id} equipment={self.contract.equipment_id} "
            f"settledContract={self.contract.settled_contract_id} "
            f"settledEquipment={self.contract.settled_equipment_id} "
            f"afterService={self.after_service.after_service_id} "
            f"excelCustomer={self.upload.excel_customer_id} "
            f"salesContact={self.upload.sales_contact_id} "
            f"boardFile={self.upload.board_file_id} "
            f"approvalFile={self.upload.approval_file_id} "
            f"expenseFile={self.upload.expense_file_id} "
            f"pendingFile={self.upload.pending_file_id} "
            f"driveFile={self.upload.drive_file_id} "
            f"board={self.staff_workflow.board_id} expense={self.staff_workflow.expense_id} "
            f"approval={self.staff_workflow.approval_id} "
            f"attendanceDate={self.staff_workflow.attendance_date.isoformat()} "
            f"managerId={self.manager_id} staffId={self.staff_id}"
        )


def prepare_acceptance_context(args: argparse.Namespace) -> AcceptanceContext:
    marker = validate_acceptance_marker(args.marker)
    status, manager_token, staff_token, manager, staff = ready_acceptance_context(args)
    return AcceptanceContext(
        base_url=args.base_url,
        marker=marker,
        generation=str(status["generation"]),
        manager_token=manager_token,
        staff_token=staff_token,
        manager_id=require_positive_int(manager.get("id"), "manager acceptance employee ID"),
        staff_id=require_positive_int(staff.get("id"), "staff acceptance employee ID"),
        now=dt.datetime.now(tz=KST),
        verify_operator_protection=args.verify_operator_protection,
        heavy_request_timeout_seconds=args.timeout_seconds,
    )


def verify_acceptance_preconditions(
    context: AcceptanceContext,
) -> AcceptancePreflightResult:
    marker = context.marker
    manager_token = context.manager_token
    staff_token = context.staff_token
    now = context.now
    attendance_path = f"/api/v1/attendances/me?year={now.year}&month={now.month}"
    if now.date() in attendance_dates(
        request_json(context.base_url, attendance_path, token=staff_token), attendance_path
    ):
        raise ControlError("직원형 계정의 오늘 근태 기준 상태 불일치")
    for token, path in acceptance_marker_queries(marker, manager_token, staff_token):
        page = require_page_data(request_json(context.base_url, path, token=token), path)
        if page["content"] or page["totalElements"] != 0:
            raise ControlError(f"acceptance marker가 이미 존재함: {path}")

    staff_dashboard_path = "/api/v1/dashboard/summary"
    staff_dashboard_before = require_object_data(
        request_json(context.base_url, staff_dashboard_path, token=staff_token),
        staff_dashboard_path,
    )
    staff_kpi_before = staff_dashboard_before.get("kpi")
    if not isinstance(staff_kpi_before, dict):
        raise ControlError("직원형 dashboard KPI 계약 위반")
    reference_customer_count_before = staff_kpi_before.get("totalCustomers")
    scoped_activity_count_before = staff_kpi_before.get("monthlySalesActivities")
    if (
        isinstance(reference_customer_count_before, bool)
        or not isinstance(reference_customer_count_before, int)
        or reference_customer_count_before < 0
        or isinstance(scoped_activity_count_before, bool)
        or not isinstance(scoped_activity_count_before, int)
        or scoped_activity_count_before < 0
    ):
        raise ControlError("직원형 dashboard 가시 범위 KPI 계약 위반")

    resigned_path = "/api/v1/employees?status=RESIGNED&size=1&sort=id,asc"
    resigned_page = require_page_data(
        request_json(context.base_url, resigned_path, token=manager_token), resigned_path
    )
    if not resigned_page["content"] or not isinstance(resigned_page["content"][0], dict):
        raise ControlError("acceptance resigned employee reference is unavailable")
    resigned_employee_id = require_positive_int(
        resigned_page["content"][0].get("id"), "acceptance resigned employee ID"
    )

    approval_create_path = "/api/v1/approvals"
    for invalid_approver_id in (context.staff_id, resigned_employee_id):
        invalid_approval = request_json(
            context.base_url,
            approval_create_path,
            token=staff_token,
            method="POST",
            body={
                "title": f"{marker}-invalid-approver",
                "content": "must not be persisted",
                "approverIds": [invalid_approver_id],
                "attachmentFileIds": [],
            },
            expected_status=400,
        )
        require_error_response(
            invalid_approval,
            400,
            approval_create_path,
            expected_message="결재선이 올바르지 않습니다.",
        )
    return AcceptancePreflightResult(
        reference_customer_count=reference_customer_count_before,
        scoped_activity_count=scoped_activity_count_before,
        resigned_employee_id=resigned_employee_id,
    )


def verify_staff_dashboard_scope(
    dashboard: dict[str, object],
    customer_id: int,
    reference_customer_count_before: int,
    scoped_activity_count_before: int,
) -> None:
    staff_kpi = dashboard.get("kpi")
    recent_customers = dashboard.get("recentCustomers")
    recent_activities = dashboard.get("recentActivities")
    if (
        not isinstance(staff_kpi, dict)
        or not isinstance(recent_customers, list)
        or not isinstance(recent_activities, list)
    ):
        raise ControlError("직원 계정의 dashboard 응답 계약 위반")
    if staff_kpi.get("totalCustomers") != reference_customer_count_before + 1:
        raise ControlError("직원 계정의 고객사 기준정보 KPI가 갱신되지 않음")
    if not any(
        isinstance(row, dict) and row.get("id") == customer_id
        for row in recent_customers
    ):
        raise ControlError("직원 계정의 최근 고객사 기준정보가 갱신되지 않음")
    if staff_kpi.get("monthlySalesActivities") != scoped_activity_count_before:
        raise ControlError("직원 계정의 미배정 고객 영업활동이 KPI에 노출됨")
    if any(
        isinstance(row, dict) and row.get("customerId") == customer_id
        for row in recent_activities
    ):
        raise ControlError("직원 계정의 미배정 고객 영업활동이 최근 목록에 노출됨")


def exercise_customer_sales_acceptance(
    context: AcceptanceContext,
    preflight: AcceptancePreflightResult,
) -> CustomerSalesAcceptanceResult:
    marker = context.marker
    manager_token = context.manager_token
    staff_token = context.staff_token
    now = context.now
    reference_customer_count_before = preflight.reference_customer_count
    scoped_activity_count_before = preflight.scoped_activity_count
    staff_dashboard_path = "/api/v1/dashboard/summary"
    customer_path = "/api/v1/customers"
    customer_create = {
        "code": None,
        "name": marker,
        "type": "POTENTIAL",
        "status": "ACTIVE",
        "note": "reset acceptance marker",
    }
    customer_id = require_created_id(
        request_json(
            context.base_url,
            customer_path,
            token=manager_token,
            method="POST",
            body=customer_create,
        ),
        customer_path,
    )
    customer_detail_path = f"{customer_path}/{customer_id}"
    customer = require_object_data(
        request_json(context.base_url, customer_detail_path, token=manager_token),
        customer_detail_path,
    )
    if customer.get("name") != marker:
        raise ControlError("고객사 생성 종단 계약 불일치")

    updated_name = f"{marker}-updated"
    request_json(
        context.base_url,
        customer_detail_path,
        token=manager_token,
        method="PUT",
        body={
            "name": updated_name,
            "type": "KEY_ACCOUNT",
            "status": "ACTIVE",
            "note": "reset acceptance updated",
        },
        expected_status=204,
    )
    updated_customer = require_object_data(
        request_json(context.base_url, customer_detail_path, token=manager_token),
        customer_detail_path,
    )
    if updated_customer.get("name") != updated_name or updated_customer.get("type") != "KEY_ACCOUNT":
        raise ControlError("고객사 수정 종단 계약 불일치")

    staff_sales_customer_path = f"/api/v1/sales-customers/{customer_id}"
    hidden_customer_error = request_json(
        context.base_url,
        staff_sales_customer_path,
        token=staff_token,
        expected_status=404,
    )
    require_error_response(hidden_customer_error, 404, staff_sales_customer_path)
    staff_aggregate_path = "/api/v1/sales-customers/aggregates?" + urllib.parse.urlencode(
        {"customerIds": customer_id}
    )
    hidden_aggregates = envelope_data(
        request_json(context.base_url, staff_aggregate_path, token=staff_token),
        staff_aggregate_path,
    )
    if hidden_aggregates != []:
        raise ControlError("직원형 계정에 미배정 고객 집계가 노출됨")

    contact_path = "/api/v1/sales-contacts?size=1&sort=id,asc"
    contact_page = require_page_data(
        request_json(context.base_url, contact_path, token=manager_token), contact_path
    )
    if not contact_page["content"] or not isinstance(contact_page["content"][0], dict):
        raise ControlError("acceptance sales contact reference is unavailable")
    unrelated_contact_id = require_positive_int(
        contact_page["content"][0].get("id"), "acceptance sales contact ID"
    )
    activity_path = "/api/v1/sales-customers/activities"
    mismatched_activity = request_json(
        context.base_url,
        activity_path,
        token=manager_token,
        method="POST",
        body={
            "customerId": customer_id,
            "type": "CALL",
            "activityDate": now.replace(tzinfo=None, microsecond=0).isoformat(),
            "subject": f"{marker}-must-not-persist",
            "content": "cross-customer contact integrity check",
            "ourEmployeeId": context.manager_id,
            "customerContactId": unrelated_contact_id,
        },
        expected_status=400,
    )
    require_error_response(
        mismatched_activity,
        400,
        activity_path,
        expected_message="해당 고객사에 현재 재직 중인 고객 담당자만 지정할 수 있습니다.",
    )

    scoped_contact_create_path = "/api/v1/sales-contacts"
    scoped_contact_id = require_created_id(
        request_json(
            context.base_url,
            scoped_contact_create_path,
            token=manager_token,
            method="POST",
            body={
                "name": f"{marker}-contact",
                "sourceIds": [],
                "note": "customer scope acceptance",
            },
        ),
        scoped_contact_create_path,
    )
    scoped_employment_path = f"/api/v1/sales-contacts/{scoped_contact_id}/employments"
    require_created_id(
        request_json(
            context.base_url,
            scoped_employment_path,
            token=manager_token,
            method="POST",
            body={
                "customerId": customer_id,
                "externalCompanyName": None,
                "position": "담당자",
                "department": "영업팀",
                "startDate": now.date().isoformat(),
            },
        ),
        scoped_employment_path,
    )
    require_created_id(
        request_json(
            context.base_url,
            activity_path,
            token=manager_token,
            method="POST",
            body={
                "customerId": customer_id,
                "type": "CALL",
                "activityDate": now.replace(tzinfo=None, microsecond=0).isoformat(),
                "subject": marker,
                "content": "customer scope acceptance",
                "ourEmployeeId": context.manager_id,
                "customerContactId": scoped_contact_id,
            },
        ),
        activity_path,
    )
    staff_contact_activities_path = (
        f"/api/v1/sales-customers/contacts/{scoped_contact_id}/activities"
    )
    hidden_contact_activities = envelope_data(
        request_json(context.base_url, staff_contact_activities_path, token=staff_token),
        staff_contact_activities_path,
    )
    if hidden_contact_activities != []:
        raise ControlError("직원형 계정에 미배정 고객의 contact 활동이 노출됨")

    staff_dashboard_after = require_object_data(
        request_json(context.base_url, staff_dashboard_path, token=staff_token),
        staff_dashboard_path,
    )
    verify_staff_dashboard_scope(
        staff_dashboard_after,
        customer_id,
        reference_customer_count_before,
        scoped_activity_count_before,
    )

    staff_customer_error = request_json(
        context.base_url,
        customer_path,
        token=staff_token,
        method="POST",
        body=customer_create,
        expected_status=403,
    )
    require_error_response(staff_customer_error, 403, customer_path)
    return CustomerSalesAcceptanceResult(customer_id=customer_id)


def verify_operator_protection_acceptance(
    context: AcceptanceContext,
) -> OperatorProtectionAcceptanceResult:
    marker = context.marker
    manager_token = context.manager_token
    staff_token = context.staff_token
    now = context.now
    approval_create_path = "/api/v1/approvals"
    operator_employee_id: int | None = None
    staff_leave_admin_path = f"/api/v1/leaves/balances?year={now.year}"
    staff_leave_admin_error = request_json(
        context.base_url,
        staff_leave_admin_path,
        token=staff_token,
        expected_status=403,
    )
    require_error_response(staff_leave_admin_error, 403, staff_leave_admin_path)
    manager_employee_error = request_json(
        context.base_url,
        f"/api/v1/employees/{context.manager_id}",
        token=manager_token,
        method="DELETE",
        expected_status=403,
    )
    require_error_response(
        manager_employee_error,
        403,
        f"/api/v1/employees/{context.manager_id}",
    )

    if context.verify_operator_protection:
        operator_login_id = os.environ.get("APP_ADMIN_LOGIN_ID")
        operator_password = os.environ.get("APP_ADMIN_PASSWORD")
        if not operator_login_id or not operator_password:
            raise ControlError("operator protection acceptance credential missing")
        operator_token = login(context.base_url, operator_login_id, operator_password)
        operator_profile_path = "/api/v1/employees/me"
        operator_profile = require_object_data(
            request_json(context.base_url, operator_profile_path, token=operator_token),
            operator_profile_path,
        )
        if operator_profile.get("loginId") != operator_login_id:
            raise ControlError("recovery operator self profile contract mismatch")
        operator_employee_id = require_positive_int(
            operator_profile.get("id"), "recovery operator employee ID"
        )

        employee_search_path = "/api/v1/employees?size=100&sort=id,asc"
        employee_page = require_page_data(
            request_json(context.base_url, employee_search_path, token=manager_token),
            employee_search_path,
        )
        if any(
            isinstance(row, dict) and row.get("loginId") == operator_login_id
            for row in employee_page["content"]
        ):
            raise ControlError("recovery operator leaked through employee search")
        hidden_operator_path = f"/api/v1/employees/{operator_employee_id}"
        hidden_operator_error = request_json(
            context.base_url,
            hidden_operator_path,
            token=manager_token,
            expected_status=404,
        )
        require_error_response(hidden_operator_error, 404, hidden_operator_path)

        leave_balances_path = f"/api/v1/leaves/balances?year={now.year}"
        leave_balances = envelope_data(
            request_json(context.base_url, leave_balances_path, token=manager_token),
            leave_balances_path,
        )
        if not isinstance(leave_balances, list):
            raise ControlError("leave balance list envelope contract violation")
        if any(
            isinstance(row, dict) and row.get("employeeId") == operator_employee_id
            for row in leave_balances
        ):
            raise ControlError("recovery operator leaked through leave balances")

        recovery_operator_approval = request_json(
            context.base_url,
            approval_create_path,
            token=staff_token,
            method="POST",
            body={
                "title": f"{marker}-recovery-operator-approver",
                "content": "must not be persisted",
                "approverIds": [operator_employee_id],
                "attachmentFileIds": [],
            },
            expected_status=400,
        )
        require_error_response(
            recovery_operator_approval,
            400,
            approval_create_path,
            expected_message="결재선이 올바르지 않습니다.",
        )

        protected_employee_path = f"/api/v1/employees/{context.manager_id}"
        protected_employee_error = request_json(
            context.base_url,
            protected_employee_path,
            token=operator_token,
            method="DELETE",
            expected_status=403,
        )
        require_error_response(
            protected_employee_error,
            403,
            protected_employee_path,
            expected_code="DEMO_PROTECTED_RESOURCE",
        )
    return OperatorProtectionAcceptanceResult(employee_id=operator_employee_id)


def exercise_contract_equipment_acceptance(
    context: AcceptanceContext,
    preflight: AcceptancePreflightResult,
    customer: CustomerSalesAcceptanceResult,
    operator: OperatorProtectionAcceptanceResult,
) -> ContractEquipmentAcceptanceResult:
    marker = context.marker
    manager_token = context.manager_token
    customer_id = customer.customer_id
    resigned_employee_id = preflight.resigned_employee_id
    operator_employee_id = operator.employee_id
    today = context.today
    product_path = "/api/v1/products/summary?size=1&sort=id,asc"
    product_page = require_page_data(
        request_json(context.base_url, product_path, token=manager_token),
        product_path,
    )
    if not product_page["content"] or not isinstance(product_page["content"][0], dict):
        raise ControlError("acceptance product reference is unavailable")
    product_id = product_page["content"][0].get("id")
    product_supplier_id = product_page["content"][0].get("supplierId")
    product_id = require_positive_int(product_id, "acceptance product reference ID")
    product_supplier_id = require_positive_int(
        product_supplier_id, "acceptance product supplier ID"
    )

    contract_create = {
        "contractNo": None,
        "customerId": customer_id,
        "employeeId": context.manager_id,
        "productId": product_id,
        "outputValue": 12.5,
        "outputUnit": "KW",
        "optionText": marker,
        "initialAmount": 100000,
        "finalAmount": 1000000,
        "cretopGrade": None,
        "supportProgramName": None,
        "supportProgramStatus": "NONE",
        "contractDate": (today - dt.timedelta(days=3)).isoformat(),
        "dueDate": (today + dt.timedelta(days=30)).isoformat(),
        "orderDate": None,
        "expectedArrivalDate": None,
        "arrivalDate": None,
        "installedDate": None,
        "settledDate": None,
        "logisticsNote": "integration contract",
        "status": "CONTRACTED",
    }
    contract_path = "/api/v1/contracts"

    invalid_contract = dict(contract_create)
    invalid_contract["status"] = "INSTALLED"
    invalid_contract_error = request_json(
        context.base_url,
        contract_path,
        token=manager_token,
        method="POST",
        body=invalid_contract,
        expected_status=400,
    )
    require_error_response(
        invalid_contract_error,
        400,
        contract_path,
        expected_message="계약 상태와 일정 날짜의 흐름이 올바르지 않습니다.",
    )

    inactive_employee_contract = dict(contract_create)
    inactive_employee_contract["employeeId"] = resigned_employee_id
    inactive_contract_error = request_json(
        context.base_url,
        contract_path,
        token=manager_token,
        method="POST",
        body=inactive_employee_contract,
        expected_status=400,
    )
    require_error_response(
        inactive_contract_error,
        400,
        contract_path,
        expected_message="계약 담당자는 재직 중인 직원만 지정할 수 있습니다.",
    )

    if operator_employee_id is not None:
        recovery_operator_contract = dict(contract_create)
        recovery_operator_contract["employeeId"] = operator_employee_id
        recovery_operator_contract_error = request_json(
            context.base_url,
            contract_path,
            token=manager_token,
            method="POST",
            body=recovery_operator_contract,
            expected_status=400,
        )
        require_error_response(
            recovery_operator_contract_error,
            400,
            contract_path,
            expected_message="계약 담당자는 재직 중인 직원만 지정할 수 있습니다.",
        )

    contract_id = require_created_id(
        request_json(
            context.base_url,
            contract_path,
            token=manager_token,
            method="POST",
            body=contract_create,
        ),
        contract_path,
    )
    contract_detail_path = f"{contract_path}/{contract_id}"
    contract = require_object_data(
        request_json(context.base_url, contract_detail_path, token=manager_token),
        contract_detail_path,
    )
    if (
        contract.get("id") != contract_id
        or contract.get("customerId") != customer_id
        or contract.get("employeeId") != context.manager_id
        or contract.get("productId") != product_id
        or contract.get("supplierId") != product_supplier_id
        or contract.get("status") != "CONTRACTED"
        or not str(contract.get("contractNo", "")).startswith(
            f"CT{(today - dt.timedelta(days=3)).year}-"
        )
    ):
        raise ControlError("contract create mapping contract violation")
    contract_update = dict(contract_create)
    contract_update.pop("contractNo")
    contract_update["orderDate"] = (today - dt.timedelta(days=2)).isoformat()
    contract_update["expectedArrivalDate"] = (today - dt.timedelta(days=1)).isoformat()
    contract_update["arrivalDate"] = (today - dt.timedelta(days=1)).isoformat()
    contract_update["installedDate"] = today.isoformat()
    contract_update["status"] = "INSTALLED"
    request_json(
        context.base_url,
        contract_detail_path,
        token=manager_token,
        method="PUT",
        body=contract_update,
        expected_status=204,
    )
    installed_contract = require_object_data(
        request_json(context.base_url, contract_detail_path, token=manager_token),
        contract_detail_path,
    )
    if (
        installed_contract.get("status") != "INSTALLED"
        or installed_contract.get("installedDate") != today.isoformat()
    ):
        raise ControlError("installed contract transition contract violation")

    equipment_path = "/api/v1/equipments?" + urllib.parse.urlencode(
        {"customerId": customer_id, "size": 100}
    )
    linked_equipment = wait_for_contract_equipment(
        context.base_url, equipment_path, manager_token, contract_id
    )
    equipment_id = linked_equipment.get("id")
    equipment_id = require_positive_int(equipment_id, "contract-installed equipment ID")
    if (
        linked_equipment.get("customerId") != customer_id
        or linked_equipment.get("supplierId") != product_supplier_id
        or linked_equipment.get("productId") != product_id
        or linked_equipment.get("outputValue") != 12.5
        or linked_equipment.get("outputUnit") != "KW"
        or linked_equipment.get("installedDate") != today.isoformat()
        or linked_equipment.get("warrantyInsurance") is not False
    ):
        raise ControlError("contract-installed equipment mapping contract violation")

    # Repeating an already-installed update must not publish a duplicate equipment event.
    request_json(
        context.base_url,
        contract_detail_path,
        token=manager_token,
        method="PUT",
        body=contract_update,
        expected_status=204,
    )
    equipment_page = require_page_data(
        request_json(context.base_url, equipment_path, token=manager_token),
        equipment_path,
    )
    linked_equipments = [
        row
        for row in equipment_page["content"]
        if isinstance(row, dict) and row.get("contractId") == contract_id
    ]
    if len(linked_equipments) != 1:
        raise ControlError("installed contract created duplicate linked equipment")

    immutable_contract_update = dict(contract_update)
    immutable_contract_update["outputValue"] = 13.0
    immutable_contract_error = request_json(
        context.base_url,
        contract_detail_path,
        token=manager_token,
        method="PUT",
        body=immutable_contract_update,
        expected_status=400,
    )
    require_error_response(
        immutable_contract_error,
        400,
        contract_detail_path,
        expected_message=(
            "설비가 생성된 계약의 고객사·제품·출력·설치일과 완료 상태는 변경할 수 없습니다."
        ),
    )
    installed_contract_delete_error = request_json(
        context.base_url,
        contract_detail_path,
        token=manager_token,
        method="DELETE",
        expected_status=400,
    )
    require_error_response(
        installed_contract_delete_error,
        400,
        contract_detail_path,
        expected_message="설비 생성 대상이 된 계약은 삭제할 수 없습니다.",
    )

    linked_equipment_path = f"/api/v1/equipments/{equipment_id}"
    immutable_equipment_error = request_json(
        context.base_url,
        linked_equipment_path,
        token=manager_token,
        method="PUT",
        body={
            "customerId": customer_id,
            "productId": product_id,
            "outputValue": 13.0,
            "outputUnit": "KW",
            "serialNo": None,
            "installAddress": None,
            "installedDate": today.isoformat(),
            "confirmedDate": None,
            "warrantyStartDate": None,
            "oscillatorWarrantyMonths": None,
            "generalWarrantyMonths": None,
            "warrantyInsurance": False,
            "note": "must not be persisted",
        },
        expected_status=400,
    )
    require_error_response(
        immutable_equipment_error,
        400,
        linked_equipment_path,
        expected_message=(
            "계약에서 생성된 설비의 고객사·제품·출력·설치일은 변경할 수 없습니다."
        ),
    )
    linked_equipment_delete_error = request_json(
        context.base_url,
        linked_equipment_path,
        token=manager_token,
        method="DELETE",
        expected_status=400,
    )
    require_error_response(
        linked_equipment_delete_error,
        400,
        linked_equipment_path,
        expected_message="계약에서 생성된 설비는 직접 삭제할 수 없습니다.",
    )

    direct_settled_create = dict(contract_create)
    direct_settled_create.update(
        {
            "optionText": f"{marker}-direct-settled",
            "orderDate": (today - dt.timedelta(days=2)).isoformat(),
            "expectedArrivalDate": (today - dt.timedelta(days=1)).isoformat(),
            "arrivalDate": (today - dt.timedelta(days=1)).isoformat(),
            "installedDate": today.isoformat(),
            "settledDate": today.isoformat(),
            "status": "SETTLED",
        }
    )
    settled_contract_id = require_created_id(
        request_json(
            context.base_url,
            contract_path,
            token=manager_token,
            method="POST",
            body=direct_settled_create,
        ),
        contract_path,
    )
    settled_contract_path = f"{contract_path}/{settled_contract_id}"
    settled_contract = require_object_data(
        request_json(context.base_url, settled_contract_path, token=manager_token),
        settled_contract_path,
    )
    if (
        settled_contract.get("status") != "SETTLED"
        or settled_contract.get("installedDate") != today.isoformat()
        or settled_contract.get("settledDate") != today.isoformat()
        or not str(settled_contract.get("contractNo", "")).startswith(
            f"CT{(today - dt.timedelta(days=3)).year}-"
        )
    ):
        raise ControlError("direct-settled contract mapping contract violation")
    settled_equipment = wait_for_contract_equipment(
        context.base_url, equipment_path, manager_token, settled_contract_id
    )
    settled_equipment_id = require_positive_int(
        settled_equipment.get("id"), "direct-settled equipment ID"
    )
    if (
        settled_equipment.get("customerId") != customer_id
        or settled_equipment.get("contractId") != settled_contract_id
        or settled_equipment.get("installedDate") != today.isoformat()
    ):
        raise ControlError("direct-settled equipment mapping contract violation")
    return ContractEquipmentAcceptanceResult(
        contract_id=contract_id,
        equipment_id=equipment_id,
        settled_contract_id=settled_contract_id,
        settled_equipment_id=settled_equipment_id,
    )


def exercise_after_service_acceptance(
    context: AcceptanceContext,
    customer: CustomerSalesAcceptanceResult,
    contract: ContractEquipmentAcceptanceResult,
) -> AfterServiceAcceptanceResult:
    manager_token = context.manager_token
    marker = context.marker
    today = context.today
    customer_id = customer.customer_id
    equipment_id = contract.equipment_id
    after_service_path = "/api/v1/after-services"
    after_service_id = require_created_id(
        request_json(
            context.base_url,
            after_service_path,
            token=manager_token,
            method="POST",
            body={
                "receiptNo": None,
                "customerId": customer_id,
                "equipmentId": equipment_id,
                "receivedDate": today.isoformat(),
                "type": "REPAIR",
                "symptom": marker,
                "status": "RECEIVED",
                "assignedEngineerId": None,
                "warrantyDecision": "UNDECIDED",
                "billingAmount": None,
                "completedDate": None,
            },
        ),
        after_service_path,
    )
    after_service_detail_path = f"{after_service_path}/{after_service_id}"
    after_service = require_object_data(
        request_json(context.base_url, after_service_detail_path, token=manager_token),
        after_service_detail_path,
    )
    if (
        after_service.get("id") != after_service_id
        or after_service.get("customerId") != customer_id
        or after_service.get("equipmentId") != equipment_id
        or after_service.get("type") != "REPAIR"
        or after_service.get("symptom") != marker
        or after_service.get("status") != "RECEIVED"
        or after_service.get("warrantyDecision") != "UNDECIDED"
    ):
        raise ControlError("contract-equipment-after-service mapping contract violation")
    return AfterServiceAcceptanceResult(after_service_id=after_service_id)


def exercise_excel_upload_acceptance(
    context: AcceptanceContext,
) -> ExcelUploadAcceptanceResult:
    marker = context.marker
    base_url = context.base_url
    manager_token = context.manager_token
    today = context.today.isoformat()

    customer_template_path = "/api/v1/customers/excel/template"
    customer_template, _ = request(
        base_url,
        customer_template_path,
        token=manager_token,
        timeout=context.heavy_request_timeout_seconds,
    )
    customer_name = f"{marker}-excel"
    customer_workbook = fill_xlsx_template(
        customer_template,
        (
            None,
            customer_name,
            None,
            "데모 담당자",
            "000-0000-9142",
            "acceptance@customer.example",
            "서울특별시 중구 세종대로 110",
            "일반고객",
            "거래중",
            today,
        ),
    )
    customer_upload_path = "/api/v1/customers/excel/upload"
    require_excel_upload_success(
        request_file_upload(
            base_url,
            customer_upload_path,
            token=manager_token,
            filename="customers_acceptance.xlsx",
            content_type=(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            ),
            payload=customer_workbook,
            timeout=context.heavy_request_timeout_seconds,
        ),
        customer_upload_path,
    )
    customer_search_path = "/api/v1/customers?" + urllib.parse.urlencode(
        {"nameKeyword": customer_name, "size": 10}
    )
    excel_customer = require_unique_named_row(
        request_json(base_url, customer_search_path, token=manager_token),
        customer_search_path,
        customer_name,
    )
    excel_customer_id = require_positive_int(
        excel_customer.get("id"), "Excel 고객사 ID"
    )

    contact_template_path = "/api/v1/sales-contacts/excel/template"
    contact_template, _ = request(
        base_url,
        contact_template_path,
        token=manager_token,
        timeout=context.heavy_request_timeout_seconds,
    )
    contact_name = f"{marker}-xls-ct"
    contact_workbook = fill_xlsx_template(
        contact_template,
        (
            contact_name,
            None,
            None,
            "000-2371-6408",
            f"{marker}@contact.example",
            None,
            today,
            "자동화 산업전",
            customer_name,
            "구매팀장",
            "구매팀",
            "Excel upload reset acceptance",
        ),
    )
    contact_upload_path = "/api/v1/sales-contacts/excel/upload"
    require_excel_upload_success(
        request_file_upload(
            base_url,
            contact_upload_path,
            token=manager_token,
            filename="sales_contacts_acceptance.xlsx",
            content_type=(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            ),
            payload=contact_workbook,
            timeout=context.heavy_request_timeout_seconds,
        ),
        contact_upload_path,
    )
    contact_search_path = "/api/v1/sales-contacts?" + urllib.parse.urlencode(
        {"nameKeyword": contact_name, "size": 10}
    )
    sales_contact = require_unique_named_row(
        request_json(base_url, contact_search_path, token=manager_token),
        contact_search_path,
        contact_name,
    )
    sales_contact_id = require_positive_int(
        sales_contact.get("id"), "Excel 영업 명부 ID"
    )
    return ExcelUploadAcceptanceResult(
        customer_id=excel_customer_id,
        sales_contact_id=sales_contact_id,
    )


def upload_pending_acceptance_file(
    context: AcceptanceContext,
    kind: str,
    token: str,
    expected_uploader_id: int,
) -> int:
    path = "/api/v1/files"
    name = f"{context.marker}-{kind}.txt"
    payload = acceptance_file_payload(context.marker, kind)
    uploaded = require_object_data(
        request_file_upload(
            context.base_url,
            path,
            token=token,
            filename=name,
            content_type="text/plain",
            payload=payload,
        ),
        path,
    )
    file_id = require_positive_int(uploaded.get("id"), f"{kind} file ID")
    if (
        uploaded.get("originalName") != name
        or uploaded.get("contentType") != "text/plain"
        or uploaded.get("size") != len(payload)
        or uploaded.get("uploaderId") != expected_uploader_id
    ):
        raise ControlError(f"{kind} pending upload metadata contract violation")
    return file_id


def exercise_generic_file_upload_acceptance(
    context: AcceptanceContext,
) -> GenericFileUploadAcceptanceResult:
    marker = context.marker

    generic_upload_path = "/api/v1/files"
    board_name = f"{marker}-board.txt"
    board_file = request_acceptance_attachment_at_size_boundary(
        context,
        generic_upload_path,
        board_name,
    )
    board_file_id = require_positive_int(
        board_file.get("id"), "게시판 첨부 파일 ID"
    )
    if (
        board_file.get("originalName") != board_name
        or board_file.get("contentType") != "text/plain"
        or board_file.get("size") != ACCEPTANCE_ATTACHMENT_SIZE_BYTES
        or board_file.get("uploaderId") != context.staff_id
    ):
        raise ControlError("게시판 첨부 업로드 메타데이터 계약 불일치")

    approval_file_id = upload_pending_acceptance_file(
        context, "approval", context.staff_token, context.staff_id
    )
    expense_file_id = upload_pending_acceptance_file(
        context, "expense", context.staff_token, context.staff_id
    )
    # This object deliberately remains PENDING. A different uploader attempts to
    # claim it later so both uploader isolation and restart retention are observable.
    pending_file_id = upload_pending_acceptance_file(
        context, "pending", context.manager_token, context.manager_id
    )
    return GenericFileUploadAcceptanceResult(
        board_file_id=board_file_id,
        approval_file_id=approval_file_id,
        expense_file_id=expense_file_id,
        pending_file_id=pending_file_id,
    )


def exercise_drive_file_upload_acceptance(
    context: AcceptanceContext,
) -> DriveFileUploadAcceptanceResult:
    marker = context.marker

    # Drive owns an atomic store-and-DRIVE_FILE claim. It deliberately does not
    # consume a generic PENDING upload.
    drive_name = f"{marker}-drive.txt"
    drive_payload = (f"{marker}-drive-body\n").encode("utf-8")
    drive_upload_path = "/api/v1/drive/files"
    drive_file_id = require_created_id(
        request_file_upload(
            context.base_url,
            drive_upload_path,
            token=context.manager_token,
            filename=drive_name,
            content_type="text/plain",
            payload=drive_payload,
        ),
        drive_upload_path,
    )
    drive_browse_path = "/api/v1/drive"
    drive = require_object_data(
        request_json(
            context.base_url,
            drive_browse_path,
            token=context.manager_token,
        ),
        drive_browse_path,
    )
    drive_files = drive.get("files")
    if not isinstance(drive_files, list) or not any(
        isinstance(row, dict)
        and row.get("id") == drive_file_id
        and row.get("name") == drive_name
        and row.get("size") == len(drive_payload)
        and row.get("uploaderId") == context.manager_id
        for row in drive_files
    ):
        raise ControlError("Drive 업로드 목록 반영 계약 불일치")
    require_download_contract(
        context.base_url,
        f"/api/v1/drive/files/{drive_file_id}/download",
        token=context.manager_token,
        expected_name=drive_name,
        expected_content_type="text/plain",
        expected_payload=drive_payload,
    )

    return DriveFileUploadAcceptanceResult(drive_file_id=drive_file_id)


def exercise_upload_acceptance(context: AcceptanceContext) -> UploadAcceptanceResult:
    excel = exercise_excel_upload_acceptance(context)
    generic = exercise_generic_file_upload_acceptance(context)
    drive = exercise_drive_file_upload_acceptance(context)
    return UploadAcceptanceResult(
        excel_customer_id=excel.customer_id,
        sales_contact_id=excel.sales_contact_id,
        board_file_id=generic.board_file_id,
        approval_file_id=generic.approval_file_id,
        expense_file_id=generic.expense_file_id,
        pending_file_id=generic.pending_file_id,
        drive_file_id=drive.drive_file_id,
    )


def require_hidden_download(base_url: str, path: str, token: str) -> None:
    _, error = request(base_url, path, token=token, expected_status=404)
    require_error_response(error, 404, path)


def exercise_board_file_acceptance(
    context: AcceptanceContext,
    upload: UploadAcceptanceResult,
) -> BoardFileAcceptanceResult:
    board_path = "/api/v1/boards"
    board_id = require_created_id(
        request_json(
            context.base_url,
            board_path,
            token=context.staff_token,
            method="POST",
            body={
                "category": "FREE",
                "title": context.marker,
                "content": "reset acceptance",
                "attachmentFileIds": [upload.board_file_id],
            },
        ),
        board_path,
    )
    detail_path = f"{board_path}/{board_id}"
    board = require_object_data(
        request_json(context.base_url, detail_path, token=context.staff_token),
        detail_path,
    )
    attachments = board.get("attachments")
    if (
        board.get("title") != context.marker
        or board.get("authorId") != context.staff_id
        or not isinstance(attachments, list)
        or len(attachments) != 1
        or not isinstance(attachments[0], dict)
        or attachments[0].get("fileId") != upload.board_file_id
        or attachments[0].get("name") != f"{context.marker}-board.txt"
        or attachments[0].get("size") != ACCEPTANCE_ATTACHMENT_SIZE_BYTES
    ):
        raise ControlError("게시글 첨부 claim 계약 불일치")
    require_download_contract(
        context.base_url,
        f"{detail_path}/attachments/{upload.board_file_id}",
        token=context.staff_token,
        expected_name=f"{context.marker}-board.txt",
        expected_content_type="text/plain",
        expected_payload=acceptance_file_payload(context.marker, "board"),
    )

    foreign_pending_error = request_json(
        context.base_url,
        board_path,
        token=context.staff_token,
        method="POST",
        body={
            "category": "FREE",
            "title": f"{context.marker}-foreign-pending",
            "content": "must not be persisted",
            "attachmentFileIds": [upload.pending_file_id],
        },
        expected_status=400,
    )
    require_error_response(
        foreign_pending_error,
        400,
        board_path,
        expected_message="업로드한 본인의 미사용 파일만 연결할 수 있습니다.",
    )
    return BoardFileAcceptanceResult(board_id=board_id)


def exercise_approval_file_acceptance(
    context: AcceptanceContext,
    upload: UploadAcceptanceResult,
    board: BoardFileAcceptanceResult,
) -> ApprovalFileAcceptanceResult:
    create_path = "/api/v1/approvals"
    approval_id = require_created_id(
        request_json(
            context.base_url,
            create_path,
            token=context.staff_token,
            method="POST",
            body={
                "title": f"{context.marker}-approval",
                "content": "attachment owner acceptance",
                "approverIds": [context.manager_id],
                "attachmentFileIds": [upload.approval_file_id],
            },
        ),
        create_path,
    )
    detail_path = f"{create_path}/{approval_id}"
    approval = require_object_data(
        request_json(context.base_url, detail_path, token=context.staff_token),
        detail_path,
    )
    attachments = approval.get("attachments")
    if (
        approval.get("docType") != "GENERAL"
        or not isinstance(attachments, list)
        or len(attachments) != 1
        or not isinstance(attachments[0], dict)
        or attachments[0].get("fileId") != upload.approval_file_id
    ):
        raise ControlError("일반 결재 첨부 claim 계약 불일치")
    require_download_contract(
        context.base_url,
        f"{detail_path}/attachments/{upload.approval_file_id}",
        token=context.staff_token,
        expected_name=f"{context.marker}-approval.txt",
        expected_content_type="text/plain",
        expected_payload=acceptance_file_payload(context.marker, "approval"),
    )

    reused_error = request_json(
        context.base_url,
        create_path,
        token=context.staff_token,
        method="POST",
        body={
            "title": f"{context.marker}-reused-board-approval",
            "content": "must not be persisted",
            "approverIds": [context.manager_id],
            "attachmentFileIds": [upload.board_file_id],
        },
        expected_status=400,
    )
    require_error_response(
        reused_error,
        400,
        create_path,
        expected_message="업로드한 본인의 미사용 파일만 연결할 수 있습니다.",
    )
    require_hidden_download(
        context.base_url,
        f"{detail_path}/attachments/{upload.board_file_id}",
        context.staff_token,
    )
    require_hidden_download(
        context.base_url,
        f"/api/v1/boards/{board.board_id}/attachments/{upload.approval_file_id}",
        context.staff_token,
    )
    return ApprovalFileAcceptanceResult(approval_id=approval_id)


def exercise_expense_file_acceptance(
    context: AcceptanceContext,
    upload: UploadAcceptanceResult,
) -> ExpenseFileAcceptanceResult:
    create_path = "/api/v1/expenses"
    expense_id = require_created_id(
        request_json(
            context.base_url,
            create_path,
            token=context.staff_token,
            method="POST",
            body={
                "title": context.marker,
                "items": [{
                    "expenseDate": context.today.isoformat(),
                    "category": "MEAL",
                    "amount": 12345,
                    "description": "integration approval",
                    "receiptFileId": upload.expense_file_id,
                }],
                "approverIds": [context.manager_id],
            },
        ),
        create_path,
    )
    detail_path = f"{create_path}/{expense_id}"
    expense = require_object_data(
        request_json(context.base_url, detail_path, token=context.staff_token),
        detail_path,
    )
    items = expense.get("items")
    approval_id = require_positive_int(
        expense.get("approvalDocumentId"), "경비 결재문서 ID"
    )
    if (
        expense.get("status") != "IN_PROGRESS"
        or not isinstance(items, list)
        or len(items) != 1
        or not isinstance(items[0], dict)
        or items[0].get("receiptFileId") != upload.expense_file_id
    ):
        raise ControlError("경비 영수증 claim 계약 불일치")
    require_download_contract(
        context.base_url,
        f"{detail_path}/receipts/{upload.expense_file_id}",
        token=context.staff_token,
        expected_name=f"{context.marker}-expense.txt",
        expected_content_type="text/plain",
        expected_payload=acceptance_file_payload(context.marker, "expense"),
    )

    reused_error = request_json(
        context.base_url,
        create_path,
        token=context.staff_token,
        method="POST",
        body={
            "title": f"{context.marker}-reused-board-expense",
            "items": [{
                "expenseDate": context.today.isoformat(),
                "category": "MEAL",
                "amount": 1000,
                "description": "must not be persisted",
                "receiptFileId": upload.board_file_id,
            }],
            "approverIds": [context.manager_id],
        },
        expected_status=400,
    )
    require_error_response(
        reused_error,
        400,
        create_path,
        expected_message="업로드한 본인의 미사용 파일만 연결할 수 있습니다.",
    )
    require_hidden_download(
        context.base_url,
        f"{detail_path}/receipts/{upload.board_file_id}",
        context.staff_token,
    )
    verify_expense_approval_acceptance(context, detail_path, approval_id)
    return ExpenseFileAcceptanceResult(expense_id=expense_id)


def verify_expense_approval_acceptance(
    context: AcceptanceContext,
    expense_detail_path: str,
    approval_id: int,
) -> None:
    approval_path = f"/api/v1/approvals/{approval_id}"
    request_json(
        context.base_url,
        f"{approval_path}/approve",
        token=context.manager_token,
        method="POST",
        body={"comment": "integration approved"},
        expected_status=204,
    )
    approved_expense = require_object_data(
        request_json(
            context.base_url,
            expense_detail_path,
            token=context.staff_token,
        ),
        expense_detail_path,
    )
    approved_document = require_object_data(
        request_json(context.base_url, approval_path, token=context.manager_token),
        approval_path,
    )
    if (
        approved_expense.get("status") != "APPROVED"
        or approved_document.get("status") != "APPROVED"
    ):
        raise ControlError("결재 결과의 경비 상태 반영 계약 불일치")


def exercise_attendance_acceptance(context: AcceptanceContext) -> dt.date:
    check_in_path = "/api/v1/attendances/check-in"
    check_in = require_object_data(
        request_json(
            context.base_url,
            check_in_path,
            token=context.staff_token,
            method="POST",
            body={"latitude": 0.0, "longitude": 0.0},
        ),
        check_in_path,
    )
    if (
        check_in.get("employeeId") != context.staff_id
        or check_in.get("checkInWithinRange") is not True
    ):
        raise ControlError("모의 위치 출근 종단 계약 불일치")
    try:
        work_date = dt.date.fromisoformat(str(check_in.get("workDate")))
    except ValueError as error:
        raise ControlError("모의 위치 출근 근무일 계약 불일치") from error
    check_out_path = "/api/v1/attendances/check-out"
    check_out = require_object_data(
        request_json(
            context.base_url,
            check_out_path,
            token=context.staff_token,
            method="POST",
            body={"latitude": 0.0, "longitude": 0.0},
        ),
        check_out_path,
    )
    if (
        check_out.get("checkOutWithinRange") is not True
        or check_out.get("workDate") != work_date.isoformat()
    ):
        raise ControlError("모의 위치 퇴근 종단 계약 불일치")
    return work_date


def exercise_staff_workflow_acceptance(
    context: AcceptanceContext,
    upload: UploadAcceptanceResult,
) -> StaffWorkflowAcceptanceResult:
    board = exercise_board_file_acceptance(context, upload)
    approval = exercise_approval_file_acceptance(context, upload, board)
    expense = exercise_expense_file_acceptance(context, upload)
    attendance_date = exercise_attendance_acceptance(context)
    return StaffWorkflowAcceptanceResult(
        board_id=board.board_id,
        expense_id=expense.expense_id,
        approval_id=approval.approval_id,
        attendance_date=attendance_date,
    )


def exercise_acceptance(args: argparse.Namespace) -> None:
    context = prepare_acceptance_context(args)
    preflight = verify_acceptance_preconditions(context)
    customer = exercise_customer_sales_acceptance(context, preflight)
    operator_result = verify_operator_protection_acceptance(context)
    contract_result = exercise_contract_equipment_acceptance(
        context, preflight, customer, operator_result
    )
    after_service_result = exercise_after_service_acceptance(
        context, customer, contract_result
    )
    upload_result = exercise_upload_acceptance(context)
    staff_workflow_result = exercise_staff_workflow_acceptance(context, upload_result)
    result = AcceptanceExerciseResult(
        marker=context.marker,
        generation=context.generation,
        manager_id=context.manager_id,
        staff_id=context.staff_id,
        customer=customer,
        contract=contract_result,
        after_service=after_service_result,
        upload=upload_result,
        staff_workflow=staff_workflow_result,
    )
    print(result.summary())


def acceptance_created_ids(args: argparse.Namespace) -> dict[str, int]:
    return {
        "customer": require_positive_int(args.customer_id, "acceptance customer ID evidence"),
        "excel-customer": require_positive_int(
            args.excel_customer_id, "acceptance Excel customer ID evidence"
        ),
        "sales-contact": require_positive_int(
            args.sales_contact_id, "acceptance sales contact ID evidence"
        ),
        "contract": require_positive_int(args.contract_id, "acceptance contract ID evidence"),
        "equipment": require_positive_int(args.equipment_id, "acceptance equipment ID evidence"),
        "settled-contract": require_positive_int(
            args.settled_contract_id, "acceptance settled contract ID evidence"
        ),
        "settled-equipment": require_positive_int(
            args.settled_equipment_id, "acceptance settled equipment ID evidence"
        ),
        "after-service": require_positive_int(
            args.after_service_id, "acceptance after-service ID evidence"
        ),
        "board": require_positive_int(args.board_id, "acceptance board ID evidence"),
        "expense": require_positive_int(args.expense_id, "acceptance expense ID evidence"),
        "approval": require_positive_int(args.approval_id, "acceptance approval ID evidence"),
        "board-file": require_positive_int(
            args.board_file_id, "acceptance board file ID evidence"
        ),
        "approval-file": require_positive_int(
            args.approval_file_id, "acceptance approval file ID evidence"
        ),
        "expense-file": require_positive_int(
            args.expense_file_id, "acceptance expense file ID evidence"
        ),
        "pending-file": require_positive_int(
            args.pending_file_id, "acceptance pending file ID evidence"
        ),
        "drive-file": require_positive_int(
            args.drive_file_id, "acceptance Drive file ID evidence"
        ),
    }


def delete_retained_acceptance(args: argparse.Namespace) -> None:
    marker = validate_acceptance_marker(args.marker)
    previous_generation = validate_generation(args.previous_generation)
    board_id = require_positive_int(args.board_id, "acceptance board ID evidence")
    board_file_id = require_positive_int(
        args.board_file_id, "acceptance board file ID evidence"
    )
    drive_file_id = require_positive_int(
        args.drive_file_id, "acceptance Drive file ID evidence"
    )
    status, manager_token, staff_token, _, _ = ready_acceptance_context(args)
    if status["generation"] != previous_generation:
        raise ControlError("delete-retained generation changed before deletion")

    board_path = f"/api/v1/boards/{board_id}"
    request_json(
        args.base_url,
        board_path,
        token=staff_token,
        method="DELETE",
        expected_status=204,
    )
    board_error = request_json(
        args.base_url, board_path, token=staff_token, expected_status=404
    )
    require_error_response(board_error, 404, board_path)
    require_hidden_download(
        args.base_url,
        f"{board_path}/attachments/{board_file_id}",
        staff_token,
    )

    drive_path = f"/api/v1/drive/files/{drive_file_id}"
    request_json(
        args.base_url,
        drive_path,
        token=manager_token,
        method="DELETE",
        expected_status=204,
    )
    drive_download_path = f"{drive_path}/download"
    _, drive_error = request(
        args.base_url,
        drive_download_path,
        token=manager_token,
        expected_status=404,
    )
    require_error_response(drive_error, 404, drive_download_path)
    print(
        "acceptance-delete-retained-ok: "
        f"marker={marker} generation={previous_generation} board={board_id} drive={drive_file_id}"
    )


def verify_live_acceptance(args: argparse.Namespace) -> None:
    marker = validate_acceptance_marker(args.marker)
    previous_generation = validate_generation(args.previous_generation)
    created_ids = acceptance_created_ids(args)
    status, manager_token, staff_token, _, _ = ready_acceptance_context(args)
    if status["generation"] != previous_generation:
        raise ControlError("backend 재시작 뒤 acceptance generation이 변경됨")

    excel_customer_path = f"/api/v1/customers/{created_ids['excel-customer']}"
    excel_customer = require_object_data(
        request_json(args.base_url, excel_customer_path, token=manager_token),
        excel_customer_path,
    )
    if excel_customer.get("name") != f"{marker}-excel":
        raise ControlError("backend 재시작 뒤 Excel 고객사 누락")

    sales_contact_path = f"/api/v1/sales-contacts/{created_ids['sales-contact']}"
    sales_contact = require_object_data(
        request_json(args.base_url, sales_contact_path, token=manager_token),
        sales_contact_path,
    )
    if sales_contact.get("name") != f"{marker}-xls-ct":
        raise ControlError("backend 재시작 뒤 Excel 영업 명부 누락")

    approval_path = f"/api/v1/approvals/{created_ids['approval']}"
    require_download_contract(
        args.base_url,
        f"{approval_path}/attachments/{created_ids['approval-file']}",
        token=staff_token,
        expected_name=f"{marker}-approval.txt",
        expected_content_type="text/plain",
        expected_payload=acceptance_file_payload(marker, "approval"),
    )
    expense_path = f"/api/v1/expenses/{created_ids['expense']}"
    require_download_contract(
        args.base_url,
        f"{expense_path}/receipts/{created_ids['expense-file']}",
        token=staff_token,
        expected_name=f"{marker}-expense.txt",
        expected_content_type="text/plain",
        expected_payload=acceptance_file_payload(marker, "expense"),
    )

    removed_paths = (
        (
            staff_token,
            f"/api/v1/boards/{created_ids['board']}",
            "GET",
        ),
        (
            manager_token,
            f"/api/v1/drive/files/{created_ids['drive-file']}/download",
            "GET",
        ),
        (
            staff_token,
            f"/api/v1/boards/{created_ids['board']}/attachments/"
            f"{created_ids['board-file']}",
            "GET",
        ),
    )
    for token, path, method in removed_paths:
        _, error = request(
            args.base_url, path, token=token, method=method, expected_status=404
        )
        require_error_response(error, 404, path)

    print(
        "acceptance-live-ok: "
        f"marker={marker} generation={previous_generation} "
        "claimedDownloads=2 deletedOwners=2"
    )


def verify_reset_acceptance(args: argparse.Namespace) -> None:
    marker = validate_acceptance_marker(args.marker)
    previous_generation = validate_generation(args.previous_generation)
    created_ids = acceptance_created_ids(args)
    try:
        attendance_date = dt.date.fromisoformat(args.attendance_date)
    except (TypeError, ValueError) as error:
        raise ControlError("reset attendance date evidence contract violation") from error

    status, manager_token, staff_token, _, _ = ready_acceptance_context(args)
    if status["generation"] == previous_generation:
        raise ControlError("reset 뒤 generation이 변경되지 않음")

    for token, path in acceptance_marker_queries(marker, manager_token, staff_token):
        page = require_page_data(request_json(args.base_url, path, token=token), path)
        if page["content"] or page["totalElements"] != 0:
            raise ControlError(f"reset 뒤 acceptance marker가 남음: {path}")

    detail_contracts = (
        (manager_token, f"/api/v1/customers/{created_ids['customer']}"),
        (manager_token, f"/api/v1/customers/{created_ids['excel-customer']}"),
        (manager_token, f"/api/v1/sales-contacts/{created_ids['sales-contact']}"),
        (manager_token, f"/api/v1/contracts/{created_ids['contract']}"),
        (manager_token, f"/api/v1/equipments/{created_ids['equipment']}"),
        (manager_token, f"/api/v1/contracts/{created_ids['settled-contract']}"),
        (manager_token, f"/api/v1/equipments/{created_ids['settled-equipment']}"),
        (manager_token, f"/api/v1/after-services/{created_ids['after-service']}"),
        (staff_token, f"/api/v1/boards/{created_ids['board']}"),
        (staff_token, f"/api/v1/expenses/{created_ids['expense']}"),
        (manager_token, f"/api/v1/approvals/{created_ids['approval']}"),
    )
    for token, path in detail_contracts:
        error = request_json(args.base_url, path, token=token, expected_status=404)
        require_error_response(error, 404, path)

    removed_downloads = (
        (
            staff_token,
            f"/api/v1/boards/{created_ids['board']}/attachments/"
            f"{created_ids['board-file']}",
        ),
        (
            manager_token,
            f"/api/v1/drive/files/{created_ids['drive-file']}/download",
        ),
        (
            staff_token,
            f"/api/v1/approvals/{created_ids['approval']}/attachments/"
            f"{created_ids['approval-file']}",
        ),
        (
            staff_token,
            f"/api/v1/expenses/{created_ids['expense']}/receipts/"
            f"{created_ids['expense-file']}",
        ),
    )
    for token, path in removed_downloads:
        _, error = request(
            args.base_url, path, token=token, expected_status=404
        )
        require_error_response(error, 404, path)

    attendance_path = (
        f"/api/v1/attendances/me?year={attendance_date.year}&month={attendance_date.month}"
    )
    if attendance_date in attendance_dates(
        request_json(args.base_url, attendance_path, token=staff_token), attendance_path
    ):
        raise ControlError("reset 뒤 직원형 계정의 근태 변경이 남음")

    print(
        "acceptance-reset-ok: "
        f"marker={marker} previous={previous_generation} generation={status['generation']}"
    )


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser()
    subparsers = parser.add_subparsers(dest="command", required=True)

    validate = subparsers.add_parser("validate-bundle")
    validate.add_argument("--seed-dir", type=Path, required=True)
    validate.add_argument("--expected-app-version")

    manifest_value = subparsers.add_parser("manifest-value")
    manifest_value.add_argument("--seed-dir", type=Path, required=True)
    manifest_value.add_argument("--key", choices=sorted(ALLOWED_MANIFEST_KEYS), required=True)

    subparsers.add_parser("new-generation")

    state = subparsers.add_parser("write-state")
    state.add_argument("--seed-dir", type=Path, required=True)
    state.add_argument("--state-dir", type=Path, required=True)
    state.add_argument("--filename", choices=sorted(ALLOWED_STATE_FILES), default="status.json")
    state.add_argument("--state", choices=("READY", "VERIFYING"), required=True)
    state.add_argument("--candidate", required=True)
    state.add_argument("--next-reset-at", required=True)
    state.add_argument("--expected-app-version")

    resetting_state = subparsers.add_parser("write-resetting-state")
    resetting_state.add_argument("--state-dir", type=Path, required=True)
    resetting_state.add_argument("--candidate", required=True)
    resetting_state.add_argument("--next-reset-at", required=True)

    failed_state = subparsers.add_parser("write-failed-state")
    failed_state.add_argument("--state-dir", type=Path, required=True)
    failed_state.add_argument("--candidate", required=True)
    failed_state.add_argument("--next-reset-at", required=True)

    failure_log = subparsers.add_parser("write-control-plane-failure-log")
    failure_log.add_argument("--logs-root", type=Path, required=True)
    failure_log.add_argument("--candidate", required=True)
    failure_log.add_argument("--stage", required=True)
    failure_log.add_argument("--line", type=int, required=True)
    failure_log.add_argument("--exit-code", type=int, required=True)
    failure_log.add_argument(
        "--failed-state-published", choices=("true", "false"), required=True
    )

    stage = subparsers.add_parser("stage-files")
    stage.add_argument("--seed-dir", type=Path, required=True)
    stage.add_argument("--files-root", type=Path, required=True)
    stage.add_argument("--mapping", type=Path, required=True)
    stage.add_argument("--generation", required=True)
    stage.add_argument("--expected-app-version")

    promote = subparsers.add_parser("promote-files")
    promote.add_argument("--seed-dir", type=Path, required=True)
    promote.add_argument("--files-root", type=Path, required=True)
    promote.add_argument("--mapping", type=Path, required=True)
    promote.add_argument("--generation", required=True)
    promote.add_argument("--expected-app-version")

    current = subparsers.add_parser("verify-current-files")
    current.add_argument("--seed-dir", type=Path, required=True)
    current.add_argument("--files-root", type=Path, required=True)
    current.add_argument("--mapping", type=Path, required=True)
    current.add_argument("--generation", required=True)
    current.add_argument("--expected-app-version")

    absent = subparsers.add_parser("assert-generation-absent")
    absent.add_argument("--files-root", type=Path, required=True)
    absent.add_argument("--generation", required=True)

    acceptance_file = subparsers.add_parser("verify-acceptance-file")
    acceptance_file.add_argument("--files-root", type=Path, required=True)
    acceptance_file.add_argument("--generation", required=True)
    acceptance_file.add_argument("--relative-path", required=True)
    acceptance_file.add_argument("--marker", required=True)
    acceptance_file.add_argument(
        "--kind",
        choices=("board", "approval", "expense", "pending", "drive"),
        required=True,
    )

    cleanup = subparsers.add_parser("cleanup-artifacts")
    cleanup.add_argument("--phase", choices=("pre-reset", "post-success"), required=True)
    cleanup.add_argument("--files-root", type=Path, required=True)
    cleanup.add_argument("--state-dir", type=Path, required=True)
    cleanup.add_argument("--work-root", type=Path, required=True)
    cleanup.add_argument("--logs-root", type=Path, required=True)
    cleanup.add_argument("--candidate")

    smoke_parser = subparsers.add_parser("smoke")
    smoke_parser.add_argument("--seed-dir", type=Path, required=True)
    smoke_parser.add_argument("--base-url", required=True)
    smoke_parser.add_argument("--expected-state", choices=("READY", "VERIFYING"), required=True)
    smoke_parser.add_argument("--candidate", required=True)
    smoke_parser.add_argument("--expected-app-version")
    smoke_parser.add_argument("--timeout-seconds", type=int, default=120)

    acceptance_parser = subparsers.add_parser("acceptance")
    acceptance_parser.add_argument("--seed-dir", type=Path, required=True)
    acceptance_parser.add_argument("--base-url", required=True)
    acceptance_parser.add_argument(
        "--phase",
        choices=("exercise", "delete-retained", "verify-live", "verify-reset"),
        required=True,
    )
    acceptance_parser.add_argument("--marker", required=True)
    acceptance_parser.add_argument("--previous-generation")
    acceptance_parser.add_argument("--customer-id", type=int)
    acceptance_parser.add_argument("--excel-customer-id", type=int)
    acceptance_parser.add_argument("--sales-contact-id", type=int)
    acceptance_parser.add_argument("--contract-id", type=int)
    acceptance_parser.add_argument("--equipment-id", type=int)
    acceptance_parser.add_argument("--settled-contract-id", type=int)
    acceptance_parser.add_argument("--settled-equipment-id", type=int)
    acceptance_parser.add_argument("--after-service-id", type=int)
    acceptance_parser.add_argument("--board-id", type=int)
    acceptance_parser.add_argument("--expense-id", type=int)
    acceptance_parser.add_argument("--approval-id", type=int)
    acceptance_parser.add_argument("--board-file-id", type=int)
    acceptance_parser.add_argument("--approval-file-id", type=int)
    acceptance_parser.add_argument("--expense-file-id", type=int)
    acceptance_parser.add_argument("--pending-file-id", type=int)
    acceptance_parser.add_argument("--drive-file-id", type=int)
    acceptance_parser.add_argument("--attendance-date")
    acceptance_parser.add_argument("--verify-operator-protection", action="store_true")
    acceptance_parser.add_argument("--expected-app-version")
    acceptance_parser.add_argument("--timeout-seconds", type=int, default=120)
    return parser


def main() -> int:
    args = build_parser().parse_args()
    try:
        if args.command == "validate-bundle":
            manifest = validate_bundle(args.seed_dir, args.expected_app_version)
            print(
                f"bundle-ok: seedVersion={manifest['seedVersion']} "
                f"schemaVersion={manifest['schemaVersion']} files={len(manifest['files'])}"
            )
        elif args.command == "manifest-value":
            manifest = validate_bundle(args.seed_dir)
            value = manifest[args.key]
            if not isinstance(value, (str, int, float, bool)):
                raise ControlError("요청한 manifest 값은 scalar가 아님")
            print(value)
        elif args.command == "new-generation":
            print(uuid.uuid4())
        elif args.command == "write-state":
            write_state(args)
        elif args.command == "write-resetting-state":
            write_resetting_state(args)
        elif args.command == "write-failed-state":
            write_failed_state(args)
        elif args.command == "write-control-plane-failure-log":
            write_control_plane_failure_log(args)
        elif args.command == "stage-files":
            stage_files(args)
        elif args.command == "promote-files":
            promote_files(args)
        elif args.command == "verify-current-files":
            verify_current_files(args)
        elif args.command == "assert-generation-absent":
            assert_generation_absent(args)
        elif args.command == "verify-acceptance-file":
            verify_acceptance_file(args)
        elif args.command == "cleanup-artifacts":
            cleanup_artifacts(args)
        elif args.command == "smoke":
            smoke(args)
        elif args.command == "acceptance":
            if args.phase == "exercise":
                if args.previous_generation is not None:
                    raise ControlError("exercise phase에는 previous generation을 지정할 수 없음")
                exercise_acceptance(args)
            elif args.phase == "delete-retained":
                if args.previous_generation is None:
                    raise ControlError("delete-retained phase에는 previous generation이 필요")
                delete_retained_acceptance(args)
            elif args.phase == "verify-live":
                if args.previous_generation is None:
                    raise ControlError("verify-live phase에는 previous generation이 필요")
                verify_live_acceptance(args)
            else:
                if args.previous_generation is None:
                    raise ControlError("verify-reset phase에는 previous generation이 필요")
                verify_reset_acceptance(args)
        else:
            raise ControlError(f"지원하지 않는 command: {args.command}")
    except (ControlError, EOFError, OSError, tarfile.TarError, zipfile.BadZipFile) as error:
        print(f"demo-control-error: {error}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
