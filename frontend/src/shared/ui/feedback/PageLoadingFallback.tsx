import { useEffect, useState } from 'react';
import {
  PageLoadingBoundary,
  PageLoadingStatus,
} from './PageLoadingFallback.styles';

const INDICATOR_DELAY_MS = 200;

export default function PageLoadingFallback() {
  const [announceLoading, setAnnounceLoading] = useState(false);

  useEffect(() => {
    const timeoutId = window.setTimeout(() => setAnnounceLoading(true), INDICATOR_DELAY_MS);
    return () => window.clearTimeout(timeoutId);
  }, []);

  return (
    <>
      <PageLoadingBoundary aria-busy="true" />
      <PageLoadingStatus role="status" aria-live="polite" aria-atomic="true">
        {announceLoading ? '페이지 불러오는 중' : ''}
      </PageLoadingStatus>
    </>
  );
}
