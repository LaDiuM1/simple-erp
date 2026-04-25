import ApartmentRoundedIcon from '@mui/icons-material/ApartmentRounded';
import {
  HeroAvatar,
  HeroAvatarPlaceholder,
  HeroBody,
  HeroCard,
  HeroEyebrow,
  HeroSubtitle,
  HeroTitle,
} from './employeeForm.styles';

type Mode = 'create' | 'edit';

const COPY: Record<Mode, { eyebrow: string; titleFallback: string; subtitle: string }> = {
  create: {
    eyebrow: '새 직원',
    titleFallback: '직원 등록',
    subtitle: '필수 항목을 입력한 뒤 우측 상단의 등록 버튼을 눌러주세요.',
  },
  edit: {
    eyebrow: '직원 수정',
    titleFallback: '직원 수정',
    subtitle: '필요한 항목을 수정한 뒤 우측 상단의 저장 버튼을 눌러주세요.',
  },
};

export default function EmployeeFormHero({ name, mode }: { name: string; mode: Mode }) {
  const initial = name.trim().charAt(0);
  const copy = COPY[mode];
  return (
    <HeroCard>
      {initial ? (
        <HeroAvatar>{initial}</HeroAvatar>
      ) : (
        <HeroAvatarPlaceholder>
          <ApartmentRoundedIcon sx={{ fontSize: 28 }} />
        </HeroAvatarPlaceholder>
      )}
      <HeroBody>
        <HeroEyebrow>{copy.eyebrow}</HeroEyebrow>
        <HeroTitle>{name.trim() || copy.titleFallback}</HeroTitle>
        <HeroSubtitle>{copy.subtitle}</HeroSubtitle>
      </HeroBody>
    </HeroCard>
  );
}
