package io.github.ladium1.erp.members.internal.entity;

import io.github.ladium1.erp.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false,
            comment = "권한")
    private Role role;

    // 메뉴 수정 시 이벤트 리스너 기반 자동 관리
    @Column(nullable = false,
            comment = "메뉴 id")
    private Long menuId;

    @Column(comment = "읽기 권한")
    private boolean canRead;

    @Column(comment = "쓰기 권한")
    private boolean canWrite;

    @Builder
    public RoleMenu(Role role, Long menuId, boolean canRead, boolean canWrite) {
        this.role = role;
        this.menuId = menuId;
        this.canRead = canRead;
        this.canWrite = canWrite;
    }

}