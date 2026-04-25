import type { ReactNode } from 'react';
import {
  SectionDescription,
  SectionHeader,
  SectionIcon,
  SectionSurface,
  SectionTitle,
  SectionTitleBox,
} from './employeeForm.styles';

interface FormSectionProps {
  icon: ReactNode;
  title: string;
  description?: string;
  children: ReactNode;
}

export default function FormSection({ icon, title, description, children }: FormSectionProps) {
  return (
    <SectionSurface>
      <SectionHeader>
        <SectionIcon>{icon}</SectionIcon>
        <SectionTitleBox>
          <SectionTitle>{title}</SectionTitle>
          {description && <SectionDescription>{description}</SectionDescription>}
        </SectionTitleBox>
      </SectionHeader>
      {children}
    </SectionSurface>
  );
}
