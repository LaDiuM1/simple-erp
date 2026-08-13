#!/usr/bin/env python3
"""Negative contracts for the demo seed control plane."""

from __future__ import annotations

import copy
import datetime as dt
import heapq
import importlib.util
import io
import json
import os
from pathlib import Path
import signal
import shutil
import subprocess
import sys
import tempfile
import unittest
from unittest import mock
import uuid
import xml.etree.ElementTree as ET
import zipfile


ROOT = Path(__file__).resolve().parents[2]


def load_module(name: str, path: Path):
    spec = importlib.util.spec_from_file_location(name, path)
    if spec is None or spec.loader is None:
        raise RuntimeError(f"module을 불러올 수 없음: {path}")
    module = importlib.util.module_from_spec(spec)
    sys.modules[name] = module
    spec.loader.exec_module(module)
    return module


control = load_module("demo_control_under_test", ROOT / "scripts/demo/demo_control.py")
generator = load_module("generate_seed_under_test", ROOT / "demo/tools/generate_seed.py")


class ResetScriptResourceContractTest(unittest.TestCase):

    def test_live_backend_stops_immediately_after_write_lock(self) -> None:
        reset_script = (ROOT / "scripts/demo/reset-demo.sh").read_text(encoding="utf-8")
        significant_lines = [
            line.strip()
            for line in reset_script.splitlines()
            if line.strip() and not line.lstrip().startswith("#")
        ]
        write_lock_index = significant_lines.index("write_resetting_lifecycle_state")
        self.assertEqual(
            significant_lines[write_lock_index + 1 : write_lock_index + 3],
            ['failure_stage="backend-stop"', "demo_compose stop -t 30 backend"],
        )
        self.assertLess(
            significant_lines.index("demo_compose stop -t 30 backend"),
            significant_lines.index('failure_stage="candidate-app-preflight"'),
        )

    def test_fresh_files_volume_is_prepared_before_cleanup(self) -> None:
        reset_script = (ROOT / "scripts/demo/reset-demo.sh").read_text(encoding="utf-8")
        self.assertLess(
            reset_script.index('failure_stage="files-volume-ownership"'),
            reset_script.index('failure_stage="retention-pre-prune"'),
        )
        self.assertIn("demo_prepare_files_volume", reset_script)

    def test_database_password_values_are_not_compose_arguments(self) -> None:
        library = (ROOT / "scripts/demo/lib.sh").read_text(encoding="utf-8")
        reset_script = (ROOT / "scripts/demo/reset-demo.sh").read_text(encoding="utf-8")
        readme = (ROOT / "demo/README.md").read_text(encoding="utf-8")

        self.assertIn('export MYSQL_PWD="${DEMO_DB_ROOT_PASSWORD}"', library)
        self.assertIn("demo_compose exec -T -e MYSQL_PWD", library)
        self.assertNotIn('MYSQL_PWD=${DEMO_DB_ROOT_PASSWORD}', library)

        self.assertIn('export DB_PASSWORD="${preflight_db_password}"', reset_script)
        self.assertIn("    -e DB_PASSWORD \\", reset_script)
        self.assertNotIn('DB_PASSWORD=${preflight_db_password}', reset_script)
        self.assertIn("demo_db_root <<SQL", reset_script)
        self.assertNotIn('demo_db_root -e "\n  DROP USER', reset_script)

        self.assertIn("-e MARIADB_ROOT_PASSWORD mariadb:", readme)
        self.assertIn("docker exec -e MYSQL_PWD simple-erp-seed-check", readme)
        self.assertNotIn("-e MARIADB_ROOT_PASSWORD=", readme)
        self.assertNotIn("-e MYSQL_PWD=", readme)


@unittest.skipUnless(os.name == "posix" and shutil.which("bash"), "POSIX bash 필요")
class ResetTimerScheduleContractTest(unittest.TestCase):

    KST = dt.timezone(dt.timedelta(hours=9))
    LIBRARY = ROOT / "scripts/demo/lib.sh"
    TIMER_CALENDAR = "OnCalendar=*-*-* 00/6:00:00 Asia/Seoul\n"

    @classmethod
    def snapshot(cls, local_time: str) -> str:
        local = dt.datetime.fromisoformat(local_time).replace(tzinfo=cls.KST)
        return f"{int(local.timestamp())}|{local:%Y-%m-%d}|{local:%H:%M:%S}"

    @classmethod
    def epoch(cls, local_time: str) -> str:
        local = dt.datetime.fromisoformat(local_time).replace(tzinfo=cls.KST)
        return str(int(local.timestamp()))

    def run_library(self, command: str, environment: dict[str, str] | None = None):
        merged_environment = os.environ.copy()
        if environment:
            merged_environment.update(environment)
        return subprocess.run(
            [
                "bash",
                "-c",
                f"source {json.dumps(str(self.LIBRARY))}; {command}",
            ],
            text=True,
            capture_output=True,
            env=merged_environment,
            check=False,
        )

    def run_next_reset(self, **overrides: str):
        with tempfile.TemporaryDirectory(prefix="demo-reset-timer-") as directory:
            temporary = Path(directory)
            fake_systemctl = temporary / "systemctl"
            fake_systemctl.write_text(
                "#!/usr/bin/env python3\n"
                "import os\n"
                "import sys\n"
                "args = sys.argv[1:]\n"
                "if args == ['is-active', '--quiet', 'simple-erp-demo-reset.timer']:\n"
                "    raise SystemExit(int(os.environ.get('FAKE_TIMER_ACTIVE_EXIT', '0')))\n"
                "if args == ['is-enabled', '--quiet', 'simple-erp-demo-reset.timer']:\n"
                "    raise SystemExit(int(os.environ.get('FAKE_TIMER_ENABLED_EXIT', '0')))\n"
                "if args == ['show', 'simple-erp-demo-reset.timer', '--property=Unit', '--value']:\n"
                "    print(os.environ.get('FAKE_TIMER_UNIT', 'simple-erp-demo-reset.service'))\n"
                "    raise SystemExit(int(os.environ.get('FAKE_TIMER_UNIT_EXIT', '0')))\n"
                "if args == ['cat', 'simple-erp-demo-reset.timer']:\n"
                "    sys.stdout.write(os.environ['FAKE_TIMER_TEXT'])\n"
                "    raise SystemExit(int(os.environ.get('FAKE_TIMER_CAT_EXIT', '0')))\n"
                "if args == ['show', 'simple-erp-demo-reset.timer', '--property=NextElapseUSecRealtime', '--value']:\n"
                "    print(os.environ.get('FAKE_NEXT_ELAPSE', ''))\n"
                "    raise SystemExit(int(os.environ.get('FAKE_NEXT_ELAPSE_EXIT', '0')))\n"
                "if args == ['show', 'simple-erp-demo-reset.service', '--property=ActiveState', '--value']:\n"
                "    print(os.environ.get('FAKE_SERVICE_STATE', 'activating'))\n"
                "    raise SystemExit(int(os.environ.get('FAKE_SERVICE_STATE_EXIT', '0')))\n"
                "print(f'unexpected systemctl arguments: {args!r}', file=sys.stderr)\n"
                "raise SystemExit(64)\n",
                encoding="utf-8",
            )
            fake_systemctl.chmod(0o755)
            environment = os.environ.copy()
            environment.update(
                {
                    "PATH": f"{temporary}{os.pathsep}{environment['PATH']}",
                    "FAKE_TIMER_TEXT": self.TIMER_CALENDAR,
                    "FAKE_KST_SNAPSHOT": self.snapshot("2026-08-14 05:59:59"),
                    "FAKE_NOW_EPOCH": self.epoch("2026-08-14 05:59:59"),
                }
            )
            environment.update(overrides)
            return self.run_library(
                "demo_kst_clock_snapshot() { printf '%s\\n' \"${FAKE_KST_SNAPSHOT}\"; }; "
                "demo_epoch_now() { printf '%s\\n' \"${FAKE_NOW_EPOCH}\"; }; "
                "demo_next_reset_at",
                environment,
            )

    def test_calendar_helper_is_deterministic_at_all_reset_boundaries(self) -> None:
        scenarios = {
            "2026-08-14 05:59:59": "2026-08-14T06:00:00+09:00",
            "2026-08-14 06:00:00": "2026-08-14T12:00:00+09:00",
            "2026-08-14 17:59:59": "2026-08-14T18:00:00+09:00",
            "2026-08-14 18:00:00": "2026-08-15T00:00:00+09:00",
            "2026-08-14 23:59:59": "2026-08-15T00:00:00+09:00",
        }
        for snapshot, expected in scenarios.items():
            with self.subTest(snapshot=snapshot):
                result = self.run_library(
                    "demo_next_calendar_reset_at "
                    + json.dumps(self.snapshot(snapshot))
                )
                self.assertEqual(result.returncode, 0, result.stderr)
                self.assertEqual(result.stdout.strip(), expected)

    def test_elapsed_boundary_is_advanced_by_six_hours(self) -> None:
        result = self.run_library(
            "demo_ensure_future_reset_at "
            "2026-08-14T06:00:00+09:00 "
            + self.epoch("2026-08-14 06:00:00")
        )
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual(result.stdout.strip(), "2026-08-14T12:00:00+09:00")

    def test_activating_service_allows_blank_or_na_timer_property(self) -> None:
        for value in ("", "n/a"):
            with self.subTest(next_elapse=value):
                result = self.run_next_reset(FAKE_NEXT_ELAPSE=value)
                self.assertEqual(result.returncode, 0, result.stderr)
                self.assertEqual(
                    result.stdout.strip(),
                    "2026-08-14T06:00:00+09:00",
                )

    def test_fallback_requires_exact_activating_service_state(self) -> None:
        for state in ("active", "inactive", "failed", ""):
            with self.subTest(state=state):
                result = self.run_next_reset(FAKE_SERVICE_STATE=state)
                self.assertNotEqual(result.returncode, 0)
                self.assertIn("outside service activation", result.stderr)

    def test_systemd_property_read_failures_are_not_treated_as_empty_values(self) -> None:
        scenarios = (
            (
                {"FAKE_NEXT_ELAPSE_EXIT": "1"},
                "next execution time cannot be read",
            ),
            (
                {"FAKE_SERVICE_STATE_EXIT": "1"},
                "service state cannot be read",
            ),
        )
        for overrides, error in scenarios:
            with self.subTest(overrides=overrides):
                result = self.run_next_reset(**overrides)
                self.assertNotEqual(result.returncode, 0)
                self.assertIn(error, result.stderr)

    def test_timer_unit_and_calendar_contracts_fail_closed(self) -> None:
        scenarios = (
            {"FAKE_TIMER_ACTIVE_EXIT": "3"},
            {"FAKE_TIMER_ENABLED_EXIT": "1"},
            {"FAKE_TIMER_UNIT_EXIT": "1"},
            {"FAKE_TIMER_UNIT": "other-reset.service"},
            {"FAKE_TIMER_CAT_EXIT": "1"},
            {"FAKE_TIMER_TEXT": self.TIMER_CALENDAR * 2},
            {"FAKE_TIMER_TEXT": "OnCalendar=*-*-* 00/6:00:00 +09\n"},
        )
        for overrides in scenarios:
            with self.subTest(overrides=overrides):
                result = self.run_next_reset(**overrides)
                self.assertNotEqual(result.returncode, 0)

    def test_valid_systemd_timestamp_with_numeric_offset_is_preserved(self) -> None:
        result = self.run_next_reset(
            FAKE_NEXT_ELAPSE="2026-08-20T06:00:00+09:00",
            FAKE_SERVICE_STATE="inactive",
        )
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual(result.stdout.strip(), "2026-08-20T06:00:00+09:00")

    def test_invalid_nonempty_systemd_timestamp_is_rejected(self) -> None:
        result = self.run_next_reset(FAKE_NEXT_ELAPSE="not-a-timestamp")
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("invalid next execution time", result.stderr)

    def test_fallback_rechecks_clock_after_candidate_calculation(self) -> None:
        result = self.run_next_reset(
            FAKE_KST_SNAPSHOT=self.snapshot("2026-08-14 05:59:59"),
            FAKE_NOW_EPOCH=self.epoch("2026-08-14 06:00:00"),
        )
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual(result.stdout.strip(), "2026-08-14T12:00:00+09:00")


