import Typography from '@mui/material/Typography';
import type { ColumnConfig } from '@/shared/ui/GenericList';
import {
  ACQUISITION_SOURCE_TYPE_LABELS,
  type AcquisitionSourceInfo,
} from '@/features/acquisitionSource/types';

/** 검색과 관리 모달이 공유하는 컨택 경로 정보 위계. */
export const acquisitionSourceModalColumns: ColumnConfig<AcquisitionSourceInfo>[] = [
  {
    key: 'name',
    label: '이름',
    mobilePrimary: true,
    flex: 1.2,
    render: (source) => (
      <Typography sx={{ fontSize: '0.875rem', fontWeight: 600, color: 'text.primary' }}>
        {source.name}
      </Typography>
    ),
  },
  {
    key: 'type',
    label: '분류',
    width: 120,
    render: (source) => ACQUISITION_SOURCE_TYPE_LABELS[source.type],
  },
  { key: 'description', label: '설명', flex: 2 },
];
