import { useState } from 'react';
import Collapse from '@mui/material/Collapse';
import ExpandMoreRoundedIcon from '@mui/icons-material/ExpandMoreRounded';
import type { EmployeeProfileResponse } from '@/features/employee/types';
import { useDemo } from '@/shared/demo/DemoContext';
import { GuideHeader, GuideList, GuideRoot } from './DemoSurface.styles';

export default function DemoExperienceGuide({ profile }: { profile: EmployeeProfileResponse }) {
  const demo = useDemo();
  const [open, setOpen] = useState(false);
  if (!demo.status.enabled) return null;

  const isStaff = profile.loginId === 'demo.staff';
  const steps = isStaff
    ? [
        '전자결재에서 일반 기안을 작성해 상신해 보세요.',
        '경비에서 합성 영수증 없이 청구 흐름을 확인해 보세요.',
        '근태에서 모의 위치로 출·퇴근을 체험해 보세요.',
        '게시판에 일반 글을 작성한 뒤, 관리자형 계정으로 전환해 앞서 상신한 기안을 결재해 보세요.',
      ]
    : [
        '고객사 상세에서 담당자와 영업 활동을 확인해 보세요.',
        '계약 상세에서 계약금·중도금·잔금 일정을 확인해 보세요.',
        '설비 상세에서 두 종류의 보증기간을 확인해 보세요.',
        'AS 상세에서 방문 내역과 비용을 확인해 보세요.',
        '직원형 계정의 기안을 결재해 두 계정의 권한 차이를 확인해 보세요.',
      ];

  return (
    <GuideRoot>
      <GuideHeader
        type="button"
        onClick={() => setOpen((value) => !value)}
        endIcon={<ExpandMoreRoundedIcon sx={{ transform: open ? 'rotate(180deg)' : 'none', transition: 'transform 0.2s' }} />}
        aria-expanded={open}
      >
        추천 체험 흐름
      </GuideHeader>
      <Collapse in={open} timeout={180}>
        <GuideList>
          {steps.map((step) => <li key={step}>{step}</li>)}
        </GuideList>
      </Collapse>
    </GuideRoot>
  );
}