@unittest.skipUnless(os.name == "posix" and shutil.which("bash"), "POSIX bash 필요")
class DbSecretArgvContractTest(unittest.TestCase):

    def test_root_db_password_reaches_docker_environment_but_not_actual_argv(self) -> None:
        marker = "root-password-must-not-appear-in-argv"
        sql_marker = "preflight-password-must-travel-on-stdin"
        sql = f"CREATE USER demo IDENTIFIED BY '{sql_marker}';\n"
        with tempfile.TemporaryDirectory(prefix="demo-db-argv-") as directory:
            temporary = Path(directory)
            capture = temporary / "capture.json"
            fake_docker = temporary / "docker"
            fake_docker.write_text(
                "#!/usr/bin/env python3\n"
                "import json\n"
                "import os\n"
                "from pathlib import Path\n"
                "import sys\n"
                "capture = {\n"
                "    'argv': sys.argv,\n"
                "    'procCmdline': Path('/proc/self/cmdline').read_bytes().decode().split('\\0'),\n"
                "    'mysqlPwd': os.environ.get('MYSQL_PWD'),\n"
                "    'stdin': sys.stdin.read(),\n"
                "}\n"
                "Path(os.environ['DEMO_ARGV_CAPTURE']).write_text(\n"
                "    json.dumps(capture), encoding='utf-8'\n"
                ")\n",
                encoding="utf-8",
            )
            fake_docker.chmod(0o755)
            environment = os.environ.copy()
            environment.update(
                {
                    "PATH": f"{temporary}{os.pathsep}{environment['PATH']}",
                    "DEMO_ARGV_CAPTURE": str(capture),
                    "DEMO_DB_ROOT_PASSWORD": marker,
                }
            )

            subprocess.run(
                [
                    "bash",
                    "-c",
                    f"source {json.dumps(str(ROOT / 'scripts/demo/lib.sh'))}; "
                    "demo_db_root simple_erp_demo",
                ],
                input=sql,
                text=True,
                check=True,
                env=environment,
            )

            observed = json.loads(capture.read_text(encoding="utf-8"))
            self.assertEqual(observed["mysqlPwd"], marker)
            self.assertEqual(observed["stdin"], sql)
            self.assertTrue(
                all(
                    marker not in argument and sql_marker not in argument
                    for argument in observed["argv"]
                )
            )
            self.assertTrue(
                all(
                    marker not in argument and sql_marker not in argument
                    for argument in observed["procCmdline"]
                )
            )
            self.assertNotIn(f"MYSQL_PWD={marker}", observed["argv"])
            self.assertIn("MYSQL_PWD", observed["argv"])


@unittest.skipUnless(os.name == "posix" and shutil.which("bash"), "POSIX bash 필요")
class AcceptanceTrapTest(unittest.TestCase):

    def run_child(self, mode: str, child_signal: signal.Signals | None = None) -> tuple[int, str]:
        with tempfile.TemporaryDirectory(prefix="demo-acceptance-trap-") as directory:
            marker = Path(directory) / "cleanup.log"
            script = Path(directory) / "child.sh"
            helper = ROOT / "scripts/demo/acceptance_traps.sh"
            script.write_text(
                "#!/usr/bin/env bash\n"
                "set -Eeuo pipefail\n"
                f"source {json.dumps(str(helper))}\n"
                "marker=$1\n"
                "mode=$2\n"
                "finalize() {\n"
                "  original_exit=$?\n"
                "  trap - EXIT INT TERM\n"
                "  printf '%s\\n' \"${original_exit}\" > \"${marker}\"\n"
                "  exit \"${original_exit}\"\n"
                "}\n"
                "demo_install_acceptance_traps finalize\n"
                "printf 'ready\\n'\n"
                "case \"${mode}\" in\n"
                "  success) exit 0 ;;\n"
                "  failure) exit 7 ;;\n"
                "  wait) while true; do sleep 1; done ;;\n"
                "esac\n",
                encoding="utf-8",
            )
            process = subprocess.Popen(
                ["bash", str(script), str(marker), mode],
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
                text=True,
            )
            try:
                self.assertEqual(process.stdout.readline().strip(), "ready")
                if child_signal is not None:
                    process.send_signal(child_signal)
                exit_code = process.wait(timeout=5)
            finally:
                if process.poll() is None:
                    process.kill()
                    process.wait(timeout=5)
                if process.stdout is not None:
                    process.stdout.close()
                if process.stderr is not None:
                    process.stderr.close()
            return exit_code, marker.read_text(encoding="utf-8").strip()

    def test_exit_status_is_preserved_after_cleanup(self) -> None:
        self.assertEqual(self.run_child("success"), (0, "0"))
        self.assertEqual(self.run_child("failure"), (7, "7"))

    def test_signal_status_is_preserved_after_cleanup(self) -> None:
        self.assertEqual(self.run_child("wait", signal.SIGINT), (130, "130"))
        self.assertEqual(self.run_child("wait", signal.SIGTERM), (143, "143"))


def replace_zip_member(payload: bytes, member_name: str, replacement: bytes) -> bytes:
    source = zipfile.ZipFile(io.BytesIO(payload))
    output = io.BytesIO()
    with source, zipfile.ZipFile(output, "w", compression=zipfile.ZIP_STORED) as target:
        for info in source.infolist():
            data = replacement if info.filename == member_name else source.read(info)
            copied = zipfile.ZipInfo(info.filename, info.date_time)
            copied.create_system = info.create_system
            copied.compress_type = info.compress_type
            copied.external_attr = info.external_attr
            target.writestr(copied, data)
    return output.getvalue()


