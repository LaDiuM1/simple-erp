import { Component, type ReactNode } from 'react';
import ErrorScreen from './ErrorScreen';

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
}

export default class PageErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false };

  static getDerivedStateFromError(): State {
    return { hasError: true };
  }

  render() {
    if (this.state.hasError) {
      return (
        <ErrorScreen
          message="화면을 불러오지 못했습니다. 다른 메뉴로 이동하거나 다시 시도해 주세요."
          onRetry={() => window.location.reload()}
          fullScreen={false}
        />
      );
    }

    return this.props.children;
  }
}
