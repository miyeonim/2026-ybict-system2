-- 1. 부서 및 직원 테이블
CREATE TABLE IF NOT EXISTS its_kdn_dep (
    DEP_ID VARCHAR(10) NOT NULL COMMENT '부서코드',
    PAR_DEP_ID VARCHAR(20) COMMENT '대분류',
    DEP_TITLE VARCHAR(30) COMMENT '부서명',
    KEPCO_MAP VARCHAR(10) COMMENT '본사여부',
    PRIMARY KEY (DEP_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='KDN부서';

CREATE TABLE IF NOT EXISTS its_kdn_user (
    DEP_ID VARCHAR(10) NOT NULL COMMENT '부서코드',
    LOGIN_ID VARCHAR(10) NOT NULL COMMENT '로그인ID',
    USER_ID VARCHAR(10) COMMENT '사용자ID',
    USER_NM VARCHAR(20) COMMENT '이름',
    USER_PWD VARCHAR(100) COMMENT '비번',
    EMAIL VARCHAR(50) COMMENT '이메일',
    CELL_PHONE VARCHAR(20) COMMENT '전화번호',
    PWD_FAIL_COUNT INT DEFAULT 0 COMMENT '로그인실패카운트',
    DEL_YN VARCHAR(1) DEFAULT 'N' COMMENT '삭제여부',
    POSITION_CODE VARCHAR(10) COMMENT '직책코드',
    POSITION_NM VARCHAR(20) COMMENT '직책',
    CP_AUTH_YN VARCHAR(1) DEFAULT 'N' COMMENT '형상연계여부',
    CP_AUTH_DT VARCHAR(14) COMMENT '형상연계일',
    PRIMARY KEY (DEP_ID, LOGIN_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='KDN직원정보';

-- 1-1. 한전(KEPCO) 조직도/인사정보 (임시)
-- 한전측이 제공한 실제 조직도/인사정보 테이블의 컬럼 구조를 그대로 반영.
-- 실 연동(API/배치) 전까지는 이 테이블에 수기로 데이터를 채워 쓰는 임시 상태.
-- 한전 사용자는 SSO로 인증되어 비밀번호 컬럼이 없다(추후 한전 망 이관 시 SSO 연동 예정).
-- 그 전까지는 JwtServiceImp에서 한전 사용자는 비밀번호 검증 없이(공란이어도) 로그인 처리한다.
CREATE TABLE IF NOT EXISTS ictyb_kepco_dep (
    OF_CD VARCHAR(16) NOT NULL COMMENT '조직코드(전체, OF_CD1~4 연결값)',
    OF_CODE VARCHAR(4) COMMENT '조직코드(말단, OF_CD4와 동일)',
    OF_CD1 VARCHAR(4) COMMENT '본부코드',
    OF_CD2 VARCHAR(4) COMMENT '지사코드',
    OF_CD3 VARCHAR(4) COMMENT '부코드',
    OF_CD4 VARCHAR(4) COMMENT '팀코드',
    OF_HAN1 VARCHAR(30) COMMENT '본부명',
    OF_HAN2 VARCHAR(30) COMMENT '지사명',
    OF_HAN3 VARCHAR(30) COMMENT '부명',
    OF_HAN4 VARCHAR(30) COMMENT '팀명',
    SER_GU VARCHAR(2) COMMENT '서비스구분',
    SER_1 VARCHAR(4) COMMENT '서비스코드1',
    SER_2 VARCHAR(4) COMMENT '서비스코드2',
    SER_3 VARCHAR(4) COMMENT '서비스코드3',
    SER_4 VARCHAR(4) COMMENT '서비스코드4',
    F_BONSA VARCHAR(1) COMMENT '본사구분값',
    F_JIKHAL VARCHAR(1) COMMENT '직할여부',
    UPDATEDATE VARCHAR(10) COMMENT '갱신일자',
    PRIMARY KEY (OF_CD)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='한전 조직도(임시, 실제 테이블 구조 반영)';

CREATE TABLE IF NOT EXISTS ictyb_kepco_user (
    SABUN VARCHAR(10) NOT NULL COMMENT '사번',
    NAME VARCHAR(20) COMMENT '이름',
    JIKGUB_HAN VARCHAR(20) COMMENT '직급명',
    JIKKUN_HAN VARCHAR(20) COMMENT '직군명',
    SOSOK_HAN VARCHAR(30) COMMENT '소속명',
    SOSOK_CD VARCHAR(16) COMMENT '소속코드(전체, ictyb_kepco_dep.OF_CD 참조)',
    SOSOK_CD1 VARCHAR(4) COMMENT '소속코드-본부',
    SOSOK_CD2 VARCHAR(4) COMMENT '소속코드-지사',
    SOSOK_CD3 VARCHAR(4) COMMENT '소속코드-부',
    SOSOK_CD4 VARCHAR(4) COMMENT '소속코드-팀',
    TEL VARCHAR(20) COMMENT '전화번호',
    JIKGUB VARCHAR(4) COMMENT '직급코드',
    JIKYEE VARCHAR(30) COMMENT '직예명',
    HP VARCHAR(20) COMMENT '휴대폰번호',
    E_MAIL VARCHAR(50) COMMENT '이메일',
    UPDATEDATE VARCHAR(10) COMMENT '갱신일자',
    PRIMARY KEY (SABUN)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='한전 인사정보(임시, 실제 테이블 구조 반영)';

CREATE TABLE IF NOT EXISTS ictyb_mw_lgin_tkn_info (
    RFSTKN_KEY VARCHAR(255) NOT NULL COMMENT '리프레시 토큰 키',
    FRST_REG_DT DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '최초 등록 일시',
    FRST_REGR_EMPNO VARCHAR(20) COMMENT '최초 등록자 사원번호',
    LST_CHG_DT DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 변경 일시',
    LST_CHGR_EMPNO VARCHAR(20) COMMENT '최종 변경자 사원번호',
    USER_EMPNO VARCHAR(20) COMMENT '사용자 사원번호',
    EXP_YMD DATE COMMENT '만료 일자',
    PRIMARY KEY (RFSTKN_KEY)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='로그인 토큰 정보';

-- 2. 자료실/공지사항 테이블
CREATE TABLE IF NOT EXISTS its_notice (
    NOTICE_NO INT AUTO_INCREMENT PRIMARY KEY COMMENT '문서번호 (PK)',
    NOTICE_TITLE VARCHAR(1000) COMMENT '제목',
    NOTICE_DEP_CD VARCHAR(16) COMMENT '부서코드',
    NOTICE_CONTENTS VARCHAR(4000) COMMENT '내용',
    PRIORITY INT DEFAULT 0 COMMENT '정렬 우선순위',
    REG_USER_SABUN VARCHAR(10) COMMENT '등록자 사번',
    REG_USER_DEP_CD VARCHAR(16) COMMENT '등록자 부서코드',
    REG_USER_NAME VARCHAR(12) COMMENT '등록자 성명',
    REG_DT VARCHAR(14) COMMENT '등록일시',
    END_DT VARCHAR(14) COMMENT '게시 종료일',
    DEL_YN VARCHAR(1) DEFAULT 'N' COMMENT '삭제여부',
    VIEW_CNT INT DEFAULT 0 COMMENT '조회수',
    NOTICE_TYPE VARCHAR(1) COMMENT '자료실(D)/공지사항(N)/QA(Q) 구분'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='자료실 및 공지사항';

CREATE TABLE IF NOT EXISTS its_notice_attach (
    NOTICE_NO INT COMMENT '문서번호 (PK)',
    SEQ VARCHAR(3) COMMENT '순번 (PK)',
    REAL_FILE_NAME VARCHAR(1000) COMMENT '실제 파일명',
    FILE_NAME VARCHAR(1000) COMMENT '변환 파일명',
    FILE_LOCATION VARCHAR(1000) COMMENT '파일 위치 경로',
    REG_DT VARCHAR(14) COMMENT '등록일시',
    ATTACH_TYPE VARCHAR(1) COMMENT '파일 타입',
    FILE_SIZE VARCHAR(10) COMMENT '파일 크기',
    PRIMARY KEY (NOTICE_NO, SEQ)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. IT 작업 보고 테이블 (지시서_IT부서)
CREATE TABLE IF NOT EXISTS its_it_work_report (
    INST_ID VARCHAR(14) NOT NULL COMMENT '처리번호',
    REQ_ID VARCHAR(14) NOT NULL COMMENT '요청번호',
    CHANGE_TITLE VARCHAR(1000) COMMENT '지시제목',
    CHANGE_REASON VARCHAR(4000) COMMENT '지시내용',
    SERVICE_TYPE VARCHAR(2) COMMENT '서비스유형',
    SYSTEM_CD VARCHAR(6) COMMENT '시스템코드',
    WORK_GUBUN VARCHAR(2) COMMENT '작업구분',
    EXPT_FP_CNT_EI DECIMAL(10,0) COMMENT '예상기능_EI',
    EXPT_FP_CNT_EO DECIMAL(10,0) COMMENT '예상기능_EO',
    EXPT_FP_CNT_EQ DECIMAL(10,0) COMMENT '예상기능_EQ',
    EXPT_FP_CNT_ILF DECIMAL(10,0) COMMENT '예상기능_ILF',
    EXPT_FP_CNT_EIF DECIMAL(10,0) COMMENT '예상기능_EIF',
    WORK_TYPE VARCHAR(2) COMMENT '작업유형',
    WORK_LEVEL VARCHAR(2) COMMENT '작업레벨',
    WORK_PERIOD_MANUAL VARCHAR(5) COMMENT '작업기간메뉴얼',
    WORK_PERIOD VARCHAR(5) COMMENT '작업기간',
    WORK_CHANGE_PERIOD VARCHAR(5) COMMENT '변경기간',
    WORK_CHANGE_PERIOD_CMT VARCHAR(4000) COMMENT '변경기간_사유',
    EXPECTED_FINISHED_DT VARCHAR(14) COMMENT '완료예정일',
    WORK_CODE VARCHAR(3) COMMENT '형상연계용작업코드',
    REQUEST_CODE VARCHAR(3) COMMENT '형상연계용요청코드',
    WORK_START_DT VARCHAR(14) COMMENT '지시서시작일',
    WORK_END_DT VARCHAR(14) COMMENT '지시서종료일',
    REPORT_DT VARCHAR(14) COMMENT '작업보고일',
    IS_SUCCESS VARCHAR(1) COMMENT '합격여부',
    SATISFY_POINT VARCHAR(3) COMMENT '만족도',
    SATISFY_CNT VARCHAR(4000) COMMENT '만족도의견',
    ACT_ID VARCHAR(3) COMMENT '진행단계',
    APPROVE1_SABUN VARCHAR(10) COMMENT '1차결재자사번',
    APPROVE1_NAME VARCHAR(12) COMMENT '1차결재자이름',
    APPROVE1_DT VARCHAR(14) COMMENT '1차결재일',
    APPROVE2_SABUN VARCHAR(10) COMMENT '2차결재자사번',
    APPROVE2_NAME VARCHAR(12) COMMENT '2차결재자이름',
    APPROVE2_DT VARCHAR(14) COMMENT '2차결재일',
    APPROVE3_SABUN VARCHAR(10) COMMENT '3차결재자사번',
    APPROVE3_NAME VARCHAR(12) COMMENT '3차결재자이름',
    APPROVE3_DT VARCHAR(14) COMMENT '3차결재일',
    REG_USER_SABUN VARCHAR(10) COMMENT '등록자사번',
    REG_USER_NAME VARCHAR(12) COMMENT '등록자이름',
    REG_USER_DEP_CD VARCHAR(16) COMMENT '등록자부서코드',
    REG_USER_DEP_NM VARCHAR(500) COMMENT '등록자부서명',
    REG_DT VARCHAR(14) COMMENT '등록일',
    WORKER_SABUN VARCHAR(10) COMMENT '작업자ID',
    WORKER_NAME VARCHAR(30) COMMENT '작업자이름',
    WORKER_DEP_CD VARCHAR(8) COMMENT '작업부서',
    WORKER_WORK_CNT VARCHAR(3) COMMENT '작업자작업건수',
    WORKER_WORK_DAYS VARCHAR(5) COMMENT '작업자작업일수',
    ITMS_INST_ID VARCHAR(10) COMMENT 'ITMS처리번호',
    EXPT_FP_SUM DECIMAL(10,0) COMMENT '예상기능점수합계',
    CHANGE_ROOT VARCHAR(2000) COMMENT '변경사유',
    TEST_MODE_YN VARCHAR(1) COMMENT '테스트모드여부',
    USER_MANUAL_YN VARCHAR(1) COMMENT '사용자메뉴얼갱신여부',
    DEPLOY_DT VARCHAR(14) COMMENT '배포일',
    OPPB_YN VARCHAR(1) COMMENT '공개여부',
    DRS_IMPT_YN VARCHAR(1) COMMENT '재해복구시스템영향여부',
    PRIMARY KEY (INST_ID, REQ_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='지시서_IT부서';

CREATE TABLE IF NOT EXISTS its_it_work_report_attach (
    INST_ID VARCHAR(14) NOT NULL COMMENT '처리번호',
    SEQ VARCHAR(3) NOT NULL COMMENT '순번',
    REAL_FILE_NAME VARCHAR(1000) COMMENT '실제파일명',
    FILE_NAME VARCHAR(1000) COMMENT '변환파일명',
    FILE_LOCATION VARCHAR(1000) COMMENT '파일위치',
    REG_DT VARCHAR(14) COMMENT '등록일',
    ATTACH_TYPE VARCHAR(2) COMMENT '첨부파일유형',
    FILE_SIZE VARCHAR(10) COMMENT '파일크기',
    PRIMARY KEY (INST_ID, SEQ)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='지시서_IT부서_첨부';

-- 4-1. 현재결재자정보 테이블 (its_n_sign)
-- NSignVo 엔티티가 이미 참조하는 기존 테이블. sql.init.mode=always 로 schema.sql이 Hibernate
-- ddl-auto보다 먼저 실행되므로, 결재 기능 시드 데이터를 넣기 위해 명시적으로 선언해둔다.
CREATE TABLE IF NOT EXISTS its_n_sign (
    INST_ID VARCHAR(14) NOT NULL COMMENT '처리번호',
    SABUN VARCHAR(10) NOT NULL COMMENT '사번',
    ACT_ID VARCHAR(3) COMMENT '진행단계(대기중인 결재 코드)',
    NAME VARCHAR(10) COMMENT '이름',
    PRIMARY KEY (INST_ID, SABUN)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='현재결재자정보';

-- 5. 결재 이력 테이블
CREATE TABLE IF NOT EXISTS its_work_history (
    INST_ID VARCHAR(14) NOT NULL COMMENT '처리번호',
    SEQ VARCHAR(3) NOT NULL COMMENT '순번',
    REG_SABUN VARCHAR(8) COMMENT '결재자사번',
    ACT_ID VARCHAR(3) COMMENT '작업ID',
    ACT_ID_NM VARCHAR(200) COMMENT '작업한글명',
    ACT_SIGN VARCHAR(1) COMMENT '유형코드',
    REG_NAME VARCHAR(12) COMMENT '결재자이름',
    REG_DT VARCHAR(14) COMMENT '결재일',
    REG_CNTNT VARCHAR(4000) COMMENT '결재의견',
    PRIMARY KEY (INST_ID, SEQ)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='결재이력';

-- 6. 업무지시서(ALL) 협의 상태
-- ACT_ID 기반 진행단계와 별개로, 담당 부서 간 협의가 필요한 건을 수동으로 표시하기 위한 테이블
CREATE TABLE IF NOT EXISTS ictyb_work_negotiation (
    INST_ID VARCHAR(14) NOT NULL COMMENT '처리번호 (its_it_work_report.INST_ID)',
    NEGOTIATION_YN VARCHAR(1) NOT NULL DEFAULT 'Y' COMMENT '협의여부',
    REG_SABUN VARCHAR(10) COMMENT '등록자사번',
    REG_NAME VARCHAR(12) COMMENT '등록자이름',
    REG_DT VARCHAR(14) COMMENT '등록일시',
    PRIMARY KEY (INST_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='업무지시서 협의 상태';

-- 7. 영업 점검일지 테이블
-- REPORT_ID/PART_ID는 ictyb_sales_daily_report / ybict_part_info를 가리키는 값이지만
-- 프로젝트 컨벤션상 FK 제약은 두지 않고 조회 시점에 키 컬럼으로만 조인한다.
CREATE TABLE IF NOT EXISTS ictyb_sales_daily_report (
    REPORT_ID BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '점검일지 ID',
    REPORT_DATE DATE COMMENT '작성일',
    AUTHOR_SABUN VARCHAR(20) COMMENT '작성자 사번',
    AUTHOR_NAME VARCHAR(50) COMMENT '작성자명',
    FRST_REG_DT DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '최초 등록 일시',
    LST_CHG_DT DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 변경 일시'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='영업 점검일지 헤더';

CREATE TABLE IF NOT EXISTS ictyb_sales_daily_report_part (
    REPORT_ID BIGINT NOT NULL COMMENT '점검일지 ID (ictyb_sales_daily_report.REPORT_ID)',
    PART_ID VARCHAR(20) NOT NULL COMMENT '파트ID (ybict_part_info.PART_ID)',
    PART_NM VARCHAR(100) COMMENT '파트명 스냅샷',
    EFFICIENCY_CONTENT VARCHAR(2000) COMMENT '유지보수 관리 효율화 및 주요 배포내용',
    MAIN_INSTRUCTION_CONTENT VARCHAR(2000) COMMENT '주요 작업지시 내용',
    WAS_ERROR_CONTENT VARCHAR(2000) COMMENT 'WAS 오류 내역',
    MEETING_SCHEDULE VARCHAR(1000) COMMENT '회의예정',
    SPECIAL_NOTES VARCHAR(1000) COMMENT '특이사항',
    PRIMARY KEY (REPORT_ID, PART_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='영업 점검일지 파트별 작성 내용';

CREATE TABLE IF NOT EXISTS ictyb_sales_daily_report_person (
    PERSON_SEQ BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '인원 행 ID',
    REPORT_ID BIGINT NOT NULL COMMENT '점검일지 ID',
    PART_ID VARCHAR(20) COMMENT '파트ID',
    PERSON_NM VARCHAR(50) COMMENT '인원명',
    IN_PROGRESS_CNT INT DEFAULT 0 COMMENT '진행중 건수',
    DELAYED_CNT INT DEFAULT 0 COMMENT '일정지연 건수',
    DISTRIBUTED_CNT INT DEFAULT 0 COMMENT '배포건수'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='영업 점검일지 파트별 인원 진행현황 (등록 시점 스냅샷)';

CREATE TABLE IF NOT EXISTS ictyb_sales_daily_report_attach (
    REPORT_ID BIGINT NOT NULL COMMENT '점검일지 ID',
    PART_ID VARCHAR(20) NOT NULL COMMENT '파트ID',
    SEQ VARCHAR(3) NOT NULL COMMENT '첨부 순번',
    REAL_FILE_NAME VARCHAR(1000) COMMENT '실제파일명',
    FILE_NAME VARCHAR(1000) COMMENT '변환파일명',
    FILE_LOCATION VARCHAR(1000) COMMENT '파일위치',
    FILE_SIZE BIGINT COMMENT '파일크기(byte)',
    ATTACH_FULL_TYPE VARCHAR(10) COMMENT '확장자',
    REG_DT DATETIME COMMENT '등록일시',
    PRIMARY KEY (REPORT_ID, PART_ID, SEQ)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='영업 점검일지 파트별 첨부파일';


-- 업무 협의 (피드백) 테이블
CREATE TABLE IF NOT EXISTS ictyb_work_opinion (
    OPN_ID VARCHAR(20) NOT NULL COMMENT '협의ID',
    INSTR_NO VARCHAR(20) COMMENT '지시번호',
    OPN_TITLE VARCHAR(200) COMMENT '협의 제목',
    WRTR_EMPNO VARCHAR(10) COMMENT '작성자사번',
    WRTR_NM VARCHAR(60) COMMENT '작성자명',
    FRST_REGR_EMPNO VARCHAR(10) COMMENT '최초등록자사번',
    LST_CHGR_EMPNO VARCHAR(10) COMMENT '최종변경자사번',
    FRST_REG_DT DATETIME COMMENT '최초등록일시',
    LST_CHG_DT DATETIME COMMENT '최종변경일시',
    PRIMARY KEY (OPN_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='업무 협의 스레드';

CREATE TABLE IF NOT EXISTS ictyb_work_opinion_cmnt (
    CMNT_ID VARCHAR(20) NOT NULL COMMENT '댓글ID',
    OPN_ID VARCHAR(20) COMMENT '협의ID',
    CMNT_CTT VARCHAR(4000) COMMENT '댓글내용',
    WRTR_EMPNO VARCHAR(10) COMMENT '작성자사번',
    WRTR_NM VARCHAR(60) COMMENT '작성자명',
    WRTR_ROLE_NM VARCHAR(100) COMMENT '작성자역할명',
    FRST_REGR_EMPNO VARCHAR(10) COMMENT '최초등록자사번',
    LST_CHGR_EMPNO VARCHAR(10) COMMENT '최종변경자사번',
    FRST_REG_DT DATETIME COMMENT '최초등록일시',
    LST_CHG_DT DATETIME COMMENT '최종변경일시',
    PRIMARY KEY (CMNT_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='업무 협의 댓글';

CREATE TABLE IF NOT EXISTS ictyb_work_opinion_attach (
    CMNT_ID VARCHAR(20) NOT NULL COMMENT '댓글ID',
    SEQ_NO BIGINT NOT NULL COMMENT '시퀀스번호',
    RL_FILE_NM VARCHAR(255) COMMENT '실제파일명',
    FILE_NM VARCHAR(100) COMMENT '파일명',
    FILE_PTH_CTT VARCHAR(500) COMMENT '파일경로내용',
    FILE_CPCT BIGINT COMMENT '파일용량',
    FRST_REGR_EMPNO VARCHAR(10) COMMENT '최초등록자사번',
    LST_CHGR_EMPNO VARCHAR(10) COMMENT '최종변경자사번',
    FRST_REG_DT DATETIME COMMENT '최초등록일시',
    LST_CHG_DT DATETIME COMMENT '최종변경일시',
    PRIMARY KEY (CMNT_ID, SEQ_NO)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='업무 협의 댓글 첨부파일';


-- 3. 파트 및 사용자 정보
CREATE TABLE IF NOT EXISTS ybict_part_info (
    PAR_DEP_ID VARCHAR(20) NOT NULL COMMENT '상위부서ID',
    DEP_ID VARCHAR(20) NOT NULL COMMENT '부서ID',
    DEP_TITLE VARCHAR(100) COMMENT '부서명',
    PART_ID VARCHAR(20) NOT NULL COMMENT '파트ID',
    PART_NM VARCHAR(100) COMMENT '파트명',
    PART_ORDER INT DEFAULT 0 COMMENT '파트순번',
    PART_START_DT DATE COMMENT '파트시작일',
    PART_END_DT DATE COMMENT '파트종료일',
    USE_YN CHAR(1) DEFAULT 'Y' COMMENT '사용여부',
    PRIMARY KEY (DEP_ID, PART_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='영배 파트정보';

CREATE TABLE IF NOT EXISTS ybict_user_info (
    PART_ID VARCHAR(20) NOT NULL COMMENT '파트ID',
    PART_NM VARCHAR(100) COMMENT '파트명',
    EMPNO VARCHAR(20) NOT NULL COMMENT '사번',
    USER_NM VARCHAR(50) COMMENT '사용자명',
    BUJAN_YN CHAR(1) COMMENT '부서장여부',
    PARTLEADER_YN CHAR(1) COMMENT '파트장여부',
    USE_YN CHAR(1) DEFAULT 'Y' COMMENT '사용여부',
    PART_START_DT DATE COMMENT '파트시작일',
    PART_END_DT DATE COMMENT '파트종료일',
    PRIMARY KEY (PART_ID, EMPNO)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='영배 ICT 사용자정보';


-- 1. 결과보고_KDN 테이블 생성
CREATE TABLE IF NOT EXISTS `its_work_result` (
  `INST_ID` VARCHAR(14) NOT NULL COMMENT '처리번호',
  `REQ_ID` VARCHAR(14) NOT NULL COMMENT '요청번호',
  `RESULT` VARCHAR(4000) DEFAULT NULL COMMENT '작업결과',
  `WORKER_ID` VARCHAR(8) DEFAULT NULL COMMENT '작업자ID',
  `WORKER_NAME` VARCHAR(12) DEFAULT NULL COMMENT '작업자이름',
  `WORKER_DEP_NM` VARCHAR(1000) DEFAULT NULL COMMENT '작업자부서명',
  `WORKER_TEL` VARCHAR(20) DEFAULT NULL COMMENT '작업자전화',
  `REG_DT` VARCHAR(14) DEFAULT NULL COMMENT '등록일',
  `QRY_CTT` VARCHAR(4000) DEFAULT NULL COMMENT '쿼리내용',
  CONSTRAINT `PK_ITS_WORK_RESULT` PRIMARY KEY (`INST_ID`, `REQ_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='결과보고_KDN';


-- 2. 결과보고_KDN첨부 테이블 생성 (its_work_result의 자식 테이블)
CREATE TABLE IF NOT EXISTS `its_work_result_attach` (
  `INST_ID` VARCHAR(14) NOT NULL COMMENT '처리번호',
  `SEQ` VARCHAR(3) NOT NULL COMMENT '순번',
  `REQ_ID` VARCHAR(14) NOT NULL COMMENT '요청번호',
  `REAL_FILE_NAME` VARCHAR(1000) DEFAULT NULL COMMENT '실제파일명',
  `FILE_NAME` VARCHAR(1000) DEFAULT NULL COMMENT '변환파일명',
  `FILE_LOCATION` VARCHAR(1000) DEFAULT NULL COMMENT '파일위치',
  `REG_DT` VARCHAR(14) DEFAULT NULL COMMENT '등록일',
  `ITMSFLG` CHAR(1) DEFAULT NULL COMMENT 'ITMS구분값',
  `ATTACH_TYPE` VARCHAR(2) DEFAULT NULL COMMENT '첨부파일유형',
  `FILE_SIZE` VARCHAR(10) DEFAULT NULL COMMENT '파일크기',
  CONSTRAINT `PK_ITS_WORK_RESULT_ATTACH` PRIMARY KEY (`INST_ID`, `SEQ`, `REQ_ID`),
  -- 데이터 무결성을 위해 외래키(FK) 제약조건을 추가하시는 것을 권장합니다.
  CONSTRAINT `FK_its_work_result_TO_attach` FOREIGN KEY (`INST_ID`, `REQ_ID`) 
    REFERENCES `its_work_result` (`INST_ID`, `REQ_ID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='결과보고_KDN첨부';

-- 부서
INSERT IGNORE INTO its_kdn_dep VALUES ('9611175', '9811000', '영업시스템운영부', 'Y');
INSERT IGNORE INTO its_kdn_dep VALUES ('9611170', '9811000', '배전시스템운영부', 'Y');
INSERT IGNORE INTO its_kdn_dep VALUES ('9411400', '9811000', '영배시스템기술부', 'Y');
-- 처 레벨(영업시스템운영부/배전시스템운영부/영배시스템기술부 3개 부의 상위) 부서 레코드.
-- 지금까지는 ybict_part_info의 PAR_DEP_ID로만 참조되고 실제 부서 행은 없었는데,
-- 처장(강명희, 952452)의 로그인 시 부서 조회(KdnDepRepository.findByDepId)를 위해 추가(2026-07-08).
INSERT IGNORE INTO its_kdn_dep VALUES ('9811000', NULL, '영배사업처', 'Y');

-- 사용자
INSERT IGNORE INTO its_kdn_user (DEP_ID, LOGIN_ID, USER_ID, USER_NM, USER_PWD, EMAIL, CELL_PHONE, PWD_FAIL_COUNT, DEL_YN, POSITION_CODE, POSITION_NM, CP_AUTH_YN, CP_AUTH_DT)
VALUES ('9611175', '222142', '222142', '임미연', '222142', '222142@kdn.com', '010-0000-0000', 0, 'N', 'P001', '담당원', 'N', NULL);

INSERT IGNORE INTO its_kdn_user (DEP_ID, LOGIN_ID, USER_ID, USER_NM, USER_PWD, EMAIL, CELL_PHONE, PWD_FAIL_COUNT, DEL_YN, POSITION_CODE, POSITION_NM, CP_AUTH_YN, CP_AUTH_DT)
VALUES ('9611175', '252059', '252059', '윤도경', '252059', '252059@kdn.com', '010-0000-0000', 0, 'N', 'P001', '담당원', 'N', NULL);

-- 결재 기능 테스트용 로그인 계정 (임시, 사번=비밀번호): APR0001/APR0002 결재 체인 중 KDN 소속 3명.
INSERT IGNORE INTO its_kdn_user (DEP_ID, LOGIN_ID, USER_ID, USER_NM, USER_PWD, EMAIL, CELL_PHONE, PWD_FAIL_COUNT, DEL_YN, POSITION_CODE, POSITION_NM, CP_AUTH_YN, CP_AUTH_DT) VALUES
('9611175', '962332', '962332', '이인호', '962332', '962332@kdn.com', '010-0000-0000', 0, 'N', 'P004', '부장', 'N', NULL),
('9611175', '953352', '953352', '박은석', '953352', '953352@kdn.com', '010-0000-0000', 0, 'N', 'P003', '파트장', 'N', NULL),
('9611175', '212102', '212102', '하수연', '212102', '212102@kdn.com', '010-0000-0000', 0, 'N', 'P001', '담당원', 'N', NULL);

-- 영업시스템운영부 영업일반/수요/검침/요금청구/수금 파트 로그인 계정 (임시, 사번=비밀번호)
INSERT IGNORE INTO its_kdn_user (DEP_ID, LOGIN_ID, USER_ID, USER_NM, USER_PWD, EMAIL, CELL_PHONE, PWD_FAIL_COUNT, DEL_YN, POSITION_CODE, POSITION_NM, CP_AUTH_YN, CP_AUTH_DT) VALUES
('9611175', '072045', '072045', '김남인', '072045', '072045@kdn.com', '010-0000-0000', 0, 'N', 'P003', '파트장', 'N', NULL),
('9611175', '252015', '252015', '김문희', '252015', '252015@kdn.com', '010-0000-0000', 0, 'N', 'P001', '담당원', 'N', NULL),
('9611175', '232038', '232038', '전웅', '232038', '232038@kdn.com', '010-0000-0000', 0, 'N', 'P001', '담당원', 'N', NULL),
('9611175', '942075', '942075', '백세용', '942075', '942075@kdn.com', '010-0000-0000', 0, 'N', 'P003', '파트장', 'N', NULL),
('9611175', '252038', '252038', '박희천', '252038', '252038@kdn.com', '010-0000-0000', 0, 'N', 'P001', '담당원', 'N', NULL),
('9611175', '252087', '252087', '조윤재', '252087', '252087@kdn.com', '010-0000-0000', 0, 'N', 'P001', '담당원', 'N', NULL),
('9611175', '172101', '172101', '정동일', '172101', '172101@kdn.com', '010-0000-0000', 0, 'N', 'P003', '파트장', 'N', NULL),
('9611175', '222152', '222152', '공지현', '222152', '222152@kdn.com', '010-0000-0000', 0, 'N', 'P001', '담당원', 'N', NULL),
('9611175', '252028', '252028', '김채윤', '252028', '252028@kdn.com', '010-0000-0000', 0, 'N', 'P001', '담당원', 'N', NULL),
('9611175', '962155', '962155', '정은주', '962155', '962155@kdn.com', '010-0000-0000', 0, 'N', 'P003', '파트장', 'N', NULL),
('9611175', '202075', '202075', '이승준', '202075', '202075@kdn.com', '010-0000-0000', 0, 'N', 'P001', '담당원', 'N', NULL),
('9611175', '232031', '232031', '윤동욱', '232031', '232031@kdn.com', '010-0000-0000', 0, 'N', 'P001', '담당원', 'N', NULL),
('9611175', '962358', '962358', '최영인', '962358', '962358@kdn.com', '010-0000-0000', 0, 'N', 'P003', '파트장', 'N', NULL),
('9611175', '252102', '252102', '송태정', '252102', '252102@kdn.com', '010-0000-0000', 0, 'N', 'P001', '담당원', 'N', NULL);

-- 영업시스템운영부 신증설/한전ON 파트 로그인 계정 (임시, 사번=비밀번호)
-- ybict_user_info엔 있었지만 위 5개 파트 시드 블록에서 빠져 있어 로그인이 불가능했던 인원(2026-07-08 확인).
INSERT IGNORE INTO its_kdn_user (DEP_ID, LOGIN_ID, USER_ID, USER_NM, USER_PWD, EMAIL, CELL_PHONE, PWD_FAIL_COUNT, DEL_YN, POSITION_CODE, POSITION_NM, CP_AUTH_YN, CP_AUTH_DT) VALUES
('9611175', '972067', '972067', '정승환', '972067', '972067@kdn.com', '010-0000-0000', 0, 'N', 'P003', '파트장', 'N', NULL),
('9611175', '252039', '252039', '배효진', '252039', '252039@kdn.com', '010-0000-0000', 0, 'N', 'P001', '담당원', 'N', NULL),
('9611175', '202198', '202198', '박종연', '202198', '202198@kdn.com', '010-0000-0000', 0, 'N', 'P001', '담당원', 'N', NULL);

-- 처장(부장보다 한 단계 위 직급, 영업시스템운영부/배전시스템운영부/영배시스템기술부 3개 부를 총괄하는
-- 영배사업처(9811000) 소속). 다른 직급(P001 담당원/P003 파트장/P004 부장)과 구분되는 별도 코드 P005 사용.
INSERT IGNORE INTO its_kdn_user (DEP_ID, LOGIN_ID, USER_ID, USER_NM, USER_PWD, EMAIL, CELL_PHONE, PWD_FAIL_COUNT, DEL_YN, POSITION_CODE, POSITION_NM, CP_AUTH_YN, CP_AUTH_DT) VALUES
('9811000', '952452', '952452', '강명희', '952452', '952452@kdn.com', '010-0000-0000', 0, 'N', 'P005', '처장', 'N', NULL);

-- 한전(KEPCO) 조직도/인사정보 (임시)
-- 한전 사람은 SSO로 인증되어 비밀번호가 없다. 한전 사람은 its_kdn_user가 아닌
-- 이 테이블로 직접 로그인 처리하도록 JwtServiceImp에 별도 조회 경로를 추가했다(비밀번호 검증 없음).
--
-- 실제 조직도 원본(예시) 3건 — 한전측이 전달한 실제 테이블 컬럼 구조 확인용 참고 데이터.
INSERT IGNORE INTO ictyb_kepco_dep (OF_CD, OF_CODE, OF_CD1, OF_CD2, OF_CD3, OF_CD4, OF_HAN1, OF_HAN2, OF_HAN3, OF_HAN4, SER_GU, SER_1, SER_2, SER_3, SER_4, F_BONSA, F_JIKHAL, UPDATEDATE) VALUES
('450045304744844F', '844F', '4500', '4530', '4744', '844F', '충북본부', '충주지사', '고객지원부', '수금팀', '51', 'C07', 'A06', '001', '004', '4', 'N', '2026-07-03'),
('390039973998763M', '763M', '3900', '3997', '3998', '763M', '경기본부', '서수원지사', '고객지원부', '종합봉사팀', '51', 'C05', 'A12', '001', '002', '4', 'N', '2026-07-03'),
('400019997555814Y', '814Y', '4000', '1999', '7555', '814Y', '인천본부', '부평전력지사', '송전부', '송전팀', '51', 'C03', 'A14', '001', '001', '4', 'N', '2026-07-03');

-- 결재 기능 테스트용 로그인 계정 (임시, 비밀번호 없이 사번만으로 로그인). 위 참고 데이터에는
-- 포함되지 않은 인원이라 별도의 임시 소속코드(9999999999999900)를 붙여 관리한다.
INSERT IGNORE INTO ictyb_kepco_dep (OF_CD, OF_CODE, OF_CD1, OF_CD2, OF_CD3, OF_CD4, OF_HAN1, OF_HAN4, F_BONSA, F_JIKHAL) VALUES
('9999999999999900', '9900', '9999', '9999', '9999', '9900', 'KDN연계(임시)', '영업배전시스템실', '9', 'N');

INSERT IGNORE INTO ictyb_kepco_user (SABUN, NAME, SOSOK_HAN, SOSOK_CD, SOSOK_CD1, SOSOK_CD2, SOSOK_CD3, SOSOK_CD4) VALUES
('19109330', '배강우', '영업배전시스템실', '9999999999999900', '9999', '9999', '9999', '9900'),
('07104936', '정재균', '영업배전시스템실', '9999999999999900', '9999', '9999', '9999', '9900');

-- 영배 처장(영업배전시스템실 소속, 다른 인원과 구분되는 직급으로 JIKGUB/JIKGUB_HAN을 채운다.
-- 나머지 한전 인원은 JIKGUB_HAN이 비어 있어 자동으로 구분됨).
INSERT IGNORE INTO ictyb_kepco_user (SABUN, NAME, JIKGUB_HAN, SOSOK_HAN, SOSOK_CD, SOSOK_CD1, SOSOK_CD2, SOSOK_CD3, SOSOK_CD4, JIKGUB) VALUES
('97109061', '이명종', '처장', '영업배전시스템실', '9999999999999900', '9999', '9999', '9999', '9900', 'J005');

-- 부서의 파트
-- 영업시스템운영부 (9611175)
INSERT IGNORE INTO ybict_part_info VALUES ('9811000', '9611175', '영업시스템운영부', 'YY_0000', '영업총괄', 0, '2020-01-01', '9999-12-31', 'Y');
INSERT IGNORE INTO ybict_part_info VALUES ('9811000', '9611175', '영업시스템운영부', 'YY_1000', '신증설', 1, '2020-01-01', '9999-12-31', 'Y');
INSERT IGNORE INTO ybict_part_info VALUES ('9811000', '9611175', '영업시스템운영부', 'YY_1001', '한전ON', 2, '2020-01-01', '9999-12-31', 'Y');
INSERT IGNORE INTO ybict_part_info VALUES ('9811000', '9611175', '영업시스템운영부', 'YY_1002', '영업일반', 3, '2020-01-01', '9999-12-31', 'Y');
INSERT IGNORE INTO ybict_part_info VALUES ('9811000', '9611175', '영업시스템운영부', 'YY_1003', '수요', 4, '2020-01-01', '9999-12-31', 'Y');
INSERT IGNORE INTO ybict_part_info VALUES ('9811000', '9611175', '영업시스템운영부', 'YY_1004', '검침', 5, '2020-01-01', '9999-12-31', 'Y');
INSERT IGNORE INTO ybict_part_info VALUES ('9811000', '9611175', '영업시스템운영부', 'YY_1005', '요금/청구', 6, '2020-01-01', '9999-12-31', 'Y');
INSERT IGNORE INTO ybict_part_info VALUES ('9811000', '9611175', '영업시스템운영부', 'YY_1006', '수금', 7, '2020-01-01', '9999-12-31', 'Y');

-- 배전시스템운영부 (9611170)
INSERT IGNORE INTO ybict_part_info VALUES ('9811000', '9611170', '배전시스템운영부', 'BJ_0000', '배전총괄', 0, '2020-01-01', '9999-12-31', 'Y');
INSERT IGNORE INTO ybict_part_info VALUES ('9811000', '9611170', '배전시스템운영부', 'BJ_1000', '배전1팀', 1, '2020-01-01', '9999-12-31', 'Y');
INSERT IGNORE INTO ybict_part_info VALUES ('9811000', '9611170', '배전시스템운영부', 'BJ_1001', '배전2팀', 2, '2020-01-01', '9999-12-31', 'Y');
INSERT IGNORE INTO ybict_part_info VALUES ('9811000', '9611170', '배전시스템운영부', 'BJ_1002', '배전3팀', 3, '2020-01-01', '9999-12-31', 'Y');

-- 영배시스템기술부 (9411400)
INSERT IGNORE INTO ybict_part_info VALUES ('9811000', '9411400', '영배시스템기술부', 'GS_0000', '기술총괄', 0, '2020-01-01', '9999-12-31', 'Y');
INSERT IGNORE INTO ybict_part_info VALUES ('9811000', '9411400', '영배시스템기술부', 'GS_1000', 'BPMS', 1, '2020-01-01', '9999-12-31', 'Y');
INSERT IGNORE INTO ybict_part_info VALUES ('9811000', '9411400', '영배시스템기술부', 'GS_1001', 'JOBpass', 2, '2020-01-01', '9999-12-31', 'Y');
INSERT IGNORE INTO ybict_part_info VALUES ('9811000', '9411400', '영배시스템기술부', 'GS_1002', '통계', 3, '2020-01-01', '9999-12-31', 'Y');

-- 사용자
INSERT IGNORE INTO ybict_user_info VALUES 
('YY_0000', '영업총괄', '962332', '이인호', 'Y', NULL, 'Y', '2020-01-01', '9999-12-31'),
('YY_1000', '신증설', '972067', '정승환', NULL, 'Y', 'Y', '2020-01-01', '9999-12-31'),
('YY_1000', '신증설', '252039', '배효진', NULL, NULL, 'Y', '2020-01-01', '9999-12-31'),
('YY_1001', '한전ON', '953352', '박은석', NULL, 'Y', 'Y', '2020-01-01', '9999-12-31'),
('YY_1001', '한전ON', '202198', '박종연', NULL, NULL, 'Y', '2020-01-01', '9999-12-31'),
('YY_1001', '한전ON', '222142', '임미연', NULL, NULL, 'Y', '2020-01-01', '9999-12-31'),
('YY_1001', '한전ON', '212102', '하수연', NULL, NULL, 'Y', '2020-01-01', '9999-12-31'),
-- 영업일반/수요/검침/요금청구/수금: 파트당 1명뿐이던 유일 멤버를 전부 파트장으로 잘못 표시했던 걸
-- 협업자가 지정한 실제 파트장 + 직원 구성으로 정정 (2026-07-07). 기존 사번(300001~300005)은
-- 아래 DELETE로 제거하고, 해당 인원은 새 사번으로 다시 등록된다(예: 김문희 300001→252015).
('YY_1002', '영업일반', '072045', '김남인', NULL, 'Y', 'Y', '2020-01-01', '9999-12-31'),
('YY_1002', '영업일반', '252015', '김문희', NULL, NULL, 'Y', '2020-01-01', '9999-12-31'),
('YY_1002', '영업일반', '232038', '전웅', NULL, NULL, 'Y', '2020-01-01', '9999-12-31'),
('YY_1003', '수요', '942075', '백세용', NULL, 'Y', 'Y', '2020-01-01', '9999-12-31'),
('YY_1003', '수요', '252038', '박희천', NULL, NULL, 'Y', '2020-01-01', '9999-12-31'),
('YY_1003', '수요', '252087', '조윤재', NULL, NULL, 'Y', '2020-01-01', '9999-12-31'),
('YY_1004', '검침', '172101', '정동일', NULL, 'Y', 'Y', '2020-01-01', '9999-12-31'),
('YY_1004', '검침', '222152', '공지현', NULL, NULL, 'Y', '2020-01-01', '9999-12-31'),
('YY_1004', '검침', '252028', '김채윤', NULL, NULL, 'Y', '2020-01-01', '9999-12-31'),
('YY_1005', '요금/청구', '962155', '정은주', NULL, 'Y', 'Y', '2020-01-01', '9999-12-31'),
('YY_1005', '요금/청구', '202075', '이승준', NULL, NULL, 'Y', '2020-01-01', '9999-12-31'),
('YY_1005', '요금/청구', '232031', '윤동욱', NULL, NULL, 'Y', '2020-01-01', '9999-12-31'),
('YY_1006', '수금', '962358', '최영인', NULL, 'Y', 'Y', '2020-01-01', '9999-12-31'),
('YY_1006', '수금', '252102', '송태정', NULL, NULL, 'Y', '2020-01-01', '9999-12-31'),
('BJ_1000', '배전1팀', '300006', '이규호', NULL, 'Y', 'Y', '2020-01-01', '9999-12-31'),
('BJ_1001', '배전2팀', '300007', '신희만', NULL, 'Y', 'Y', '2020-01-01', '9999-12-31'),
('BJ_1002', '배전3팀', '300008', '김정태', NULL, 'Y', 'Y', '2020-01-01', '9999-12-31'),
('GS_1000', 'BPMS', '300009', '강진구', NULL, 'Y', 'Y', '2020-01-01', '9999-12-31'),
('GS_1001', 'JOBpass', '300010', '윤승지', NULL, 'Y', 'Y', '2020-01-01', '9999-12-31'),
('GS_1002', '통계', '300011', '허희원', NULL, 'Y', 'Y', '2020-01-01', '9999-12-31');

-- 배전/기술 부서장(BUJAN_YN='Y')이 아직 한 명도 없어, 지시서 접수(107) 단계에서 대상 부서
-- 부장 후보를 영업/배전/기술 전체로 넓히면 배전·기술은 후보가 비어버리는 문제가 있었다.
-- 영업총괄(YY_0000/이인호)과 같은 패턴으로 배전총괄(BJ_0000)/기술총괄(GS_0000)에 부장을 배정한다.
INSERT IGNORE INTO ybict_user_info VALUES
('BJ_0000', '배전총괄', '300012', '박승철', 'Y', NULL, 'Y', '2020-01-01', '9999-12-31'),
('GS_0000', '기술총괄', '300013', '이정훈', 'Y', NULL, 'Y', '2020-01-01', '9999-12-31');

-- 영업일반/수요/검침/요금청구/수금의 옛 사번(300001~300005, 파트당 1명뿐이라 전부 파트장으로
-- 잘못 표시됐던 자리)은 이미 시드된 환경에서 제거해야 위의 새 정정 데이터와 중복/충돌하지 않는다.
DELETE FROM ybict_user_info WHERE EMPNO IN ('300001', '300002', '300003', '300004', '300005');

-- 위 11명(배전/기술 6명은 아직 정정 전, 영업 5명은 위 DELETE로 대체됨)의 이름은 협업자가 지정한
-- 실제 담당자로 갱신될 수 있어, 이미 시드된 환경에도 반영되도록 보정한다
UPDATE ybict_user_info SET USER_NM = '이규호' WHERE EMPNO = '300006';
UPDATE ybict_user_info SET USER_NM = '신희만' WHERE EMPNO = '300007';
UPDATE ybict_user_info SET USER_NM = '김정태' WHERE EMPNO = '300008';
UPDATE ybict_user_info SET USER_NM = '강진구' WHERE EMPNO = '300009';
UPDATE ybict_user_info SET USER_NM = '윤승지' WHERE EMPNO = '300010';
UPDATE ybict_user_info SET USER_NM = '허희원' WHERE EMPNO = '300011';

-- 작업지시서
INSERT IGNORE INTO its_it_work_report (
    INST_ID, REQ_ID, CHANGE_TITLE, CHANGE_REASON, SERVICE_TYPE, SYSTEM_CD, WORK_GUBUN, 
    EXPT_FP_CNT_EI, EXPT_FP_CNT_EO, EXPT_FP_CNT_EQ, EXPT_FP_CNT_ILF, EXPT_FP_CNT_EIF,
    WORK_TYPE, WORK_LEVEL, WORK_PERIOD_MANUAL, WORK_PERIOD, WORK_CHANGE_PERIOD, 
    WORK_CHANGE_PERIOD_CMT, EXPECTED_FINISHED_DT, WORK_CODE, REQUEST_CODE, WORK_START_DT, 
    WORK_END_DT, REPORT_DT, IS_SUCCESS, SATISFY_POINT, SATISFY_CNT, ACT_ID, 
    APPROVE1_SABUN, APPROVE1_NAME, APPROVE1_DT, APPROVE2_SABUN, APPROVE2_NAME, APPROVE2_DT, 
    APPROVE3_SABUN, APPROVE3_NAME, APPROVE3_DT, REG_USER_SABUN, REG_USER_NAME, 
    REG_USER_DEP_CD, REG_USER_DEP_NM, REG_DT, WORKER_SABUN, WORKER_NAME, WORKER_DEP_CD, 
    WORKER_WORK_CNT, WORKER_WORK_DAYS, ITMS_INST_ID, EXPT_FP_SUM, CHANGE_ROOT, 
    TEST_MODE_YN, USER_MANUAL_YN, DEPLOY_DT, OPPB_YN, DRS_IMPT_YN
) VALUES 
('3738394', '3737563', '[한전ON] 한전온 월별 회원수 현황 및 요금조회건수 추출 요청1', '○ 추출기간 : 2026년 1월 31일, 2월 28일, 3월 20일 기준...', '02', '075822', '12', 0, 0, 0, 0, 0, '01', '하', NULL, '3', '29', NULL, '20260430235959', NULL, NULL, '20260323150639', '20260408164145', '20260408160048', NULL, NULL, NULL, '800', '19109330', '배강우', '20260323150639', NULL, NULL, NULL, '07104936', '정재균', '20260323163534', '19109330', '배강우', '70ZR71AF', '영업시스템부', '20260323150639', '212102', '하수연', '9611175', '7', '53', NULL, '0', NULL, NULL, NULL, 'Y', NULL, NULL),
('3738395', '3737564', '[한전ON] 한전온 월별 회원수 현황 및 요금조회건수 추출 요청2', '○ 추출기간 : 2026년 1월 31일, 2월 28일, 3월 20일 기준...', '02', '075822', '12', 0, 0, 0, 0, 0, '01', '하', NULL, '3', '29', NULL, '20260430235959', NULL, NULL, '20260323150639', '20260408164145', '20260408160048', NULL, NULL, NULL, '800', '19109330', '배강우', '20260323150639', NULL, NULL, NULL, '07104936', '정재균', '20260323163534', '19109330', '배강우', '70ZR71AF', '영업시스템부', '20260323150639', '212102', '하수연', '9611175', '7', '53', NULL, '0', NULL, NULL, NULL, 'Y', NULL, NULL),
('3738396', '3737565', '[한전ON] 한전온 월별 회원수 현황 및 요금조회건수 추출 요청3', '○ 추출기간 : 2026년 1월 31일, 2월 28일, 3월 20일 기준...', '02', '075822', '12', 0, 0, 0, 0, 0, '01', '하', NULL, '3', '29', NULL, '20260430235959', NULL, NULL, '20260323150639', '20260408164145', '20260408160048', NULL, NULL, NULL, '800', '19109330', '배강우', '20260323150639', NULL, NULL, NULL, '07104936', '정재균', '20260323163534', '19109330', '배강우', '70ZR71AF', '영업시스템부', '20260323150639', '212102', '하수연', '9611175', '7', '53', NULL, '0', NULL, NULL, NULL, 'Y', NULL, NULL),
('3738397', '3737566', '[한전ON] 한전온 월별 회원수 현황 및 요금조회건수 추출 요청4', '○ 추출기간 : 2026년 1월 31일, 2월 28일, 3월 20일 기준...', '02', '075822', '12', 0, 0, 0, 0, 0, '01', '하', NULL, '3', '29', NULL, '20260430235959', NULL, NULL, '20260323150639', '20260408164145', '20260408160048', NULL, NULL, NULL, '800', '19109330', '배강우', '20260323150639', NULL, NULL, NULL, '07104936', '정재균', '20260323163534', '19109330', '배강우', '70ZR71AF', '영업시스템부', '20260323150639', '212102', '하수연', '9611175', '7', '53', NULL, '0', NULL, NULL, NULL, 'Y', NULL, NULL),
('3738398', '3737567', '[한전ON] 한전온 월별 회원수 현황 및 요금조회건수 추출 요청5', '○ 추출기간 : 2026년 1월 31일, 2월 28일, 3월 20일 기준...', '02', '075822', '12', 0, 0, 0, 0, 0, '01', '하', NULL, '3', '29', NULL, '20260430235959', NULL, NULL, '20260323150639', '20260408164145', '20260408160048', NULL, NULL, NULL, '800', '19109330', '배강우', '20260323150639', NULL, NULL, NULL, '07104936', '정재균', '20260323163534', '19109330', '배강우', '70ZR71AF', '영업시스템부', '20260323150639', '212102', '하수연', '9611175', '7', '53', NULL, '0', NULL, NULL, NULL, 'Y', NULL, NULL),
('3738399', '3737568', '[한전ON] 한전온 월별 회원수 현황 및 요금조회건수 추출 요청6', '○ 추출기간 : 2026년 1월 31일, 2월 28일, 3월 20일 기준...', '02', '075822', '12', 0, 0, 0, 0, 0, '01', '하', NULL, '3', '29', NULL, '20260430235959', NULL, NULL, '20260323150639', '20260408164145', '20260408160048', NULL, NULL, NULL, '800', '19109330', '배강우', '20260323150639', NULL, NULL, NULL, '07104936', '정재균', '20260323163534', '19109330', '배강우', '70ZR71AF', '영업시스템부', '20260323150639', '212102', '하수연', '9611175', '7', '53', NULL, '0', NULL, NULL, NULL, 'Y', NULL, NULL),
('3738400', '3737569', '[한전ON] 한전온 월별 회원수 현황 및 요금조회건수 추출 요청7', '○ 추출기간 : 2026년 1월 31일, 2월 28일, 3월 20일 기준...', '02', '075822', '12', 0, 0, 0, 0, 0, '01', '하', NULL, '3', '29', NULL, '20260430235959', NULL, NULL, '20260323150639', '20260408164145', '20260408160048', NULL, NULL, NULL, '108', '19109330', '배강우', '20260323150639', NULL, NULL, NULL, '07104936', '정재균', '20260323163534', '19109330', '배강우', '70ZR71AF', '영업시스템부', '20260323150639', '212102', '하수연', '9611175', '7', '53', NULL, '0', NULL, NULL, NULL, 'Y', NULL, NULL),
('3738401', '3737570', '[한전ON] 한전온 월별 회원수 현황 및 요금조회건수 추출 요청8', '○ 추출기간 : 2026년 1월 31일, 2월 28일, 3월 20일 기준...', '02', '075822', '12', 0, 0, 0, 0, 0, '01', '하', NULL, '3', '29', NULL, '20260430235959', NULL, NULL, '20260323150639', '20260408164145', '20260408160048', NULL, NULL, NULL, '109', '19109330', '배강우', '20260323150639', NULL, NULL, NULL, '07104936', '정재균', '20260323163534', '19109330', '배강우', '70ZR71AF', '영업시스템부', '20260323150639', '212102', '하수연', '9611175', '7', '53', NULL, '0', NULL, NULL, NULL, 'Y', NULL, NULL),
('3738402', '3737571', '[한전ON] 한전온 월별 회원수 현황 및 요금조회건수 추출 요청9', '○ 추출기간 : 2026년 1월 31일, 2월 28일, 3월 20일 기준...', '02', '075822', '12', 0, 0, 0, 0, 0, '01', '하', NULL, '3', '29', NULL, '20260430235959', NULL, NULL, '20260323150639', '20260408164145', '20260408160048', NULL, NULL, NULL, '111', '19109330', '배강우', '20260323150639', NULL, NULL, NULL, '07104936', '정재균', '20260323163534', '19109330', '배강우', '70ZR71AF', '영업시스템부', '20260323150639', '212102', '하수연', '9611175', '7', '53', NULL, '0', NULL, NULL, NULL, 'Y', NULL, NULL);

-- 이력내역
INSERT IGNORE INTO its_work_history (
    INST_ID, SEQ, ACT_ID, ACT_ID_NM, ACT_SIGN, REG_SABUN, REG_NAME, REG_DT, REG_CNTNT
) VALUES 
('3738394', '1', '104', '지시서 작성', 'S', '19109330', '배강우', '20260323150639', NULL),
('3738394', '2', '106', '지시서 승인', 'S', '07104936', '정재균', '20260323163534', NULL),
('3738394', '3', '108', '지시서 배부', 'C', '962332', '이인호', '20260323165454', '[회수]26-03-23 19:11:44 109/953352/박은석'),
('3738394', '4', '108', '지시서 배부', 'S', '962332', '이인호', '20260323191156', NULL),
('3738394', '5', '109', '작업결과 보고', 'S', '212102', '하수연', '20260408104607', NULL),
('3738394', '6', '111', '작업결과 승인', 'S', '953352', '박은석', '20260408160048', NULL),
('3738395', '1', '104', '지시서 작성', 'S', '19109330', '배강우', '20260323150639', NULL),
('3738395', '2', '106', '지시서 승인', 'S', '07104936', '정재균', '20260323163534', NULL),
('3738395', '3', '108', '지시서 배부', 'C', '962332', '이인호', '20260323165454', NULL),
('3738395', '4', '108', '지시서 배부', 'S', '962332', '이인호', '20260323191156', NULL),
('3738395', '5', '109', '작업결과 보고', 'S', '212102', '하수연', '20260408104607', NULL),
('3738395', '6', '111', '작업결과 승인', 'S', '953352', '박은석', '20260408160048', NULL),
('3738396', '1', '104', '지시서 작성', 'S', '19109330', '배강우', '20260323150639', NULL),
('3738396', '2', '106', '지시서 승인', 'S', '07104936', '정재균', '20260323163534', NULL),
('3738396', '3', '108', '지시서 배부', 'C', '962332', '이인호', '20260323165454', NULL),
('3738396', '4', '108', '지시서 배부', 'S', '962332', '이인호', '20260323191156', NULL),
('3738396', '5', '109', '작업결과 보고', 'S', '212102', '하수연', '20260408104607', NULL),
('3738396', '6', '111', '작업결과 승인', 'S', '953352', '박은석', '20260408160048', NULL),
('3738397', '1', '104', '지시서 작성', 'S', '19109330', '배강우', '20260323150639', NULL),
('3738397', '2', '106', '지시서 승인', 'S', '07104936', '정재균', '20260323163534', NULL),
('3738397', '3', '108', '지시서 배부', 'C', '962332', '이인호', '20260323165454', NULL),
('3738397', '4', '108', '지시서 배부', 'S', '962332', '이인호', '20260323191156', NULL),
('3738397', '5', '109', '작업결과 보고', 'S', '212102', '하수연', '20260408104607', NULL),
('3738397', '6', '111', '작업결과 승인', 'S', '953352', '박은석', '20260408160048', NULL),
('3738397', '7', '114', '조치결과 승인', 'S', '19109330', '배강우', '20260408164143', NULL),
('3738398', '1', '104', '지시서 작성', 'S', '19109330', '배강우', '20260323150639', NULL),
('3738398', '2', '106', '지시서 승인', 'S', '07104936', '정재균', '20260323163534', NULL),
('3738398', '3', '108', '지시서 배부', 'C', '962332', '이인호', '20260323165454', NULL),
('3738398', '4', '108', '지시서 배부', 'S', '962332', '이인호', '20260323191156', NULL),
('3738398', '5', '109', '작업결과 보고', 'S', '212102', '하수연', '20260408104607', NULL),
('3738398', '6', '111', '작업결과 승인', 'S', '953352', '박은석', '20260408160048', NULL),
('3738398', '7', '114', '조치결과 승인', 'S', '19109330', '배강우', '20260408164143', NULL),
('3738399', '1', '104', '지시서 작성', 'S', '19109330', '배강우', '20260323150639', NULL),
('3738399', '2', '106', '지시서 승인', 'S', '07104936', '정재균', '20260323163534', NULL),
('3738399', '3', '108', '지시서 배부', 'C', '962332', '이인호', '20260323165454', NULL),
('3738399', '4', '108', '지시서 배부', 'S', '962332', '이인호', '20260323191156', NULL),
('3738399', '5', '109', '작업결과 보고', 'S', '212102', '하수연', '20260408104607', NULL),
('3738399', '6', '111', '작업결과 승인', 'S', '953352', '박은석', '20260408160048', NULL),
('3738400', '1', '104', '지시서 작성', 'S', '19109330', '배강우', '20260323150639', NULL),
('3738400', '2', '106', '지시서 승인', 'S', '07104936', '정재균', '20260323163534', NULL),
('3738400', '3', '108', '지시서 배부', 'C', '962332', '이인호', '20260323165454', NULL),
('3738400', '4', '108', '지시서 배부', 'S', '962332', '이인호', '20260323191156', NULL),
('3738400', '5', '109', '작업결과 보고', 'S', '212102', '하수연', '20260408104607', NULL),
('3738400', '6', '111', '작업결과 승인', 'S', '953352', '박은석', '20260408160048', NULL),
('3738401', '1', '104', '지시서 작성', 'S', '19109330', '배강우', '20260323150639', NULL),
('3738401', '2', '106', '지시서 승인', 'S', '07104936', '정재균', '20260323163534', NULL),
('3738401', '3', '108', '지시서 배부', 'C', '962332', '이인호', '20260323165454', NULL),
('3738401', '4', '108', '지시서 배부', 'S', '962332', '이인호', '20260323191156', NULL),
('3738401', '5', '109', '작업결과 보고', 'S', '212102', '하수연', '20260408104607', NULL),
('3738401', '6', '111', '작업결과 승인', 'S', '953352', '박은석', '20260408160048', NULL),
('3738402', '1', '104', '지시서 작성', 'S', '19109330', '배강우', '20260323150639', NULL),
('3738402', '2', '106', '지시서 승인', 'S', '07104936', '정재균', '20260323163534', NULL),
('3738402', '3', '108', '지시서 배부', 'C', '962332', '이인호', '20260323165454', NULL),
('3738402', '4', '108', '지시서 배부', 'S', '962332', '이인호', '20260323191156', NULL),
('3738402', '5', '109', '작업결과 보고', 'S', '212102', '하수연', '20260408104607', NULL),
('3738402', '6', '111', '작업결과 승인', 'S', '953352', '박은석', '20260408160048', NULL);


-- ════════════════════════════════════════════════════════════════
-- 장기 미처리 (5건): EXPECTED_FINISHED_DT < 오늘(2026-06-25) - 3개월 = 2026-03-25 이전
-- ACT_ID가 800이 아닌 미완료 건
-- ════════════════════════════════════════════════════════════════
INSERT IGNORE INTO its_it_work_report (
    INST_ID, REQ_ID, CHANGE_TITLE,
    WORK_START_DT, EXPECTED_FINISHED_DT,
    ACT_ID,
    WORKER_SABUN, WORKER_NAME, WORKER_DEP_CD,
    REG_USER_SABUN, REG_USER_NAME, REG_DT,
    SERVICE_TYPE, WORK_TYPE, WORK_LEVEL
) VALUES
-- ① 영업 / YY_1001 한전ON / WORKER_SABUN 있음 (212102 하수연)
('LNG0001', 'LNG0001', '[장기] 한전ON 회원 통합 인증 시스템 개편',
 '20251001090000', '20260101235959', '108',
 '212102', '하수연', '9611175',
 '19109330', '배강우', '20251001090000', '01', '01', '상'),

-- ② 영업 / YY_1000 신증설 / WORKER_SABUN 있음 (972067 정승환)
('LNG0002', 'LNG0002', '[장기] 신증설 계약 전자문서 자동화',
 '20251101090000', '20260115235959', '109',
 '972067', '정승환', '9611175',
 '19109330', '배강우', '20251101090000', '01', '01', '중'),

-- ③ 기술 / GS_1000 BPMS / WORKER_SABUN 없음 → history에서 953352 박은석
('LNG0003', 'LNG0003', '[장기] BPMS 배치 스케줄러 장애 처리',
 '20251201090000', '20260201235959', '111',
 NULL, NULL, NULL,
 '19109330', '배강우', '20251201090000', '02', '02', '상'),

-- ④ 기술 / GS_1002 통계 / WORKER_SABUN 있음 (212102 하수연)
('LNG0004', 'LNG0004', '[장기] 월별 통계 리포트 자동화 기능 추가',
 '20260101090000', '20260210235959', '108',
 '212102', '하수연', '9611175',
 '19109330', '배강우', '20260101090000', '01', '01', '중'),

-- ⑤ 배전 / BJ_1000 배전1팀 / WORKER_SABUN 없음 → history에서 972067 정승환
('LNG0005', 'LNG0005', '[장기] 배전 GIS 연동 API 장애 복구',
 '20251015090000', '20260120235959', '109',
 NULL, NULL, NULL,
 '19109330', '배강우', '20251015090000', '02', '01', '하');


-- ════════════════════════════════════════════════════════════════
-- 장기 미처리용 its_work_history (WORKER_SABUN 없는 케이스 ③⑤)
-- ════════════════════════════════════════════════════════════════
INSERT IGNORE INTO its_work_history (
    INST_ID, SEQ, ACT_ID, ACT_ID_NM, ACT_SIGN, REG_SABUN, REG_NAME, REG_DT
) VALUES
-- LNG0003: 최신 6자리 사번 → 953352 박은석 (GS_1000)
('LNG0003', '1', '104', '지시서 작성',   'S', '19109330', '배강우', '20251201090000'),
('LNG0003', '2', '106', '지시서 승인',   'S', '07104936', '정재균', '20251201120000'),
('LNG0003', '3', '111', '작업결과 승인', 'S', '953352',   '박은석', '20251202090000'),

-- LNG0005: 최신 6자리 사번 → 972067 정승환 (YY_1000 → BJ 매칭 안될 수 있어 212102로 변경)
('LNG0005', '1', '104', '지시서 작성',   'S', '19109330', '배강우', '20251015090000'),
('LNG0005', '2', '108', '지시서 배부',   'S', '07104936', '정재균', '20251016090000'),
('LNG0005', '3', '109', '작업결과 보고', 'S', '212102',   '하수연', '20251017090000');


-- ════════════════════════════════════════════════════════════════
-- 마감 임박 (5건): EXPECTED_FINISHED_DT가 오늘(2026-06-25)로부터 3일 이내
-- 즉 2026-06-25 ~ 2026-06-28 사이, ACT_ID가 800이 아닌 미완료 건
-- ════════════════════════════════════════════════════════════════
INSERT IGNORE INTO its_it_work_report (
    INST_ID, REQ_ID, CHANGE_TITLE,
    WORK_START_DT, EXPECTED_FINISHED_DT,
    ACT_ID,
    WORKER_SABUN, WORKER_NAME, WORKER_DEP_CD,
    REG_USER_SABUN, REG_USER_NAME, REG_DT,
    SERVICE_TYPE, WORK_TYPE, WORK_LEVEL
) VALUES
-- ① 영업 / YY_1001 한전ON / WORKER_SABUN 있음 (212102 하수연) / D-0
('DUE0001', 'DUE0001', '[마감임박] 한전ON 앱 푸시 알림 기능 개선',
 '20260601090000', '20260625235959', '108',
 '212102', '하수연', '9611175',
 '19109330', '배강우', '20260601090000', '01', '01', '중'),

-- ② 영업 / YY_1000 신중설 / WORKER_SABUN 있음 (972067 정승환) / D-1
('DUE0002', 'DUE0002', '[마감임박] 신증설 신청 모바일 UI 개편',
 '20260610090000', '20260626235959', '109',
 '972067', '정승환', '9611175',
 '19109330', '배강우', '20260610090000', '01', '01', '하'),

-- ③ 기술 / GS_1000 BPMS / WORKER_SABUN 없음 → history에서 953352 박은석 / D-2
('DUE0003', 'DUE0003', '[마감임박] BPMS 권한 관리 모듈 패치',
 '20260615090000', '20260627235959', '111',
 NULL, NULL, NULL,
 '19109330', '배강우', '20260615090000', '02', '02', '중'),

-- ④ 기술 / GS_1002 통계 / WORKER_SABUN 있음 (212102 하수연) / D-3
('DUE0004', 'DUE0004', '[마감임박] 연간 통계 대시보드 데이터 오류 수정',
 '20260618090000', '20260628235959', '108',
 '212102', '하수연', '9611175',
 '19109330', '배강우', '20260618090000', '01', '01', '하'),

-- ⑤ 배전 / BJ_1001 배전2팀 / WORKER_SABUN 없음 → history에서 212102 하수연 / D-1
('DUE0005', 'DUE0005', '[마감임박] 배전 설비 점검 이력 조회 기능 수정',
 '20260612090000', '20260626235959', '109',
 NULL, NULL, NULL,
 '19109330', '배강우', '20260612090000', '02', '01', '중');


-- ════════════════════════════════════════════════════════════════
-- 마감 임박용 its_work_history (WORKER_SABUN 없는 케이스 ③⑤)
-- ════════════════════════════════════════════════════════════════
INSERT IGNORE INTO its_work_history (
    INST_ID, SEQ, ACT_ID, ACT_ID_NM, ACT_SIGN, REG_SABUN, REG_NAME, REG_DT
) VALUES
-- DUE0003: 최신 6자리 사번 → 953352 박은석 (GS_1000)
('DUE0003', '1', '104', '지시서 작성',   'S', '19109330', '배강우', '20260615090000'),
('DUE0003', '2', '106', '지시서 승인',   'S', '07104936', '정재균', '20260615120000'),
('DUE0003', '3', '111', '작업결과 승인', 'S', '953352',   '박은석', '20260616090000'),

-- DUE0005: 최신 6자리 사번 → 212102 하수연 (YY_1001)
('DUE0005', '1', '104', '지시서 작성',   'S', '19109330', '배강우', '20260612090000'),
('DUE0005', '2', '106', '지시서 승인',   'S', '07104936', '정재균', '20260612120000'),
('DUE0005', '3', '109', '작업결과 보고', 'S', '212102',   '하수연', '20260613090000');


-- ════════════════════════════════════════════════════════════════
-- 검증
-- ════════════════════════════════════════════════════════════════

-- 장기 미처리 확인
SELECT '=== 장기 미처리 ===' AS check_type, INST_ID, ACT_ID,
    DATE(STR_TO_DATE(EXPECTED_FINISHED_DT, '%Y%m%d%H%i%s')) AS expected_date,
    DATEDIFF(CURDATE(), DATE(STR_TO_DATE(EXPECTED_FINISHED_DT, '%Y%m%d%H%i%s'))) AS overdue_days
FROM its_it_work_report
WHERE INST_ID LIKE 'LNG%'
UNION ALL
-- 마감 임박 확인
SELECT '=== 마감 임박 ===' AS check_type, INST_ID, ACT_ID,
    DATE(STR_TO_DATE(EXPECTED_FINISHED_DT, '%Y%m%d%H%i%s')) AS expected_date,
    DATEDIFF(DATE(STR_TO_DATE(EXPECTED_FINISHED_DT, '%Y%m%d%H%i%s')), CURDATE()) AS days_left
FROM its_it_work_report
WHERE INST_ID LIKE 'DUE%'
ORDER BY check_type, expected_date;

-- ════════════════════════════════════════════════════════════════
-- 파트별 샘플 (11건): 현재 작업지시서 데이터가 없는 파트(영업일반/수요/검침/요금·청구/수금,
-- 배전1·2·3팀, BPMS/JOBpass/통계)에 1건씩 추가 (담당자는 위에서 추가한 파트별 신규 사용자)
-- ════════════════════════════════════════════════════════════════
INSERT IGNORE INTO its_it_work_report (
    INST_ID, REQ_ID, CHANGE_TITLE,
    WORK_START_DT, EXPECTED_FINISHED_DT,
    ACT_ID,
    WORKER_SABUN, WORKER_NAME, WORKER_DEP_CD,
    REG_USER_SABUN, REG_USER_NAME, REG_DT,
    SERVICE_TYPE, WORK_TYPE, WORK_LEVEL
) VALUES
-- 영업 / 영업일반 / 300001 김문희
('PRT0001', 'PRT0001', '[영업일반] 영업 실적 현황 보고서 작성',
 '20260501090000', '20260530235959', '800',
 '300001', '김문희', '9611175',
 '19109330', '배강우', '20260501090000', '01', '01', '중'),

-- 영업 / 수요 / 300002 박희천
('PRT0002', 'PRT0002', '[수요] 전력 수요예측 데이터 점검',
 '20260601090000', '20260630235959', '109',
 '300002', '박희천', '9611175',
 '19109330', '배강우', '20260601090000', '01', '01', '중'),

-- 영업 / 검침 / 300003 김채윤
('PRT0003', 'PRT0003', '[검침] 원격검침 데이터 오류 점검',
 '20260610090000', '20260625235959', '108',
 '300003', '김채윤', '9611175',
 '19109330', '배강우', '20260610090000', '01', '02', '하'),

-- 영업 / 요금/청구 / 300004 정은주
('PRT0004', 'PRT0004', '[요금/청구] 요금 체계 개편 사전 검토',
 '20260615090000', '20260710235959', '107',
 '300004', '정은주', '9611175',
 '19109330', '배강우', '20260615090000', '01', '01', '중'),

-- 영업 / 수금 / 300005 송태정
('PRT0005', 'PRT0005', '[수금] 연체 분석 리포트 자동화',
 '20260301090000', '20260415235959', '111',
 '300005', '송태정', '9611175',
 '19109330', '배강우', '20260301090000', '01', '02', '상'),

-- 배전 / 배전1팀 / 300006 이규호
('PRT0006', 'PRT0006', '[배전1팀] 배전설비 점검 이력 데이터 정리',
 '20260205090000', '20260320235959', '800',
 '300006', '이규호', '9611170',
 '19109330', '배강우', '20260205090000', '02', '01', '중'),

-- 배전 / 배전2팀 / 300007 신희만
('PRT0007', 'PRT0007', '[배전2팀] 신규 배전 설비 등록 작업',
 '20260612090000', '20260628235959', '108',
 '300007', '신희만', '9611170',
 '19109330', '배강우', '20260612090000', '02', '01', '중'),

-- 배전 / 배전3팀 / 300008 김정태
('PRT0008', 'PRT0008', '[배전3팀] 배전 공사 진행 현황 통계',
 '20260618090000', '20260703235959', '107',
 '300008', '김정태', '9611170',
 '19109330', '배강우', '20260618090000', '02', '01', '하'),

-- 기술 / BPMS / 300009 강진구
('PRT0009', 'PRT0009', '[BPMS] BPMS 연계 모듈 안정화 작업',
 '20260115090000', '20260228235959', '800',
 '300009', '강진구', '9411400',
 '19109330', '배강우', '20260115090000', '02', '02', '상'),

-- 기술 / JOBpass / 300010 윤승지
('PRT0010', 'PRT0010', '[JOBpass] 야간 배치 작업 수행 결과 점검',
 '20260614090000', '20260630235959', '109',
 '300010', '윤승지', '9411400',
 '19109330', '배강우', '20260614090000', '02', '01', '중'),

-- 기술 / 통계 / 300011 허희원
('PRT0011', 'PRT0011', '[통계] 통계 리포트 자동화 개선',
 '20260619090000', '20260705235959', '107',
 '300011', '허희원', '9411400',
 '19109330', '배강우', '20260619090000', '02', '01', '중');

-- 위 11건의 WORKER_NAME도 협업자가 지정한 실제 담당자로 보정한다
-- PRT0001~5는 WORKER_SABUN도 함께 보정한다 (영업 5명의 사번이 300001~5→새 사번으로 바뀌었으므로)
UPDATE its_it_work_report SET WORKER_NAME = '김문희', WORKER_SABUN = '252015' WHERE INST_ID = 'PRT0001';
UPDATE its_it_work_report SET WORKER_NAME = '박희천', WORKER_SABUN = '252038' WHERE INST_ID = 'PRT0002';
UPDATE its_it_work_report SET WORKER_NAME = '김채윤', WORKER_SABUN = '252028' WHERE INST_ID = 'PRT0003';
UPDATE its_it_work_report SET WORKER_NAME = '정은주', WORKER_SABUN = '962155' WHERE INST_ID = 'PRT0004';
UPDATE its_it_work_report SET WORKER_NAME = '송태정', WORKER_SABUN = '252102' WHERE INST_ID = 'PRT0005';
UPDATE its_it_work_report SET WORKER_NAME = '이규호' WHERE INST_ID = 'PRT0006';
UPDATE its_it_work_report SET WORKER_NAME = '신희만' WHERE INST_ID = 'PRT0007';
UPDATE its_it_work_report SET WORKER_NAME = '김정태' WHERE INST_ID = 'PRT0008';
UPDATE its_it_work_report SET WORKER_NAME = '강진구' WHERE INST_ID = 'PRT0009';
UPDATE its_it_work_report SET WORKER_NAME = '윤승지' WHERE INST_ID = 'PRT0010';
UPDATE its_it_work_report SET WORKER_NAME = '허희원' WHERE INST_ID = 'PRT0011';

-- ════════════════════════════════════════════════════════════════
-- 파트별 샘플 추가 (3건): 영업 점검일지 자동 인원 집계에서 파트장/부서장을 제외하도록
-- 바꾼 뒤(2026-07-07), 신증설/요금·청구/영업일반은 실적이 파트장(정승환/정은주)이나
-- 완료건(김문희 PRT0001)에만 걸려 있어 대리급 인원이 0명으로 보이는 문제 확인.
-- 각 파트의 대리급(파트장/부서장 아닌) 인원 앞으로 진행중 건을 1건씩 추가한다.
-- ════════════════════════════════════════════════════════════════
INSERT IGNORE INTO its_it_work_report (
    INST_ID, REQ_ID, CHANGE_TITLE,
    WORK_START_DT, EXPECTED_FINISHED_DT,
    ACT_ID,
    WORKER_SABUN, WORKER_NAME, WORKER_DEP_CD,
    REG_USER_SABUN, REG_USER_NAME, REG_DT,
    SERVICE_TYPE, WORK_TYPE, WORK_LEVEL
) VALUES
-- 영업 / 신증설 / 252039 배효진 (대리급)
('PRT0012', 'PRT0012', '[신증설] 신증설 접수 시스템 화면 개선',
 '20260701090000', '20260731235959', '109',
 '252039', '배효진', '9611175',
 '19109330', '배강우', '20260701090000', '01', '01', '중'),

-- 영업 / 요금/청구 / 202075 이승준 (대리급)
('PRT0013', 'PRT0013', '[요금/청구] 요금 고지서 양식 오류 수정',
 '20260702090000', '20260725235959', '108',
 '202075', '이승준', '9611175',
 '19109330', '배강우', '20260702090000', '01', '02', '하'),

-- 영업 / 영업일반 / 232038 전웅 (대리급)
('PRT0014', 'PRT0014', '[영업일반] 영업 실적 집계 자동화 점검',
 '20260703090000', '20260720235959', '107',
 '232038', '전웅', '9611175',
 '19109330', '배강우', '20260703090000', '01', '01', '중');

-- ════════════════════════════════════════════════════════════════
-- 파트 매칭 오류 수정: 제목상 부서/파트와 실제 매칭 결과가 어긋나던 건들
-- (WORKER_SABUN이 비어 있어 its_work_history의 사번으로 대체 매칭되었는데,
--  그 사번이 한전ON 소속이라 제목과 무관하게 영업/한전ON으로 표시되던 문제)
-- ════════════════════════════════════════════════════════════════
-- BPMS 건 → 기술/BPMS (강진구)
UPDATE its_it_work_report SET WORKER_SABUN = '300009', WORKER_NAME = '강진구', WORKER_DEP_CD = '9411400' WHERE INST_ID IN ('DUE0003', 'LNG0003');
-- 통계 건 → 기술/통계 (허희원)
UPDATE its_it_work_report SET WORKER_SABUN = '300011', WORKER_NAME = '허희원', WORKER_DEP_CD = '9411400' WHERE INST_ID IN ('DUE0004', 'LNG0004');
-- 배전 설비/GIS 건 → 배전1/2/3팀에 임의 배정 (협업자 파트 확정 전까지 임시)
UPDATE its_it_work_report SET WORKER_SABUN = '300008', WORKER_NAME = '김정태', WORKER_DEP_CD = '9611170' WHERE INST_ID = 'DUE0005';
UPDATE its_it_work_report SET WORKER_SABUN = '300007', WORKER_NAME = '신희만', WORKER_DEP_CD = '9611170' WHERE INST_ID = 'LNG0005';


-- ════════════════════════════════════════════════════════════════
-- 협의 (3건): ACT_ID가 800(완료)이 아닌 건 중 일부를 수동 표시
-- (LNG0003은 위 수정으로 기술/BPMS 소속이 되어, 협의 상태가 영업 외 부서에서도 노출됨을 확인할 수 있다)
-- ════════════════════════════════════════════════════════════════
INSERT IGNORE INTO ictyb_work_negotiation (INST_ID, NEGOTIATION_YN, REG_SABUN, REG_NAME, REG_DT) VALUES
('3738400', 'Y', '19109330', '배강우', '20260612090000'),
('LNG0003', 'Y', '19109330', '배강우', '20260613090000'),
('DUE0005', 'Y', '19109330', '배강우', '20260614090000');


-- Q&A
INSERT IGNORE INTO its_notice (NOTICE_NO, NOTICE_TITLE, NOTICE_DEP_CD, NOTICE_CONTENTS, PRIORITY, REG_USER_SABUN, REG_USER_DEP_CD, REG_USER_NAME, REG_DT, END_DT, DEL_YN, VIEW_CNT, NOTICE_TYPE)
VALUES (1, '시스템 점검 안내', '9611175', '정기 점검으로 인한 서비스 일시 중단 안내입니다.', 1, '222142', '9611175', '테스터', '20260611090000', '20260612090000', 'N', 10, 'N');

INSERT IGNORE INTO its_notice (NOTICE_NO, NOTICE_TITLE, NOTICE_DEP_CD, NOTICE_CONTENTS, PRIORITY, REG_USER_SABUN, REG_USER_DEP_CD, REG_USER_NAME, REG_DT, END_DT, DEL_YN, VIEW_CNT, NOTICE_TYPE)
VALUES (2, '로그인 관련 문의', '9611175', '로그인 시 사번 입력 후 패스워드가 올바르지 않다고 나옵니다.', 0, '222142', '9611175', '테스터', '20260611100000', NULL, 'N', 5, 'Q');

INSERT IGNORE INTO its_notice (NOTICE_NO, NOTICE_TITLE, NOTICE_DEP_CD, NOTICE_CONTENTS, PRIORITY, REG_USER_SABUN, REG_USER_DEP_CD, REG_USER_NAME, REG_DT, END_DT, DEL_YN, VIEW_CNT, NOTICE_TYPE)
VALUES (3, '업무 매뉴얼 자료', '9611175', '업무 처리 매뉴얼 v1.0 파일 첨부합니다.', 2, '222142', '9611175', '테스터', '20260611110000', NULL, 'N', 25, 'D');

-- ════════════════════════════════════════════════════════════════
-- 업무 협의 시드 데이터 (ictyb_work_opinion / ictyb_work_opinion_cmnt)
-- INSTR_NO = INST_ID (its_it_work_report 기준)
-- ════════════════════════════════════════════════════════════════

-- 협의 스레드 (3738400 - 한전ON 7번 건: 협의 상태)
INSERT IGNORE INTO ictyb_work_opinion (OPN_ID, INSTR_NO, OPN_TITLE, WRTR_EMPNO, WRTR_NM, FRST_REGR_EMPNO, LST_CHGR_EMPNO, FRST_REG_DT, LST_CHG_DT) VALUES
('OPN20261101000001', '3738400', '정산 기준 및 부분 충전 처리 방식', '212102', '하수연', '212102', '212102', '2025-11-10 09:00:00', '2025-11-10 09:00:00'),
('OPN20261101000002', '3738400', 'UI 디자인 확정 요청', '212102', '하수연', '212102', '212102', '2025-11-15 14:00:00', '2025-11-15 14:00:00');

-- 협의 스레드 (LNG0003 - 협의 상태 건)
INSERT IGNORE INTO ictyb_work_opinion (OPN_ID, INSTR_NO, OPN_TITLE, WRTR_EMPNO, WRTR_NM, FRST_REGR_EMPNO, LST_CHGR_EMPNO, FRST_REG_DT, LST_CHG_DT) VALUES
('OPN20261101000003', 'LNG0003', 'API 인터페이스 명세 확인', '300007', '신희만', '300007', '300007', '2025-11-20 10:30:00', '2025-11-20 10:30:00');

-- 댓글 (OPN20261101000001 - 정산 기준)
INSERT IGNORE INTO ictyb_work_opinion_cmnt (CMNT_ID, OPN_ID, CMNT_CTT, WRTR_EMPNO, WRTR_NM, WRTR_ROLE_NM, FRST_REGR_EMPNO, LST_CHGR_EMPNO, FRST_REG_DT, LST_CHG_DT) VALUES
('CMT20261101000001', 'OPN20261101000001', '충전 완료 시점 요금인지 시작 시점인지 불명확합니다.', '212102', '하수연', '실무자', '212102', '212102', '2025-11-10 09:00:00', '2025-11-10 09:00:00'),
('CMT20261101000002', 'OPN20261101000001', '충전 완료 시점 기준이며 부분 충전은 kWh 비례 계산으로 처리해 주세요.', '07104936', '정재균', '한전 담당자', '07104936', '07104936', '2025-11-12 11:00:00', '2025-11-12 11:00:00');

-- 댓글 (OPN20261101000002 - UI 디자인)
INSERT IGNORE INTO ictyb_work_opinion_cmnt (CMNT_ID, OPN_ID, CMNT_CTT, WRTR_EMPNO, WRTR_NM, WRTR_ROLE_NM, FRST_REGR_EMPNO, LST_CHGR_EMPNO, FRST_REG_DT, LST_CHG_DT) VALUES
('CMT20261101000003', 'OPN20261101000002', '첨부된 와이어프레임 검토 후 회신드리겠습니다.', '07104936', '정재균', '한전 담당자', '07104936', '07104936', '2025-11-16 09:30:00', '2025-11-16 09:30:00');

-- 댓글 (OPN20261101000003 - API 명세)
INSERT IGNORE INTO ictyb_work_opinion_cmnt (CMNT_ID, OPN_ID, CMNT_CTT, WRTR_EMPNO, WRTR_NM, WRTR_ROLE_NM, FRST_REGR_EMPNO, LST_CHGR_EMPNO, FRST_REG_DT, LST_CHG_DT) VALUES
('CMT20261101000004', 'OPN20261101000003', '현재 명세서 v2.1 기준으로 개발 진행 중입니다.', '300007', '신희만', '실무자', '300007', '300007', '2025-11-20 11:00:00', '2025-11-20 11:00:00'),
('CMT20261101000005', 'OPN20261101000003', '해당 버전으로 확인했습니다. 추가 변경 시 사전 공유 부탁드립니다.', '962332', '이인호', '한전 담당자', '962332', '962332', '2025-11-21 14:00:00', '2025-11-21 14:00:00');


-- ════════════════════════════════════════════════════════════════
-- 결재 기능 테스트용 (2건): 8단계 진행코드(104→106→107→108→109→110→111→114)를
-- 처음부터/중간부터 각각 밟아볼 수 있도록 만든 전용 픽스처.
-- APR0001: 방금 작성되어 106(한전 파트장 승인) 대기 중 → 승인을 끝까지 밟아보는 용도
-- APR0002: 109(결과보고)까지 끝나 110(KDN 파트장 검토) 대기 중 → 반송(반려) 테스트 용도
-- ════════════════════════════════════════════════════════════════
INSERT IGNORE INTO its_it_work_report (
    INST_ID, REQ_ID, CHANGE_TITLE, ACT_ID,
    WORK_START_DT, EXPECTED_FINISHED_DT,
    WORKER_SABUN, WORKER_NAME, WORKER_DEP_CD,
    REG_USER_SABUN, REG_USER_NAME, REG_USER_DEP_CD, REG_USER_DEP_NM, REG_DT,
    SERVICE_TYPE, WORK_TYPE, WORK_LEVEL
) VALUES
('APR0001', 'APR0001', '[결재테스트] 한전ON 사용자 메뉴 개선', '104',
 '20260701090000', '20260801235959',
 NULL, NULL, '9611175',
 '19109330', '배강우', 'KP_YBDS', '영업배전시스템실', '20260701090000', '01', '01', '중'),

('APR0002', 'APR0002', '[결재테스트] 한전ON 요금조회 화면 오류 수정', '109',
 '20260620090000', '20260715235959',
 '212102', '하수연', '9611175',
 '19109330', '배강우', 'KP_YBDS', '영업배전시스템실', '20260620090000', '01', '01', '하');

INSERT IGNORE INTO its_work_history (INST_ID, SEQ, ACT_ID, ACT_ID_NM, ACT_SIGN, REG_SABUN, REG_NAME, REG_DT) VALUES
('APR0001', '1', '104', '지시서 작성', 'S', '19109330', '배강우', '20260701090000'),

('APR0002', '1', '104', '지시서 작성',   'S', '19109330', '배강우', '20260620090000'),
('APR0002', '2', '106', '지시서 승인',   'S', '07104936', '정재균', '20260620110000'),
('APR0002', '3', '108', '지시서 배부',   'S', '962332',   '이인호', '20260620130000'),
('APR0002', '4', '109', '결과 보고', 'S', '212102',   '하수연', '20260621090000');

-- 현재결재자(대기중인 다음 단계 담당자)
INSERT IGNORE INTO its_n_sign (INST_ID, SABUN, ACT_ID, NAME) VALUES
('APR0001', '07104936', '106', '정재균'),
('APR0002', '953352', '110', '박은석');