import CheckRoundedIcon from '@mui/icons-material/CheckRounded';
import { PRESETS, type PresetId } from '@/features/codeRule/config/presets';
import {
  Card,
  CardCheckMark,
  CardDescription,
  CardExample,
  CardLabel,
  CardsGrid,
} from './PresetCards.styles';

interface PresetCardsProps {
  selectedId: PresetId;
  onSelect: (id: PresetId) => void;
}

/**
 * 코드 형식 프리셋 5종을 카드 그리드로 렌더.
 * 클릭 시 onSelect 콜백 → 폼 값 일괄 갱신 (pattern + resetPolicy + parentScoped).
 */
export default function PresetCards({ selectedId, onSelect }: PresetCardsProps) {
  return (
    <CardsGrid role="radiogroup" aria-label="코드 형식 선택">
      {PRESETS.map((preset) => {
        const selected = selectedId === preset.id;
        return (
          <Card
            key={preset.id}
            type="button"
            role="radio"
            aria-checked={selected}
            selected={selected}
            onClick={() => onSelect(preset.id)}
          >
            {selected && (
              <CardCheckMark>
                <CheckRoundedIcon fontSize="small" />
              </CardCheckMark>
            )}
            <CardLabel selected={selected}>{preset.label}</CardLabel>
            <CardExample>{preset.example}</CardExample>
            <CardDescription>{preset.description}</CardDescription>
          </Card>
        );
      })}
    </CardsGrid>
  );
}
