package io.github.ladium1.erp.members.internal.entity;

import io.github.ladium1.erp.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "role_menus")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoleMenu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 권한
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
    // 메뉴 id, 메뉴 수정 시 이벤트 리스너 기반 자동 관리
    @Column(nullable = false)
    private Long menuId;
    // 읽기 권한
    private boolean canRead;
    // 쓰기 권한
    private boolean canWrite;

}