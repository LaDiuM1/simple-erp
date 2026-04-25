import type { ReactNode } from 'react';
import {
  SectionDescription,
  SectionHeader,
  SectionIcon,
  SectionSurface,
  SectionTitle,
  SectionTitleBox,
} from './GenericForm.styles';

export interface FormSectionProps {
  icon?: ReactNode;
  title?: string;
  description?: string;
  children: ReactNode;
}

/**
 * 폼 섹션 wrapper. icon/title/description 이 모두 없으면 헤더 없이 컨텐츠만 노출
 * (= GenericForm 의 단일 섹션 mode 에서 헤더가 불필요한 경우).
 * 인접한 다른 FormSection 과는 점선 divider 로 자동 구분 (SectionSurface 의 `& + &` 룰).
 */
export default function FormSection({ icon, title, description, children }: FormSectionProps) {
  const hasHeader = !!icon || !!title || !!description;
  return (
    <SectionSurface>
      {hasHeader && (
        <SectionHeader>
          {icon && <SectionIcon>{icon}</SectionIcon>}
          <SectionTitleBox>
            {title && <SectionTitle>{title}</SectionTitle>}
            {description && <SectionDescription>{description}</SectionDescription>}
          </SectionTitleBox>
        </SectionHeader>
      )}
      {children}
    </SectionSurface>
  );
}
