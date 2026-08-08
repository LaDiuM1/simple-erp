const SECOND_MS = 1_000;

export function millisecondsUntil(iso: string | null, nowMs: number): number | null {
  if (!iso) return null;
  const target = Date.parse(iso);
  if (Number.isNaN(target)) return null;
  return Math.max(0, target - nowMs);
}

export function formatCountdown(milliseconds: number | null): string {
  if (milliseconds === null) return '--:--:--';
  const totalSeconds = Math.max(0, Math.floor(milliseconds / SECOND_MS));
  const hours = Math.floor(totalSeconds / 3_600);
  const minutes = Math.floor((totalSeconds % 3_600) / 60);
  const seconds = totalSeconds % 60;
  return [hours, minutes, seconds].map((value) => String(value).padStart(2, '0')).join(':');
}
