package io.github.ladium1.erp.members.internal.entity;

import io.github.ladium1.erp.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
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
    // 직책 코드
    @Column(nullable = false, unique = true)
    private String code;
    // 직책명
    @Column(nullable = false)
    private String name;
    // 직책 서열, 오름차순
    @Column(nullable = false)
    private int rankLevel;

}