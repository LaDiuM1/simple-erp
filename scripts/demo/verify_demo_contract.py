#!/usr/bin/env python3
"""Validate the static deployment contract shared by local demo runs and CI."""

from __future__ import annotations

import argparse
import ast
import json
from pathlib import Path
import re
import sys
from typing import NoReturn


CONTROL_IMAGE = (
    "python:3.13-alpine@sha256:"
    "399babc8b49529dabfd9c922f2b5eea81d611e4512e3ed250d75bd2e7683f4b0"
)
MARIADB_IMAGE = (
    "mariadb:11.8.6@sha256:"
    "78a5047d3ba33975f183f183c2464cc7f1eab13ec8667e57cc9a5821d6da7577"
)
TESTED_BACKEND_IMAGE = "simple-erp-backend:acceptance"
TESTED_WEB_IMAGE = "simple-erp-web:acceptance"
TESTED_BACKEND_ARCHIVE = "/tmp/simple-erp-backend.tar.gz"
TESTED_WEB_ARCHIVE = "/tmp/simple-erp-web.tar.gz"
DOWNLOADED_ARTIFACT_DIR = "/tmp/tested-demo-images"
TESTED_ARTIFACT_NAME = "tested-demo-images-${{ github.sha }}"
DIGEST_ARTIFACT_NAME = "demo-image-digests-${{ github.sha }}"
ACTION_PINS = {
    "actions/checkout": "11d5960a326750d5838078e36cf38b85af677262",
    "actions/setup-java": "b6effb05e454b25005698d916606bdc6ffcbf961",
    "actions/setup-node": "49933ea5288caeca8642d1e84afbd3f7d6820020",
    "actions/setup-python": "a26af69be951a213d495a4c3e4e4022e16d87065",
    "actions/upload-artifact": "ea165f8d65b6e75b540449e92b4886f43607fa02",
    "actions/download-artifact": "d3f86a106a0bac45b974a628896c90dbdf5c8093",
    "docker/setup-qemu-action": "c7c53464625b32c7a7e944ae62b3e17d2b600130",
    "docker/setup-buildx-action": "8d2750c68a42422c14e847fe6c8ac0403b4cbd6f",
    "docker/build-push-action": "10e90e3645eae34f1e60eeb005ba3a3d33f178e8",
    "docker/login-action": "c94ce9fb468520275223c153574b00df6fe4bcc9",
}
DEMO_SERVICE_NAMES = {"db", "backend", "web", "demo-tool", "demo-tool-smoke"}
EXPECTED_LOGGING = {
    "driver": "local",
    "options": {"max-file": "3", "max-size": "10m"},
}
PUBLIC_RESOURCE_LIMITS = {
    "DEMO_UPLOAD_ACCOUNT_QUOTA_BYTES": "268435456",
    "DEMO_UPLOAD_ACCOUNT_QUOTA_FILES": "16",
    "DEMO_UPLOAD_GENERATION_QUOTA_BYTES": "536870912",
    "DEMO_UPLOAD_GENERATION_QUOTA_FILES": "32",
    "DEMO_UPLOAD_MIN_FREE_BYTES": "5368709120",
    "DEMO_UPLOAD_MIN_FREE_RATIO": "0.20",
    "DEMO_MAX_CONCURRENT_TRANSFERS": "2",
    "DEMO_MAX_CONCURRENT_UPLOADS_PER_ACCOUNT": "1",
    "DEMO_MAX_CONCURRENT_DOWNLOADS_PER_ACCOUNT": "2",
    "DEMO_EXCEL_ACCOUNT_QUOTA_ROWS": "500",
    "DEMO_EXCEL_GENERATION_QUOTA_ROWS": "1000",
    "DEMO_EXCEL_EXPORT_MAX_ROWS": "500",
    "DEMO_LOGIN_GLOBAL_RATE_LIMIT": "30",
    "DEMO_RATE_LIMIT_WINDOW": "PT1M",
    "DEMO_INGRESS_RATE_LIMIT": "300",
    "DEMO_INGRESS_GLOBAL_RATE_LIMIT": "600",
    "DEMO_MAX_CONCURRENT_INGRESS": "8",
    "DEMO_WRITE_RATE_LIMIT": "60",
    "DEMO_WRITE_GLOBAL_RATE_LIMIT": "90",
    "DEMO_MAX_CONCURRENT_WRITES": "4",
    "DEMO_READ_RATE_LIMIT": "120",
    "DEMO_READ_GLOBAL_RATE_LIMIT": "180",
    "DEMO_PREVIEW_RATE_LIMIT": "20",
    "DEMO_PREVIEW_GLOBAL_RATE_LIMIT": "30",
    "DEMO_MAX_CONCURRENT_READS": "4",
    "DEMO_MAX_CONCURRENT_PREVIEWS": "2",
    "DEMO_UPLOAD_RATE_LIMIT": "10",
    "DEMO_UPLOAD_GLOBAL_RATE_LIMIT": "16",
    "DEMO_EXCEL_UPLOAD_RATE_LIMIT": "2",
    "DEMO_EXCEL_UPLOAD_GLOBAL_RATE_LIMIT": "2",
    "DEMO_DOWNLOAD_RATE_LIMIT": "20",
    "DEMO_DOWNLOAD_GLOBAL_RATE_LIMIT": "30",
    "DEMO_DOWNLOAD_BYTE_RATE_WINDOW": "PT1H",
    "DEMO_DOWNLOAD_BYTE_RATE_LIMIT": "67108864",
    "DEMO_DOWNLOAD_GLOBAL_BYTE_RATE_LIMIT": "100663296",
}
IMMUTABLE_IMAGE_PATTERN = re.compile(
    r"(?:sha256:[0-9a-f]{64}|[^@\s]+@sha256:[0-9a-f]{64})"
)
STORED_FILE_MAPPING_COLUMNS = (
    "id, stored_name, original_name, content_type, size, "
    "DATE_FORMAT(created_at, '%Y-%m-%d'), "
    "DATE_FORMAT((SELECT reset_at FROM demo_seed_manifest WHERE id=1), '%Y-%m-%d'), "
    "status, owner_type, owner_id, uploader_id"
)


class ContractViolation(RuntimeError):
    """Raised when a resolved demo configuration weakens a required invariant."""


def fail(message: str) -> NoReturn:
    raise ContractViolation(message)


def require(condition: bool, message: str) -> None:
    if not condition:
        fail(message)


def require_mapping(value: object, label: str) -> dict[str, object]:
    if not isinstance(value, dict):
        fail(f"{label} must be an object")
    return value


def require_list(value: object, label: str) -> list[object]:
    if not isinstance(value, list):
        fail(f"{label} must be a list")
    return value


def normalize_sql(value: str) -> str:
    return " ".join(value.split())


def validate_stored_file_mapping_queries(reset_script: str, seed_workflow: str) -> None:
    expected = normalize_sql("SELECT " + STORED_FILE_MAPPING_COLUMNS + " FROM stored_files ORDER BY id")
    for label, source in (("reset", reset_script), ("seed workflow", seed_workflow)):
        normalized = normalize_sql(source)
        require(expected in normalized, f"{label} stored file mapping query changed")
    require_ordered(
        seed_workflow,
        "> runtime/work/ci/stored-files.tsv",
        "chmod 0711 runtime/work runtime/work/ci",
        "chmod 0644 runtime/work/ci/stored-files.tsv",
        'demo-tool stage-files \\\n',
        label="seed workflow bind fixture permissions",
    )
    require_ordered(
        seed_workflow,
        "demo-tool verify-current-files \\\n",
        "rm -f -- runtime/work/ci/stored-files.tsv",
        "rmdir -- runtime/work/ci",
        "chmod 0700 runtime/work",
        label="seed workflow bind fixture cleanup",
    )
    require_ordered(
        seed_workflow,
        "chmod 0700 runtime/work",
        "sudo install -d -o 0 -g 10001 -m 0755 runtime/state",
        'demo-tool write-state \\\n',
        label="seed workflow state bind ownership",
    )
    require_ordered(
        seed_workflow,
        "- name: Stop isolated CI services",
        'sudo chown -R "$(id -u):$(id -g)" runtime/state',
        label="seed workflow state bind cleanup",
    )


