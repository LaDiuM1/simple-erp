import { describe, expect, it, vi } from 'vitest';
import { fireEvent, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Provider } from 'react-redux';
import { createPortal } from 'react-dom';
import { MemoryRouter, Outlet, Route, Routes } from 'react-router-dom';
import { store } from '@/app/store';
import { DemoContext } from '@/shared/demo/DemoContext';
import FileAttachField from '@/shared/ui/FileAttachField';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import {
  READY_DEMO_CONTEXT,
  WRITE_LOCKED_DEMO_CONTEXT,
} from '@/test/demoContext';
import { renderWithTheme } from '@/test/renderWithTheme';
import DemoStateBoundary from './DemoStateBoundary';

const lockedContext = WRITE_LOCKED_DEMO_CONTEXT;
const readyContext = READY_DEMO_CONTEXT;

function OutletLayout({ node }: { node: HTMLElement }) {
  return <Outlet context={{ pageHeaderActionsNode: node }} />;
}

describe('demo write guards', () => {
  it('reset 쓰기 잠금 중에는 첨부 업로드를 비활성화한다', () => {
    renderWithTheme(
      <Provider store={store}>
        <DemoContext.Provider value={lockedContext}>
          <FileAttachField value={[]} onChange={vi.fn()} />
        </DemoContext.Provider>
      </Provider>,
    );

    expect(screen.getByRole('button', { name: '파일 선택' })).toBeDisabled();
    expect(screen.getByText(
      '데모 초기화 준비 중에는 파일을 업로드할 수 없습니다. 파일당 최대 30.0MB.',
    )).toBeInTheDocument();
  });

  it('READY에서는 첨부 업로드를 열고 합성 파일 안내를 보여준다', () => {
    renderWithTheme(
      <Provider store={store}>
        <DemoContext.Provider value={readyContext}>
          <FileAttachField value={[]} onChange={vi.fn()} />
        </DemoContext.Provider>
      </Provider>,
    );

    expect(screen.getByRole('button', { name: '파일 선택' })).toBeEnabled();
    expect(screen.getByText(
      '합성 파일만 업로드해 주세요. 실제 개인정보나 업무 자료는 입력하지 마세요. 파일당 최대 30.0MB.',
    )).toBeInTheDocument();
  });

  it('반려·상신 취소 같은 secondary writeAction만 잠그고 읽기 이동은 유지한다', () => {
    const portalNode = document.createElement('div');
    document.body.appendChild(portalNode);

    renderWithTheme(
      <DemoContext.Provider value={lockedContext}>
        <MemoryRouter>
          <Routes>
            <Route element={<OutletLayout node={portalNode} />}>
              <Route index element={<PageHeaderActions actions={[
                { design: 'secondary', label: '반려', writeAction: true, onClick: vi.fn() },
                { design: 'secondary', label: '상신 취소', writeAction: true, onClick: vi.fn() },
                { design: 'secondary', label: '경비 상세', onClick: vi.fn() },
              ]} />} />
            </Route>
          </Routes>
        </MemoryRouter>
      </DemoContext.Provider>,
    );

    expect(screen.getByRole('button', { name: '반려' })).toBeDisabled();
    expect(screen.getByRole('button', { name: '상신 취소' })).toBeDisabled();
    expect(screen.getByRole('button', { name: '경비 상세' })).toBeEnabled();
    portalNode.remove();
  });

  it('쓰기 잠금 중 form 제출은 공통 경계에서 막고 일반 탐색은 유지한다', async () => {
    const onSubmit = vi.fn();
    const onDrop = vi.fn();
    const onNavigate = vi.fn();
    const user = userEvent.setup();
    const portalNode = document.createElement('div');
    document.body.appendChild(portalNode);

    renderWithTheme(
      <Provider store={store}>
        <DemoContext.Provider value={lockedContext}>
          <DemoStateBoundary>
            {createPortal(
              <>
                <form onSubmit={onSubmit}>
                  <button type="submit">저장</button>
                </form>
                <div data-testid="drop-target" onDrop={onDrop}>첨부 영역</div>
              </>,
              portalNode,
            )}
            <button type="button" onClick={onNavigate}>목록 보기</button>
          </DemoStateBoundary>
        </DemoContext.Provider>
      </Provider>,
    );

    await user.click(screen.getByRole('button', { name: '저장' }));
    fireEvent.drop(screen.getByTestId('drop-target'));
    await user.click(screen.getByRole('button', { name: '목록 보기' }));

    expect(onSubmit).not.toHaveBeenCalled();
    expect(onDrop).not.toHaveBeenCalled();
    expect(onNavigate).toHaveBeenCalledOnce();
    portalNode.remove();
  });

  it('열려 있던 변경 확인 모달의 확인 버튼을 잠그고 취소는 유지한다', () => {
    renderWithTheme(
      <DemoContext.Provider value={lockedContext}>
        <ConfirmModal
          isOpen
          title="삭제 확인"
          onConfirm={vi.fn()}
          onCancel={vi.fn()}
        />
      </DemoContext.Provider>,
    );

    expect(screen.getByRole('button', { name: '확인' })).toBeDisabled();
    expect(screen.getByRole('button', { name: '취소' })).toBeEnabled();
  });

  it('로그인 form은 쓰기 잠금 중에도 제출을 허용한다', async () => {
    const onLogin = vi.fn();
    const user = userEvent.setup();

    renderWithTheme(
      <Provider store={store}>
        <DemoContext.Provider value={lockedContext}>
          <DemoStateBoundary>
            <form
              data-demo-write-action="false"
              onSubmit={(event) => {
                event.preventDefault();
                onLogin();
              }}
            >
              <button type="submit">로그인</button>
            </form>
          </DemoStateBoundary>
        </DemoContext.Provider>
      </Provider>,
    );

    await user.click(screen.getByRole('button', { name: '로그인' }));

    expect(onLogin).toHaveBeenCalledOnce();
  });

  it('로그아웃 확인처럼 서버 데이터를 바꾸지 않는 확인 동작은 잠그지 않는다', async () => {
    const onLogout = vi.fn();
    const user = userEvent.setup();

    renderWithTheme(
      <DemoContext.Provider value={lockedContext}>
        <ConfirmModal
          isOpen
          title="로그아웃 확인"
          confirmLabel="로그아웃"
          writeAction={false}
          onConfirm={onLogout}
          onCancel={vi.fn()}
        />
      </DemoContext.Provider>,
    );

    await user.click(screen.getByRole('button', { name: '로그아웃' }));

    expect(onLogout).toHaveBeenCalledOnce();
  });
});
