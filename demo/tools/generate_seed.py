#!/usr/bin/env python3
"""Generate the deterministic Simple ERP demo seed bundle.

The generated SQL stays time-relative: reset time is captured once in @seed_now.
All archive metadata, UUIDs, fixture bytes, JSON ordering and line endings are fixed,
so identical inputs produce byte-for-byte identical outputs.
"""

from __future__ import annotations

import argparse
import datetime as dt
import gzip
import hashlib
import io
import json
import os
from pathlib import Path
import struct
import tarfile
import tempfile
import uuid
import zipfile
import zlib


SCHEMA_SOURCE_COMMIT = "7a6925e095ab371dfc76dd2d0c2fadea550b5db9"
COMPATIBLE_APP_VERSION = "0.0.1-demo-schema-v3"
SEED_VERSION = "2026.08.08.1"
SCHEMA_VERSION = "7a6925e0-post-migrator-v1"
SCENARIO_VERSION = "demo-v2"
DEFAULT_SOURCE_DATE_EPOCH = 1786114800
KST = dt.timezone(dt.timedelta(hours=9))
MANAGER_PASSWORD = "ManagerDemo!2026"
STAFF_PASSWORD = "StaffDemo!2026"
MANAGER_HASH = "$2a$10$yGoM3b.6oo5BhmdLT1KCBOq002lxF9W5N.Zq6546SK24q4VmS2VKG"
STAFF_HASH = "$2a$10$GnmNeh1fcMAFoiFSkKw1cOkfLE.bIj1PIcHP69cCcmDlFKK5Qm6Xi"
LOCKED_PASSWORD_HASH = "$2a$10$25XfA5OernCEENFnr7UyK.E2qIfwz8k1LmsNP3dscHOyW85RZ1sci"
NAMESPACE = uuid.UUID("893c0ddb-a51c-5f45-b0ac-aab2fd1ccb54")

# These identities and organizations are deliberately invented for the demo.
# Contact channels stay in reserved/invalid ranges even though the labels shown
# in the UI read like ordinary business data.
EMPLOYEE_IDENTITIES = (
    ("김도윤", "doyun.kim"),
    ("이서연", "seoyeon.lee"),
    ("박지훈", "jihoon.park"),
    ("최유진", "yujin.choi"),
    ("정민석", "minseok.jung"),
    ("강하윤", "hayoon.kang"),
    ("조현우", "hyunwoo.cho"),
    ("윤수빈", "subin.yoon"),
    ("장태준", "taejun.jang"),
    ("임채원", "chaewon.lim"),
    ("한서준", "seojun.han"),
    ("오지민", "jimin.oh"),
    ("서민준", "minjun.seo"),
    ("신예린", "yerin.shin"),
    ("권도현", "dohyun.kwon"),
    ("황가은", "gaeun.hwang"),
    ("안재원", "jaewon.ahn"),
    ("송나연", "nayeon.song"),
    ("류시우", "siwoo.ryu"),
    ("전하린", "harin.jeon"),
    ("문준혁", "junhyeok.moon"),
    ("백소윤", "soyoon.baek"),
)
EMPLOYEE_DEPARTMENT_IDS = (
    6, 5, 8, 5, 6, 7, 7, 7, 7, 7, 7,
    5, 6, 8, 8, 5, 6, 7, 8, 5, 6, 7,
)
EMPLOYEE_POSITION_IDS = (
    2, 5, 5, 4, 4, 5, 4, 5, 4, 5, 6,
    5, 4, 5, 6, 4, 5, 4, 5, 5, 6, 6,
)
SALES_EMPLOYEE_IDS = (2, 4, 5, 12, 13, 16, 17, 1)
SALES_ACTIVITY_COUNT_PLAN = (
    2, 3, 5, 2, 4, 1, 2, 4, 1, 3, 4, 1,
    3, 5, 2, 3, 5, 2, 3, 5, 2, 4, 1, 3,
    4, 1, 3, 4, 1, 3, 5, 2, 3, 5, 2, 4,
    5, 2, 4, 1, 3, 4, 1, 3, 5, 1, 3, 5,
)
SALES_ACTIVITY_SUBJECT_CATALOG = (
    (
        "현장 생산 조건 확인",
        "설비 배치 동선 점검",
        "시운전 환경 사전 확인",
        "작업자 동선 및 안전구역 확인",
    ),
    (
        "설비 사양 유선 협의",
        "견적 옵션 추가 확인",
        "납품 일정 유선 조율",
        "기술 담당자 후속 통화",
    ),
    (
        "도입 일정 회의",
        "투자 검토 실무 회의",
        "설치 준비사항 점검 회의",
        "계약 조건 조율 회의",
    ),
    (
        "견적서 및 사양서 전달",
        "설치 준비자료 이메일 공유",
        "기술 검토 결과 전달",
        "요청 도면 및 자료 송부",
    ),
    (
        "투자 예산 일정 확인",
        "지원사업 적용 여부 검토",
        "내부 승인 일정 후속 확인",
        "구매 검토 일정 확인",
    ),
)

EMPLOYEE_ADDRESS_CATALOG = (
    ("서울특별시 새온구 솔빛로 74", "도담채 104동 1203호", "98143"),
    ("경기도 다온시 여울로 128", "다온마루 202동 804호", "98427"),
    ("인천광역시 미르구 온샘로 31", "미르하임 103동 1502호", "98216"),
    ("대전광역시 해온구 나래길 56", "해온빌리지 201동 603호", "98735"),
    ("충청북도 가람시 도담로 142", "가람채 105동 1101호", "98308"),
    ("부산광역시 윤슬구 새결로 89", "윤슬마을 304동 1704호", "98564"),
    ("광주광역시 솔누리구 한빛로 23", "솔누리파크 102동 902호", "98097"),
    ("대구광역시 온결구 이음길 117", "온결하우스 203동 1403호", "98921"),
    ("서울특별시 새온구 나래로 153", "새온리버 108동 702호", "98612"),
    ("경기도 다온시 온샘길 42", "여울마을 301동 1301호", "98273"),
    ("인천광역시 미르구 도담로 96", "미르파크 205동 1004호", "98845"),
    ("대전광역시 해온구 이음로 135", "나래하임 101동 1602호", "98391"),
    ("충청북도 가람시 새결길 68", "가람마루 302동 503호", "98507"),
    ("부산광역시 윤슬구 한빛로 27", "윤슬채 106동 1901호", "98184"),
    ("광주광역시 솔누리구 여울길 164", "솔빛마을 204동 1104호", "98702"),
    ("대구광역시 온결구 나래로 51", "이음파크 107동 802호", "98463"),
    ("서울특별시 새온구 도담길 109", "새온마루 303동 1401호", "98038"),
    ("경기도 다온시 솔빛로 36", "다온하임 202동 602호", "98816"),
    ("인천광역시 미르구 새결로 147", "온샘마을 105동 1803호", "98259"),
    ("대전광역시 해온구 한빛길 82", "해온채 301동 904호", "98671"),
    ("충청북도 가람시 이음로 19", "도담파크 103동 1201호", "98346"),
    ("부산광역시 윤슬구 여울로 121", "윤슬하임 206동 1504호", "98908"),
)

CONTACT_SURNAMES = (
    ("김", "Kim"), ("이", "Lee"), ("박", "Park"), ("최", "Choi"),
    ("정", "Jung"), ("강", "Kang"), ("조", "Cho"), ("윤", "Yoon"),
    ("장", "Jang"), ("임", "Lim"), ("한", "Han"), ("오", "Oh"),
)
CONTACT_GIVEN_NAMES = (
    ("도현", "Dohyeon"),
    ("서윤", "Seoyun"),
    ("민재", "Minjae"),
    ("하린", "Harin"),
    ("준서", "Junseo"),
    ("지우", "Jiwoo"),
)
CONTACT_PERMUTATION_MULTIPLIER = 17
CONTACT_PERMUTATION_OFFSET = 11

CUSTOMER_NAMES = (
    "미르온정밀", "솔누리금속", "해온기공", "온결산업", "아른테크", "라움메탈",
    "다솜정공", "윤슬설비", "누온레이저", "가람프레임", "모아진기계", "세온플랜트",
    "도담모션", "하람스틸", "리안정밀", "새론기공", "이루온메탈", "담우산업",
    "마루빛테크", "오름정공", "나래금속", "늘품기계", "한울프레임", "다원설비",
    "이로운테크", "솔찬정밀", "해들메탈", "시온기공", "라온웍스", "온새미산업",
    "미르재금속", "가온정밀", "보담기계", "여울메탈", "두온산업", "아람정공",
    "해담프레임", "루온테크", "모루기공", "도란설비", "이든정밀", "소담메탈",
    "가람솔루션", "온빛기계", "푸름산업", "해솔정공", "다온플랜트", "새결테크",
)

CUSTOMER_ROADS = (
    "경기도 새온시 산업로",
    "충청남도 해담시 테크노로",
    "경상북도 미르시 국가산단로",
    "경상남도 온결시 공단중앙로",
    "전북특별자치도 다온시 혁신산업로",
    "전라남도 윤슬시 기업도시로",
    "강원특별자치도 솔누리시 첨단로",
    "충청북도 가람시 미래산업로",
)

SUPPLIERS = (
    ("NOVAMACH INDUSTRIES", "노바맥시온", "대한민국", "NMX"),
    ("AURORA LASERWORKS", "오로라레이저웍스", "독일", "ALW"),
    ("BLUEMET SYSTEMS", "블루메트시스템즈", "대한민국", "BMS"),
    ("HANGYEOL AUTOMATION", "한결오토메이션", "일본", "HGA"),
    ("LUMINA FABTECH", "루미나팹테크", "이탈리아", "LFT"),
    ("DAON MACHINERY", "다온머시너리", "대한민국", "DAM"),
    ("IEUM ROBOTICS", "이음로보틱스", "대한민국", "IER"),
    ("SAEBIT WELDING", "새빛웰딩시스템", "중국", "SBW"),
)

EXTERNAL_COMPANIES = (
    "에버온모션", "모노웨이브", "루미코어", "에이든팩토리", "브릭스메카",
    "오브릭시스템", "네오담테크", "파인루트", "플로온기공", "에코베인",
    "로움산업", "이노브릿지",
)

SERVICE_TYPE_PLAN = (
    "REPAIR", "INSTALL_SUPPORT", "REPAIR", "TRAINING", "TUNING",
    "REPAIR", "INSTALL_SUPPORT", "INTERPRET", "REPAIR", "TUNING",
    "INSTALL_SUPPORT", "REPAIR", "TRAINING", "REPAIR", "INSTALL_SUPPORT",
    "TUNING", "REPAIR", "TRAINING", "INTERPRET", "INSTALL_SUPPORT",
    "REPAIR", "TUNING", "INSTALL_SUPPORT", "REPAIR", "TRAINING",
    "REPAIR", "INTERPRET", "INSTALL_SUPPORT", "TUNING", "REPAIR",
    "TRAINING", "INSTALL_SUPPORT", "REPAIR", "INTERPRET", "TUNING",
    "INSTALL_SUPPORT", "INSTALL_SUPPORT", "TRAINING", "REPAIR", "TUNING",
    "INSTALL_SUPPORT", "REPAIR", "INTERPRET", "TRAINING", "TRAINING",
)

AUDIT_TARGET_CATALOG = (
    ("CUSTOMERS", "Customer", 48),
    ("CONTRACTS", "Contract", 42),
    ("AFTER_SERVICES", "AfterService", 45),
    ("APPROVALS", "ApprovalDocument", 36),
    ("EXPENSES", "ExpenseClaim", 12),
    ("ATTENDANCE", "LeaveRequest", 16),
    ("BOARDS", "Post", 28),
)