class FixtureValidationTest(unittest.TestCase):
    def test_generated_fixtures_are_accepted(self) -> None:
        for index in range(1, len(generator.FILE_CATALOG) + 1):
            original_name, content_type, payload = generator.file_spec(index)
            with self.subTest(index=index):
                control.validate_fixture(payload, content_type, original_name)

    def test_fixture_catalog_is_exact_and_business_like(self) -> None:
        self.assertEqual(
            tuple(entry[1] for entry in generator.FILE_CATALOG),
            control.EXPECTED_FIXTURE_NAMES,
        )
        forbidden = ("샘플", "가상", "합성", "SAMPLE", "SYNTH", "포트폴리오")
        for index, expected_name in enumerate(control.EXPECTED_FIXTURE_NAMES, 1):
            actual_name, _, _ = generator.file_spec(index)
            with self.subTest(index=index):
                self.assertEqual(actual_name, expected_name)
                self.assertFalse(any(marker in actual_name for marker in forbidden))

    def test_approval_comments_match_document_evidence(self) -> None:
        for status in ("APPROVED", "REJECTED"):
            with self.subTest(doc_type="EXPENSE", status=status):
                self.assertIn("증빙", generator.approval_step_comment("EXPENSE", status))
            for doc_type in ("GENERAL", "LEAVE"):
                comment = generator.approval_step_comment(doc_type, status)
                with self.subTest(doc_type=doc_type, status=status):
                    self.assertNotIn("증빙", comment)
                    self.assertNotIn("첨부", comment)
        self.assertIsNone(generator.approval_step_comment("LEAVE", "PENDING"))

    def test_cancellation_only_exists_without_a_domain_shadow(self) -> None:
        self.assertEqual(generator.approval_document_status(4), "CANCELED")
        self.assertEqual(generator.approval_document_status(8), "CANCELED")
        self.assertNotIn(
            "CANCELED",
            {generator.approval_document_status(index) for index in range(9, 37)},
        )

    def test_contact_catalog_and_channels_avoid_adjacent_generator_patterns(self) -> None:
        identities = [generator.contact_identity(index) for index in range(1, 73)]
        self.assertEqual(len(set(identities)), 72)
        self.assertGreater(len({name[-2:] for name, _ in identities[:10]}), 3)

        employee_suffixes = [generator.employee_phone(index)[-4:] for index in range(1, 11)]
        contact_suffixes = [
            generator.contact_channels(index, identities[index - 1][1])[1][-4:]
            for index in range(1, 11)
        ]
        for suffixes in (employee_suffixes, contact_suffixes):
            self.assertEqual(len(set(suffixes)), len(suffixes))
            deltas = [int(right) - int(left) for left, right in zip(suffixes, suffixes[1:])]
            self.assertTrue(any(delta > 0 for delta in deltas))
            self.assertTrue(any(delta < 0 for delta in deltas))

        for index in range(1, 49):
            self.assertEqual(
                generator.contact_business_role(index),
                ("설비 투자 최종 의사결정자", "경영관리", "대표이사"),
            )
        for index in range(49, 73):
            _, department, position = generator.contact_business_role(index)
            self.assertIn(department, {"생산기술팀", "구매팀", "설비보전팀"})
            self.assertIn(position, {"부장", "차장", "과장"})

        met_offsets = [generator.contact_met_days_ago(index) for index in range(1, 73)]
        employment_offsets = [
            generator.contact_employment_start_days_ago(index)
            for index in range(1, 73)
        ]
        for offsets in (met_offsets, employment_offsets):
            self.assertEqual(len(set(offsets)), 72)
            deltas = [right - left for left, right in zip(offsets, offsets[1:])]
            self.assertGreaterEqual(len(set(deltas)), 20)
            self.assertTrue(any(delta > 0 for delta in deltas[:10]))
            self.assertTrue(any(delta < 0 for delta in deltas[:10]))
            self.assertFalse(all(abs(delta) == 1 for delta in deltas[:10]))
        self.assertTrue(
            all(
                employment > met
                for employment, met in zip(employment_offsets, met_offsets)
            )
        )

    def test_sales_owners_are_active_sales_team_employees(self) -> None:
        self.assertEqual(len(generator.EMPLOYEE_DEPARTMENT_IDS), 22)
        self.assertEqual(len(generator.EMPLOYEE_POSITION_IDS), 22)
        self.assertEqual(len(set(generator.SALES_EMPLOYEE_IDS)), 8)
        for employee_id in generator.SALES_EMPLOYEE_IDS:
            self.assertLessEqual(employee_id, 18)
            self.assertIn(
                generator.EMPLOYEE_DEPARTMENT_IDS[employee_id - 1],
                {5, 6},
            )
            self.assertNotEqual(generator.EMPLOYEE_POSITION_IDS[employee_id - 1], 7)
        self.assertTrue(all(
            generator.EMPLOYEE_DEPARTMENT_IDS[employee_id - 1] == 7
            for employee_id in range(7, 12)
        ))

    def test_customer_and_employee_display_fields_avoid_linear_sequences(self) -> None:
        customer_rows = [
            (*generator.customer_address(index), *generator.customer_channels(index))
            for index in range(1, 49)
        ]
        self.assertEqual(len(set(customer_rows)), 48)
        phone_suffixes = [int(row[4][-4:]) for row in customer_rows]
        fax_suffixes = [int(row[5][-4:]) for row in customer_rows]
        road_numbers = [int(row[0].rsplit(" ", 1)[1]) for row in customer_rows]
        trade_offsets = [
            generator.customer_trade_start_days_ago(index)
            for index in range(1, 49)
        ]
        for values in (phone_suffixes, fax_suffixes, road_numbers, trade_offsets):
            self.assertEqual(len(set(values)), 48)
            deltas = [right - left for left, right in zip(values, values[1:])]
            self.assertGreaterEqual(len(set(deltas)), 8)
            self.assertTrue(any(delta > 0 for delta in deltas))
            self.assertTrue(any(delta < 0 for delta in deltas))
        self.assertFalse(any("customer" in row[3] for row in customer_rows))

        dates = [generator.employee_dates(index) for index in range(1, 23)]
        self.assertEqual(len(set(dates)), 22)
        birth_deltas = [right[0] - left[0] for left, right in zip(dates, dates[1:])]
        join_deltas = [right[1] - left[1] for left, right in zip(dates, dates[1:])]
        self.assertGreaterEqual(len(set(birth_deltas)), 8)
        self.assertGreaterEqual(len(set(join_deltas)), 8)

        addresses = [generator.employee_address(index) for index in range(1, 23)]
        self.assertEqual(len(addresses), 22)
        self.assertEqual(len(set(addresses)), 22)
        self.assertGreaterEqual(len({address[0].split()[0] for address in addresses}), 6)
        self.assertFalse(any("솔빛타워" in field for row in addresses for field in row))

    def test_sales_activities_use_operational_contacts_when_available(self) -> None:
        operational_types = ("VISIT", "CALL", "MEETING", "EMAIL")
        for customer_id in range(1, 25):
            for activity_type in operational_types:
                self.assertEqual(
                    generator.sales_activity_contact_id(customer_id, activity_type),
                    48 + customer_id,
                )
            self.assertEqual(
                generator.sales_activity_contact_id(customer_id, "OTHER"),
                customer_id,
            )
        for customer_id in range(25, 49):
            for activity_type in (*operational_types, "OTHER"):
                self.assertEqual(
                    generator.sales_activity_contact_id(customer_id, activity_type),
                    customer_id,
                )

        self.assertEqual(len(generator.SALES_ACTIVITY_COUNT_PLAN), 48)
        self.assertEqual(sum(generator.SALES_ACTIVITY_COUNT_PLAN), 144)
        self.assertEqual(
            {
                count: generator.SALES_ACTIVITY_COUNT_PLAN.count(count)
                for count in range(1, 6)
            },
            {1: 9, 2: 9, 3: 12, 4: 9, 5: 9},
        )
        for customer_id, activity_count in enumerate(
            generator.SALES_ACTIVITY_COUNT_PLAN,
            1,
        ):
            self.assertIn(activity_count, range(1, 6))
            self.assertLess(
                generator.sales_activity_type_index(customer_id, 0),
                4,
            )

        recent_ranks = [
            generator.sales_activity_recent_rank(customer_id)
            for customer_id in range(1, 49)
        ]
        self.assertEqual(sorted(recent_ranks), list(range(48)))
        self.assertEqual(
            len({generator.recent_activity_clock(rank) for rank in recent_ranks}),
            48,
        )
        recent_clock_seconds = {
            rank: sum(
                value * multiplier
                for value, multiplier in zip(
                    map(int, generator.recent_activity_clock(rank).split(":")),
                    (3600, 60, 1),
                )
            )
            for rank in recent_ranks
        }
        recent_metadata = {}
        for customer_id in range(1, 49):
            recent_rank = generator.sales_activity_recent_rank(customer_id)
            activity_type_index = generator.sales_activity_type_index(
                customer_id,
                0,
            )
            recent_metadata[recent_rank] = (
                customer_id,
                activity_type_index,
                generator.sales_activity_subject(
                    customer_id,
                    0,
                    activity_type_index,
                ),
            )

        # Ordering can change only when a fixed clock becomes eligible or a
        # reset-relative fallback crosses one. Day one is checked second by
        # second; the remaining dates check every such transition boundary.
        transition_seconds = {0, 86_399}
        for clock_seconds in recent_clock_seconds.values():
            for fallback_rank in recent_ranks:
                for boundary in (
                    clock_seconds - 1,
                    clock_seconds,
                    clock_seconds + 1,
                    clock_seconds + fallback_rank,
                    clock_seconds + fallback_rank + 1,
                    clock_seconds + fallback_rank + 2,
                ):
                    if 0 <= boundary < 86_400:
                        transition_seconds.add(boundary)

        def recent_dashboard_rows(day_of_month: int, reset_seconds: int):
            candidates = []
            for recent_rank in recent_ranks:
                clock_seconds = recent_clock_seconds[recent_rank]
                days_ago = recent_rank % day_of_month
                if days_ago == 0 and clock_seconds > reset_seconds:
                    activity_seconds = reset_seconds - recent_rank - 0.5
                else:
                    activity_seconds = clock_seconds - days_ago * 86_400
                customer_id, activity_type_index, subject = recent_metadata[
                    recent_rank
                ]
                candidates.append(
                    (
                        activity_seconds,
                        -recent_rank,
                        customer_id,
                        activity_type_index,
                        subject,
                    )
                )
            return heapq.nlargest(5, candidates)

        for day_of_month in range(1, 32):
            reset_scenarios = (
                range(86_400) if day_of_month == 1 else transition_seconds
            )
            for reset_seconds in reset_scenarios:
                candidates = recent_dashboard_rows(day_of_month, reset_seconds)
                customer_count = len({row[2] for row in candidates})
                type_counts = [
                    sum(row[3] == activity_type for row in candidates)
                    for activity_type in range(4)
                ]
                subject_count = len({row[4] for row in candidates})
                if (
                    customer_count != 5
                    or len({row[3] for row in candidates}) < 3
                    or subject_count < 4
                    or max(type_counts) > 3
                ):
                    self.fail(
                        "recent activity diversity failed at "
                        f"day={day_of_month}, second={reset_seconds}, "
                        f"rows={candidates}"
                    )

    def test_business_timestamps_do_not_inherit_reset_wall_clock(self) -> None:
        clocks = [generator.business_clock(salt) for salt in range(1, 49)]
        self.assertEqual(len(set(clocks)), 48)
        self.assertTrue(all("08:20:00" <= clock <= "17:20:00" for clock in clocks))

        historical = str(generator.rel_ts(7, 13))
        self.assertIn("DATE_SUB(@seed_today, INTERVAL 7 DAY)", historical)
        self.assertNotIn("DATE_SUB(@seed_now", historical)
        self.assertNotIn("LEAST", historical)
        self.assertIn("LEAST", str(generator.rel_ts(0, 13)))

        verify_sql = (ROOT / "demo/seed/verify-seed.sql").read_text(
            encoding="utf-8"
        )
        self.assertIn(
            "WHERE DATE(activity_date)<@verify_today\n"
            "  AND activity_date<DATE_SUB(@verify_now,INTERVAL 49 SECOND)\n"
            "  AND TIME(activity_date) NOT BETWEEN '08:20:00' AND '17:20:00'",
            verify_sql,
        )

    def test_service_type_plan_matches_operational_distribution(self) -> None:
        expected = {
            "REPAIR": 14,
            "INSTALL_SUPPORT": 11,
            "TRAINING": 8,
            "INTERPRET": 5,
            "TUNING": 7,
        }
        self.assertEqual(len(generator.SERVICE_TYPE_PLAN), 45)
        self.assertEqual(
            {kind: generator.SERVICE_TYPE_PLAN.count(kind) for kind in expected},
            expected,
        )

    def test_audit_targets_match_their_menu_domain(self) -> None:
        expected_pairs = {
            (menu_code, target_type)
            for menu_code, target_type, _ in generator.AUDIT_TARGET_CATALOG
        }
        targets = [generator.audit_target(index) for index in range(1, 91)]
        self.assertEqual({target[:2] for target in targets}, expected_pairs)
        for menu_code, target_type, target_id in targets:
            target_count = next(
                count
                for menu, kind, count in generator.AUDIT_TARGET_CATALOG
                if (menu, kind) == (menu_code, target_type)
            )
            self.assertGreaterEqual(target_id, 1)
            self.assertLessEqual(target_id, target_count)

    def test_posts_and_attendance_use_deterministic_realistic_variation(self) -> None:
        self.assertEqual(len(generator.POST_CATALOG), 28)
        self.assertEqual(len({title for _, title, _ in generator.POST_CATALOG}), 28)
        self.assertEqual(len({content for _, _, content in generator.POST_CATALOG}), 28)
        self.assertFalse(any(title.endswith(("(2차)", "(3차)")) for _, title, _ in generator.POST_CATALOG))

        profiles = [generator.attendance_profile(1, day) for day in range(1, 33)]
        self.assertGreaterEqual(len({profile[0] for profile in profiles}), 8)
        self.assertGreaterEqual(len({profile[1] for profile in profiles}), 8)
        for profile in profiles:
            self.assertTrue("08:38:00" <= profile[0] <= "09:00:00")
            self.assertTrue("17:35:00" <= profile[1] <= "18:35:00")
            self.assertTrue(37.5662 <= profile[2] <= 37.5664)
            self.assertTrue(126.9778 <= profile[3] <= 126.9780)
            self.assertTrue(37.5662 <= profile[4] <= 37.5664)
            self.assertTrue(126.9778 <= profile[5] <= 126.9780)

    def test_drive_fixture_folders_match_business_meaning(self) -> None:
        self.assertEqual(len(generator.DRIVE_FOLDER_CATALOG), 10)
        self.assertEqual(len(generator.DRIVE_FILE_FOLDER_IDS), 12)
        folder_names = {
            folder_id: name
            for folder_id, name, _ in generator.DRIVE_FOLDER_CATALOG
        }
        expected_folder_names = (
            "영업자료", "서비스점검", "업무양식", "프로젝트자료",
            "프로젝트자료", "교육자료", "고객요구사항", "고객요구사항",
            "고객요구사항", "고객요구사항", "이용안내", "이용안내",
        )
        self.assertEqual(
            tuple(folder_names[folder_id] for folder_id in generator.DRIVE_FILE_FOLDER_IDS),
            expected_folder_names,
        )

    def test_active_or_malformed_fixtures_are_rejected(self) -> None:
        pdf = generator.make_pdf(1)
        png = generator.make_png(13)
        xlsx = generator.make_xlsx(7)

        sheet_name = "xl/worksheets/sheet1.xml"
        with zipfile.ZipFile(io.BytesIO(xlsx)) as workbook:
            sheet = workbook.read(sheet_name)
            relationship = workbook.read("_rels/.rels")
        formula_xlsx = replace_zip_member(
            xlsx,
            sheet_name,
            sheet.replace(b"</c>", b"<f>1+1</f></c>", 1),
        )
        external_xlsx = replace_zip_member(
            xlsx,
            "_rels/.rels",
            relationship.replace(
                b'Target="xl/workbook.xml"',
                b'Target="https://example.invalid/book" TargetMode="External"',
            ),
        )

        invalid = (
            (pdf.replace(b"DEMO DOCUMENT", b"FAKE DOCUMENT"), "application/pdf"),
            (pdf.replace(b"%%EOF", b"/JavaScript /JS %%EOF"), "application/pdf"),
            (pdf[:-12], "application/pdf"),
            (b"\x89PNG\r\n\x1a\n", "image/png"),
            (png[:-1] + bytes([png[-1] ^ 1]), "image/png"),
            (formula_xlsx, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
            (external_xlsx, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
            (b"plain text without markers", "text/plain"),
        )
        for payload, content_type in invalid:
            with self.subTest(content_type=content_type):
                with self.assertRaises(control.ControlError):
                    control.validate_fixture(payload, content_type, "negative-fixture")


class ManifestValidationTest(unittest.TestCase):
    def setUp(self) -> None:
        self.temporary = tempfile.TemporaryDirectory(prefix="demo-control-test-")
        self.seed_dir = Path(self.temporary.name)
        generator.generate(self.seed_dir)
        shutil.copy2(ROOT / "demo/seed/schema.sql", self.seed_dir / "schema.sql")
        self.manifest_path = self.seed_dir / "manifest.json"
        self.manifest = json.loads(self.manifest_path.read_text(encoding="utf-8"))

    def tearDown(self) -> None:
        self.temporary.cleanup()

    def assert_manifest_rejected(self, mutated: dict[str, object]) -> None:
        self.manifest_path.write_text(
            json.dumps(mutated, ensure_ascii=False, sort_keys=True, indent=2) + "\n",
            encoding="utf-8",
        )
        with self.assertRaises(control.ControlError):
            control.validate_bundle(self.seed_dir, generator.COMPATIBLE_APP_VERSION)

    def test_exact_manifest_contract_rejects_extensions_and_count_drift(self) -> None:
        control.validate_bundle(self.seed_dir, generator.COMPATIBLE_APP_VERSION)

        top_level_extra = copy.deepcopy(self.manifest)
        top_level_extra["privateContact"] = "operator@example.com"
        self.assert_manifest_rejected(top_level_extra)

        privacy_extra = copy.deepcopy(self.manifest)
        privacy_extra["privacy"]["forbiddenSupplierNames"] = ["real supplier"]
        self.assert_manifest_rejected(privacy_extra)

        count_drift = copy.deepcopy(self.manifest)
        count_drift["expectedCounts"]["employees"] = 999
        self.assert_manifest_rejected(count_drift)

        file_extra = copy.deepcopy(self.manifest)
        file_extra["files"][0]["sourcePath"] = "private/path"
        self.assert_manifest_rejected(file_extra)

        safe_but_unexpected_name = copy.deepcopy(self.manifest)
        safe_but_unexpected_name["files"][0]["originalName"] = "계약검토_최종.pdf"
        self.assert_manifest_rejected(safe_but_unexpected_name)

    def test_seed_sql_has_realistic_labels_without_visible_safety_markers(self) -> None:
        seed_sql = (self.seed_dir / "seed-data.sql").read_text(encoding="utf-8")
        for marker in ("샘플", "가상", "합성", "SAMPLE", "SYNTH", "포트폴리오"):
            with self.subTest(marker=marker):
                self.assertNotIn(marker, seed_sql)
        self.assertIn("@ongyeol.example", seed_sql)
        self.assertIn("000-00-00001", seed_sql)
        self.assertIn(generator.employee_phone(1), seed_sql)
        self.assertNotIn("010-0000-0001", seed_sql)

    def test_staff_scope_matches_reference_and_work_menu_contracts(self) -> None:
        seed_sql = (self.seed_dir / "seed-data.sql").read_text(encoding="utf-8")
        staff_rows = [
            line
            for line in seed_sql.splitlines()
            if line.rstrip(",;").endswith(", 3)")
        ]
        customer_row = next(line for line in staff_rows if "'CUSTOMERS'" in line)
        sales_customer_row = next(
            line for line in staff_rows if "'SALES_CUSTOMERS'" in line
        )
        contract_row = next(line for line in staff_rows if "'CONTRACTS'" in line)

        self.assertIn("'ALL', 'CUSTOMERS'", customer_row)
        self.assertIn("'SELF', 'SALES_CUSTOMERS'", sales_customer_row)
        self.assertIn("'SELF', 'CONTRACTS'", contract_row)

    def test_db_mapping_and_generation_metadata_are_exact(self) -> None:
        runtime_reset_date = dt.date(2027, 1, 2)
        self.assertNotEqual(
            runtime_reset_date,
            dt.datetime.fromisoformat(self.manifest["generatedAt"]).date(),
        )
        rows = []
        for item in self.manifest["files"]:
            created_date = runtime_reset_date - dt.timedelta(days=item["createdAtDaysAgo"])
            rows.append(
                "\t".join(
                    map(
                        str,
                        (
                            item["id"],
                            item["storedName"],
                            item["originalName"],
                            item["contentType"],
                            item["size"],
                            created_date.isoformat(),
                            runtime_reset_date.isoformat(),
                            item["status"],
                            item["ownerType"],
                            item["ownerId"],
                            item["uploaderId"],
                        ),
                    )
                )
            )
        mapping_path = self.seed_dir / "stored-files.tsv"
        mapping_path.write_text("\n".join(rows) + "\n", encoding="utf-8")
        mapping = control.load_mapping(mapping_path)
        control.validate_mapping_contract(mapping, self.manifest)

        generation = "00000000-0000-4000-8000-000000000001"
        metadata = json.loads(
            control.generation_metadata_bytes(generation, self.manifest, mapping)
        )
        self.assertEqual(
            set(metadata), {"files", "formatVersion", "generation", "seedVersion"}
        )
        self.assertEqual(
            set(metadata["files"][0]),
            {
                "contentType",
                "createdMonth",
                "createdYear",
                "id",
                "originalName",
                "ownerId",
                "ownerType",
                "sha256",
                "size",
                "status",
                "storedName",
                "uploaderId",
            },
        )

        fields = rows[0].split("\t")
        fields[2] = "private\tcontact.pdf"
        mapping_path.write_text("\t".join(fields) + "\n", encoding="utf-8")
        with self.assertRaises(control.ControlError):
            control.load_mapping(mapping_path)

        fields = rows[0].split("\t")
        fields[2] = "계약검토_최종.pdf"
        mapping_path.write_text("\t".join(fields) + "\n", encoding="utf-8")
        with self.assertRaises(control.ControlError):
            control.load_mapping(mapping_path)

        fields = rows[0].split("\t")
        fields[4] = str(int(fields[4]) + 1)
        changed_rows = rows.copy()
        changed_rows[0] = "\t".join(fields)
        mapping_path.write_text("\n".join(changed_rows) + "\n", encoding="utf-8")
        changed_mapping = control.load_mapping(mapping_path)
        with self.assertRaises(control.ControlError):
            control.validate_mapping_contract(changed_mapping, self.manifest)

        fields = rows[0].split("\t")
        fields[9] = str(int(fields[9]) + 1)
        changed_rows = rows.copy()
        changed_rows[0] = "\t".join(fields)
        mapping_path.write_text("\n".join(changed_rows) + "\n", encoding="utf-8")
        changed_mapping = control.load_mapping(mapping_path)
        with self.assertRaises(control.ControlError):
            control.validate_mapping_contract(changed_mapping, self.manifest)

        fields = rows[0].split("\t")
        fields[6] = (runtime_reset_date - dt.timedelta(days=1)).isoformat()
        changed_rows = rows.copy()
        changed_rows[0] = "\t".join(fields)
        mapping_path.write_text("\n".join(changed_rows) + "\n", encoding="utf-8")
        changed_mapping = control.load_mapping(mapping_path)
        with self.assertRaises(control.ControlError):
            control.validate_mapping_contract(changed_mapping, self.manifest)

    @unittest.skipUnless(os.name == "posix", "POSIX permission contract")
    def test_staged_generation_is_writable_by_the_backend_group(self) -> None:
        reset_date = dt.date(2027, 1, 2)
        rows = []
        for item in self.manifest["files"]:
            created_date = reset_date - dt.timedelta(days=item["createdAtDaysAgo"])
            rows.append(
                "\t".join(
                    map(
                        str,
                        (
                            item["id"],
                            item["storedName"],
                            item["originalName"],
                            item["contentType"],
                            item["size"],
                            created_date.isoformat(),
                            reset_date.isoformat(),
                            item["status"],
                            item["ownerType"],
                            item["ownerId"],
                            item["uploaderId"],
                        ),
                    )
                )
            )
        mapping_path = self.seed_dir / "stored-files.tsv"
        mapping_path.write_text("\n".join(rows) + "\n", encoding="utf-8")
        files_root = self.seed_dir / "files"
        files_root.mkdir()
        generation = "00000000-0000-4000-8000-000000000001"
        args = mock.Mock(
            generation=generation,
            files_root=Path("/files"),
            seed_dir=self.seed_dir,
            mapping=mapping_path,
            expected_app_version=generator.COMPATIBLE_APP_VERSION,
        )

        with mock.patch.object(
            control, "validated_files_root", return_value=files_root
        ):
            control.stage_files(args)

        generation_root = files_root / "generations" / generation
        self.assertEqual(
            (files_root / "generations").stat().st_mode & 0o7777,
            control.GENERATION_DIRECTORY_MODE,
        )
        for directory in (generation_root, *generation_root.rglob("*")):
            if directory.is_dir():
                self.assertEqual(
                    directory.stat().st_mode & 0o7777,
                    control.GENERATION_DIRECTORY_MODE,
                )
        mapping = control.load_mapping(mapping_path)
        for row in mapping.values():
            target = generation_root / row.year / row.month / row.stored_name
            self.assertEqual(
                target.stat().st_mode & 0o777,
                control.GENERATION_SEED_FILE_MODE,
            )


class StateRecoveryTest(unittest.TestCase):
    generation = "00000000-0000-4000-8000-000000000001"
    transition = "2026-08-02T12:00:00+09:00"

    def test_ready_state_advertises_the_enabled_upload_capability(self) -> None:
        manifest = {
            "publicAccounts": [
                {
                    "loginId": "demo.manager",
                    "password": "manager-password",
                    "recommended": True,
                },
                {
                    "loginId": "demo.staff",
                    "password": "staff-password",
                    "recommended": False,
                },
            ]
        }
        with tempfile.TemporaryDirectory(prefix="demo-state-test-") as temporary:
            state_dir = Path(temporary)
            args = mock.Mock(
                filename="status.json",
                candidate=self.generation,
                next_reset_at="2026-08-03T00:00:00+09:00",
                seed_dir=ROOT / "demo/seed",
                expected_app_version=generator.COMPATIBLE_APP_VERSION,
                state="READY",
            )
            with (
                mock.patch.object(
                    control, "validated_state_dir", return_value=state_dir
                ),
                mock.patch.object(control, "validate_bundle", return_value=manifest),
                mock.patch.object(control, "iso_now", return_value=self.transition),
            ):
                control.write_state(args)

            payload = json.loads(
                (state_dir / "status.json").read_text(encoding="utf-8")
            )
            self.assertIs(payload["data"]["uploadEnabled"], True)
            self.assertIs(payload["data"]["writeLocked"], False)

    def test_resetting_state_is_published_without_validating_the_bundle(self) -> None:
        accounts = [
            {
                "label": "manager",
                "description": "manager account",
                "loginId": "demo.manager",
                "password": "manager-password",
                "recommended": True,
            },
            {
                "label": "staff",
                "description": "staff account",
                "loginId": "demo.staff",
                "password": "staff-password",
                "recommended": False,
            },
        ]
        with tempfile.TemporaryDirectory(prefix="demo-resetting-state-test-") as temporary:
            state_dir = Path(temporary)
            (state_dir / "status.json").write_text(
                json.dumps(
                    {
                        "status": 200,
                        "data": {
                            "generation": self.generation,
                            "lastResetAt": "2026-08-02T02:00:00+09:00",
                            "publicAccounts": accounts,
                        },
                    }
                ),
                encoding="utf-8",
            )
            args = mock.Mock(
                state_dir=state_dir,
                candidate="00000000-0000-4000-8000-000000000002",
                next_reset_at="2026-08-03T00:00:00+09:00",
            )
            with (
                mock.patch.object(
                    control, "validated_state_dir", return_value=state_dir
                ),
                mock.patch.object(control, "validate_bundle") as validate_bundle,
                mock.patch.object(control, "iso_now", return_value=self.transition),
            ):
                control.write_resetting_state(args)

            validate_bundle.assert_not_called()
            payload = json.loads(
                (state_dir / "status.json").read_text(encoding="utf-8")
            )["data"]
            self.assertEqual(payload["state"], "RESETTING")
            self.assertIs(payload["writeLocked"], True)
            self.assertEqual(payload["generation"], self.generation)
            self.assertEqual(payload["publicAccounts"], accounts)

    def test_early_failed_state_accepts_an_unresolved_reset_schedule(self) -> None:
        with tempfile.TemporaryDirectory(prefix="demo-failed-state-test-") as temporary:
            state_dir = Path(temporary)
            args = mock.Mock(
                state_dir=state_dir,
                candidate="00000000-0000-4000-8000-000000000000",
                next_reset_at="",
            )
            with (
                mock.patch.object(
                    control, "validated_state_dir", return_value=state_dir
                ),
                mock.patch.object(control, "iso_now", return_value=self.transition),
            ):
                control.write_failed_state(args)

            payload = json.loads(
                (state_dir / "status.json").read_text(encoding="utf-8")
            )["data"]
            self.assertEqual(payload["state"], "FAILED")
            self.assertIs(payload["writeLocked"], True)
            self.assertIsNone(payload["nextResetAt"])
            self.assertEqual(
                payload["candidateGeneration"],
                "00000000-0000-4000-8000-000000000000",
            )

    def test_valid_previous_generation_and_reset_time_are_preserved(self) -> None:
        self.assertEqual(
            control.validated_previous_provenance(
                {
                    "generation": self.generation,
                    "lastResetAt": "2026-08-02T02:00:00Z",
                },
                self.transition,
            ),
            (self.generation, "2026-08-02T02:00:00+00:00"),
        )

    def test_invalid_or_future_previous_provenance_is_dropped_as_a_pair(self) -> None:
        invalid_values = (
            {"generation": "not-a-generation", "lastResetAt": "2026-08-02T02:00:00Z"},
            {"generation": self.generation, "lastResetAt": "not-a-time"},
            {"generation": self.generation, "lastResetAt": "2026-08-03T12:00:00+09:00"},
            {"generation": self.generation, "lastResetAt": "2026-08-02T02:00:00"},
            {"generation": self.generation, "lastResetAt": None},
        )
        for previous in invalid_values:
            with self.subTest(previous=previous):
                self.assertEqual(
                    control.validated_previous_provenance(previous, self.transition),
                    (None, None),
                )


class AcceptanceContractTest(unittest.TestCase):
    def test_request_reports_method_and_path_for_transport_timeout(self) -> None:
        with mock.patch.object(
            control.urllib.request,
            "urlopen",
            side_effect=TimeoutError("timed out"),
        ):
            with self.assertRaisesRegex(
                control.ControlError,
                r"HTTP POST /api/v1/contracts transport failure after 8s: timed out",
            ):
                control.request(
                    "http://web:8080",
                    "/api/v1/contracts",
                    method="POST",
                    body=b"{}",
                    content_type="application/json",
                )

    def test_staff_dashboard_separates_reference_and_assignment_scopes(self) -> None:
        control.verify_staff_dashboard_scope(
            {
                "kpi": {"totalCustomers": 49, "monthlySalesActivities": 6},
                "recentCustomers": [{"id": 49}],
                "recentActivities": [{"customerId": 33}],
            },
            customer_id=49,
            reference_customer_count_before=48,
            scoped_activity_count_before=6,
        )

        invalid = (
            {
                "kpi": {"totalCustomers": 48, "monthlySalesActivities": 6},
                "recentCustomers": [{"id": 49}],
                "recentActivities": [],
            },
            {
                "kpi": {"totalCustomers": 49, "monthlySalesActivities": 6},
                "recentCustomers": [{"id": 1}],
                "recentActivities": [],
            },
            {
                "kpi": {"totalCustomers": 49, "monthlySalesActivities": 7},
                "recentCustomers": [{"id": 49}],
                "recentActivities": [],
            },
            {
                "kpi": {"totalCustomers": 49, "monthlySalesActivities": 6},
                "recentCustomers": [{"id": 49}],
                "recentActivities": [{"customerId": 49}],
            },
        )
        for dashboard in invalid:
            with self.subTest(dashboard=dashboard):
                with self.assertRaises(control.ControlError):
                    control.verify_staff_dashboard_scope(dashboard, 49, 48, 6)

    def test_prepare_acceptance_context_snapshots_ready_identity(self) -> None:
        args = mock.Mock(
            marker="demo-it-0123456789abcdef0123456789abcdef",
            base_url="http://web:8080",
            verify_operator_protection=True,
            timeout_seconds=420,
        )
        status = {"generation": "00000000-0000-4000-8000-000000000001"}
        manager = {"id": 1}
        staff = {"id": 2}
        before = dt.datetime.now(tz=control.KST)

        with mock.patch.object(
            control,
            "ready_acceptance_context",
            return_value=(status, "manager-token", "staff-token", manager, staff),
        ) as ready:
            context = control.prepare_acceptance_context(args)

        after = dt.datetime.now(tz=control.KST)
        ready.assert_called_once_with(args)
        self.assertEqual(context.base_url, "http://web:8080")
        self.assertEqual(context.marker, args.marker)
        self.assertEqual(context.generation, status["generation"])
        self.assertEqual(context.manager_token, "manager-token")
        self.assertEqual(context.staff_token, "staff-token")
        self.assertEqual((context.manager_id, context.staff_id), (1, 2))
        self.assertTrue(context.verify_operator_protection)
        self.assertEqual(context.heavy_request_timeout_seconds, 420)
        self.assertLessEqual(before, context.now)
        self.assertLessEqual(context.now, after)
        self.assertEqual(context.today, context.now.date())

    def test_exercise_acceptance_orchestrates_named_stages(self) -> None:
        context = control.AcceptanceContext(
            base_url="http://web:8080",
            marker="demo-it-0123456789abcdef0123456789abcdef",
            generation="00000000-0000-4000-8000-000000000001",
            manager_token="manager-token",
            staff_token="staff-token",
            manager_id=1,
            staff_id=2,
            now=dt.datetime(2026, 8, 11, 12, 0, tzinfo=dt.timezone.utc),
            verify_operator_protection=True,
            heavy_request_timeout_seconds=420,
        )
        preflight = control.AcceptancePreflightResult(48, 20, 3)
        customer = control.CustomerSalesAcceptanceResult(customer_id=11)
        operator = control.OperatorProtectionAcceptanceResult(employee_id=4)
        contract = control.ContractEquipmentAcceptanceResult(12, 13, 14, 15)
        after_service = control.AfterServiceAcceptanceResult(after_service_id=16)
        upload = control.UploadAcceptanceResult(
            excel_customer_id=20,
            sales_contact_id=21,
            board_file_id=22,
            approval_file_id=23,
            expense_file_id=24,
            pending_file_id=25,
            drive_file_id=26,
        )
        staff_workflow = control.StaffWorkflowAcceptanceResult(
            board_id=17,
            expense_id=18,
            approval_id=19,
            attendance_date=dt.date(2026, 8, 11),
        )
        args = mock.sentinel.args

        with (
            mock.patch.object(
                control, "prepare_acceptance_context", return_value=context
            ) as prepare,
            mock.patch.object(
                control, "verify_acceptance_preconditions", return_value=preflight
            ) as verify_preconditions,
            mock.patch.object(
                control, "exercise_customer_sales_acceptance", return_value=customer
            ) as exercise_customer_sales,
            mock.patch.object(
                control, "verify_operator_protection_acceptance", return_value=operator
            ) as verify_operator,
            mock.patch.object(
                control, "exercise_contract_equipment_acceptance", return_value=contract
            ) as exercise_contract_equipment,
            mock.patch.object(
                control, "exercise_after_service_acceptance", return_value=after_service
            ) as exercise_after_service,
            mock.patch.object(
                control, "exercise_upload_acceptance", return_value=upload
            ) as exercise_upload,
            mock.patch.object(
                control, "exercise_staff_workflow_acceptance", return_value=staff_workflow
            ) as exercise_staff_workflow,
            mock.patch("builtins.print") as print_result,
        ):
            control.exercise_acceptance(args)

        prepare.assert_called_once_with(args)
        verify_preconditions.assert_called_once_with(context)
        exercise_customer_sales.assert_called_once_with(context, preflight)
        verify_operator.assert_called_once_with(context)
        exercise_contract_equipment.assert_called_once_with(
            context, preflight, customer, operator
        )
        exercise_after_service.assert_called_once_with(context, customer, contract)
        exercise_upload.assert_called_once_with(context)
        exercise_staff_workflow.assert_called_once_with(context, upload)
        print_result.assert_called_once_with(
            control.AcceptanceExerciseResult(
                marker=context.marker,
                generation=context.generation,
                manager_id=context.manager_id,
                staff_id=context.staff_id,
                customer=customer,
                contract=contract,
                after_service=after_service,
                upload=upload,
                staff_workflow=staff_workflow,
            ).summary()
        )

    def test_exercise_summary_preserves_reset_evidence_keys(self) -> None:
        result = control.AcceptanceExerciseResult(
            marker="demo-it-0123456789abcdef0123456789abcdef",
            generation="00000000-0000-4000-8000-000000000001",
            manager_id=1,
            staff_id=2,
            customer=control.CustomerSalesAcceptanceResult(customer_id=11),
            contract=control.ContractEquipmentAcceptanceResult(
                contract_id=12,
                equipment_id=13,
                settled_contract_id=14,
                settled_equipment_id=15,
            ),
            after_service=control.AfterServiceAcceptanceResult(after_service_id=16),
            upload=control.UploadAcceptanceResult(
                excel_customer_id=20,
                sales_contact_id=21,
                board_file_id=22,
                approval_file_id=23,
                expense_file_id=24,
                pending_file_id=25,
                drive_file_id=26,
            ),
            staff_workflow=control.StaffWorkflowAcceptanceResult(
                board_id=17,
                expense_id=18,
                approval_id=19,
                attendance_date=dt.date(2026, 8, 11),
            ),
        )

        self.assertEqual(
            result.summary(),
            "acceptance-exercise-ok: "
            "marker=demo-it-0123456789abcdef0123456789abcdef "
            "generation=00000000-0000-4000-8000-000000000001 customer=11 "
            "contract=12 equipment=13 settledContract=14 settledEquipment=15 "
            "afterService=16 excelCustomer=20 salesContact=21 boardFile=22 "
            "approvalFile=23 expenseFile=24 pendingFile=25 driveFile=26 "
            "board=17 expense=18 approval=19 attendanceDate=2026-08-11 "
            "managerId=1 staffId=2",
        )

    def test_staff_workflow_orchestrates_owner_specific_file_paths(self) -> None:
        context = mock.sentinel.context
        upload = mock.sentinel.upload
        board = control.BoardFileAcceptanceResult(board_id=11)
        approval = control.ApprovalFileAcceptanceResult(approval_id=12)
        expense = control.ExpenseFileAcceptanceResult(expense_id=13)
        attendance_date = dt.date(2026, 8, 13)

        with (
            mock.patch.object(
                control, "exercise_board_file_acceptance", return_value=board
            ) as board_flow,
            mock.patch.object(
                control, "exercise_approval_file_acceptance", return_value=approval
            ) as approval_flow,
            mock.patch.object(
                control, "exercise_expense_file_acceptance", return_value=expense
            ) as expense_flow,
            mock.patch.object(
                control, "exercise_attendance_acceptance", return_value=attendance_date
            ) as attendance_flow,
        ):
            result = control.exercise_staff_workflow_acceptance(context, upload)

        board_flow.assert_called_once_with(context, upload)
        approval_flow.assert_called_once_with(context, upload, board)
        expense_flow.assert_called_once_with(context, upload)
        attendance_flow.assert_called_once_with(context)
        self.assertEqual(
            result,
            control.StaffWorkflowAcceptanceResult(11, 13, 12, attendance_date),
        )

    def test_upload_acceptance_orchestrates_named_surfaces(self) -> None:
        context = mock.sentinel.context
        excel = control.ExcelUploadAcceptanceResult(20, 21)
        generic = control.GenericFileUploadAcceptanceResult(22, 23, 24, 25)
        drive = control.DriveFileUploadAcceptanceResult(26)

        with (
            mock.patch.object(
                control, "exercise_excel_upload_acceptance", return_value=excel
            ) as excel_flow,
            mock.patch.object(
                control, "exercise_generic_file_upload_acceptance", return_value=generic
            ) as generic_flow,
            mock.patch.object(
                control, "exercise_drive_file_upload_acceptance", return_value=drive
            ) as drive_flow,
        ):
            result = control.exercise_upload_acceptance(context)

        excel_flow.assert_called_once_with(context)
        generic_flow.assert_called_once_with(context)
        drive_flow.assert_called_once_with(context)
        self.assertEqual(
            result,
            control.UploadAcceptanceResult(20, 21, 22, 23, 24, 25, 26),
        )

    def test_excel_acceptance_propagates_heavy_request_timeout(self) -> None:
        marker = "demo-it-0123456789abcdef0123456789abcdef"
        context = mock.Mock(
            marker=marker,
            base_url="http://web:8080",
            manager_token="manager-token",
            today=dt.date(2026, 8, 13),
            heavy_request_timeout_seconds=420,
        )
        upload_result = {
            "status": 200,
            "data": {
                "totalRows": 1,
                "successRows": 1,
                "failedRows": 0,
                "errors": [],
            },
        }
        customer_name = f"{marker}-excel"
        contact_name = f"{marker}-xls-ct"
        page = lambda row: {
            "status": 200,
            "data": {"content": [row], "totalElements": 1},
        }

        with (
            mock.patch.object(
                control,
                "request",
                side_effect=((b"customer-template", None), (b"contact-template", None)),
            ) as template_request,
            mock.patch.object(
                control,
                "fill_xlsx_template",
                side_effect=(b"customer-workbook", b"contact-workbook"),
            ),
            mock.patch.object(
                control,
                "request_file_upload",
                side_effect=(upload_result, upload_result),
            ) as upload_request,
            mock.patch.object(
                control,
                "request_json",
                side_effect=(
                    page({"id": 20, "name": customer_name}),
                    page({"id": 21, "name": contact_name}),
                ),
            ),
        ):
            result = control.exercise_excel_upload_acceptance(context)

        self.assertEqual(result, control.ExcelUploadAcceptanceResult(20, 21))
        self.assertEqual(
            [call.kwargs["timeout"] for call in template_request.call_args_list],
            [420, 420],
        )
        self.assertEqual(
            [call.kwargs["timeout"] for call in upload_request.call_args_list],
            [420, 420],
        )

    def test_multipart_file_body_preserves_binary_payload_and_rejects_header_injection(self) -> None:
        payload = b"\x00acceptance\xff\r\n"
        body, content_type = control.multipart_file_body(
            "file", "evidence.txt", "text/plain", payload
        )
        boundary = content_type.removeprefix("multipart/form-data; boundary=")

        self.assertTrue(boundary.startswith("simple-erp-demo-"))
        self.assertIn(b'name="file"; filename="evidence.txt"', body)
        self.assertIn(b"Content-Type: text/plain\r\n\r\n" + payload, body)
        self.assertTrue(body.endswith(f"\r\n--{boundary}--\r\n".encode("ascii")))
        for field_name, filename, file_content_type in (
            ("file\r\nInjected", "evidence.txt", "text/plain"),
            ("file", 'bad".txt', "text/plain"),
            ("file", "evidence.txt", "text/plain\nInjected"),
        ):
            with self.subTest(
                field_name=field_name,
                filename=filename,
                content_type=file_content_type,
            ):
                with self.assertRaises(control.ControlError):
                    control.multipart_file_body(
                        field_name, filename, file_content_type, payload
                    )

    def test_fill_xlsx_template_updates_only_the_first_data_row_contract(self) -> None:
        workbook = control.fill_xlsx_template(generator.make_xlsx(7), ("첫째", "둘째"))
        with zipfile.ZipFile(io.BytesIO(workbook)) as archive:
            worksheet = ET.fromstring(archive.read("xl/worksheets/sheet1.xml"))
        namespace = {"x": control.XLSX_MAIN_NAMESPACE}
        row = worksheet.find(".//x:sheetData/x:row[@r='2']", namespace)

        self.assertIsNotNone(row)
        self.assertEqual(
            [cell.findtext("x:is/x:t", namespaces=namespace) for cell in row],
            ["첫째", "둘째"],
        )
        with self.assertRaises(control.ControlError):
            control.fill_xlsx_template(generator.make_xlsx(7), ("한 열뿐",))

    def test_acceptance_attachment_matches_the_demo_file_limit_exactly(self) -> None:
        marker = "demo-it-0123456789abcdef0123456789abcdef"
        payload = control.acceptance_attachment_payload(marker)

        self.assertEqual(len(payload), control.MAX_UPLOAD_FILE_SIZE_BYTES)
        self.assertEqual(len(payload), control.ACCEPTANCE_ATTACHMENT_SIZE_BYTES)
        self.assertGreater(len(payload), 1_000_000)
        self.assertTrue(payload.startswith(f"{marker}-attachment-body\n".encode("utf-8")))

    def test_attachment_boundary_requests_plus_one_then_exact_30_mib(self) -> None:
        context = mock.Mock(
            marker="demo-it-0123456789abcdef0123456789abcdef",
            base_url="http://web:8080",
            staff_token="staff-token",
            heavy_request_timeout_seconds=420,
        )
        exact_file = {
            "id": 17,
            "originalName": f"{context.marker}-attachment.txt",
            "contentType": "text/plain",
            "size": control.MAX_UPLOAD_FILE_SIZE_BYTES,
            "uploaderId": 2,
        }
        with mock.patch.object(
            control,
            "request_file_upload",
            side_effect=(
                {"status": 413, "message": "file is too large"},
                {"status": 200, "message": "OK", "data": exact_file},
            ),
        ) as upload:
            result = control.request_acceptance_attachment_at_size_boundary(
                context,
                "/api/v1/files",
                exact_file["originalName"],
            )

        self.assertEqual(result, exact_file)
        self.assertEqual(upload.call_count, 2)
        oversized_call, exact_call = upload.call_args_list
        oversized_payload = oversized_call.kwargs["payload"]
        exact_payload = exact_call.kwargs["payload"]
        self.assertEqual(
            len(oversized_payload), control.MAX_UPLOAD_FILE_SIZE_BYTES + 1
        )
        self.assertEqual(len(exact_payload), control.MAX_UPLOAD_FILE_SIZE_BYTES)
        self.assertEqual(oversized_payload[-1:], b"x")
        self.assertEqual(oversized_payload[:64], exact_payload[:64])
        self.assertEqual(oversized_call.kwargs["expected_status"], 413)
        self.assertEqual(oversized_call.kwargs["timeout"], 420)
        self.assertEqual(exact_call.kwargs["timeout"], 420)
        self.assertNotIn("expected_status", exact_call.kwargs)

    def test_acceptance_file_payloads_keep_each_owner_body_distinct(self) -> None:
        marker = "demo-it-0123456789abcdef0123456789abcdef"

        payloads = {
            kind: control.acceptance_file_payload(marker, kind)
            for kind in ("board", "approval", "expense", "pending", "drive")
        }

        self.assertEqual(len(payloads["board"]), control.MAX_UPLOAD_FILE_SIZE_BYTES)
        self.assertEqual(len(set(payloads.values())), 5)
        for kind in ("approval", "expense", "pending", "drive"):
            self.assertEqual(payloads[kind], f"{marker}-{kind}-body\n".encode())
        with self.assertRaises(control.ControlError):
            control.acceptance_file_payload(marker, "unknown")

    def test_download_contract_checks_bytes_length_type_and_filename(self) -> None:
        payload = b"owner-specific-body\n"
        name = "owner evidence.txt"

        def respond(*_args, **kwargs):
            kwargs["response_headers"].update(
                {
                    "content-type": "text/plain;charset=UTF-8",
                    "content-length": str(len(payload)),
                    "content-disposition": "attachment; filename=owner+evidence.txt",
                }
            )
            return payload, None

        with mock.patch.object(control, "request", side_effect=respond) as request:
            control.require_download_contract(
                "http://web:8080",
                "/api/v1/owners/1/files/2",
                token="token",
                expected_name=name,
                expected_content_type="text/plain",
                expected_payload=payload,
            )

        request.assert_called_once()
        with mock.patch.object(
            control,
            "request",
            side_effect=lambda *_args, **kwargs: (b"wrong", None),
        ):
            with self.assertRaises(control.ControlError):
                control.require_download_contract(
                    "http://web:8080",
                    "/api/v1/owners/1/files/2",
                    token="token",
                    expected_name=name,
                    expected_content_type="text/plain",
                    expected_payload=payload,
                )

    def test_acceptance_file_verifier_pins_generation_path_and_bytes(self) -> None:
        marker = "demo-it-0123456789abcdef0123456789abcdef"
        generation = "00000000-0000-4000-8000-000000000001"
        stored_name = "00000000-0000-4000-8000-000000000002"
        with tempfile.TemporaryDirectory() as directory:
            files_root = Path(directory)
            target = files_root / "generations" / generation / "2026" / "08" / stored_name
            target.parent.mkdir(parents=True)
            target.write_bytes(control.acceptance_file_payload(marker, "expense"))
            args = mock.Mock(
                files_root=Path("/files"),
                generation=generation,
                marker=marker,
                relative_path=f"2026/08/{stored_name}",
                kind="expense",
            )

            with mock.patch.object(
                control, "validated_files_root", return_value=files_root
            ):
                control.verify_acceptance_file(args)
                target.write_bytes(b"wrong-owner-body")
                with self.assertRaises(control.ControlError):
                    control.verify_acceptance_file(args)
                args.relative_path = f"../08/{stored_name}"
                with self.assertRaises(control.ControlError):
                    control.verify_acceptance_file(args)

    def test_marker_and_page_contracts_are_fail_closed(self) -> None:
        marker = "demo-it-0123456789abcdef0123456789abcdef"
        self.assertEqual(control.validate_acceptance_marker(marker), marker)
        self.assertEqual(
            control.validate_acceptance_base_url("http://web:8080"), "http://web:8080"
        )
        for base_url in (
            "http://web",
            "http://web:8080/",
            "http://localhost",
            "https://demo.example.com",
        ):
            with self.subTest(base_url=base_url):
                with self.assertRaises(control.ControlError):
                    control.validate_acceptance_base_url(base_url)
        for invalid in (
            "DEMO-IT-0123456789abcdef0123456789abcdef",
            "demo-it-short",
            "demo-it-0123456789abcdef0123456789abcdeg",
        ):
            with self.subTest(invalid=invalid):
                with self.assertRaises(control.ControlError):
                    control.validate_acceptance_marker(invalid)

        page = {
            "status": 200,
            "message": "OK",
            "data": {"content": [], "totalElements": 0},
        }
        self.assertEqual(control.require_page_data(page, "/test"), page["data"])
        for total_elements in (True, -1, "0", None):
            malformed = copy.deepcopy(page)
            malformed["data"]["totalElements"] = total_elements
            with self.subTest(total_elements=total_elements):
                with self.assertRaises(control.ControlError):
                    control.require_page_data(malformed, "/test")

        self.assertEqual(control.require_positive_int(1, "test ID"), 1)
        for invalid_id in (True, False, 0, -1, 1.0, "1", None):
            with self.subTest(invalid_id=invalid_id):
                with self.assertRaises(control.ControlError):
                    control.require_positive_int(invalid_id, "test ID")

        attendance = {
            "status": 200,
            "message": "OK",
            "data": [{"workDate": "2026-08-11"}],
        }
        self.assertEqual(
            control.attendance_dates(attendance, "/attendance"),
            {dt.date(2026, 8, 11)},
        )
        for malformed_rows in (
            [True],
            [{}],
            [{"workDate": True}],
            [{"workDate": "2026-02-30"}],
        ):
            malformed_attendance = copy.deepcopy(attendance)
            malformed_attendance["data"] = malformed_rows
            with self.subTest(malformed_rows=malformed_rows):
                with self.assertRaises(control.ControlError):
                    control.attendance_dates(malformed_attendance, "/attendance")

        error = {"status": 403, "message": "denied", "code": "DEMO_PROTECTED_RESOURCE"}
        self.assertEqual(
            control.require_error_response(
                error,
                403,
                "/protected",
                expected_code="DEMO_PROTECTED_RESOURCE",
                expected_message="denied",
            ),
            error,
        )
        with self.assertRaises(control.ControlError):
            control.require_error_response({"status": 403, "message": ""}, 403, "/test")
        with self.assertRaises(control.ControlError):
            control.require_error_response(
                error,
                403,
                "/protected",
                expected_message="different failure",
            )


class ArtifactCleanupTest(unittest.TestCase):
    def setUp(self) -> None:
        self.temporary = tempfile.TemporaryDirectory(prefix="demo-cleanup-test-")
        self.root = Path(self.temporary.name)
        self.files = self.root / "files"
        self.state = self.root / "state"
        self.work = self.root / "work"
        self.logs = self.root / "logs"
        self.generations = self.files / "generations"
        for directory in (self.generations, self.state, self.work, self.logs):
            directory.mkdir(parents=True, exist_ok=True)

    def tearDown(self) -> None:
        self.temporary.cleanup()

    @staticmethod
    def generation(label: str) -> str:
        return str(uuid.uuid5(uuid.NAMESPACE_URL, f"simple-erp-cleanup:{label}"))

    def create_generation(self, generation: str, modified_ns: int = 1) -> Path:
        directory = self.generations / generation
        directory.mkdir()
        (directory / ".generation.json").write_text("{}\n", encoding="utf-8")
        os.utime(directory, ns=(modified_ns, modified_ns))
        return directory

    def point_current(self, generation: str) -> None:
        current = self.files / "current"
        if current.is_symlink():
            current.unlink()
        try:
            os.symlink(
                f"generations/{generation}",
                current,
                target_is_directory=True,
            )
        except OSError as error:
            self.skipTest(f"symlink 권한이 없어 cleanup 계약을 검사할 수 없음: {error}")

    def write_state(
        self,
        state: str,
        generation: str | None,
        candidate: str | None,
    ) -> None:
        payload = {
            "status": 200,
            "message": "test",
            "data": {
                "state": state,
                "generation": generation,
                "candidateGeneration": candidate,
                "writeLocked": state != "READY",
            },
        }
        (self.state / "status.json").write_text(
            json.dumps(payload) + "\n", encoding="utf-8"
        )

    def create_work(self, generation: str, modified_ns: int) -> Path:
        directory = self.work / generation
        directory.mkdir()
        (directory / "stored-files.tsv").write_text("test\n", encoding="utf-8")
        os.utime(directory, ns=(modified_ns, modified_ns))
        return directory

    def create_log(self, name: str, modified_ns: int) -> Path:
        path = self.logs / name
        path.write_text("test\n", encoding="utf-8")
        os.utime(path, ns=(modified_ns, modified_ns))
        return path

    def test_pre_reset_cleanup_accepts_an_empty_first_boot_volume(self) -> None:
        self.generations.rmdir()
        plan = control.build_cleanup_plan(
            "pre-reset", self.files, self.state, self.work, self.logs
        )
        self.assertEqual(plan.targets, ())
        self.assertEqual(plan.preserved_generations, ())

    def test_success_cleanup_keeps_only_current_and_removes_uploaded_bytes(self) -> None:
        first, second, third, fourth = (
            self.generation(label) for label in ("first", "second", "third", "fourth")
        )
        for index, generation in enumerate((first, second, third), start=1):
            self.create_generation(generation, index)
        self.point_current(third)
        self.write_state("VERIFYING", second, third)
        self.create_work(third, 3)
        uploaded = self.generations / second / "2026" / "08" / "visitor-upload.xlsx"
        uploaded.parent.mkdir(parents=True)
        uploaded.write_bytes(b"visitor upload")

        plan = control.build_cleanup_plan(
            "post-success", self.files, self.state, self.work, self.logs, third
        )
        control.execute_cleanup_plan(plan)
        self.assertEqual(
            {path.name for path in self.generations.iterdir()}, {third}
        )
        self.assertFalse(uploaded.exists())
        self.assertFalse((self.work / third).exists())

        self.create_generation(fourth, 4)
        self.point_current(fourth)
        self.write_state("VERIFYING", third, fourth)
        self.create_work(fourth, 4)
        plan = control.build_cleanup_plan(
            "post-success", self.files, self.state, self.work, self.logs, fourth
        )
        control.execute_cleanup_plan(plan)
        self.assertEqual(
            {path.name for path in self.generations.iterdir()}, {fourth}
        )
        self.assertFalse((self.work / fourth).exists())

    def test_generation_absence_check_proves_reset_removed_the_previous_files(self) -> None:
        removed = self.generation("removed")
        args = mock.Mock(files_root=self.files, generation=removed)
        with (
            mock.patch.object(control, "validated_files_root", return_value=self.files),
            mock.patch("builtins.print") as printed,
        ):
            control.assert_generation_absent(args)
        printed.assert_called_once_with(f"files-generation-absent: generation={removed}")

        self.create_generation(removed)
        with (
            mock.patch.object(control, "validated_files_root", return_value=self.files),
            self.assertRaises(control.ControlError),
        ):
            control.assert_generation_absent(args)

    def test_pre_reset_removes_only_exact_interrupted_file_and_state_artifacts(self) -> None:
        current_generation = self.generation("interrupted-current")
        self.create_generation(current_generation)
        self.point_current(current_generation)
        self.write_state("READY", current_generation, None)
        active_state = (self.state / "status.json").read_bytes()
        (self.state / "preflight.json").write_text("active\n", encoding="utf-8")

        stale_generations = [self.generation(f"stale-{index}") for index in range(3)]
        for generation in stale_generations:
            staging = self.files / f".staging-{generation}"
            staging.mkdir()
            (staging / "partial-object").write_text("partial\n", encoding="utf-8")
        outside = self.root / "outside-target"
        outside.mkdir()
        try:
            for generation in stale_generations:
                os.symlink(
                    outside,
                    self.files / f".current-{generation}",
                    target_is_directory=True,
                )
        except OSError as error:
            self.skipTest(f"symlink 권한이 없어 cleanup 계약을 검사할 수 없음: {error}")
        old_state_temp = self.state / ".status.json.1.tmp"
        fixed_state_temp = self.state / ".preflight.json.tmp"
        old_state_temp.write_text("partial\n", encoding="utf-8")
        fixed_state_temp.write_text("partial\n", encoding="utf-8")

        plan = control.build_cleanup_plan(
            "pre-reset", self.files, self.state, self.work, self.logs
        )
        control.execute_cleanup_plan(plan)

        self.assertTrue((self.files / "current").is_symlink())
        self.assertTrue((self.generations / current_generation).is_dir())
        self.assertTrue(outside.is_dir())
        for generation in stale_generations:
            self.assertFalse((self.files / f".staging-{generation}").exists())
            self.assertFalse((self.files / f".current-{generation}").is_symlink())
        self.assertFalse(old_state_temp.exists())
        self.assertFalse(fixed_state_temp.exists())
        self.assertEqual((self.state / "status.json").read_bytes(), active_state)
        self.assertEqual((self.state / "preflight.json").read_text(encoding="utf-8"), "active\n")

    def test_unknown_file_root_and_wrong_transient_types_fail_without_deletion(self) -> None:
        current_generation = self.generation("root-current")
        self.create_generation(current_generation)
        self.point_current(current_generation)
        unknown = self.files / "operator-note"
        unknown.write_text("keep\n", encoding="utf-8")
        with self.assertRaises(control.ControlError):
            control.build_cleanup_plan(
                "pre-reset", self.files, self.state, self.work, self.logs
            )
        self.assertTrue(unknown.is_file())
        unknown.unlink()

        outside = self.root / "outside-staging"
        outside.mkdir()
        malicious = self.files / f".staging-{self.generation('malicious-staging')}"
        try:
            os.symlink(outside, malicious, target_is_directory=True)
        except OSError as error:
            self.skipTest(f"symlink 권한이 없어 cleanup 계약을 검사할 수 없음: {error}")
        with self.assertRaises(control.ControlError):
            control.build_cleanup_plan(
                "pre-reset", self.files, self.state, self.work, self.logs
            )
        self.assertTrue(malicious.is_symlink())
        self.assertTrue(outside.is_dir())

    def test_atomic_state_writer_recovers_fixed_regular_temp_but_rejects_symlink(self) -> None:
        target = self.state / "status.json"
        temporary = self.state / ".status.json.tmp"
        temporary.write_text("interrupted\n", encoding="utf-8")
        control.atomic_json_write(target, {"status": 200, "data": {"state": "READY"}})
        self.assertFalse(temporary.exists())
        self.assertEqual(json.loads(target.read_text(encoding="utf-8"))["status"], 200)

        target.unlink()
        outside = self.root / "outside-state-temp"
        outside.write_text("keep\n", encoding="utf-8")
        try:
            os.symlink(outside, temporary)
        except OSError as error:
            self.skipTest(f"symlink 권한이 없어 cleanup 계약을 검사할 수 없음: {error}")
        with self.assertRaises(control.ControlError):
            control.atomic_json_write(target, {"status": 200})
        with self.assertRaises(control.ControlError):
            control.build_cleanup_plan(
                "pre-reset", self.files, self.state, self.work, self.logs
            )
        self.assertTrue(temporary.is_symlink())
        self.assertEqual(outside.read_text(encoding="utf-8"), "keep\n")

    def test_all_pre_prune_stages_fit_the_bounded_failure_log_contract(self) -> None:
        reset_lines = (ROOT / "scripts/demo/reset-demo.sh").read_text(
            encoding="utf-8"
        ).splitlines()
        gate_index = reset_lines.index('pre_prune_completed="true"')
        stages = {
            line.removeprefix('failure_stage="').removesuffix('"')
            for line in reset_lines[:gate_index]
            if line.startswith('failure_stage="') and line.endswith('"')
        }
        self.assertEqual(
            stages,
            {
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
        )

        candidate = self.generation("early-failure")
        for stage in sorted(stages):
            with self.subTest(stage=stage):
                parsed = control.build_parser().parse_args(
                    [
                        "write-control-plane-failure-log",
                        "--logs-root",
                        "/logs",
                        "--candidate",
                        candidate,
                        "--stage",
                        stage,
                        "--line",
                        "100",
                        "--exit-code",
                        "1",
                        "--failed-state-published",
                        "false",
                    ]
                )
                self.assertEqual(parsed.stage, stage)
                control.write_control_plane_failure_log_file(
                    self.logs,
                    candidate,
                    stage,
                    100,
                    1,
                    "false",
                )
                payload = (
                    self.logs / control.RETENTION_FAILURE_LOG_NAME
                ).read_text(encoding="utf-8")
                self.assertIn(f"stage={stage}\n", payload)

        for invalid_stage in ("", "Image-Contract", "../image-contract", "image contract"):
            with self.subTest(invalid_stage=invalid_stage):
                with self.assertRaises(control.ControlError):
                    control.write_control_plane_failure_log_file(
                        self.logs,
                        candidate,
                        invalid_stage,
                        100,
                        1,
                        "false",
                    )

    def test_persistent_pre_prune_failures_replace_one_bounded_control_log(self) -> None:
        current_generation = self.generation("persistent-current")
        self.create_generation(current_generation)
        self.point_current(current_generation)
        forensic_log = self.create_log(f"backend-{current_generation}.log", 1)
        invalid_generation = self.generations / "persistent-invalid-generation"
        invalid_generation.mkdir()

        candidates = [self.generation(f"attempt-{index}") for index in range(8)]
        for index, candidate in enumerate(candidates, start=1):
            with self.assertRaises(control.ControlError):
                control.build_cleanup_plan(
                    "pre-reset", self.files, self.state, self.work, self.logs
                )
            control.write_control_plane_failure_log_file(
                self.logs,
                candidate,
                "retention-pre-prune",
                100 + index,
                1,
                "true",
            )
        failure_log = self.logs / control.RETENTION_FAILURE_LOG_NAME
        self.assertEqual({path.name for path in self.logs.iterdir()}, {forensic_log.name, failure_log.name})
        payload = failure_log.read_bytes()
        self.assertLessEqual(len(payload), control.MAX_RETENTION_FAILURE_LOG_BYTES)
        self.assertEqual(failure_log.stat().st_mode & 0o777, 0o600)
        self.assertIn(f"candidate={candidates[-1]}".encode(), payload)
        self.assertNotIn(f"candidate={candidates[0]}".encode(), payload)
        self.assertFalse((self.logs / control.RETENTION_FAILURE_TEMP_NAME).exists())

        invalid_generation.rmdir()
        linked_work = self.work / self.generation("persistent-linked-work")
        try:
            os.symlink(self.root, linked_work, target_is_directory=True)
        except OSError as error:
            self.skipTest(f"symlink 권한이 없어 cleanup 계약을 검사할 수 없음: {error}")
        for index, candidate in enumerate(candidates, start=1):
            with self.assertRaises(control.ControlError):
                control.build_cleanup_plan(
                    "pre-reset", self.files, self.state, self.work, self.logs
                )
            control.write_control_plane_failure_log_file(
                self.logs,
                candidate,
                "retention-pre-prune",
                200 + index,
                1,
                "false",
            )
        self.assertEqual({path.name for path in self.logs.iterdir()}, {forensic_log.name, failure_log.name})
        payload = failure_log.read_bytes()
        self.assertIn(b"failedStatePublished=false", payload)
        self.assertIn(f"candidate={candidates[-1]}".encode(), payload)
        self.assertTrue(linked_work.is_symlink())

        failure_log.unlink()
        outside_log = self.root / "outside-control-log"
        outside_log.write_text("keep\n", encoding="utf-8")
        os.symlink(outside_log, failure_log)
        with self.assertRaises(control.ControlError):
            control.write_control_plane_failure_log_file(
                self.logs,
                candidates[-1],
                "retention-pre-prune",
                999,
                1,
                "false",
            )
        with self.assertRaises(control.ControlError):
            control.build_cleanup_plan(
                "pre-reset", self.files, self.state, self.work, self.logs
            )
        self.assertTrue(failure_log.is_symlink())
        self.assertEqual(outside_log.read_text(encoding="utf-8"), "keep\n")

    def test_unknown_and_symlink_artifacts_are_never_deleted(self) -> None:
        current_generation = self.generation("current")
        self.create_generation(current_generation)
        self.point_current(current_generation)
        unknown_generation = self.generations / "operator-note"
        unknown_generation.mkdir()

        with self.assertRaises(control.ControlError):
            control.build_cleanup_plan(
                "pre-reset", self.files, self.state, self.work, self.logs
            )
        self.assertTrue(unknown_generation.is_dir())
        self.assertTrue((self.generations / current_generation).is_dir())

        unknown_generation.rmdir()
        linked_generation = self.generation("linked")
        try:
            os.symlink(
                current_generation,
                self.generations / linked_generation,
                target_is_directory=True,
            )
            os.symlink(
                self.root,
                self.work / "operator-link",
                target_is_directory=True,
            )
            os.symlink(self.root / "missing", self.logs / "operator-link")
        except OSError as error:
            self.skipTest(f"symlink 권한이 없어 cleanup 계약을 검사할 수 없음: {error}")
        with self.assertRaises(control.ControlError):
            control.build_cleanup_plan(
                "pre-reset", self.files, self.state, self.work, self.logs
            )
        self.assertTrue((self.generations / linked_generation).is_symlink())
        self.assertTrue((self.work / "operator-link").is_symlink())
        self.assertTrue((self.logs / "operator-link").is_symlink())

        (self.generations / linked_generation).unlink()
        recognized_log = self.logs / f"backend-{self.generation('linked-log')}.log"
        os.symlink(self.root / "missing-log", recognized_log)
        with self.assertRaises(control.ControlError):
            control.build_cleanup_plan(
                "pre-reset", self.files, self.state, self.work, self.logs
            )
        self.assertTrue(recognized_log.is_symlink())

    def test_pre_reset_cleanup_bounds_failed_generations_work_and_logs(self) -> None:
        current_generation = self.generation("current")
        failed_generations = [self.generation(f"failed-{index}") for index in range(7)]
        self.create_generation(current_generation, 100)
        for index, generation in enumerate(failed_generations, start=1):
            self.create_generation(generation, index)
            self.create_work(generation, index)
        self.point_current(current_generation)
        forced_failure = failed_generations[0]
        self.write_state("FAILED", current_generation, forced_failure)

        log_names = []
        for index in range(25):
            generation = self.generation(f"log-{index}")
            name = f"{'preflight' if index % 2 == 0 else 'backend'}-{generation}.log"
            self.create_log(name, index + 1)
            log_names.append(name)
        forced_log = f"backend-{forced_failure}.log"
        self.create_log(forced_log, 1)
        unknown_work = self.work / "ci"
        unknown_work.mkdir()
        unknown_log = self.logs / "operator-note.txt"
        unknown_log.write_text("keep\n", encoding="utf-8")

        plan = control.build_cleanup_plan(
            "pre-reset", self.files, self.state, self.work, self.logs
        )
        control.execute_cleanup_plan(plan)

        remaining_generations = {path.name for path in self.generations.iterdir()}
        self.assertEqual(len(remaining_generations), 5)
        self.assertIn(current_generation, remaining_generations)
        self.assertIn(forced_failure, remaining_generations)
        remaining_work = {
            path.name
            for path in self.work.iterdir()
            if control.GENERATION_PATTERN.fullmatch(path.name)
        }
        self.assertEqual(len(remaining_work), control.FAILED_WORK_RETENTION)
        self.assertIn(forced_failure, remaining_work)
        remaining_logs = {
            path.name
            for path in self.logs.iterdir()
            if control.RECOGNIZED_LOG_PATTERN.fullmatch(path.name)
        }
        self.assertEqual(len(remaining_logs), control.RECOGNIZED_LOG_RETENTION)
        self.assertIn(forced_log, remaining_logs)
        self.assertTrue(unknown_work.is_dir())
        self.assertTrue(unknown_log.is_file())


if __name__ == "__main__":
    unittest.main()
