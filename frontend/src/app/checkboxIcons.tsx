import Box from '@mui/material/Box';

/**
 * MUI 기본 Checkbox 아이콘은 SVG 2px stroke + 진한 회색이라 무거워 보이는데,
 * 본 ERP 톤에 맞춰 1px border + divider 톤으로 교체.
 * theme.MuiCheckbox.defaultProps 의 icon / checkedIcon / indeterminateIcon 으로 주입.
 */

const ICON_SIZE = 16;
const RADIUS = 3;

export function ThinUncheckedIcon() {
  return (
    <Box
      sx={{
        width: ICON_SIZE,
        height: ICON_SIZE,
        borderRadius: `${RADIUS}px`,
        border: '1px solid #C1C1C1',
        backgroundColor: 'transparent',
        boxSizing: 'border-box',
      }}
    />
  );
}

export function ThinCheckedIcon() {
  return (
    <Box
      sx={(theme) => ({
        width: ICON_SIZE,
        height: ICON_SIZE,
        borderRadius: `${RADIUS}px`,
        border: `1px solid ${theme.palette.primary.main}`,
        backgroundColor: theme.palette.primary.main,
        boxSizing: 'border-box',
        position: 'relative',
        // 체크 마크 — CSS border 두 변을 45도 회전.
        '&::after': {
          content: '""',
          position: 'absolute',
          top: '1px',
          left: '4px',
          width: '4px',
          height: '8px',
          border: 'solid white',
          borderWidth: '0 1.5px 1.5px 0',
          transform: 'rotate(45deg)',
        },
      })}
    />
  );
}

export function ThinIndeterminateIcon() {
  return (
    <Box
      sx={(theme) => ({
        width: ICON_SIZE,
        height: ICON_SIZE,
        borderRadius: `${RADIUS}px`,
        border: `1px solid ${theme.palette.primary.main}`,
        backgroundColor: theme.palette.primary.main,
        boxSizing: 'border-box',
        position: 'relative',
        '&::after': {
          content: '""',
          position: 'absolute',
          top: '50%',
          left: '3px',
          right: '3px',
          height: '1.5px',
          marginTop: '-0.75px',
          backgroundColor: 'white',
          borderRadius: '1px',
        },
      })}
    />
  );
}