# SQL rows, archive bytes and manifest metadata are all derived from this catalog.
# Business reference tables are generated separately and verified against it after import.
FILE_CATALOG = (
    (1, "영업회의_현황_01.pdf", "CLAIMED", "DRIVE_FILE", 1, 1),
    (2, "장비점검_일정_01.pdf", "CLAIMED", "DRIVE_FILE", 2, 2),
    (3, "현장방문_체크리스트_01.pdf", "CLAIMED", "DRIVE_FILE", 3, 1),
    (4, "설치일정_동선도_01.pdf", "CLAIMED", "DRIVE_FILE", 4, 2),
    (5, "출장계획_첨부_01.pdf", "CLAIMED", "DRIVE_FILE", 5, 1),
    (6, "기술교육_운영안_01.pdf", "CLAIMED", "DRIVE_FILE", 6, 2),
    (7, "고객요구사항_정리_01.xlsx", "CLAIMED", "DRIVE_FILE", 7, 1),
    (8, "고객요구사항_정리_02.xlsx", "CLAIMED", "DRIVE_FILE", 8, 2),
    (9, "고객요구사항_정리_03.xlsx", "CLAIMED", "DRIVE_FILE", 9, 1),
    (10, "고객요구사항_정리_04.xlsx", "CLAIMED", "DRIVE_FILE", 10, 2),
    (11, "장비점검_안내_01.txt", "CLAIMED", "DRIVE_FILE", 11, 1),
    (12, "장비점검_안내_02.txt", "CLAIMED", "DRIVE_FILE", 12, 2),
    (13, "영업회의_회의록_첨부.pdf", "CLAIMED", "BOARD_POST", 1, 3),
    (14, "정기점검_일정표.pdf", "CLAIMED", "BOARD_POST", 2, 1),
    (15, "현장방문_체크리스트.pdf", "CLAIMED", "BOARD_POST", 3, 5),
    (16, "설치동선_검토자료.pdf", "CLAIMED", "BOARD_POST", 4, 6),
    (17, "미르온정밀_출장계획.pdf", "CLAIMED", "APPROVAL_DOCUMENT", 1, 2),
    (18, "솔누리금속_출장계획.pdf", "CLAIMED", "APPROVAL_DOCUMENT", 2, 1),
    (19, "출장비_증빙묶음_01.png", "CLAIMED", "EXPENSE_CLAIM", 1, 2),
    (20, "출장비_증빙묶음_02.png", "CLAIMED", "EXPENSE_CLAIM", 2, 2),
    (21, "출장비_증빙묶음_03.png", "CLAIMED", "EXPENSE_CLAIM", 3, 2),
    (22, "출장비_증빙묶음_04.png", "CLAIMED", "EXPENSE_CLAIM", 4, 2),
    (23, "출장비_증빙묶음_05.png", "CLAIMED", "EXPENSE_CLAIM", 5, 2),
    (24, "출장비_증빙묶음_06.png", "CLAIMED", "EXPENSE_CLAIM", 6, 2),
    (25, "출장비_증빙묶음_07.png", "CLAIMED", "EXPENSE_CLAIM", 7, 10),
    (26, "출장비_증빙묶음_08.png", "CLAIMED", "EXPENSE_CLAIM", 8, 11),
    (27, "출장비_증빙묶음_09.png", "CLAIMED", "EXPENSE_CLAIM", 9, 12),
    (28, "출장비_증빙묶음_10.png", "CLAIMED", "EXPENSE_CLAIM", 10, 3),
    (29, "출장비_증빙묶음_11.png", "CLAIMED", "EXPENSE_CLAIM", 11, 4),
    (30, "출장비_증빙묶음_12.png", "CLAIMED", "EXPENSE_CLAIM", 12, 5),
)
DRIVE_FOLDER_CATALOG = (
    (1, "공용자료", None),
    (2, "프로젝트자료", 1),
    (3, "기술자료", 1),
    (4, "영업자료", 1),
    (5, "고객요구사항", 4),
    (6, "서비스점검", 3),
    (7, "교육자료", 3),
    (8, "업무양식", 1),
    (9, "과거자료", 1),
    (10, "이용안내", None),
)
DRIVE_FILE_FOLDER_IDS = (4, 6, 8, 2, 2, 7, 5, 5, 5, 5, 10, 10)

# Each row is intentionally distinct because recent-first board lists otherwise
# expose generator repetition before a viewer reaches the detailed workflows.
POST_CATALOG = (
    ("MEETING", "주간 영업 파이프라인 점검", "이번 주 신규 상담 6건의 예상 발주 시점과 다음 연락 일정을 담당자별로 확인합니다."),
    ("NOTICE", "하반기 장비 정기 점검 일정 안내", "보증 만료 예정 장비부터 순차 방문합니다. 고객과 합의한 정지 가능 시간을 서비스 일정에 등록해 주세요."),
    ("FREE", "현장 방문 전 체크리스트 공유", "전원 용량, 집진 배관, 장비 반입구 폭을 방문 전에 확인할 수 있도록 현장 체크리스트를 공유합니다."),
    ("MEETING", "미르온정밀 설치 동선 사전 검토", "본체와 집진기 반입 순서, 지게차 대기 위치, 설치 당일 고객 담당자 역할을 확정합니다."),
    ("NOTICE", "서비스 엔지니어 안전교육 일정", "현장 작업 전 잠금장치와 보호구 점검 절차를 중심으로 안전교육을 진행합니다."),
    ("FREE", "레이저 보호창 교체 요령", "오염 확인부터 교체 후 출력 시험까지 현장에서 자주 놓치는 순서를 정리했습니다."),
    ("MEETING", "월간 계약·입금 예정 점검", "계약 단계별 선수금과 잔금 예정일을 대조하고 지연 가능 건의 고객 연락 계획을 정리합니다."),
    ("NOTICE", "휴가철 비상 연락 체계 안내", "기술지원 당번과 부품 출고 담당을 확인하고 긴급 장애 접수 시 인계 순서를 공유합니다."),
    ("FREE", "장거리 현장 방문 차량 점검 팁", "출발 전 타이어와 소모품을 확인하고 장비별 공구 상자가 실렸는지 함께 점검해 주세요."),
    ("MEETING", "솔누리금속 납품 전 사양 확정", "가공 소재 두께와 자동 로딩 범위를 기준으로 최종 옵션, 교육 일정, 검수 조건을 확정합니다."),
    ("NOTICE", "기술 문서 표준 양식 개정 안내", "현장 점검표와 설치 확인서의 고객 확인란을 통일했습니다. 새 양식은 다음 방문부터 사용해 주세요."),
    ("FREE", "고객 응대 회의록 작성 예시", "결정 사항, 미확정 항목, 다음 담당자와 기한이 한눈에 보이도록 작성한 회의록 예시를 공유합니다."),
    ("MEETING", "보증 만료 예정 고객 대응 회의", "90일 안에 보증이 끝나는 장비를 확인하고 사전 점검과 유지보수 계약 안내 대상을 선정합니다."),
    ("NOTICE", "고객 시연회 운영 동선 공지", "장비 가동 구역과 상담 구역을 분리하고 방문 고객의 보호구 착용과 사진 촬영 기준을 안내합니다."),
    ("FREE", "출장비 증빙 정리 방법", "교통·숙박 영수증을 출장 건별로 묶고 누락 여부를 확인하는 간단한 정리 순서를 공유합니다."),
    ("MEETING", "서비스 예비부품 재고 점검", "보호창, 노즐, 센서의 최근 사용량을 기준으로 안전 재고와 다음 발주 수량을 검토합니다."),
    ("NOTICE", "장비 반입 시 출입 절차 안내", "방문자 명단과 차량 정보를 전날까지 전달하고 반입 장비의 중량표를 함께 준비해 주세요."),
    ("FREE", "네스팅 교육 자료 추천", "초보 작업자가 소재 배치와 공정 순서를 연습할 수 있는 단계별 교육 자료를 모았습니다."),
    ("MEETING", "신규 영업 접점 후속 조치 점검", "산업전과 홈페이지에서 유입된 상담을 관심 품목과 도입 시점으로 나누어 후속 연락 우선순위를 정합니다."),
    ("NOTICE", "문서 보관 기준 정기 점검", "계약서와 설치 확인서는 지정 폴더에 보관하고 임시 사본은 검토 완료 후 정리해 주세요."),
    ("FREE", "현장 사진 촬영 기준 공유", "고객 정보가 노출되지 않도록 촬영 범위를 조정하고 장비 상태를 재현할 수 있는 필수 각도를 정리했습니다."),
    ("MEETING", "기술지원 사례 회고", "원점 센서 알람과 집진기 인터록 사례의 진단 순서, 고객 안내, 재발 방지 항목을 함께 검토합니다."),
    ("NOTICE", "분기 연락처 정확성 점검 안내", "퇴사자와 담당 변경 정보를 확인해 고객 연락처와 영업 인계 기록을 최신 상태로 유지해 주세요."),
    ("FREE", "설치 완료 후 고객 인계 항목", "시운전 결과, 교육 참석자, 소모품 위치, 다음 점검 예정일을 고객에게 인계할 때 확인할 항목입니다."),
    ("MEETING", "다음 달 설치 일정 리스크 점검", "동시 설치가 예정된 현장의 인력, 운송, 부품 준비 상태를 대조하고 충돌 일정을 조정합니다."),
    ("NOTICE", "연휴 전 고객 연락망 확인", "진행 중인 설치와 서비스 건의 주 담당·대체 담당 연락처를 연휴 전에 다시 확인해 주세요."),
    ("FREE", "출장 준비물 체크리스트", "노트북 어댑터, 통신 케이블, 측정기, 보호구처럼 자주 빠지는 준비물을 한 장으로 정리했습니다."),
    ("MEETING", "월말 매출 전망 및 수금 계획", "계약 진행률과 검수 일정을 기준으로 이번 달 인식 가능 매출과 수금 후속 조치를 최종 점검합니다."),
)

ROOT = Path(__file__).resolve().parents[2]
DEFAULT_OUTPUT = ROOT / "demo" / "seed"
VERIFY_FILES_BEGIN = "-- BEGIN GENERATED FILE EXPECTATIONS"
VERIFY_FILES_END = "-- END GENERATED FILE EXPECTATIONS"


class Raw(str):
    """SQL expression that must not be quoted."""


def sql_value(value: object) -> str:
    if isinstance(value, Raw):
        return str(value)
    if value is None:
        return "NULL"
    if isinstance(value, bool):
        return "1" if value else "0"
    if isinstance(value, (int, float)):
        return str(value)
    text = str(value).replace("\\", "\\\\").replace("'", "''")
    return f"'{text}'"


class SqlWriter:
    def __init__(self) -> None:
        self.lines: list[str] = []

    def line(self, value: str = "") -> None:
        self.lines.append(value)

    def insert(self, table: str, columns: list[str], rows: list[tuple[object, ...]], chunk: int = 100) -> None:
        for start in range(0, len(rows), chunk):
            part = rows[start : start + chunk]
            self.line(f"INSERT INTO `{table}` ({', '.join(f'`{c}`' for c in columns)}) VALUES")
            for index, row in enumerate(part):
                suffix = "," if index + 1 < len(part) else ";"
                self.line("  (" + ", ".join(sql_value(v) for v in row) + ")" + suffix)
        if rows:
            self.line()

    def render(self) -> bytes:
        return ("\n".join(self.lines).rstrip() + "\n").encode("utf-8")


def business_clock(salt: int) -> str:
    """Return a stable workday clock that cannot inherit reset wall-clock time."""
    minute_of_day = 8 * 60 + 20 + ((salt * salt * 17 + salt * 43 + 29) % 541)
    return f"{minute_of_day // 60:02d}:{minute_of_day % 60:02d}:00"


def business_ts(date_expression: object, salt: int, *, clamp_to_reset: bool = False) -> Raw:
    expression = f"TIMESTAMP({date_expression}, '{business_clock(salt)}')"
    if clamp_to_reset:
        expression = f"LEAST({expression}, @seed_now)"
    return Raw(expression)


def rel_ts(days: int, salt: int | None = None) -> Raw:
    """Return a date-relative deterministic business timestamp."""
    timestamp_salt = days if salt is None else salt
    return business_ts(
        f"DATE_SUB(@seed_today, INTERVAL {days} DAY)",
        timestamp_salt,
        clamp_to_reset=days == 0,
    )


def rel_date(days: int) -> Raw:
    return Raw(f"DATE_SUB(@seed_today, INTERVAL {days} DAY)")


def month_date(months_ago: int, day_offset: int) -> Raw:
    base = f"DATE_ADD(DATE_SUB(@seed_month, INTERVAL {months_ago} MONTH), INTERVAL {day_offset} DAY)"
    return Raw(f"LEAST({base}, @seed_today)")


def date_add(expression: object, days: int) -> Raw:
    return Raw(f"DATE_ADD({expression}, INTERVAL {days} DAY)")


def source_datetime() -> dt.datetime:
    epoch = int(os.getenv("SOURCE_DATE_EPOCH", str(DEFAULT_SOURCE_DATE_EPOCH)))
    return dt.datetime.fromtimestamp(epoch, tz=dt.timezone.utc).astimezone(KST)