def load_compose_config(path: Path) -> dict[str, object]:
    try:
        value = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as error:
        raise ContractViolation(f"cannot read resolved Compose config: {path}") from error
    return require_mapping(value, "resolved Compose config")


def bind_contract(volumes: object) -> set[tuple[Path, str, bool]]:
    result: set[tuple[Path, str, bool]] = set()
    for index, raw_volume in enumerate(require_list(volumes, "service volumes")):
        volume = require_mapping(raw_volume, f"service volume {index}")
        if volume.get("type") != "bind":
            continue
        source = volume.get("source")
        target = volume.get("target")
        if not isinstance(source, str) or not isinstance(target, str):
            fail(f"bind volume {index} must have string source and target")
        result.add((Path(source).resolve(), target, volume.get("read_only") is True))
    return result


def named_volume_contract(volumes: object) -> set[tuple[str, str, bool]]:
    result: set[tuple[str, str, bool]] = set()
    for index, raw_volume in enumerate(require_list(volumes, "service volumes")):
        volume = require_mapping(raw_volume, f"service volume {index}")
        if volume.get("type") != "volume":
            continue
        source = volume.get("source")
        target = volume.get("target")
        if not isinstance(source, str) or not isinstance(target, str):
            fail(f"named volume {index} must have string source and target")
        result.add((source, target, volume.get("read_only") is True))
    return result


def validate_root_credential_isolation(
    db_environment: dict[str, object],
    service_environments: dict[str, dict[str, object]],
) -> None:
    root_password = db_environment.get("MARIADB_ROOT_PASSWORD")
    require(
        isinstance(root_password, str) and bool(root_password),
        "database root credential is missing",
    )
    for service_name, environment in service_environments.items():
        credential_embedded = any(
            isinstance(value, str) and root_password in value
            for value in environment.values()
        )
        require(
            "DEMO_DB_ROOT_PASSWORD" not in environment
            and "MARIADB_ROOT_PASSWORD" not in environment
            and not credential_embedded,
            f"root DB credential leaked to {service_name}",
        )


def select_demo_services(config: dict[str, object]) -> dict[str, dict[str, object]]:
    services_value = require_mapping(config.get("services"), "Compose services")
    require(
        set(services_value) == DEMO_SERVICE_NAMES,
        "Compose service allowlist changed",
    )
    return {
        name: require_mapping(services_value[name], f"service {name}")
        for name in DEMO_SERVICE_NAMES
    }


def validate_web_public_ports(value: object) -> None:
    ports = require_list(value, "web ports")
    normalized: set[tuple[int, str]] = set()
    for index, raw_port in enumerate(ports):
        port = require_mapping(raw_port, f"web port {index}")
        host_ip = port.get("host_ip")
        target = port.get("target")
        published = port.get("published")
        protocol = port.get("protocol")
        if not isinstance(host_ip, str) or not host_ip:
            fail(f"web port {index} has an invalid host bind")
        if not isinstance(target, int):
            fail(f"web port {index} has an invalid container target")
        published_text = str(published)
        if not published_text.isdecimal() or not 1 <= int(published_text) <= 65535:
            fail(f"web port {index} has an invalid published host port")
        normalized.add((target, str(protocol)))
    require(
        len(ports) == 2 and normalized == {(80, "tcp"), (443, "tcp")},
        "web must publish exactly container TCP targets 80 and 443",
    )


def validate_public_resource_limits(environment: dict[str, object]) -> None:
    actual = {key: environment.get(key) for key in PUBLIC_RESOURCE_LIMITS}
    require(
        actual == PUBLIC_RESOURCE_LIMITS,
        "public demo resource and cost envelope changed",
    )


def validate_demo_transaction_isolation(environment: dict[str, object]) -> None:
    require(
        environment.get("SPRING_DATASOURCE_HIKARI_TRANSACTION_ISOLATION")
        == "TRANSACTION_READ_COMMITTED",
        "demo database transaction isolation must stay READ_COMMITTED",
    )


def validate_service_contracts(config: dict[str, object]) -> dict[str, dict[str, object]]:
    require(config.get("name") == "simple-erp-demo", "Compose project name changed")
    services = select_demo_services(config)

    db = services["db"]
    backend = services["backend"]
    web = services["web"]
    tool = services["demo-tool"]
    smoke_tool = services["demo-tool-smoke"]
    backend_env = require_mapping(backend.get("environment"), "backend environment")
    db_env = require_mapping(db.get("environment"), "db environment")
    web_env = require_mapping(web.get("environment"), "web environment")
    tool_env = require_mapping(tool.get("environment"), "demo-tool environment")
    smoke_tool_env = require_mapping(
        smoke_tool.get("environment"), "demo-tool-smoke environment"
    )

    require(not db.get("ports"), "database must not publish host ports")
    require(not backend.get("ports"), "backend must not publish host ports")
    require(db.get("image") == MARIADB_IMAGE, "MariaDB image digest changed")
    validate_web_public_ports(web.get("ports"))
    require(backend_env.get("DB_USERNAME") == "simple_erp_app", "app DB user changed")
    require(backend_env.get("DDL_AUTO") == "validate", "backend DDL mode must stay validate")
    require(
        backend_env.get("APP_SCHEMA_MAINTENANCE_ENABLED") == "false",
        "schema maintenance must stay disabled",
    )
    require(
        backend_env.get("APP_REFERENCE_BOOTSTRAP_ENABLED") == "false",
        "reference bootstrap must stay disabled",
    )
    require(
        backend_env.get("APP_ADMIN_BOOTSTRAP_ENABLED") == "true",
        "recovery operator bootstrap must stay enabled",
    )
    require(
        backend_env.get("DEMO_UPLOAD_ENABLED") == "true",
        "demo upload capability must stay enabled",
    )
    validate_demo_transaction_isolation(backend_env)
    validate_public_resource_limits(backend_env)
    for service_name in ("backend", "web"):
        image = services[service_name].get("image")
        require(
            isinstance(image, str) and IMMUTABLE_IMAGE_PATTERN.fullmatch(image) is not None,
            f"{service_name} image must be immutable",
        )
    validate_root_credential_isolation(
        db_env,
        {
            "backend": backend_env,
            "web": web_env,
            "demo-tool": tool_env,
            "demo-tool-smoke": smoke_tool_env,
        },
    )
    require(
        set(tool_env) == {"PYTHONDONTWRITEBYTECODE", "TZ"},
        "demo-tool environment allowlist changed",
    )
    require(
        set(smoke_tool_env) == {"PYTHONDONTWRITEBYTECODE", "TZ"},
        "demo-tool-smoke environment allowlist changed",
    )
    require(web_env.get("API_REQUEST_BODY_MAX_SIZE") == "32MB", "web upload body limit changed")
    require(web_env.get("API_JSON_REQUEST_BODY_MAX_SIZE") == "1MB", "web JSON body limit changed")
    require(
        db_env.get("MARIADB_ROOT_PASSWORD") != backend_env.get("DB_PASSWORD"),
        "database root and app credentials must differ",
    )
    for service_name in DEMO_SERVICE_NAMES:
        require(
            services[service_name].get("logging") == EXPECTED_LOGGING,
            f"bounded logging contract changed for {service_name}",
        )
    for service_name in ("demo-tool", "demo-tool-smoke"):
        service = services[service_name]
        require(service.get("image") == CONTROL_IMAGE, f"{service_name} image changed")
        require(
            service.get("entrypoint") == ["python", "/opt/demo-control.py"],
            f"{service_name} entrypoint changed",
        )
        require(
            service.get("user") == "0:10001",
            f"{service_name} must share the backend file group",
        )
        require(service.get("read_only") is True, f"{service_name} must stay read-only")
        require(service.get("cap_drop") == ["ALL"], f"{service_name} capabilities changed")
        require(
            service.get("security_opt") == ["no-new-privileges:true"],
            f"{service_name} security options changed",
        )
    require(tool.get("network_mode") == "none", "offline demo-tool gained network access")
    require(
        smoke_tool.get("network_mode") != "none",
        "smoke tool must retain network access to the demo web service",
    )

    volumes = require_mapping(config.get("volumes"), "Compose volumes")
    demo_files = require_mapping(volumes.get("demo_files"), "demo_files volume")
    require(
        demo_files.get("name") == "simple-erp-demo-files",
        "demo files volume allowlist changed",
    )
    return services


