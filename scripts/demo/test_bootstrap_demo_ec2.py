#!/usr/bin/env python3
"""Static safety contract for the dedicated EC2 demo bootstrap."""

from pathlib import Path
import re
import unittest


ROOT = Path(__file__).resolve().parents[2]
BOOTSTRAP = ROOT / "scripts/demo/bootstrap-demo-ec2.sh"
SSHD_HARDENING = ROOT / "ops/sshd/00-simple-erp-demo-hardening.conf"


class DemoEc2BootstrapContractTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        cls.script = BOOTSTRAP.read_text(encoding="utf-8")

    def test_release_assets_are_exact_and_checksum_pinned(self) -> None:
        expected = (
            'readonly COMPOSE_VERSION="5.4.0"',
            'readonly COMPOSE_SHA256="fc5d1371f1ec7987e703da94ede49af3fbfb240b83f22991a98511de7bc4b93b"',
            'readonly BUILDX_VERSION="0.36.1"',
            'readonly BUILDX_SHA256="5d0cafd9d16afe1a0f0d9529885344ace2cc99efdd531b6c783c5455a6001569"',
            "sha256sum --check --status",
            "--proto '=https' --proto-redir '=https' --tlsv1.2",
        )
        for token in expected:
            with self.subTest(token=token):
                self.assertIn(token, self.script)
        self.assertNotIn("releases/latest", self.script)
        self.assertNotRegex(self.script, r"github\.com/.+/releases/download/latest/")

    def test_host_and_privilege_boundaries_are_explicit(self) -> None:
        expected = (
            '[[ "${EUID}" == "0" ]]',
            '[[ "$(uname -m)" == "aarch64" ]]',
            '[[ "${ID:-}" == "amzn" && "${VERSION_ID:-}" == "2023" ]]',
            "gpasswd -d ec2-user docker",
            'readonly PROJECT_ROOT="/opt/simple-erp-demo"',
            'readonly SECRET_ROOT="/etc/simple-erp-demo"',
            'install -d -o root -g root -m 0755 "${PROJECT_ROOT}"',
            'install -d -o root -g root -m 0700 "${SECRET_ROOT}"',
        )
        for token in expected:
            with self.subTest(token=token):
                self.assertIn(token, self.script)
        self.assertNotRegex(self.script, r"usermod\s+-aG\s+docker")

    def test_required_packages_services_swap_and_reboot_gate_are_present(self) -> None:
        for package in ("docker", "git", "jq", "util-linux"):
            with self.subTest(package=package):
                self.assertRegex(
                    self.script,
                    rf"dnf[\s\S]+install[\s\S]+\b{re.escape(package)}\b",
                )
        self.assertIn("curl-minimal", self.script)
        self.assertNotRegex(self.script, r"\bcoreutils\s+curl\s+dnf-utils\b")
        self.assertNotRegex(
            self.script,
            r"(?m)^\s+ca-certificates\s+coreutils(?:\s|$)",
        )
        expected = (
            "systemctl enable --now docker",
            'readonly COMPOSE_MIN_VERSION="2.24.4"',
            'readonly SWAP_BYTES="1073741824"',
            "fallocate -l 1G",
            "vm.swappiness=10",
            "dnf -q needs-restarting -r",
            'readonly REBOOT_MARKER="/run/simple-erp-demo-bootstrap-reboot-required"',
            "exit 194",
            "BOOTSTRAP_READY",
        )
        for token in expected:
            with self.subTest(token=token):
                self.assertIn(token, self.script)

    def test_script_does_not_enable_shell_trace_or_create_deployment_secrets(self) -> None:
        self.assertNotRegex(self.script, r"set\s+-[^\n]*x")
        self.assertNotIn("JWT_SECRET=", self.script)
        self.assertNotIn("DB_PASSWORD=", self.script)
        self.assertNotIn("APP_ADMIN_PASSWORD=", self.script)

    def test_sshd_hardening_keeps_key_access_and_removes_remote_privilege_surface(self) -> None:
        hardening = SSHD_HARDENING.read_text(encoding="utf-8")
        self.assertIn("first value", hardening)
        for directive in (
            "PermitRootLogin no",
            "PubkeyAuthentication yes",
            "PasswordAuthentication no",
            "KbdInteractiveAuthentication no",
            "X11Forwarding no",
            "AllowAgentForwarding no",
            "AllowTcpForwarding no",
            "PermitTunnel no",
        ):
            with self.subTest(directive=directive):
                self.assertEqual(hardening.count(directive), 1)


if __name__ == "__main__":
    unittest.main()
