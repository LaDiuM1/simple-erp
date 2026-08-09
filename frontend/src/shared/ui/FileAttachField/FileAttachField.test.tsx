import { useState } from 'react';
import { fireEvent, screen } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { Provider } from 'react-redux';
import { http, HttpResponse } from 'msw';
import { store } from '@/app/store';
import { DemoContext } from '@/shared/demo/DemoContext';
import { setDemoWriteBlocked } from '@/shared/demo/demoRuntimeSlice';
import { READY_DEMO_CONTEXT } from '@/test/demoContext';
import { renderWithTheme } from '@/test/renderWithTheme';
import { apiResponse } from '@/test/msw/handlers';
import { server } from '@/test/msw/server';
import FileAttachField, { type AttachedFile } from './FileAttachField';

function Harness() {
  const [files, setFiles] = useState<AttachedFile[]>([]);
  return <FileAttachField value={files} onChange={setFiles} />;
}

describe('FileAttachField upload adapter', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    store.dispatch({ type: 'api/resetApiState' });
    store.dispatch(setDemoWriteBlocked(false));
  });

  afterEach(() => {
    store.dispatch(setDemoWriteBlocked(true));
  });

  it('선택 파일을 boundary 포함 multipart 요청으로 업로드하고 응답을 반영한다', async () => {
    let contentType: string | null = null;
    let multipartBody = '';
    server.use(
      http.post('*/api/v1/files', async ({ request }) => {
        contentType = request.headers.get('content-type');
        multipartBody = new TextDecoder().decode(await request.arrayBuffer());
        return HttpResponse.json(apiResponse({
          id: 41,
          originalName: '합성-견적서.txt',
          contentType: 'text/plain',
          size: 6,
        }));
      }),
    );
    const { container } = renderWithTheme(
      <Provider store={store}>
        <DemoContext.Provider value={READY_DEMO_CONTEXT}>
          <Harness />
        </DemoContext.Provider>
      </Provider>,
    );
    const file = new File(['sample'], '합성-견적서.txt', { type: 'text/plain' });
    const input = container.querySelector<HTMLInputElement>('input[type="file"]');
    expect(input).not.toBeNull();

    fireEvent.change(input!, { target: { files: [file] } });

    expect(await screen.findByText('합성-견적서.txt')).toBeInTheDocument();
    expect(contentType).toMatch(/^multipart\/form-data;\s*boundary=/i);
    expect(multipartBody).toContain('name="file"');
    expect(multipartBody).toContain('filename=');
    expect(multipartBody).toContain('Content-Type: text/plain');
  });
});