def validate_service_mount_contracts(
    project_root: Path,
    services: dict[str, dict[str, object]],
) -> None:
    state_source = (project_root / "runtime/state").resolve()
    backend_volumes = services["backend"].get("volumes")
    web_volumes = services["web"].get("volumes")

    require(
        bind_contract(backend_volumes)
        == {(state_source, "/app/data/demo-state", True)},
        "backend state mount must stay read-only and isolated",
    )
    require(
        named_volume_contract(backend_volumes)
        == {("demo_files", "/app/data/files", False)},
        "backend demo files mount must stay writable and isolated",
    )
    require(
        bind_contract(web_volumes) == {(state_source, "/srv/demo", True)},
        "web state mount must stay read-only and isolated",
    )
    require(
        named_volume_contract(web_volumes)
        == {
            ("demo_caddy_data", "/data", False),
            ("demo_caddy_config", "/config", False),
        },
        "web named volume allowlist changed",
    )


def validate_tool_mount_contracts(
    project_root: Path,
    services: dict[str, dict[str, object]],
) -> None:
    tool_volumes = services["demo-tool"].get("volumes")
    smoke_tool_volumes = services["demo-tool-smoke"].get("volumes")
    expected_shared_binds = {
        (project_root / "scripts/demo/demo_control.py", "/opt/demo-control.py", True),
        (project_root / "demo/seed", "/seed", True),
    }
    expected_tool_binds = expected_shared_binds | {
        (project_root / "runtime/state", "/state", False),
        (project_root / "runtime/work", "/work", False),
        (project_root / "runtime/logs", "/logs", False),
    }
    require(
        bind_contract(tool_volumes) == expected_tool_binds,
        "demo-tool bind allowlist changed; project-root mounts are forbidden",
    )
    require(
        bind_contract(smoke_tool_volumes) == expected_shared_binds,
        "demo-tool-smoke bind allowlist changed",
    )
    require(
        named_volume_contract(tool_volumes) == {("demo_files", "/files", False)},
        "demo-tool named volume contract changed",
    )
    require(
        not named_volume_contract(smoke_tool_volumes),
        "demo-tool-smoke must not mount named volumes",
    )


def require_ordered(text: str, *needles: str, label: str) -> None:
    positions: list[int] = []
    for needle in needles:
        position = text.find(needle)
        require(position >= 0, f"{label} is missing: {needle}")
        positions.append(position)
    require(positions == sorted(positions), f"{label} ordering changed")


def shell_function_body(script: str, name: str) -> tuple[str, int]:
    match = re.search(
        rf"(?ms)^{re.escape(name)}\(\) \{{\n(?P<body>.*?)^\}}$",
        script,
    )
    require(match is not None, f"shell function is missing: {name}")
    return match.group("body"), match.end()


def python_function_source(script: str, name: str) -> str:
    try:
        module = ast.parse(script)
    except SyntaxError as error:
        raise ContractViolation("demo control script is not valid Python") from error
    matches = [
        node
        for node in module.body
        if isinstance(node, (ast.FunctionDef, ast.AsyncFunctionDef))
        and node.name == name
    ]
    require(len(matches) == 1, f"Python function is missing or duplicated: {name}")
    function = matches[0]
    lines = script.splitlines(keepends=True)
    return "".join(lines[function.lineno - 1 : function.end_lineno])


def properties_contract(text: str, label: str) -> dict[str, str]:
    values: dict[str, str] = {}
    for line_number, raw_line in enumerate(text.splitlines(), start=1):
        line = raw_line.strip()
        if not line or line.startswith(("#", "!")):
            continue
        key, separator, value = line.partition("=")
        require(
            bool(separator) and bool(key.strip()),
            f"{label}:{line_number} malformed property",
        )
        key = key.strip()
        require(key not in values, f"{label} contains duplicate property: {key}")
        values[key] = value.strip()
    return values


def workflow_steps(job: str) -> list[str]:
    starts = [match.start() for match in re.finditer(r"(?m)^      - ", job)]
    return [
        job[start : starts[index + 1] if index + 1 < len(starts) else len(job)].rstrip()
        for index, start in enumerate(starts)
    ]


def workflow_step_identities(job: str, label: str) -> list[str]:
    identities: list[str] = []
    for step in workflow_steps(job):
        first_line = step.splitlines()[0]
        if first_line.startswith("      - name: "):
            identities.append(first_line.removeprefix("      - name: "))
        elif first_line.startswith("      - uses: "):
            reference = first_line.removeprefix("      - uses: ").split(" #", 1)[0]
            identities.append(f"uses:{reference}")
        else:
            fail(f"{label} contains an unnamed or unsupported step")
    return identities


def workflow_named_step(job: str, name: str) -> str:
    marker = f"      - name: {name}"
    matches = [step for step in workflow_steps(job) if step.splitlines()[0] == marker]
    require(len(matches) == 1, f"workflow step must appear exactly once: {name}")
    return matches[0]


def workflow_run_commands(step: str, name: str) -> list[str]:
    marker = "\n        run: |\n"
    start = step.find(marker)
    require(start >= 0, f"workflow step has no run block: {name}")
    body = step[start + len(marker) :]
    commands: list[str] = []
    for line in body.splitlines():
        require(line.startswith("          "), f"workflow run indentation changed: {name}")
        if command := line[10:].strip():
            commands.append(command)
    return commands


