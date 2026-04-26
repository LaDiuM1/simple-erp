package io.github.ladium1.erp.coderule.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 시퀀스 초기화 주기.
 */
@Getter
@RequiredArgsConstructor
public enum ResetPolicy {

    NEVER("초기화 없음"),
    YEARLY("매년"),
    MONTHLY("매월"),
    DAILY("매일");

    private final String label;
}
