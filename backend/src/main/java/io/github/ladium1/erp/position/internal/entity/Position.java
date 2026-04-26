package io.github.ladium1.erp.position.internal.entity;

import io.github.ladium1.erp.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "positions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Position extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true,
            comment = "직책 코드")
    private String code;

    @Column(nullable = false,
            comment = "직책명")
    private String name;

    @Column(nullable = false,
            comment = "직책 서열 — 작을수록 상위. 등록 시 자동으로 max+1 부여, 서열 관리 페이지에서 일괄 재계산.")
    private int rankLevel;

    @Column(length = 500,
            comment = "직책 설명")
    private String description;

    @Builder
    Position(String code, String name, int rankLevel, String description) {
        this.code = code;
        this.name = name;
        this.rankLevel = rankLevel;
        this.description = description;
    }

    public void update(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void changeRankLevel(int rankLevel) {
        this.rankLevel = rankLevel;
    }
}