def validate_tested_image_pair_flow(live_job: str, publish_job: str) -> None:
    require(
        workflow_step_identities(live_job, "live verification job")
        == [
            f"uses:actions/checkout@{ACTION_PINS['actions/checkout']}",
            "Verify image platform filter",
            f"uses:docker/setup-qemu-action@{ACTION_PINS['docker/setup-qemu-action']}",
            f"uses:docker/setup-buildx-action@{ACTION_PINS['docker/setup-buildx-action']}",
            "Build backend acceptance image",
            "Build web acceptance image",
            "Configure isolated demo",
            "Run reset-safe live acceptance",
            f"uses:actions/setup-node@{ACTION_PINS['actions/setup-node']}",
            "Run live browser contract",
            "Export tested images",
            "Upload tested images",
            "Clean isolated demo",
        ],
        "live verification step allowlist or ordering changed",
    )
    require(
        all(
            command not in live_job
            for command in ("docker pull ", "docker load", "docker tag ", "docker build ")
        ),
        "live verification gained an unapproved image mutation command",
    )
    require(
        workflow_step_identities(publish_job, "publish job")
        == [
            f"uses:actions/checkout@{ACTION_PINS['actions/checkout']}",
            "Download tested images",
            f"uses:docker/setup-buildx-action@{ACTION_PINS['docker/setup-buildx-action']}",
            "Lowercase repo owner",
            "Login to GHCR",
            "Load and publish exact tested pair",
            "Upload image digest handoff",
        ],
        "publish step allowlist or ordering changed",
    )

    export_step = workflow_named_step(live_job, "Export tested images")
    export_commands = workflow_run_commands(export_step, "Export tested images")
    require(
        export_commands
        == [
            f"docker save {TESTED_BACKEND_IMAGE} | gzip -1 > {TESTED_BACKEND_ARCHIVE}",
            f"docker save {TESTED_WEB_IMAGE} | gzip -1 > {TESTED_WEB_ARCHIVE}",
        ],
        "tested image export commands changed",
    )

    upload_step = workflow_named_step(live_job, "Upload tested images")
    require(
        f"uses: actions/upload-artifact@{ACTION_PINS['actions/upload-artifact']}" in upload_step,
        "tested image upload action changed",
    )
    require(
        "if: github.event_name == 'push'" in upload_step
        and f"name: {TESTED_ARTIFACT_NAME}" in upload_step
        and upload_step.count(TESTED_ARTIFACT_NAME) == 1,
        "tested image upload identity or push gate changed",
    )
    require(
        f"path: |\n            {TESTED_BACKEND_ARCHIVE}\n"
        f"            {TESTED_WEB_ARCHIVE}" in upload_step
        and upload_step.count(".tar.gz") == 2,
        "tested image upload pair changed",
    )

    download_step = workflow_named_step(publish_job, "Download tested images")
    require(
        (
            f"uses: actions/download-artifact@{ACTION_PINS['actions/download-artifact']}"
            in download_step
        )
        and f"name: {TESTED_ARTIFACT_NAME}" in download_step
        and f"path: {DOWNLOADED_ARTIFACT_DIR}" in download_step,
        "tested image download contract changed",
    )

    owner_step = workflow_named_step(publish_job, "Lowercase repo owner")
    require(
        workflow_run_commands(owner_step, "Lowercase repo owner")
        == [
            "echo \"OWNER=$(echo '${{ github.repository_owner }}' | "
            "tr '[:upper:]' '[:lower:]')\" >> \"$GITHUB_ENV\""
        ],
        "publish owner normalization command changed",
    )
    login_step = workflow_named_step(publish_job, "Login to GHCR")
    require(
        f"uses: docker/login-action@{ACTION_PINS['docker/login-action']}" in login_step
        and "registry: ghcr.io" in login_step
        and "username: ${{ github.actor }}" in login_step
        and "password: ${{ secrets.GITHUB_TOKEN }}" in login_step,
        "registry login contract changed",
    )

    validate_registry_digest_handoff(live_job, publish_job)


def validate_registry_digest_handoff(live_job: str, publish_job: str) -> None:
    publish_step = workflow_named_step(publish_job, "Load and publish exact tested pair")
    publish_commands = workflow_run_commands(
        publish_step,
        "Load and publish exact tested pair",
    )
    expected_image_mutations = [
        f"gzip -dc {DOWNLOADED_ARTIFACT_DIR}/simple-erp-backend.tar.gz | docker load",
        f"gzip -dc {DOWNLOADED_ARTIFACT_DIR}/simple-erp-web.tar.gz | docker load",
        f'docker tag {TESTED_BACKEND_IMAGE} "${{backend}}"',
        f'docker tag {TESTED_WEB_IMAGE} "${{web}}"',
        'docker push "${backend}"',
        'docker push "${web}"',
    ]
    image_mutation_pattern = re.compile(
        r"(?:^|\| )docker (?:load|tag|push|pull|build|import|image|create|commit)\b"
    )
    require(
        [
            command
            for command in publish_commands
            if image_mutation_pattern.search(command)
        ]
        == expected_image_mutations,
        "publish must load, tag, and push only the exact tested image pair",
    )
    require_ordered(
        "\n".join(publish_commands),
        "set -euo pipefail",
        f"gzip -dc {DOWNLOADED_ARTIFACT_DIR}/simple-erp-backend.tar.gz | docker load",
        f"gzip -dc {DOWNLOADED_ARTIFACT_DIR}/simple-erp-web.tar.gz | docker load",
        'tag="sha-${GITHUB_SHA}"',
        'backend_repository="ghcr.io/${OWNER}/simple-erp-backend"',
        'web_repository="ghcr.io/${OWNER}/simple-erp-web"',
        f'docker tag {TESTED_BACKEND_IMAGE} "${{backend}}"',
        f'docker tag {TESTED_WEB_IMAGE} "${{web}}"',
        'docker push "${backend}"',
        'docker push "${web}"',
        'docker buildx imagetools inspect "${backend}" --format',
        'docker buildx imagetools inspect "${web}" --format',
        'backend_ref="${backend_repository}@${backend_digest}"',
        'web_ref="${web_repository}@${web_digest}"',
        '> image-digests.json',
        '>> "${GITHUB_STEP_SUMMARY}"',
        label="tested ARM64 image digest handoff",
    )
    require(
        all(
            token in publish_step
            for token in (
                "jq -e -f scripts/demo/require-linux-arm64.jq",
                "--format '{{json .Image}}'",
                "--format '{{json .Manifest}}'",
                '^sha256:[0-9a-f]{64}$',
                "'{commit: $commit, platform: $platform, backend: $backend, web: $web}'",
                'echo "- Backend: \\`${backend_ref}\\`"',
                'echo "- Web: \\`${web_ref}\\`"',
            )
        )
        and publish_step.count("--format '{{json .Image}}'") == 2
        and publish_step.count("--format '{{json .Manifest}}'") == 2
        and "docker pull " not in publish_step
        and "docker build " not in publish_step,
        "publish must preserve the tested pair and prove linux/arm64 registry digests",
    )

    digest_upload = workflow_named_step(publish_job, "Upload image digest handoff")
    require(
        f"uses: actions/upload-artifact@{ACTION_PINS['actions/upload-artifact']}" in digest_upload
        and f"name: {DIGEST_ARTIFACT_NAME}" in digest_upload
        and "path: image-digests.json" in digest_upload
        and "if-no-files-found: error" in digest_upload,
        "registry digest handoff artifact contract changed",
    )

    filter_step = workflow_named_step(live_job, "Verify image platform filter")
    require(
        workflow_run_commands(filter_step, "Verify image platform filter")
        == ["bash scripts/demo/test-image-platform-filter.sh"],
        "image platform filter regression command changed",
    )


