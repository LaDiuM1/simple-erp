package io.github.ladium1.erp.role.internal.entity;

import io.github.ladium1.erp.global.jpa.BaseEntity;
import io.github.ladium1.erp.global.menu.Menu;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "role_menus",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_role_menus_role_menu",
                columnNames = {"role_id", "menu_code"}
        ))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoleMenu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false,
            comment = "권한 외래키")
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "menu_code", nullable = false, length = 50,
            comment = "메뉴 코드")
    private Menu menuCode;

    @Column(comment = "읽기 권한")
    private boolean canRead;

    @Column(comment = "쓰기 권한")
    private boolean canWrite;

    @Builder
    public RoleMenu(Role role, Menu menuCode, boolean canRead, boolean canWrite) {
        this.role = role;
        this.menuCode = menuCode;
        this.canRead = canRead;
        this.canWrite = canWrite;
    }

}
