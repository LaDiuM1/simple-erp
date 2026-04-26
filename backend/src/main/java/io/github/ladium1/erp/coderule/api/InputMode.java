package io.github.ladium1.erp.coderule.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 코드 입력 방식.
 */
@Getter
@RequiredArgsConstructor
public enum InputMode {

    AUTO("자동", "시스템이 패턴에 따라 코드를 생성한다."),
    MANUAL("수동", "사용자가 직접 코드를 입력한다. 패턴 검증을 통과해야 한다."),
    AUTO_OR_MANUAL("선택", "사용자가 자동/수동 중 선택한다.");

    private final String label;
    private final String description;
}
