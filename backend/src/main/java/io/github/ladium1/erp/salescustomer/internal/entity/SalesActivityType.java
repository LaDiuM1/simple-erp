package io.github.ladium1.erp.salescustomer.internal.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SalesActivityType {

    VISIT("방문"),
    CALL("전화"),
    MEETING("미팅"),
    EMAIL("이메일"),
    OTHER("기타");

    private final String description;
}