def validate_reset_script_text(reset_script: str) -> None:
    require(
        re.search(r'-v "\$\{DEMO_PROJECT_ROOT\}:[^"]+"', reset_script) is None,
        "reset script mounts the whole project root",
    )
    require(
        '${DEMO_PROJECT_ROOT}/scripts/demo/demo_control.py:/opt/demo-control.py:ro'
        in reset_script,
        "reset script lost the single-file read-only control mount",
    )
    require("--network none" in reset_script, "reset control container gained network access")
    require('"${DEMO_CONTROL_IMAGE}"' in reset_script, "reset control image pin is missing")
    candidate_generation_command = (
        "IFS= read -r generated_candidate < /proc/sys/kernel/random/uuid"
    )
    candidate_validation = (
        '[[ "${generated_candidate}" =~ ^[0-9a-f]{8}-[0-9a-f]{4}-'
        '[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$ ]]'
    )
    candidate_promotion = 'candidate_generation="${generated_candidate}"'
    require_ordered(
        reset_script,
        "trap 'on_failure $? $LINENO' ERR",
        candidate_generation_command,
        label="reset failure trap",
    )
    require_ordered(
        reset_script,
        candidate_generation_command,
        candidate_validation,
        candidate_promotion,
        label="reset candidate validation",
    )
    require_ordered(
        reset_script,
        'candidate_generation="${generated_candidate}"',
        'failure_stage="reset-schedule"',
        'next_reset_at="$(demo_next_reset_at)"',
        'failure_stage="resetting-state"',
        "\nwrite_resetting_lifecycle_state\n",
        'failure_stage="backend-stop"',
        "demo_compose stop -t 30 backend",
        'failure_stage="image-contract"',
        "demo_require_immutable_image_reference BACKEND_IMAGE",
        'failure_stage="preflight-credential"',
        'preflight_secret="$(demo_tool new-generation',
        'failure_stage="compose-contract"',
        "demo_compose config --quiet",
        'resolved_files_volume="$(docker volume inspect',
        'failure_stage="files-volume-ownership"',
        "demo_prepare_files_volume",
        'failure_stage="retention-pre-prune"',
        "demo_tool validate-bundle",
        label="early reset write lock",
    )
    resetting_body, _ = shell_function_body(
        reset_script, "write_resetting_lifecycle_state"
    )
    require(
        "demo_tool write-resetting-state" in resetting_body
        and "--seed-dir" not in resetting_body,
        "RESETTING publication must not validate the seed bundle",
    )
    significant_lines = [
        line.strip()
        for line in reset_script.splitlines()
        if line.strip() and not line.lstrip().startswith("#")
    ]
    schedule_index = significant_lines.index('next_reset_at="$(demo_next_reset_at)"')
    require(
        significant_lines[schedule_index + 1 : schedule_index + 3]
        == [
            'failure_stage="resetting-state"',
            "write_resetting_lifecycle_state",
        ],
        "reset write lock must immediately follow the resolved schedule",
    )
    write_lock_index = significant_lines.index("write_resetting_lifecycle_state")
    require(
        significant_lines[write_lock_index + 1 : write_lock_index + 3]
        == [
            'failure_stage="backend-stop"',
            "demo_compose stop -t 30 backend",
        ],
        "live backend must stop immediately after the reset write lock",
    )
    require(
        reset_script.count("demo_tool new-generation") == 1,
        "candidate generation must not depend on a pre-lock demo-tool container",
    )
    require(
        "if write_failed_lifecycle_state; then" in reset_script
        and "demo_compose stop -t 10 backend" in reset_script,
        "post-lock failure must publish FAILED and stop the backend",
    )
    require(
        "write_lifecycle_state VERIFYING preflight.json" in reset_script
        and "write_lifecycle_state READY preflight.json" not in reset_script,
        "candidate preflight must stay write-locked",
    )
    require_ordered(
        reset_script,
        'failure_stage="verifying-state"',
        'failure_stage="success-retention"',
        'failure_stage="ready-promotion"',
        label="successful generation retirement",
    )
    pre_prune_script = reset_script[: reset_script.find('pre_prune_completed="true"')]
    early_stages = set(
        re.findall(r'(?m)^failure_stage="([a-z][a-z0-9-]*)"$', pre_prune_script)
    )
    require(
        early_stages
        == {
            "bootstrap",
            "candidate-generation",
            "reset-schedule",
            "resetting-state",
            "backend-stop",
            "image-contract",
            "preflight-credential",
            "compose-contract",
            "files-volume-ownership",
            "retention-pre-prune",
        },
        "pre-prune failure stage contract changed",
    )

    require(
        reset_script.count("demo_prepare_files_volume") == 1,
        "files volume ownership initialization changed",
    )


def validate_files_volume_initializer(lib_script: str) -> None:
    initializer, _ = shell_function_body(lib_script, "demo_prepare_files_volume")
    for token in (
        "--pull never",
        "--network none",
        "--read-only",
        "--log-driver none",
        "--user 0:10001",
        "--cap-drop ALL",
        "--cap-add CHOWN",
        "--cap-add FOWNER",
        "--security-opt no-new-privileges:true",
        '-v "${DEMO_FILES_VOLUME}:/files"',
        '"${DEMO_CONTROL_IMAGE}"',
        '"0:0:755"',
        "find /files -mindepth 1 -maxdepth 1 -print -quit",
        "chown 10001:10001 /files",
        "chmod 2775 /files",
        '"10001:10001:2775"',
    ):
        require(token in initializer, f"files volume initializer changed: {token}")
    require(
        '"10001:10001:755"' not in initializer,
        "files volume initializer must reject an existing app-owned volume with an invalid mode",
    )


def validate_control_plane_failure_contract(control_script: str) -> None:
    pattern_match = re.search(
        r'CONTROL_PLANE_FAILURE_STAGE_PATTERN\s*=\s*re\.compile\(\s*r"([^"]+)"',
        control_script,
    )
    require(
        pattern_match is not None
        and pattern_match.group(1) == r"[a-z][a-z0-9]*(?:-[a-z0-9]+)*",
        "control-plane failure stage grammar changed",
    )
    failure_log_function = python_function_source(
        control_script, "write_control_plane_failure_log_file"
    )
    require(
        "CONTROL_PLANE_FAILURE_STAGE_PATTERN.fullmatch(stage)"
        in failure_log_function,
        "bounded failure log no longer validates its stage grammar",
    )
    failed_state_function = python_function_source(control_script, "write_failed_state")
    require(
        "if args.next_reset_at" in failed_state_function
        and "else None" in failed_state_function,
        "early FAILED publication must allow an unresolved reset schedule",
    )


def validate_reset_script_contract(project_root: Path) -> None:
    reset_script = (project_root / "scripts/demo/reset-demo.sh").read_text(
        encoding="utf-8"
    )
    control_script = (project_root / "scripts/demo/demo_control.py").read_text(
        encoding="utf-8"
    )
    lib_script = (project_root / "scripts/demo/lib.sh").read_text(encoding="utf-8")
    validate_reset_script_text(reset_script)
    validate_files_volume_initializer(lib_script)
    validate_control_plane_failure_contract(control_script)


