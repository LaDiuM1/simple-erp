import CloseIcon from '@mui/icons-material/Close';
import EmployeeSelectField from '@/features/employee/components/EmployeeSelectField/EmployeeSelectField';
import {
  ApproverName,
  EmptyHint,
  FieldLabel,
  FieldRoot,
  LineList,
  LineRow,
  RemoveButton,
  SelectRow,
  StepOrder,
} from './ApprovalLineField.styles';

/** 결재선 한 단계 — 추가 순서 = 결재 순서. submit 시 employeeId 만 추출해 approverIds 로 전송. */
export interface ApprovalLineEntry {
  employeeId: number;
  name: string;
}

interface Props {
  label?: string;
  value: ApprovalLineEntry[];
  onChange: (line: ApprovalLineEntry[]) => void;
  disabled?: boolean;
}

/**
 * 결재선 빌더 공용 필드 — 직원 검색으로 결재자를 순서대로 추가.
 * 전자결재 기안 / 경비 청구 / 휴가 신청이 공용 사용.
 */
export default function ApprovalLineField({ label = '결재선', value, onChange, disabled }: Props) {
  const handleSelect = (id: string, name: string) => {
    if (!id) return;

    const employeeId = Number(id);
    // 중복 결재자는 무시
    if (value.some((entry) => entry.employeeId === employeeId)) return;
    onChange([...value, { employeeId, name }]);
  };

  const handleRemove = (employeeId: number) => {
    onChange(value.filter((entry) => entry.employeeId !== employeeId));
  };

  return (
    <FieldRoot>
      <FieldLabel>{label}</FieldLabel>
      {!disabled && (
        <SelectRow>
          <EmployeeSelectField
            label="결재자 추가"
            value=""
            valueLabel=""
            onChange={handleSelect}
            placeholder="결재자 검색"
          />
        </SelectRow>
      )}
      {value.length === 0 ? (
        <EmptyHint>결재자를 순서대로 추가하세요. 추가 순서 = 결재 순서.</EmptyHint>
      ) : (
        <LineList>
          {value.map((entry, index) => (
            <LineRow key={entry.employeeId}>
              <StepOrder>{index + 1}차</StepOrder>
              <ApproverName>{entry.name}</ApproverName>
              {!disabled && (
                <RemoveButton size="small" onClick={() => handleRemove(entry.employeeId)} aria-label="결재자 제거">
                  <CloseIcon sx={{ fontSize: '0.875rem' }} />
                </RemoveButton>
              )}
            </LineRow>
          ))}
        </LineList>
      )}
    </FieldRoot>
  );
}
