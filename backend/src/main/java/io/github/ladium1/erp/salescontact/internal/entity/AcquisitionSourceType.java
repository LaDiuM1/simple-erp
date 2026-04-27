package io.github.ladium1.erp.salescontact.internal.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 컨택 경로 분류.
 */
@Getter
@RequiredArgsConstructor
public enum AcquisitionSourceType {

    EXHIBITION("전시회"),
    REFERRAL("소개"),
    WEB("웹·인터넷"),
    OTHER("기타");

    private final String description;
}
