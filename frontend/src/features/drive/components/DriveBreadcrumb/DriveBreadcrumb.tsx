import { Fragment } from 'react';
import type { DriveBreadcrumbItem } from '@/features/drive/types';
import { BreadcrumbBar, CrumbButton, CrumbSeparator, CurrentCrumb } from './DriveBreadcrumb.styles';

interface Props {
  /** 루트 → 현재 폴더 순서 (자기 자신 포함). 루트 탐색이면 빈 배열. */
  breadcrumb: DriveBreadcrumbItem[];
  /** null = 루트로 이동. */
  onNavigate: (folderId: number | null) => void;
}

/** 드라이브 경로 breadcrumb — 루트('드라이브') → … → 현재 폴더. 현재 폴더만 클릭 불가. */
export default function DriveBreadcrumb({ breadcrumb, onNavigate }: Props) {
  const lastIndex = breadcrumb.length - 1;

  return (
    <BreadcrumbBar>
      {breadcrumb.length === 0 ? (
        <CurrentCrumb>드라이브</CurrentCrumb>
      ) : (
        <CrumbButton type="button" onClick={() => onNavigate(null)}>
          드라이브
        </CrumbButton>
      )}
      {breadcrumb.map((item, idx) => (
        <Fragment key={item.id}>
          <CrumbSeparator>/</CrumbSeparator>
          {idx === lastIndex ? (
            <CurrentCrumb>{item.name}</CurrentCrumb>
          ) : (
            <CrumbButton type="button" onClick={() => onNavigate(item.id)}>
              {item.name}
            </CrumbButton>
          )}
        </Fragment>
      ))}
    </BreadcrumbBar>
  );
}
