import type { ReactNode } from 'react';
import LoadingScreen from '@/shared/ui/feedback/LoadingScreen';
import { useDemo } from '@/shared/demo/DemoContext';
import DemoMaintenanceScreen from './DemoMaintenanceScreen';

export default function DemoStateBoundary({ children }: { children: ReactNode }) {
  const demo = useDemo();

  if (!demo.statusResolved && !demo.statusUnavailable) return <LoadingScreen />;
  if (demo.statusUnavailable || demo.maintenance || demo.failed) {
    return <DemoMaintenanceScreen />;
  }

  return children;
}
