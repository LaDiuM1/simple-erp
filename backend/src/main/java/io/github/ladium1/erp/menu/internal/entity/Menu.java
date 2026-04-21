package io.github.ladium1.erp.menu.internal.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "menus")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,
            unique = true,
            comment = "메뉴 코드")
    private String code;

    @Column(nullable = false,
            comment = "메뉴명")
    private String name;

    @Column(comment = "접근 관리 주소")
    private String url;

    @Column(comment = "메뉴 순서, 부모 기준으로 1부터 시작")
    private int sortOrder;

    @Column(comment = "계층 레벨, 1부터 시작")
    private int level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Menu parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Menu> children = new ArrayList<>();

    // 계층 레벨 자동 계산
    @PrePersist
    @PreUpdate
    private void calculateLevel() {
        // 부모가 없으면 1레벨, 있으면 부모 레벨 + 1
        this.level = (this.parent == null) ? 1 : this.parent.getLevel() + 1;
    }

}