def contact_identity(index: int) -> tuple[str, str]:
    """Return a unique fictional identity without clustering adjacent records."""
    catalog_size = len(CONTACT_SURNAMES) * len(CONTACT_GIVEN_NAMES)
    slot = (
        (index - 1) * CONTACT_PERMUTATION_MULTIPLIER
        + CONTACT_PERMUTATION_OFFSET
    ) % catalog_size
    surname_ko, surname_en = CONTACT_SURNAMES[slot % len(CONTACT_SURNAMES)]
    given_ko, given_en = CONTACT_GIVEN_NAMES[slot // len(CONTACT_SURNAMES)]
    return f"{surname_ko}{given_ko}", f"{given_en} {surname_en}"


def invalid_number_suffix(index: int, multiplier: int, offset: int) -> int:
    """Return a unique, visibly non-sequential suffix in the invalid 000x range."""
    return 1000 + ((index * multiplier + offset) % 8000)


def employee_phone(index: int) -> str:
    return f"010-0000-{invalid_number_suffix(index, 3191, 211):04d}"


def contact_channels(index: int, name_en: str) -> tuple[str, str, str, str]:
    account = name_en.lower().replace(" ", ".")
    return (
        f"{account}@partners.example",
        f"010-0001-{invalid_number_suffix(index, 2879, 503):04d}",
        f"02-0000-{invalid_number_suffix(index, 3557, 907):04d}",
        f"{account}@contacts.example",
    )


def contact_business_role(index: int) -> tuple[str, str, str]:
    """Return note, department and position for a customer's current contact."""
    if index <= len(CUSTOMER_NAMES):
        return "설비 투자 최종 의사결정자", "경영관리", "대표이사"
    secondary_roles = (
        ("생산 조건 및 설비 사양 협의 담당", "생산기술팀", "부장"),
        ("견적 및 계약 실무 담당", "구매팀", "차장"),
        ("장비 점검 및 서비스 일정 담당", "설비보전팀", "과장"),
    )
    return secondary_roles[(index - len(CUSTOMER_NAMES) - 1) % len(secondary_roles)]


def attendance_profile(
    employee_id: int,
    day_offset: int,
) -> tuple[str, str, float, float, float, float]:
    """Create stable employee/day variation around the simulated office point."""
    check_in_minutes = 8 * 60 + 38 + ((employee_id * 7 + day_offset * 11) % 23)
    check_out_minutes = 17 * 60 + 35 + ((employee_id * 13 + day_offset * 7) % 61)

    def clock(total_minutes: int) -> str:
        return f"{total_minutes // 60:02d}:{total_minutes % 60:02d}:00"

    check_in_latitude = round(37.5663 + (((employee_id * 5 + day_offset * 3) % 9) - 4) * 0.00001, 6)
    check_in_longitude = round(126.9779 + (((employee_id * 3 + day_offset * 5) % 9) - 4) * 0.00001, 6)
    check_out_latitude = round(37.5663 + (((employee_id * 7 + day_offset * 2) % 9) - 4) * 0.00001, 6)
    check_out_longitude = round(126.9779 + (((employee_id * 2 + day_offset * 7) % 9) - 4) * 0.00001, 6)
    return (
        clock(check_in_minutes), clock(check_out_minutes),
        check_in_latitude, check_in_longitude,
        check_out_latitude, check_out_longitude,
    )


def customer_address(index: int) -> tuple[str, str, str]:
    road = CUSTOMER_ROADS[((index - 1) * 5 + 3) % len(CUSTOMER_ROADS)]
    road_number = 20 + ((index * index * 38 + index * 53 + 17) % 380)
    building = 1 + ((index * index * 3 + index * 7 + 2) % 5)
    unit = 101 + ((index * index * 42 + index * 65 + 9) % 780)
    zip_code = 91000 + ((index * index * 173 + index * 283 + 731) % 8000)
    return f"{road} {road_number}", f"{building}공장 {unit}호", f"{zip_code:05d}"


def customer_channels(index: int) -> tuple[str, str, str, str]:
    slug = f"{CUSTOMER_NAMES[index - 1].lower()}-{(index * index * 17 + index * 37 + 19) % 997:03d}"
    return (
        f"office@{slug}.example",
        f"02-0000-{1000 + ((index * index * 719 + index * 2539 + 1207) % 8000):04d}",
        f"02-0000-{1000 + ((index * index * 997 + index * 3319 + 1801) % 8000):04d}",
        f"https://{slug}.example",
    )


def employee_dates(index: int) -> tuple[int, int]:
    """Return non-uniform birth/join day offsets with realistic tenure order."""
    birth_days_ago = 8300 + ((index * index * 113 + index * 487 + 613) % 5600)
    join_days_ago = 240 + ((index * index * 59 + index * 173 + 41) % 1180)
    return birth_days_ago, join_days_ago


def employee_address(index: int) -> tuple[str, str, str]:
    """Return a curated fictional residence without adjacent-number artifacts."""
    return EMPLOYEE_ADDRESS_CATALOG[index - 1]


def customer_trade_start_days_ago(index: int) -> int:
    """Return stable non-linear trade tenure without a visible fixed interval."""
    return 150 + ((index * index * 43 + index * 97 + 31) % 1103)


def contact_met_days_ago(index: int) -> int:
    """Return a unique non-monotonic first-meeting date offset."""
    return 420 + ((index * index * 80 + index + 29) % 307)


def contact_employment_start_days_ago(index: int) -> int:
    """Return a unique fictional tenure older than every first meeting."""
    return 740 + ((index * index * 80 + index + 31) % 307)


def sales_activity_contact_id(customer_id: int, activity_type: str) -> int:
    """Route working-level activities to the customer's operational contact."""
    if customer_id <= 24 and activity_type in {"VISIT", "CALL", "MEETING", "EMAIL"}:
        return 48 + customer_id
    return customer_id


def sales_activity_recent_rank(customer_id: int) -> int:
    """Return a full permutation independent of customer list order."""
    return ((customer_id - 1) * 17 + 11) % len(CUSTOMER_NAMES)


def recent_activity_clock(recent_rank: int) -> str:
    """Keep recent dashboard records distinct and before the afternoon reset."""
    minute_of_day = 9 * 60 + 5 + ((recent_rank * 131 + 47) % 359)
    return f"{minute_of_day // 60:02d}:{minute_of_day % 60:02d}:00"


def sales_activity_type_index(customer_id: int, occurrence: int) -> int:
    """Keep first touches diverse across every reset-clock ordering."""
    if occurrence == 0:
        recent_rank = sales_activity_recent_rank(customer_id)
        return ((10 * recent_rank * recent_rank + 15 * recent_rank + 15) % 47) % 4
    return (customer_id * 2 + occurrence * 3) % 5


def sales_activity_subject(
    customer_id: int,
    occurrence: int,
    activity_type_index: int,
) -> str:
    """Choose a title with an independent salt from date and activity type."""
    recent_rank = sales_activity_recent_rank(customer_id)
    if occurrence == 0:
        variant = ((recent_rank * recent_rank + 15) % 47) % 4
    else:
        variant = (
            recent_rank
            + 3 * (recent_rank // 2)
            + customer_id // 3
            + occurrence * 3
        ) % len(SALES_ACTIVITY_SUBJECT_CATALOG[activity_type_index])
    return SALES_ACTIVITY_SUBJECT_CATALOG[activity_type_index][variant]


def audit_target(index: int) -> tuple[str, str, int]:
    """Return a menu-compatible target from an existing seeded aggregate."""
    slot = (index - 1) % len(AUDIT_TARGET_CATALOG)
    menu_code, target_type, target_count = AUDIT_TARGET_CATALOG[slot]
    target_id = 1 + ((index * 17 + slot * 11) % target_count)
    return menu_code, target_type, target_id


def product_output(product_id: int) -> tuple[int, str]:
    category_id = ((product_id - 1) % 10) + 1
    series_index = (product_id - 1) // len(SUPPLIERS)
    if category_id == 4:
        return (80, 130, 180, 220)[series_index], "TON"
    return (3, 6, 12, 20)[series_index], "KW"


def expense_claim_title(index: int) -> str:
    purpose = (
        "고객사 설비 도입 협의", "현장 설치 일정 조율", "정기 점검 지원",
        "기술 교육 진행", "수리 부품 긴급 전달", "납품 전 사양 확인",
    )[(index - 1) % 6]
    return f"{CUSTOMER_NAMES[(index * 3 - 1) % len(CUSTOMER_NAMES)]} {purpose} 출장비"


def leave_reason(leave_type: str, index: int) -> str:
    reasons = {
        "ANNUAL": ("개인 일정", "가족 행사"),
        "HALF_DAY_AM": ("오전 병원 진료", "관공서 방문"),
        "HALF_DAY_PM": ("오후 개인 일정", "자녀 학교 일정"),
        "SICK": ("건강 회복", "진료 및 휴식"),
        "ETC": ("예비군 훈련", "경조사 참석"),
    }
    values = reasons[leave_type]
    return values[(index - 1) % len(values)]


def approval_step_comment(doc_type: str, step_status: str) -> str | None:
    """Return review copy that matches the evidence available for the document."""
    if step_status == "PENDING":
        return None
    comments = {
        "GENERAL": {
            "APPROVED": "요청 내용과 일정을 확인했습니다.",
            "REJECTED": "요청 내용과 일정을 보완해 주세요.",
        },
        "EXPENSE": {
            "APPROVED": "출장 목적과 첨부 증빙을 확인했습니다.",
            "REJECTED": "증빙 내용과 경비 항목을 보완해 주세요.",
        },
        "LEAVE": {
            "APPROVED": "일정과 인수인계 내용을 확인했습니다.",
            "REJECTED": "신청 사유와 일정을 보완해 주세요.",
        },
    }
    return comments[doc_type][step_status]


def approval_document_status(document_id: int) -> str:
    """Keep cancellation examples on general approvals with no domain shadow."""
    if document_id <= 8:
        return ("IN_PROGRESS", "APPROVED", "REJECTED", "CANCELED")[
            (document_id - 1) % 4
        ]
    return ("IN_PROGRESS", "APPROVED", "REJECTED")[(document_id - 9) % 3]


def make_pdf(index: int) -> bytes:
    titles = (
        "SALES MEETING STATUS",
        "EQUIPMENT INSPECTION PLAN",
        "SITE VISIT CHECKLIST",
        "INSTALLATION ROUTE PLAN",
        "BUSINESS TRIP PLAN",
        "TECHNICAL TRAINING PLAN",
    )
    title = titles[(index - 1) % len(titles)]
    stream = (
        "BT /F1 17 Tf 36 128 Td "
        f"(ONGYEOL SOLUTIONS - {title} 01) Tj "
        "0 -32 Td /F1 10 Tf (DEMO DOCUMENT - NO LEGAL EFFECT) Tj ET"
    ).encode("ascii")
    objects = [
        b"<< /Type /Catalog /Pages 2 0 R >>",
        b"<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
        b"<< /Type /Page /Parent 2 0 R /MediaBox [0 0 420 180] /Resources << /Font << /F1 5 0 R >> >> /Contents 4 0 R >>",
        b"<< /Length " + str(len(stream)).encode("ascii") + b" >>\nstream\n" + stream + b"\nendstream",
        b"<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
    ]
    out = io.BytesIO()
    out.write(b"%PDF-1.4\n%\xe2\xe3\xcf\xd3\n")
    offsets = [0]
    for number, obj in enumerate(objects, 1):
        offsets.append(out.tell())
        out.write(f"{number} 0 obj\n".encode("ascii") + obj + b"\nendobj\n")
    xref = out.tell()
    out.write(f"xref\n0 {len(objects)+1}\n0000000000 65535 f \n".encode("ascii"))
    for offset in offsets[1:]:
        out.write(f"{offset:010d} 00000 n \n".encode("ascii"))
    out.write(f"trailer << /Size {len(objects)+1} /Root 1 0 R >>\nstartxref\n{xref}\n%%EOF\n".encode("ascii"))
    return out.getvalue()


def make_png(index: int) -> bytes:
    """Create an inert receipt-like image with a hidden synthetic-data marker."""
    width, height = 320, 180
    accent = ((42 + index * 11) % 120, (95 + index * 7) % 140, (150 + index * 5) % 190)
    pixels = [[(245, 247, 250) for _ in range(width)] for _ in range(height)]

    def rectangle(left: int, top: int, right: int, bottom: int, color: tuple[int, int, int]) -> None:
        for y in range(top, bottom):
            pixels[y][left:right] = [color] * (right - left)

    rectangle(86, 10, 234, 170, (255, 255, 255))
    rectangle(86, 10, 234, 36, accent)
    for offset in range(0, 5):
        y = 55 + offset * 18
        rectangle(102, y, 174, y + 4, (152, 161, 174))
        rectangle(192, y, 220, y + 4, (89, 99, 115))
    rectangle(102, 148, 220, 151, (72, 80, 94))
    rectangle(178, 158, 220, 164, accent)
    raw = b"".join(b"\x00" + b"".join(bytes(pixel) for pixel in row) for row in pixels)

    def chunk(kind: bytes, data: bytes) -> bytes:
        body = kind + data
        return struct.pack(">I", len(data)) + body + struct.pack(">I", zlib.crc32(body) & 0xFFFFFFFF)

    return (
        b"\x89PNG\r\n\x1a\n"
        + chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 2, 0, 0, 0))
        + chunk(b"tEXt", b"Description\x00SYNTHETIC DEMO DATA - NO REAL RECEIPT")
        + chunk(b"IDAT", zlib.compress(raw, 9))
        + chunk(b"IEND", b"")
    )


