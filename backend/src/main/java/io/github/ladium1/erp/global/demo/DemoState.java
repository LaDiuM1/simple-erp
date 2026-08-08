package io.github.ladium1.erp.global.demo;

/** 외부 reset harness 가 소유하는 데모 수명주기 상태. */
public enum DemoState {
    READY,
    RESETTING,
    VERIFYING,
    FAILED
}
