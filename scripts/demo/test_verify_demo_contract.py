#!/usr/bin/env python3
"""Unit tests for the static demo deployment contract verifier."""

from __future__ import annotations

import importlib.util
from copy import deepcopy
import json
from pathlib import Path
import sys
import unittest


ROOT = Path(__file__).resolve().parents[2]


def load_verifier():
    path = ROOT / "scripts/demo/verify_demo_contract.py"
    spec = importlib.util.spec_from_file_location("verify_demo_contract_under_test", path)
    if spec is None or spec.loader is None:
        raise RuntimeError(f"cannot load verifier: {path}")
    module = importlib.util.module_from_spec(spec)
    sys.modules[spec.name] = module
    spec.loader.exec_module(module)
    return module


verifier = load_verifier()


def service_mount_fixture() -> dict[str, dict[str, object]]:
    state_source = str(ROOT / "runtime/state")
    return {
        "backend": {
            "volumes": [
                {
                    "type": "bind",
                    "source": state_source,
                    "target": "/app/data/demo-state",
                    "read_only": True,
                },
                {
                    "type": "volume",
                    "source": "demo_files",
                    "target": "/app/data/files",
                },
            ]
        },
        "web": {
            "volumes": [
                {
                    "type": "bind",
                    "source": state_source,
                    "target": "/srv/demo",
                    "read_only": True,
                },
                {"type": "volume", "source": "demo_caddy_data", "target": "/data"},
                {
                    "type": "volume",
                    "source": "demo_caddy_config",
                    "target": "/config",
                },
            ]
        },
    }


def build_workflow_jobs() -> tuple[str, str]:
    workflow = (ROOT / ".github/workflows/build-and-push.yml").read_text(
        encoding="utf-8"
    )
    verify_jobs, publish_job = workflow.split("\n  publish:\n", maxsplit=1)
    live_job = verify_jobs.split("\n  verify-live-demo:\n", maxsplit=1)[1]
    return live_job, publish_job


