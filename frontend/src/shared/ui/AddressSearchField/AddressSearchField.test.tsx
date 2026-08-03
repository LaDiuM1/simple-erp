import { act, fireEvent, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { DaumPostcodeData } from '@/shared/hooks/useDaumPostcode';
import { renderWithTheme } from '@/test/renderWithTheme';
import AddressSearchField from './AddressSearchField';

const SCRIPT_SELECTOR = '#daum-postcode-script';

describe('AddressSearchField', () => {
  beforeEach(() => {
    document.querySelector(SCRIPT_SELECTOR)?.remove();
    delete window.kakao;
    delete window.daum;
  });

  afterEach(() => {
    document.querySelector(SCRIPT_SELECTOR)?.remove();
    delete window.kakao;
    delete window.daum;
  });

  it('접근 가능한 Dialog 안에 위젯을 embed 하고 주소 선택 완료 시 값을 반영한 뒤 닫는다', async () => {
    const embed = vi.fn();
    let onComplete: ((data: DaumPostcodeData) => void) | undefined;
    class PostcodeMock {
      constructor(options: { oncomplete: (data: DaumPostcodeData) => void }) {
        onComplete = options.oncomplete;
      }

      embed(container: HTMLElement) {
        embed(container);
      }
    }
    window.kakao = { Postcode: PostcodeMock };
    const onZipCodeChange = vi.fn();
    const onRoadAddressChange = vi.fn();
    renderWithTheme(
      <AddressSearchField
        zipCode=""
        roadAddress=""
        onZipCodeChange={onZipCodeChange}
        onRoadAddressChange={onRoadAddressChange}
      />,
    );

    fireEvent.click(screen.getByRole('button', { name: '주소 검색' }));
    const dialog = await screen.findByRole('dialog', { name: '주소 검색' });
    const resultRegion = screen.getByRole('region', { name: '주소 검색 결과' });
    expect(dialog).toHaveAttribute('aria-modal', 'true');
    expect(screen.getByRole('button', { name: '주소 검색 닫기' })).toBeInTheDocument();
    await waitFor(() => expect(embed).toHaveBeenCalledWith(resultRegion));

    act(() => {
      onComplete?.({
        zonecode: '04524',
        roadAddress: '서울시 중구 세종대로 110',
        jibunAddress: '서울시 중구 태평로1가 31',
        buildingName: '',
        bname: '태평로1가',
        sido: '서울',
        sigungu: '중구',
      });
    });

    expect(onZipCodeChange).toHaveBeenCalledWith('04524');
    expect(onRoadAddressChange).toHaveBeenCalledWith('서울시 중구 세종대로 110');
    await waitFor(() => expect(screen.queryByRole('dialog', { name: '주소 검색' }))
      .not.toBeInTheDocument());
  });

  it('script 실패를 Dialog 에 알리고 닫은 뒤 우편번호와 기본 주소 직접 입력을 허용한다', async () => {
    const onZipCodeChange = vi.fn();
    const onRoadAddressChange = vi.fn();
    renderWithTheme(
      <AddressSearchField
        zipCode=""
        roadAddress=""
        onZipCodeChange={onZipCodeChange}
        onRoadAddressChange={onRoadAddressChange}
      />,
    );

    fireEvent.click(screen.getByRole('button', { name: '주소 검색' }));
    await screen.findByRole('dialog', { name: '주소 검색' });
    await waitFor(() => expect(document.querySelector(SCRIPT_SELECTOR)).not.toBeNull());
    const script = document.querySelector<HTMLScriptElement>(SCRIPT_SELECTOR);
    expect(script).not.toBeNull();
    act(() => script?.dispatchEvent(new Event('error')));

    const alert = await screen.findByRole('alert');
    expect(alert).toHaveTextContent('주소를 직접 입력');
    expect(screen.getByRole('button', { name: '다시 시도' })).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '주소 검색 닫기' }));
    await waitFor(() => expect(screen.queryByRole('dialog', { name: '주소 검색' }))
      .not.toBeInTheDocument());

    const zipCode = screen.getByLabelText('우편번호');
    const roadAddress = screen.getByLabelText('기본 주소');
    expect(zipCode).toBeEnabled();
    expect(roadAddress).toBeEnabled();
    fireEvent.change(zipCode, { target: { value: '06236' } });
    fireEvent.change(roadAddress, { target: { value: '서울시 강남구 테헤란로' } });
    expect(onZipCodeChange).toHaveBeenCalledWith('06236');
    expect(onRoadAddressChange).toHaveBeenCalledWith('서울시 강남구 테헤란로');
  });

  it('위젯에서 검색을 취소하면 Dialog 를 종료하고 기존 값을 보존한다', async () => {
    let onClose: (() => void) | undefined;
    class PostcodeMock {
      constructor(options: { onclose?: () => void }) {
        onClose = options.onclose;
      }

      embed() {}
    }
    window.kakao = { Postcode: PostcodeMock };
    const onZipCodeChange = vi.fn();
    const onRoadAddressChange = vi.fn();
    renderWithTheme(
      <AddressSearchField
        zipCode="12345"
        roadAddress="기존 주소"
        onZipCodeChange={onZipCodeChange}
        onRoadAddressChange={onRoadAddressChange}
      />,
    );

    fireEvent.click(screen.getByRole('button', { name: '주소 검색' }));
    await screen.findByRole('dialog', { name: '주소 검색' });
    await waitFor(() => expect(onClose).toBeTypeOf('function'));
    act(() => onClose?.());

    await waitFor(() => expect(screen.queryByRole('dialog', { name: '주소 검색' }))
      .not.toBeInTheDocument());
    expect(onZipCodeChange).not.toHaveBeenCalled();
    expect(onRoadAddressChange).not.toHaveBeenCalled();
    expect(screen.getByLabelText('우편번호')).toHaveValue('12345');
    expect(screen.getByLabelText('기본 주소')).toHaveValue('기존 주소');
  });
});
