package io.github.ladium1.erp.members.internal.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    // 우편번호
    private String zipCode;
    // 기본 주소
    private String roadAddress;
    // 상세 주소
    private String detailAddress;

    @Builder
    public Address(String zipCode, String roadAddress, String detailAddress) {
        this.zipCode = zipCode;
        this.roadAddress = roadAddress;
        this.detailAddress = detailAddress;
    }
}