class StaticContractVerifierTest(unittest.TestCase):
    def test_stored_file_mapping_queries_share_exact_column_contract(self) -> None:
        verifier.validate_stored_file_mapping_queries(
            (ROOT / "scripts/demo/reset-demo.sh").read_text(encoding="utf-8"),
            (ROOT / ".github/workflows/demo-seed.yml").read_text(encoding="utf-8"),
        )

    def test_stored_file_mapping_query_rejects_missing_owner_column(self) -> None:
        reset = (ROOT / "scripts/demo/reset-demo.sh").read_text(encoding="utf-8")
        workflow = (ROOT / ".github/workflows/demo-seed.yml").read_text(encoding="utf-8")
        with self.assertRaisesRegex(
            verifier.ContractViolation, "seed workflow stored file mapping query changed"
        ):
            verifier.validate_stored_file_mapping_queries(
                reset, workflow.replace("status, owner_type, owner_id, uploader_id", "status")
            )

    def test_stored_file_mapping_fixture_requires_non_privileged_read_contract(self) -> None:
        reset = (ROOT / "scripts/demo/reset-demo.sh").read_text(encoding="utf-8")
        workflow = (ROOT / ".github/workflows/demo-seed.yml").read_text(encoding="utf-8")
        verifier.validate_stored_file_mapping_queries(reset, workflow)
        with self.assertRaisesRegex(
            verifier.ContractViolation, "bind fixture permissions"
        ):
            verifier.validate_stored_file_mapping_queries(
                reset,
                workflow.replace(
                    "chmod 0644 runtime/work/ci/stored-files.tsv",
                    "chmod 0600 runtime/work/ci/stored-files.tsv",
                    1,
                ),
            )

    def test_bind_contract_normalizes_sources_and_preserves_access_mode(self) -> None:
        source = ROOT / "scripts/demo/demo_control.py"
        volumes = [
            {
                "type": "bind",
                "source": str(source.parent / "." / source.name),
                "target": "/opt/demo-control.py",
                "read_only": True,
            },
            {"type": "volume", "source": "demo_files", "target": "/files"},
        ]

        self.assertEqual(
            verifier.bind_contract(volumes),
            {(source.resolve(), "/opt/demo-control.py", True)},
        )
        self.assertEqual(
            verifier.named_volume_contract(volumes),
            {("demo_files", "/files", False)},
        )

    def test_require_fails_closed_with_named_contract(self) -> None:
        with self.assertRaisesRegex(verifier.ContractViolation, "network allowlist"):
            verifier.require(False, "network allowlist changed")

    def test_web_resolved_ports_publish_only_container_targets_80_and_443(self) -> None:
        ports = [
            {
                "mode": "ingress",
                "host_ip": "0.0.0.0",
                "target": 80,
                "published": "80",
                "protocol": "tcp",
            },
            {
                "mode": "ingress",
                "host_ip": "0.0.0.0",
                "target": 443,
                "published": "443",
                "protocol": "tcp",
            },
        ]
        verifier.validate_web_public_ports(ports)

        ci_override = deepcopy(ports)
        ci_override[0]["host_ip"] = "127.0.0.1"
        ci_override[0]["published"] = "18080"
        ci_override[1]["host_ip"] = "127.0.0.1"
        ci_override[1]["published"] = "18443"
        verifier.validate_web_public_ports(ci_override)

        with self.assertRaisesRegex(
            verifier.ContractViolation,
            "exactly container TCP targets 80 and 443",
        ):
            verifier.validate_web_public_ports(
                ports
                + [
                    {
                        "mode": "ingress",
                        "host_ip": "0.0.0.0",
                        "target": 8080,
                        "published": "8080",
                        "protocol": "tcp",
                    }
                ]
            )

    def test_public_resource_limits_are_exact_and_fail_closed(self) -> None:
        verifier.validate_public_resource_limits(dict(verifier.PUBLIC_RESOURCE_LIMITS))
        weakened = dict(verifier.PUBLIC_RESOURCE_LIMITS)
        weakened["DEMO_UPLOAD_GENERATION_QUOTA_BYTES"] = "1073741824"
        with self.assertRaisesRegex(
            verifier.ContractViolation,
            "resource and cost envelope",
        ):
            verifier.validate_public_resource_limits(weakened)

    def test_demo_transaction_isolation_is_read_committed(self) -> None:
        verifier.validate_demo_transaction_isolation(
            {
                "SPRING_DATASOURCE_HIKARI_TRANSACTION_ISOLATION":
                    "TRANSACTION_READ_COMMITTED"
            }
        )
        with self.assertRaisesRegex(
            verifier.ContractViolation,
            "transaction isolation must stay READ_COMMITTED",
        ):
            verifier.validate_demo_transaction_isolation(
                {
                    "SPRING_DATASOURCE_HIKARI_TRANSACTION_ISOLATION":
                        "TRANSACTION_REPEATABLE_READ"
                }
            )

    def test_root_credential_isolation_rejects_disguised_environment_key(self) -> None:
        with self.assertRaisesRegex(verifier.ContractViolation, "leaked to backend"):
            verifier.validate_root_credential_isolation(
                {"MARIADB_ROOT_PASSWORD": "root-secret"},
                {"backend": {"LEAKED_ROOT": "root-secret"}},
            )

    def test_root_credential_isolation_rejects_embedded_value(self) -> None:
        with self.assertRaisesRegex(verifier.ContractViolation, "leaked to backend"):
            verifier.validate_root_credential_isolation(
                {"MARIADB_ROOT_PASSWORD": "root-secret"},
                {
                    "backend": {
                        "ROOT_DSN": "mariadb://root:root-secret@db/simple_erp_demo"
                    }
                },
            )

    def test_service_allowlist_rejects_extra_sidecar(self) -> None:
        services = {name: {} for name in verifier.DEMO_SERVICE_NAMES}
        services["credential-sidecar"] = {}

        with self.assertRaisesRegex(verifier.ContractViolation, "service allowlist"):
            verifier.select_demo_services({"services": services})

    def test_require_ordered_rejects_reordered_reset_gates(self) -> None:
        with self.assertRaisesRegex(verifier.ContractViolation, "candidate gates ordering"):
            verifier.require_ordered(
                "promote validate",
                "validate",
                "promote",
                label="candidate gates",
            )

    def test_bind_contract_rejects_ambiguous_bind(self) -> None:
        with self.assertRaisesRegex(verifier.ContractViolation, "string source and target"):
            verifier.bind_contract(
                [{"type": "bind", "source": None, "target": "/opt/demo-control.py"}]
            )

    def test_tool_mount_contract_rejects_project_root_bind(self) -> None:
        shared = [
            {
                "type": "bind",
                "source": str(ROOT / "scripts/demo/demo_control.py"),
                "target": "/opt/demo-control.py",
                "read_only": True,
            },
            {
                "type": "bind",
                "source": str(ROOT / "demo/seed"),
                "target": "/seed",
                "read_only": True,
            },
        ]
        services = {
            "demo-tool": {
                "volumes": shared
                + [
                    {
                        "type": "bind",
                        "source": str(ROOT / "runtime/state"),
                        "target": "/state",
                    },
                    {
                        "type": "bind",
                        "source": str(ROOT / "runtime/work"),
                        "target": "/work",
                    },
                    {
                        "type": "bind",
                        "source": str(ROOT / "runtime/logs"),
                        "target": "/logs",
                    },
                    {
                        "type": "bind",
                        "source": str(ROOT),
                        "target": "/project",
                        "read_only": True,
                    },
                    {"type": "volume", "source": "demo_files", "target": "/files"},
                ]
            },
            "demo-tool-smoke": {"volumes": shared},
        }

        with self.assertRaisesRegex(verifier.ContractViolation, "project-root mounts"):
            verifier.validate_tool_mount_contracts(ROOT, services)

    def test_service_mount_contract_rejects_writable_control_state(self) -> None:
        for service_name in ("backend", "web"):
            with self.subTest(service=service_name):
                services = deepcopy(service_mount_fixture())
                volumes = services[service_name]["volumes"]
                bind = next(volume for volume in volumes if volume["type"] == "bind")
                bind["read_only"] = False

                with self.assertRaisesRegex(
                    verifier.ContractViolation,
                    f"{service_name} state mount must stay read-only",
                ):
                    verifier.validate_service_mount_contracts(ROOT, services)

    def test_service_mount_contract_accepts_expected_mounts(self) -> None:
        verifier.validate_service_mount_contracts(ROOT, service_mount_fixture())

    def test_service_mount_contract_rejects_read_only_upload_volume(self) -> None:
        services = service_mount_fixture()
        backend_volume = next(
            volume
            for volume in services["backend"]["volumes"]
            if volume["type"] == "volume"
        )
        backend_volume["read_only"] = True

        with self.assertRaisesRegex(
            verifier.ContractViolation,
            "backend demo files mount must stay writable",
        ):
            verifier.validate_service_mount_contracts(ROOT, services)

    def test_reset_contract_locks_immediately_after_schedule_resolution(self) -> None:
        reset_script = (ROOT / "scripts/demo/reset-demo.sh").read_text(encoding="utf-8")

        verifier.validate_reset_script_text(reset_script)
        delayed = reset_script.replace(
            'next_reset_at="$(demo_next_reset_at)"',
            'next_reset_at="$(demo_next_reset_at)"\n'
            'reset_started_at="$(date --iso-8601=seconds)"',
            1,
        )
        with self.assertRaisesRegex(
            verifier.ContractViolation,
            "write lock must immediately follow",
        ):
            verifier.validate_reset_script_text(delayed)

    def test_resetting_publication_cannot_reintroduce_bundle_validation(self) -> None:
        reset_script = (ROOT / "scripts/demo/reset-demo.sh").read_text(encoding="utf-8")
        weakened = reset_script.replace(
            "  demo_tool write-resetting-state \\\n    --state-dir /state",
            "  demo_tool write-resetting-state \\\n    --seed-dir /seed \\\n    --state-dir /state",
            1,
        )

        with self.assertRaisesRegex(
            verifier.ContractViolation,
            "must not validate the seed bundle",
        ):
            verifier.validate_reset_script_text(weakened)

    def test_reset_contract_rejects_live_backend_overlap_with_preflight(self) -> None:
        reset_script = (ROOT / "scripts/demo/reset-demo.sh").read_text(encoding="utf-8")
        delayed_stop = reset_script.replace(
            'failure_stage="backend-stop"\n'
            "demo_compose stop -t 30 backend\n",
            "",
            1,
        ).replace(
            'failure_stage="candidate-app-preflight"',
            'failure_stage="backend-stop"\n'
            "demo_compose stop -t 30 backend\n\n"
            'failure_stage="candidate-app-preflight"',
            1,
        )

        with self.assertRaisesRegex(verifier.ContractViolation, "early reset write lock"):
            verifier.validate_reset_script_text(delayed_stop)

        delayed_after_lock = reset_script.replace(
            'failure_stage="backend-stop"',
            'reset_started_at="$(date --iso-8601=seconds)"\n'
            'failure_stage="backend-stop"',
            1,
        )
        with self.assertRaisesRegex(
            verifier.ContractViolation,
            "stop immediately after the reset write lock",
        ):
            verifier.validate_reset_script_text(delayed_after_lock)

    def test_files_volume_initializer_rejects_broader_privileges(self) -> None:
        lib_script = (ROOT / "scripts/demo/lib.sh").read_text(encoding="utf-8")
        verifier.validate_files_volume_initializer(lib_script)

        for weakened in (
            lib_script.replace("--cap-drop ALL", "", 1),
            lib_script.replace("--network none", "", 1),
            lib_script.replace('"0:0:755"', '"0:0:777"', 1),
            lib_script.replace(
                'if [ "${owner}:${mode}" = "0:0:755" ]; then',
                'if [ "${owner}:${mode}" = "0:0:755" ] '
                '|| [ "${owner}:${mode}" = "10001:10001:755" ]; then',
                1,
            ),
            lib_script.replace(
                "find /files -mindepth 1 -maxdepth 1 -print -quit",
                "true",
                1,
            ),
        ):
            with self.subTest():
                with self.assertRaises(verifier.ContractViolation):
                    verifier.validate_files_volume_initializer(weakened)

    def test_control_failure_contract_accepts_every_early_stage(self) -> None:
        control = (ROOT / "scripts/demo/demo_control.py").read_text(encoding="utf-8")

        verifier.validate_control_plane_failure_contract(control)
        narrowed = control.replace(
            r"[a-z][a-z0-9]*(?:-[a-z0-9]+)*",
            r"(?:bootstrap|compose-contract|retention-pre-prune)",
            1,
        )
        with self.assertRaisesRegex(
            verifier.ContractViolation,
            "failure stage grammar changed",
        ):
            verifier.validate_control_plane_failure_contract(narrowed)

    def test_upload_size_contract_reserves_multipart_request_overhead(self) -> None:
        application = (
            ROOT / "backend/src/main/resources/application.properties"
        ).read_text(encoding="utf-8")
        demo = (
            ROOT / "backend/src/main/resources/application-demo.properties"
        ).read_text(encoding="utf-8")
        caddyfile = (ROOT / "frontend/Caddyfile").read_text(encoding="utf-8")

        verifier.validate_upload_size_texts(application, demo, caddyfile)
        with self.assertRaisesRegex(
            verifier.ContractViolation,
            "reserve 32MB for multipart overhead",
        ):
            verifier.validate_upload_size_texts(
                application,
                demo.replace(
                    "spring.servlet.multipart.max-request-size=32MB",
                    "spring.servlet.multipart.max-request-size=30MB",
                ),
                caddyfile,
            )
        with self.assertRaisesRegex(
            verifier.ContractViolation,
            "non-upload API body limit",
        ):
            verifier.validate_upload_size_texts(
                application,
                demo,
                caddyfile.replace(
                    "max_size {$API_JSON_REQUEST_BODY_MAX_SIZE:1MB}",
                    "max_size {$API_JSON_REQUEST_BODY_MAX_SIZE:32MB}",
                ),
            )
        with self.assertRaisesRegex(
            verifier.ContractViolation,
            "four exact POST upload paths",
        ):
            verifier.validate_upload_size_texts(
                application,
                demo,
                caddyfile.replace("\t\tmethod POST\n", "", 1),
            )

    def test_caddy_internal_probe_is_shared_but_not_published(self) -> None:
        caddyfile = (ROOT / "frontend/Caddyfile").read_text(encoding="utf-8")
        dockerfile = (ROOT / "frontend/Dockerfile").read_text(encoding="utf-8")
        compose_overlay = (ROOT / "compose.demo.yml").read_text(encoding="utf-8")
        reset_script = (ROOT / "scripts/demo/reset-demo.sh").read_text(encoding="utf-8")
        smoke_script = (ROOT / "scripts/demo/smoke-demo.sh").read_text(encoding="utf-8")
        acceptance_script = (ROOT / "scripts/demo/acceptance-demo.sh").read_text(
            encoding="utf-8"
        )
        control_script = (ROOT / "scripts/demo/demo_control.py").read_text(
            encoding="utf-8"
        )

        verifier.validate_caddy_internal_probe_texts(
            caddyfile,
            dockerfile,
            compose_overlay,
            reset_script,
            smoke_script,
            acceptance_script,
            control_script,
        )
        with self.assertRaisesRegex(
            verifier.ContractViolation,
            "HSTS",
        ):
            verifier.validate_caddy_internal_probe_texts(
                caddyfile.replace(
                    'header Strict-Transport-Security "max-age=31536000"',
                    "",
                ),
                dockerfile,
                compose_overlay,
                reset_script,
                smoke_script,
                acceptance_script,
                control_script,
            )
        with self.assertRaisesRegex(
            verifier.ContractViolation,
            "overwrite X-Forwarded-For",
        ):
            verifier.validate_caddy_internal_probe_texts(
                caddyfile.replace("\t\t\theader_up X-Forwarded-For {remote_host}\n", "", 1),
                dockerfile,
                compose_overlay,
                reset_script,
                smoke_script,
                acceptance_script,
                control_script,
            )
        with self.assertRaisesRegex(
            verifier.ContractViolation,
            "actuator health must be 404 publicly",
        ):
            verifier.validate_caddy_internal_probe_texts(
                caddyfile.replace("\t\trespond 404\n", "\t\treverse_proxy backend:8080\n", 1),
                dockerfile,
                compose_overlay,
                reset_script,
                smoke_script,
                acceptance_script,
                control_script,
            )
        with self.assertRaisesRegex(
            verifier.ContractViolation,
            "must not be published",
        ):
            verifier.validate_caddy_internal_probe_texts(
                caddyfile,
                dockerfile,
                compose_overlay.replace(
                    '- "${DEMO_HTTPS_BIND:-0.0.0.0}:${DEMO_HTTPS_PORT:-443}:443"',
                    '- "${DEMO_HTTPS_BIND:-0.0.0.0}:${DEMO_HTTPS_PORT:-443}:443"\n'
                    '      - "0.0.0.0:8080:8080"',
                ),
                reset_script,
                smoke_script,
                acceptance_script,
                control_script,
            )

    def test_acceptance_contract_preserves_upload_evidence_through_cleanup(self) -> None:
        acceptance = (ROOT / "scripts/demo/acceptance-demo.sh").read_text(
            encoding="utf-8"
        )
        control = (ROOT / "scripts/demo/demo_control.py").read_text(encoding="utf-8")

        verifier.validate_acceptance_script_text(acceptance, control)
        missing_reset_evidence = acceptance.replace(
            '--drive-file-id "${drive_file_id}"',
            "",
            1,
        )
        with self.assertRaisesRegex(
            verifier.ContractViolation,
            "evidence must reach live and reset verification: drive-file-id",
        ):
            verifier.validate_acceptance_script_text(missing_reset_evidence, control)

    def test_acceptance_contract_preserves_signal_exit_codes_through_cleanup(self) -> None:
        acceptance = (ROOT / "scripts/demo/acceptance-demo.sh").read_text(
            encoding="utf-8"
        )
        control = (ROOT / "scripts/demo/demo_control.py").read_text(encoding="utf-8")

        verifier.validate_acceptance_script_text(acceptance, control)
        with self.assertRaisesRegex(
            verifier.ContractViolation,
            "acceptance signal handling",
        ):
            verifier.validate_acceptance_script_text(
                acceptance.replace(
                    'source "${SCRIPT_DIR}/acceptance_traps.sh"',
                    "trap finalize_acceptance INT TERM",
                    1,
                ),
                control,
            )

        helper = (ROOT / "scripts/demo/acceptance_traps.sh").read_text(encoding="utf-8")
        verifier.validate_acceptance_signal_helper(helper)
        with self.assertRaisesRegex(
            verifier.ContractViolation,
            "acceptance signal handling",
        ):
            verifier.validate_acceptance_signal_helper(
                helper.replace("demo_acceptance_signal 143", "demo_acceptance_signal 0", 1)
            )

    def test_acceptance_contract_requires_physical_generation_cleanup(self) -> None:
        acceptance = (ROOT / "scripts/demo/acceptance-demo.sh").read_text(
            encoding="utf-8"
        )
        control = (ROOT / "scripts/demo/demo_control.py").read_text(encoding="utf-8")
        missing_cleanup = acceptance.replace(
            "demo_tool assert-generation-absent",
            "demo_tool verify-current-files",
            1,
        )

        with self.assertRaisesRegex(
            verifier.ContractViolation,
            "upload acceptance cleanup proof is missing",
        ):
            verifier.validate_acceptance_script_text(missing_cleanup, control)

    def test_acceptance_contract_requires_all_four_upload_surfaces(self) -> None:
        acceptance = (ROOT / "scripts/demo/acceptance-demo.sh").read_text(
            encoding="utf-8"
        )
        control = (ROOT / "scripts/demo/demo_control.py").read_text(encoding="utf-8")
        missing_drive = control.replace(
            'drive_upload_path = "/api/v1/drive/files"',
            'drive_upload_path = "/api/v1/files"',
            1,
        )

        with self.assertRaisesRegex(
            verifier.ContractViolation,
            "upload acceptance path is missing: drive_upload_path",
        ):
            verifier.validate_acceptance_script_text(acceptance, missing_drive)

    def test_acceptance_contract_pins_plus_one_rejection_before_exact_limit(self) -> None:
        acceptance = (ROOT / "scripts/demo/acceptance-demo.sh").read_text(
            encoding="utf-8"
        )
        control = (ROOT / "scripts/demo/demo_control.py").read_text(encoding="utf-8")
        missing_plus_one = control.replace('payload=payload + b"x"', "payload=payload", 1)

        with self.assertRaisesRegex(
            verifier.ContractViolation,
            "30MiB upload boundary acceptance",
        ):
            verifier.validate_acceptance_script_text(acceptance, missing_plus_one)

    def test_acceptance_contract_pins_uploader_endpoint_and_cross_owner_paths(self) -> None:
        acceptance = (ROOT / "scripts/demo/acceptance-demo.sh").read_text(
            encoding="utf-8"
        )
        control = (ROOT / "scripts/demo/demo_control.py").read_text(encoding="utf-8")
        manager_pending = 'context, "pending", context.manager_token, context.manager_id'
        staff_pending = 'context, "pending", context.staff_token, context.staff_id'

        mutations = (
            (
                control.replace(manager_pending, staff_pending, 1),
                "generic PENDING uploader isolation",
            ),
            (
                control.replace(
                    '"title": f"{context.marker}-reused-board-approval",\n'
                    '            "content": "must not be persisted",\n'
                    '            "approverIds": [context.manager_id],\n'
                    '            "attachmentFileIds": [upload.board_file_id],',
                    '"title": f"{context.marker}-reused-board-approval",\n'
                    '            "content": "must not be persisted",\n'
                    '            "approverIds": [context.manager_id],\n'
                    '            "attachmentFileIds": [upload.approval_file_id],',
                    1,
                ),
                "approval owner isolation",
            ),
            (
                control.replace(
                    'create_path = "/api/v1/expenses"',
                    'create_path = "/api/v1/approvals"',
                    1,
                ),
                "expense owner isolation",
            ),
        )
        for mutated_control, expected_error in mutations:
            with self.subTest(expected_error=expected_error):
                self.assertNotEqual(mutated_control, control)
                with self.assertRaisesRegex(verifier.ContractViolation, expected_error):
                    verifier.validate_acceptance_script_text(acceptance, mutated_control)

    def test_acceptance_contract_pins_restart_bytes_and_database_fields(self) -> None:
        acceptance = (ROOT / "scripts/demo/acceptance-demo.sh").read_text(
            encoding="utf-8"
        )
        control = (ROOT / "scripts/demo/demo_control.py").read_text(encoding="utf-8")
        before_last_body_check, separator, after_last_body_check = acceptance.rpartition(
            "verify_acceptance_file_bodies"
        )
        self.assertTrue(separator)
        missing_restart_body_check = before_last_body_check + after_last_body_check
        with self.assertRaisesRegex(
            verifier.ContractViolation,
            "acceptance restart file evidence",
        ):
            verifier.validate_acceptance_script_text(missing_restart_body_check, control)

        missing_uploader_field = acceptance.replace(
            "COALESCE(uploader_id, 0), size FROM stored_files",
            "size FROM stored_files",
            1,
        )
        with self.assertRaisesRegex(
            verifier.ContractViolation,
            "evidence query fields",
        ):
            verifier.validate_acceptance_script_text(missing_uploader_field, control)

    def test_tested_image_pair_contract_accepts_current_workflow(self) -> None:
        live_job, publish_job = build_workflow_jobs()

        verifier.validate_tested_image_pair_flow(live_job, publish_job)

    def test_build_workflow_runs_for_master_and_demo_branch_pushes(self) -> None:
        workflow = (ROOT / ".github/workflows/build-and-push.yml").read_text(
            encoding="utf-8"
        )

        verifier.validate_build_workflow_trigger(workflow)
        for invalid in (
            workflow.replace("branches: [master, demo]", "branches: [master]", 1),
            workflow.replace("branches: [master, demo]", "branches: [master, feat/demo]", 1),
            workflow.replace("branches: [master, demo]", "branches: ['**']", 1),
        ):
            with self.assertRaisesRegex(
                verifier.ContractViolation,
                "master and demo pushes",
            ):
                verifier.validate_build_workflow_trigger(invalid)

    def test_build_workflow_rejects_floating_action_references(self) -> None:
        workflow = (ROOT / ".github/workflows/build-and-push.yml").read_text(
            encoding="utf-8"
        )

        verifier.validate_action_pins(workflow)
        floating = workflow.replace(
            f"actions/checkout@{verifier.ACTION_PINS['actions/checkout']}",
            "actions/checkout@v4",
            1,
        )
        with self.assertRaisesRegex(
            verifier.ContractViolation,
            "not pinned to a full commit",
        ):
            verifier.validate_action_pins(floating)

        wrong_commit = workflow.replace(
            verifier.ACTION_PINS["actions/checkout"],
            "0" * 40,
            1,
        )
        with self.assertRaisesRegex(
            verifier.ContractViolation,
            "not pinned to the approved commit",
        ):
            verifier.validate_action_pins(wrong_commit)

    def test_all_workflows_pin_external_actions_to_full_commits(self) -> None:
        verifier.validate_all_workflow_action_pins(ROOT)

        demo_seed = (ROOT / ".github/workflows/demo-seed.yml").read_text(
            encoding="utf-8"
        )
        floating = demo_seed.replace(
            f"actions/setup-python@{verifier.ACTION_PINS['actions/setup-python']}",
            "actions/setup-python@v5",
            1,
        )
        with self.assertRaisesRegex(
            verifier.ContractViolation,
            "not pinned to a full commit",
        ):
            verifier.validate_action_pins(floating)

        with self.assertRaisesRegex(
            verifier.ContractViolation,
            "not pinned to a full commit",
        ):
            verifier.validate_action_pins("steps:\n  - uses: ./local-action")

        with self.assertRaisesRegex(
            verifier.ContractViolation,
            "local workflow reference is invalid",
        ):
            verifier.validate_action_pins(
                "steps:\n  - uses: ./.github/workflows/../local-action.yml"
            )

    def test_image_platform_filter_pins_single_and_map_shapes(self) -> None:
        filter_text = (
            ROOT / "scripts/demo/require-linux-arm64.jq"
        ).read_text(encoding="utf-8")
        fixture = json.loads(
            (ROOT / "scripts/demo/fixtures/imagetools-image-map.json").read_text(
                encoding="utf-8"
            )
        )
        single_fixture = json.loads(
            (ROOT / "scripts/demo/fixtures/imagetools-image-single.json").read_text(
                encoding="utf-8"
            )
        )

        self.assertEqual(
            " ".join(filter_text.split()),
            (
                'if type != "object" then empty '
                'elif has("os") or has("architecture") then '
                'select(.os == "linux" and .architecture == "arm64") '
                'else .["linux/arm64"]? | select(type == "object" and '
                '.os == "linux" and .architecture == "arm64") end'
            ),
        )
        self.assertEqual(fixture["linux/arm64"]["os"], "linux")
        self.assertEqual(fixture["linux/arm64"]["architecture"], "arm64")
        self.assertEqual(single_fixture["os"], "linux")
        self.assertEqual(single_fixture["architecture"], "arm64")

        live_job, publish_job = build_workflow_jobs()
        wrong_shape = publish_job.replace("{{json .Image}}", "{{json .Manifest}}", 1)
        with self.assertRaisesRegex(
            verifier.ContractViolation,
            "registry digests",
        ):
            verifier.validate_tested_image_pair_flow(live_job, wrong_shape)

    def test_tested_image_pair_contract_rejects_different_tag_source(self) -> None:
        live_job, publish_job = build_workflow_jobs()
        publish_job = publish_job.replace(
            "docker tag simple-erp-backend:acceptance",
            "docker pull untested-backend:latest\n"
            "          docker tag untested-backend:latest",
        )

        with self.assertRaisesRegex(
            verifier.ContractViolation,
            "only the exact tested image pair",
        ):
            verifier.validate_tested_image_pair_flow(live_job, publish_job)

    def test_tested_image_pair_contract_rejects_later_overwrite_step(self) -> None:
        live_job, publish_job = build_workflow_jobs()
        publish_job += """
      - name: Overwrite published pair
        run: |
          docker pull untested-backend:latest
          docker tag untested-backend:latest "${backend}"
          docker push "${backend}"
"""

        with self.assertRaisesRegex(
            verifier.ContractViolation,
            "publish step allowlist",
        ):
            verifier.validate_tested_image_pair_flow(live_job, publish_job)

    def test_tested_image_pair_contract_rejects_artifact_name_mismatch(self) -> None:
        live_job, publish_job = build_workflow_jobs()
        publish_job = publish_job.replace(
            verifier.TESTED_ARTIFACT_NAME,
            "untested-demo-images-${{ github.sha }}",
        )

        with self.assertRaisesRegex(
            verifier.ContractViolation,
            "download contract",
        ):
            verifier.validate_tested_image_pair_flow(live_job, publish_job)


if __name__ == "__main__":
    unittest.main()