def validate_acceptance_shell_flow(acceptance_script: str) -> None:
    finalize_body, finalize_end = shell_function_body(acceptance_script, "finalize_acceptance")
    main_script = acceptance_script[finalize_end:]
    trap_marker = "demo_install_acceptance_traps finalize_acceptance"
    runtime_script = main_script[main_script.find(trap_marker) :]
    require_ordered(
        runtime_script,
        trap_marker,
        '# Start from the canonical seed',
        '"${SCRIPT_DIR}/reset-demo.sh"',
        'cleanup_required="true"',
        'exercise_output="$(demo_compose run',
        "--phase exercise",
        'excel_customer_id="$(acceptance_field excelCustomer)"',
        'sales_contact_id="$(acceptance_field salesContact)"',
        'board_file_id="$(acceptance_field boardFile)"',
        'approval_file_id="$(acceptance_field approvalFile)"',
        'expense_file_id="$(acceptance_field expenseFile)"',
        'pending_file_id="$(acceptance_field pendingFile)"',
        'drive_file_id="$(acceptance_field driveFile)"',
        "capture_file_evidence board",
        "capture_file_evidence approval",
        "capture_file_evidence expense",
        "capture_file_evidence pending",
        "capture_file_evidence drive",
        "--phase delete-retained",
        '--board-file-id "${board_file_id}"',
        "assert_file_evidence board",
        "verify_acceptance_file_bodies",
        'exercise_succeeded="true"',
        "demo_compose restart backend",
        '--expected-state READY',
        "--phase verify-live",
        label="upload acceptance exercise, deletion, and restart verification",
    )
    live_phase_index = runtime_script.find("--phase verify-live")
    require(live_phase_index >= 0, "acceptance live verification phase is missing")
    require_ordered(
        runtime_script[live_phase_index:],
        "--phase verify-live",
        "assert_file_evidence board",
        "verify_acceptance_file_bodies",
        label="acceptance restart file evidence",
    )
    require_ordered(
        finalize_body,
        '"${SCRIPT_DIR}/reset-demo.sh"',
        'if [[ "${exercise_succeeded}" == "true" ]]; then',
        "--phase verify-reset",
        "assert_acceptance_file_rows_absent",
        "demo_tool assert-generation-absent",
        '--generation "${previous_generation}"',
        label="upload acceptance cleanup proof",
    )
    require(
        'if [[ "${cleanup_required}" == "true" ]]; then' in finalize_body,
        "acceptance cleanup reset lost its execution guard",
    )
    require(
        'local original_exit="$?"' in finalize_body
        and 'exit "${original_exit}"' in finalize_body,
        "acceptance cleanup must preserve the original success, failure, or signal status",
    )
    require(
        'source "${SCRIPT_DIR}/acceptance_traps.sh"' in acceptance_script,
        "acceptance signal handling helper is missing",
    )

    verify_live_tail = runtime_script[runtime_script.find("--phase verify-live") :]
    verify_reset_tail = finalize_body[finalize_body.find("--phase verify-reset") :]
    evidence_arguments = {
        "marker": "marker",
        "previous-generation": "previous_generation",
        "excel-customer-id": "excel_customer_id",
        "sales-contact-id": "sales_contact_id",
        "board-file-id": "board_file_id",
        "approval-file-id": "approval_file_id",
        "expense-file-id": "expense_file_id",
        "pending-file-id": "pending_file_id",
        "drive-file-id": "drive_file_id",
    }
    for argument, variable in evidence_arguments.items():
        evidence = f'--{argument} "${{{variable}}}"'
        require(
            evidence in verify_live_tail and evidence in verify_reset_tail,
            f"acceptance evidence must reach live and reset verification: {argument}",
        )
    for field in (
        "excelCustomer", "salesContact", "boardFile", "approvalFile",
        "expenseFile", "pendingFile", "driveFile",
    ):
        require(
            f"$(acceptance_field {field})" in runtime_script,
            f"acceptance exercise output is missing upload evidence: {field}",
        )


def validate_acceptance_upload_flows(control_script: str) -> None:
    upload_paths = (
        'customer_upload_path = "/api/v1/customers/excel/upload"',
        'contact_upload_path = "/api/v1/sales-contacts/excel/upload"',
        'generic_upload_path = "/api/v1/files"',
        'drive_upload_path = "/api/v1/drive/files"',
    )
    for upload_path in upload_paths:
        require(upload_path in control_script, f"upload acceptance path is missing: {upload_path}")

    boundary_function = python_function_source(
        control_script, "request_acceptance_attachment_at_size_boundary"
    )
    require_ordered(
        boundary_function,
        "payload = acceptance_attachment_payload(context.marker)",
        'payload=payload + b"x"',
        "expected_status=413",
        "require_error_response(oversized_error, 413, path)",
        "payload=payload,",
        label="30MiB upload boundary acceptance",
    )
    upload_function = python_function_source(control_script, "exercise_upload_acceptance")
    excel_upload_function = python_function_source(
        control_script, "exercise_excel_upload_acceptance"
    )
    pending_upload_function = python_function_source(
        control_script, "upload_pending_acceptance_file"
    )
    generic_upload_function = python_function_source(
        control_script, "exercise_generic_file_upload_acceptance"
    )
    drive_upload_function = python_function_source(
        control_script, "exercise_drive_file_upload_acceptance"
    )
    require_ordered(
        upload_function,
        "exercise_excel_upload_acceptance(context)",
        "exercise_generic_file_upload_acceptance(context)",
        "exercise_drive_file_upload_acceptance(context)",
        "return UploadAcceptanceResult(",
        label="file upload surface orchestration",
    )
    require_ordered(
        excel_upload_function,
        "customer_upload_path =",
        "contact_upload_path =",
        "return ExcelUploadAcceptanceResult(",
        label="Excel upload surfaces",
    )
    require_ordered(
        generic_upload_function,
        "generic_upload_path =",
        "request_acceptance_attachment_at_size_boundary(",
        "approval_file_id = upload_pending_acceptance_file(",
        "expense_file_id = upload_pending_acceptance_file(",
        "pending_file_id = upload_pending_acceptance_file(",
        "return GenericFileUploadAcceptanceResult(",
        label="generic PENDING upload surfaces",
    )
    require(
        'context, "approval", context.staff_token, context.staff_id'
        in generic_upload_function
        and 'context, "expense", context.staff_token, context.staff_id'
        in generic_upload_function
        and 'context, "pending", context.manager_token, context.manager_id'
        in generic_upload_function
        and 'path = "/api/v1/files"' in pending_upload_function
        and "request_file_upload(" in pending_upload_function
        and 'uploaded.get("uploaderId") != expected_uploader_id'
        in pending_upload_function,
        "generic PENDING uploader isolation changed",
    )
    require(
        'drive_upload_path = "/api/v1/drive/files"' in drive_upload_function
        and "atomic store-and-DRIVE_FILE claim" in drive_upload_function
        and "consume a generic PENDING upload" in drive_upload_function,
        "Drive acceptance must preserve its atomic store-and-claim endpoint",
    )


def validate_acceptance_owner_flows(control_script: str) -> None:
    board_function = python_function_source(control_script, "exercise_board_file_acceptance")
    approval_function = python_function_source(
        control_script, "exercise_approval_file_acceptance"
    )
    expense_function = python_function_source(
        control_script, "exercise_expense_file_acceptance"
    )
    require_ordered(
        board_function,
        'board_path = "/api/v1/boards"',
        '"attachmentFileIds": [upload.board_file_id]',
        "foreign_pending_error = request_json(",
        '"attachmentFileIds": [upload.pending_file_id]',
        "expected_status=400",
        label="board uploader isolation",
    )
    foreign_pending_tail = board_function[
        board_function.find("foreign_pending_error = request_json(") :
    ]
    require(
        'token=context.staff_token' in foreign_pending_tail,
        "board foreign uploader identity changed",
    )
    require_ordered(
        approval_function,
        'create_path = "/api/v1/approvals"',
        '"attachmentFileIds": [upload.approval_file_id]',
        "reused_error = request_json(",
        '"attachmentFileIds": [upload.board_file_id]',
        "require_hidden_download(",
        label="approval owner isolation",
    )
    require(
        'f"{detail_path}/attachments/{upload.board_file_id}"' in approval_function
        and (
            'f"/api/v1/boards/{board.board_id}/attachments/'
            '{upload.approval_file_id}"' in approval_function
        ),
        "approval cross-owner downloads changed",
    )
    require_ordered(
        expense_function,
        'create_path = "/api/v1/expenses"',
        '"receiptFileId": upload.expense_file_id',
        "reused_error = request_json(",
        '"receiptFileId": upload.board_file_id',
        "require_hidden_download(",
        "verify_expense_approval_acceptance(",
        label="expense owner isolation",
    )
    require(
        'f"{detail_path}/receipts/{upload.board_file_id}"' in expense_function,
        "expense cross-owner download changed",
    )
    require(
        sum(
            function.count(
                'expected_message="업로드한 본인의 미사용 파일만 연결할 수 있습니다."'
            )
            for function in (board_function, approval_function, expense_function)
        )
        == 3,
        "file claim rejection paths changed",
    )


