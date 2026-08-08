import { Provider } from 'react-redux';
import { BrowserRouter } from 'react-router-dom';
import { SnackbarHost } from '@/shared/ui/feedback/snackbar';
import DemoProvider from '@/features/demo/components/DemoProvider';
import DemoStateBoundary from '@/features/demo/components/DemoStateBoundary';
import { store } from './store';
import AppRoutes from './routes';

export default function App() {
  return (
    <Provider store={store}>
      <BrowserRouter>
        <DemoProvider>
          <DemoStateBoundary>
            <AppRoutes />
          </DemoStateBoundary>
          <SnackbarHost />
        </DemoProvider>
      </BrowserRouter>
    </Provider>
  );
}
