import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import IconButton from '@mui/material/IconButton';
import DeleteOutlineRoundedIcon from '@mui/icons-material/DeleteOutlineRounded';
import Typography from '@mui/material/Typography';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import QueryGate from '@/shared/ui/feedback/QueryGate';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import Muted from '@/shared/ui/atoms/Muted';
import ActiveStatusIndicator from '@/shared/ui/atoms/ActiveStatusIndicator';
import {
  BodyCell,
  BodyRow,
  EmptyState,
  HeaderCell,
  ListRoot,
  ListSurface,
  StyledTableContainer,
  TableScrollArea,
  TableWrapper,
} from '@/shared/ui/GenericList';
import EngineerFormModal from '@/features/afterService/components/EngineerFormModal/EngineerFormModal';
import { useEngineerPage } from '@/features/afterService/hooks/useEngineerPage';
import { ENGINEER_TYPE_LABELS } from '@/features/afterService/types';

/**
 * 엔지니어 관리 — AS 관리 (AFTER_SERVICES) 의 서브 기능. 페이징 / 검색이 불필요한 소규모
 * 마스터라 GenericList styled primitives 만 재사용 (코드 채번 규칙 목록과 동일 경로).
 */
const TABLE_MIN_WIDTH = 720;
const NO_COL_WIDTH = 64;
const ROW_HEIGHT = 44;

export default function EngineerPage() {
  const { queries, canWrite, headerActions, modal, onRowClick, remove } = useEngineerPage();

  return (
    <>
      <PageHeaderActions actions={headerActions} />
      <QueryGate queries={queries}>
        {({ engineers }) => (
          <ListRoot>
            <ListSurface>
              <TableWrapper>
                <TableScrollArea>
                  <StyledTableContainer>
                    <Table size="small" sx={{ tableLayout: 'fixed', width: '100%', minWidth: TABLE_MIN_WIDTH }}>
                      <colgroup>
                        <col style={{ width: NO_COL_WIDTH }} />
                        <col style={{ width: '22%' }} />
                        <col style={{ width: '14%' }} />
                        <col style={{ width: '26%' }} />
                        <col style={{ width: '20%' }} />
                        <col style={{ width: '12%' }} />
                        <col style={{ width: 72 }} />
                      </colgroup>
                      <TableHead>
                        <TableRow>
                          <HeaderCell align="center">No</HeaderCell>
                          <HeaderCell>이름</HeaderCell>
                          <HeaderCell>구분</HeaderCell>
                          <HeaderCell>소속</HeaderCell>
                          <HeaderCell>연락처</HeaderCell>
                          <HeaderCell>사용 여부</HeaderCell>
                          <HeaderCell align="center">액션</HeaderCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {engineers.length === 0 ? (
                          <TableRow>
                            <BodyCell colSpan={7} sx={{ p: 0 }}>
                              <EmptyState message="등록된 엔지니어가 없습니다." />
                            </BodyCell>
                          </TableRow>
                        ) : (
                          engineers.map((engineer, idx) => (
                            <BodyRow
                              key={engineer.id}
                              clickable={canWrite}
                              onClick={() => onRowClick(engineer)}
                              style={{ height: ROW_HEIGHT }}
                            >
                              <BodyCell align="center" sx={{ color: 'text.secondary' }}>
                                {idx + 1}
                              </BodyCell>
                              <BodyCell>
                                <Typography sx={{ fontSize: '0.875rem', fontWeight: 600 }}>
                                  {engineer.name}
                                </Typography>
                              </BodyCell>
                              <BodyCell>{ENGINEER_TYPE_LABELS[engineer.type]}</BodyCell>
                              <BodyCell>{engineer.affiliation ?? <Muted />}</BodyCell>
                              <BodyCell>{engineer.phone ?? <Muted />}</BodyCell>
                              <BodyCell>
                                <ActiveStatusIndicator active={engineer.active} />
                              </BodyCell>
                              <BodyCell align="center">
                                {canWrite ? (
                                  <IconButton
                                    size="small"
                                    aria-label="엔지니어 삭제"
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      remove.onRequest(engineer);
                                    }}
                                  >
                                    <DeleteOutlineRoundedIcon fontSize="small" />
                                  </IconButton>
                                ) : (
                                  <Muted />
                                )}
                              </BodyCell>
                            </BodyRow>
                          ))
                        )}
                      </TableBody>
                    </Table>
                  </StyledTableContainer>
                </TableScrollArea>
              </TableWrapper>
            </ListSurface>
          </ListRoot>
        )}
      </QueryGate>

      <EngineerFormModal open={modal.creating} onClose={modal.onCloseCreate} />
      {modal.editing && (
        <EngineerFormModal open onClose={modal.onCloseEdit} engineer={modal.editing} />
      )}
      <ConfirmModal
        isOpen={remove.target !== null}
        title="엔지니어 삭제"
        message={
          remove.target
            ? `'${remove.target.name}' 엔지니어를 삭제하시겠습니까? AS 기록이 참조 중이면 삭제할 수 없습니다.`
            : ''
        }
        confirmLabel={remove.isDeleting ? '삭제 중...' : '삭제'}
        danger
        confirmDisabled={remove.isDeleting}
        onConfirm={remove.onConfirm}
        onCancel={remove.onCancel}
      />
    </>
  );
}
