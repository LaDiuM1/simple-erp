import { useEffect, useState } from 'react';

/** 현재 날짜를 제공하고 다음 자정마다 한 번만 갱신한다. */
export function useToday(): Date {
  const [today, setToday] = useState(() => new Date());

  useEffect(() => {
    let timeoutId: number;

    const scheduleNextDay = () => {
      const now = new Date();
      const nextDay = new Date(now);
      nextDay.setHours(24, 0, 0, 0);
      timeoutId = window.setTimeout(() => {
        setToday(new Date());
        scheduleNextDay();
      }, nextDay.getTime() - now.getTime());
    };

    scheduleNextDay();
    return () => window.clearTimeout(timeoutId);
  }, []);

  return today;
}
