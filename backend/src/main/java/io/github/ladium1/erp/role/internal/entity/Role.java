package io.github.ladium1.erp.role.internal.entity;

import io.github.ladium1.erp.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
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

    @Column(nullable = false, unique = true,
            comment = "역할 코드")
    private String code;

    @Column(nullable = false,
            comment = "역할명")
    private String name;

    @Column(comment = "역할 설명")
    private String description;

    @Column(nullable = false,
            comment = "시스템 권한 여부 — true 면 삭제/코드 변경/매트릭스 편집 차단 (예: MASTER)")
    private boolean system;

    @Builder
    public Role(String code, String name, String description, boolean system) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.system = system;
    }

    public void update(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
