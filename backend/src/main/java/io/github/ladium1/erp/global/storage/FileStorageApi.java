package io.github.ladium1.erp.global.storage;

import java.util.List;
import java.util.Map;

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
    StoredFileInfo getInfo(Long fileId, FileOwner expectedOwner);

    /**
     * 같은 업무 소유자의 여러 파일 메타 조회. 하나라도 소유권이 다르면 FILE_NOT_FOUND.
     */
    List<StoredFileInfo> getInfos(List<Long> fileIds, FileOwner expectedOwner);

    /**
     * 서로 다른 소유자의 파일을 한 번에 조회한다. 키는 파일 ID, 값은 각 파일에 기대하는 소유자다.
     */
    Map<Long, StoredFileInfo> getInfos(Map<Long, FileOwner> expectedOwners);

    /**
     * 파일 본체 로드 — 파일이 없거나 기대 소유자와 다르면 FILE_NOT_FOUND.
     */
    byte[] loadContent(Long fileId, FileOwner expectedOwner);

    /**
     * 업로더 본인의 미연결 파일을 하나의 업무 소유자에 귀속한다.
     */
    void claim(List<Long> fileIds, FileOwner owner, Long uploaderId);

    /**
     * 업무 연결 해제와 같은 트랜잭션에서 삭제를 예약한다. 물리 파일은 별도 정리 주기에 제거한다.
     */
    void requestDeletion(List<Long> fileIds, FileOwner owner);
}