def validate_acceptance_storage_lifecycle(acceptance_script: str) -> None:
    for function_name in (
        "capture_file_evidence",
        "assert_file_evidence",
        "verify_acceptance_file_bodies",
        "assert_acceptance_file_rows_absent",
    ):
        shell_function_body(acceptance_script, function_name)
    require(
        (
            "SELECT id, stored_name, DATE_FORMAT(created_at, '%Y/%m'), status, "
            "COALESCE(owner_type, 'NULL'), COALESCE(owner_id, 0), "
            "COALESCE(uploader_id, 0), size FROM stored_files"
        )
        in acceptance_script,
        "stored file evidence query fields changed",
    )
    require(
        all(
            token in acceptance_script
            for token in (
                "stored_name", "status", "owner_type", "owner_id", "uploader_id",
                "CLAIMED BOARD_POST", "CLAIMED APPROVAL_DOCUMENT",
                "CLAIMED EXPENSE_CLAIM", "PENDING NULL 0", "CLAIMED DRIVE_FILE",
                "DELETE_PENDING BOARD_POST", "DELETE_PENDING DRIVE_FILE",
                "verify-acceptance-file", "assert_acceptance_file_rows_absent",
            )
        ),
        "stored file lifecycle evidence contract changed",
    )


def validate_acceptance_script_text(acceptance_script: str, control_script: str) -> None:
    validate_acceptance_shell_flow(acceptance_script)
    validate_acceptance_upload_flows(control_script)
    validate_acceptance_owner_flows(control_script)
    validate_acceptance_storage_lifecycle(acceptance_script)


def validate_acceptance_signal_helper(helper_script: str) -> None:
    require_ordered(
        helper_script,
        "demo_acceptance_signal()",
        'exit "$1"',
        "demo_install_acceptance_traps()",
        'trap "${finalize_function}" EXIT',
        "trap 'demo_acceptance_signal 130' INT",
        "trap 'demo_acceptance_signal 143' TERM",
        label="acceptance signal handling",
    )


def validate_acceptance_script_contract(project_root: Path) -> None:
    validate_acceptance_script_text(
        (project_root / "scripts/demo/acceptance-demo.sh").read_text(encoding="utf-8"),
        (project_root / "scripts/demo/demo_control.py").read_text(encoding="utf-8"),
    )
    validate_acceptance_signal_helper(
        (project_root / "scripts/demo/acceptance_traps.sh").read_text(encoding="utf-8")
    )


def validate_upload_size_texts(
    application_properties: str,
    demo_properties: str,
    caddyfile: str,
) -> None:
    base = properties_contract(application_properties, "application.properties")
    demo = properties_contract(demo_properties, "application-demo.properties")
    effective = base | demo
    require(
        effective.get("spring.servlet.multipart.max-file-size") == "30MB",
        "demo multipart file limit must stay 30MB",
    )
    require(
        demo.get("spring.servlet.multipart.max-request-size") == "32MB"
        and effective.get("spring.servlet.multipart.max-request-size") == "32MB",
        "demo multipart request limit must reserve 32MB for multipart overhead",
    )
    require(
        caddyfile.count("max_size {$API_REQUEST_BODY_MAX_SIZE:32MB}") == 1,
        "Caddy upload body limit must stay 32MB",
    )
    require(
        caddyfile.count("max_size {$API_JSON_REQUEST_BODY_MAX_SIZE:1MB}") == 1,
        "Caddy non-upload API body limit must stay 1MB",
    )
    upload_matcher = caddyfile.partition("@uploadApi {")[2].partition("handle @uploadApi")[0]
    require(
        "method POST" in upload_matcher
        and all(
            path in upload_matcher
            for path in (
                "/api/v1/files",
                "/api/v1/drive/files",
                "/api/v1/customers/excel/upload",
                "/api/v1/sales-contacts/excel/upload",
            )
        ),
        "Caddy 32MB boundary must be limited to the four exact POST upload paths",
    )
    require(
        caddyfile.find("handle @uploadApi")
        < caddyfile.find("handle /api/*")
        < caddyfile.find("max_size {$API_JSON_REQUEST_BODY_MAX_SIZE:1MB}"),
        "Caddy exact upload route must precede the generic 1MB API route",
    )


def validate_caddy_internal_probe_texts(
    caddyfile: str,
    dockerfile: str,
    compose_overlay: str,
    reset_script: str,
    smoke_script: str,
    acceptance_script: str,
    control_script: str,
) -> None:
    require(
        caddyfile.count("(simple_erp_routes) {") == 1
        and caddyfile.count("import simple_erp_routes") == 2,
        "Caddy public and internal sites must share one route contract",
    )
    status_matcher = caddyfile.partition("@demoStatus {")[2].partition("handle @demoStatus")[0]
    require(
        "method GET HEAD" in status_matcher
        and "path /api/v1/demo/status" in status_matcher,
        "Caddy static demo status must be limited to GET and HEAD",
    )
    require(
        caddyfile.count("reverse_proxy backend:8080 {") == 3
        and caddyfile.count("header_up X-Forwarded-For {remote_host}") == 3,
        "every Caddy backend proxy must overwrite X-Forwarded-For with its peer address",
    )
    shared_routes = caddyfile.partition("(simple_erp_routes) {")[2].partition(
        "# 공개 진입점"
    )[0]
    public_site = caddyfile.partition("{$SITE_ADDRESS::80} {")[2].partition("\n:8080 {")[0]
    internal_site = caddyfile.partition("\n:8080 {")[2]
    require(
        "/actuator" not in shared_routes
        and "@publicActuator path /actuator /actuator/*" in public_site
        and "handle @publicActuator" in public_site
        and "respond 404" in public_site
        and "@internalHealth path /actuator/health /actuator/health/*" in internal_site
        and "handle @internalHealth" in internal_site,
        "Caddy actuator health must be 404 publicly and proxied only on the internal site",
    )
    require(
        caddyfile.count("\n:8080 {\n") == 1,
        "Caddy internal probe site must stay on container-only port 8080",
    )
    require(
        caddyfile.count('header Strict-Transport-Security "max-age=31536000"') == 1,
        "Caddy public site must keep the one-year HSTS boundary",
    )
    require(
        "EXPOSE 80 443 8080" in dockerfile
        and "http://localhost:8080/" in dockerfile,
        "web healthcheck must use the internal Caddy probe port",
    )
    require(
        "DEMO_HTTP_PORT:-80}:80" in compose_overlay
        and "DEMO_HTTPS_PORT:-443}:443" in compose_overlay
        and not re.search(
            r"(?m)^\s*-\s*[\"']?[^#\r\n]*:8080(?::8080)?[\"']?\s*$",
            compose_overlay,
        ),
        "Caddy internal probe port must not be published by Compose",
    )
    require(
        "--base-url http://web:8080" in reset_script
        and reset_script.count("--base-url http://web:8080") == 1,
        "reset live smoke must use the internal Caddy probe port",
    )
    require(
        '"${base_url}" == "http://web:8080"' in smoke_script,
        "smoke base URL allowlist must pin the internal Caddy probe port",
    )
    require(
        acceptance_script.count("--base-url http://web:8080") == 5,
        "all acceptance phases must use the internal Caddy probe port",
    )
    require(
        'if value != "http://web:8080":' in control_script,
        "acceptance control must pin the internal Caddy probe port",
    )


