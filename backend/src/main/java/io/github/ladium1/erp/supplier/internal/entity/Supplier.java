package io.github.ladium1.erp.supplier.internal.entity;

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

@Entity
@Getter
@Table(name = "suppliers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Supplier extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true,
            comment = "공급사명 (영문 표준 표기)")
    private String name;

    @Column(comment = "공급사명 (한글 표기)")
    private String nameKo;

    @Column(comment = "국가")
    private String country;

    @Column(columnDefinition = "TEXT", comment = "비고")
    private String note;

    @Column(nullable = false,
            comment = "사용 여부 — 거래 중단 공급사 숨김용")
    private boolean active;

    @Builder
    Supplier(String name, String nameKo, String country, String note, boolean active) {
        this.name = name;
        this.nameKo = nameKo;
        this.country = country;
        this.note = note;
        this.active = active;
    }

    public void update(String name, String nameKo, String country, String note, boolean active) {
        this.name = name;
        this.nameKo = nameKo;
        this.country = country;
        this.note = note;
        this.active = active;
    }
}
