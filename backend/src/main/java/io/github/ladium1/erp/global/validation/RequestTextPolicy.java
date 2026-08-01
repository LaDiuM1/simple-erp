package io.github.ladium1.erp.global.validation;

/** 요청 장문 필드가 DB TEXT 저장 경계를 넘지 않도록 문자 단위 상한을 공유한다. */
public final class RequestTextPolicy {

    public static final int MAX_LONG_TEXT_LENGTH = 4_000;

    private RequestTextPolicy() {
    }
}
