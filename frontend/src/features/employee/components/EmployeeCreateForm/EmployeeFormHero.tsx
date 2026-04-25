import ApartmentRoundedIcon from '@mui/icons-material/ApartmentRounded';
import {
  HeroAvatar,
  HeroAvatarPlaceholder,
  HeroBody,
  HeroCard,
  HeroEyebrow,
  HeroSubtitle,
  HeroTitle,
} from './EmployeeCreateForm.styles';

export default function EmployeeFormHero({ name }: { name: string }) {
  const initial = name.trim().charAt(0);
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
        <HeroEyebrow>새 직원</HeroEyebrow>
        <HeroTitle>{name.trim() || '직원 등록'}</HeroTitle>
        <HeroSubtitle>
          필수 항목을 입력한 뒤 우측 상단의 등록 버튼을 눌러주세요.
        </HeroSubtitle>
      </HeroBody>
    </HeroCard>
  );
}
