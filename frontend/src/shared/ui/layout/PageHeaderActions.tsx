import { Fragment, type ReactNode } from 'react';
import { createPortal } from 'react-dom';
import { useOutletContext } from 'react-router-dom';
import Button from '@mui/material/Button';
import CircularProgress from '@mui/material/CircularProgress';
import { styled } from '@mui/material/styles';
import AddIcon from '@mui/icons-material/Add';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import SaveIcon from '@mui/icons-material/Save';
import PermissionGate from './PermissionGate';

interface OutletCtx {
  pageHeaderActionsNode: HTMLElement | null;
}

/**
 * PageHeader 우측 액션 영역에 표준 버튼들을 주입한다.
 * 표준 design 3가지 (create / save / cancel) 만 책임지며, escape 케이스 (예: 엑셀 다운로드)
 * 는 별도 컨텍스트 (필터바 등) 에서 자체 처리한다.
 */
export default function PageHeaderActions({ actions }: { actions: PageHeaderAction[] }) {
  const ctx = useOutletContext<OutletCtx | null>();
  const node = ctx?.pageHeaderActionsNode;
  if (!node) return null;
  return createPortal(
    <>
      {actions.map((action, i) => (
        <Fragment key={i}>{renderAction(action)}</Fragment>
      ))}
    </>,
    node,
  );
}

export type PageHeaderAction =
  | {
      design: 'create' | 'save';
      label?: string;
      menuCode?: string;
      loading?: boolean;
      disabled?: boolean;
      /** 있으면 type=submit + form 속성으로 폼 포털 연결. 없으면 onClick 사용. */
      formId?: string;
      onClick?: () => void;
    }
  | {
      design: 'cancel';
      label?: string;
      /** 선택적 시작 아이콘. 일반 취소 버튼엔 안 쓰지만, 동일 outlined 톤의 보조 액션 (예: 부서 계층도) 에서 사용. */
      icon?: ReactNode;
      onClick: () => void;
      disabled?: boolean;
    }
  | {
      design: 'delete';
      label?: string;
      menuCode?: string;
      loading?: boolean;
      disabled?: boolean;
      onClick: () => void;
    };

const DEFAULT_LABEL = {
  create: '등록',
  save: '저장',
  cancel: '취소',
  delete: '삭제',
} as const;

const PRIMARY_ICON = {
  create: <AddIcon />,
  save: <SaveIcon />,
} as const;

function renderAction(action: PageHeaderAction): ReactNode {
  if (action.design === 'cancel') {
    return (
      <CancelButton
        type="button"
        onClick={action.onClick}
        disabled={action.disabled}
        startIcon={action.icon}
      >
        {action.label ?? DEFAULT_LABEL.cancel}
      </CancelButton>
    );
  }

  if (action.design === 'delete') {
    const startIcon = action.loading
      ? <CircularProgress size={14} color="inherit" />
      : <DeleteOutlineIcon />;
    const button = (
      <DangerButton
        type="button"
        onClick={action.onClick}
        disabled={action.disabled || action.loading}
        startIcon={startIcon}
      >
        {action.label ?? DEFAULT_LABEL.delete}
      </DangerButton>
    );
    if (action.menuCode) {
      return (
        <PermissionGate menuCode={action.menuCode} action="write">
          {button}
        </PermissionGate>
      );
    }
    return button;
  }

  const startIcon = action.loading
    ? <CircularProgress size={14} color="inherit" />
    : PRIMARY_ICON[action.design];

  const button = (
    <PrimaryButton
      type={action.formId ? 'submit' : 'button'}
      form={action.formId}
      onClick={action.onClick}
      disabled={action.disabled || action.loading}
      startIcon={startIcon}
    >
      {action.label ?? DEFAULT_LABEL[action.design]}
    </PrimaryButton>
  );

  if (action.menuCode) {
    return (
      <PermissionGate menuCode={action.menuCode} action="write">
        {button}
      </PermissionGate>
    );
  }
  return button;
}

const buttonBase = {
  height: 34,
  paddingLeft: '0.875rem',
  paddingRight: '0.875rem',
  fontSize: '0.8125rem',
  fontWeight: 500,
  borderRadius: 0,
  textTransform: 'none' as const,
  letterSpacing: '-0.005em',
  boxShadow: 'none',
  '& .MuiButton-startIcon': { marginRight: '0.375rem' },
  '& .MuiButton-startIcon > *': { fontSize: '1rem' },
};

const PrimaryButton = styled(Button)(({ theme }) => ({
  ...buttonBase,
  backgroundColor: theme.palette.primary.main,
  color: theme.palette.primary.contrastText,
  fontWeight: 600,
  border: `1px solid ${theme.palette.primary.main}`,
  '&:hover': {
    backgroundColor: theme.palette.primary.dark,
    borderColor: theme.palette.primary.dark,
  },
  '&.Mui-disabled': {
    backgroundColor: theme.palette.primary.main,
    borderColor: theme.palette.primary.main,
    color: theme.palette.primary.contrastText,
    opacity: 0.5,
  },
}));

const CancelButton = styled(Button)(({ theme }) => ({
  ...buttonBase,
  backgroundColor: 'transparent',
  color: theme.palette.text.secondary,
  border: `1px solid ${theme.palette.divider}`,
  '&:hover': {
    backgroundColor: theme.palette.background.default,
    borderColor: theme.palette.text.disabled,
    color: theme.palette.text.primary,
  },
  '&.Mui-disabled': {
    color: theme.palette.text.disabled,
    borderColor: theme.palette.divider,
  },
}));

const DangerButton = styled(Button)(({ theme }) => ({
  ...buttonBase,
  backgroundColor: 'transparent',
  color: theme.palette.error.main,
  border: `1px solid ${theme.palette.errorBorder}`,
  '&:hover': {
    backgroundColor: theme.palette.errorBg,
    borderColor: theme.palette.error.main,
    color: theme.palette.error.dark,
  },
  '&.Mui-disabled': {
    color: theme.palette.error.main,
    borderColor: theme.palette.errorBorder,
    opacity: 0.5,
  },
}));