def make_xlsx(index: int) -> bytes:
    customer_name = CUSTOMER_NAMES[(index - 7) % len(CUSTOMER_NAMES)]
    requested_power = (3, 6, 12, 20)[(index - 7) % 4]
    files = {
        "[Content_Types].xml": """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/><Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/></Types>""",
        "_rels/.rels": """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>""",
        "xl/workbook.xml": """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="고객요구사항" sheetId="1" r:id="rId1"/></sheets></workbook>""",
        "xl/_rels/workbook.xml.rels": """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/></Relationships>""",
        "xl/worksheets/sheet1.xml": f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?><worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData><row r="1"><c r="A1" t="inlineStr"><is><t>고객사</t></is></c><c r="B1" t="inlineStr"><is><t>요청 사양</t></is></c></row><row r="2"><c r="A2" t="inlineStr"><is><t>{customer_name}</t></is></c><c r="B2" t="inlineStr"><is><t>{requested_power}kW 평판 레이저 견적</t></is></c></row><row r="3"><c r="A3" t="inlineStr"><is><t>담당</t></is></c><c r="B3" t="inlineStr"><is><t>sales@demo.example</t></is></c></row><row r="4"><c r="A4" t="inlineStr"><is><t>안내</t></is></c><c r="B4" t="inlineStr"><is><t>데모 데이터 · 실제 효력 없음</t></is></c></row></sheetData></worksheet>""",
    }
    output = io.BytesIO()
    # Store the tiny XML members verbatim. Deflate output can vary with the zlib
    # implementation bundled by each Python distribution, breaking cross-host
    # byte-for-byte reproducibility even when every ZIP timestamp is fixed.
    with zipfile.ZipFile(output, "w", compression=zipfile.ZIP_STORED) as archive:
        for name in sorted(files):
            info = zipfile.ZipInfo(name, (1980, 1, 1, 0, 0, 0))
            info.create_system = 3
            info.compress_type = zipfile.ZIP_STORED
            info.external_attr = 0o100644 << 16
            archive.writestr(info, files[name].encode("utf-8"))
    return output.getvalue()


def file_spec(index: int) -> tuple[str, str, bytes]:
    fixture_id, original_name, *_ = FILE_CATALOG[index - 1]
    if fixture_id != index:
        raise ValueError(f"FILE_CATALOG id sequence mismatch: expected {index}, got {fixture_id}")
    if index <= 6 or 13 <= index <= 18:
        return original_name, "application/pdf", make_pdf(index)
    if index <= 10:
        return original_name, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", make_xlsx(index)
    if index <= 12:
        body = (
            "장비 정기 점검 안내\n"
            "- 점검 전 생산 일정을 공유해 주세요.\n"
            "- 장비 주변 안전 공간을 확보해 주세요.\n\n"
            "데모 데이터 · 실제 효력 없음\nservice@demo.example\n"
        ).encode("utf-8")
        return original_name, "text/plain", body
    return original_name, "image/png", make_png(index)


def create_files_archive(path: Path) -> list[dict[str, object]]:
    objects: list[dict[str, object]] = []
    payloads: list[tuple[str, bytes]] = []
    catalog_ids = tuple(entry[0] for entry in FILE_CATALOG)
    if catalog_ids != tuple(range(1, len(FILE_CATALOG) + 1)):
        raise ValueError("FILE_CATALOG ids must be contiguous and ordered from 1")
    for index, _, status, owner_type, owner_id, uploader_id in FILE_CATALOG:
        original, content_type, content = file_spec(index)
        stored = str(uuid.uuid5(NAMESPACE, f"stored-file-{index:02d}"))
        payloads.append((f"objects/{stored}", content))
        objects.append(
            {
                "id": index,
                "storedName": stored,
                "originalName": original,
                "contentType": content_type,
                "size": len(content),
                "sha256": hashlib.sha256(content).hexdigest(),
                "createdAtDaysAgo": 9 + index,
                "status": status,
                "ownerType": owner_type,
                "ownerId": owner_id,
                "uploaderId": uploader_id,
            }
        )
    with path.open("wb") as raw:
        # Level 0 emits stored DEFLATE blocks and avoids zlib-version-specific
        # compression choices across Windows and Linux Python distributions.
        with gzip.GzipFile(filename="", mode="wb", fileobj=raw, mtime=0, compresslevel=0) as gz:
            with tarfile.open(fileobj=gz, mode="w", format=tarfile.PAX_FORMAT) as archive:
                for name, content in sorted(payloads):
                    info = tarfile.TarInfo(name)
                    info.size = len(content)
                    info.mtime = 0
                    info.mode = 0o644
                    info.uid = info.gid = 0
                    info.uname = info.gname = ""
                    archive.addfile(info, io.BytesIO(content))
    return objects


def build_seed(
    file_objects: list[dict[str, object]],
    provenance_time: dt.datetime,
) -> tuple[bytes, dict[str, object]]:
    sql = SqlWriter()
    sql.line("-- Generated by demo/tools/generate_seed.py; do not edit by hand.")
    sql.line(f"-- seedVersion={SEED_VERSION}; scenarioVersion={SCENARIO_VERSION}; compatibleAppVersion={COMPATIBLE_APP_VERSION}")
    sql.line("SET NAMES utf8mb4;")
    sql.line("SET time_zone = '+09:00';")
    sql.line("SET @seed_now = UTC_TIMESTAMP(6) + INTERVAL 9 HOUR;")
    sql.line("SET @seed_today = DATE(@seed_now);")
    sql.line("SET @seed_month = STR_TO_DATE(DATE_FORMAT(@seed_today, '%Y-%m-01'), '%Y-%m-%d');")
    sql.line("SET @seed_year = YEAR(@seed_today);")
    sql.line("START TRANSACTION;")
    sql.line()

    sql.insert(
        "demo_seed_manifest",
        ["id", "seed_version", "schema_version", "scenario_version", "generated_at", "reset_at"],
        [(1, SEED_VERSION, SCHEMA_VERSION, SCENARIO_VERSION,
          provenance_time.strftime("%Y-%m-%d %H:%M:%S.%f"), Raw("@seed_now"))],
    )

    roles = [
        (1, rel_ts(500), rel_ts(500), "MASTER", "시스템 관리자", "복구 운영 관리자 전용", True),
        (2, rel_ts(500), rel_ts(500), "DEMO_MANAGER", "업무 관리자", "전체 업무 흐름 관리", False),
        (3, rel_ts(500), rel_ts(500), "DEMO_STAFF", "일반 사용자", "기안·경비·근태 사용", False),
    ]
    sql.insert("roles", ["id", "created_at", "updated_at", "code", "name", "description", "system"], roles)

    menus = ["EMPLOYEES", "DEPARTMENTS", "POSITIONS", "CUSTOMERS", "SUPPLIERS", "PRODUCTS", "SALES_CONTACTS", "SALES_CUSTOMERS", "CONTRACTS", "EQUIPMENTS", "AFTER_SERVICES", "ROLES", "CODE_RULES", "APPROVALS", "EXPENSES", "ATTENDANCE", "BOARDS", "DRIVE"]
    manager_no_write = {"EMPLOYEES", "ROLES", "CODE_RULES"}
    # 본인 근태·휴가 흐름은 read 권한으로 열리고, write 는 관리자 전체 조회·조정 API를 노출한다.
    staff_write = {"APPROVALS", "EXPENSES", "BOARDS"}
    staff_hidden = {"ROLES", "CODE_RULES"}
    staff_scoped = {"SALES_CUSTOMERS", "CONTRACTS"}
    role_menu_rows: list[tuple[object, ...]] = []
    row_id = 1
    for role_id, allowed in ((1, menus), (2, menus), (3, [m for m in menus if m not in staff_hidden])):
        for menu in allowed:
            can_write = role_id == 1 or (role_id == 2 and menu not in manager_no_write) or (role_id == 3 and menu in staff_write)
            scope = "SELF" if role_id == 3 and menu in staff_scoped else "ALL"
            role_menu_rows.append((row_id, rel_ts(499), rel_ts(499), True, can_write, scope, menu, role_id))
            row_id += 1
    sql.insert("role_menus", ["id", "created_at", "updated_at", "can_read", "can_write", "data_scope", "menu_code", "role_id"], role_menu_rows)

    departments = [
        (1, "D001", "온결솔루션", None), (2, "D002", "영업본부", 1),
        (3, "D003", "기술본부", 1), (4, "D004", "경영지원본부", 1),
        (5, "D005", "수도권영업팀", 2), (6, "D006", "중부영업팀", 2),
        (7, "D007", "기술지원팀", 3), (8, "D008", "솔루션개발팀", 3),
    ]
    sql.insert("departments", ["id", "created_at", "updated_at", "code", "name", "parent_id"], [(i, rel_ts(480), rel_ts(480), code, name, parent) for i, code, name, parent in departments])
    position_names = ["대표", "본부장", "팀장", "책임", "선임", "사원", "인턴"]
    sql.insert("positions", ["id", "created_at", "updated_at", "code", "name", "rank_level", "description"], [(i, rel_ts(480), rel_ts(480), f"P{i:03d}", name, i, "표준 직급 체계") for i, name in enumerate(position_names, 1)])

    employees: list[tuple[object, ...]] = []
    for i in range(1, 23):
        name, account_name = EMPLOYEE_IDENTITIES[i - 1]
        if i == 1:
            login, password, role_id = "demo.manager", MANAGER_HASH, 2
        elif i == 2:
            login, password, role_id = "demo.staff", STAFF_HASH, 3
        else:
            login, password, role_id = account_name, LOCKED_PASSWORD_HASH, 3 if i % 4 else 2
        status = "ACTIVE" if i <= 18 else ("LEAVE" if i <= 20 else "RESIGNED")
        department_id = EMPLOYEE_DEPARTMENT_IDS[i - 1]
        birth_days_ago, join_days_ago = employee_dates(i)
        road_address, detail_address, zip_code = employee_address(i)
        employees.append(
            (
                i, rel_ts(join_days_ago), rel_ts(30), detail_address,
                road_address, zip_code,
                Raw(f"DATE_SUB(@seed_today, INTERVAL {birth_days_ago} DAY)"),
                department_id, f"{login}@ongyeol.example",
                Raw(f"DATE_SUB(@seed_today, INTERVAL {join_days_ago} DAY)"),
                login, name, password, employee_phone(i),
                EMPLOYEE_POSITION_IDS[i - 1], role_id, status,
            )
        )
    sql.insert("employees", ["id", "created_at", "updated_at", "detail_address", "road_address", "zip_code", "birth_date", "department_id", "email", "join_date", "login_id", "name", "password", "phone", "position_id", "role_id", "status"], employees)

    code_rules = [
        (1, "DEPARTMENT", "D{SEQ:3}", "AUTO_OR_MANUAL", "부서 코드"),
        (2, "POSITION", "P{SEQ:3}", "AUTO_OR_MANUAL", "직책 코드"),
        (3, "CUSTOMER", "C{SEQ:4}", "AUTO_OR_MANUAL", "고객사 코드"),
        (4, "CONTRACT", "CT{YYYY}-{SEQ:3}", "AUTO", "계약 번호"),
        (5, "AFTER_SERVICE", "AS{YYYY}-{SEQ:4}", "AUTO", "AS 접수번호"),
    ]
    sql.insert("code_rules", ["id", "created_at", "updated_at", "target", "pattern", "input_mode", "description"], [(i, rel_ts(480), rel_ts(480), target, pattern, mode, desc) for i, target, pattern, mode, desc in code_rules])

    categories = ["평판 레이저", "형강 레이저", "파이프 레이저", "절곡기", "복합기", "디버링기", "엣지머신", "용접기", "발진기", "기타"]
    sql.insert("product_categories", ["id", "created_at", "updated_at", "name", "sort_order"], [(i, rel_ts(470), rel_ts(470), name, i) for i, name in enumerate(categories, 1)])
    sql.insert(
        "suppliers",
        ["id", "created_at", "updated_at", "active", "country", "name", "name_ko", "note"],
        [
            (i, rel_ts(460), rel_ts(20), True, country, name_en, name_ko, "설비 공급 및 기술지원 협력사")
            for i, (name_en, name_ko, country, _) in enumerate(SUPPLIERS, 1)
        ],
    )
    products = []
    for i in range(1, 33):
        supplier_id = ((i - 1) % len(SUPPLIERS)) + 1
        _, _, _, model_prefix = SUPPLIERS[supplier_id - 1]
        variant = ("CORE", "PRO", "X", "FLEX")[(i - 1) // len(SUPPLIERS)]
        products.append(
            (
                i, rel_ts(440 - i), rel_ts(15), True,
                f"{model_prefix}-{((i - 1) % 10) + 1:02d}-{variant}",
                "자동화 셀 및 원격 점검 연동 사양", supplier_id,
                ((i - 1) % 10) + 1,
            )
        )
    sql.insert("products", ["id", "created_at", "updated_at", "active", "model_name", "note", "supplier_id", "category_id"], products)

    customers = []
    for i in range(1, 49):
        road_address, detail_address, zip_code = customer_address(i)
        email, phone, fax, website = customer_channels(i)
        representative, _ = contact_identity(i)
        status = "ACTIVE" if i <= 42 else ("INACTIVE" if i <= 45 else "SUSPENDED")
        customers.append(
            (
                i, rel_ts((i * 7) % 45, i), Raw("@seed_now"), detail_address,
                road_address, zip_code,
                ("정밀가공", "판금가공", "산업기계 제작", "자동화 설비")[(i - 1) % 4],
                f"000-00-{i:05d}", "제조업", f"C{i:04d}",
                f"000000-00000{i:02d}", email,
                fax, CUSTOMER_NAMES[i - 1], None,
                ("신규 설비 증설 검토", "정기 유지보수 계약 협의", "스마트공장 전환 상담", "기존 장비 교체 검토")[(i - 1) % 4],
                phone, representative, status,
                rel_date(customer_trade_start_days_ago(i)),
                ("POTENTIAL", "GENERAL", "KEY_ACCOUNT", "PARTNER")[i % 4],
                website,
            )
        )
    sql.insert("customers", ["id", "created_at", "updated_at", "detail_address", "road_address", "zip_code", "biz_item", "biz_reg_no", "biz_type", "code", "corp_reg_no", "email", "fax", "name", "name_en", "note", "phone", "representative", "status", "trade_start_date", "type", "website"], customers)

    source_names = ["자동화 산업전", "기존 고객 소개", "홈페이지 기술 문의", "파트너 공동 세미나", "제품 뉴스레터", "현장 기술 상담", "공급사 추천", "기타 영업 문의"]
    source_types = ["EXHIBITION", "REFERRAL", "WEB", "EXHIBITION", "WEB", "OTHER", "REFERRAL", "OTHER"]
    sql.insert("acquisition_sources", ["id", "created_at", "updated_at", "description", "name", "type"], [(i, rel_ts(400), rel_ts(400), "고객 접점 등록 경로", source_names[i-1], source_types[i-1]) for i in range(1, 9)])
    contacts = []
    for i in range(1, 73):
        contact_name, contact_name_en = contact_identity(i)
        business_email, mobile_phone, office_phone, personal_email = contact_channels(
            i, contact_name_en
        )
        note, _, _ = contact_business_role(i)
        contacts.append(
            (
                i, rel_ts(360 - i, i), rel_ts(10, i),
                business_email,
                rel_date(contact_met_days_ago(i)), mobile_phone, contact_name,
                contact_name_en, note,
                office_phone, personal_email,
            )
        )
    sql.insert("sales_contacts", ["id", "created_at", "updated_at", "email", "met_at", "mobile_phone", "name", "name_en", "note", "office_phone", "personal_email"], contacts)
    employments = []
    for i in range(1, 73):
        _, department, position = contact_business_role(i)
        employments.append(
            (
                i, rel_ts(330 - i, i), rel_ts(5, i), i,
                ((i - 1) % len(CUSTOMER_NAMES)) + 1,
                department, None, None, None, None, position,
                rel_date(contact_employment_start_days_ago(i)),
            )
        )
    for i in range(1, 13):
        # 과거 외부 재직은 현재 고객사 재직 시작보다 먼저 끝난다.
        employments.append((72 + i, rel_ts(1700), rel_ts(1100 + i), i, None, "기술부", "설비 자동화 분야로 이직", ["JOB_CHANGE", "RETIREMENT", "OTHER"][i % 3], rel_date(1100 + i), EXTERNAL_COMPANIES[i - 1], "과장", rel_date(1600 + i)))
    sql.insert("sales_contact_employments", ["id", "created_at", "updated_at", "contact_id", "customer_id", "department", "departure_note", "departure_type", "end_date", "external_company_name", "position", "start_date"], employments)
    contact_sources = []
    for i in range(1, 73):
        contact_sources.append((len(contact_sources)+1, rel_ts(300), rel_ts(300), i, ((i-1)%8)+1))
    for i in range(1, 25):
        contact_sources.append((len(contact_sources)+1, rel_ts(300), rel_ts(300), i, (i%8)+1))
    sql.insert("sales_contact_sources", ["id", "created_at", "updated_at", "contact_id", "source_id"], contact_sources)

    assignments = []
    assignment_employee: dict[int, int] = {}
    for customer_id in range(1, 49):
        employee_id = SALES_EMPLOYEE_IDS[(customer_id - 1) % len(SALES_EMPLOYEE_IDS)]
        assignment_employee[customer_id] = employee_id
        reason = ("신규 영업 전담", "지역 담당 배정", "기존 거래처 인계", "기술 영업 협업")[
            (customer_id - 1) % 4
        ]
        assignments.append(
            (
                customer_id, rel_ts(250), rel_ts(2), customer_id, employee_id,
                None, True, reason, rel_date(500 - customer_id),
            )
        )
    for i in range(1, 13):
        # 과거 담당 이력은 현재 주 담당 배정 시작 전에 종료한다.
        historical_employee_id = SALES_EMPLOYEE_IDS[
            (i + 3) % len(SALES_EMPLOYEE_IDS)
        ]
        assignments.append(
            (
                48 + i, rel_ts(1100), rel_ts(600 + i), i,
                historical_employee_id,
                rel_date(600 + i), False, "조직 개편에 따른 담당 변경",
                rel_date(1000 + i),
            )
        )
    sql.insert("sales_assignments", ["id", "created_at", "updated_at", "customer_id", "employee_id", "end_date", "is_primary", "reason", "start_date"], assignments)
    activities = []
    activity_types = ["VISIT", "CALL", "MEETING", "EMAIL", "OTHER"]
    activity_contents = [
        "주력 생산 품목과 월간 가동시간을 확인하고 장비 배치 도면을 요청받음.",
        "요청 출력과 가공 소재 두께를 확인하고 적용 모델 비교안을 안내함.",
        "발주·입고·설치 희망일을 조율하고 현장 전원 및 집진 설비 조건을 확인함.",
        "협의한 옵션을 반영한 견적서와 기본 사양서를 담당자에게 전달함.",
        "투자 심의 일정을 확인하고 정부지원사업 적용 여부를 후속 검토하기로 함.",
    ]
    activity_id = 1
    for customer_id, activity_count in enumerate(SALES_ACTIVITY_COUNT_PLAN, 1):
        recent_rank = sales_activity_recent_rank(customer_id)
        for occurrence in range(activity_count):
            if occurrence == 0:
                days_expression = (
                    f"MOD({recent_rank}, GREATEST(DAY(@seed_today), 1))"
                )
                candidate_activity_at = (
                    f"TIMESTAMP(DATE_SUB(@seed_today, INTERVAL ({days_expression}) DAY), "
                    f"'{recent_activity_clock(recent_rank)}')"
                )
                fallback_activity_at = (
                    "DATE_ADD(DATE_SUB(CAST(DATE_FORMAT(@seed_now, "
                    "'%Y-%m-%d %H:%i:%s') AS DATETIME(6)), "
                    f"INTERVAL {recent_rank + 1} SECOND), "
                    "INTERVAL 500000 MICROSECOND)"
                )
                activity_at = Raw(
                    f"CASE WHEN {candidate_activity_at}<=@seed_now "
                    f"THEN {candidate_activity_at} ELSE "
                    f"{fallback_activity_at} END"
                )
            else:
                days_ago = 31 + ((customer_id * 37 + occurrence * 29) % 145)
                timestamp_salt = customer_id * 11 + occurrence * 37
                activity_at = rel_ts(days_ago, timestamp_salt)
            created_at = Raw(
                f"LEAST(DATE_ADD({activity_at}, "
                f"INTERVAL {5 + ((customer_id * 7 + occurrence * 11) % 31)} MINUTE), "
                "@seed_now)"
            )
            activity_index = sales_activity_type_index(customer_id, occurrence)
            activity_type = activity_types[activity_index]
            activity_subject = sales_activity_subject(
                customer_id,
                occurrence,
                activity_index,
            )
            contact_id = sales_activity_contact_id(customer_id, activity_type)
            activities.append(
                (
                    activity_id, created_at, Raw("@seed_now"), activity_at,
                    activity_contents[activity_index], contact_id, customer_id,
                    assignment_employee[customer_id], activity_subject,
                    activity_type,
                )
            )
            activity_id += 1
    sql.insert("sales_activities", ["id", "created_at", "updated_at", "activity_date", "content", "customer_contact_id", "customer_id", "our_employee_id", "subject", "type"], activities)

    pipeline_statuses = ["CONTRACTED"]*6 + ["ORDERED"]*6 + ["ARRIVED"]*5 + ["INSTALLING"]*5 + ["CANCELED"]*3
    contract_statuses = pipeline_statuses + ["INSTALLED"]*8 + ["SETTLED"]*9
    contracts = []
    contract_meta: dict[int, dict[str, object]] = {}
    logistics_notes = (
        "현장 반입 전 출입구 폭과 지게차 동선을 재확인한다.",
        "본체와 집진기를 분리 출고하고 현장 도착 후 일괄 반입한다.",
        "오전 반입 후 수평 조정과 전원 연결을 같은 날 진행한다.",
        "운송 보험 증권을 출고 전 고객사 구매팀에 전달한다.",
    )
    option_texts = (
        "자동 노즐 교환기, 집진기 연동, 원격 점검 모듈",
        "로딩 테이블, 소재 감지 센서, 작업 모니터링 패키지",
        "회전축 모듈, 보호창 예비품, 작업자 교육 2회",
        "자동 초점 헤드, 질소 절단 패키지, 예방 정비 1회",
    )
    warranty_offsets = [7, 21, 45, 70, 89, 150, 210, 270, 300, 330, -30, -60, -90, -120, -150]
    for i, status in enumerate(contract_statuses, 1):
        customer_id = ((i - 1) % 48) + 1
        employee_id = assignment_employee[customer_id]
        product_id = ((i - 1) % 32) + 1
        supplier_id = ((product_id - 1) % 8) + 1
        if i <= 25:
            raw_contract_date = month_date((i - 1) % 6, 2 + ((i * 3) % 18))
            # 입고 완료 이상의 상태는 실제 마일스톤이 오늘 이전이 되도록 충분한 경과 기간을 확보한다.
            contract_date = (
                Raw(f"LEAST({raw_contract_date}, DATE_SUB(@seed_today, INTERVAL 55 DAY))")
                if status in {"ARRIVED", "INSTALLING"}
                else raw_contract_date
            )
            order_date = (
                Raw(f"LEAST(DATE_ADD({contract_date}, INTERVAL 7 DAY), @seed_today)")
                if status in {"ORDERED", "ARRIVED", "INSTALLING"}
                else None
            )
            expected = date_add(contract_date, 45) if order_date else None
            arrival = date_add(contract_date, 50) if status in {"ARRIVED", "INSTALLING"} else None
            installed = settled = None
        else:
            equipment_index = i - 26
            if equipment_index < 15:
                desired_end = Raw(f"DATE_ADD(@seed_today, INTERVAL {warranty_offsets[equipment_index]} DAY)")
                warranty_start = Raw(f"DATE_SUB({desired_end}, INTERVAL 12 MONTH)")
            else:
                warranty_start = rel_date(150 + equipment_index * 5)
            installed = Raw(f"DATE_SUB({warranty_start}, INTERVAL 7 DAY)")
            contract_date = Raw(f"DATE_SUB({installed}, INTERVAL 70 DAY)")
            order_date = date_add(contract_date, 10)
            expected = date_add(contract_date, 55)
            arrival = date_add(contract_date, 60)
            settled = date_add(installed, 30) if status == "SETTLED" else None
        final_amount = 85_000_000 + i * 4_750_000
        created_at = Raw(f"LEAST(TIMESTAMP({contract_date}, '09:00:00'), @seed_now)")
        output_value, output_unit = product_output(product_id)
        contracts.append(
            (
                i, created_at, Raw("@seed_now"), arrival, contract_date,
                f"TMP-{i:03d}", ("A", "B", "C", "D")[i % 4], customer_id,
                date_add(contract_date, 120), employee_id, expected, final_amount,
                final_amount // 5, installed,
                logistics_notes[(i - 1) % len(logistics_notes)],
                option_texts[(i - 1) % len(option_texts)], order_date,
                output_unit, output_value, product_id, settled,
                status, supplier_id, "제조혁신 설비고도화 사업",
                ("NONE", "APPLIED", "SELECTED", "REJECTED")[i % 4],
            )
        )
        contract_meta[i] = {
            "status": status,
            "date": contract_date,
            "installed": installed,
            "customer": customer_id,
            "employee": employee_id,
            "product": product_id,
            "supplier": supplier_id,
            "amount": final_amount,
            "output_unit": output_unit,
            "output_value": output_value,
        }
    sql.insert("contracts", ["id", "created_at", "updated_at", "arrival_date", "contract_date", "contract_no", "cretop_grade", "customer_id", "due_date", "employee_id", "expected_arrival_date", "final_amount", "initial_amount", "installed_date", "logistics_note", "option_text", "order_date", "output_unit", "output_value", "product_id", "settled_date", "status", "supplier_id", "support_program_name", "support_program_status"], contracts)
    sql.line("UPDATE contracts c JOIN (SELECT id, CONCAT('CT', YEAR(contract_date), '-', LPAD(ROW_NUMBER() OVER (PARTITION BY YEAR(contract_date) ORDER BY contract_date, id), 3, '0')) AS generated_no FROM contracts) x ON x.id=c.id SET c.contract_no=x.generated_no;")
    sql.line()
    payments = []
    for contract_id in range(1, 43):
        meta = contract_meta[contract_id]
        amount = int(meta["amount"])
        status = str(meta["status"])
        for part in (1, 2):
            planned = amount * (40 if part == 1 else 60) // 100
            paid = planned if (part == 1 and status in {"ARRIVED", "INSTALLING", "INSTALLED", "SETTLED"}) or (part == 2 and status == "SETTLED") else None
            base_date = meta["date"]
            planned_date = date_add(base_date, 30 if part == 1 else 90)
            paid_date = date_add(planned_date, 2) if paid is not None else None
            created_at = Raw(f"LEAST(TIMESTAMP({base_date}, '10:00:00'), @seed_now)")
            payments.append(
                (
                    (contract_id - 1) * 2 + part, created_at, Raw("@seed_now"),
                    contract_id, paid, paid_date, "계약금" if part == 1 else "잔금",
                    paid, paid_date, "세금계산서 발행 후 약정일 기준 입금",
                    planned, planned_date,
                )
            )
    sql.insert("contract_payments", ["id", "created_at", "updated_at", "contract_id", "invoice_amount", "invoice_date", "label", "paid_amount", "paid_date", "note", "planned_amount", "planned_date"], payments)
    notes = []
    contract_note_contents = (
        "고객사 전원 용량 확인서를 수령함.",
        "요청 옵션을 반영한 변경 견적을 전달함.",
        "출고 전 검수 일정에 생산기술 담당자가 참석하기로 함.",
        "설치 교육 참석자 명단을 설치 3일 전까지 받기로 함.",
        "운송 차량 진입 가능 시간은 오전 9시 이후로 확인됨.",
        "잔금 세금계산서는 설치 확인서 서명 후 발행하기로 함.",
    )
    for i in range(1, 57):
        contract_id = ((i-1)%42)+1
        contract_date = contract_meta[contract_id]["date"]
        created_at = Raw(f"LEAST(TIMESTAMP(LEAST(DATE_ADD({contract_date}, INTERVAL {3 + (i % 20)} DAY), @seed_today), '13:00:00'), @seed_now)")
        notes.append(
            (
                i, created_at, Raw("@seed_now"),
                contract_meta[contract_id]["employee"],
                contract_note_contents[(i - 1) % len(contract_note_contents)],
                contract_id,
            )
        )
    sql.insert("contract_notes", ["id", "created_at", "updated_at", "author_employee_id", "content", "contract_id"], notes)

    equipments = []
    equipment_meta: dict[int, dict[str, int]] = {}
    for equipment_id, contract_id in enumerate(range(26, 43), 1):
        meta = contract_meta[contract_id]
        idx = equipment_id - 1
        if idx < 15:
            desired_end = Raw(f"DATE_ADD(@seed_today, INTERVAL {warranty_offsets[idx]} DAY)")
            warranty_start = Raw(f"DATE_SUB({desired_end}, INTERVAL 12 MONTH)")
            general_months = 12
            general_end = Raw(f"DATE_ADD({warranty_start}, INTERVAL 12 MONTH)")
            oscillator_months = 24
            oscillator_end = Raw(f"DATE_ADD({warranty_start}, INTERVAL 24 MONTH)")
        else:
            warranty_start = general_months = general_end = oscillator_months = oscillator_end = None
        customer_road, _, _ = customer_address(int(meta["customer"]))
        equipments.append(
            (
                equipment_id, rel_ts(160 - idx), rel_ts(1),
                date_add(meta["installed"], 2), contract_id, meta["customer"],
                general_end, general_months, f"{customer_road} 제2공장",
                meta["installed"], "정기 점검 주기 2,000시간, 소모품 교체 이력 관리",
                oscillator_end, oscillator_months,
                meta["output_unit"], meta["output_value"],
                meta["product"], f"OGS-25-{equipment_id:04d}", meta["supplier"],
                equipment_id % 2 == 0, warranty_start,
            )
        )
        equipment_meta[equipment_id] = {"customer": int(meta["customer"]), "contract": contract_id}
    sql.insert("equipments", ["id", "created_at", "updated_at", "confirmed_date", "contract_id", "customer_id", "general_warranty_end_date", "general_warranty_months", "install_address", "installed_date", "note", "oscillator_warranty_end_date", "oscillator_warranty_months", "output_unit", "output_value", "product_id", "serial_no", "supplier_id", "warranty_insurance", "warranty_start_date"], equipments)

    engineer_rows = []
    external_engineers = (
        ("정우진", "테크온서비스"),
        ("김세아", "테크온서비스"),
        ("마틴 베버", "오로라레이저웍스"),
        ("루카 로시", "루미나팹테크"),
    )
    for i in range(1, 10):
        kind = "INTERNAL" if i <= 5 else ("OUTSOURCED" if i <= 7 else "MANUFACTURER")
        if kind == "INTERNAL":
            employee_id = 6 + i
            engineer_name = EMPLOYEE_IDENTITIES[employee_id - 1][0]
            affiliation = "온결솔루션 기술지원팀"
        else:
            employee_id = None
            engineer_name, affiliation = external_engineers[i - 6]
        engineer_rows.append(
            (
                i, rel_ts(300), rel_ts(4), True, affiliation,
                employee_id,
                engineer_name, f"010-0002-{i:04d}", kind,
            )
        )
    sql.insert("engineers", ["id", "created_at", "updated_at", "active", "affiliation", "employee_id", "name", "phone", "type"], engineer_rows)
    service_statuses = ["RECEIVED"]*8 + ["ASSIGNED"]*9 + ["IN_PROGRESS"]*10 + ["COMPLETED"]*18
    service_symptoms = {
        "REPAIR": "절단 시작 후 X축 원점 복귀 알람이 반복 발생함.",
        "INSTALL_SUPPORT": "설치 후 집진기와 장비 간 운전 신호 확인이 필요함.",
        "TRAINING": "신규 작업자 대상 네스팅 및 일상 점검 교육을 요청함.",
        "INTERPRET": "해외 제조사 원격 진단 회의의 기술 통역 지원을 요청함.",
        "TUNING": "박판 절단면에 버가 발생해 출력·속도 조건 조정이 필요함.",
    }
    service_resolutions = {
        "REPAIR": "원점 센서 커넥터를 재체결하고 반복 원점 복귀 시험을 완료함.",
        "INSTALL_SUPPORT": "집진기 인터록 배선을 수정하고 자동 운전 연동을 확인함.",
        "TRAINING": "표준 작업 예제로 네스팅·노즐 점검 실습을 진행함.",
        "INTERPRET": "진단 로그와 제조사 조치 항목을 정리해 고객 담당자에게 전달함.",
        "TUNING": "소재별 초점 위치와 절단 속도를 조정하고 시험편 품질을 확인함.",
    }
    after_services = []
    as_meta: dict[int, dict[str, object]] = {}
    for i, status in enumerate(service_statuses, 1):
        equipment_id = ((i-1)%17)+1
        completion_offset = 4 + (i % 10)
        raw_received = month_date((i-1)%6, 1 + ((i*5)%20))
        required_age = max(5, completion_offset if status == "COMPLETED" else 5)
        received = Raw(f"LEAST({raw_received}, DATE_SUB(@seed_today, INTERVAL {required_age} DAY))")
        decision = "UNDECIDED" if i <= 17 else ("FREE" if i <= 31 else "PAID")
        billing = 450_000 + i*25_000 if decision == "PAID" else None
        completed = date_add(received, completion_offset) if status == "COMPLETED" else None
        engineer = None if status == "RECEIVED" else ((i-1)%9)+1
        created_at = Raw(f"LEAST(TIMESTAMP({received}, '08:30:00'), @seed_now)")
        service_type = SERVICE_TYPE_PLAN[i - 1]
        after_services.append(
            (
                i, created_at, Raw("@seed_now"), engineer, billing, completed,
                equipment_meta[equipment_id]["customer"], equipment_id,
                f"AS-TMP-{i:04d}", received, status,
                service_symptoms[service_type], service_type, decision,
            )
        )
        as_meta[i] = {
            "received": received,
            "status": status,
            "completion_offset": completion_offset,
            "type": service_type,
        }
    sql.insert("after_services", ["id", "created_at", "updated_at", "assigned_engineer_id", "billing_amount", "completed_date", "customer_id", "equipment_id", "receipt_no", "received_date", "status", "symptom", "type", "warranty_decision"], after_services)
    sql.line("UPDATE after_services a JOIN (SELECT id, CONCAT('AS', YEAR(received_date), '-', LPAD(ROW_NUMBER() OVER (PARTITION BY YEAR(received_date) ORDER BY received_date, id), 4, '0')) AS generated_no FROM after_services) x ON x.id=a.id SET a.receipt_no=x.generated_no;")
    sql.line()
    visits = []
    service_record_ids = [
        service_id
        for service_id, status in enumerate(service_statuses, 1)
        if status in {"IN_PROGRESS", "COMPLETED"}
    ]
    for i in range(1, 61):
        service_id = service_record_ids[(i - 1) % len(service_record_ids)]
        visit_offset = 1 + (i % 5)
        if as_meta[service_id]["status"] == "COMPLETED":
            visit_offset = min(visit_offset, int(as_meta[service_id]["completion_offset"]))
        visit_date = date_add(as_meta[service_id]["received"], visit_offset)
        created_at = Raw(f"LEAST(TIMESTAMP({visit_date}, '18:00:00'), @seed_now)")
        service_type = str(as_meta[service_id]["type"])
        visits.append(
            (
                i, created_at, Raw("@seed_now"), service_id,
                ((i - 1) % 9) + 1, service_symptoms[service_type],
                service_resolutions[service_type], visit_date,
            )
        )
    sql.insert("service_visits", ["id", "created_at", "updated_at", "after_service_id", "engineer_id", "problem", "resolution", "visit_date"], visits)
    service_expenses = []
    expense_categories = ["DAILY_WAGE", "LODGING", "MEAL", "PARTS", "ETC"]
    service_expense_notes = {
        "DAILY_WAGE": "현장 기술지원 작업비",
        "LODGING": "지방 현장 지원 숙박비",
        "MEAL": "현장 지원 식비",
        "PARTS": "센서 및 소모 부품 구입비",
        "ETC": "현장 주차 및 통행료",
    }
    service_expense_base = {
        "REPAIR": 110_000,
        "INSTALL_SUPPORT": 85_000,
        "TRAINING": 55_000,
        "INTERPRET": 65_000,
        "TUNING": 75_000,
    }
    for i in range(1, 91):
        service_id = service_record_ids[(i - 1) % len(service_record_ids)]
        paid_offset = 1 + (i % 4)
        if as_meta[service_id]["status"] == "COMPLETED":
            paid_offset = min(paid_offset, int(as_meta[service_id]["completion_offset"]))
        paid_date = date_add(as_meta[service_id]["received"], paid_offset)
        created_at = Raw(f"LEAST(TIMESTAMP({paid_date}, '18:30:00'), @seed_now)")
        category = expense_categories[(i-1)%len(expense_categories)]
        service_type = str(as_meta[service_id]["type"])
        service_expenses.append(
            (
                i, created_at, Raw("@seed_now"), service_id,
                service_expense_base[service_type] + (i % 6) * 12_000, category,
                ((i - 1) % 9) + 1 if i % 7 else None,
                service_expense_notes[category], paid_date,
                "COMPANY" if i % 3 else "ENGINEER",
            )
        )
    sql.insert("service_expenses", ["id", "created_at", "updated_at", "after_service_id", "amount", "category", "engineer_id", "note", "paid_date", "payer_type"], service_expenses)

    stored_rows = []
    for obj in file_objects:
        fixture_id = int(obj["id"])
        created_days_ago = int(obj["createdAtDaysAgo"])
        fixture_timestamp = rel_ts(created_days_ago, fixture_id)
        stored_rows.append((
            fixture_id, fixture_timestamp, fixture_timestamp, obj["contentType"],
            obj["originalName"], obj["ownerId"], obj["ownerType"], obj["size"],
            obj["status"], obj["storedName"], obj["uploaderId"],
        ))
    sql.insert(
        "stored_files",
        ["id", "created_at", "updated_at", "content_type", "original_name",
         "owner_id", "owner_type", "size", "status", "stored_name", "uploader_id"],
        stored_rows,
    )

    document_status = {
        document_id: approval_document_status(document_id)
        for document_id in range(1, 37)
    }
    expense_claim_created = {
        claim_id: rel_ts(12 + (claim_id % 4), claim_id)
        for claim_id in range(1, 13)
    }
    leave_types = ["ANNUAL", "HALF_DAY_AM", "HALF_DAY_PM", "SICK", "ETC"]
    leave_plan: dict[int, dict[str, object]] = {}
    for request_id in range(1, 17):
        doc_id = 20 + request_id
        domain_status = document_status[doc_id]
        if domain_status == "IN_PROGRESS":
            candidate = f"DATE_ADD(@seed_today, INTERVAL {7 + request_id} DAY)"
            # 결재 중 휴가는 미래 평일로 둔다. 연말이면 다음 연도로 넘어갈 수 있다.
            start = Raw(
                f"CASE DAYOFWEEK({candidate}) "
                f"WHEN 1 THEN DATE_ADD({candidate}, INTERVAL 1 DAY) "
                f"WHEN 7 THEN DATE_ADD({candidate}, INTERVAL 2 DAY) ELSE {candidate} END"
            )
            created_at = rel_ts(1 + (request_id % 3), request_id)
        else:
            candidate = (
                f"GREATEST(MAKEDATE(@seed_year, 1), "
                f"DATE_SUB(@seed_today, INTERVAL {35 + request_id} DAY))"
            )
            # 완료된 휴가는 가장 가까운 이전 평일로 맞춘다.
            start = Raw(
                f"CASE DAYOFWEEK({candidate}) "
                f"WHEN 1 THEN DATE_SUB({candidate}, INTERVAL 2 DAY) "
                f"WHEN 7 THEN DATE_SUB({candidate}, INTERVAL 1 DAY) ELSE {candidate} END"
            )
            created_at = business_ts(
                f"DATE_SUB({start}, INTERVAL 10 DAY)",
                request_id,
            )
        leave_plan[request_id] = {
            "status": domain_status,
            "start": start,
            "created_at": created_at,
        }
    approval_docs = []
    approval_steps = []
    step_id = 1
    general_titles = (
        "미르온정밀 설비 상담 출장 계획",
        "솔누리금속 설치 일정 협의 출장 계획",
        "3분기 기술지원 교육 운영안",
        "서비스 예비 부품 재고 보충 요청",
        "고객사 정기 점검 캠페인 시행안",
        "신규 장비 시연회 운영 예산 요청",
        "영업 차량 정기 점검 품의",
        "기술 문서 표준 양식 개정안",
    )
    general_contents = (
        "생산 품목과 설비 배치 조건을 확인하기 위한 방문 일정과 예상 경비를 검토해 주세요.",
        "설치 동선과 전원·집진 조건을 최종 협의하기 위한 출장 계획입니다.",
        "신규 기술지원 인력의 장비별 기본 교육과 안전 교육을 함께 진행합니다.",
        "빈번하게 사용하는 센서와 보호창의 안전 재고를 보충하고자 합니다.",
        "보증 만료 예정 장비를 대상으로 사전 점검 일정을 운영하고자 합니다.",
        "고객 초청 장비 시연회의 장소, 장비 운송, 운영 인력 비용을 요청합니다.",
        "장거리 운행 전 차량 안전 점검과 소모품 교체를 진행하고자 합니다.",
        "현장 점검표와 설치 확인서의 용어 및 승인 항목을 통일하고자 합니다.",
    )
    for doc_id in range(1, 37):
        doc_type = "GENERAL" if doc_id <= 8 else ("EXPENSE" if doc_id <= 20 else "LEAVE")
        ref_id = None if doc_type == "GENERAL" else (doc_id-8 if doc_type == "EXPENSE" else doc_id-20)
        if doc_type == "GENERAL":
            drafter = 1 if doc_id == 2 else 2
        elif doc_type == "EXPENSE":
            claim_id = int(ref_id)
            drafter = 2 if claim_id <= 6 else 3 + claim_id % 10
        else:
            drafter = int(ref_id) + 1
        status = document_status[doc_id]
        step_count = 2 if doc_id <= 16 else 1
        current = step_count if status == "APPROVED" else 1
        if doc_type == "GENERAL":
            created_at = rel_ts(20 + doc_id, doc_id)
        elif doc_type == "EXPENSE":
            created_at = expense_claim_created[doc_id - 8]
        else:
            created_at = leave_plan[doc_id - 20]["created_at"]
        if doc_type == "GENERAL":
            title = general_titles[doc_id - 1]
            content = general_contents[doc_id - 1]
        elif doc_type == "EXPENSE":
            title = expense_claim_title(int(ref_id))
            content = "출장 목적과 첨부 증빙을 확인해 주세요."
        else:
            request_id = int(ref_id)
            leave_type = leave_types[(request_id - 1) % len(leave_types)]
            title = f"{EMPLOYEE_IDENTITIES[request_id][0]} {leave_reason(leave_type, request_id)} 휴가 신청"
            content = "업무 인수인계 내용을 공유했으며 일정 확인을 요청합니다."
        approval_docs.append(
            (
                doc_id, created_at, Raw("@seed_now"), content, current,
                doc_type, drafter, ref_id, status, title, 0,
            )
        )
        for order in range(1, step_count+1):
            if status == "APPROVED":
                step_status, decided = "APPROVED", Raw(f"DATE_ADD({created_at}, INTERVAL {order} HOUR)")
            elif status == "REJECTED" and order == 1:
                step_status, decided = "REJECTED", Raw(f"DATE_ADD({created_at}, INTERVAL 1 HOUR)")
            else:
                step_status, decided = "PENDING", None
            approver = (1 if order==1 else 3) if drafter != 1 else (2 if order==1 else 3)
            comment = approval_step_comment(doc_type, step_status)
            approval_steps.append(
                (
                    step_id, created_at, Raw("@seed_now"), approver, comment,
                    decided, step_status, order, doc_id,
                )
            )
            step_id += 1
    sql.insert("approval_documents", ["id", "created_at", "updated_at", "content", "current_step_order", "doc_type", "drafter_id", "ref_id", "status", "title", "version"], approval_docs)
    sql.insert("approval_steps", ["id", "created_at", "updated_at", "approver_id", "comment", "decided_at", "status", "step_order", "document_id"], approval_steps)

    expense_claims = []
    expense_items = []
    item_id = 1
    approval_attachments = [(1, 17, 0), (2, 18, 0)]
    for claim_id in range(1, 13):
        doc_id = 8 + claim_id
        receipt_id = 18 + claim_id
        item_count = 3 if claim_id <= 6 else 2
        amounts = [35_000 + claim_id*1_000 + j*15_000 for j in range(item_count)]
        domain_status = document_status[doc_id]
        claim_created = expense_claim_created[claim_id]
        expense_claims.append(
            (
                claim_id, claim_created, Raw("@seed_now"), doc_id,
                2 if claim_id <= 6 else 3 + claim_id % 10, domain_status,
                expense_claim_title(claim_id), sum(amounts),
            )
        )
        approval_attachments.append((doc_id, receipt_id, 0))
        for j, amount in enumerate(amounts):
            category = ["TRANSPORT", "MEAL", "LODGING", "SUPPLIES", "ETC"][j%5]
            description = {
                "TRANSPORT": "고객사 이동 교통비",
                "MEAL": "현장 일정 중 식비",
                "LODGING": "지방 출장 숙박비",
                "SUPPLIES": "현장 점검 소모품",
                "ETC": "주차 및 통행료",
            }[category]
            expense_items.append(
                (
                    item_id, claim_created, Raw("@seed_now"), amount, category,
                    description, rel_date(35 + claim_id + j), receipt_id, claim_id,
                )
            )
            item_id += 1
    sql.insert("expense_claims", ["id", "created_at", "updated_at", "approval_document_id", "claimant_id", "status", "title", "total_amount"], expense_claims)
    sql.insert("expense_items", ["id", "created_at", "updated_at", "amount", "category", "description", "expense_date", "receipt_file_id", "claim_id"], expense_items)

    leave_requests = []
    for request_id in range(1, 17):
        doc_id = 20 + request_id
        employee_id = request_id + 1
        leave_type = leave_types[(request_id-1)%5]
        days = 0.5 if leave_type.startswith("HALF_DAY") else 1.0
        plan = leave_plan[request_id]
        start = plan["start"]
        domain_status = str(plan["status"])
        leave_requests.append(
            (
                request_id, plan["created_at"], Raw("@seed_now"), doc_id, days,
                employee_id, start, leave_type, leave_reason(leave_type, request_id),
                start, domain_status,
            )
        )
    sql.insert("leave_requests", ["id", "created_at", "updated_at", "approval_document_id", "days", "employee_id", "end_date", "leave_type", "reason", "start_date", "status"], leave_requests)
    sql.insert("leave_balances", ["id", "created_at", "updated_at", "employee_id", "granted_days", "used_days", "year"], [(i, rel_ts(100), rel_ts(1), i, 15.0, 0.0, Raw("@seed_year")) for i in range(1, 21)])
    sql.line(
        "UPDATE leave_balances b LEFT JOIN ("
        "SELECT employee_id, SUM(days) used_days FROM leave_requests "
        "WHERE status='APPROVED' AND leave_type IN ('ANNUAL','HALF_DAY_AM','HALF_DAY_PM') "
        "AND YEAR(start_date)=@seed_year GROUP BY employee_id"
        ") x ON x.employee_id=b.employee_id SET b.used_days=COALESCE(x.used_days,0);"
    )
    sql.insert("approval_document_attachments", ["document_id", "file_id", "attachment_order"], approval_attachments)

    attendance_rows = []
    for employee_id in range(1, 19):
        for offset in range(1, 33):
            (
                check_in_time, check_out_time,
                check_in_latitude, check_in_longitude,
                check_out_latitude, check_out_longitude,
            ) = attendance_profile(employee_id, offset)
            check_in_at = Raw(
                f"TIMESTAMP(DATE_SUB(@seed_today, INTERVAL {offset} DAY), '{check_in_time}')"
            )
            check_out_at = Raw(
                f"TIMESTAMP(DATE_SUB(@seed_today, INTERVAL {offset} DAY), '{check_out_time}')"
            )
            attendance_rows.append(
                (
                    employee_id * 1000 + offset, check_in_at, check_out_at,
                    check_in_at, check_in_latitude, check_in_longitude, True,
                    check_out_at, check_out_latitude, check_out_longitude, True,
                    employee_id, rel_date(offset),
                )
            )
    sql.insert("attendances", ["id", "created_at", "updated_at", "check_in_at", "check_in_latitude", "check_in_longitude", "check_in_within_range", "check_out_at", "check_out_latitude", "check_out_longitude", "check_out_within_range", "employee_id", "work_date"], attendance_rows)
    sql.line("DELETE a FROM attendances a WHERE DAYOFWEEK(a.work_date) IN (1, 7) OR EXISTS (SELECT 1 FROM leave_requests l WHERE l.employee_id=a.employee_id AND l.status='APPROVED' AND a.work_date BETWEEN l.start_date AND l.end_date);")
    today_check_in = Raw(
        "LEAST(@seed_now, TIMESTAMP(@seed_today, '08:47:00'))"
    )
    sql.insert("attendances", ["id", "created_at", "updated_at", "check_in_at", "check_in_latitude", "check_in_longitude", "check_in_within_range", "check_out_at", "check_out_latitude", "check_out_longitude", "check_out_within_range", "employee_id", "work_date"], [(99001, today_check_in, today_check_in, today_check_in, 37.5663, 126.9779, True, None, None, None, False, 1, Raw("@seed_today"))])

    posts = []
    post_created: dict[int, object] = {}
    for i, (category, title, content) in enumerate(POST_CATALOG, 1):
        created_at = rel_ts(35 - i, i)
        post_created[i] = created_at
        posts.append(
            (
                i, created_at, Raw("@seed_now"),
                1 if category == "NOTICE" else 2 + (i % 10),
                category, content, title,
            )
        )
    sql.insert("posts", ["id", "created_at", "updated_at", "author_id", "category", "content", "title"], posts)
    comments = []
    for i in range(1, 65):
        post_id = ((i - 1) % 28) + 1
        created_at = Raw(f"LEAST(DATE_ADD({post_created[post_id]}, INTERVAL {1 + (i % 5)} DAY), @seed_now)")
        comment = (
            "확인했습니다. 담당 일정에 반영하겠습니다.",
            "자료 공유 감사합니다.",
            "관련 고객사 일정도 함께 확인해 보겠습니다.",
            "현장 적용 후 결과를 추가로 공유하겠습니다.",
        )[(i - 1) % 4]
        comments.append((i, created_at, Raw("@seed_now"), 2+(i%12), comment, post_id))
    sql.insert("post_comments", ["id", "created_at", "updated_at", "author_id", "content", "post_id"], comments)
    sql.insert(
        "post_attachment_files",
        ["post_id", "file_id", "attachment_order"],
        [(1, 13, 0), (2, 14, 0), (3, 15, 0), (4, 16, 0)],
    )

    sql.insert(
        "drive_folders",
        ["id", "created_at", "updated_at", "created_by", "name", "parent_id"],
        [
            (i, rel_ts(200 - i, i), rel_ts(2, i), 1 if i % 2 else 2, name, parent)
            for i, name, parent in DRIVE_FOLDER_CATALOG
        ],
    )
    drive_files = [
        (
            i, rel_ts(int(file_objects[i - 1]["createdAtDaysAgo"]), i),
            Raw("@seed_now"), file_objects[i - 1]["originalName"], i,
            1 if i % 2 else 2, DRIVE_FILE_FOLDER_IDS[i - 1],
        )
        for i in range(1, 13)
    ]
    sql.insert(
        "drive_files",
        ["id", "created_at", "updated_at", "name", "storage_file_id", "uploader_id", "folder_id"],
        drive_files,
    )

    audit_rows = []
    for i in range(1, 91):
        actor = 1 if i%3 else 2
        menu_code, target_type, target_id = audit_target(i)
        audit_timestamp = rel_ts(45 - (i % 40), i)
        audit_rows.append((i, audit_timestamp, audit_timestamp, ["CREATE", "UPDATE", "DELETE"][i%3], actor, "demo.manager" if actor==1 else "demo.staff", None, menu_code, target_id, target_type, f"{i:08x}"[-8:]))
    sql.insert("audit_logs", ["id", "created_at", "updated_at", "action", "actor_id", "actor_login_id", "ip_address", "menu_code", "target_id", "target_type", "trace_id"], audit_rows)

    sql.line("INSERT INTO code_sequences (id, created_at, updated_at, current_seq, scope_key, target, version) SELECT 1, @seed_now, @seed_now, MAX(CAST(SUBSTRING(code,2) AS UNSIGNED)), 'GLOBAL', 'DEPARTMENT', 0 FROM departments;")
    sql.line("INSERT INTO code_sequences (id, created_at, updated_at, current_seq, scope_key, target, version) SELECT 2, @seed_now, @seed_now, MAX(CAST(SUBSTRING(code,2) AS UNSIGNED)), 'GLOBAL', 'POSITION', 0 FROM positions;")
    sql.line("INSERT INTO code_sequences (id, created_at, updated_at, current_seq, scope_key, target, version) SELECT 3, @seed_now, @seed_now, MAX(CAST(SUBSTRING(code,2) AS UNSIGNED)), 'GLOBAL', 'CUSTOMER', 0 FROM customers;")
    sql.line("INSERT INTO code_sequences (created_at, updated_at, current_seq, scope_key, target, version) SELECT @seed_now, @seed_now, MAX(CAST(SUBSTRING_INDEX(contract_no,'-',-1) AS UNSIGNED)), CAST(YEAR(contract_date) AS CHAR), 'CONTRACT', 0 FROM contracts GROUP BY YEAR(contract_date) ORDER BY YEAR(contract_date);")
    sql.line("INSERT INTO code_sequences (created_at, updated_at, current_seq, scope_key, target, version) SELECT @seed_now, @seed_now, MAX(CAST(SUBSTRING_INDEX(receipt_no,'-',-1) AS UNSIGNED)), CAST(YEAR(received_date) AS CHAR), 'AFTER_SERVICE', 0 FROM after_services GROUP BY YEAR(received_date) ORDER BY YEAR(received_date);")
    sql.line("COMMIT;")

    counts: dict[str, object] = {
        "acquisition_sources": 8, "after_services": 45, "approval_documents": 36,
        "approval_document_attachments": 14, "approval_steps": 52, "attendances": {"min": 380, "max": 450},
        "audit_logs": 90, "code_rules": 5, "code_rule_attribute_mappings": 0,
        "code_sequences": {"min": 6, "max": 8}, "contracts": 42, "contract_notes": 56,
        "contract_payments": 84, "customers": 48, "demo_seed_manifest": 1,
        "departments": 8, "drive_files": 12, "drive_folders": 10, "employees": 22,
        "engineers": 9, "equipments": 17, "event_publication": 0, "expense_claims": 12,
        "expense_items": 30, "leave_balances": 20, "leave_requests": 16, "positions": 7,
        "posts": 28, "post_attachment_files": 4, "post_comments": 64, "products": 32,
        "product_categories": 10, "roles": 3, "role_menus": 52, "sales_activities": 144,
        "sales_assignments": 60, "sales_contacts": 72, "sales_contact_employments": 84,
        "sales_contact_sources": 96, "service_expenses": 90, "service_visits": 60,
        "stored_files": 30, "suppliers": 8,
    }
    return sql.render(), counts


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def render_expected_files_sql(file_objects: list[dict[str, object]]) -> str:
    lines = [VERIFY_FILES_BEGIN, "INSERT INTO demo_expected_files VALUES"]
    for index, file_object in enumerate(file_objects):
        row = (
            file_object["id"],
            file_object["storedName"],
            file_object["originalName"],
            file_object["contentType"],
            file_object["size"],
            file_object["createdAtDaysAgo"],
            file_object["uploaderId"],
        )
        suffix = ";" if index + 1 == len(file_objects) else ","
        lines.append("  (" + ",".join(sql_value(value) for value in row) + ")" + suffix)
    lines.append(VERIFY_FILES_END)
    return "\n".join(lines)


def generate_verify_seed(output: Path, file_objects: list[dict[str, object]]) -> None:
    template_path = Path(__file__).with_name("verify_seed.sql.template")
    if not template_path.exists():
        raise SystemExit(f"seed verifier missing: {template_path}")
    template = template_path.read_text(encoding="utf-8")
    start = template.find(VERIFY_FILES_BEGIN)
    end = template.find(VERIFY_FILES_END)
    if start < 0 or end < start:
        raise SystemExit("seed verifier generated file markers are missing or out of order")
    end += len(VERIFY_FILES_END)
    generated = template[:start] + render_expected_files_sql(file_objects) + template[end:]
    version_tokens = {
        "@@SEED_VERSION@@": SEED_VERSION,
        "@@SCHEMA_VERSION@@": SCHEMA_VERSION,
        "@@SCENARIO_VERSION@@": SCENARIO_VERSION,
    }
    for token, value in version_tokens.items():
        if generated.count(token) != 1:
            raise SystemExit(f"seed verifier version token contract changed: {token}")
        generated = generated.replace(token, value)
    (output / "verify-seed.sql").write_text(generated, encoding="utf-8", newline="\n")


def generate(output: Path) -> None:
    output.mkdir(parents=True, exist_ok=True)
    archive_path = output / "seed-files.tar.gz"
    file_objects = create_files_archive(archive_path)
    provenance_time = source_datetime()
    seed_data, expected_counts = build_seed(file_objects, provenance_time)
    (output / "seed-data.sql").write_bytes(seed_data)
    generate_verify_seed(output, file_objects)
    schema_path = DEFAULT_OUTPUT / "schema.sql"
    if not schema_path.exists():
        raise SystemExit(f"canonical schema missing: {schema_path}")
    manifest = {
        "formatVersion": 1,
        "seedVersion": SEED_VERSION,
        "schemaVersion": SCHEMA_VERSION,
        "scenarioVersion": SCENARIO_VERSION,
        "schemaSourceCommit": SCHEMA_SOURCE_COMMIT,
        "compatibleAppVersion": COMPATIBLE_APP_VERSION,
        "generatedAt": provenance_time.isoformat(timespec="seconds"),
        "sourceDateEpoch": int(provenance_time.timestamp()),
        "timezone": "Asia/Seoul",
        "schemaSha256": sha256(schema_path),
        "dataSha256": hashlib.sha256(seed_data).hexdigest(),
        "filesSha256": sha256(archive_path),
        "expectedSchemaTableCount": 43,
        "expectedCounts": expected_counts,
        "startupDelta": {"employees": 1, "reason": "EmployeeInitializer creates the recovery operator from APP_ADMIN_*"},
        "publicAccounts": [
            {"loginId": "demo.manager", "password": MANAGER_PASSWORD, "role": "DEMO_MANAGER", "recommended": True},
            {"loginId": "demo.staff", "password": STAFF_PASSWORD, "role": "DEMO_STAFF", "recommended": False},
        ],
        "files": file_objects,
        "privacy": {
            "syntheticOnly": True,
            "operatorCredentialsIncluded": False,
            "supplierNameContract": "curated fictional supplier catalog",
            "contactDomain": ".example",
            "invalidRegistrationPrefix": "000",
            "identityNameContract": "curated fictional identity catalog",
            "invalidPhonePrefix": "000",
            "fileNameContract": "deterministic business fixture catalog",
        },
    }
    manifest_bytes = (json.dumps(manifest, ensure_ascii=False, sort_keys=True, indent=2) + "\n").encode("utf-8")
    (output / "manifest.json").write_bytes(manifest_bytes)


def check() -> int:
    with tempfile.TemporaryDirectory(prefix="simple-erp-seed-") as tmp:
        generated = Path(tmp)
        generate(generated)
        drift = []
        for name in ("seed-data.sql", "seed-files.tar.gz", "manifest.json", "verify-seed.sql"):
            tracked = DEFAULT_OUTPUT / name
            candidate = generated / name
            if not tracked.exists() or tracked.read_bytes() != candidate.read_bytes():
                drift.append(name)
        if drift:
            print("seed bundle drift: " + ", ".join(drift))
            return 1
    print("seed bundle is byte-for-byte reproducible")
    return 0


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--output-dir", type=Path, default=DEFAULT_OUTPUT)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    if args.check:
        return check()
    generate(args.output_dir.resolve())
    print(f"generated deterministic seed bundle in {args.output_dir.resolve()}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
