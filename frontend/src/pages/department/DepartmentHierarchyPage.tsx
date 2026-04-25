import { useMemo, useState, type DragEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import Box from '@mui/material/Box';
import Tooltip from '@mui/material/Tooltip';
import Typography from '@mui/material/Typography';
import { styled, useTheme } from '@mui/material/styles';
import useMediaQuery from '@mui/material/useMediaQuery';
import DragIndicatorRoundedIcon from '@mui/icons-material/DragIndicatorRounded';
import FolderRoundedIcon from '@mui/icons-material/FolderRounded';
import HomeRoundedIcon from '@mui/icons-material/HomeRounded';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import { usePermission } from '@/shared/hooks/usePermission';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import LoadingScreen from '@/shared/ui/feedback/LoadingScreen';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import { useUpdateDepartmentMutation } from '@/features/department/api/departmentApi';
import { useGetDepartmentsQuery } from '@/features/reference/api/referenceApi';
import type { DepartmentInfo } from '@/features/reference/types';
import type { ApiError } from '@/shared/types/api';

const ROOT_KEY = 'root';
type DragOverKey = number | typeof ROOT_KEY | null;

/**
 * 부서 계층 관리 페이지.
 * - 캐시된 reference 부서 목록을 parentId 기준으로 트리 구성
 * - canWrite 일 때만 HTML5 native DnD 활성화 — 다른 부서 위 드롭 = 산하 이동, 최상위 드롭존 = parentId null
 * - 자기 자신 / 자손 위 드롭은 차단 (cycle 방지). BE 도 별도로 self-as-parent 검증함
 * - reparent 는 기존 PUT /api/v1/departments/{id} 재사용 (name 유지 + parentId 만 갱신)
 */
export default function DepartmentHierarchyPage() {
    const navigate = useNavigate();
    const snackbar = useSnackbar();
    const { canWrite } = usePermission(MENU_CODE.DEPARTMENTS);
    const { data: depts = [], isLoading, isError, refetch, error } = useGetDepartmentsQuery();
    const [updateDepartment] = useUpdateDepartmentMutation();

    const [draggedId, setDraggedId] = useState<number | null>(null);
    const [dragOverKey, setDragOverKey] = useState<DragOverKey>(null);

    const childrenByParent = useMemo(() => buildChildrenMap(depts), [depts]);
    const rootDepts = childrenByParent.get(null) ?? [];

    const move = async (sourceId: number, newParentId: number | null) => {
        const dragged = depts.find((d) => d.id === sourceId);
        if (!dragged) return;

        if (newParentId === sourceId) return;
        if (newParentId !== null && isDescendant(sourceId, newParentId, childrenByParent)) {
            snackbar.error('하위 부서를 상위 부서로 설정할 수 없습니다.');
            return;
        }
        if ((dragged.parentId ?? null) === newParentId) return; // no-op

        try {
            await updateDepartment({
                id: sourceId,
                body: { name: dragged.name, parentId: newParentId },
            }).unwrap();
            snackbar.success('부서 계층이 변경되었습니다.');
        } catch (err) {
            snackbar.error((err as ApiError)?.message ?? '계층 변경 중 오류가 발생했습니다.');
        }
    };

    const handleDragStart = (e: DragEvent, id: number) => {
        if (!canWrite) return;
        e.dataTransfer.effectAllowed = 'move';
        setDraggedId(id);
    };

    const handleDragEnd = () => {
        setDraggedId(null);
        setDragOverKey(null);
    };

    const handleDragOver = (e: DragEvent, key: number | typeof ROOT_KEY) => {
        if (!canWrite || draggedId == null) return;
        e.preventDefault();
        e.dataTransfer.dropEffect = 'move';
        setDragOverKey(key);
    };

    const handleDragLeave = () => {
        setDragOverKey(null);
    };

    const handleDrop = (targetParentId: number | null) => {
        setDragOverKey(null);
        if (draggedId == null) return;
        void move(draggedId, targetParentId);
        setDraggedId(null);
    };

    return (
        <>
            <PageHeaderActions
                actions={[
                    {
                        design: 'cancel' as const,
                        label: '목록으로',
                        onClick: () => navigate(MENU_PATH[MENU_CODE.DEPARTMENTS]),
                    },
                ]}
            />

            <PageRoot>
                <PageSurface onDragEnd={handleDragEnd}>
                    {isLoading ? (
                        <LoadingScreen />
                    ) : isError ? (
                        <ErrorScreen message={(error as ApiError)?.message} onRetry={refetch} />
                    ) : (
                        <ContentBox>
                            {canWrite && (
                                <RootDropZone
                                    isOver={dragOverKey === ROOT_KEY}
                                    onDragOver={(e) => handleDragOver(e, ROOT_KEY)}
                                    onDragLeave={handleDragLeave}
                                    onDrop={() => handleDrop(null)}
                                />
                            )}

                            {rootDepts.length === 0 ? (
                                <Typography sx={{ color: 'text.secondary', py: 4, textAlign: 'center' }}>
                                    등록된 부서가 없습니다.
                                </Typography>
                            ) : (
                                rootDepts.map((dept) => (
                                    <TreeNode
                                        key={dept.id}
                                        dept={dept}
                                        level={0}
                                        childrenByParent={childrenByParent}
                                        canWrite={canWrite}
                                        draggedId={draggedId}
                                        dragOverKey={dragOverKey}
                                        onDragStart={handleDragStart}
                                        onDragOver={handleDragOver}
                                        onDragLeave={handleDragLeave}
                                        onDrop={handleDrop}
                                    />
                                ))
                            )}
                        </ContentBox>
                    )}
                </PageSurface>
            </PageRoot>
        </>
    );
}

/* --------------------------------------------------------------------------
 * Tree node (재귀)
 * ------------------------------------------------------------------------ */

interface TreeNodeProps {
    dept: DepartmentInfo;
    level: number;
    childrenByParent: Map<number | null, DepartmentInfo[]>;
    canWrite: boolean;
    draggedId: number | null;
    dragOverKey: DragOverKey;
    onDragStart: (e: DragEvent, id: number) => void;
    onDragOver: (e: DragEvent, key: number) => void;
    onDragLeave: () => void;
    onDrop: (targetId: number) => void;
}

function TreeNode({
    dept,
    level,
    childrenByParent,
    canWrite,
    draggedId,
    dragOverKey,
    onDragStart,
    onDragOver,
    onDragLeave,
    onDrop,
}: TreeNodeProps) {
    const theme = useTheme();
    // 좁은 화면에서는 row 가 가로 폭을 가득 채워 왼쪽 공간이 없으므로 top 으로 폴백.
    const tooltipPlacement = useMediaQuery(theme.breakpoints.up('md')) ? 'left' : 'top';

    const children = childrenByParent.get(dept.id) ?? [];
    const isDragging = draggedId === dept.id;
    const isDropTarget = dragOverKey === dept.id;

    const row = (
        <NodeRow
            draggable={canWrite}
            level={level}
            isDragging={isDragging}
            isDropTarget={isDropTarget}
            canWrite={canWrite}
            onDragStart={(e) => onDragStart(e, dept.id)}
            onDragOver={(e) => onDragOver(e, dept.id)}
            onDragLeave={onDragLeave}
            onDrop={(e) => {
                e.preventDefault();
                onDrop(dept.id);
            }}
        >
            {canWrite && (
                <DragIndicatorRoundedIcon
                    sx={{ fontSize: 16, color: 'text.disabled', flexShrink: 0 }}
                />
            )}
            <FolderRoundedIcon
                sx={{
                    fontSize: 18,
                    color: level === 0 ? 'primary.main' : 'text.disabled',
                    flexShrink: 0,
                }}
            />
            <Typography
                sx={{
                    fontSize: '0.875rem',
                    fontWeight: level === 0 ? 600 : 400,
                    color: 'text.primary',
                }}
            >
                {dept.name}
            </Typography>
            <Typography sx={{ fontSize: '0.75rem', color: 'text.secondary' }}>
                ({dept.code})
            </Typography>
        </NodeRow>
    );

    return (
        <Box>
            {canWrite ? (
                <Tooltip
                    title={<DragGuideContent />}
                    arrow
                    placement={tooltipPlacement}
                    enterDelay={500}
                    enterNextDelay={300}
                    disableInteractive
                >
                    {row}
                </Tooltip>
            ) : (
                row
            )}
            {children.map((child) => (
                <TreeNode
                    key={child.id}
                    dept={child}
                    level={level + 1}
                    childrenByParent={childrenByParent}
                    canWrite={canWrite}
                    draggedId={draggedId}
                    dragOverKey={dragOverKey}
                    onDragStart={onDragStart}
                    onDragOver={onDragOver}
                    onDragLeave={onDragLeave}
                    onDrop={onDrop}
                />
            ))}
        </Box>
    );
}

/* --------------------------------------------------------------------------
 * Drop zones / tooltip
 * ------------------------------------------------------------------------ */

function DragGuideContent() {
    return (
        <Box sx={{ lineHeight: 1.5 }}>
            <Typography sx={{ fontSize: '0.75rem', fontWeight: 600, color: 'inherit' }}>
                드래그하여 계층 관계를 변경
            </Typography>
            <Typography sx={{ fontSize: '0.6875rem', color: 'inherit', opacity: 0.8, mt: '0.125rem' }}>
                동일 계층 내 순서는 변경되지 않습니다.
            </Typography>
        </Box>
    );
}

interface RootDropZoneProps {
    isOver: boolean;
    onDragOver: (e: DragEvent) => void;
    onDragLeave: () => void;
    onDrop: () => void;
}

function RootDropZone({ isOver, onDragOver, onDragLeave, onDrop }: RootDropZoneProps) {
    return (
        <Box
            onDragOver={onDragOver}
            onDragLeave={onDragLeave}
            onDrop={(e) => {
                e.preventDefault();
                onDrop();
            }}
            sx={(theme) => ({
                display: 'flex',
                alignItems: 'center',
                gap: 1,
                padding: '0.625rem 0.875rem',
                marginBottom: '0.5rem',
                borderRadius: '4px',
                border: '1px dashed',
                borderColor: isOver ? theme.palette.primary.main : theme.palette.divider,
                backgroundColor: isOver ? theme.palette.primarySubtle : 'transparent',
                color: isOver ? theme.palette.primary.main : theme.palette.text.secondary,
                transition: 'background-color 0.15s, border-color 0.15s, color 0.15s',
            })}
        >
            <HomeRoundedIcon sx={{ fontSize: 18 }} />
            <Typography sx={{ fontSize: '0.8125rem', userSelect: 'none' }}>최상위 부서로 이동</Typography>
        </Box>
    );
}

/* --------------------------------------------------------------------------
 * Helpers
 * ------------------------------------------------------------------------ */

function buildChildrenMap(
    depts: DepartmentInfo[],
): Map<number | null, DepartmentInfo[]> {
    const map = new Map<number | null, DepartmentInfo[]>();
    for (const dept of depts) {
        const key = dept.parentId ?? null;
        const list = map.get(key) ?? [];
        list.push(dept);
        map.set(key, list);
    }
    for (const list of map.values()) {
        list.sort((a, b) => a.code.localeCompare(b.code));
    }
    return map;
}

function isDescendant(
    ancestorId: number,
    candidateId: number,
    childrenByParent: Map<number | null, DepartmentInfo[]>,
): boolean {
    const stack: number[] = [ancestorId];
    while (stack.length > 0) {
        const id = stack.pop() as number;
        const children = childrenByParent.get(id) ?? [];
        for (const child of children) {
            if (child.id === candidateId) return true;
            stack.push(child.id);
        }
    }
    return false;
}

/* --------------------------------------------------------------------------
 * Styled
 * ------------------------------------------------------------------------ */

const PageRoot = styled(Box)(({ theme }) => ({
    margin: '-1rem',
    [theme.breakpoints.up('sm')]: { margin: '-2rem' },
}));

const PageSurface = styled(Box)(({ theme }) => ({
    backgroundColor: theme.palette.background.paper,
    padding: '1.5rem 1.25rem',
    display: 'flex',
    flexDirection: 'column',
    [theme.breakpoints.up('md')]: { padding: '2rem' },
}));

const ContentBox = styled(Box)({
    width: '100%',
    maxWidth: 960,
    marginLeft: 'auto',
    marginRight: 'auto',
});

interface NodeRowStyleProps {
    level: number;
    isDragging: boolean;
    isDropTarget: boolean;
    canWrite: boolean;
}

const NodeRow = styled(Box, {
    shouldForwardProp: (prop) =>
        !['level', 'isDragging', 'isDropTarget', 'canWrite'].includes(prop as string),
})<NodeRowStyleProps>(({ theme, level, isDragging, isDropTarget, canWrite }) => ({
    paddingLeft: `${level * 1.75 + 0.5}rem`,
    paddingTop: '0.625rem',
    paddingBottom: '0.625rem',
    paddingRight: '0.5rem',
    display: 'flex',
    alignItems: 'center',
    gap: '0.5rem',
    borderRadius: '4px',
    transition: 'background-color 0.12s, opacity 0.12s, outline-color 0.12s',
    cursor: canWrite ? (isDragging ? 'grabbing' : 'grab') : 'default',
    opacity: isDragging ? 0.4 : 1,
    backgroundColor: isDropTarget ? theme.palette.primarySubtle : 'transparent',
    outline: isDropTarget ? `1px dashed ${theme.palette.primary.main}` : '1px solid transparent',
    outlineOffset: 1,
    '&:hover': {
        backgroundColor:
            !isDragging && !isDropTarget ? 'rgba(15, 23, 42, 0.03)' : undefined,
    },
}));
