import { useMemo } from 'react';
import AddRoundedIcon from '@mui/icons-material/AddRounded';
import CloseRoundedIcon from '@mui/icons-material/CloseRounded';
import IconButton from '@mui/material/IconButton';
import type {
  CodeRuleAttributeDescriptor,
  CodeRuleAttributeMapping,
} from '@/features/codeRule/types';
import {
  AddTokenButton,
  CardBody,
  CardHeader,
  CardRoot,
  CardTitle,
  Chip,
  ChipLabel,
  ChipMappingLine,
  ChipMappings,
  ChipsRow,
  EmptyHint,
  GroupBlock,
  GroupLabel,
} from './TokenChipsCard.styles';
import * as React from "react";

interface Props {
  hasParent: boolean;
  /** 도메인이 등록한 attribute descriptor — 분류값 토큰 chip 의 라벨 / 매핑 lookup */
  attributes: CodeRuleAttributeDescriptor[];
  /** 사용자가 토큰 만들기로 추가한 literal 들 — 카드에 chip 으로 노출, 드래그로 패턴 삽입 */
  customLiterals: string[];
  /** 분류값 매핑 — 매핑이 있는 KEY 별로 chip 1개 + 매핑 entries inline */
  mappings: CodeRuleAttributeMapping[];
  /** chip 클릭 시 호출 — 패턴의 cursor 위치에 토큰 삽입 */
  onSelectToken: (token: string) => void;
  /** 사용자 추가 literal chip 의 X 클릭 */
  onRemoveLiteral: (literal: string) => void;
  /** 분류값 매핑 entry 의 X 클릭 — 매핑 1건 제거 */
  onRemoveMapping: (attributeKey: string, sourceValue: string) => void;
  onOpenTokenBuilder: () => void;
}

interface BuiltinGroup {
  title: string;
  tokens: string[];
}

const BUILTIN_DATE_TOKENS: BuiltinGroup = {
  title: '날짜',
  tokens: ['{YYYY}', '{YY}', '{MM}', '{DD}'],
};

const BUILTIN_SEQUENCE_TOKENS: BuiltinGroup = {
  title: '시퀀스',
  tokens: ['{SEQ:3}', '{SEQ:4}', '{SEQ:5}', '{SEQ:6}'],
};

const BUILTIN_PARENT_TOKEN = '{PARENT}';

/**
 * 토큰 팔레트 — 기본 chip + 사용자 추가 chip.
 * <p>
 * chip 클릭 = 패턴 cursor 위치 삽입. chip 드래그 = 패턴 input 으로 drop 시 위치 삽입.
 * 사용자 추가 chip (사용자 입력값 literal / 분류값 토큰) 은 토큰 만들기 버튼으로 추가.
 */
