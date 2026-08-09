import { useEffect, useState } from 'react';
import {
  PageLoadingBoundary,
  PageLoadingProgress,
} from './PageLoadingFallback.styles';

const INDICATOR_DELAY_MS = 200;

export default function PageLoadingFallback() {
  const [showIndicator, setShowIndicator] = useState(false);

  useEffect(() => {
    const timeoutId = window.setTimeout(() => setShowIndicator(true), INDICATOR_DELAY_MS);
    return () => window.clearTimeout(timeoutId);
  }, []);

  return (
    <PageLoadingBoundary aria-busy="true">
      {showIndicator && <PageLoadingProgress aria-label="페이지 불러오는 중" />}
    </PageLoadingBoundary>
  );
}
