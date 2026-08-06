import {
  SectionDescription,
  SectionHeadingRoot,
  SectionHeadingTitle,
} from './DashboardSectionHeading.styles';

interface Props {
  id: string;
  title: string;
  description: string;
}

/** 페이지 안에서 서로 다른 업무 묶음의 위계를 고정하는 공통 섹션 제목. */
export default function DashboardSectionHeading({ id, title, description }: Props) {
  return (
    <SectionHeadingRoot>
      <SectionHeadingTitle id={id}>
        {title}
      </SectionHeadingTitle>
      <SectionDescription>{description}</SectionDescription>
    </SectionHeadingRoot>
  );
}
