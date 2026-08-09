package io.github.ladium1.erp.global.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

    HttpStatus getStatus();
    String getMessage();

    /**
     * 클라이언트가 분기할 수 있는 안정된 오류 코드.
     * 기존 오류는 null 을 유지해 wire contract 를 깨지 않는다.
     */
    default String getCode() {
        return null;
    }

}
