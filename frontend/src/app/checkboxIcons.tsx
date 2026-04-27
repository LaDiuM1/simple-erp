import Box from '@mui/material/Box';
import CheckRoundedIcon from '@mui/icons-material/CheckRounded';
import RemoveRoundedIcon from '@mui/icons-material/RemoveRounded';
import type { Theme } from '@mui/material/styles';

/**
 * MUI 기본 Checkbox 아이콘은 SVG 2px stroke + 진한 회색이라 무거워 보임.
 * 본 ERP 톤에 맞춰 1px border + 옅은 톤의 커스텀 Box 로 교체.
 * theme.MuiCheckbox.defaultProps 의 icon / checkedIcon / indeterminateIcon 으로 주입.
 */

const ICON_SIZE = 16;
const RADIUS = 3;

const baseBox = (theme: Theme) => ({
  width: ICON_SIZE,
  height: ICON_SIZE,
  borderRadius: `${RADIUS}px`,
  boxSizing: 'border-box' as const,
  display: 'inline-flex',
  alignItems: 'center',
  justifyContent: 'center',
  border: `1px solid ${theme.palette.text.disabled}`,
});

const filledBox = (theme: Theme) => ({
  ...baseBox(theme),
  border: `1px solid ${theme.palette.primary.main}`,
  backgroundColor: theme.palette.primary.main,
  color: '#fff',
});

export function ThinUncheckedIcon() {
  return <Box sx={baseBox} />;
}

export function ThinCheckedIcon() {
  return (
    <Box sx={filledBox}>
      <CheckRoundedIcon sx={{ fontSize: 13, strokeWidth: 1 }} />
    </Box>
  );
}

export function ThinIndeterminateIcon() {
  return (
    <Box sx={filledBox}>
      <RemoveRoundedIcon sx={{ fontSize: 13 }} />
    </Box>
  );
}
