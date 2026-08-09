import type { DragEvent, FormEvent, ReactNode } from 'react';
import LoadingScreen from '@/shared/ui/feedback/LoadingScreen';
import { DEMO_RESET_IN_PROGRESS_MESSAGE } from '@/shared/api/error';
import { useDemo } from '@/shared/demo/DemoContext';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import DemoMaintenanceScreen from './DemoMaintenanceScreen';

export default function DemoStateBoundary({ children }: { children: ReactNode }) {
  const demo = useDemo();
  const snackbar = useSnackbar();

  if (!demo.statusResolved && !demo.statusUnavailable) return <LoadingScreen />;
  if (demo.statusUnavailable || demo.maintenance || demo.failed) {
    return <DemoMaintenanceScreen />;
  }

  const blockWrite = (event: FormEvent | DragEvent) => {
    if (!demo.writeBlocked) return;
    if (event.type === 'submit'
        && event.target instanceof HTMLFormElement
        && event.target.dataset.demoWriteAction === 'false') {
      return;
    }
    event.preventDefault();
    event.stopPropagation();
    snackbar.warning(DEMO_RESET_IN_PROGRESS_MESSAGE);
  };

  // Portal 안의 form 이벤트도 React tree를 따라 여기로 전파된다. 모든 도메인에
  // 같은 guard를 반복하지 않고 Enter 제출과 drag/drop을 한 경계에서 차단한다.
  return (
    <div
      style={{ display: 'contents' }}
      onSubmitCapture={blockWrite}
      onDropCapture={blockWrite}
    >
      {children}
    </div>
  );
}
