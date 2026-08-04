package io.github.ladium1.erp.global.storage.internal.entity;

/** 저장 파일의 생명주기. */
public enum StoredFileStatus {

    /** 업로드됐지만 아직 업무 레코드에 연결되지 않은 상태. */
    PENDING,

    /** 하나의 업무 레코드가 독점 소유하는 상태. */
    CLAIMED,

    /** 업무 연결은 해제됐고 물리 파일 정리를 기다리는 상태. */
    DELETE_PENDING
}
