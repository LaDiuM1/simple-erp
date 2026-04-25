import { Provider } from 'react-redux';
import { BrowserRouter } from 'react-router-dom';
import { SnackbarHost } from '@/shared/ui/feedback/snackbar';
import { store } from './store';
import AppRoutes from './routes';

export default function App() {
  return (
    <Provider store={store}>
      <BrowserRouter>
        <AppRoutes />
        <SnackbarHost />
      </BrowserRouter>
    </Provider>
  );
}
