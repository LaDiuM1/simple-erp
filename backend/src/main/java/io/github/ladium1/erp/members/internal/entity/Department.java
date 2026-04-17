package io.github.ladium1.erp.members.internal.entity;

import io.github.ladium1.erp.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "departments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Department extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 부서 코드
    @Column(nullable = false, unique = true)
    private String code;
    // 부서명
    @Column(nullable = false)
    private String name;
    // 계층형 상위 부서
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Department parent;

}