def validate_caddy_internal_probe_contract(project_root: Path) -> None:
    validate_caddy_internal_probe_texts(
        (project_root / "frontend/Caddyfile").read_text(encoding="utf-8"),
        (project_root / "frontend/Dockerfile").read_text(encoding="utf-8"),
        (project_root / "compose.demo.yml").read_text(encoding="utf-8"),
        (project_root / "scripts/demo/reset-demo.sh").read_text(encoding="utf-8"),
        (project_root / "scripts/demo/smoke-demo.sh").read_text(encoding="utf-8"),
        (project_root / "scripts/demo/acceptance-demo.sh").read_text(encoding="utf-8"),
        (project_root / "scripts/demo/demo_control.py").read_text(encoding="utf-8"),
    )


def validate_upload_size_contract(project_root: Path) -> None:
    validate_upload_size_texts(
        (project_root / "backend/src/main/resources/application.properties").read_text(
            encoding="utf-8"
        ),
        (project_root / "backend/src/main/resources/application-demo.properties").read_text(
            encoding="utf-8"
        ),
        (project_root / "frontend/Caddyfile").read_text(encoding="utf-8"),
    )


def validate_action_pins(workflow: str) -> None:
    references = re.findall(r"(?m)^\s+(?:-\s+)?uses:\s+([^\s#]+)", workflow)
    require(references, "workflow actions are missing")
    for reference in references:
        if reference.startswith("./.github/workflows/"):
            require(
                re.fullmatch(
                    r"\./\.github/workflows/[A-Za-z0-9_.-]+\.ya?ml",
                    reference,
                )
                is not None,
                f"local workflow reference is invalid: {reference}",
            )
            continue
        owner_action, separator, revision = reference.partition("@")
        require(
            separator == "@"
            and re.fullmatch(r"[0-9a-f]{40}", revision) is not None,
            f"workflow action is not pinned to a full commit: {reference}",
        )
        approved_revision = ACTION_PINS.get(owner_action)
        require(
            approved_revision is None or revision == approved_revision,
            f"workflow action is not pinned to the approved commit: {reference}",
        )


def validate_all_workflow_action_pins(project_root: Path) -> None:
    workflow_paths = sorted(
        {
            *project_root.glob(".github/workflows/*.yml"),
            *project_root.glob(".github/workflows/*.yaml"),
        }
    )
    require(workflow_paths, "GitHub workflows are missing")
    for workflow_path in workflow_paths:
        validate_action_pins(workflow_path.read_text(encoding="utf-8"))


def validate_build_workflow_trigger(workflow: str) -> None:
    trigger = "\n  push:\n    branches: [master, demo]\n  pull_request:\n"
    require(
        workflow.count(trigger) == 1,
        "build workflow must verify and publish master and demo pushes",
    )


def validate_build_workflow_contract(project_root: Path) -> None:
    workflow = (project_root / ".github/workflows/build-and-push.yml").read_text(
        encoding="utf-8"
    )
    validate_build_workflow_trigger(workflow)
    validate_all_workflow_action_pins(project_root)
    platform_filter = (
        project_root / "scripts/demo/require-linux-arm64.jq"
    ).read_text(encoding="utf-8")
    normalized_filter = " ".join(platform_filter.split())
    require(
        normalized_filter
        == (
            'if type != "object" then empty '
            'elif has("os") or has("architecture") then '
            'select(.os == "linux" and .architecture == "arm64") '
            'else .["linux/arm64"]? | select(type == "object" and '
            '.os == "linux" and .architecture == "arm64") end'
        ),
        "linux/arm64 imagetools single/map filter changed",
    )
    platform_fixture = json.loads(
        (project_root / "scripts/demo/fixtures/imagetools-image-map.json").read_text(
            encoding="utf-8"
        )
    )
    require(
        isinstance(platform_fixture, dict)
        and isinstance(platform_fixture.get("linux/arm64"), dict)
        and platform_fixture["linux/arm64"].get("os") == "linux"
        and platform_fixture["linux/arm64"].get("architecture") == "arm64",
        "imagetools image-map regression fixture changed",
    )
    single_fixture = json.loads(
        (project_root / "scripts/demo/fixtures/imagetools-image-single.json").read_text(
            encoding="utf-8"
        )
    )
    require(
        isinstance(single_fixture, dict)
        and single_fixture.get("os") == "linux"
        and single_fixture.get("architecture") == "arm64",
        "imagetools single-image regression fixture changed",
    )
    require(
        "group: ${{ github.workflow }}-${{ github.ref }}" in workflow,
        "build workflow concurrency key changed",
    )
    require(workflow.count("cancel-in-progress: true") == 1, "build cancellation contract changed")
    require("\n  publish:\n" in workflow, "publish job is missing")
    verify_jobs, publish_job = workflow.split("\n  publish:\n", maxsplit=1)
    require("\n  verify-live-demo:\n" in verify_jobs, "live demo verification job is missing")
    live_job = verify_jobs.split("\n  verify-live-demo:\n", maxsplit=1)[1]
    require(
        "\n          cache: " not in verify_jobs,
        "verification jobs may not use build cache actions",
    )
    require("packages: write" not in verify_jobs, "verification jobs gained package write access")
    require(live_job.count("platforms: linux/arm64") == 2, "arm64 live image builds changed")
    require(live_job.count("load: true") == 2, "live acceptance must load both images")
    for required in (
        "timeout-minutes: 40",
        'echo "DEMO_SMOKE_TIMEOUT_SECONDS=420"',
        "bash scripts/demo/acceptance-demo.sh",
    ):
        require(required in live_job, f"live demo workflow is missing: {required}")
    require("if: github.event_name == 'push'" in publish_job, "publish push gate changed")
    require(publish_job.count("packages: write") == 1, "publish permission contract changed")
    validate_tested_image_pair_flow(live_job, publish_job)
    require("value=latest" not in workflow, "workflow must not publish a mutable latest tag")


def validate_control_image_contract(project_root: Path) -> None:
    lib_script = (project_root / "scripts/demo/lib.sh").read_text(encoding="utf-8")
    image_match = re.search(
        r'^readonly DEMO_CONTROL_IMAGE="([^"]+)"$', lib_script, flags=re.MULTILINE
    )
    require(
        image_match is not None and image_match.group(1) == CONTROL_IMAGE,
        "shell and Compose control image pins diverged",
    )


def validate_demo_contract(project_root: Path, config: dict[str, object]) -> None:
    root = project_root.resolve()
    services = validate_service_contracts(config)
    validate_service_mount_contracts(root, services)
    validate_tool_mount_contracts(root, services)
    validate_reset_script_contract(root)
    validate_stored_file_mapping_queries(
        (root / "scripts/demo/reset-demo.sh").read_text(encoding="utf-8"),
        (root / ".github/workflows/demo-seed.yml").read_text(encoding="utf-8"),
    )
    validate_acceptance_script_contract(root)
    validate_upload_size_contract(root)
    validate_caddy_internal_probe_contract(root)
    validate_build_workflow_contract(root)
    validate_control_image_contract(root)


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--compose-config", type=Path, required=True)
    parser.add_argument("--project-root", type=Path, default=Path.cwd())
    return parser


def main() -> int:
    args = build_parser().parse_args()
    try:
        validate_demo_contract(
            args.project_root,
            load_compose_config(args.compose_config),
        )
    except (ContractViolation, OSError) as error:
        print(f"ERROR: {error}", file=sys.stderr)
        return 1
    print("demo-static-contract-ok")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
