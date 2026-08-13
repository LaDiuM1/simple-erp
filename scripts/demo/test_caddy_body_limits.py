#!/usr/bin/env python3
"""Runtime contract test for Caddy API body limits and trusted client-IP forwarding."""

from __future__ import annotations

from http.client import HTTPConnection
from pathlib import Path
import subprocess
import sys
import time
import uuid


ROOT = Path(__file__).resolve().parents[2]
CADDYFILE = ROOT / "frontend" / "Caddyfile"
BACKEND_CODE = r"""
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

class Handler(BaseHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200)
        self.send_header("X-Seen-Xff", self.headers.get("X-Forwarded-For", ""))
        self.end_headers()

    def do_POST(self):
        remaining = int(self.headers.get("Content-Length", "0"))
        while remaining:
            chunk = self.rfile.read(min(remaining, 65536))
            if not chunk:
                break
            remaining -= len(chunk)
        self.send_response(204)
        self.send_header("X-Seen-Xff", self.headers.get("X-Forwarded-For", ""))
        self.end_headers()

    def log_message(self, *args):
        pass

ThreadingHTTPServer(("0.0.0.0", 8080), Handler).serve_forever()
"""


def docker(*args: str, check: bool = True) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        ["docker", *args],
        check=check,
        capture_output=True,
        text=True,
        encoding="utf-8",
    )


def request(port: int, path: str, size: int, *, spoofed_xff: str | None = None) -> tuple[int, str | None]:
    connection = HTTPConnection("127.0.0.1", port, timeout=20)
    headers = {"Content-Type": "application/octet-stream"}
    if spoofed_xff is not None:
        headers["X-Forwarded-For"] = spoofed_xff
    try:
        connection.request("POST", path, body=b"x" * size, headers=headers)
        response = connection.getresponse()
        response.read()
        return response.status, response.getheader("X-Seen-Xff")
    finally:
        connection.close()


def get(port: int, path: str) -> int:
    connection = HTTPConnection("127.0.0.1", port, timeout=20)
    try:
        connection.request("GET", path)
        response = connection.getresponse()
        response.read()
        return response.status
    finally:
        connection.close()


def wait_until_ready(port: int, caddy_name: str) -> None:
    for _ in range(50):
        try:
            status, _ = request(port, "/api/v1/probe", 1)
            if status == 204:
                return
        except OSError:
            pass
        time.sleep(0.1)
    logs = docker("logs", caddy_name, check=False)
    raise RuntimeError(f"Caddy did not become ready:\n{logs.stdout}\n{logs.stderr}")


def main() -> int:
    suffix = uuid.uuid4().hex[:12]
    network = f"simple-erp-caddy-test-{suffix}"
    backend = f"simple-erp-caddy-backend-{suffix}"
    caddy = f"simple-erp-caddy-{suffix}"
    created: list[str] = []
    try:
        docker("network", "create", network)
        docker(
            "run", "-d", "--name", backend,
            "--network", network, "--network-alias", "backend",
            "python:3.13-alpine", "python", "-c", BACKEND_CODE,
        )
        created.append(backend)
        docker(
            "run", "-d", "--name", caddy,
            "--network", network, "-p", "127.0.0.1::80",
            "-p", "127.0.0.1::8080",
            "-e", "SITE_ADDRESS=:80",
            "-e", "API_REQUEST_BODY_MAX_SIZE=32MB",
            "-e", "API_JSON_REQUEST_BODY_MAX_SIZE=1MB",
            "-v", f"{CADDYFILE}:/etc/caddy/Caddyfile:ro",
            "caddy:2-alpine",
        )
        created.append(caddy)

        mapping = docker("port", caddy, "80/tcp").stdout.strip().rsplit(":", 1)[-1]
        port = int(mapping)
        internal_mapping = docker("port", caddy, "8080/tcp").stdout.strip().rsplit(":", 1)[-1]
        internal_port = int(internal_mapping)
        wait_until_ready(port, caddy)

        public_health = get(port, "/actuator/health")
        if public_health != 404:
            raise AssertionError(f"public actuator health returned {public_health}, expected 404")
        internal_health = get(internal_port, "/actuator/health")
        if internal_health != 200:
            raise AssertionError(f"internal actuator health returned {internal_health}, expected 200")

        generic_status, _ = request(port, "/api/v1/probe", 2 * 1024 * 1024)
        if generic_status != 413:
            raise AssertionError(f"generic 2MiB API body returned {generic_status}, expected 413")

        spoofed = "203.0.113.77"
        upload_status, seen_xff = request(
            port,
            "/api/v1/files",
            2 * 1024 * 1024,
            spoofed_xff=spoofed,
        )
        if upload_status != 204:
            raise AssertionError(f"exact upload 2MiB body returned {upload_status}, expected 204")
        if not seen_xff or spoofed in seen_xff:
            raise AssertionError(f"Caddy did not overwrite spoofed X-Forwarded-For: {seen_xff!r}")

        oversized_status, _ = request(port, "/api/v1/files", 33 * 1024 * 1024)
        if oversized_status != 413:
            raise AssertionError(f"exact upload 33MiB body returned {oversized_status}, expected 413")

        print("Caddy runtime body/XFF contract: PASS")
        return 0
    finally:
        for container in reversed(created):
            docker("rm", "-f", container, check=False)
        docker("network", "rm", network, check=False)


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (AssertionError, RuntimeError, subprocess.CalledProcessError) as error:
        print(f"Caddy runtime body/XFF contract: FAIL: {error}", file=sys.stderr)
        raise SystemExit(1)
