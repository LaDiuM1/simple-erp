package io.github.ladium1.erp.members.internal.entity;

import io.github.ladium1.erp.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "roles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 역할 코드
    @Column(nullable = false, unique = true)
    private String code;
    // 역할명
    @Column(nullable = false)
    private String name;
    // 역할 설명
    private String description;

}