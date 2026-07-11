package io.github.ladium1.erp.global.storage;

import java.util.List;

/**
 * 파일 스토리지 공용 인터페이스.
 * <p>
 * 도메인 모듈은 파일 본체를 직접 다루지 않고 이 API 로 저장/조회하며,
 * 자기 엔티티에는 fileId(Long) 만 보관한다. 구현체는 로컬 디스크 (추후 S3 교체 가능).
 */
public interface FileStorageApi {

    /**
     * 파일 저장 — 메타 + 본체를 함께 기록하고 메타 정보를 반환.
     */
    StoredFileInfo store(String originalName, String contentType, byte[] content, Long uploaderId);

    /**
     * 파일 메타 조회 — 없으면 BusinessException(FILE_NOT_FOUND).
     */
    StoredFileInfo getInfo(Long fileId);

    /**
     * 여러 파일 메타 조회 — 존재하는 것만 반환. 빈 입력은 빈 리스트.
     */
    List<StoredFileInfo> getInfos(List<Long> fileIds);

    /**
     * 파일 본체 로드 — 없으면 BusinessException(FILE_NOT_FOUND).
     */
    byte[] loadContent(Long fileId);

    /**
     * 파일 삭제 — 메타 + 본체 모두 제거. 없으면 무시.
     */
    void delete(Long fileId);
}
