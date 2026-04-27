package io.github.ladium1.erp.salescontact.internal.entity;

import io.github.ladium1.erp.global.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 영업 명부 — 외부인 명함 정보 마스터.
 * <p>
 * 회사 / 직책 같은 소속 정보는 SalesContactEmployment 의 활성 row 로 관리. 만난 경로는
 * SalesContactSource 정션을 통해 acquisitionsource 모듈의 AcquisitionSource 와 N:N. 본 엔티티는 개인 자체 정보만.
 */
@Entity
@Getter
@Table(name = "sales_contacts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SalesContact extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, comment = "이름")
    private String name;

    @Column(comment = "영문명")
    private String nameEn;

    @Column(unique = true, length = 30, comment = "휴대폰 — 명부 식별 고유 키")
    private String mobilePhone;

    @Column(comment = "전화번호")
    private String officePhone;

    @Column(comment = "회사 이메일")
    private String email;

    @Column(comment = "개인 이메일")
    private String personalEmail;

    @Column(comment = "최초 미팅일짜")
    private LocalDate metAt;

    @Column(columnDefinition = "TEXT", comment = "비고")
    private String note;

    @Builder
    SalesContact(String name,
                 String nameEn,
                 String mobilePhone,
                 String officePhone,
                 String email,
                 String personalEmail,
                 LocalDate metAt,
                 String note) {
        this.name = name;
        this.nameEn = nameEn;
        this.mobilePhone = mobilePhone;
        this.officePhone = officePhone;
        this.email = email;
        this.personalEmail = personalEmail;
        this.metAt = metAt;
        this.note = note;
    }

    public void update(String name,
                       String nameEn,
                       String mobilePhone,
                       String officePhone,
                       String email,
                       String personalEmail,
                       LocalDate metAt,
                       String note) {
        this.name = name;
        this.nameEn = nameEn;
        this.mobilePhone = mobilePhone;
        this.officePhone = officePhone;
        this.email = email;
        this.personalEmail = personalEmail;
        this.metAt = metAt;
        this.note = note;
    }
}