export default function TokenChipsCard({
  hasParent,
  attributes,
  customLiterals,
  mappings,
  onSelectToken,
  onRemoveLiteral,
  onRemoveMapping,
  onOpenTokenBuilder,
}: Props) {
  // 분류 KEY 중 매핑이 1건 이상 정의된 것만 chip 으로 노출
  const filledMappings = useMemo(
    () => mappings.filter((m) => m.codeValue.trim() !== ''),
    [mappings],
  );
  const attrKeysWithMappings = useMemo(() => {
    const seen = new Set<string>();
    for (const m of filledMappings) {
      seen.add(m.attributeKey);
    }
    return Array.from(seen);
  }, [filledMappings]);

  const handleDragStart = (token: string) => (e: React.DragEvent) => {
    e.dataTransfer.setData('text/plain', token);
    e.dataTransfer.effectAllowed = 'copy';
  };

  const renderBuiltinChip = (token: string) => (
    <Chip
      key={token}
      data-kind="builtin"
      draggable
      onDragStart={handleDragStart(token)}
      onClick={() => onSelectToken(token)}
      title={`클릭 또는 드래그로 패턴에 ${token} 삽입`}
    >
      <ChipLabel>{token}</ChipLabel>
    </Chip>
  );

  return (
    <CardRoot>
      <CardHeader>
        <CardTitle>토큰</CardTitle>
        <AddTokenButton
          type="button"
          onClick={onOpenTokenBuilder}
          startIcon={<AddRoundedIcon sx={{ fontSize: 16 }} />}
        >
          토큰 만들기
        </AddTokenButton>
      </CardHeader>
      <CardBody>
        <GroupBlock>
          <GroupLabel>{BUILTIN_DATE_TOKENS.title}</GroupLabel>
          <ChipsRow>{BUILTIN_DATE_TOKENS.tokens.map(renderBuiltinChip)}</ChipsRow>
        </GroupBlock>
        <GroupBlock>
          <GroupLabel>{BUILTIN_SEQUENCE_TOKENS.title}</GroupLabel>
          <ChipsRow>{BUILTIN_SEQUENCE_TOKENS.tokens.map(renderBuiltinChip)}</ChipsRow>
        </GroupBlock>
        {hasParent && (
          <GroupBlock>
            <GroupLabel>부모</GroupLabel>
            <ChipsRow>{renderBuiltinChip(BUILTIN_PARENT_TOKEN)}</ChipsRow>
          </GroupBlock>
        )}
        {customLiterals.length > 0 && (
          <GroupBlock>
            <GroupLabel>사용자 입력값</GroupLabel>
            <ChipsRow>
              {customLiterals.map((literal, idx) => (
                <Chip
                  key={`${idx}-${literal}`}
                  data-kind="literal"
                  draggable
                  onDragStart={handleDragStart(literal)}
                  onClick={() => onSelectToken(literal)}
                  title={`클릭 또는 드래그로 패턴에 "${literal}" 삽입`}
                >
                  <ChipLabel>
                    {literal === ' ' ? '␣' : literal}
                    <IconButton
                      size="small"
                      onClick={(e) => {
                        e.stopPropagation();
                        onRemoveLiteral(literal);
                      }}
                      sx={{ ml: 0.25, p: '2px', color: 'inherit', opacity: 0.6, '&:hover': { opacity: 1 } }}
                      aria-label="입력값 제거"
                    >
                      <CloseRoundedIcon sx={{ fontSize: 13 }} />
                    </IconButton>
                  </ChipLabel>
                </Chip>
              ))}
            </ChipsRow>
          </GroupBlock>
        )}
        {attrKeysWithMappings.length > 0 && (
          <GroupBlock>
            <GroupLabel>분류값</GroupLabel>
            <ChipsRow>
              {attrKeysWithMappings.map((key) => {
                const descriptor = attributes.find((a) => a.key === key);
                const attrMappings = filledMappings.filter((m) => m.attributeKey === key);
                const token = `{${key}}`;
                return (
                  <Chip
                    key={key}
                    data-kind="attribute"
                    draggable
                    onDragStart={handleDragStart(token)}
                    onClick={() => onSelectToken(token)}
                    title={`클릭 또는 드래그로 패턴에 ${token} 삽입`}
                  >
                    <ChipLabel>{token}</ChipLabel>
                    <ChipMappings>
                      {attrMappings.map((m) => {
                        const srcLabel = descriptor?.values.find((v) => v.value === m.sourceValue)?.label
                          ?? m.sourceValue;
                        return (
                          <ChipMappingLine key={`${m.attributeKey}|${m.sourceValue}`}>
                            <span>{srcLabel}</span>
                            <span className="arrow">→</span>
                            <span className="code">{m.codeValue}</span>
                            <IconButton
                              size="small"
                              onClick={(e) => {
                                e.stopPropagation();
                                onRemoveMapping(m.attributeKey, m.sourceValue);
                              }}
                              sx={{ ml: 0.5, p: '1px', opacity: 0.5, '&:hover': { opacity: 1 } }}
                              aria-label="매핑 제거"
                            >
                              <CloseRoundedIcon sx={{ fontSize: 11 }} />
                            </IconButton>
                          </ChipMappingLine>
                        );
                      })}
                    </ChipMappings>
                  </Chip>
                );
              })}
            </ChipsRow>
          </GroupBlock>
        )}
        {customLiterals.length === 0 && attrKeysWithMappings.length === 0 && (
          <EmptyHint>
            토큰 만들기로 사용자 입력값 / 분류값을 추가할 수 있습니다.
          </EmptyHint>
        )}
      </CardBody>
    </CardRoot>
  );
}
