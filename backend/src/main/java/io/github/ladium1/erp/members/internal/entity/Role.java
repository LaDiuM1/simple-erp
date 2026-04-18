package io.github.ladium1.erp.members.internal.entity;

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

    @Builder
    public Role(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

}