-- Canonical post-migrator schema for Simple ERP demo.
-- Source application commit: 7a6925e095ab371dfc76dd2d0c2fadea550b5db9
-- Product tables: MariaDB server 11.8.8, captured after application readiness and all startup migrators.
-- Demo control table: demo_seed_manifest, appended deterministically after the 42 product tables.
-- Application-owned enum columns are VARCHAR; event_publication.status remains framework-owned ENUM.
/*M!999999\- enable the sandbox mode */

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*M!100616 SET @OLD_NOTE_VERBOSITY=@@NOTE_VERBOSITY, NOTE_VERBOSITY=0 */;
DROP TABLE IF EXISTS `acquisition_sources`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `acquisition_sources` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL COMMENT '설명 / 비고 — 개최년도 / 장소 / 자유 메모',
  `name` varchar(100) NOT NULL COMMENT '컨택 경로 이름 — 전시회 명 / 소개자 명함명 등',
  `type` varchar(255) NOT NULL COMMENT '분류',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_acquisition_sources_name` (`name`),
  KEY `idx_acquisition_sources_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `after_services`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `after_services` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `assigned_engineer_id` bigint(20) DEFAULT NULL COMMENT '주 담당 엔지니어 — 방문 일지는 엔지니어별 별도 기록',
  `billing_amount` bigint(20) DEFAULT NULL COMMENT '유상 청구액 (원, VAT 별도) — 유상 확정 건만',
  `completed_date` date DEFAULT NULL COMMENT '완료일',
  `customer_id` bigint(20) NOT NULL COMMENT '고객사 참조 (customer 모듈)',
  `equipment_id` bigint(20) DEFAULT NULL COMMENT '설비 대장 참조 (equipment 모듈) — 대장 미등록 설비 접수는 null',
  `receipt_no` varchar(255) NOT NULL COMMENT 'AS 접수번호 (채번 규칙 AFTER_SERVICE)',
  `received_date` date NOT NULL COMMENT '접수일',
  `status` varchar(255) NOT NULL COMMENT '진행 상태 (접수 / 배정 / 진행중 / 완료)',
  `symptom` text DEFAULT NULL COMMENT '증상 / 요청 내용',
  `type` varchar(255) NOT NULL COMMENT 'AS 유형 (수리 / 설치지원 / 교육 / 통역 / 조건셋팅)',
  `warranty_decision` varchar(255) NOT NULL COMMENT '유상 / 무상 판정 — 설비 보증 기반 제안 + 담당자 확정',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKbxgglpwx737we15tw0mfcsoww` (`receipt_no`),
  KEY `idx_after_services_customer_id` (`customer_id`),
  KEY `idx_after_services_equipment_id` (`equipment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `approval_document_attachments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `approval_document_attachments` (
  `document_id` bigint(20) NOT NULL,
  `file_id` bigint(20) NOT NULL,
  `attachment_order` int(11) NOT NULL,
  PRIMARY KEY (`document_id`,`attachment_order`),
  CONSTRAINT `FKrd81puyi5xchtr4ymd1sjrb1i` FOREIGN KEY (`document_id`) REFERENCES `approval_documents` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `approval_documents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `approval_documents` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `content` text DEFAULT NULL COMMENT '본문',
  `current_step_order` int(11) NOT NULL COMMENT '현재 결재 차례 — 1부터',
  `doc_type` varchar(255) NOT NULL COMMENT '문서 유형',
  `drafter_id` bigint(20) NOT NULL COMMENT '기안자 직원 식별자',
  `ref_id` bigint(20) DEFAULT NULL COMMENT '연동 도메인 레코드 식별자 — GENERAL 기안은 null',
  `status` varchar(255) NOT NULL COMMENT '문서 상태',
  `title` varchar(255) NOT NULL COMMENT '제목',
  `version` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_approval_documents_drafter_id` (`drafter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `approval_steps`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `approval_steps` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `approver_id` bigint(20) NOT NULL COMMENT '결재자 직원 식별자',
  `comment` varchar(255) DEFAULT NULL COMMENT '결재 의견',
  `decided_at` datetime(6) DEFAULT NULL COMMENT '결정 일시',
  `status` varchar(255) NOT NULL COMMENT '단계 상태',
  `step_order` int(11) NOT NULL COMMENT '결재 순번 — 1부터',
  `document_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_approval_steps_document_step_order` (`document_id`,`step_order`),
  KEY `idx_approval_steps_approver_id` (`approver_id`),
  CONSTRAINT `FKmpgrvg3pifq2audevoj27vi87` FOREIGN KEY (`document_id`) REFERENCES `approval_documents` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `attendances`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `attendances` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `check_in_at` datetime(6) DEFAULT NULL COMMENT '출근 시각',
  `check_in_latitude` double DEFAULT NULL COMMENT '출근 위치 위도',
  `check_in_longitude` double DEFAULT NULL COMMENT '출근 위치 경도',
  `check_in_within_range` bit(1) NOT NULL COMMENT '출근 위치가 사무실 허용 반경 내인지',
  `check_out_at` datetime(6) DEFAULT NULL COMMENT '퇴근 시각',
  `check_out_latitude` double DEFAULT NULL COMMENT '퇴근 위치 위도',
  `check_out_longitude` double DEFAULT NULL COMMENT '퇴근 위치 경도',
  `check_out_within_range` bit(1) NOT NULL COMMENT '퇴근 위치가 사무실 허용 반경 내인지',
  `employee_id` bigint(20) NOT NULL COMMENT '직원 ID — employee 모듈 참조 (bare Long)',
  `work_date` date NOT NULL COMMENT '근무일',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_attendances_employee_work_date` (`employee_id`,`work_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `audit_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_logs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `action` varchar(255) NOT NULL,
  `actor_id` bigint(20) DEFAULT NULL,
  `actor_login_id` varchar(64) NOT NULL,
  `ip_address` varchar(64) DEFAULT NULL,
  `menu_code` varchar(255) NOT NULL,
  `target_id` bigint(20) DEFAULT NULL,
  `target_type` varchar(64) DEFAULT NULL,
  `trace_id` varchar(8) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_audit_menu_created` (`menu_code`,`created_at` DESC),
  KEY `idx_audit_actor_created` (`actor_id`,`created_at` DESC),
  KEY `idx_audit_target` (`target_type`,`target_id`,`created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `code_rule_attribute_mappings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `code_rule_attribute_mappings` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `attribute_key` varchar(50) NOT NULL COMMENT '도메인 attribute key (대문자, 예: TYPE)',
  `code_value` varchar(50) NOT NULL COMMENT '코드 안에 치환될 문자열 (예: G)',
  `source_value` varchar(100) NOT NULL COMMENT '도메인 enum/분류 값 (예: GENERAL)',
  `target` varchar(255) NOT NULL COMMENT '채번 대상',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_carm_target_key_source` (`target`,`attribute_key`,`source_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `code_rules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `code_rules` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL COMMENT '사용자 메모',
  `input_mode` varchar(255) NOT NULL COMMENT '코드 입력 방식',
  `pattern` varchar(200) NOT NULL COMMENT '코드 패턴 (예: D-{YYYY}-{SEQ:4}). literal 문자열 + 토큰 조합',
  `target` varchar(255) NOT NULL COMMENT '채번 대상 (DEPARTMENT 등)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK4iq3g99r8fyf4hge5ysqiqbvm` (`target`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `code_sequences`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `code_sequences` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `current_seq` bigint(20) NOT NULL COMMENT '현재까지 발급된 시퀀스 (다음 발급 = currentSeq + 1)',
  `scope_key` varchar(200) NOT NULL COMMENT '초기화 정책 + parentScoped 조합 키 (예: 2026, 2026-04|D001)',
  `target` varchar(255) NOT NULL COMMENT '채번 대상',
  `version` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code_sequences_target_scope` (`target`,`scope_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `contract_notes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `contract_notes` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `author_employee_id` bigint(20) NOT NULL COMMENT '작성자 직원 참조 (employee 모듈)',
  `content` text NOT NULL COMMENT '메모 내용',
  `contract_id` bigint(20) NOT NULL COMMENT '계약 식별자',
  PRIMARY KEY (`id`),
  KEY `idx_contract_notes_contract_id` (`contract_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `contract_payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `contract_payments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `contract_id` bigint(20) NOT NULL COMMENT '계약 식별자',
  `invoice_amount` bigint(20) DEFAULT NULL COMMENT '세금계산서 금액 (원, VAT 별도)',
  `invoice_date` date DEFAULT NULL COMMENT '세금계산서 발행일',
  `label` varchar(50) NOT NULL COMMENT '회차 라벨 (계약금 / 중도금 / 잔금 등 자유 입력)',
  `note` varchar(255) DEFAULT NULL COMMENT '메모 (지원금 입금 연동 등)',
  `paid_amount` bigint(20) DEFAULT NULL COMMENT '입금액 (원, VAT 별도)',
  `paid_date` date DEFAULT NULL COMMENT '입금일',
  `planned_amount` bigint(20) DEFAULT NULL COMMENT '예정 금액 (원, VAT 별도)',
  `planned_date` date DEFAULT NULL COMMENT '입금 예정일',
  PRIMARY KEY (`id`),
  KEY `idx_contract_payments_contract_id` (`contract_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `contracts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `contracts` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `arrival_date` date DEFAULT NULL COMMENT '설비 입고일',
  `contract_date` date NOT NULL COMMENT '계약일',
  `contract_no` varchar(255) NOT NULL COMMENT '계약 번호 (채번 규칙 CONTRACT)',
  `cretop_grade` varchar(10) DEFAULT NULL COMMENT '계약 시점 CRETOP 신용등급 스냅샷 (소문자 = 모의등급)',
  `customer_id` bigint(20) NOT NULL COMMENT '고객사 참조 (customer 모듈)',
  `due_date` date DEFAULT NULL COMMENT '납기일',
  `employee_id` bigint(20) NOT NULL COMMENT '계약자 (영업 담당) 참조 (employee 모듈)',
  `expected_arrival_date` date DEFAULT NULL COMMENT '설비 입고 예정일',
  `final_amount` bigint(20) NOT NULL COMMENT '최종 계약금액 (원, VAT 별도) — 협상 / 옵션 변경 반영가',
  `initial_amount` bigint(20) DEFAULT NULL COMMENT '초기 계약금액 (원, VAT 별도)',
  `installed_date` date DEFAULT NULL COMMENT '설치 완료일',
  `logistics_note` varchar(255) DEFAULT NULL COMMENT '물류 메모 (컨테이너 구성 등)',
  `option_text` text DEFAULT NULL COMMENT '옵션 사양 (BEVEL, FMC, CHUCK 구성 등) — 계약별 자유 기재',
  `order_date` date DEFAULT NULL COMMENT '중국 공급사 발주일',
  `output_unit` varchar(255) DEFAULT NULL COMMENT '출력 단위 (KW / TON)',
  `output_value` decimal(10,2) DEFAULT NULL COMMENT '출력 값 (예: 12 kW, 220 ton) — 계약별 사양',
  `product_id` bigint(20) NOT NULL COMMENT '제품 모델 참조 (product 모듈)',
  `settled_date` date DEFAULT NULL COMMENT '정산 완료일',
  `status` varchar(255) NOT NULL COMMENT '계약 진행 상태',
  `supplier_id` bigint(20) NOT NULL COMMENT '공급사 참조 (supplier 모듈) — 계약 시점 제품의 공급사 스냅샷',
  `support_program_name` varchar(255) DEFAULT NULL COMMENT '정부 지원사업 프로그램명 (안전동행, 스마트공방 등)',
  `support_program_status` varchar(255) NOT NULL COMMENT '지원사업 진행 상태',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKa8rec2yv0qi7ir7byaa4rsqhy` (`contract_no`),
  KEY `idx_contracts_customer_id` (`customer_id`),
  KEY `idx_contracts_employee_id` (`employee_id`),
  KEY `idx_contracts_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `customers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `customers` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `detail_address` varchar(255) DEFAULT NULL COMMENT '상세 주소',
  `road_address` varchar(255) DEFAULT NULL COMMENT '기본 주소',
  `zip_code` varchar(255) DEFAULT NULL COMMENT '우편번호',
  `biz_item` varchar(255) DEFAULT NULL COMMENT '업종',
  `biz_reg_no` varchar(255) DEFAULT NULL COMMENT '사업자등록번호',
  `biz_type` varchar(255) DEFAULT NULL COMMENT '업태',
  `code` varchar(255) NOT NULL COMMENT '고객사 코드',
  `corp_reg_no` varchar(255) DEFAULT NULL COMMENT '법인등록번호',
  `email` varchar(255) DEFAULT NULL COMMENT '대표 이메일',
  `fax` varchar(255) DEFAULT NULL COMMENT '팩스',
  `name` varchar(255) NOT NULL COMMENT '고객사명',
  `name_en` varchar(255) DEFAULT NULL COMMENT '영문 고객사명',
  `note` text DEFAULT NULL COMMENT '비고',
  `phone` varchar(255) DEFAULT NULL COMMENT '대표 전화',
  `representative` varchar(255) DEFAULT NULL COMMENT '대표자명',
  `status` varchar(255) NOT NULL COMMENT '거래 상태',
  `trade_start_date` date DEFAULT NULL COMMENT '거래 시작일',
  `type` varchar(255) NOT NULL COMMENT '고객 분류',
  `website` varchar(255) DEFAULT NULL COMMENT '홈페이지',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKmkwx1x9mthieapj92cpxq5msc` (`code`),
  UNIQUE KEY `UKi5b6le25rpujai93b0agbt1rx` (`biz_reg_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `departments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `departments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `code` varchar(255) NOT NULL COMMENT '부서 코드',
  `name` varchar(255) NOT NULL COMMENT '부서명',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '상위 부서 외래키',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKl7tivi5261wxdnvo6cct9gg6t` (`code`),
  KEY `FK63q917a0aq92i7gcw6h7f1jrv` (`parent_id`),
  CONSTRAINT `FK63q917a0aq92i7gcw6h7f1jrv` FOREIGN KEY (`parent_id`) REFERENCES `departments` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `drive_files`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `drive_files` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `name` varchar(255) NOT NULL COMMENT '표시명 (업로드 시 원본 파일명)',
  `storage_file_id` bigint(20) NOT NULL COMMENT 'storage 모듈 파일 식별자',
  `uploader_id` bigint(20) NOT NULL COMMENT '업로드한 직원 식별자',
  `folder_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_drive_files_folder_id` (`folder_id`),
  CONSTRAINT `FKbhguubj2egsh45vjedslthyyk` FOREIGN KEY (`folder_id`) REFERENCES `drive_folders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `drive_folders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `drive_folders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `created_by` bigint(20) NOT NULL COMMENT '생성한 직원 식별자',
  `name` varchar(255) NOT NULL COMMENT '폴더명',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '상위 폴더 식별자 — null 이면 루트 직속',
  PRIMARY KEY (`id`),
  KEY `idx_drive_folders_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `employees`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `employees` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `detail_address` varchar(255) DEFAULT NULL COMMENT '상세 주소',
  `road_address` varchar(255) DEFAULT NULL COMMENT '기본 주소',
  `zip_code` varchar(255) DEFAULT NULL COMMENT '우편번호',
  `birth_date` date DEFAULT NULL COMMENT '생년월일',
  `department_id` bigint(20) DEFAULT NULL COMMENT '부서 식별자',
  `email` varchar(255) DEFAULT NULL COMMENT '이메일 주소',
  `join_date` date DEFAULT NULL COMMENT '입사일',
  `login_id` varchar(255) NOT NULL COMMENT '로그인 id',
  `name` varchar(255) NOT NULL COMMENT '직원명',
  `password` varchar(255) NOT NULL COMMENT '비밀번호',
  `phone` varchar(255) DEFAULT NULL COMMENT '연락처',
  `position_id` bigint(20) DEFAULT NULL COMMENT '직책 식별자',
  `role_id` bigint(20) DEFAULT NULL COMMENT '역할 식별자',
  `status` varchar(255) NOT NULL COMMENT '재직 상태',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKlo0aqea2uilso5tj35u8c05w` (`login_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `engineers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `engineers` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `active` bit(1) NOT NULL COMMENT '사용 여부 — 계약 종료된 외주 등 숨김용',
  `affiliation` varchar(100) DEFAULT NULL COMMENT '소속 (외주 업체명 / 공급사명 등)',
  `employee_id` bigint(20) DEFAULT NULL COMMENT '내부 직원 링크 (employee 모듈) — 내부 구분일 때만 선택 입력',
  `name` varchar(50) NOT NULL COMMENT '이름',
  `phone` varchar(30) DEFAULT NULL COMMENT '연락처',
  `type` varchar(255) NOT NULL COMMENT '구분 (내부 / 외주 / 제조사)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `equipments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `equipments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `confirmed_date` date DEFAULT NULL COMMENT '설치완료확인서 (교육 포함) 일자',
  `contract_id` bigint(20) DEFAULT NULL COMMENT '원천 계약 참조 (contract 모듈) — 계약 설치완료 자동 생성분만 채워짐, 과거 이관분은 null',
  `customer_id` bigint(20) NOT NULL COMMENT '고객사 참조 (customer 모듈)',
  `general_warranty_end_date` date DEFAULT NULL COMMENT '무상 AS 만료일 — 기산일 + 개월 파생값 (필터용 저장)',
  `general_warranty_months` int(11) DEFAULT NULL COMMENT '발진기 외 무상 AS 개월 — 계약서 기준 0~30',
  `install_address` varchar(255) DEFAULT NULL COMMENT '설치 주소 — 고객사 주소와 다른 실사례가 있어 별도 보유',
  `installed_date` date DEFAULT NULL COMMENT '설치일',
  `note` text DEFAULT NULL COMMENT '비고',
  `oscillator_warranty_end_date` date DEFAULT NULL COMMENT '발진기 보증 만료일 — 기산일 + 개월 파생값 (필터용 저장)',
  `oscillator_warranty_months` int(11) DEFAULT NULL COMMENT '발진기 (레이저 소스) 보증 개월 — 계약서 기준 24~60',
  `output_unit` varchar(255) DEFAULT NULL COMMENT '출력 단위 (KW / TON)',
  `output_value` decimal(10,2) DEFAULT NULL COMMENT '출력 값 (예: 12 kW, 220 ton)',
  `product_id` bigint(20) NOT NULL COMMENT '제품 모델 참조 (product 모듈)',
  `serial_no` varchar(100) DEFAULT NULL COMMENT '시리얼 번호 (명판) — 미확인 설비는 null',
  `supplier_id` bigint(20) NOT NULL COMMENT '공급사 참조 (supplier 모듈) — 등록 시점 제품의 공급사 스냅샷',
  `warranty_insurance` bit(1) NOT NULL COMMENT '보증보험 가입 여부',
  `warranty_start_date` date DEFAULT NULL COMMENT '보증 기산일',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_equipments_contract_id` (`contract_id`),
  KEY `idx_equipments_customer_id` (`customer_id`),
  KEY `idx_equipments_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `event_publication`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `event_publication` (
  `id` uuid NOT NULL,
  `completion_attempts` int(11) NOT NULL,
  `completion_date` datetime(6) DEFAULT NULL,
  `event_type` varchar(255) NOT NULL,
  `last_resubmission_date` datetime(6) DEFAULT NULL,
  `listener_id` varchar(255) NOT NULL,
  `publication_date` datetime(6) NOT NULL,
  `serialized_event` varchar(255) NOT NULL,
  `status` enum('COMPLETED','FAILED','PROCESSING','PUBLISHED','RESUBMITTED') DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `expense_claims`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `expense_claims` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `approval_document_id` bigint(20) DEFAULT NULL COMMENT '연동된 결재 문서 식별자',
  `claimant_id` bigint(20) NOT NULL COMMENT '청구자 직원 식별자',
  `status` varchar(255) NOT NULL COMMENT '청구 상태',
  `title` varchar(255) NOT NULL COMMENT '청구 제목',
  `total_amount` decimal(15,2) NOT NULL COMMENT '총 청구 금액 — 항목 합계를 서버에서 계산',
  PRIMARY KEY (`id`),
  KEY `idx_expense_claims_claimant_id` (`claimant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `expense_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `expense_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `amount` decimal(15,2) NOT NULL COMMENT '지출 금액',
  `category` varchar(255) NOT NULL COMMENT '지출 분류',
  `description` varchar(255) DEFAULT NULL COMMENT '지출 내용',
  `expense_date` date NOT NULL COMMENT '지출일',
  `receipt_file_id` bigint(20) DEFAULT NULL COMMENT '영수증 파일 식별자 — 없으면 null',
  `claim_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKf1jneqy8qj2pp12aq1u29p1q4` (`claim_id`),
  CONSTRAINT `FKf1jneqy8qj2pp12aq1u29p1q4` FOREIGN KEY (`claim_id`) REFERENCES `expense_claims` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `leave_balances`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `leave_balances` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `employee_id` bigint(20) NOT NULL COMMENT '직원 ID — employee 모듈 참조 (bare Long)',
  `granted_days` decimal(5,1) NOT NULL COMMENT '부여 일수',
  `used_days` decimal(5,1) NOT NULL COMMENT '사용 일수 — 승인 콜백에서만 증가',
  `year` int(11) NOT NULL COMMENT '귀속 연도',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_leave_balances_employee_year` (`employee_id`,`year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `leave_requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `leave_requests` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `approval_document_id` bigint(20) DEFAULT NULL COMMENT '연동된 결재 문서 ID — approval 모듈 참조 (bare Long)',
  `days` decimal(4,1) NOT NULL COMMENT '사용 일수 — 주말 제외 계산, 반차 0.5',
  `employee_id` bigint(20) NOT NULL COMMENT '신청 직원 ID — employee 모듈 참조 (bare Long)',
  `end_date` date NOT NULL COMMENT '종료일',
  `leave_type` varchar(255) NOT NULL COMMENT '휴가 유형',
  `reason` varchar(500) DEFAULT NULL COMMENT '사유',
  `start_date` date NOT NULL COMMENT '시작일',
  `status` varchar(255) NOT NULL COMMENT '신청 상태',
  PRIMARY KEY (`id`),
  KEY `idx_leave_requests_employee_id` (`employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `positions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `positions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `code` varchar(255) NOT NULL COMMENT '직책 코드',
  `description` varchar(500) DEFAULT NULL COMMENT '직책 설명',
  `name` varchar(255) NOT NULL COMMENT '직책명',
  `rank_level` int(11) NOT NULL COMMENT '직책 서열 — 작을수록 상위. 등록 시 자동으로 max+1 부여, 서열 관리 페이지에서 일괄 재계산.',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKdjkia0ifarv9epmv78bh62r3o` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `post_attachment_files`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_attachment_files` (
  `post_id` bigint(20) NOT NULL,
  `file_id` bigint(20) NOT NULL COMMENT '첨부 파일 식별자 — global/storage 참조',
  `attachment_order` int(11) NOT NULL,
  PRIMARY KEY (`post_id`,`attachment_order`),
  CONSTRAINT `FKmrc3adljd5jxid58gfa2rhjbw` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `post_comments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_comments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `author_id` bigint(20) NOT NULL COMMENT '댓글 작성자 직원 식별자',
  `content` varchar(1000) NOT NULL COMMENT '댓글 내용',
  `post_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKaawaqxjs3br8dw5v90w7uu514` (`post_id`),
  CONSTRAINT `FKaawaqxjs3br8dw5v90w7uu514` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `posts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `posts` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `author_id` bigint(20) NOT NULL COMMENT '작성자 직원 식별자',
  `category` varchar(255) NOT NULL COMMENT '게시판 카테고리',
  `content` text NOT NULL COMMENT '본문',
  `title` varchar(200) NOT NULL COMMENT '제목',
  PRIMARY KEY (`id`),
  KEY `idx_posts_category` (`category`),
  KEY `idx_posts_author_id` (`author_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `product_categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_categories` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `name` varchar(255) NOT NULL COMMENT '카테고리명',
  `sort_order` int(11) NOT NULL COMMENT '노출 순서 — 카탈로그 분류 순서와 동일하게 유지',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKfl075bwasjwsxybk4x174befx` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `active` bit(1) NOT NULL COMMENT '사용 여부 — 단종 / 취급 중단 모델 숨김용',
  `model_name` varchar(255) NOT NULL COMMENT '모델명',
  `note` text DEFAULT NULL COMMENT '비고',
  `supplier_id` bigint(20) NOT NULL COMMENT '공급사 참조 (supplier 모듈)',
  `category_id` bigint(20) NOT NULL COMMENT '제품 카테고리 외래키',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_products_supplier_model` (`supplier_id`,`model_name`),
  KEY `FK6t5dtw6tyo83ywljwohuc6g7k` (`category_id`),
  CONSTRAINT `FK6t5dtw6tyo83ywljwohuc6g7k` FOREIGN KEY (`category_id`) REFERENCES `product_categories` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `role_menus`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `role_menus` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `can_read` bit(1) DEFAULT NULL COMMENT '읽기 권한',
  `can_write` bit(1) DEFAULT NULL COMMENT '쓰기 권한',
  `data_scope` varchar(20) NOT NULL DEFAULT 'ALL' COMMENT '데이터 스코프 (행 단위 가시 범위)',
  `menu_code` varchar(255) NOT NULL COMMENT '메뉴 코드',
  `role_id` bigint(20) NOT NULL COMMENT '권한 외래키',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menus_role_menu` (`role_id`,`menu_code`),
  CONSTRAINT `FK8w16n9supii3exa5gnfdey3vu` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `code` varchar(255) NOT NULL COMMENT '역할 코드',
  `description` varchar(255) DEFAULT NULL COMMENT '역할 설명',
  `name` varchar(255) NOT NULL COMMENT '역할명',
  `system` bit(1) NOT NULL COMMENT '시스템 권한 여부 — true 면 삭제/코드 변경/매트릭스 편집 차단 (예: MASTER)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKch1113horj4qr56f91omojv8` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sales_activities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `sales_activities` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `activity_date` datetime(6) NOT NULL COMMENT '활동 일시',
  `content` text DEFAULT NULL COMMENT '활동 내용',
  `customer_contact_id` bigint(20) DEFAULT NULL COMMENT '고객사 담당자 (영업 명부 식별자)',
  `customer_id` bigint(20) NOT NULL COMMENT '고객사 식별자',
  `our_employee_id` bigint(20) NOT NULL COMMENT '우리쪽 담당 직원 식별자',
  `subject` varchar(255) NOT NULL COMMENT '활동 제목',
  `type` varchar(255) NOT NULL COMMENT '활동 유형',
  PRIMARY KEY (`id`),
  KEY `idx_sales_activities_customer_id` (`customer_id`),
  KEY `idx_sales_activities_activity_date` (`activity_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sales_assignments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `sales_assignments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `customer_id` bigint(20) NOT NULL COMMENT '고객사 식별자',
  `employee_id` bigint(20) NOT NULL COMMENT '영업 담당 직원 식별자',
  `end_date` date DEFAULT NULL COMMENT '배정 종료일 — null 이면 현재 담당',
  `active_employee_id` bigint(20) GENERATED ALWAYS AS (CASE WHEN `end_date` IS NULL THEN `employee_id` ELSE NULL END) VIRTUAL,
  `is_primary` bit(1) NOT NULL COMMENT '주 담당 여부',
  `reason` varchar(255) DEFAULT NULL COMMENT '배정 / 변경 사유 (이직, 퇴사, 일반 변경 등 자유)',
  `start_date` date NOT NULL COMMENT '배정 시작일',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sales_assignments_active_customer_employee` (`customer_id`,`active_employee_id`),
  KEY `idx_sales_assignments_customer_id` (`customer_id`),
  KEY `idx_sales_assignments_employee_id` (`employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sales_contact_employments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `sales_contact_employments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `contact_id` bigint(20) NOT NULL COMMENT '영업 명부 식별자',
  `customer_id` bigint(20) DEFAULT NULL COMMENT '우리 고객사 식별자 — null 이면 외부 회사',
  `department` varchar(255) DEFAULT NULL COMMENT '부서',
  `departure_note` varchar(255) DEFAULT NULL COMMENT '종료 사유 자유 메모',
  `departure_type` varchar(255) DEFAULT NULL COMMENT '종료 분류 — endDate 가 채워질 때만 의미 있음',
  `end_date` date DEFAULT NULL COMMENT '재직 종료일 — null 이면 현재 재직',
  `external_company_name` varchar(255) DEFAULT NULL COMMENT '외부 회사 자유 입력 — customerId 가 null 일 때 채움',
  `position` varchar(255) DEFAULT NULL COMMENT '직책',
  `start_date` date NOT NULL COMMENT '재직 시작일',
  PRIMARY KEY (`id`),
  KEY `idx_sales_contact_employments_contact_id` (`contact_id`),
  KEY `idx_sales_contact_employments_customer_id` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sales_contact_sources`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `sales_contact_sources` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `contact_id` bigint(20) NOT NULL COMMENT '영업 명부 식별자',
  `source_id` bigint(20) NOT NULL COMMENT '컨택 경로 식별자',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sales_contact_sources_contact_source` (`contact_id`,`source_id`),
  KEY `idx_sales_contact_sources_contact_id` (`contact_id`),
  KEY `idx_sales_contact_sources_source_id` (`source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sales_contacts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `sales_contacts` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL COMMENT '회사 이메일',
  `met_at` date DEFAULT NULL COMMENT '최초 미팅일짜',
  `mobile_phone` varchar(30) DEFAULT NULL COMMENT '휴대폰 — 명부 식별 고유 키',
  `name` varchar(255) NOT NULL COMMENT '이름',
  `name_en` varchar(255) DEFAULT NULL COMMENT '영문명',
  `note` text DEFAULT NULL COMMENT '비고',
  `office_phone` varchar(255) DEFAULT NULL COMMENT '전화번호',
  `personal_email` varchar(255) DEFAULT NULL COMMENT '개인 이메일',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKqevt0ge3r7lu0y79x3ybxkrp2` (`mobile_phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `service_expenses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `service_expenses` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `after_service_id` bigint(20) NOT NULL COMMENT 'AS 건 식별자',
  `amount` bigint(20) NOT NULL COMMENT '금액 (원)',
  `category` varchar(255) NOT NULL COMMENT '경비 분류 (일당 / 숙박비 / 식대 / 부품비 / 기타)',
  `engineer_id` bigint(20) DEFAULT NULL COMMENT '관련 엔지니어 — 부품비 등 엔지니어 무관 경비는 null',
  `note` varchar(255) DEFAULT NULL COMMENT '메모',
  `paid_date` date DEFAULT NULL COMMENT '결제일',
  `payer_type` varchar(255) NOT NULL COMMENT '결제 주체 (회사 직접결제 / 엔지니어 청구)',
  PRIMARY KEY (`id`),
  KEY `idx_service_expenses_after_service_id` (`after_service_id`),
  KEY `idx_service_expenses_engineer_id` (`engineer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `service_visits`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `service_visits` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `after_service_id` bigint(20) NOT NULL COMMENT 'AS 건 식별자',
  `engineer_id` bigint(20) NOT NULL COMMENT '담당 엔지니어 (모듈 내부 마스터)',
  `problem` text DEFAULT NULL COMMENT '문제 (증상 / 발견 사항)',
  `resolution` text DEFAULT NULL COMMENT '해결 (조치 내용)',
  `visit_date` date NOT NULL COMMENT '방문일',
  PRIMARY KEY (`id`),
  KEY `idx_service_visits_after_service_id` (`after_service_id`),
  KEY `idx_service_visits_engineer_id` (`engineer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `stored_files`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `stored_files` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `content_type` varchar(128) DEFAULT NULL,
  `original_name` varchar(255) NOT NULL,
  `owner_id` bigint(20) DEFAULT NULL COMMENT '파일을 독점 소유하는 업무 레코드 식별자',
  `owner_type` varchar(255) DEFAULT NULL COMMENT '파일을 독점 소유하는 업무 레코드 유형',
  `size` bigint(20) NOT NULL,
  `status` varchar(255) NOT NULL DEFAULT 'PENDING' COMMENT '파일 생명주기 상태',
  `stored_name` varchar(36) NOT NULL,
  `uploader_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKjjd7hf5amerajq056e2nd5y31` (`stored_name`),
  KEY `idx_stored_files_status_created_at` (`status`,`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `suppliers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `suppliers` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `active` bit(1) NOT NULL COMMENT '사용 여부 — 거래 중단 공급사 숨김용',
  `country` varchar(255) DEFAULT NULL COMMENT '국가',
  `name` varchar(255) NOT NULL COMMENT '공급사명 (영문 표준 표기)',
  `name_ko` varchar(255) DEFAULT NULL COMMENT '공급사명 (한글 표기)',
  `note` text DEFAULT NULL COMMENT '비고',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKeegixpn11chp14nb25tl3ucv0` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `demo_seed_manifest`;
CREATE TABLE `demo_seed_manifest` (
  `id` tinyint unsigned NOT NULL,
  `seed_version` varchar(64) NOT NULL,
  `schema_version` varchar(64) NOT NULL,
  `scenario_version` varchar(64) NOT NULL,
  `generated_at` datetime(6) NOT NULL,
  `reset_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `chk_demo_seed_manifest_singleton` CHECK (`id` = 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*M!100616 SET NOTE_VERBOSITY=@OLD_NOTE_VERBOSITY */;
