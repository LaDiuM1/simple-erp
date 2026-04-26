package io.github.ladium1.erp.customer.internal.entity;

import io.github.ladium1.erp.global.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Table(name = "customers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Customer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true,
            comment = "고객사 코드")
    private String code;

    @Column(nullable = false,
            comment = "고객사명")
    private String name;

    @Column(comment = "영문 고객사명")
    private String nameEn;

    @Column(unique = true,
            comment = "사업자등록번호")
    private String bizRegNo;

    @Column(comment = "법인등록번호")
    private String corpRegNo;

    @Column(comment = "대표자명")
    private String representative;

    @Column(comment = "업태")
    private String bizType;

    @Column(comment = "업종")
    private String bizItem;

    @Column(comment = "대표 전화")
    private String phone;

    @Column(comment = "팩스")
    private String fax;

    @Column(comment = "대표 이메일")
    private String email;

    @Column(comment = "홈페이지")
    private String website;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,
            comment = "고객 분류")
    private CustomerType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,
            comment = "거래 상태")
    private CustomerStatus status;

    @Column(comment = "거래 시작일")
    private LocalDate tradeStartDate;

    @Column(columnDefinition = "TEXT", comment = "비고")
    private String note;

    @Builder
    Customer(String code,
             String name,
             String nameEn,
             String bizRegNo,
             String corpRegNo,
             String representative,
             String bizType,
             String bizItem,
             String phone,
             String fax,
             String email,
             String website,
             Address address,
             CustomerType type,
             CustomerStatus status,
             LocalDate tradeStartDate,
             String note) {
        this.code = code;
        this.name = name;
        this.nameEn = nameEn;
        this.bizRegNo = bizRegNo;
        this.corpRegNo = corpRegNo;
        this.representative = representative;
        this.bizType = bizType;
        this.bizItem = bizItem;
        this.phone = phone;
        this.fax = fax;
        this.email = email;
        this.website = website;
        this.address = address;
        this.type = type;
        this.status = status;
        this.tradeStartDate = tradeStartDate;
        this.note = note;
    }

    public void update(String name,
                       String nameEn,
                       String bizRegNo,
                       String corpRegNo,
                       String representative,
                       String bizType,
                       String bizItem,
                       String phone,
                       String fax,
                       String email,
                       String website,
                       Address address,
                       CustomerType type,
                       CustomerStatus status,
                       LocalDate tradeStartDate,
                       String note) {
        this.name = name;
        this.nameEn = nameEn;
        this.bizRegNo = bizRegNo;
        this.corpRegNo = corpRegNo;
        this.representative = representative;
        this.bizType = bizType;
        this.bizItem = bizItem;
        this.phone = phone;
        this.fax = fax;
        this.email = email;
        this.website = website;
        this.address = address;
        this.type = type;
        this.status = status;
        this.tradeStartDate = tradeStartDate;
        this.note = note;
    }
}
