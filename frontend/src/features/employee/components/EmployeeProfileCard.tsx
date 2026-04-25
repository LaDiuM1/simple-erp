import Box from '@mui/material/Box';
import Chip from '@mui/material/Chip';
import Typography from '@mui/material/Typography';
import type { EmployeeProfileResponse } from '../types';
import {
  AvatarBox,
  InfoGrid,
  InfoItem,
  PermItem,
  ProfileCard,
  ProfileHeader,
} from './EmployeeProfileCard.styles';

interface Props {
  profile: EmployeeProfileResponse;
}

export default function EmployeeProfileCard({ profile }: Props) {
  return (
    <ProfileCard>
      <ProfileHeader>
        <AvatarBox>{profile.name.charAt(0)}</AvatarBox>
        <Box sx={{ minWidth: 0 }}>
          <Typography sx={{ fontSize: '1.375rem', fontWeight: 700, color: 'text.primary', mb: '0.25rem' }}>
            {profile.name}
          </Typography>
          <Typography sx={{ fontSize: '0.875rem', color: 'text.secondary' }}>
            @{profile.loginId}
          </Typography>
        </Box>
      </ProfileHeader>

      <InfoGrid>
        <InfoDetail label="부서" value={profile.departmentName} />
        <InfoDetail label="직위" value={profile.positionName} />
        <InfoDetail label="권한명" value={profile.roleName} />
        <InfoDetail label="권한코드" value={profile.roleCode} />
      </InfoGrid>

      {profile.menuPermissions.length > 0 && (
        <Box sx={{ p: '1.5rem' }}>
          <Typography
            sx={{ fontSize: '0.875rem', fontWeight: 600, color: 'text.secondary', mb: '0.875rem', textTransform: 'uppercase', letterSpacing: '0.06em' }}
          >
            메뉴 권한
          </Typography>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
            {profile.menuPermissions.map((p) => (
              <PermItem key={p.menuCode}>
                <Typography sx={{ fontSize: '0.875rem', fontWeight: 500, color: 'text.primary' }}>
                  {p.menuCode}
                </Typography>
                <Box sx={{ display: 'flex', gap: '0.375rem' }}>
                  {p.canRead && <Chip label="읽기" size="small" sx={{ backgroundColor: '#DBEAFE', color: '#1D4ED8', fontWeight: 600, fontSize: '0.75rem' }} />}
                  {p.canWrite && <Chip label="쓰기" size="small" sx={{ backgroundColor: '#D1FAE5', color: '#065F46', fontWeight: 600, fontSize: '0.75rem' }} />}
                </Box>
              </PermItem>
            ))}
          </Box>
        </Box>
      )}
    </ProfileCard>
  );
}

function InfoDetail({ label, value }: { label: string; value: string | null }) {
  return (
    <InfoItem>
      <Typography component="dt" sx={{ fontSize: '0.75rem', fontWeight: 500, textTransform: 'uppercase', letterSpacing: '0.06em', color: 'text.disabled', mb: '0.375rem' }}>
        {label}
      </Typography>
      <Typography component="dd" sx={{ fontSize: '0.9375rem', fontWeight: 500, color: 'text.primary' }}>
        {value ?? '-'}
      </Typography>
    </InfoItem>
  );
}
